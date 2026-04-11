package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.AssetNodeType

/**
 * 统一资产主树节点实体。
 *
 * 这里只承载产品、设备、模块这三类主节点，
 * 物模型、功能、标签等明细能力都通过独立表围绕 `nodeId` 关联。
 */
@Serializable
data class AssetNodeIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val nodeType: AssetNodeType = AssetNodeType.entries.first(),
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val enabled: Boolean = false,
    val sortIndex: Int = 0,
    val vendor: String? = null,
    val category: String? = null,
    val supportsTelemetry: Boolean = false,
    val supportsControl: Boolean = false,
    val parent: AssetNodeIso? = null,
    val protocolTemplate: ProtocolTemplateIso? = null,
    val deviceType: DeviceTypeIso? = null,
    val moduleTemplate: ModuleTemplateIso? = null,
    val children: List<AssetNodeIso> = emptyList(),
    val labelLinks: List<AssetNodeLabelLinkIso> = emptyList(),
    val properties: List<PropertyDefinitionIso> = emptyList(),
    val features: List<FeatureDefinitionIso> = emptyList()
)