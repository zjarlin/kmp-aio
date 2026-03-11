package com.kcloud.model

/**
 * 主题设置
 */
enum class Theme {
    LIGHT, DARK, SYSTEM
}

/**
 * 服务器配置
 */
data class ServerConfig(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String = ""
)

/**
 * 应用设置
 */
data class AppSettings(
    val theme: Theme = Theme.SYSTEM,
    val servers: List<ServerConfig> = emptyList()
)
