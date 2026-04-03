package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProbeSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProbesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.stm32.bootloader.StLinkConfig
import site.addzero.stm32.bootloader.StLinkProbeInfo
import site.addzero.stm32.bootloader.Stm32FlashRequest
import site.addzero.stm32.bootloader.Stm32FlashStage
import site.addzero.stm32.bootloader.Stm32StLinkProgrammer
import java.io.File
import java.time.Instant
import kotlin.math.roundToInt

@Single
class McuFlashService(
    private val sessionService: McuConsoleSessionService,
    private val profileCatalog: McuFlashProfileCatalog,
) {
    private val lock = Any()
    private var status = McuFlashStatusResponse()

    fun listProfiles(): McuFlashProfilesResponse {
        return profileCatalog.listProfiles()
    }

    fun listProbes(): McuFlashProbesResponse {
        return McuFlashProbesResponse(
            items = Stm32StLinkProgrammer.listProbes().map { probe ->
                McuFlashProbeSummary(
                    serialNumber = probe.serialNumber,
                    productName = probe.productName,
                    manufacturerName = probe.manufacturerName,
                    vendorId = probe.vendorId,
                    productId = probe.productId,
                )
            },
        )
    }

    suspend fun flash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse = withContext(Dispatchers.IO) {
        val profile = profileCatalog.resolve(request.profileId)
        val artifact = resolveArtifactFile(request.firmwarePath)
        val startAddress = request.startAddress ?: profile.defaultStartAddress
        val firmwareBytes = artifact.readBytes()

        updateStatus(
            state = McuFlashRunState.RUNNING,
            profile = profile,
            probe = null,
            targetChipId = null,
            targetVoltageMillivolts = null,
            flashStartAddress = startAddress,
            firmwarePath = artifact.absolutePath,
            bytesSent = 0,
            totalBytes = firmwareBytes.size,
            progressPercent = 0.0,
            lastMessage = "准备通过 ST-Link 连接目标",
        )

        sessionService.closeSession("SWD 烧录前关闭串口会话")
        sessionService.appendEvent(
            kind = McuEventKind.FLASH,
            title = "开始烧录",
            message = profile.title,
            raw = artifact.absolutePath,
        )

        try {
            val config = StLinkConfig(
                serialNumber = request.probeSerialNumber?.trim()?.takeIf { it.isNotBlank() },
                connectUnderReset = profile.connectUnderReset,
            )
            Stm32StLinkProgrammer(config).use { programmer ->
                val targetInfo = programmer.connectTarget()
                if (profile.supportedChipIds.isNotEmpty() && targetInfo.chipId !in profile.supportedChipIds) {
                    val supportedChipIds = profile.supportedChipIds.joinToString { chipId ->
                        "0x${chipId.toString(16).uppercase()}"
                    }
                    throw IllegalStateException(
                        "当前配置仅支持 chipId=$supportedChipIds，检测到 0x${targetInfo.chipId.toString(16).uppercase()}",
                    )
                }
                val geometry = programmer.readFlashGeometry(targetInfo)
                updateStatus(
                    state = McuFlashRunState.RUNNING,
                    profile = profile,
                    probe = targetInfo.probe,
                    targetChipId = targetInfo.chipId,
                    targetVoltageMillivolts = targetInfo.targetVoltageMillivolts,
                    flashStartAddress = startAddress,
                    firmwarePath = artifact.absolutePath,
                    bytesSent = 0,
                    totalBytes = firmwareBytes.size,
                    progressPercent = 0.0,
                    lastMessage = "已连接 ${probeLabel(targetInfo.probe)} / chipId=0x${targetInfo.chipId.toString(16).uppercase()}",
                )
                sessionService.appendEvent(
                    kind = McuEventKind.FLASH,
                    title = "目标已连接",
                    message = "chipId=0x${targetInfo.chipId.toString(16).uppercase()} / ${geometry.flashSizeBytes / 1024}KB Flash",
                    raw = probeLabel(targetInfo.probe),
                )

                var lastStage: Stm32FlashStage? = null
                var lastPercentBucket = -1
                programmer.flash(
                    request = Stm32FlashRequest(
                        startAddress = startAddress,
                        firmware = firmwareBytes,
                    ),
                    targetInfo = targetInfo,
                    geometry = geometry,
                    progressListener = { progress ->
                        val bytesSent = when (progress.stage) {
                            Stm32FlashStage.WRITING -> progress.stageCompletedBytes.toInt()
                            Stm32FlashStage.VERIFYING,
                            Stm32FlashStage.STARTING_APPLICATION,
                            Stm32FlashStage.COMPLETED -> firmwareBytes.size

                            else -> 0
                        }
                        updateStatus(
                            state = McuFlashRunState.RUNNING,
                            profile = profile,
                            probe = targetInfo.probe,
                            targetChipId = targetInfo.chipId,
                            targetVoltageMillivolts = targetInfo.targetVoltageMillivolts,
                            flashStartAddress = startAddress,
                            firmwarePath = artifact.absolutePath,
                            bytesSent = bytesSent,
                            totalBytes = firmwareBytes.size,
                            progressPercent = progress.overallPercent,
                            lastMessage = progress.message,
                        )
                        val currentBucket = (progress.overallPercent / 10.0).toInt()
                        if (progress.stage != lastStage || currentBucket > lastPercentBucket) {
                            lastStage = progress.stage
                            lastPercentBucket = currentBucket
                            sessionService.appendEvent(
                                kind = McuEventKind.FLASH,
                                title = "烧录进度",
                                message = "${progress.stage.name} ${progress.overallPercent.roundToInt()}%",
                                raw = progress.message,
                            )
                        }
                    },
                )
            }

            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "烧录完成",
                message = artifact.name,
            )
            synchronized(lock) {
                status = status.copy(
                    state = McuFlashRunState.SUCCESS,
                    bytesSent = firmwareBytes.size,
                    totalBytes = firmwareBytes.size,
                    progressPercent = 100.0,
                    lastMessage = "SWD 烧录完成",
                    updatedAt = Instant.now().toString(),
                )
                return@withContext status
            }
        } catch (error: Throwable) {
            sessionService.appendEvent(
                kind = McuEventKind.ERROR,
                title = "烧录失败",
                message = error.message ?: "未知错误",
                raw = artifact.absolutePath,
            )
            synchronized(lock) {
                status = status.copy(
                    state = McuFlashRunState.ERROR,
                    lastMessage = error.message ?: "烧录失败",
                    updatedAt = Instant.now().toString(),
                )
            }
            throw error
        }
    }

    suspend fun reset(
        request: McuResetRequest,
        profileId: String?,
        probeSerialNumber: String?,
    ): McuFlashStatusResponse = withContext(Dispatchers.IO) {
        val profile = profileCatalog.resolve(profileId)
        val config = StLinkConfig(
            serialNumber = probeSerialNumber?.trim()?.takeIf { it.isNotBlank() },
            connectUnderReset = profile.connectUnderReset,
        )
        try {
            val nextStatus = Stm32StLinkProgrammer(config).use { programmer ->
                programmer.pulseReset(lowTimeMs = request.pulseMs.toLong())
                val probe = programmer.probeInfo
                sessionService.appendEvent(
                    kind = McuEventKind.FLASH,
                    title = "设备复位",
                    message = "ST-Link NRST ${request.pulseMs}ms",
                    raw = probeLabel(probe),
                )
                synchronized(lock) {
                    status = status.copy(
                        profileId = profile.id,
                        profileTitle = profile.title,
                        runtimeKind = profile.runtimeKind,
                        strategyKind = profile.strategyKind,
                        probeSerialNumber = probe.serialNumber,
                        probeDescription = probeLabel(probe),
                        progressPercent = if (status.state == McuFlashRunState.SUCCESS) 100.0 else status.progressPercent,
                        lastMessage = "已通过 ST-Link 复位目标",
                        updatedAt = Instant.now().toString(),
                    )
                    status
                }
            }
            return@withContext nextStatus
        } catch (error: Throwable) {
            sessionService.appendEvent(
                kind = McuEventKind.ERROR,
                title = "复位失败",
                message = error.message ?: "未知错误",
            )
            throw error
        }
    }

    fun getStatus(): McuFlashStatusResponse {
        synchronized(lock) {
            return status
        }
    }

    private fun updateStatus(
        state: McuFlashRunState,
        profile: McuFlashProfileSummary,
        probe: StLinkProbeInfo?,
        targetChipId: Int?,
        targetVoltageMillivolts: Int?,
        flashStartAddress: Long?,
        firmwarePath: String,
        bytesSent: Int,
        totalBytes: Int,
        progressPercent: Double,
        lastMessage: String,
    ) {
        synchronized(lock) {
            status = McuFlashStatusResponse(
                state = state,
                profileId = profile.id,
                profileTitle = profile.title,
                runtimeKind = profile.runtimeKind,
                strategyKind = profile.strategyKind,
                probeSerialNumber = probe?.serialNumber,
                probeDescription = probe?.let(::probeLabel),
                targetChipId = targetChipId,
                targetVoltageMillivolts = targetVoltageMillivolts,
                flashStartAddress = flashStartAddress,
                firmwarePath = firmwarePath,
                bytesSent = bytesSent,
                totalBytes = totalBytes,
                progressPercent = progressPercent,
                lastMessage = lastMessage,
                updatedAt = Instant.now().toString(),
            )
        }
    }

    private fun resolveArtifactFile(
        firmwarePath: String,
    ): File {
        val normalized = firmwarePath.trim()
        require(normalized.isNotBlank()) { "firmwarePath is required" }
        val file = File(normalized)
        require(file.isFile) { "固件不存在: $normalized" }
        return file
    }

    private fun probeLabel(
        probe: StLinkProbeInfo,
    ): String {
        return buildString {
            append(probe.productName ?: "ST-Link")
            probe.serialNumber?.takeIf { it.isNotBlank() }?.let { serial ->
                append(" / ")
                append(serial)
            }
            append(" / 0x")
            append(probe.productId.toString(16).uppercase().padStart(4, '0'))
        }
    }
}
