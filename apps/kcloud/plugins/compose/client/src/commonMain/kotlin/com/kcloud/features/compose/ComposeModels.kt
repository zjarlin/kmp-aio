package com.kcloud.features.compose

import kotlinx.serialization.Serializable

@Serializable
enum class ComposeTargetMode {
    LOCAL,
    SERVER
}

@Serializable
data class ComposeManagerSettings(
    val targetMode: ComposeTargetMode = ComposeTargetMode.LOCAL,
    val selectedServerId: String? = null,
    val localStacksPath: String = "",
    val remoteStacksPath: String = "/opt/stacks",
    val composeCommand: String = "docker compose",
    val composeFileName: String = DEFAULT_COMPOSE_FILE_NAME,
    val useSudo: Boolean = false
)

@Serializable
data class ComposeServerTarget(
    val id: String,
    val name: String,
    val host: String,
    val port: Int
)

@Serializable
enum class ComposeStackStatus {
    RUNNING,
    PARTIAL,
    STOPPED,
    EMPTY,
    INVALID,
    MISSING_DOCKER,
    UNKNOWN
}

@Serializable
data class ComposeStackSummary(
    val name: String,
    val path: String,
    val composeFileName: String,
    val status: ComposeStackStatus,
    val runningCount: Int = 0,
    val containerCount: Int = 0,
    val message: String = ""
)

@Serializable
data class ComposeServiceStatus(
    val service: String,
    val state: String,
    val health: String = "",
    val publishers: List<String> = emptyList()
)

@Serializable
data class ComposeStackDraft(
    val name: String = "",
    val composeYaml: String = defaultComposeTemplate(),
    val path: String = "",
    val composeFileName: String = DEFAULT_COMPOSE_FILE_NAME,
    val exists: Boolean = false,
    val status: ComposeStackStatus = ComposeStackStatus.EMPTY,
    val services: List<ComposeServiceStatus> = emptyList(),
    val message: String = ""
)

@Serializable
data class ComposeRuntimeInfo(
    val success: Boolean,
    val hostLabel: String,
    val targetMode: ComposeTargetMode,
    val composeCommand: String,
    val message: String,
    val versionOutput: String = ""
)

@Serializable
data class ComposeCommandResult(
    val success: Boolean,
    val message: String,
    val output: String = ""
)

@Serializable
data class ComposeLogsResult(
    val stackName: String,
    val success: Boolean,
    val message: String,
    val output: String
)

interface ComposeManagerService {
    fun loadSettings(): ComposeManagerSettings
    fun saveSettings(settings: ComposeManagerSettings): ComposeManagerSettings
    fun listServerTargets(): List<ComposeServerTarget>
    suspend fun inspectRuntime(): ComposeRuntimeInfo
    suspend fun listStacks(): List<ComposeStackSummary>
    suspend fun readStack(name: String): ComposeStackDraft?
    fun createDraft(name: String = ""): ComposeStackDraft
    suspend fun validateDraft(draft: ComposeStackDraft): ComposeCommandResult
    suspend fun saveStack(draft: ComposeStackDraft): ComposeCommandResult
    suspend fun upStack(name: String): ComposeCommandResult
    suspend fun downStack(name: String): ComposeCommandResult
    suspend fun restartStack(name: String): ComposeCommandResult
    suspend fun pullStack(name: String): ComposeCommandResult
    suspend fun deleteStack(name: String): ComposeCommandResult
    suspend fun readLogs(name: String, tail: Int = 200): ComposeLogsResult
}

const val DEFAULT_COMPOSE_FILE_NAME = "compose.yaml"

fun defaultComposeTemplate(): String {
    return """
        services:
          app:
            image: nginx:alpine
            ports:
              - "8080:80"
            restart: unless-stopped
    """.trimIndent()
}
