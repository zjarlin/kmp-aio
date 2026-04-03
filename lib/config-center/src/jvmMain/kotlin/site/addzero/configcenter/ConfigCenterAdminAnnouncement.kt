package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Single
import java.util.logging.Logger

private val configCenterAnnouncementLogger: Logger =
    Logger.getLogger(ConfigCenterAdminAnnouncement::class.java.name)

const val CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY: String = "config-center.admin.base-url"

@Single(createdAtStart = true)
class ConfigCenterAdminAnnouncement(
    private val applicationConfig: ApplicationConfig,
) {
    init {
        buildConfigCenterAdminAnnouncement(applicationConfig)?.let(configCenterAnnouncementLogger::info)
    }
}

internal fun buildConfigCenterAdminAnnouncement(
    applicationConfig: ApplicationConfig,
): String? {
    if (applicationConfig.configCenterJdbcSettingsOrNull() == null) {
        return null
    }
    val adminSettings = applicationConfig.configCenterAdminSettings()
    if (!adminSettings.enabled) {
        return null
    }
    val adminLink = applicationConfig.resolveConfigCenterAdminLink(adminSettings)
    return buildString {
        append("配置中心管理页已启用。")
        append("可通过 H5 页面管理配置命名空间、环境、键值和说明等配置元数据。")
        append("访问入口：")
        append(adminLink)
    }
}

internal fun ApplicationConfig.resolveConfigCenterAdminLink(
    adminSettings: ConfigCenterAdminSettings = configCenterAdminSettings(),
): String {
    val normalizedPath = adminSettings.normalizedPath
    val runtimeBaseUrl = System.getProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY)
        ?.trim()
        ?.removeSuffix("/")
        ?.ifBlank { null }
    if (runtimeBaseUrl != null) {
        return "$runtimeBaseUrl$normalizedPath"
    }
    val port = propertyOrNull("ktor.deployment.port")?.getString()?.trim()?.toIntOrNull()
    if (port == null) {
        return normalizedPath
    }
    val host = propertyOrNull("ktor.deployment.host")?.getString()?.trim()?.normalizeConfigCenterAdminHost()
        ?: "127.0.0.1"
    return "http://$host:$port$normalizedPath"
}

private fun String?.normalizeConfigCenterAdminHost(): String {
    val rawValue = this?.trim().orEmpty()
    return when (rawValue) {
        "",
        "0.0.0.0",
        "::",
        "[::]",
        "::0" -> "127.0.0.1"
        else -> rawValue
    }
}
