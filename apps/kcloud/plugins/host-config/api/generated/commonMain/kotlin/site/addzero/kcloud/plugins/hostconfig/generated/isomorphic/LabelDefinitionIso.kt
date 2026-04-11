package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 标签定义实体。
 * 标签用于给资产主树做语义分类，
 * 例如行业、协议能力、交付形态等维度。
 */
@Serializable
data class LabelDefinitionIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val colorHex: String? = null,
    val sortIndex: Int = 0,
    val productLinks: List<ProductDefinitionLabelLinkIso> = emptyList()
)