package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Single

@Single
class ConfigCenterCompatService(
    private val service: ConfigCenterService,
) {
    fun readValue(
        namespace: String,
        key: String,
        active: String = "dev",
    ): String? {
        return service.readValue(namespace, key, active).value
    }

    fun writeValue(
        namespace: String,
        key: String,
        value: String,
        active: String = "dev",
    ) {
        service.writeValue(
            namespace = namespace,
            key = key,
            value = value,
            active = active,
        )
    }
}
