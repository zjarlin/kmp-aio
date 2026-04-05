package site.addzero.configcenter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import site.addzero.util.db.SqlExecutor

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

class ConfigCenterBootstrapRepository(
    private val applicationConfig: ApplicationConfig,
) {
    private val jdbcSettings = applicationConfig.configCenterJdbcSettingsOrNull()
    private val service: ConfigCenterValueService? = jdbcSettings?.let(::JdbcConfigCenterValueService)
    private val sqlExecutor: SqlExecutor? = jdbcSettings?.let { settings ->
        SqlExecutor(
            url = settings.url,
            username = settings.username.orEmpty(),
            password = settings.password.orEmpty(),
            driver = settings.driver,
        )
    }

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
        val values = LinkedHashMap<String, String>()
        appendConfigCenterValues(
            target = values,
            source = readLegacyValues(
                namespace = namespace,
                active = active,
            ),
        )
        appendConfigCenterValues(
            target = values,
            source = readCurrentValues(
                namespace = namespace,
                active = active,
            ),
            replaceExisting = true,
        )
        return resolveConfigCenterSnapshot(values)
    }

    private fun readCurrentValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val delegate = service ?: return emptyMap()
        return delegate.listValues(
            namespace = namespace,
            active = active,
        ).associate { item ->
            item.key to (item.value ?: "")
        }
    }

    private fun readLegacyValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val executor = sqlExecutor ?: return emptyMap()
        return runCatching {
            executor.query(
                sql = """
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
                params = listOf(
                    normalizeConfigCenterNamespace(namespace),
                    normalizeConfigCenterActive(active),
                    "ROOT",
                    true,
                    true,
                    false,
                ),
            ) { resultSet ->
                resultSet.getString(1)?.trim().orEmpty() to (resultSet.getString(2) ?: return@query null)
            }.filterNotNull()
                .fold(LinkedHashMap<String, String>()) { values, (key, value) ->
                    values.putIfAbsent(key, value)
                    values
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

private fun appendConfigCenterValues(
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
