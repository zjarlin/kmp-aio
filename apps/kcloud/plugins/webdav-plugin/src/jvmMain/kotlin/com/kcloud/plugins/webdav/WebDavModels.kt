package com.kcloud.plugins.webdav

import kotlinx.serialization.Serializable

@Serializable
data class WebDavConnectionConfig(
    val baseUrl: String = "",
    val username: String = "",
    val password: String = "",
    val basePath: String = "/"
)

@Serializable
data class WebDavEntry(
    val name: String,
    val path: String,
    val directory: Boolean,
    val size: Long,
    val modifiedAt: Long
)

@Serializable
data class WebDavActionResult(
    val success: Boolean,
    val message: String
)

@Serializable
data class WebDavPathRequest(
    val path: String
)

interface WebDavWorkspaceService {
    fun loadSettings(): WebDavConnectionConfig
    fun saveSettings(settings: WebDavConnectionConfig): WebDavConnectionConfig
    fun testConnection(settings: WebDavConnectionConfig): WebDavActionResult
    fun listDirectory(path: String): List<WebDavEntry>
    fun createDirectory(path: String): WebDavActionResult
    fun deletePath(path: String): WebDavActionResult
}
