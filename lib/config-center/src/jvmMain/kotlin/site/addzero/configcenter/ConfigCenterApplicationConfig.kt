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
    private val service: ConfigCenterValueService? = applicationConfig
        .configCenterJdbcSettingsOrNull()
        ?.let(::JdbcConfigCenterValueService)

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
        val delegate = service ?: return emptyMap()
        return delegate.listValues(
            namespace = namespace,
            active = active,
        ).associate { item ->
            item.key to (item.value ?: "")
        }
    }
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
