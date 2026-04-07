package site.addzero.kcloud.plugins.hostconfig.api.template

import kotlinx.serialization.Serializable

@Serializable
data class TemplateOptionResponse(
    val id: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
)

@Serializable
data class ModuleTemplateOptionResponse(
    val id: Long,
    val protocolTemplateId: Long,
    val code: String,
    val name: String,
    val description: String?,
    val sortIndex: Int,
    val channelCount: Int?,
)
