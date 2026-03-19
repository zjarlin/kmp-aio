package com.kcloud.features.servermanagement.server

import com.kcloud.model.AppSettings
import com.kcloud.model.ServerConfig
import com.kcloud.model.json
import com.kcloud.feature.KCloudLocalPaths
import com.kcloud.features.servermanagement.ServerManagementMutationResult
import com.kcloud.features.servermanagement.ServerManagementService
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import org.koin.core.annotation.Single

private val serverManagementLogger: Logger = Logger.getLogger(
    "com.kcloud.features.servermanagement.server.ServerManagementServiceImpl"
)

@Single
class ServerManagementServiceImpl : ServerManagementService {
    private val settingsFile = File(KCloudLocalPaths.appSupportDir(), "settings.json").also { file ->
        file.parentFile?.mkdirs()
    }
    private val lock = Any()

    override fun listServers(): List<ServerConfig> {
        return synchronized(lock) {
            loadSettings().servers.sortedBy { server -> server.name.lowercase() }
        }
    }

    override fun findServer(serverId: String): ServerConfig? {
        if (serverId.isBlank()) {
            return null
        }

        return synchronized(lock) {
            loadSettings().servers.firstOrNull { server -> server.id == serverId }
        }
    }

    override fun saveServer(server: ServerConfig): ServerManagementMutationResult {
        if (server.name.isBlank() || server.host.isBlank() || server.username.isBlank()) {
            return ServerManagementMutationResult(
                success = false,
                message = "名称、Host、用户名不能为空",
                servers = listServers()
            )
        }

        return synchronized(lock) {
            val settings = loadSettings()
            val existingServer = settings.servers.firstOrNull { item -> item.id == server.id }
            val normalizedServer = normalizeServer(server, existingServer)
            val nextServers = (
                settings.servers.filterNot { item -> item.id == normalizedServer.id } + normalizedServer
                ).sortedBy { item -> item.name.lowercase() }

            saveSettings(settings.copy(servers = nextServers))

            ServerManagementMutationResult(
                success = true,
                message = "已保存服务器：${normalizedServer.name}",
                servers = nextServers
            )
        }
    }

    override fun deleteServer(serverId: String): ServerManagementMutationResult {
        if (serverId.isBlank()) {
            return ServerManagementMutationResult(
                success = false,
                message = "缺少服务器 ID",
                servers = listServers()
            )
        }

        return synchronized(lock) {
            val settings = loadSettings()
            val targetServer = settings.servers.firstOrNull { server -> server.id == serverId }
                ?: return@synchronized ServerManagementMutationResult(
                    success = false,
                    message = "未找到服务器记录",
                    servers = settings.servers.sortedBy { server -> server.name.lowercase() }
                )

            val nextServers = settings.servers
                .filterNot { server -> server.id == serverId }
                .sortedBy { server -> server.name.lowercase() }

            saveSettings(settings.copy(servers = nextServers))

            ServerManagementMutationResult(
                success = true,
                message = "已删除服务器：${targetServer.name}",
                servers = nextServers
            )
        }
    }

    private fun normalizeServer(
        server: ServerConfig,
        existingServer: ServerConfig?
    ): ServerConfig {
        val normalizedUsername = server.username.trim()

        return server.copy(
            name = server.name.trim(),
            host = server.host.trim(),
            username = normalizedUsername,
            password = server.password?.takeIf { value -> value.isNotBlank() },
            privateKeyPath = server.privateKeyPath?.trim()?.takeIf { value -> value.isNotBlank() },
            passphrase = server.passphrase?.takeIf { value -> value.isNotBlank() },
            remoteRootPath = server.remoteRootPath
                .trim()
                .ifBlank { "/home/$normalizedUsername/kcloud" },
            createdAt = existingServer?.createdAt ?: server.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun loadSettings(): AppSettings {
        return try {
            if (settingsFile.exists()) {
                json.decodeFromString(settingsFile.readText())
            } else {
                AppSettings()
            }
        } catch (throwable: Throwable) {
            serverManagementLogger.log(Level.SEVERE, "Failed to load settings", throwable)
            AppSettings()
        }
    }

    private fun saveSettings(settings: AppSettings) {
        try {
            settingsFile.writeText(json.encodeToString(settings))
        } catch (throwable: Throwable) {
            serverManagementLogger.log(Level.SEVERE, "Failed to save settings", throwable)
            throw throwable
        }
    }
}
