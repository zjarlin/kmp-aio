package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType

/**
 * 属性定义实体。
 *
 * 用于描述资产主节点下的遥测、状态或可写属性，
 * 同时为 spec-iot 视图提供底层源数据。
 */
@Entity
@Table(name = "host_config_property_definition")
interface PropertyDefinition : BaseEntity {

    /** 属性标识符，在所属主节点内唯一。 */
    val identifier: String

    /** 属性显示名称。 */
    val name: String

    /** 属性说明。 */
    val description: String?

    /** 工程单位，例如 ℃、V、kWh。 */
    val unit: String?

    /** 是否必填。 */
    val required: Boolean

    /** 是否允许写入。 */
    val writable: Boolean

    /** 是否作为遥测属性对外暴露。 */
    val telemetry: Boolean

    /** 是否允许空值。 */
    val nullable: Boolean

    /** 值长度，常用于字符串或字节数组。 */
    val length: Int?

    /** 扩展属性 JSON 字符串。 */
    val attributesJson: String?

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 兼容旧设备定义关联。 */
    @ManyToOne
    val deviceDefinition: DeviceDefinition?

    /** 所属资产主节点。 */
    @ManyToOne
    val node: AssetNode

    /** 属性值数据类型。 */
    @ManyToOne
    val dataType: DataType
}
