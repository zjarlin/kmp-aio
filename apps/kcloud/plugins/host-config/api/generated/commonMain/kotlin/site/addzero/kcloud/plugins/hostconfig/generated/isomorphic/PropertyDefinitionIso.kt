package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 属性定义实体。
 * 用于描述资产主节点下的遥测、状态或可写属性，
 * 同时为 spec-iot 视图提供底层源数据。
 */
@Serializable
data class PropertyDefinitionIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val identifier: String = "",
    val name: String = "",
    val description: String? = null,
    val unit: String? = null,
    val required: Boolean = false,
    val writable: Boolean = false,
    val telemetry: Boolean = false,
    val nullable: Boolean = false,
    val length: Int? = null,
    val attributesJson: String? = null,
    val sortIndex: Int = 0,
    val deviceDefinition: DeviceDefinitionIso? = null,
    val node: AssetNodeIso = AssetNodeIso(),
    val dataType: DataTypeIso = DataTypeIso()
)