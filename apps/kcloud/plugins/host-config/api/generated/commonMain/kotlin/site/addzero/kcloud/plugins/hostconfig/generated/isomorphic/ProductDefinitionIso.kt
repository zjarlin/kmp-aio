package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 产品定义实体。
 * 这一层描述物联网产品目录中的产品级模板信息，
 * 负责承载型号、供应商、分类以及其下挂的设备定义。
 */
@Serializable
data class ProductDefinitionIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val vendor: String? = null,
    val category: String? = null,
    val enabled: Boolean = false,
    val sortIndex: Int = 0,
    val devices: List<DeviceDefinitionIso> = emptyList(),
    val labelLinks: List<ProductDefinitionLabelLinkIso> = emptyList()
)