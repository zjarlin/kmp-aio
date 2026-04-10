package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 统一资产树节点类型。
 *
 * 这里不再把“产品表 / 设备表 / 属性表 / 功能表”拆成多棵树，
 * 而是统一收敛到一棵资产树里，再用节点类型表达语义。
 */
enum class AssetNodeType {
    ASSET,
    PROPERTY,
    SERVICE,
    LABEL,
}
