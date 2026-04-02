package site.addzero.kcloud.vibepocket.routes

import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.kcloud.config.KCloudConfigKeys
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.kcloud.vibepocket.dto.OkResponse
import site.addzero.kcloud.vibepocket.config.VibepocketConfigKeys
import site.addzero.kcloud.vibepocket.model.AppConfig

private const val VIBEPOCKET_CONFIG_NAMESPACE = "vibepocket"

private fun KSqlClient.readLegacyConfig(key: String): String? {
    return createQuery(AppConfig::class) {
        select(table)
    }.execute().firstOrNull { it.key == key }?.value
}

@GetMapping("/api/config/runtime")
suspend fun getRuntimeInfo(): ConfigRuntimeInfo {
    return runtimeConfig().toRuntimeInfo()
}

@GetMapping("/api/config/{key}")
suspend fun getConfig(
    @PathVariable key: String,
): ConfigResponse {
    val value = readConfigValue(key)
    return ConfigResponse(key = key, value = value)
}

@PutMapping("/api/config")
suspend fun updateConfig(
    @RequestBody entry: ConfigEntry,
): OkResponse {
    configCenterService().writeValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = entry.key,
        value = entry.value,
    )
    return OkResponse()
}

@GetMapping("/api/config/storage")
suspend fun getStorageConfig(): StorageConfig {
    fun readValue(definition: ConfigCenterKeyDefinition): String? {
        return runBlocking { readConfigValue(definition) }
    }

    return StorageConfig(
        type = readValue(VibepocketConfigKeys.storageType)
            ?: VibepocketConfigKeys.storageType.defaultValue
            ?: "LOCAL",
        endpoint = readValue(VibepocketConfigKeys.storageEndpoint),
        accessKey = readValue(VibepocketConfigKeys.storageAccessKey),
        secretKey = readValue(VibepocketConfigKeys.storageSecretKey),
        bucketName = readValue(VibepocketConfigKeys.storageBucketName),
        region = readValue(VibepocketConfigKeys.storageRegion),
        domain = readValue(VibepocketConfigKeys.storageDomain),
        basePath = readValue(VibepocketConfigKeys.storageBasePath),
    )
}

@PutMapping("/api/config/storage")
suspend fun saveStorageConfig(
    @RequestBody config: StorageConfig,
): OkResponse {
    configCenterService().writeValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = VibepocketConfigKeys.STORAGE_TYPE,
        value = config.type,
    )
    config.endpoint?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_ENDPOINT, it) }
    config.accessKey?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_ACCESS_KEY, it) }
    config.secretKey?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_SECRET_KEY, it) }
    config.bucketName?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_BUCKET_NAME, it) }
    config.region?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_REGION, it) }
    config.domain?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_DOMAIN, it) }
    config.basePath?.let { saveStorageValue(VibepocketConfigKeys.STORAGE_BASE_PATH, it) }
    return OkResponse()
}

private suspend fun saveStorageValue(key: String, value: String) {
    configCenterService().writeValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = key,
        value = value,
    )
}

private suspend fun readConfigValue(
    definition: ConfigCenterKeyDefinition,
): String? {
    return readConfigValue(definition.key)
}

private suspend fun readConfigValue(
    key: String,
): String? {
    configCenterService().readValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = key,
    ).value?.let { value ->
        return value
    }
    val legacyValue = legacySqlClient().readLegacyConfig(key) ?: return null
    configCenterService().writeValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = key,
        value = legacyValue,
    )
    return legacyValue
}

private fun ApplicationConfig.toRuntimeInfo(): ConfigRuntimeInfo {
    val sqliteEnabled = propertyOrNull(KCloudConfigKeys.SQLITE_ENABLED)
        ?.getString()
        ?.toBoolean() == true
    val postgresEnabled = propertyOrNull(KCloudConfigKeys.POSTGRES_ENABLED)
        ?.getString()
        ?.toBoolean() == true
    val sqliteUrl = propertyOrNull(KCloudConfigKeys.SQLITE_URL)?.getString()

    return ConfigRuntimeInfo(
        storage = when {
            sqliteEnabled -> "sqlite"
            postgresEnabled -> "postgres"
            else -> "unknown"
        },
        sqlitePath = sqliteUrl?.removePrefix("jdbc:sqlite:"),
        dataDir = propertyOrNull(KCloudConfigKeys.DATA_DIR)?.getString(),
        cacheDir = propertyOrNull(KCloudConfigKeys.CACHE_DIR)?.getString(),
    )
}

private fun runtimeConfig(): ApplicationConfig {
    return KoinPlatform.getKoin().get()
}

private fun configCenterService(): ConfigValueServiceSpi {
    return KoinPlatform.getKoin().get()
}

private fun legacySqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
