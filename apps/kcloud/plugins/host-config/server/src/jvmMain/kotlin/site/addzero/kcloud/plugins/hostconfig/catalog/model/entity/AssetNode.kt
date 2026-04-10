package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Serialized
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate
import site.addzero.kcloud.plugins.hostconfig.model.enums.AssetNodeType

@Entity
@Table(name = "host_config_asset_node")
/**
 * 统一资产树节点实体。
 *
 * 这一层承载产品、设备、属性、功能和标签定义，
 * 后续如果要继续往设备实例、模块、事件等能力扩展，也只需要继续往这棵树里加类型。
 */
interface AssetNode : BaseEntity {

    /** 节点类型。 */
    val nodeType: AssetNodeType

    /** 业务编码。 */
    val code: String

    /** 展示名称。 */
    val name: String

    /** 描述。 */
    val description: String?


    /** 排序值。 */
    val sortIndex: Int

    /** 供应商。 */
    val vendor: String?


    /** 扩展属性 JSON。 */
//    @Serialized
    val attributesJson: String?

    @ManyToOne
            /** 上级节点。 */
    val parent: AssetNode?

    @ManyToOne
            /** 协议模板。 */
    val protocolTemplate: ProtocolTemplate?

    @ManyToOne
            /** 设备类型。 */
    val deviceType: DeviceType?

    @ManyToOne
            /** 数据类型。 */
    val dataType: DataType?

    @OneToMany(mappedBy = "parent")
            /** 下级节点。 */
    val children: List<AssetNode>

    @OneToMany(mappedBy = "asset")
            /** 标签关联。 */
    val labelLinks: List<AssetNodeLabelLink>
}
