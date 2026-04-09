package site.addzero.kcloud.plugins.hostconfig.api.tag

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType

@Serializable
/**
 * 表示标签创建请求参数。
 *
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property registerTypeId 寄存器类型 ID。
 * @property registerAddress 寄存器地址。
 * @property enabled 是否启用。
 * @property defaultValue 默认值。
 * @property exceptionValue 异常值。
 * @property pointType 点位类型。
 * @property debounceMs 去抖毫秒。
 * @property sortIndex 排序序号。
 * @property scalingEnabled 缩放启用状态。
 * @property scalingOffset 缩放offset。
 * @property rawMin 原始min。
 * @property rawMax 原始max。
 * @property engMin 工程min。
 * @property engMax 工程max。
 * @property forwardEnabled 转发启用状态。
 * @property forwardRegisterTypeId 转发寄存器类型 ID。
 * @property forwardRegisterAddress 转发寄存器地址。
 */
data class TagCreateRequest(
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val registerTypeId: Long,
    val registerAddress: Int,
    val enabled: Boolean = true,
    val defaultValue: String? = null,
    val exceptionValue: String? = null,
    val pointType: PointType? = null,
    val debounceMs: Int? = null,
    val sortIndex: Int = 0,
    val scalingEnabled: Boolean = false,
    val scalingOffset: String? = null,
    val rawMin: String? = null,
    val rawMax: String? = null,
    val engMin: String? = null,
    val engMax: String? = null,
    val forwardEnabled: Boolean = false,
    val forwardRegisterTypeId: Long? = null,
    val forwardRegisterAddress: Int? = null,
)

@Serializable
/**
 * 表示标签更新请求参数。
 *
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property registerTypeId 寄存器类型 ID。
 * @property registerAddress 寄存器地址。
 * @property enabled 是否启用。
 * @property defaultValue 默认值。
 * @property exceptionValue 异常值。
 * @property pointType 点位类型。
 * @property debounceMs 去抖毫秒。
 * @property sortIndex 排序序号。
 * @property scalingEnabled 缩放启用状态。
 * @property scalingOffset 缩放offset。
 * @property rawMin 原始min。
 * @property rawMax 原始max。
 * @property engMin 工程min。
 * @property engMax 工程max。
 * @property forwardEnabled 转发启用状态。
 * @property forwardRegisterTypeId 转发寄存器类型 ID。
 * @property forwardRegisterAddress 转发寄存器地址。
 */
data class TagUpdateRequest(
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val registerTypeId: Long,
    val registerAddress: Int,
    val enabled: Boolean = true,
    val defaultValue: String? = null,
    val exceptionValue: String? = null,
    val pointType: PointType? = null,
    val debounceMs: Int? = null,
    val sortIndex: Int = 0,
    val scalingEnabled: Boolean = false,
    val scalingOffset: String? = null,
    val rawMin: String? = null,
    val rawMax: String? = null,
    val engMin: String? = null,
    val engMax: String? = null,
    val forwardEnabled: Boolean = false,
    val forwardRegisterTypeId: Long? = null,
    val forwardRegisterAddress: Int? = null,
)

@Serializable
/**
 * 表示replace标签值texts请求参数。
 *
 * @property items 条目列表。
 */
data class ReplaceTagValueTextsRequest(
    val items: List<TagValueTextInput>,
)

@Serializable
/**
 * 表示标签值text输入。
 *
 * @property rawValue 原始值。
 * @property displayText 显示文本。
 * @property sortIndex 排序序号。
 */
data class TagValueTextInput(
    val rawValue: String,
    val displayText: String,
    val sortIndex: Int = 0,
)

@Serializable
/**
 * 表示标签响应结果。
 *
 * @property id 主键 ID。
 * @property name 名称。
 * @property description 描述。
 * @property dataTypeId 数据类型 ID。
 * @property dataTypeCode 数据类型编码。
 * @property dataTypeName 数据类型名。
 * @property registerTypeId 寄存器类型 ID。
 * @property registerTypeCode 寄存器类型编码。
 * @property registerTypeName 寄存器类型名。
 * @property registerAddress 寄存器地址。
 * @property enabled 是否启用。
 * @property defaultValue 默认值。
 * @property exceptionValue 异常值。
 * @property pointType 点位类型。
 * @property debounceMs 去抖毫秒。
 * @property sortIndex 排序序号。
 * @property scalingEnabled 缩放启用状态。
 * @property scalingOffset 缩放offset。
 * @property rawMin 原始min。
 * @property rawMax 原始max。
 * @property engMin 工程min。
 * @property engMax 工程max。
 * @property forwardEnabled 转发启用状态。
 * @property forwardRegisterTypeId 转发寄存器类型 ID。
 * @property forwardRegisterTypeCode 转发寄存器类型编码。
 * @property forwardRegisterTypeName 转发寄存器类型名。
 * @property forwardRegisterAddress 转发寄存器地址。
 * @property valueTexts 值文本。
 */
data class TagResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val dataTypeId: Long,
    val dataTypeCode: String,
    val dataTypeName: String,
    val registerTypeId: Long,
    val registerTypeCode: String,
    val registerTypeName: String,
    val registerAddress: Int,
    val enabled: Boolean,
    val defaultValue: String?,
    val exceptionValue: String?,
    val pointType: PointType?,
    val debounceMs: Int?,
    val sortIndex: Int,
    val scalingEnabled: Boolean,
    val scalingOffset: String?,
    val rawMin: String?,
    val rawMax: String?,
    val engMin: String?,
    val engMax: String?,
    val forwardEnabled: Boolean,
    val forwardRegisterTypeId: Long?,
    val forwardRegisterTypeCode: String?,
    val forwardRegisterTypeName: String?,
    val forwardRegisterAddress: Int?,
    val valueTexts: List<TagValueTextResponse>,
)

@Serializable
/**
 * 表示标签值text响应结果。
 *
 * @property id 主键 ID。
 * @property rawValue 原始值。
 * @property displayText 显示文本。
 * @property sortIndex 排序序号。
 */
data class TagValueTextResponse(
    val id: Long,
    val rawValue: String,
    val displayText: String,
    val sortIndex: Int,
)

@Serializable
/**
 * 表示标签位置更新请求参数。
 *
 * @property deviceId 设备 ID。
 * @property sortIndex 排序序号。
 */
data class TagPositionUpdateRequest(
    val deviceId: Long,
    val sortIndex: Int = 0,
)
