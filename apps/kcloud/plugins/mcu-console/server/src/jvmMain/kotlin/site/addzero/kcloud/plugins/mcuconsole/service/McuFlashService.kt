package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import java.io.File
import java.time.Instant

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

    suspend fun downloadFirmware(
        request: McuFlashDownloadRequest,
    ): McuFlashDownloadResponse = withContext(Dispatchers.IO) {
        val profile = profileCatalog.resolve(request.profileId)
        val requestedUrl = request.downloadUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: profile.defaultDownloadUrl
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("当前烧录能力包没有默认在线固件，请填写下载地址")
        val resolvedUrl = resolveOnlineFirmwareUrl(profile, requestedUrl)
        val targetFile = resolveDownloadTargetFile(profile, resolvedUrl)
        val commandLine = buildCurlDownloadCommand(
            url = resolvedUrl,
            targetFile = targetFile,
        )

        sessionService.appendEvent(
            kind = McuEventKind.FLASH,
            title = "开始下载固件",
            message = profile.title,
            raw = resolvedUrl,
        )

        targetFile.parentFile?.mkdirs()
        targetFile.delete()

        try {
            val result = commandRunner.run(
                commandLine = commandLine,
                workingDirectory = targetFile.parentFile,
            )
            if (result.stdout.isNotBlank()) {
                sessionService.appendEvent(
                    kind = McuEventKind.FLASH,
                    title = "下载输出",
                    message = profile.title,
                    raw = result.stdout.takeLast(1_500),
                )
            }
            if (result.stderr.isNotBlank()) {
                sessionService.appendEvent(
                    kind = if (result.exitCode == 0) McuEventKind.FLASH else McuEventKind.ERROR,
                    title = if (result.exitCode == 0) "下载告警" else "下载错误",
                    message = profile.title,
                    raw = result.stderr.takeLast(1_500),
                )
            }
            check(result.exitCode == 0) {
                result.stderr.ifBlank { "固件下载命令退出码 ${result.exitCode}" }
            }
            require(targetFile.isFile && targetFile.length() > 0L) {
                "固件下载失败: ${targetFile.absolutePath}"
            }
            sessionService.appendEvent(
                kind = McuEventKind.FLASH,
                title = "固件已下载",
                message = targetFile.name,
                raw = targetFile.absolutePath,
            )
            return@withContext McuFlashDownloadResponse(
                profileId = profile.id,
                profileTitle = profile.title,
                runtimeKind = profile.runtimeKind,
                resolvedUrl = resolvedUrl,
                downloadPath = targetFile.absolutePath,
                commandPreview = commandLine,
                lastMessage = "固件已下载到 ${targetFile.absolutePath}",
                updatedAt = Instant.now().toString(),
            )
        } catch (error: Throwable) {
            targetFile.delete()
            sessionService.appendEvent(
                kind = McuEventKind.ERROR,
                title = "固件下载失败",
                message = error.message ?: "未知错误",
                raw = resolvedUrl,
            )
            throw error
        }
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
            .replace("{esptoolCommand}", resolveEsptoolCommand())
            .replace("{portPath}", target.portPath.orEmpty())
            .replace("{baudRate}", target.baudRate.toString())
            .replace("{firmwarePath}", artifact.absolutePath)
            .replace("{firmwareName}", artifact.name)
            .replace("{firmwareDir}", artifact.parentFile?.absolutePath.orEmpty())
            .replace("{profileId}", profile.id)
            .replace("{runtimeKind}", profile.runtimeKind.name.lowercase())
            .replace("{mcuFamily}", profile.mcuFamily)
    }

    private fun resolveEsptoolCommand(): String {
        System.getProperty("kcloud.mcu.esptool.cmd")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val candidates = listOf(
            EsptoolCommandCandidate(
                command = "python3 -m esptool",
                probePosix = "command -v python3 >/dev/null 2>&1 && python3 -m esptool version >/dev/null 2>&1",
                probeWindows = "where python3 >nul 2>nul && python3 -m esptool version >nul 2>nul",
            ),
            EsptoolCommandCandidate(
                command = "python -m esptool",
                probePosix = "command -v python >/dev/null 2>&1 && python -m esptool version >/dev/null 2>&1",
                probeWindows = "where python >nul 2>nul && python -m esptool version >nul 2>nul",
            ),
            EsptoolCommandCandidate(
                command = "esptool.py",
                probePosix = "command -v esptool.py >/dev/null 2>&1",
                probeWindows = "where esptool.py >nul 2>nul",
            ),
            EsptoolCommandCandidate(
                command = "esptool",
                probePosix = "command -v esptool >/dev/null 2>&1",
                probeWindows = "where esptool >nul 2>nul",
            ),
        )
        return candidates.firstOrNull { candidate ->
            val probeCommand = if (isWindows()) candidate.probeWindows else candidate.probePosix
            commandRunner.run(commandLine = probeCommand).exitCode == 0
        }?.command
            ?: throw IllegalStateException(
                "未找到可用的 esptool。请先安装 esptool，或通过 -Dkcloud.mcu.esptool.cmd 指定刷机命令，例如 python3 -m esptool、esptool 或 esptool.py",
            )
    }

    private fun resolveOnlineFirmwareUrl(
        profile: McuFlashProfileSummary,
        requestedUrl: String,
    ): String {
        if (profile.runtimeKind == McuFlashRuntimeKind.MICROPYTHON && !requestedUrl.isDirectFirmwareUrl()) {
            return resolveLatestMicroPythonFirmwareUrl(requestedUrl)
        }
        return requestedUrl
    }

    private fun resolveLatestMicroPythonFirmwareUrl(
        catalogUrl: String,
    ): String {
        val commandLine = "curl -fsSL ${quoteShellArg(catalogUrl)}"
        val result = commandRunner.run(commandLine = commandLine)
        check(result.exitCode == 0) {
            result.stderr.ifBlank { "无法读取 MicroPython 下载页: $catalogUrl" }
        }
        val relativePath = micropythonFirmwareRegex.findAll(result.stdout)
            .map { match -> match.value }
            .firstOrNull { path -> !path.contains(".app-bin") }
            ?: throw IllegalStateException("未在 MicroPython 下载页解析到可用固件: $catalogUrl")
        return when {
            relativePath.startsWith("http://") || relativePath.startsWith("https://") -> relativePath
            relativePath.startsWith("/") -> "https://micropython.org$relativePath"
            else -> "https://micropython.org/$relativePath"
        }
    }

    private fun resolveDownloadTargetFile(
        profile: McuFlashProfileSummary,
        resolvedUrl: String,
    ): File {
        val fileName = resolvedUrl.substringAfterLast('/')
            .substringBefore('?')
            .takeIf { it.isNotBlank() }
            ?.sanitizeFileName()
            ?: "${profile.id}.bin"
        val rootDir = File(
            System.getProperty("user.home"),
            ".kcloud/mcu-downloads/${profile.id}",
        )
        return File(rootDir, fileName)
    }

    private fun buildCurlDownloadCommand(
        url: String,
        targetFile: File,
    ): String {
        return "curl -fL --retry 2 --connect-timeout 15 -o ${quoteShellArg(targetFile.absolutePath)} ${quoteShellArg(url)}"
    }

    private fun quoteShellArg(
        value: String,
    ): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
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

    companion object {
        private val micropythonFirmwareRegex = Regex("""/resources/firmware/[^"\s]+\.bin""")
    }

    private data class EsptoolCommandCandidate(
        val command: String,
        val probePosix: String,
        val probeWindows: String,
    )
}

private fun String.isDirectFirmwareUrl(): Boolean {
    val normalized = substringBefore('?').substringBefore('#')
    return normalized.endsWith(".bin", ignoreCase = true) || normalized.endsWith(".uf2", ignoreCase = true)
}

private fun String.sanitizeFileName(): String {
    return replace(Regex("""[^A-Za-z0-9._-]"""), "_")
}

private fun isWindows(): Boolean {
    return System.getProperty("os.name")
        ?.contains("Windows", ignoreCase = true) == true
}
