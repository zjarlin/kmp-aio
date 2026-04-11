package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义协议模板实体。
 */
@Serializable
data class ProtocolTemplateIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val metadataJson: String? = null,
    val sortIndex: Int = 0,
    val moduleTemplates: List<ModuleTemplateIso> = emptyList()
)