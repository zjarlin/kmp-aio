package com.kcloud.features.ssh

import kotlinx.serialization.Serializable

@Serializable
enum class SshAuthMode {
    PASSWORD,
    PRIVATE_KEY
}

@Serializable
data class SshConnectionConfig(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String = "",
    val privateKeyPath: String = "",
    val remoteRootPath: String = ".",
    val authMode: SshAuthMode = SshAuthMode.PASSWORD
)

@Serializable
data class SshDirectoryEntry(
    val name: String,
    val path: String,
    val directory: Boolean,
    val size: Long,
    val modifiedAt: Long
)

@Serializable
data class SshActionResult(
    val success: Boolean,
    val message: String
)

@Serializable
data class RemotePathRequest(
    val path: String
)

interface SshWorkspaceService {
    fun loadSettings(): SshConnectionConfig
    fun saveSettings(settings: SshConnectionConfig): SshConnectionConfig
    fun testConnection(settings: SshConnectionConfig): SshActionResult
    fun listDirectory(path: String): List<SshDirectoryEntry>
    fun createDirectory(path: String): SshActionResult
    fun deletePath(path: String): SshActionResult
}
