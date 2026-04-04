package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Single
import java.util.logging.Logger

private val configCenterAnnouncementLogger: Logger =
    Logger.getLogger(ConfigCenterAdminAnnouncement::class.java.name)

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
    val port = propertyOrNull("ktor.deployment.port")
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.toIntOrNull()
        ?: error("缺少配置中心管理页访问地址所需配置：ktor.deployment.port")
    val host = propertyOrNull("ktor.deployment.host")
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error("缺少配置中心管理页访问地址所需配置：ktor.deployment.host")
    return "http://$host:$port$normalizedPath"
}
