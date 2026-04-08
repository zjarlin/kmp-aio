package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table

/**
 * 功能定义实体。
 *
 * 这一层描述设备可调用的功能，
 * 例如控制指令、动作触发以及其入参与出参结构。
 */
@Entity
@Table(name = "host_config_feature_definition")
interface FeatureDefinition {

    /** 数据库主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 功能标识符，在所属设备定义内唯一。 */
    val identifier: String

    /** 功能显示名称。 */
    val name: String

    /** 功能说明。 */
    val description: String?

    /** 输入参数 JSON Schema。 */
    val inputSchema: String?

    /** 输出结果 JSON Schema。 */
    val outputSchema: String?

    /** 是否异步执行。 */
    val asynchronous: Boolean

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 创建时间，使用 epoch millis。 */
    val createdAt: Long

    /** 最近更新时间，使用 epoch millis。 */
    val updatedAt: Long

    /** 所属设备定义。 */
    @ManyToOne
    val deviceDefinition: DeviceDefinition
}
