package site.addzero.kcloud.plugins.mcuconsole.routes

import org.springframework.web.bind.annotation.GetMapping
import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbeSummary
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProbesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashResetRequest
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashStatusResponse
import site.addzero.stm32.bootloader.StLinkConfig
import site.addzero.stm32.bootloader.Stm32FlashProgress
import site.addzero.stm32.bootloader.Stm32FlashRequest
import site.addzero.stm32.bootloader.Stm32StLinkProgrammer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

@Single
@RestController
@RequestMapping("/api/mcu/flash")
class FlashController {
    private val statusRef = AtomicReference(McuFlashStatusResponse())
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "mcu-flash-controller").apply {
            isDaemon = true
        }
    }

    @GetMapping("/profiles")
    fun listProfiles(): McuFlashProfilesResponse {
        return McuFlashProfilesResponse(items = profiles.map { it.summary })
    }

    @GetMapping("/probes")
    fun listProbes(): McuFlashProbesResponse {
        return McuFlashProbesResponse(
            items = Stm32StLinkProgrammer.listProbes().map { probe ->
                McuFlashProbeSummary(
                    vendorId = probe.vendorId,
                    productId = probe.productId,
                    serialNumber = probe.serialNumber,
                    productName = probe.productName,
                    manufacturerName = probe.manufacturerName,
                )
            },
        )
    }

    @PostMapping("/start")
    fun startFlash(
        @RequestBody request: McuFlashRequest,
    ): McuFlashStatusResponse {
        val current = statusRef.get()
        if (current.state == McuFlashRunState.RUNNING) {
            return current.copy(
                lastMessage = "已有烧录任务正在执行，请等待完成后再试",
                updatedAt = now(),
            ).also(statusRef::set)
        }
        val profile = resolveProfile(request.profileId)
        val firmwarePath = Path.of(request.firmwarePath.trim())
        require(Files.isRegularFile(firmwarePath)) {
            "固件文件不存在: $firmwarePath"
        }
        val firmware = Files.readAllBytes(firmwarePath)
        val startedAt = now()
        val initial = McuFlashStatusResponse(
            state = McuFlashRunState.RUNNING,
            profileId = profile.summary.id,
            probeSerialNumber = request.probeSerialNumber,
            flashStartAddress = request.startAddress ?: profile.summary.defaultStartAddress,
            firmwarePath = firmwarePath.toString(),
            totalBytes = firmware.size.toLong(),
            lastMessage = "烧录任务已开始",
            startedAt = startedAt,
            updatedAt = startedAt,
        )
        statusRef.set(initial)
        executor.submit {
            runCatching {
                val config = StLinkConfig(
                    serialNumber = request.probeSerialNumber?.takeIf { it.isNotBlank() },
                    connectUnderReset = profile.summary.connectUnderReset,
                )
                Stm32StLinkProgrammer(config).use { programmer ->
                    val targetInfo = programmer.connectTarget()
                    require(targetInfo.chipId in profile.summary.supportedChipIds) {
                        "当前目标 chipId=0x${targetInfo.chipId.toString(16).uppercase()} 不在配置 ${profile.summary.id} 支持列表中"
                    }
                    updateStatus {
                        copy(
                            probeDescription = targetInfo.probe.productName ?: targetInfo.probe.manufacturerName,
                            probeSerialNumber = targetInfo.probe.serialNumber ?: probeSerialNumber,
                            targetChipId = targetInfo.chipId,
                            targetVoltageMillivolts = targetInfo.targetVoltageMillivolts,
                            lastMessage = "已连接目标芯片，开始烧录",
                            updatedAt = now(),
                        )
                    }
                    val report = programmer.flash(
                        request = Stm32FlashRequest(
                            startAddress = request.startAddress ?: profile.summary.defaultStartAddress,
                            firmware = firmware,
                        ),
                        progressListener = { progress ->
                            updateProgress(progress)
                        },
                    )
                    val finishedAt = now()
                    statusRef.set(
                        statusRef.get().copy(
                            state = McuFlashRunState.COMPLETED,
                            probeDescription = report.targetInfo.probe.productName ?: report.targetInfo.probe.manufacturerName,
                            probeSerialNumber = report.targetInfo.probe.serialNumber ?: statusRef.get().probeSerialNumber,
                            targetChipId = report.targetInfo.chipId,
                            targetVoltageMillivolts = report.targetInfo.targetVoltageMillivolts,
                            bytesSent = report.bytesWritten.toLong(),
                            totalBytes = firmware.size.toLong(),
                            progressPercent = 100.0,
                            currentStage = "COMPLETED",
                            verified = report.verified,
                            startedApplication = report.startedApplication,
                            lastMessage = "烧录完成",
                            finishedAt = finishedAt,
                            updatedAt = finishedAt,
                        ),
                    )
                }
            }.onFailure { throwable ->
                val finishedAt = now()
                statusRef.set(
                    statusRef.get().copy(
                        state = McuFlashRunState.ERROR,
                        lastMessage = throwable.message ?: throwable::class.java.simpleName,
                        finishedAt = finishedAt,
                        updatedAt = finishedAt,
                    ),
                )
            }
        }
        return initial
    }

    @GetMapping("/status")
    fun getStatus(): McuFlashStatusResponse {
        return statusRef.get()
    }

    @PostMapping("/reset")
    fun reset(
        @RequestBody request: McuFlashResetRequest,
    ): McuFlashStatusResponse {
        val profile = request.profileId?.let(::resolveProfile) ?: defaultProfile
        val config = StLinkConfig(
            serialNumber = request.probeSerialNumber?.takeIf { it.isNotBlank() },
            connectUnderReset = profile.summary.connectUnderReset,
            resetHoldTimeMs = request.pulseMs.toLong().coerceAtLeast(0),
        )
        Stm32StLinkProgrammer(config).use { programmer ->
            val targetInfo = programmer.connectTarget()
            programmer.pulseReset(lowTimeMs = request.pulseMs.toLong().coerceAtLeast(0))
            val updated = McuFlashStatusResponse(
                state = McuFlashRunState.IDLE,
                profileId = profile.summary.id,
                probeSerialNumber = targetInfo.probe.serialNumber,
                probeDescription = targetInfo.probe.productName ?: targetInfo.probe.manufacturerName,
                targetChipId = targetInfo.chipId,
                targetVoltageMillivolts = targetInfo.targetVoltageMillivolts,
                lastMessage = "已通过 ST-Link 发送复位脉冲",
                updatedAt = now(),
            )
            statusRef.set(updated)
            return updated
        }
    }

    private fun updateProgress(
        progress: Stm32FlashProgress,
    ) {
        updateStatus {
            copy(
                state = McuFlashRunState.RUNNING,
                currentStage = progress.stage.name,
                bytesSent = progress.stageCompletedBytes,
                progressPercent = progress.overallPercent,
                lastMessage = progress.message,
                updatedAt = now(),
            )
        }
    }

    private fun updateStatus(
        transform: McuFlashStatusResponse.() -> McuFlashStatusResponse,
    ) {
        while (true) {
            val current = statusRef.get()
            val next = current.transform()
            if (statusRef.compareAndSet(current, next)) {
                return
            }
        }
    }

    private fun resolveProfile(
        profileId: String,
    ): FlashProfileDefinition {
        return profiles.firstOrNull { it.summary.id == profileId }
            ?: error("未找到烧录配置: $profileId")
    }

    private fun now(): String = Instant.now().toString()

    private companion object {
        private val profiles = listOf(
            FlashProfileDefinition(
                summary = McuFlashProfileSummary(
                    id = "stm32-stlink-swd-f1-hd",
                    title = "STM32 F1 High Density / ST-Link SWD",
                    supportedChipIds = listOf(0x414),
                    artifactHint = "例如 build/firmware.bin",
                ),
            ),
        )

        private val defaultProfile: FlashProfileDefinition
            get() = profiles.first()
    }
}

private data class FlashProfileDefinition(
    val summary: McuFlashProfileSummary,
)
