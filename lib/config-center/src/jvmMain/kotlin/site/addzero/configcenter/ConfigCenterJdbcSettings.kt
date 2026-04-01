package site.addzero.configcenter

import io.ktor.server.config.ApplicationConfig

private const val CONFIG_CENTER_TABLE_NOTE = "来自config-center中间件请勿删除!"

data class ConfigCenterJdbcSettings(
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val driver: String? = ConfigCenterJdbcSettings.inferDriver(url),
    val autoDdl: Boolean = true,
    val tableNote: String = CONFIG_CENTER_TABLE_NOTE,
) {
    companion object {
        fun inferDriver(
            url: String,
        ): String? {
            return when {
                url.startsWith("jdbc:sqlite:") -> "org.sqlite.JDBC"
                url.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
                else -> null
            }
        }
    }
}

data class ConfigCenterAdminSettings(
    val enabled: Boolean = true,
    val path: String = "/config-center",
    val title: String = "Config Center",
) {
    val normalizedPath: String
        get() = path.trim().ifBlank { "/config-center" }.let { raw ->
            if (raw.startsWith("/")) raw else "/$raw"
        }.removeSuffix("/").ifBlank { "/config-center" }
}

internal enum class ConfigCenterJdbcDialect {
    SQLITE,
    POSTGRES,
}

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

internal fun ConfigCenterJdbcSettings.detectDialect(): ConfigCenterJdbcDialect {
    return when {
        url.startsWith("jdbc:sqlite:") -> ConfigCenterJdbcDialect.SQLITE
        url.startsWith("jdbc:postgresql:") -> ConfigCenterJdbcDialect.POSTGRES
        else -> error("Unsupported config-center JDBC url: $url")
    }
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
