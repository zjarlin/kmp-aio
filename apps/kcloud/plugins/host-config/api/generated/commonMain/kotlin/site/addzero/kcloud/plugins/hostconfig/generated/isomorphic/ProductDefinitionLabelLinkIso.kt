package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 产品定义与标签定义的关联实体。
 *
 * 独立建模关联表，便于后续在标签关系上继续扩展排序、
 * 来源、权重等元信息，而不是把多对多关系写死成隐式连接。
 */
@Serializable
data class ProductDefinitionLabelLinkIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val sortIndex: Int = 0,
    val product: ProductDefinitionIso = ProductDefinitionIso(),
    val label: LabelDefinitionIso = LabelDefinitionIso()
)