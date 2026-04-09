package site.addzero.kcloud.plugins.hostconfig.protocols

import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse

/**
 * 表示协议界面状态。
 *
 * @property loading 加载状态。
 * @property errorMessage 错误消息。
 * @property protocolTemplates 协议模板。
 * @property selectedProtocolTemplateId 选中协议模板 ID。
 * @property moduleTemplates 模块模板。
 */
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
