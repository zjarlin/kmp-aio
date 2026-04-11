package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 资产主节点与标签定义关联。
 */
@Serializable
data class AssetNodeLabelLinkIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val sortIndex: Int = 0,
    val asset: AssetNodeIso = AssetNodeIso(),
    val label: LabelDefinitionIso = LabelDefinitionIso()
)