package site.addzero.kcloud.plugins.hostconfig.api.template

import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示模板选项响应结果。
 *
 * @property id 主键 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 */
data class TemplateOptionResponse(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
)

@Serializable
/**
 * 表示模块模板选项响应结果。
 *
 * @property id 主键 ID。
 * @property protocolTemplateId 协议模板 ID。
 * @property code 编码。
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property channelCount channelcount。
 */
data class ModuleTemplateOptionResponse(
    val id: Long,
    val protocolTemplateId: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
    val channelCount: Int?,
)
