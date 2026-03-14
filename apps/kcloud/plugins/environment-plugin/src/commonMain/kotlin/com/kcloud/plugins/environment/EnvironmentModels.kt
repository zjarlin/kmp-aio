package com.kcloud.plugins.environment

import com.kcloud.plugins.ssh.SshConnectionConfig
import kotlinx.serialization.Serializable

@Serializable
enum class EnvironmentInstallTarget {
    LOCAL,
    SSH
}

@Serializable
enum class EnvironmentPackage {
    JDK17,
    JDK21,
    GIT,
    MYSQL,
    POSTGRESQL,
    REDIS,
    NGINX,
    DOCKER,
    NODEJS,
    PNPM
}

@Serializable
data class EnvironmentSetupSettings(
    val installTarget: EnvironmentInstallTarget = EnvironmentInstallTarget.LOCAL,
    val useSudo: Boolean = true,
    val selectedPackages: List<EnvironmentPackage> = listOf(
        EnvironmentPackage.JDK21,
        EnvironmentPackage.REDIS
    ),
    val sshConfig: SshConnectionConfig = SshConnectionConfig()
)

@Serializable
data class EnvironmentHostInfo(
    val installTarget: EnvironmentInstallTarget,
    val unix: Boolean,
    val osName: String,
    val hostName: String,
    val currentUser: String,
    val workingDirectory: String,
    val packageManager: String,
    val message: String
)

@Serializable
data class EnvironmentExecutionResult(
    val success: Boolean,
    val installTarget: EnvironmentInstallTarget,
    val message: String,
    val script: String = "",
    val output: String = "",
    val hints: List<String> = emptyList(),
    val hostInfo: EnvironmentHostInfo? = null
)

interface EnvironmentSetupService {
    fun loadSettings(): EnvironmentSetupSettings
    fun saveSettings(settings: EnvironmentSetupSettings): EnvironmentSetupSettings
    fun inspectEnvironment(settings: EnvironmentSetupSettings): EnvironmentHostInfo
    fun previewInstall(settings: EnvironmentSetupSettings): EnvironmentExecutionResult
    fun install(settings: EnvironmentSetupSettings): EnvironmentExecutionResult
}
