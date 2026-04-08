package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table

/**
 * 标签定义实体。
 *
 * 标签用于给产品目录做语义分类，
 * 例如行业、协议能力、交付形态等维度。
 */
@Entity
@Table(name = "host_config_label_definition")
interface LabelDefinition {

    /** 数据库主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 标签业务编码，要求全局唯一。 */
    @Key
    val code: String

    /** 标签显示名称。 */
    val name: String

    /** 标签说明。 */
    val description: String?

    /** 前端展示颜色，例如十六进制色值。 */
    val colorHex: String?

    /** 同级排序值，越小越靠前。 */
    val sortIndex: Int

    /** 创建时间，使用 epoch millis。 */
    val createdAt: Long

    /** 最近更新时间，使用 epoch millis。 */
    val updatedAt: Long

    /** 标签与产品的关联关系。 */
    @OneToMany(mappedBy = "label")
    val productLinks: List<ProductDefinitionLabelLink>
}
