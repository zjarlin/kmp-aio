package site.addzero.vibepocket.routes

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import kotlinx.serialization.Serializable
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.configcenter.runtime.ConfigCenterCompatService
import site.addzero.vibepocket.dto.OkResponse
import site.addzero.vibepocket.model.AppConfig
import site.addzero.vibepocket.model.by
import site.addzero.vibepocket.model.key
import site.addzero.vibepocket.model.value

private const val VIBEPOCKET_CONFIG_NAMESPACE = "vibepocket"

/**
 * 从历史 app_config 表读取配置值。
 */
private fun KSqlClient.readLegacyConfig(
    key: String,
): String? {
    return createQuery(AppConfig::class) {
        where(table.key eq key)
        select(table.value)
    }.execute().firstOrNull()
}

/**
 * 写入或更新历史 app_config 配置。
 */
private fun KSqlClient.writeLegacyConfig(
    key: String,
    value: String,
    description: String? = null,
) {
    save(
        new(AppConfig::class).by {
            this.key = key
            this.value = value
            this.description = description
        },
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
    val basePath: String? = null,
)

@Serializable
data class ConfigRuntimeInfo(
    val storage: String = "unknown",
    val sqlitePath: String? = null,
    val dataDir: String? = null,
    val cacheDir: String? = null,
)

/**
 * 返回当前运行态存储信息。
 */
@GetMapping("/api/config/runtime")
suspend fun readRuntimeConfig(application: Application): ConfigRuntimeInfo {
    return application.environment.config.toRuntimeInfo()
}

/**
 * 兼容旧接口读取单个配置，同时把历史值迁移进配置中心。
 */
@GetMapping("/api/config/{key}")
suspend fun readConfig(
    @PathVariable key: String,
): ConfigResponse {
    val value = compatService().getOrImportLegacyValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = key,
    ) {
        legacySqlClient().readLegacyConfig(key)
    }
    return ConfigResponse(key = key, value = value)
}

/**
 * 兼容旧接口写入配置，事实源改为配置中心。
 */
@PutMapping("/api/config")
suspend fun updateConfig(
    @RequestBody entry: ConfigEntry,
): OkResponse {
    compatService().saveLegacyValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = entry.key,
        value = entry.value,
        description = entry.description,
    )
    return OkResponse()
}

/**
 * 兼容旧接口读取存储配置。
 */
@GetMapping("/api/config/storage")
suspend fun readStorageConfig(): StorageConfig {
    fun readValue(key: String): String? {
        return runBlocking {
            compatService().getOrImportLegacyValue(
                namespace = VIBEPOCKET_CONFIG_NAMESPACE,
                key = key,
            ) {
                legacySqlClient().readLegacyConfig(key)
            }
        }
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

/**
 * 兼容旧接口写入存储配置。
 */
@PutMapping("/api/config/storage")
suspend fun updateStorageConfig(
    @RequestBody config: StorageConfig,
): OkResponse {
    compatService().saveLegacyValue(
        namespace = VIBEPOCKET_CONFIG_NAMESPACE,
        key = "storage.type",
        value = config.type,
    )
    config.endpoint?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.endpoint",
            value = it,
        )
    }
    config.accessKey?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.accessKey",
            value = it,
        )
    }
    config.secretKey?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.secretKey",
            value = it,
        )
    }
    config.bucketName?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.bucketName",
            value = it,
        )
    }
    config.region?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.region",
            value = it,
        )
    }
    config.domain?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.domain",
            value = it,
        )
    }
    config.basePath?.let {
        compatService().saveLegacyValue(
            namespace = VIBEPOCKET_CONFIG_NAMESPACE,
            key = "storage.basePath",
            value = it,
        )
    }

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

private fun compatService(): ConfigCenterCompatService {
    return KoinPlatform.getKoin().get()
}

private fun legacySqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
