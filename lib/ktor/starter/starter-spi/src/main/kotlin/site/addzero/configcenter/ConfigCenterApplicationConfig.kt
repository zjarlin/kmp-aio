package site.addzero.configcenter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback

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
    private val service: ConfigCenterValueService? =
        applicationConfig.configCenterJdbcSettingsOrNull()?.let(::JdbcConfigCenterValueService)

    fun readValue(
        namespace: String,
        active: String,
        path: String,
        defaultValue: String? = null,
    ): String? {
        return readValues(
            namespace = namespace,
            active = active,
        )[normalizeConfigCenterPath(path)] ?: defaultValue
    }

    fun readValues(
        namespace: String,
        active: String,
    ): Map<String, String> {
        val delegate = service ?: return emptyMap()
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val snapshot = delegate.listValues(
            namespace = normalizedNamespace,
            active = normalizedActive,
            limit = Int.MAX_VALUE,
        ).associateNotNull { item ->
            val normalizedPath = normalizeConfigCenterPath(item.path)
            if (normalizedPath.isBlank()) {
                null
            } else {
                normalizedPath to (item.value ?: "")
            }
        }
        return resolveConfigCenterSnapshot(snapshot)
    }
}

private fun <K, V> Iterable<Pair<K, V>?>.associateNotNull(): Map<K, V> {
    val values = LinkedHashMap<K, V>()
    for (entry in this) {
        if (entry != null) {
            values[entry.first] = entry.second
        }
    }
    return values
}

private fun buildConfigCenterOverrideConfig(
    values: Map<String, String>,
): ApplicationConfig {
    var mergedConfig: Config = ConfigFactory.empty()
    for ((path, rawValue) in values) {
        mergedConfig = parseConfigCenterValue(
            path = path,
            rawValue = rawValue,
        ).withFallback(mergedConfig)
    }
    return HoconApplicationConfig(mergedConfig.resolve())
}

private fun parseConfigCenterValue(
    path: String,
    rawValue: String,
): Config {
    return runCatching {
        ConfigFactory.parseString("$path = $rawValue")
    }.getOrElse {
        ConfigFactory.parseString(
            "$path = ${ConfigValueFactory.fromAnyRef(rawValue).render()}",
        )
    }
}
