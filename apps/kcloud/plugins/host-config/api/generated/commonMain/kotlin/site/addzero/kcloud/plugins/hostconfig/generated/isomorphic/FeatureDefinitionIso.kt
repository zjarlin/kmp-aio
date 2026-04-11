package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 功能定义实体。
 * 这一层描述资产主节点可调用的功能，
 * 例如控制指令、动作触发以及其入参与出参结构。
 */
@Serializable
data class FeatureDefinitionIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val identifier: String = "",
    val name: String = "",
    val description: String? = null,
    val inputSchema: String? = null,
    val outputSchema: String? = null,
    val asynchronous: Boolean = false,
    val sortIndex: Int = 0,
    val deviceDefinition: DeviceDefinitionIso? = null,
    val node: AssetNodeIso = AssetNodeIso()
)