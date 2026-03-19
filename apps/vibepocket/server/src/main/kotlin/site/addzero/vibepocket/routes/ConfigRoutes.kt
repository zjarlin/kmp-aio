package site.addzero.vibepocket.routes

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.vibepocket.dto.OkResponse
import site.addzero.vibepocket.model.AppConfig
import site.addzero.vibepocket.model.by
import site.addzero.vibepocket.model.key
import site.addzero.vibepocket.model.value

/**
 * 从 app_config 表读取配置值
 */
fun KSqlClient.getConfig(key: String): String? {
    return createQuery(AppConfig::class) {
        where(table.key eq key)
        select(table.value)
    }.execute().firstOrNull()
}

/**
 * 写入或更新 app_config 配置
 *
 * AppConfig 的 key 字段标注了 @Key，Jimmer save 会自动 upsert。
 */
fun KSqlClient.setConfig(key: String, value: String, description: String? = null) {
    save(
        new(AppConfig::class).by {
            this.key = key
            this.value = value
            this.description = description
        }
    )
}


@Serializable
data class ConfigEntry(val key: String, val value: String, val description: String? = null)

@Serializable
data class ConfigResponse(val key: String, val value: String?)

@Serializable
data class StorageConfig(
    val type: String = "LOCAL",
    val endpoint: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null,
    val bucketName: String? = null,
    val region: String? = null,
    val domain: String? = null,
    val basePath: String? = null
)

@Serializable
data class ConfigRuntimeInfo(
    val storage: String = "unknown",
    val sqlitePath: String? = null,
    val dataDir: String? = null,
    val cacheDir: String? = null,
)


@GetMapping("/api/config/runtime")
suspend fun readRuntimeConfig(application: Application): ConfigRuntimeInfo {
    return application.environment.config.toRuntimeInfo()
}

@GetMapping("/api/config/{key}")
suspend fun readConfig(
    @PathVariable key: String,
): ConfigResponse {
    val value = sqlClient().getConfig(key)
    return ConfigResponse(key = key, value = value)
}

@PutMapping("/api/config")
suspend fun updateConfig(
    @RequestBody entry: ConfigEntry,
): OkResponse {
    sqlClient().setConfig(entry.key, entry.value, entry.description)
    return OkResponse()
}

@GetMapping("/api/config/storage")
suspend fun readStorageConfig(): StorageConfig {
    val sqlClient = sqlClient()
    return StorageConfig(
        type = sqlClient.getConfig("storage.type") ?: "LOCAL",
        endpoint = sqlClient.getConfig("storage.endpoint"),
        accessKey = sqlClient.getConfig("storage.accessKey"),
        secretKey = sqlClient.getConfig("storage.secretKey"),
        bucketName = sqlClient.getConfig("storage.bucketName"),
        region = sqlClient.getConfig("storage.region"),
        domain = sqlClient.getConfig("storage.domain"),
        basePath = sqlClient.getConfig("storage.basePath"),
    )
}

@PutMapping("/api/config/storage")
suspend fun updateStorageConfig(
    @RequestBody config: StorageConfig,
): OkResponse {
    val sqlClient = sqlClient()
    sqlClient.setConfig("storage.type", config.type)
    config.endpoint?.let { sqlClient.setConfig("storage.endpoint", it) }
    config.accessKey?.let { sqlClient.setConfig("storage.accessKey", it) }
    config.secretKey?.let { sqlClient.setConfig("storage.secretKey", it) }
    config.bucketName?.let { sqlClient.setConfig("storage.bucketName", it) }
    config.region?.let { sqlClient.setConfig("storage.region", it) }
    config.domain?.let { sqlClient.setConfig("storage.domain", it) }
    config.basePath?.let { sqlClient.setConfig("storage.basePath", it) }
    return OkResponse()
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
        sqlitePath = sqliteUrl?.takeIf { it.startsWith("jdbc:sqlite:") }?.removePrefix("jdbc:sqlite:"),
        dataDir = propertyOrNull("vibepocket.runtime.dataDir")?.getString(),
        cacheDir = propertyOrNull("vibepocket.runtime.cacheDir")?.getString(),
    )
}

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
