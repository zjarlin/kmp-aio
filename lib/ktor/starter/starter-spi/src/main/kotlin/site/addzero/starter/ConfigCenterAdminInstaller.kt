package site.addzero.starter

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import site.addzero.configcenter.configCenterAdminSettings
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.installConfigCenterAdmin
import site.addzero.configcenter.JdbcConfigCenterValueService

fun Application.installConfigCenterAdminIfEnabled(
    config: ApplicationConfig = effectiveConfig(),
) {
    val jdbcSettings = config.configCenterJdbcSettingsOrNull() ?: return
    installConfigCenterAdmin(
        service = JdbcConfigCenterValueService(jdbcSettings),
        adminSettings = config.configCenterAdminSettings(),
    )
}
