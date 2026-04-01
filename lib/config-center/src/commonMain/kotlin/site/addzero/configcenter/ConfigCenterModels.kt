package site.addzero.configcenter

import kotlinx.serialization.Serializable

const val DEFAULT_CONFIG_CENTER_ACTIVE: String = "dev"

@Serializable
data class ConfigCenterValueDto(
    val id: String = "",
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String? = null,
    val description: String? = null,
    val createTimeMillis: Long? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterValueWriteRequest(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String,
    val description: String? = null,
)

@Serializable
data class ConfigCenterValueListResponse(
    val items: List<ConfigCenterValueDto> = emptyList(),
)

@Serializable
data class ConfigCenterDeleteResponse(
    val deleted: Boolean = false,
)

interface ConfigCenterValueService {
    fun listValues(
        namespace: String? = null,
        active: String? = null,
        keyword: String? = null,
        limit: Int = 200,
    ): List<ConfigCenterValueDto>

    fun readValue(
        namespace: String,
        key: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): ConfigCenterValueDto

    fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto

    fun deleteValue(
        namespace: String,
        key: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): Boolean
}
