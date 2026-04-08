package site.addzero.kcloud.plugins.hostconfig.api.catalog

import kotlinx.serialization.Serializable

@Serializable
enum class CatalogEntityType {
    PRODUCT,
    DEVICE,
    PROPERTY,
    FEATURE,
    LABEL,
}

@Serializable
enum class CatalogFieldWidgetType {
    TEXT,
    TEXTAREA,
    NUMBER,
    BOOLEAN,
    SELECT,
    MULTI_SELECT,
    JSON,
}

@Serializable
enum class CatalogValueRenderType {
    TEXT,
    BOOLEAN,
    BADGE,
    TAGS,
    DATETIME,
    CODE,
}

@Serializable
data class CatalogSnapshotResponse(
    val products: List<ProductDefinitionTreeResponse>,
    val labels: List<LabelDefinitionResponse>,
    val metadata: CatalogMetadataResponse,
)

@Serializable
data class ProductDefinitionCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val vendor: String? = null,
    val category: String? = null,
    val enabled: Boolean = true,
    val sortIndex: Int = 0,
    val labelIds: List<Long> = emptyList(),
)

@Serializable
data class ProductDefinitionUpdateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val vendor: String? = null,
    val category: String? = null,
    val enabled: Boolean = true,
    val sortIndex: Int = 0,
    val labelIds: List<Long> = emptyList(),
)

@Serializable
data class ProductDefinitionTreeResponse(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val vendor: String?,
    val category: String?,
    val enabled: Boolean,
    val sortIndex: Int,
    val labels: List<LabelDefinitionResponse>,
    val devices: List<DeviceDefinitionTreeResponse>,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class DeviceDefinitionCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val deviceTypeId: Long? = null,
    val supportsTelemetry: Boolean = true,
    val supportsControl: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class DeviceDefinitionUpdateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val deviceTypeId: Long? = null,
    val supportsTelemetry: Boolean = true,
    val supportsControl: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class DeviceDefinitionTreeResponse(
    val id: Long,
    val productId: Long,
    val code: String,
    val name: String,
    val description: String?,
    val deviceTypeId: Long?,
    val deviceTypeCode: String?,
    val deviceTypeName: String?,
    val supportsTelemetry: Boolean,
    val supportsControl: Boolean,
    val sortIndex: Int,
    val properties: List<PropertyDefinitionResponse>,
    val features: List<FeatureDefinitionResponse>,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class PropertyDefinitionCreateRequest(
    val identifier: String,
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val unit: String? = null,
    val required: Boolean = false,
    val writable: Boolean = false,
    val telemetry: Boolean = true,
    val nullable: Boolean = true,
    val length: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
    val sortIndex: Int = 0,
)

@Serializable
data class PropertyDefinitionUpdateRequest(
    val identifier: String,
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val unit: String? = null,
    val required: Boolean = false,
    val writable: Boolean = false,
    val telemetry: Boolean = true,
    val nullable: Boolean = true,
    val length: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
    val sortIndex: Int = 0,
)

@Serializable
data class PropertyDefinitionResponse(
    val id: Long,
    val deviceDefinitionId: Long,
    val identifier: String,
    val name: String,
    val description: String?,
    val dataTypeId: Long,
    val dataTypeCode: String,
    val dataTypeName: String,
    val unit: String?,
    val required: Boolean,
    val writable: Boolean,
    val telemetry: Boolean,
    val nullable: Boolean,
    val length: Int?,
    val attributes: Map<String, String> = emptyMap(),
    val sortIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class FeatureDefinitionCreateRequest(
    val identifier: String,
    val name: String,
    val description: String? = null,
    val inputSchema: String? = null,
    val outputSchema: String? = null,
    val asynchronous: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class FeatureDefinitionUpdateRequest(
    val identifier: String,
    val name: String,
    val description: String? = null,
    val inputSchema: String? = null,
    val outputSchema: String? = null,
    val asynchronous: Boolean = false,
    val sortIndex: Int = 0,
)

@Serializable
data class FeatureDefinitionResponse(
    val id: Long,
    val deviceDefinitionId: Long,
    val identifier: String,
    val name: String,
    val description: String?,
    val inputSchema: String?,
    val outputSchema: String?,
    val asynchronous: Boolean,
    val sortIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class LabelDefinitionCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val colorHex: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class LabelDefinitionUpdateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val colorHex: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class LabelDefinitionResponse(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val colorHex: String?,
    val sortIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class CatalogMetadataResponse(
    val entities: List<CatalogEntityMetadataResponse>,
    val optionSets: List<CatalogOptionSetResponse> = emptyList(),
)

@Serializable
data class CatalogEntityMetadataResponse(
    val entityType: CatalogEntityType,
    val title: String,
    val subtitle: String? = null,
    val formFields: List<CatalogFieldMetadataResponse>,
    val detailFields: List<CatalogDetailFieldMetadataResponse>,
)

@Serializable
data class CatalogFieldMetadataResponse(
    val key: String,
    val label: String,
    val widget: CatalogFieldWidgetType,
    val required: Boolean = false,
    val multiple: Boolean = false,
    val readOnly: Boolean = false,
    val placeholder: String? = null,
    val helperText: String? = null,
    val optionSource: String? = null,
    val options: List<CatalogFieldOptionResponse> = emptyList(),
)

@Serializable
data class CatalogFieldOptionResponse(
    val value: String,
    val label: String,
    val description: String? = null,
)

@Serializable
data class CatalogOptionSetResponse(
    val key: String,
    val options: List<CatalogFieldOptionResponse>,
)

@Serializable
data class CatalogDetailFieldMetadataResponse(
    val key: String,
    val label: String,
    val renderType: CatalogValueRenderType = CatalogValueRenderType.TEXT,
)

@Serializable
data class SpecIotPropertyResponse(
    val identifier: String,
    val name: String?,
    val description: String?,
    val unit: String?,
    val valueType: String,
    val length: Int?,
    val attributes: Map<String, String>,
)
