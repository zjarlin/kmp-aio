package site.addzero.starter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import java.sql.Connection
import java.sql.DriverManager

private const val DEFAULT_CONFIG_CENTER_ACTIVE = "dev"

fun ApplicationConfig.withConfigCenterOverrides(
    namespace: String,
    active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
): ApplicationConfig {
    val overrides = readConfigCenterValues(
        namespace = namespace,
        active = active,
    )
    if (overrides.isEmpty()) {
        return this
    }
    return buildConfigCenterOverrideConfig(overrides).withFallback(this)
}

fun ApplicationConfig.readConfigCenterValues(
    namespace: String,
    active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
): Map<String, String> {
    return ConfigCenterBootstrapRepository(this).readValues(
        namespace = namespace,
        active = active,
    )
}

fun normalizeConfigCenterNamespace(
    rawValue: String,
): String {
    return rawValue
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

fun normalizeConfigCenterActive(
    rawValue: String,
): String {
    val normalized = rawValue.trim().lowercase().ifBlank { DEFAULT_CONFIG_CENTER_ACTIVE }
    return when (normalized) {
        "default",
        "dev",
        "development",
        -> "dev"

        "prod",
        "prd",
        "production",
        -> "prod"

        else -> normalizeConfigCenterNamespace(normalized).ifBlank { DEFAULT_CONFIG_CENTER_ACTIVE }
    }
}

class ConfigCenterBootstrapRepository(
    private val applicationConfig: ApplicationConfig,
) {
    fun readValue(
        namespace: String,
        active: String,
        key: String,
        defaultValue: String? = null,
    ): String? {
        return readValues(
            namespace = namespace,
            active = active,
        )[key.trim()] ?: defaultValue
    }

    fun readValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val jdbcRuntime = resolveJdbcRuntime() ?: return emptyMap()
        DriverManager.getConnection(
            jdbcRuntime.url,
            jdbcRuntime.username,
            jdbcRuntime.password,
        ).use { connection ->
            val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
            val normalizedActive = normalizeConfigCenterActive(active)
            val values = LinkedHashMap<String, String>()
            appendValues(
                target = values,
                source = readLegacyValues(
                    connection = connection,
                    namespace = normalizedNamespace,
                    active = normalizedActive,
                ),
            )
            appendValues(
                target = values,
                source = readStoredValues(
                    connection = connection,
                    namespace = normalizedNamespace,
                    active = normalizedActive,
                ),
                replaceExisting = true,
            )
            return values
        }
    }

    private fun resolveJdbcRuntime(): JdbcRuntime? {
        val sqliteEnabled = applicationConfig.propertyOrNull("datasources.sqlite.enabled")
            ?.getString()
            ?.toBooleanStrictOrNull() == true
        val postgresEnabled = applicationConfig.propertyOrNull("datasources.postgres.enabled")
            ?.getString()
            ?.toBooleanStrictOrNull() == true
        return when {
            sqliteEnabled -> {
                val driver = applicationConfig.propertyOrNull("datasources.sqlite.driver")
                    ?.getString()
                    ?.ifBlank { "org.sqlite.JDBC" }
                    ?: "org.sqlite.JDBC"
                Class.forName(driver)
                JdbcRuntime(
                    url = applicationConfig.property("datasources.sqlite.url").getString(),
                )
            }

            postgresEnabled -> {
                val driver = applicationConfig.propertyOrNull("datasources.postgres.driver")
                    ?.getString()
                    ?.ifBlank { "org.postgresql.Driver" }
                    ?: "org.postgresql.Driver"
                Class.forName(driver)
                JdbcRuntime(
                    url = applicationConfig.property("datasources.postgres.url").getString(),
                    username = applicationConfig.propertyOrNull("datasources.postgres.user")?.getString()
                        ?: applicationConfig.propertyOrNull("datasources.postgres.username")?.getString(),
                    password = applicationConfig.propertyOrNull("datasources.postgres.password")?.getString(),
                )
            }

            else -> null
        }
    }

    private fun readStoredValues(
        connection: Connection,
        namespace: String,
        active: String,
    ): Map<String, String> {
        return runCatching {
            connection.prepareStatement(
                """
                SELECT config_key, config_value
                FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                ORDER BY update_time DESC, id DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, namespace)
                statement.setString(2, active)
                statement.executeQuery().use { resultSet ->
                    val values = LinkedHashMap<String, String>()
                    while (resultSet.next()) {
                        val key = resultSet.getString(1)?.trim().orEmpty()
                        val value = resultSet.getString(2) ?: continue
                        if (key.isNotBlank()) {
                            values.putIfAbsent(key, value)
                        }
                    }
                    values
                }
            }
        }.recover { error ->
            if (error.isMissingTable("config_center_value")) {
                emptyMap()
            } else {
                throw error
            }
        }.getOrThrow()
    }

    private fun readLegacyValues(
        connection: Connection,
        namespace: String,
        active: String,
    ): Map<String, String> {
        return runCatching {
            connection.prepareStatement(
                """
                SELECT secret.name, secret.value_text
                FROM config_center_secret secret
                JOIN config_center_config cfg ON cfg.id = secret.config_id
                JOIN config_center_environment env ON env.id = cfg.environment_id
                JOIN config_center_project project ON project.id = cfg.project_id
                WHERE project.slug = ?
                  AND env.slug = ?
                  AND cfg.config_type = ?
                  AND cfg.enabled = ?
                  AND secret.enabled = ?
                  AND secret.deleted = ?
                ORDER BY secret.update_time DESC, secret.id DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, namespace)
                statement.setString(2, active)
                statement.setString(3, "ROOT")
                statement.setBoolean(4, true)
                statement.setBoolean(5, true)
                statement.setBoolean(6, false)
                statement.executeQuery().use { resultSet ->
                    val values = LinkedHashMap<String, String>()
                    while (resultSet.next()) {
                        val key = resultSet.getString(1)?.trim().orEmpty()
                        val value = resultSet.getString(2) ?: continue
                        if (key.isNotBlank()) {
                            values.putIfAbsent(key, value)
                        }
                    }
                    values
                }
            }
        }.recover { error ->
            if (error.isMissingTable("config_center_secret")) {
                emptyMap()
            } else {
                throw error
            }
        }.getOrThrow()
    }
}

private data class JdbcRuntime(
    val url: String,
    val username: String? = null,
    val password: String? = null,
)

private fun buildConfigCenterOverrideConfig(
    values: Map<String, String>,
): ApplicationConfig {
    var mergedConfig: Config = ConfigFactory.empty()
    for ((key, rawValue) in values) {
        mergedConfig = parseConfigCenterValue(
            key = key,
            rawValue = rawValue,
        ).withFallback(mergedConfig)
    }
    return HoconApplicationConfig(mergedConfig.resolve())
}

private fun parseConfigCenterValue(
    key: String,
    rawValue: String,
): Config {
    return runCatching {
        ConfigFactory.parseString("$key = $rawValue")
    }.getOrElse {
        ConfigFactory.parseString(
            "$key = ${ConfigValueFactory.fromAnyRef(rawValue).render()}",
        )
    }
}

private fun appendValues(
    target: LinkedHashMap<String, String>,
    source: Map<String, String>,
    replaceExisting: Boolean = false,
) {
    for ((key, value) in source) {
        if (replaceExisting) {
            target[key] = value
        } else {
            target.putIfAbsent(key, value)
        }
    }
}

private fun Throwable.isMissingTable(
    tableName: String,
): Boolean {
    val messageText = generateSequence(this) { error -> error.cause }
        .mapNotNull { error -> error.message }
        .joinToString(" ")
        .lowercase()
    return messageText.contains(tableName.lowercase()) &&
        (
            messageText.contains("no such table") ||
                messageText.contains("does not exist") ||
                messageText.contains("not exist") ||
                messageText.contains("unknown table")
            )
}
