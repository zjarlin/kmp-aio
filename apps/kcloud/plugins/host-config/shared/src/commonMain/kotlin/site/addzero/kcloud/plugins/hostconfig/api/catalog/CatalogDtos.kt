package site.addzero.kcloud.plugins.hostconfig.api.catalog

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义目录entity类型枚举。
 */
enum class CatalogEntityType {
    PRODUCT,
    DEVICE,
    PROPERTY,
    FEATURE,
    LABEL,
}

@Serializable
/**
 * 定义目录字段widget类型枚举。
 */
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
/**
 * 定义目录值render类型枚举。
 */
enum class CatalogValueRenderType {
    TEXT,
    BOOLEAN,
    BADGE,
    TAGS,
    DATETIME,
    CODE,
}

@Serializable
/**
 * 表示目录快照响应结果。
 *
 * @property products products。
 * @property labels labels。
 * @property metadata 元数据。
 */
data class CatalogSnapshotResponse(
    val products: List<ProductDefinitionTreeResponse>,
    val labels: List<LabelDefinitionResponse>,
    val metadata: CatalogMetadataResponse,
)

@Serializable
/**
 * 表示product定义创建请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property vendor vendor。
 * @property category category。
 * @property enabled 是否启用。
 * @property sortIndex 排序序号。
 * @property labelIds label ID 列表。
 */
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
/**
 * 表示product定义更新请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property vendor vendor。
 * @property category category。
 * @property enabled 是否启用。
 * @property sortIndex 排序序号。
 * @property labelIds label ID 列表。
 */
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
/**
 * 表示product定义树响应结果。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property vendor vendor。
 * @property category category。
 * @property enabled 是否启用。
 * @property sortIndex 排序序号。
 * @property labels labels。
 * @property devices 设备。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示设备定义创建请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property deviceTypeId 设备类型 ID。
 * @property supportsTelemetry 支持telemetry。
 * @property supportsControl 支持控制。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示设备定义更新请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property deviceTypeId 设备类型 ID。
 * @property supportsTelemetry 支持telemetry。
 * @property supportsControl 支持控制。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示设备定义树响应结果。
 *
 * @property id 主键 ID。
 * @property productId product ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property deviceTypeId 设备类型 ID。
 * @property deviceTypeCode 设备类型编码。
 * @property deviceTypeName 设备类型名称。
 * @property supportsTelemetry 支持telemetry。
 * @property supportsControl 支持控制。
 * @property sortIndex 排序序号。
 * @property properties 属性。
 * @property features features。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示属性定义创建请求参数。
 *
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property unit 单元。
 * @property required 是否必填。
 * @property writable writable。
 * @property telemetry telemetry。
 * @property nullable 是否可空。
 * @property length length。
 * @property attributes attributes。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示属性定义更新请求参数。
 *
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property unit 单元。
 * @property required 是否必填。
 * @property writable writable。
 * @property telemetry telemetry。
 * @property nullable 是否可空。
 * @property length length。
 * @property attributes attributes。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示属性定义响应结果。
 *
 * @property id 主键 ID。
 * @property deviceDefinitionId 设备定义 ID。
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property dataTypeCode 数据类型编码。
 * @property dataTypeName 数据类型名。
 * @property unit 单元。
 * @property required 是否必填。
 * @property writable writable。
 * @property telemetry telemetry。
 * @property nullable 是否可空。
 * @property length length。
 * @property attributes attributes。
 * @property sortIndex 排序序号。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示feature定义创建请求参数。
 *
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property inputSchema 输入结构。
 * @property outputSchema 输出结构。
 * @property asynchronous asynchronous。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示feature定义更新请求参数。
 *
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property inputSchema 输入结构。
 * @property outputSchema 输出结构。
 * @property asynchronous asynchronous。
 * @property sortIndex 排序序号。
 */
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
/**
 * 表示feature定义响应结果。
 *
 * @property id 主键 ID。
 * @property deviceDefinitionId 设备定义 ID。
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property inputSchema 输入结构。
 * @property outputSchema 输出结构。
 * @property asynchronous asynchronous。
 * @property sortIndex 排序序号。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示标签定义创建请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property colorHex colorhex。
 * @property sortIndex 排序序号。
 */
data class LabelDefinitionCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val colorHex: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示标签定义更新请求参数。
 *
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property colorHex colorhex。
 * @property sortIndex 排序序号。
 */
data class LabelDefinitionUpdateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val colorHex: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示标签定义响应结果。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property colorHex colorhex。
 * @property sortIndex 排序序号。
 * @property createdAt 创建时间戳。
 * @property updatedAt 更新时间戳。
 */
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
/**
 * 表示目录metadata响应结果。
 *
 * @property entities entities。
 * @property optionSets 选项sets。
 */
data class CatalogMetadataResponse(
    val entities: List<CatalogEntityMetadataResponse>,
    val optionSets: List<CatalogOptionSetResponse> = emptyList(),
)

@Serializable
/**
 * 表示目录entitymetadata响应结果。
 *
 * @property entityType entity类型。
 * @property title title。
 * @property subtitle subtitle。
 * @property formFields formfields。
 * @property detailFields 详情fields。
 */
data class CatalogEntityMetadataResponse(
    val entityType: CatalogEntityType,
    val title: String,
    val subtitle: String? = null,
    val formFields: List<CatalogFieldMetadataResponse>,
    val detailFields: List<CatalogDetailFieldMetadataResponse>,
)

@Serializable
/**
 * 表示目录字段metadata响应结果。
 *
 * @property key key。
 * @property label label。
 * @property widget widget。
 * @property required 是否必填。
 * @property multiple multiple。
 * @property readOnly readonly。
 * @property placeholder 占位提示。
 * @property helperText helper文本。
 * @property optionSource 选项来源。
 * @property options 选项。
 */
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
/**
 * 表示目录字段选项响应结果。
 *
 * @property value 值。
 * @property label label。
 * @property description 描述。
 */
data class CatalogFieldOptionResponse(
    val value: String,
    val label: String,
    val description: String? = null,
)

@Serializable
/**
 * 表示目录选项set响应结果。
 *
 * @property key key。
 * @property options 选项。
 */
data class CatalogOptionSetResponse(
    val key: String,
    val options: List<CatalogFieldOptionResponse>,
)

@Serializable
/**
 * 表示目录详情字段metadata响应结果。
 *
 * @property key key。
 * @property label label。
 * @property renderType render类型。
 */
data class CatalogDetailFieldMetadataResponse(
    val key: String,
    val label: String,
    val renderType: CatalogValueRenderType = CatalogValueRenderType.TEXT,
)

@Serializable
/**
 * 表示speciot属性响应结果。
 *
 * @property identifier identifier。
 * @property name 名称。
 * @property description 描述。
 * @property unit 单元。
 * @property valueType 取值类型。
 * @property length length。
 * @property attributes attributes。
 */
data class SpecIotPropertyResponse(
    val identifier: String,
    val name: String?,
    val description: String?,
    val unit: String?,
    val valueType: String,
    val length: Int?,
    val attributes: Map<String, String>,
)
