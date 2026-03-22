package com.kcloud.features.servermanagement

import com.kcloud.model.ServerConfig
import kotlinx.serialization.Serializable

@Serializable
data class ServerManagementMutationResult(
    val success: Boolean,
    val message: String,
    val servers: List<ServerConfig> = emptyList()
)

interface ServerManagementService {
    fun listServers(): List<ServerConfig>
    fun findServer(serverId: String): ServerConfig?
    fun saveServer(server: ServerConfig): ServerManagementMutationResult
    fun deleteServer(serverId: String): ServerManagementMutationResult
}
