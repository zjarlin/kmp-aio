package site.addzero.configcenter

import kotlinx.serialization.Serializable

const val DEFAULT_CONFIG_CENTER_ACTIVE: String = "dev"

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ConfigCenterNamespace(
    val namespace: String,
    val objectName: String = "",
    val providerName: String = "",
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ConfigCenterItem(
    val key: String = "",
    val comment: String = "",
    val defaultValue: String = "",
    val required: Boolean = false,
)

@Serializable
data class ConfigCenterKeyDefinition(
    val namespace: String,
    val key: String,
    val valueType: String,
    val comment: String? = null,
    val defaultValue: String? = null,
    val required: Boolean = false,
    val source: String? = null,
    val builtin: Boolean = false,
    val editable: Boolean = true,
    val deletable: Boolean = true,
)

interface ConfigCenterDefinitionProvider {
    val definitions: List<ConfigCenterKeyDefinition>
}

@Serializable
data class ConfigCenterValueDto(
    val id: String = "",
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String? = null,
    val comment: String? = null,
    val createTimeMillis: Long? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterValueWriteRequest(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String,
    val comment: String? = null,
)

@Serializable
data class ConfigCenterValueListResponse(
    val items: List<ConfigCenterValueDto> = emptyList(),
)

@Serializable
data class ConfigCenterEntryDto(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String? = null,
    val comment: String? = null,
    val defaultValue: String? = null,
    val valueType: String = "kotlin.String",
    val required: Boolean = false,
    val source: String? = null,
    val builtin: Boolean = false,
    val editable: Boolean = true,
    val deletable: Boolean = true,
    val createTimeMillis: Long? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterEntryWriteRequest(
    val namespace: String,
    val active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    val key: String,
    val value: String? = null,
    val comment: String? = null,
    val defaultValue: String? = null,
    val valueType: String? = null,
    val required: Boolean? = null,
)

@Serializable
data class ConfigCenterEntryListResponse(
    val items: List<ConfigCenterEntryDto> = emptyList(),
)

@Serializable
data class ConfigCenterDeleteResponse(
    val deleted: Boolean = false,
)

@Serializable
data class ConfigCenterNamespaceDto(
    val id: String = "",
    val namespace: String,
    val entryCount: Int = 0,
    val definitionCount: Int = 0,
    val createTimeMillis: Long? = null,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterNamespaceWriteRequest(
    val namespace: String,
    val renameFrom: String? = null,
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

interface ConfigCenterAdminService : ConfigCenterValueService {
    fun listNamespaces(): List<ConfigCenterNamespaceDto>

    fun writeNamespace(
        request: ConfigCenterNamespaceWriteRequest,
    ): ConfigCenterNamespaceDto

    fun deleteNamespace(
        namespace: String,
    ): Boolean

    fun listEntries(
        namespace: String? = null,
        active: String? = null,
        keyword: String? = null,
        limit: Int = 200,
    ): List<ConfigCenterEntryDto>

    fun readEntry(
        namespace: String,
        key: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): ConfigCenterEntryDto

    fun writeEntry(
        request: ConfigCenterEntryWriteRequest,
    ): ConfigCenterEntryDto

    fun deleteEntry(
        namespace: String,
        key: String,
        active: String = DEFAULT_CONFIG_CENTER_ACTIVE,
    ): Boolean
}
