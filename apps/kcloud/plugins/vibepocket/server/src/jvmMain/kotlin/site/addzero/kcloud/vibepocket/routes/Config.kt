package site.addzero.kcloud.vibepocket.routes

import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterService
import site.addzero.kcloud.vibepocket.dto.OkResponse
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
    fun readValue(key: String): String? {
        return runBlocking { readConfigValue(key) }
    }

    return StorageConfig(
        type = readValue("storage.type") ?: "LOCAL",
        endpoint = readValue("storage.endpoint"),
        accessKey = readValue("storage.accessKey"),
        secretKey = readValue("storage.secretKey"),
        bucketName = readValue("storage.bucketName"),
        region = readValue("storage.region"),
        domain = readValue("storage.domain"),
        basePath = readValue("storage.basePath"),
    )
}

@PutMapping("/api/config/storage")
suspend fun saveStorageConfig(
    @RequestBody config: StorageConfig,
): OkResponse {
    configCenterService().writeValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = "storage.type",
        value = config.type,
    )
    config.endpoint?.let { saveStorageValue("storage.endpoint", it) }
    config.accessKey?.let { saveStorageValue("storage.accessKey", it) }
    config.secretKey?.let { saveStorageValue("storage.secretKey", it) }
    config.bucketName?.let { saveStorageValue("storage.bucketName", it) }
    config.region?.let { saveStorageValue("storage.region", it) }
    config.domain?.let { saveStorageValue("storage.domain", it) }
    config.basePath?.let { saveStorageValue("storage.basePath", it) }
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
    val sqliteEnabled = propertyOrNull("datasources.sqlite.enabled")
        ?.getString()
        ?.toBoolean() == true
    val postgresEnabled = propertyOrNull("datasources.postgres.enabled")
        ?.getString()
        ?.toBoolean() == true
    val sqliteUrl = propertyOrNull("datasources.sqlite.url")?.getString()

    return ConfigRuntimeInfo(
        storage = when {
            sqliteEnabled -> "sqlite"
            postgresEnabled -> "postgres"
            else -> "unknown"
        },
        sqlitePath = sqliteUrl?.removePrefix("jdbc:sqlite:"),
        dataDir = propertyOrNull("kcloud.dataDir")?.getString(),
        cacheDir = propertyOrNull("kcloud.cacheDir")?.getString(),
    )
}

private fun runtimeConfig(): ApplicationConfig {
    return KoinPlatform.getKoin().get()
}

private fun configCenterService(): ConfigCenterService {
    return KoinPlatform.getKoin().get()
}

private fun legacySqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
