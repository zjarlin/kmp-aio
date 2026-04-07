package site.addzero.kcloud.plugins.hostconfig.protocols

import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse

data class ProtocolsScreenState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val protocolTemplates: List<TemplateOptionResponse> = emptyList(),
    val selectedProtocolTemplateId: Long? = null,
    val moduleTemplates: List<ModuleTemplateOptionResponse> = emptyList(),
) {
    val selectedProtocolTemplate: TemplateOptionResponse?
        get() = protocolTemplates.firstOrNull { item -> item.id == selectedProtocolTemplateId }
}
