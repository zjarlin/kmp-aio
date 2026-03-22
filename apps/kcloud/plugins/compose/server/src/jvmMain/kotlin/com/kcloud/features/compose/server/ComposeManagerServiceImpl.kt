package com.kcloud.features.compose.server

import com.kcloud.model.AuthType
import com.kcloud.model.ServerConfig
import com.kcloud.feature.KCloudLocalPaths
import com.kcloud.feature.kcloudJson
import com.kcloud.feature.readKCloudJson
import com.kcloud.feature.writeKCloudJson
import com.kcloud.features.compose.ComposeCommandResult
import com.kcloud.features.compose.ComposeLogsResult
import com.kcloud.features.compose.ComposeManagerService
import com.kcloud.features.compose.ComposeManagerSettings
import com.kcloud.features.compose.ComposeRuntimeInfo
import com.kcloud.features.compose.ComposeServerTarget
import com.kcloud.features.compose.ComposeStackDraft
import com.kcloud.features.compose.ComposeStackStatus
import com.kcloud.features.compose.ComposeStackSummary
import com.kcloud.features.compose.ComposeTargetMode
import com.kcloud.features.compose.DEFAULT_COMPOSE_FILE_NAME
import com.kcloud.features.compose.defaultComposeTemplate
import com.kcloud.features.servermanagement.ServerManagementService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.concurrent.TimeUnit
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.koin.core.annotation.Single

private const val COMPOSE_FEATURE_ID = "compose"
private val SUPPORTED_COMPOSE_FILE_NAMES = listOf(
    DEFAULT_COMPOSE_FILE_NAME,
    "compose.yml",
    "docker-compose.yml"
)

@Single
class ComposeManagerServiceImpl internal constructor(
    private val settingsStore: ComposeSettingsStore,
    private val serverManagementService: ServerManagementService,
    private val commandRunner: ComposeCommandRunner
) : ComposeManagerService {
    override fun loadSettings(): ComposeManagerSettings {
        return normalizeSettings(settingsStore.load())
    }

    override fun saveSettings(settings: ComposeManagerSettings): ComposeManagerSettings {
        val normalized = normalizeSettings(settings)
        settingsStore.save(normalized)
        return normalized
    }

    override fun listServerTargets(): List<ComposeServerTarget> {
        return serverManagementService.listServers().map { server ->
            ComposeServerTarget(
                id = server.id,
                name = server.name,
                host = server.host,
                port = server.port
            )
        }
    }

    override suspend fun inspectRuntime(): ComposeRuntimeInfo = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings)
        if (executionTarget == null) {
            return@withContext ComposeRuntimeInfo(
                success = false,
                hostLabel = "未选择服务器",
                targetMode = settings.targetMode,
                composeCommand = settings.composeCommand,
                message = "远程模式下请先选择一个已保存服务器"
            )
        }

        val result = executeShell(
            executionTarget = executionTarget,
            command = "${fullComposeCommand(settings)} version",
            timeoutSeconds = 30
        )
        ComposeRuntimeInfo(
            success = result.exitCode == 0,
            hostLabel = executionTarget.label,
            targetMode = settings.targetMode,
            composeCommand = settings.composeCommand,
            message = if (result.exitCode == 0) {
                "${executionTarget.label} 的 Compose 环境可用"
            } else {
                result.output.ifBlank { "${executionTarget.label} 的 Compose 命令不可用" }
            },
            versionOutput = result.output
        )
    }

    override suspend fun listStacks(): List<ComposeStackSummary> = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext emptyList()
        scanStackRefs(executionTarget, settings).map { stackRef ->
            summarizeStack(executionTarget, settings, stackRef)
        }
    }

    override suspend fun readStack(name: String): ComposeStackDraft? = withContext(Dispatchers.IO) {
        if (name.isBlank()) {
            return@withContext null
        }

        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext null
        val stackRef = resolveStackRef(executionTarget, settings, name) ?: return@withContext null
        val yaml = readStackContent(executionTarget, stackRef)
        val summary = summarizeStack(executionTarget, settings, stackRef)
        val psResult = readComposePs(executionTarget, settings, stackRef)
        val services = readComposeServices(executionTarget, settings, stackRef)
        ComposeStackDraft(
            name = stackRef.name,
            composeYaml = yaml,
            path = stackRef.path,
            composeFileName = stackRef.composeFileName,
            exists = true,
            status = summary.status,
            services = materializeServiceStatuses(services, psResult.containers),
            message = summary.message
        )
    }

    override fun createDraft(name: String): ComposeStackDraft {
        return ComposeStackDraft(
            name = name,
            composeYaml = defaultComposeTemplate()
        )
    }

    override suspend fun validateDraft(draft: ComposeStackDraft): ComposeCommandResult = withContext(Dispatchers.IO) {
        validateStackName(draft.name)
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext ComposeCommandResult(
            success = false,
            message = "当前目标不可用，请先保存并校验 Compose 目标设置"
        )
        validateComposeContent(executionTarget, settings, draft)
    }

    override suspend fun saveStack(draft: ComposeStackDraft): ComposeCommandResult = withContext(Dispatchers.IO) {
        validateStackName(draft.name)
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext ComposeCommandResult(
            success = false,
            message = "当前目标不可用，请先保存并校验 Compose 目标设置"
        )

        val validation = validateComposeContent(executionTarget, settings, draft)
        if (!validation.success) {
            return@withContext validation
        }

        val stackRef = composeStackRef(settings, draft.name, draft.composeFileName)
        val saveResult = writeStackContent(executionTarget, stackRef, draft.composeYaml)
        if (!saveResult.success) {
            return@withContext saveResult
        }
        ComposeCommandResult(
            success = true,
            message = "已保存栈 ${draft.name}",
            output = validation.output
        )
    }

    override suspend fun upStack(name: String): ComposeCommandResult = runStackCommand(
        name = name,
        command = "up -d --remove-orphans",
        successMessage = "已启动栈 $name"
    )

    override suspend fun downStack(name: String): ComposeCommandResult = runStackCommand(
        name = name,
        command = "down --remove-orphans",
        successMessage = "已停止栈 $name"
    )

    override suspend fun restartStack(name: String): ComposeCommandResult = runStackCommand(
        name = name,
        command = "restart",
        successMessage = "已重启栈 $name"
    )

    override suspend fun pullStack(name: String): ComposeCommandResult = runStackCommand(
        name = name,
        command = "pull",
        successMessage = "已拉取栈 $name 的镜像"
    )

    override suspend fun deleteStack(name: String): ComposeCommandResult = withContext(Dispatchers.IO) {
        validateStackName(name)
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext ComposeCommandResult(
            success = false,
            message = "当前目标不可用，请先保存并校验 Compose 目标设置"
        )
        val stackRef = resolveStackRef(executionTarget, settings, name) ?: composeStackRef(settings, name, settings.composeFileName)

        runCatching {
            executeShell(
                executionTarget,
                stackCommand(settings, stackRef, "down --remove-orphans || true"),
                timeoutSeconds = 300
            )
        }

        val deleteResult = when (executionTarget) {
            is ComposeExecutionTarget.Local -> {
                val directory = File(stackRef.path)
                val deleted = !directory.exists() || directory.deleteRecursively()
                if (deleted) {
                    ShellCommandResult(exitCode = 0, output = "(no output)")
                } else {
                    ShellCommandResult(exitCode = 1, output = "删除栈目录失败：${stackRef.path}")
                }
            }

            is ComposeExecutionTarget.Remote -> executeShell(
                executionTarget,
                "rm -rf ${shellQuote(stackRef.path)}",
                timeoutSeconds = 120
            )
        }

        if (deleteResult.exitCode != 0) {
            return@withContext ComposeCommandResult(
                success = false,
                message = deleteResult.output.ifBlank { "删除栈 $name 失败" },
                output = deleteResult.output
            )
        }

        ComposeCommandResult(
            success = true,
            message = "已删除栈 $name"
        )
    }

    override suspend fun readLogs(name: String, tail: Int): ComposeLogsResult = withContext(Dispatchers.IO) {
        validateStackName(name)
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings)
        if (executionTarget == null) {
            return@withContext ComposeLogsResult(
                stackName = name,
                success = false,
                message = "当前目标不可用，请先保存并校验 Compose 目标设置",
                output = ""
            )
        }

        val stackRef = resolveStackRef(executionTarget, settings, name) ?: return@withContext ComposeLogsResult(
            stackName = name,
            success = false,
            message = "未找到栈 $name",
            output = ""
        )
        val result = executeShell(
            executionTarget,
            stackCommand(settings, stackRef, "logs --no-color --tail ${tail.coerceIn(20, 1000)}"),
            timeoutSeconds = 180
        )
        ComposeLogsResult(
            stackName = name,
            success = result.exitCode == 0,
            message = if (result.exitCode == 0) {
                "已加载栈 $name 的最近日志"
            } else {
                "读取栈 $name 日志失败"
            },
            output = result.output
        )
    }

    private suspend fun runStackCommand(
        name: String,
        command: String,
        successMessage: String
    ): ComposeCommandResult = withContext(Dispatchers.IO) {
        validateStackName(name)
        val settings = loadSettings()
        val executionTarget = resolveExecutionTarget(settings) ?: return@withContext ComposeCommandResult(
            success = false,
            message = "当前目标不可用，请先保存并校验 Compose 目标设置"
        )
        val stackRef = resolveStackRef(executionTarget, settings, name) ?: return@withContext ComposeCommandResult(
            success = false,
            message = "未找到栈 $name"
        )
        val result = executeShell(
            executionTarget,
            stackCommand(settings, stackRef, command),
            timeoutSeconds = if (command.startsWith("pull")) 600 else 300
        )
        ComposeCommandResult(
            success = result.exitCode == 0,
            message = if (result.exitCode == 0) successMessage else result.output.ifBlank { "$name 执行失败" },
            output = result.output
        )
    }

    private fun normalizeSettings(settings: ComposeManagerSettings): ComposeManagerSettings {
        val localStacksPath = settings.localStacksPath.ifBlank { settingsStore.defaultLocalStacksPath() }
        return settings.copy(
            localStacksPath = localStacksPath,
            remoteStacksPath = settings.remoteStacksPath.ifBlank { "/opt/stacks" },
            composeCommand = settings.composeCommand.ifBlank { "docker compose" },
            composeFileName = settings.composeFileName.ifBlank { DEFAULT_COMPOSE_FILE_NAME }
        )
    }

    private fun resolveExecutionTarget(settings: ComposeManagerSettings): ComposeExecutionTarget? {
        return when (settings.targetMode) {
            ComposeTargetMode.LOCAL -> ComposeExecutionTarget.Local
            ComposeTargetMode.SERVER -> {
                val serverId = settings.selectedServerId ?: return null
                val server = serverManagementService.findServer(serverId) ?: return null
                ComposeExecutionTarget.Remote(server)
            }
        }
    }

    private suspend fun scanStackRefs(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings
    ): List<ComposeStackRef> {
        return when (executionTarget) {
            is ComposeExecutionTarget.Local -> {
                val root = File(settings.localStacksPath)
                root.listFiles()
                    .orEmpty()
                    .filter { file -> file.isDirectory }
                    .sortedBy { file -> file.name.lowercase() }
                    .mapNotNull { directory ->
                        resolveLocalComposeFile(directory)?.let { composeFile ->
                            ComposeStackRef(
                                name = directory.name,
                                path = directory.absolutePath,
                                composeFileName = composeFile.name
                            )
                        }
                    }
            }

            is ComposeExecutionTarget.Remote -> {
                val command = buildRemoteScanCommand(settings.remoteStacksPath)
                val result = executeShell(executionTarget, command, timeoutSeconds = 60)
                if (result.exitCode != 0) {
                    return emptyList()
                }
                result.output.lineSequence()
                    .filter { line -> line.isNotBlank() && line != "(no output)" }
                    .mapNotNull { line ->
                        val parts = line.split('\t')
                        if (parts.size != 2) {
                            null
                        } else {
                            ComposeStackRef(
                                name = parts[0],
                                path = "${settings.remoteStacksPath.trimEnd('/')}/${parts[0]}",
                                composeFileName = parts[1]
                            )
                        }
                    }
                    .sortedBy { item -> item.name.lowercase() }
                    .toList()
            }
        }
    }

    private suspend fun resolveStackRef(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings,
        name: String
    ): ComposeStackRef? {
        return scanStackRefs(executionTarget, settings).firstOrNull { item -> item.name == name }
    }

    private suspend fun summarizeStack(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings,
        stackRef: ComposeStackRef
    ): ComposeStackSummary {
        val psResult = readComposePs(executionTarget, settings, stackRef)
        val containers = psResult.containers
        val runningCount = containers.count { container -> container.state.equals("running", ignoreCase = true) }
        val containerCount = containers.size
        val status = when {
            !psResult.success && psResult.output.contains("not found", ignoreCase = true) ->
                ComposeStackStatus.MISSING_DOCKER
            !psResult.success ->
                ComposeStackStatus.INVALID
            containerCount == 0 ->
                ComposeStackStatus.EMPTY
            runningCount == containerCount ->
                ComposeStackStatus.RUNNING
            runningCount > 0 ->
                ComposeStackStatus.PARTIAL
            else ->
                ComposeStackStatus.STOPPED
        }
        return ComposeStackSummary(
            name = stackRef.name,
            path = stackRef.path,
            composeFileName = stackRef.composeFileName,
            status = status,
            runningCount = runningCount,
            containerCount = containerCount,
            message = psResult.output.takeIf { output ->
                !psResult.success && output.isNotBlank()
            }.orEmpty()
        )
    }

    private suspend fun validateComposeContent(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings,
        draft: ComposeStackDraft
    ): ComposeCommandResult {
        val command = "printf %s ${shellQuote(draft.composeYaml)} | ${fullComposeCommand(settings)} -f - config -q"
        val result = executeShell(executionTarget, command, timeoutSeconds = 90)
        return ComposeCommandResult(
            success = result.exitCode == 0,
            message = if (result.exitCode == 0) {
                "Compose 配置校验通过"
            } else {
                result.output.ifBlank { "Compose 配置校验失败" }
            },
            output = result.output
        )
    }

    private fun composeStackRef(
        settings: ComposeManagerSettings,
        name: String,
        composeFileName: String
    ): ComposeStackRef {
        val root = when (settings.targetMode) {
            ComposeTargetMode.LOCAL -> settings.localStacksPath
            ComposeTargetMode.SERVER -> settings.remoteStacksPath
        }.trimEnd('/')
        return ComposeStackRef(
            name = name,
            path = "$root/$name",
            composeFileName = composeFileName.ifBlank { settings.composeFileName }
        )
    }

    private suspend fun readStackContent(
        executionTarget: ComposeExecutionTarget,
        stackRef: ComposeStackRef
    ): String {
        return when (executionTarget) {
            is ComposeExecutionTarget.Local -> File(stackRef.path, stackRef.composeFileName)
                .takeIf { file -> file.exists() }
                ?.readText()
                .orEmpty()
            is ComposeExecutionTarget.Remote -> {
                val result = executeShell(
                    executionTarget,
                    "cat ${shellQuote("${stackRef.path}/${stackRef.composeFileName}")}",
                    timeoutSeconds = 60
                )
                result.output.takeUnless { output -> output == "(no output)" }.orEmpty()
            }
        }
    }

    private suspend fun writeStackContent(
        executionTarget: ComposeExecutionTarget,
        stackRef: ComposeStackRef,
        composeYaml: String
    ): ComposeCommandResult {
        return when (executionTarget) {
            is ComposeExecutionTarget.Local -> {
                val directory = File(stackRef.path)
                directory.mkdirs()
                File(directory, stackRef.composeFileName).writeText(composeYaml)
                ComposeCommandResult(
                    success = true,
                    message = "已保存栈 ${stackRef.name}"
                )
            }

            is ComposeExecutionTarget.Remote -> {
                val result = executeShell(
                    executionTarget,
                    "mkdir -p ${shellQuote(stackRef.path)} && printf %s ${shellQuote(composeYaml)} > ${shellQuote("${stackRef.path}/${stackRef.composeFileName}")}",
                    timeoutSeconds = 60
                )
                ComposeCommandResult(
                    success = result.exitCode == 0,
                    message = if (result.exitCode == 0) {
                        "已保存栈 ${stackRef.name}"
                    } else {
                        result.output.ifBlank { "保存栈 ${stackRef.name} 失败" }
                    },
                    output = result.output
                )
            }
        }
    }

    private suspend fun readComposePs(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings,
        stackRef: ComposeStackRef
    ): ComposePsResult {
        val result = executeShell(
            executionTarget,
            stackCommand(settings, stackRef, "ps -a --format json"),
            timeoutSeconds = 90
        )
        val output = result.output.takeUnless { item -> item == "(no output)" }.orEmpty()
        if (result.exitCode != 0) {
            return ComposePsResult(
                containers = emptyList(),
                success = false,
                output = output
            )
        }
        val containers = parseDockerComposePsContainers(output)
        return ComposePsResult(
            containers = containers,
            success = true,
            output = output
        )
    }

    private suspend fun readComposeServices(
        executionTarget: ComposeExecutionTarget,
        settings: ComposeManagerSettings,
        stackRef: ComposeStackRef
    ): List<String> {
        val result = executeShell(
            executionTarget,
            stackCommand(settings, stackRef, "config --services"),
            timeoutSeconds = 90
        )
        if (result.exitCode != 0) {
            return emptyList()
        }
        return result.output.lineSequence()
            .filter { line -> line.isNotBlank() && line != "(no output)" }
            .map { line -> line.trim() }
            .toList()
    }

    private fun materializeServiceStatuses(
        services: List<String>,
        containers: List<DockerComposePsContainer>
    ) = if (services.isEmpty()) {
        containers
            .groupBy { item -> item.service.ifBlank { item.name } }
            .map { (serviceName, items) ->
                val primary = items.first()
                com.kcloud.features.compose.ComposeServiceStatus(
                    service = serviceName,
                    state = primary.state.ifBlank { "unknown" },
                    health = primary.health.orEmpty(),
                    publishers = primary.publishers.orEmpty().map { publisher -> publisher.displayText() }
                )
            }
    } else {
        services.map { serviceName ->
            val container = containers.firstOrNull { item -> item.service == serviceName }
            com.kcloud.features.compose.ComposeServiceStatus(
                service = serviceName,
                state = container?.state?.ifBlank { "not-created" } ?: "not-created",
                health = container?.health.orEmpty(),
                publishers = container?.publishers.orEmpty().map { publisher -> publisher.displayText() }
            )
        }
    }

    private fun fullComposeCommand(settings: ComposeManagerSettings): String {
        return if (settings.useSudo) {
            "sudo ${settings.composeCommand.trim()}"
        } else {
            settings.composeCommand.trim()
        }
    }

    private fun stackCommand(
        settings: ComposeManagerSettings,
        stackRef: ComposeStackRef,
        command: String
    ): String {
        return "cd ${shellQuote(stackRef.path)} && ${fullComposeCommand(settings)} -f ${shellQuote(stackRef.composeFileName)} $command"
    }

    private suspend fun executeShell(
        executionTarget: ComposeExecutionTarget,
        command: String,
        timeoutSeconds: Long
    ): ShellCommandResult {
        return when (executionTarget) {
            is ComposeExecutionTarget.Local -> commandRunner.executeLocal(command, timeoutSeconds)
            is ComposeExecutionTarget.Remote -> commandRunner.executeRemote(executionTarget.server, command, timeoutSeconds)
        }
    }

    private fun resolveLocalComposeFile(directory: File): File? {
        return SUPPORTED_COMPOSE_FILE_NAMES
            .asSequence()
            .map { fileName -> File(directory, fileName) }
            .firstOrNull { file -> file.exists() }
    }

    private fun buildRemoteScanCommand(rootPath: String): String {
        return """
            ROOT=${shellQuote(rootPath)}
            if [ ! -d "${'$'}ROOT" ]; then
              exit 0
            fi
            for dir in "${'$'}ROOT"/*; do
              [ -d "${'$'}dir" ] || continue
              if [ -f "${'$'}dir/compose.yaml" ]; then
                printf '%s\t%s\n' "$(basename "${'$'}dir")" "compose.yaml"
              elif [ -f "${'$'}dir/compose.yml" ]; then
                printf '%s\t%s\n' "$(basename "${'$'}dir")" "compose.yml"
              elif [ -f "${'$'}dir/docker-compose.yml" ]; then
                printf '%s\t%s\n' "$(basename "${'$'}dir")" "docker-compose.yml"
              fi
            done
        """.trimIndent()
    }

    private fun validateStackName(name: String) {
        require(name.isNotBlank()) { "栈名称不能为空" }
        require('/' !in name && '\\' !in name) { "栈名称不能包含路径分隔符" }
    }
}

internal interface ComposeSettingsStore {
    fun load(): ComposeManagerSettings
    fun save(settings: ComposeManagerSettings)
    fun defaultLocalStacksPath(): String
}

@Single
internal class FileComposeSettingsStore : ComposeSettingsStore {
    private val pluginDirectory = KCloudLocalPaths.featureDir(COMPOSE_FEATURE_ID)
    private val settingsFile = File(pluginDirectory, "settings.json")

    override fun load(): ComposeManagerSettings {
        return readKCloudJson(settingsFile) {
            ComposeManagerSettings(
                localStacksPath = defaultLocalStacksPath()
            )
        }
    }

    override fun save(settings: ComposeManagerSettings) {
        writeKCloudJson(settingsFile, settings)
    }

    override fun defaultLocalStacksPath(): String {
        return File(pluginDirectory, "stacks").absolutePath
    }
}

internal interface ComposeCommandRunner {
    suspend fun executeLocal(command: String, timeoutSeconds: Long): ShellCommandResult
    suspend fun executeRemote(server: ServerConfig, command: String, timeoutSeconds: Long): ShellCommandResult
}

@Single
internal class DefaultComposeCommandRunner : ComposeCommandRunner {
    override suspend fun executeLocal(
        command: String,
        timeoutSeconds: Long
    ): ShellCommandResult = withContext(Dispatchers.IO) {
        val process = ProcessBuilder("/bin/sh", "-lc", command)
            .redirectErrorStream(false)
            .start()
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            return@withContext ShellCommandResult(
                exitCode = -1,
                output = "命令执行超时（${timeoutSeconds}s）"
            )
        }
        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        ShellCommandResult(
            exitCode = process.exitValue(),
            output = combineOutput(stdout, stderr)
        )
    }

    override suspend fun executeRemote(
        server: ServerConfig,
        command: String,
        timeoutSeconds: Long
    ): ShellCommandResult = withContext(Dispatchers.IO) {
        require(server.host.isNotBlank()) { "远程服务器 Host 不能为空" }
        require(server.username.isNotBlank()) { "远程服务器用户名不能为空" }

        val client = SSHClient().apply {
            addHostKeyVerifier(PromiscuousVerifier())
            connect(server.host, server.port)
            when (server.authType) {
                AuthType.PASSWORD -> authPassword(server.username, server.password.orEmpty())
                AuthType.PRIVATE_KEY -> {
                    require(!server.privateKeyPath.isNullOrBlank()) { "远程服务器私钥路径不能为空" }
                    authPublickey(server.username, server.privateKeyPath)
                }
            }
        }

        client.use { ssh ->
            ssh.startSession().use { session ->
                executeSshCommand(session, command, timeoutSeconds)
            }
        }
    }

    private fun executeSshCommand(
        session: Session,
        command: String,
        timeoutSeconds: Long
    ): ShellCommandResult {
        val remoteCommand = session.exec("sh -lc ${shellQuote(command)}")
        remoteCommand.join(timeoutSeconds, TimeUnit.SECONDS)
        val stdout = remoteCommand.inputStream.bufferedReader().readText()
        val stderr = remoteCommand.errorStream.bufferedReader().readText()
        return ShellCommandResult(
            exitCode = remoteCommand.exitStatus ?: -1,
            output = combineOutput(stdout, stderr)
        )
    }
}

internal data class ShellCommandResult(
    val exitCode: Int,
    val output: String
)

private data class ComposePsResult(
    val containers: List<DockerComposePsContainer>,
    val success: Boolean,
    val output: String
)

private data class ComposeStackRef(
    val name: String,
    val path: String,
    val composeFileName: String
)

private sealed interface ComposeExecutionTarget {
    val label: String

    data object Local : ComposeExecutionTarget {
        override val label: String = "本机"
    }

    data class Remote(val server: ServerConfig) : ComposeExecutionTarget {
        override val label: String = server.name.ifBlank { "${server.username}@${server.host}" }
    }
}

@Serializable
private data class DockerComposePsContainer(
    @SerialName("Name")
    val name: String = "",
    @SerialName("Service")
    val service: String = "",
    @SerialName("State")
    val state: String = "",
    @SerialName("Health")
    val health: String? = null,
    @SerialName("Publishers")
    val publishers: List<DockerComposePublisher>? = null
)

@Serializable
private data class DockerComposePublisher(
    @SerialName("URL")
    val url: String? = null,
    @SerialName("PublishedPort")
    val publishedPort: Int? = null,
    @SerialName("TargetPort")
    val targetPort: Int? = null,
    @SerialName("Protocol")
    val protocol: String? = null
) {
    fun displayText(): String {
        val host = url ?: "localhost"
        val published = publishedPort?.toString() ?: "?"
        val target = targetPort?.toString() ?: "?"
        val suffix = protocol?.takeIf { value -> value.isNotBlank() }?.let { value -> "/$value" }.orEmpty()
        return "$host:$published->$target$suffix"
    }
}

private fun combineOutput(stdout: String, stderr: String): String {
    return listOf(stdout.trim(), stderr.trim())
        .filter { item -> item.isNotBlank() }
        .joinToString(separator = "\n")
        .ifBlank { "(no output)" }
}

private fun shellQuote(value: String): String {
    return "'" + value.replace("'", "'\"'\"'") + "'"
}

private fun parseDockerComposePsContainers(output: String): List<DockerComposePsContainer> {
    if (output.isBlank() || output == "(no output)") {
        return emptyList()
    }

    runCatching {
        when (val element = kcloudJson.parseToJsonElement(output)) {
            is JsonArray -> {
                return element.mapNotNull(::toDockerComposePsContainer)
            }

            is JsonObject -> {
                return listOfNotNull(toDockerComposePsContainer(element))
            }

            else -> Unit
        }
    }

    return output.lineSequence()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                null
            } else {
                runCatching {
                    toDockerComposePsContainer(kcloudJson.parseToJsonElement(trimmed))
                }.getOrNull()
            }
        }
        .toList()
}

private fun toDockerComposePsContainer(element: JsonElement): DockerComposePsContainer? {
    val jsonObject = runCatching { element.jsonObject }.getOrNull() ?: return null
    return DockerComposePsContainer(
        name = jsonObject.string("Name"),
        service = jsonObject.string("Service"),
        state = jsonObject.string("State"),
        health = jsonObject["Health"]?.jsonPrimitive?.contentOrNull,
        publishers = jsonObject["Publishers"]
            ?.let { publishers ->
                runCatching { publishers.jsonArray }.getOrNull()
            }
            ?.mapNotNull(::toDockerComposePublisher)
    )
}

private fun toDockerComposePublisher(element: JsonElement): DockerComposePublisher? {
    val jsonObject = runCatching { element.jsonObject }.getOrNull() ?: return null
    return DockerComposePublisher(
        url = jsonObject["URL"]?.jsonPrimitive?.contentOrNull,
        publishedPort = jsonObject["PublishedPort"]?.jsonPrimitive?.intOrNull,
        targetPort = jsonObject["TargetPort"]?.jsonPrimitive?.intOrNull,
        protocol = jsonObject["Protocol"]?.jsonPrimitive?.contentOrNull
    )
}

private fun JsonObject.string(name: String): String {
    return this[name]?.jsonPrimitive?.contentOrNull.orEmpty()
}
