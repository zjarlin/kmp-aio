package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

/**
 * 功能定义实体。
 *
 * 这一层描述资产主节点可调用的功能，
 * 例如控制指令、动作触发以及其入参与出参结构。
 */
@Entity
@Table(name = "host_config_feature_definition")
interface FeatureDefinition : BaseEntity {

    /** 功能标识符，在所属主节点内唯一。 */
    val identifier: String

    /** 功能显示名称。 */
    val name: String

    /** 功能说明。 */
    val description: String?

    /**
     * 输入结构。
     */
    val inputSchema: String?

    /**
     * 输出结构。
     */
    val outputSchema: String?

    /** 是否异步执行。 */
    val asynchronous: Boolean

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 兼容旧设备定义关联。 */
    @ManyToOne
    val deviceDefinition: DeviceDefinition?

    /** 所属资产主节点。 */
    @ManyToOne
    val node: AssetNode
}
