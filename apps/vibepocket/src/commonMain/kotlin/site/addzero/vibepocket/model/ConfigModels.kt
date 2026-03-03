package site.addzero.vibepocket.model

import kotlinx.serialization.Serializable

/** 配置项请求 */
@Serializable
data class ConfigEntry(
    val key: String,
    val value: String,
    val description: String? = null
)

/** 配置项响应 */
@Serializable
data class ConfigResponse(
    val key: String,
    val value: String?
)

/** 存储配置 */
@Serializable
data class StorageConfig(
    val type: String = "LOCAL", // LOCAL, S3, MINIO, ALIYUN, QINIU
    val endpoint: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null,
    val bucketName: String? = null,
    val region: String? = null,
    val domain: String? = null, // 访问域名
    val basePath: String? = null
)
