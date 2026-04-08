package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table

/**
 * 产品定义实体。
 *
 * 这一层描述物联网产品目录中的产品级模板信息，
 * 负责承载型号、供应商、分类以及其下挂的设备定义。
 */
@Entity
@Table(name = "host_config_product_definition")
interface ProductDefinition {

    /** 数据库主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 产品业务编码，要求全局唯一。 */
    @Key
    val code: String

    /** 产品显示名称。 */
    val name: String

    /** 产品说明，供前端详情和编辑页展示。 */
    val description: String?

    /** 供应商名称。 */
    val vendor: String?

    /** 产品分类名称。 */
    val category: String?

    /** 是否启用当前产品定义。 */
    val enabled: Boolean

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 创建时间，使用 epoch millis。 */
    val createdAt: Long

    /** 最近更新时间，使用 epoch millis。 */
    val updatedAt: Long

    /** 产品下挂的设备定义列表。 */
    @OneToMany(mappedBy = "product")
    val devices: List<DeviceDefinition>

    /** 产品与标签的关联关系。 */
    @OneToMany(mappedBy = "product")
    val labelLinks: List<ProductDefinitionLabelLink>
}
