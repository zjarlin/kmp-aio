package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "host_config_asset_node_label")
/**
 * 统一资产树节点与标签节点关联。
 */
interface AssetNodeLabelLink : BaseEntity {

    /** 同一资产节点下的标签排序。 */
    val sortIndex: Int

    @ManyToOne
    /** 资产节点。 */
    val asset: AssetNode

    @ManyToOne
    /** 标签节点。 */
    val label: AssetNode
}
