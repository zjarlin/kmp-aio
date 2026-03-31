package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import java.time.Instant
import java.util.*

@Single
class McuRuntimeEnsureService(
    private val bundleCatalog: McuRuntimeBundleCatalog,
    private val assetExtractor: McuRuntimeAssetExtractor,
    private val flashService: McuFlashService,
    private val sessionService: McuConsoleSessionService,
    private val protocolCodec: McuVmProtocolCodec,
) {
    private val lock = Any()
    private var status = McuRuntimeStatusResponse()

    fun listBundles(): McuRuntimeBundlesResponse {
        return bundleCatalog.listBundles()
    }

    fun getStatus(): McuRuntimeStatusResponse {
        synchronized(lock) {
            return status
        }
    }

    suspend fun ensureRuntime(
        request: McuRuntimeEnsureRequest,
    ): McuRuntimeStatusResponse {
        val bundle = bundleCatalog.resolveSummary(request.bundleId)
        val currentSession = sessionService.getSessionSnapshot()
        val currentStatus = getStatus()
        val portPath = currentSession.portPath?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("请先打开串口会话")
        val baudRate = currentSession.baudRate.takeIf { it > 0 } ?: bundle.defaultBaudRate
        val rememberedArtifactPath = currentStatus
            .takeIf { it.bundleId == bundle.bundleId }
            ?.artifactPath

        if (bundle.runtimeKind == McuFlashRuntimeKind.MICROPYTHON && !request.forceReflash) {
            updateStatus(
                bundle = bundle,
                state = McuRuntimeEnsureState.READY,
                baudRate = baudRate,
                artifactPath = rememberedArtifactPath,
                message = "MicroPython 固件不支持 VM 在线探测；如需重刷请在烧录页手动触发",
            )
            return getStatus()
        }

        updateStatus(
            bundle = bundle,
            state = McuRuntimeEnsureState.PROBING,
            baudRate = baudRate,
            artifactPath = rememberedArtifactPath,
            message = "探测 ${bundle.title}",
        )

        if (!request.forceReflash) {
            val probeFrame = probeRuntime()
            if (probeFrame != null) {
                updateStatus(
                    bundle = bundle,
                    state = McuRuntimeEnsureState.READY,
                    baudRate = baudRate,
                    artifactPath = rememberedArtifactPath,
                    message = probeFrame.message ?: "运行时已就绪",
                )
                return getStatus()
            }
        }

        updateStatus(
            bundle = bundle,
            state = McuRuntimeEnsureState.INITIALIZING,
            baudRate = baudRate,
            artifactPath = rememberedArtifactPath,
            message = "刷写 ${bundle.title}",
        )

        val artifactPath = resolveArtifactPathForFlash(bundle)

        try {
            flashService.flash(
                McuFlashRequest(
                    profileId = bundle.defaultFlashProfileId,
                    firmwarePath = artifactPath,
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )
            sessionService.openSession(
                McuSessionOpenRequest(
                    portPath = portPath,
                    baudRate = baudRate,
                ),
            )
            if (bundle.runtimeKind == McuFlashRuntimeKind.MICROPYTHON) {
                updateStatus(
                    bundle = bundle,
                    state = McuRuntimeEnsureState.READY,
                    baudRate = baudRate,
                    artifactPath = artifactPath,
                    message = "MicroPython 固件已刷写；请使用 REPL 或 mpremote 验证设备状态",
                )
                return getStatus()
            }
            val probeFrame = probeRuntime()
            if (probeFrame != null) {
                updateStatus(
                    bundle = bundle,
                    state = McuRuntimeEnsureState.READY,
                    baudRate = baudRate,
                    artifactPath = artifactPath,
                    message = probeFrame.message ?: "运行时已就绪",
                )
            } else {
                updateStatus(
                    bundle = bundle,
                    state = McuRuntimeEnsureState.ERROR,
                    baudRate = baudRate,
                    artifactPath = artifactPath,
                    message = "刷写完成，但未探测到运行时响应",
                )
            }
            return getStatus()
        } catch (throwable: Throwable) {
            updateStatus(
                bundle = bundle,
                state = McuRuntimeEnsureState.ERROR,
                baudRate = baudRate,
                artifactPath = artifactPath,
                message = throwable.message ?: "运行时初始化失败",
            )
            throw throwable
        }
    }

    private suspend fun resolveArtifactPathForFlash(
        bundle: McuRuntimeBundleSummary,
    ): String {
        return try {
            assetExtractor.extractBundle(bundle.bundleId).artifactFile.absolutePath
        } catch (throwable: IllegalArgumentException) {
            if (bundle.runtimeKind != McuFlashRuntimeKind.MICROPYTHON ||
                !throwable.message.orEmpty().contains("占位固件")
            ) {
                throw throwable
            }
            val download = flashService.downloadFirmware(
                McuFlashDownloadRequest(
                    profileId = bundle.defaultFlashProfileId,
                ),
            )
            download.downloadPath
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("MicroPython 在线下载成功，但未返回固件路径")
        }
    }

    private suspend fun probeRuntime(): McuVmIncomingFrame? {
        val frames = Channel<McuVmIncomingFrame>(capacity = Channel.UNLIMITED)
        val registration = sessionService.registerFrameListener { frame ->
            frames.trySendBlocking(frame)
        }
        return try {
            sendProbeCommand(
                requestId = UUID.randomUUID().toString(),
                send = { requestId ->
                    sessionService.sendVmFrame(protocolCodec.buildPingFrame(requestId))
                },
                frames = frames,
                timeoutMs = 700L,
            ) ?: sendProbeCommand(
                requestId = UUID.randomUUID().toString(),
                send = { requestId ->
                    sessionService.sendVmFrame(protocolCodec.buildStatusFrame(requestId))
                },
                frames = frames,
                timeoutMs = 900L,
            )
        } finally {
            registration.close()
            frames.close()
        }
    }

    private suspend fun sendProbeCommand(
        requestId: String,
        send: (String) -> Unit,
        frames: Channel<McuVmIncomingFrame>,
        timeoutMs: Long,
    ): McuVmIncomingFrame? {
        send(requestId)
        return withTimeoutOrNull(timeoutMs) {
            lateinit var matched: McuVmIncomingFrame
            while (true) {
                val frame = frames.receive()
                if (frame.requestId == null || frame.requestId == requestId) {
                    matched = frame
                    break
                }
            }
            matched
        }
    }

    private fun updateStatus(
        bundle: McuRuntimeBundleSummary,
        state: McuRuntimeEnsureState,
        baudRate: Int,
        artifactPath: String?,
        message: String,
    ) {
        synchronized(lock) {
            status = McuRuntimeStatusResponse(
                state = state,
                bundleId = bundle.bundleId,
                bundleTitle = bundle.title,
                runtimeKind = bundle.runtimeKind,
                mcuFamily = bundle.mcuFamily,
                defaultFlashProfileId = bundle.defaultFlashProfileId,
                baudRate = baudRate,
                artifactPath = artifactPath,
                lastMessage = message,
                updatedAt = Instant.now().toString(),
            )
        }
    }
}
