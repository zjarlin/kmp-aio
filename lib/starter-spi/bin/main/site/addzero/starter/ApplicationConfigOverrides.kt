package site.addzero.starter

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.util.AttributeKey

private val EffectiveApplicationConfigKey =
    AttributeKey<ApplicationConfig>("site.addzero.starter.effective-application-config")

fun Application.installEffectiveConfig(config: ApplicationConfig) {
    val installed = runCatching { attributes[EffectiveApplicationConfigKey] }.getOrNull()
    if (installed == null) {
        attributes.put(EffectiveApplicationConfigKey, config)
    }
}

fun Application.effectiveConfig(): ApplicationConfig {
    return runCatching { attributes[EffectiveApplicationConfigKey] }.getOrNull()
        ?: environment.config
}
