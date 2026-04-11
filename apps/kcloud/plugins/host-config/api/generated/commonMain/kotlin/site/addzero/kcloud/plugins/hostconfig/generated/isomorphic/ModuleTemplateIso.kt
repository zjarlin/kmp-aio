package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义模块模板实体。
 */
@Serializable
data class ModuleTemplateIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0,
    val channelCount: Int? = null,
    val protocolTemplate: ProtocolTemplateIso = ProtocolTemplateIso()
)