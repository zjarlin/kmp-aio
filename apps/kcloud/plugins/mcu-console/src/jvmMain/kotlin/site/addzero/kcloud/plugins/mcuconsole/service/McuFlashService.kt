package site.addzero.kcloud.plugins.mcuconsole.service

import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStrategyKind
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway

class McuFlashService(
    private val gateway: SerialPortGateway,
    private val sessionService: McuConsoleSessionService,
    private val profileCatalog: McuFlashProfileCatalog,
    private val commandRunner: McuFlashCommandRunner,
) {
    private val lock = Any()
    private var status = McuFlashStatusResponse()

    fun listProfiles(): McuFlashProfilesResponse {
        return profileCatalog.listProfiles()
    }

    suspend fun flash(
        request: McuFlashRequest,
    ): McuFlashStatusResponse = withContext(Dispatchers.IO) {
        val profile = profileCatalog.resolve(request.profileId)
        val target = resolveTarget(request, profile)
        val artifact = resolveArtifactFile(request.firmwarePath)
        val commandPreview = resolveCommandPreview(
            request = request,
            profile = profile,
            target = target,
            artifact = artifact,
        )

        updateStatus(
            state = McuFlashRunState.RUNNING,
            profile = profile,
            portPath = target.portPath,
            baudRate = target.baudRate,
            firmwarePath = artifact.absolutePath,
            bytesSent = 0,
            totalBytes = artifact.length().toInt(),
            commandPreview = commandPreview,
            lastMessage = "烧录开始",
        )

        sessionService.closeSession("烧录前关闭当前串口会话")
        sessionService.appendEvent(
            kind = McuEventKind.FLASH,
            title = "开始烧录",
            message = buildString {
                append(profile.title)
                target.portPath?.takeIf { it.isNotBlank() }?.let { portPath ->
                    append(" -> ")
                    append(portPath)
                }
            },
            raw = commandPreview,
        )

        try {
            when (profile.strategyKind) {
                McuFlashStrategyKind.SERIAL_ACK_STREAM -> flashBySerialAck(
                    profile = profile,
                    target = target,
                    artifact = artifact,
                )

                McuFlashStrategyKind.COMMAND_TEMPLATE -> flashByCommand(
                    profile = profile,
                    target = target,
                    artifact = artifact,
                    commandLine = commandPreview
                        ?: throw IllegalArgumentException("烧录命令不能为空"),
                )
            }

            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "烧录完成",
                message = artifact.name,
            )
            updateStatus(
                state = McuFlashRunState.SUCCESS,
                profile = profile,
                portPath = target.portPath,
                baudRate = target.baudRate,
                firmwarePath = artifact.absolutePath,
                bytesSent = artifact.length().toInt(),
                totalBytes = artifact.length().toInt(),
                commandPreview = commandPreview,
                lastMessage = "烧录完成",
            )
            return@withContext getStatus()
        } catch (error: Throwable) {
            sessionService.appendEvent(
                kind = McuEventKind.ERROR,
                title = "烧录失败",
                message = error.message ?: "未知错误",
                raw = commandPreview,
            )
            updateStatus(
                state = McuFlashRunState.ERROR,
                profile = profile,
                portPath = target.portPath,
                baudRate = target.baudRate,
                firmwarePath = artifact.absolutePath,
                bytesSent = getStatus().bytesSent,
                totalBytes = artifact.length().toInt(),
                commandPreview = commandPreview,
                lastMessage = error.message ?: "烧录失败",
            )
            throw error
        }
    }

    fun getStatus(): McuFlashStatusResponse {
        synchronized(lock) {
            return status
        }
    }

    private fun flashBySerialAck(
        profile: McuFlashProfileSummary,
        target: FlashTarget,
        artifact: File,
    ) {
        val portPath = target.portPath
            ?: throw IllegalStateException("串口 Bootloader 模式必须选择串口")
        gateway.openConnection(
            portPath = portPath,
            baudRate = target.baudRate,
        ).use { connection ->
            connection.writeUtf8("START_FLASH\r\n")
            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "烧录握手",
                message = "${profile.title} 已发送 START_FLASH",
            )
            Thread.sleep(500)

            val bytes = artifact.readBytes()
            val progressStep = (bytes.size / 10).coerceAtLeast(1)
            bytes.forEachIndexed { index, byte ->
                connection.writeBytes(byteArrayOf(byte), 1)
                readUntilContains(
                    portPath = portPath,
                    readBlock = { buffer -> connection.read(buffer, timeoutMs = 1200) },
                    expected = "ACK",
                    timeoutMs = 2_000,
                )
                val sent = index + 1
                updateStatus(
                    state = McuFlashRunState.RUNNING,
                    profile = profile,
                    portPath = target.portPath,
                    baudRate = target.baudRate,
                    firmwarePath = artifact.absolutePath,
                    bytesSent = sent,
                    totalBytes = bytes.size,
                    commandPreview = null,
                    lastMessage = "烧录中 $sent/${bytes.size}",
                )
                if (sent == bytes.size || sent % progressStep == 0) {
                    sessionService.appendEvent(
                        kind = McuEventKind.FLASH,
                        title = "烧录进度",
                        message = "$sent / ${bytes.size}",
                    )
                }
            }

            connection.writeUtf8("DONE\r\n")
            val response = readUntilContains(
                portPath = portPath,
                readBlock = { buffer -> connection.read(buffer, timeoutMs = 1200) },
                expected = "SUCCESS",
                timeoutMs = 3_000,
            )
            check(response.contains("SUCCESS")) { "烧录失败: 未收到 SUCCESS" }
        }
    }

    private fun flashByCommand(
        profile: McuFlashProfileSummary,
        target: FlashTarget,
        artifact: File,
        commandLine: String,
    ) {
        sessionService.appendEvent(
            kind = McuEventKind.FLASH,
            title = "执行命令",
            message = profile.title,
            raw = commandLine,
        )
        val result = commandRunner.run(
            commandLine = commandLine,
            workingDirectory = artifact.parentFile,
        )
        if (result.stdout.isNotBlank()) {
            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "命令输出",
                message = profile.title,
                raw = result.stdout.takeLast(1_500),
            )
        }
        if (result.stderr.isNotBlank()) {
            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = if (result.exitCode == 0) "命令告警" else "命令错误",
                message = profile.title,
                raw = result.stderr.takeLast(1_500),
            )
        }
        check(result.exitCode == 0) {
            result.stderr.ifBlank { "烧录命令退出码 ${result.exitCode}" }
        }
        updateStatus(
            state = McuFlashRunState.RUNNING,
            profile = profile,
            portPath = target.portPath,
            baudRate = target.baudRate,
            firmwarePath = artifact.absolutePath,
            bytesSent = artifact.length().toInt(),
            totalBytes = artifact.length().toInt(),
            commandPreview = commandLine,
            lastMessage = "命令执行完成",
        )
    }

    private fun updateStatus(
        state: McuFlashRunState,
        profile: McuFlashProfileSummary,
        portPath: String?,
        baudRate: Int,
        firmwarePath: String,
        bytesSent: Int,
        totalBytes: Int,
        commandPreview: String?,
        lastMessage: String,
    ) {
        synchronized(lock) {
            status = McuFlashStatusResponse(
                state = state,
                profileId = profile.id,
                profileTitle = profile.title,
                runtimeKind = profile.runtimeKind,
                strategyKind = profile.strategyKind,
                portPath = portPath,
                baudRate = baudRate,
                firmwarePath = firmwarePath,
                bytesSent = bytesSent,
                totalBytes = totalBytes,
                commandPreview = commandPreview,
                lastMessage = lastMessage,
                updatedAt = Instant.now().toString(),
            )
        }
    }

    private fun resolveTarget(
        request: McuFlashRequest,
        profile: McuFlashProfileSummary,
    ): FlashTarget {
        val session = sessionService.getSessionSnapshot()
        val portPath = request.portPath?.takeIf { it.isNotBlank() }
            ?: session.portPath
        if (profile.requiresPort && portPath == null) {
            throw IllegalArgumentException("请先选择串口")
        }
        val baudRate = request.baudRate
            ?: session.baudRate.takeIf { it > 0 }
            ?: profile.defaultBaudRate
        return FlashTarget(
            portPath = portPath,
            baudRate = baudRate,
        )
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

    private fun resolveCommandPreview(
        request: McuFlashRequest,
        profile: McuFlashProfileSummary,
        target: FlashTarget,
        artifact: File,
    ): String? {
        if (profile.strategyKind != McuFlashStrategyKind.COMMAND_TEMPLATE) {
            return null
        }
        val template = request.commandTemplate
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: profile.commandTemplate
            ?: throw IllegalArgumentException("烧录命令不能为空")
        return renderTemplate(
            template = template,
            profile = profile,
            target = target,
            artifact = artifact,
        )
    }

    private fun renderTemplate(
        template: String,
        profile: McuFlashProfileSummary,
        target: FlashTarget,
        artifact: File,
    ): String {
        return template
            .replace("{portPath}", target.portPath.orEmpty())
            .replace("{baudRate}", target.baudRate.toString())
            .replace("{firmwarePath}", artifact.absolutePath)
            .replace("{firmwareName}", artifact.name)
            .replace("{firmwareDir}", artifact.parentFile?.absolutePath.orEmpty())
            .replace("{profileId}", profile.id)
            .replace("{runtimeKind}", profile.runtimeKind.name.lowercase())
            .replace("{mcuFamily}", profile.mcuFamily)
    }

    private fun readUntilContains(
        portPath: String,
        readBlock: (ByteArray) -> Int,
        expected: String,
        timeoutMs: Int,
    ): String {
        val startedAt = System.currentTimeMillis()
        val buffer = ByteArray(256)
        val builder = StringBuilder()
        while (System.currentTimeMillis() - startedAt < timeoutMs) {
            val count = readBlock(buffer)
            if (count <= 0) {
                continue
            }
            builder.append(buffer.decodeToString(0, count))
            val content = builder.toString()
            if (content.contains(expected)) {
                return content
            }
        }
        throw IllegalStateException("串口 $portPath 未收到 $expected")
    }

    private data class FlashTarget(
        val portPath: String?,
        val baudRate: Int,
    )
}
