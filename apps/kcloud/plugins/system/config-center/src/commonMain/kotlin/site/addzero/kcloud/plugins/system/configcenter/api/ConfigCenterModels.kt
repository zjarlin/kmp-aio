package site.addzero.kcloud.plugins.system.configcenter.api

import kotlinx.serialization.Serializable

@Serializable
data class ConfigCenterValueDto(
    val namespace: String,
    val active: String,
    val key: String,
    val value: String? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterValueWriteRequest(
    val namespace: String,
    val active: String = "dev",
    val key: String,
    val value: String,
)
