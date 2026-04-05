package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.configCenterJdbcSettingsOrNull(): ConfigCenterJdbcSettings? {
    val enabled = propertyAsBoolean("config-center.enabled") ?: true
    if (!enabled) {
        return null
    }
    val explicitUrl = propertyOrNull("config-center.jdbc.url")?.getString()?.trim()?.ifBlank { null }
    if (explicitUrl != null) {
        return ConfigCenterJdbcSettings(
            url = explicitUrl,
            username = propertyOrNull("config-center.jdbc.username")?.getString()
                ?: propertyOrNull("config-center.jdbc.user")?.getString(),
            password = propertyOrNull("config-center.jdbc.password")?.getString(),
            driver = propertyOrNull("config-center.jdbc.driver")?.getString()
                ?: ConfigCenterJdbcSettings.inferDriver(explicitUrl),
            autoDdl = propertyAsBoolean("config-center.jdbc.auto-ddl") ?: true,
        )
    }

    val sqliteEnabled = propertyAsBoolean("datasources.sqlite.enabled") == true
    if (sqliteEnabled) {
        val url = propertyOrNull("datasources.sqlite.url")?.getString()?.trim()?.ifBlank { null } ?: return null
        return ConfigCenterJdbcSettings(
            url = url,
            driver = propertyOrNull("datasources.sqlite.driver")?.getString()
                ?: ConfigCenterJdbcSettings.inferDriver(url),
            autoDdl = propertyAsBoolean("config-center.jdbc.auto-ddl") ?: true,
        )
    }

    val postgresEnabled = propertyAsBoolean("datasources.postgres.enabled") == true
    if (postgresEnabled) {
        val url = propertyOrNull("datasources.postgres.url")?.getString()?.trim()?.ifBlank { null } ?: return null
        return ConfigCenterJdbcSettings(
            url = url,
            username = propertyOrNull("datasources.postgres.user")?.getString()
                ?: propertyOrNull("datasources.postgres.username")?.getString(),
            password = propertyOrNull("datasources.postgres.password")?.getString(),
            driver = propertyOrNull("datasources.postgres.driver")?.getString()
                ?: ConfigCenterJdbcSettings.inferDriver(url),
            autoDdl = propertyAsBoolean("config-center.jdbc.auto-ddl") ?: true,
        )
    }

    return null
}

fun ApplicationConfig.configCenterAdminSettings(): ConfigCenterAdminSettings {
    return ConfigCenterAdminSettings(
        enabled = propertyAsBoolean("config-center.admin.enabled") ?: true,
        path = propertyOrNull("config-center.admin.path")?.getString() ?: "/config-center",
        title = propertyOrNull("config-center.admin.title")?.getString() ?: "Config Center",
    )
}

private fun ApplicationConfig.propertyAsBoolean(
    path: String,
): Boolean? {
    return propertyOrNull(path)?.getString()?.trim()?.lowercase()?.let { value ->
        when (value) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}
