package site.addzero.configcenter

import kotlinx.serialization.Serializable

const val DEFAULT_CONFIG_CENTER_ACTIVE = "dev"

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ConfigCenterNamespace(
    val namespace: String,
    val objectName: String = "",
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ConfigCenterItem(
    val key: String = "",
    val comment: String = "",
)

@Serializable
data class ConfigCenterValueDto(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val path: String,
    val value: String? = null,
    val createTimeMillis: Long? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterValueWriteRequest(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val path: String,
    val value: String,
)

@Serializable
data class ConfigCenterValueListResponse(
    val items: List<ConfigCenterValueDto> = emptyList(),
)

@Serializable
data class ConfigCenterDeleteResponse(
    val deleted: Boolean = false,
)

@Serializable
data class ConfigCenterNamespaceDto(
    val namespace: String,
    val entryCount: Int = 0,
)

@Serializable
data class ConfigCenterNamespaceListResponse(
    val items: List<ConfigCenterNamespaceDto> = emptyList(),
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
        path: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): ConfigCenterValueDto

    fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto

    fun deleteValue(
        namespace: String,
        path: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): Boolean
}
