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
    val adminLink = applicationConfig.resolveConfigCenterAdminLinkOrNull(adminSettings)
    return buildString {
        append("配置中心管理页已启用。")
        append("可通过 H5 页面管理 namespace、active、path、value。")
        if (adminLink != null) {
            append("访问入口：")
            append(adminLink)
        } else {
            append("访问路径：")
            append(adminSettings.normalizedPath)
            append("。当前运行时未提供可推导的 host/port，因此未生成绝对访问地址。")
        }
    }
}

internal fun ApplicationConfig.resolveConfigCenterAdminLink(
    adminSettings: ConfigCenterAdminSettings = configCenterAdminSettings(),
): String {
    return resolveConfigCenterAdminLinkOrNull(adminSettings)
        ?: error("缺少配置中心管理页访问地址所需配置：ktor.deployment.host / ktor.deployment.port")
}

internal fun ApplicationConfig.resolveConfigCenterAdminLinkOrNull(
    adminSettings: ConfigCenterAdminSettings = configCenterAdminSettings(),
): String? {
    val normalizedPath = adminSettings.normalizedPath
    val port = propertyOrNull("ktor.deployment.port")
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.toIntOrNull()
        ?: return null
    val host = propertyOrNull("ktor.deployment.host")
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: return null
    return "http://$host:$port$normalizedPath"
}
