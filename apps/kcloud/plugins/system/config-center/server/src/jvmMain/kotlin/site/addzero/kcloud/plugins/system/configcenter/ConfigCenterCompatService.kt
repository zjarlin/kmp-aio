package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Single

@Single
class ConfigCenterCompatService(
    private val service: ConfigCenterService,
) {
    suspend fun getLegacyValue(
        namespace: String,
        key: String,
        profile: String = "default",
    ): String? {
        return service.readCompatValue(namespace, key, profile).value
    }

    suspend fun listLegacyValues(
        namespace: String,
        profile: String = "default",
    ): Map<String, String> {
        return service.readCompatSnapshot(namespace, profile)
    }

    suspend fun getOrImportLegacyValue(
        namespace: String,
        key: String,
        description: String? = null,
        profile: String = "default",
        legacyLoader: (() -> String?)? = null,
    ): String? {
        val existing = getLegacyValue(namespace, key, profile)
        if (existing != null) {
            return existing
        }
        val legacyValue = legacyLoader?.invoke() ?: return null
        saveLegacyValue(
            namespace = namespace,
            key = key,
            value = legacyValue,
            description = description,
            profile = profile,
        )
        return legacyValue
    }

    suspend fun saveLegacyValue(
        namespace: String,
        key: String,
        value: String,
        description: String? = null,
        profile: String = "default",
    ) {
        service.saveCompatValue(
            namespace = namespace,
            key = key,
            value = value,
            description = description,
            profile = profile,
        )
    }
}
