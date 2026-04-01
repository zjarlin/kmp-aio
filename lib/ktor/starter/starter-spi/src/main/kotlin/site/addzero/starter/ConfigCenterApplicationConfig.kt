package site.addzero.starter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.ConfigCenterBootstrapRepository as DelegateBootstrapRepository
import site.addzero.configcenter.DEFAULT_CONFIG_CENTER_ACTIVE
import site.addzero.configcenter.normalizeConfigCenterActive as delegateNormalizeActive
import site.addzero.configcenter.normalizeConfigCenterNamespace as delegateNormalizeNamespace
import java.sql.Connection
import java.sql.DriverManager

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
    return delegateNormalizeNamespace(rawValue)
}

fun normalizeConfigCenterActive(
    rawValue: String,
): String {
    return delegateNormalizeActive(rawValue)
}

class ConfigCenterBootstrapRepository(
    private val applicationConfig: ApplicationConfig,
) {
    private val delegate = DelegateBootstrapRepository(applicationConfig)

    fun readValue(
        namespace: String,
        active: String,
        key: String,
        defaultValue: String? = null,
    ): String? {
        return readValues(namespace, active)[key.trim()] ?: defaultValue
    }

    fun readValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val values = LinkedHashMap<String, String>()
        appendValues(
            target = values,
            source = readLegacyValues(
                namespace = namespace,
                active = active,
            ),
        )
        appendValues(
            target = values,
            source = delegate.readValues(
                namespace = namespace,
                active = active,
            ),
            replaceExisting = true,
        )
        return values
    }

    private fun readLegacyValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val settings = applicationConfig.configCenterJdbcSettingsOrNull() ?: return emptyMap()
        settings.driver?.let { driver ->
            Class.forName(driver)
        }
        return openConnection(settings.url, settings.username, settings.password).use { connection ->
            runCatching {
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
                    statement.setString(1, normalizeConfigCenterNamespace(namespace))
                    statement.setString(2, normalizeConfigCenterActive(active))
                    statement.setString(3, "ROOT")
                    statement.setBoolean(4, true)
                    statement.setBoolean(5, true)
                    statement.setBoolean(6, false)
                    statement.executeQuery().use { resultSet ->
                        val legacyValues = LinkedHashMap<String, String>()
                        while (resultSet.next()) {
                            val key = resultSet.getString(1)?.trim().orEmpty()
                            val value = resultSet.getString(2) ?: continue
                            if (key.isNotBlank()) {
                                legacyValues.putIfAbsent(key, value)
                            }
                        }
                        legacyValues
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

private fun openConnection(
    url: String,
    username: String?,
    password: String?,
): Connection {
    return if (username.isNullOrBlank() && password.isNullOrBlank()) {
        DriverManager.getConnection(url)
    } else {
        DriverManager.getConnection(url, username, password)
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
