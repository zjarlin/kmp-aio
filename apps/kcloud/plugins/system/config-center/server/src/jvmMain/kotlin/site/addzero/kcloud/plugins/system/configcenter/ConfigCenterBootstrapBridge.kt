package site.addzero.kcloud.plugins.system.configcenter

import io.ktor.server.config.ApplicationConfig
import site.addzero.starter.ConfigCenterBootstrapRepository

class ConfigCenterBootstrapBridge(
    private val applicationConfig: ApplicationConfig,
    private val namespace: String = "kcloud",
    private val profile: String = "default",
) {
    private val repository = ConfigCenterBootstrapRepository(applicationConfig)

    fun getString(
        key: String,
        defaultValue: String? = null,
    ): String? {
        return repository.readValue(
            namespace = namespace,
            active = profile,
            key = key,
            defaultValue = defaultValue,
        )
    }

    fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return getString(
            key = key,
            defaultValue = defaultValue.toString(),
        )?.toIntOrNull() ?: defaultValue
    }
}
