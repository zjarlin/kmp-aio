package site.addzero.configcenter

private const val CONFIG_CENTER_TABLE_NOTE = "来自config-center中间件请勿删除!"

data class ConfigCenterJdbcSettings(
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val driver: String? = inferDriver(url),
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
    val normalizedPath
        get() = path.trim().ifBlank { "/config-center" }.let { raw ->
            if (raw.startsWith("/")) raw else "/$raw"
        }.removeSuffix("/").ifBlank { "/config-center" }
}

internal enum class ConfigCenterJdbcDialect {
    SQLITE,
    POSTGRES,
}

internal fun ConfigCenterJdbcSettings.detectDialect(): ConfigCenterJdbcDialect {
    return when {
        url.startsWith("jdbc:sqlite:") -> ConfigCenterJdbcDialect.SQLITE
        url.startsWith("jdbc:postgresql:") -> ConfigCenterJdbcDialect.POSTGRES
        else -> error("Unsupported config-center JDBC url: $url")
    }
}
