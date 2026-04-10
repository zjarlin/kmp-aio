package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType
import site.addzero.kcloud.plugins.hostconfig.model.entity.ModuleTemplate
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate
import site.addzero.kcloud.plugins.hostconfig.model.enums.AssetNodeType

@Entity
@Table(name = "host_config_asset_node")
/**
 * 统一资产主树节点实体。
 *
 * 这里只承载产品、设备、模块这三类主节点，
 * 物模型、功能、标签等明细能力都通过独立表围绕 `nodeId` 关联。
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

    /** 是否启用。 */
    val enabled: Boolean

    /** 排序值。 */
    val sortIndex: Int

    /** 供应商。 */
    val vendor: String?

    /** 分类。 */
    val category: String?

    /** 是否支持遥测。 */
    val supportsTelemetry: Boolean

    /** 是否支持控制。 */
    val supportsControl: Boolean

    /** 上级节点。 */
    @ManyToOne
    val parent: AssetNode?

    /** 协议模板，通常用于产品级节点。 */
    @ManyToOne
    val protocolTemplate: ProtocolTemplate?

    /** 设备类型，通常用于设备级节点。 */
    @ManyToOne
    val deviceType: DeviceType?

    /** 模块模板，通常用于模块级节点。 */
    @ManyToOne
    val moduleTemplate: ModuleTemplate?

    /** 下级节点。 */
    @OneToMany(mappedBy = "parent")
    val children: List<AssetNode>

    /** 节点标签关联。 */
    @OneToMany(mappedBy = "asset")
    val labelLinks: List<AssetNodeLabelLink>

    /** 节点属性定义。 */
    @OneToMany(mappedBy = "node")
    val properties: List<PropertyDefinition>

    /** 节点功能定义。 */
    @OneToMany(mappedBy = "node")
    val features: List<FeatureDefinition>
}
