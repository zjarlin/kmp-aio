package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 统一资产主树节点类型。
 *
 * `AssetNode` 只承载产品、设备、模块这三类主节点。
 * 属性、功能、标签等明细能力继续独立建模，通过 `nodeId` 关联到主树。
 */
enum class AssetNodeType {
    PRODUCT,
    DEVICE,
    MODULE,
}
