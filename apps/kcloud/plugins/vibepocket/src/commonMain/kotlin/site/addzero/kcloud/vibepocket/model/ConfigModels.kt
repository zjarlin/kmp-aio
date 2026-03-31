package site.addzero.kcloud.vibepocket.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfigEntry(
    val key: String,
    val value: String,
    val description: String? = null,
)

@Serializable
data class ConfigResponse(
    val key: String,
    val value: String?,
)

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
