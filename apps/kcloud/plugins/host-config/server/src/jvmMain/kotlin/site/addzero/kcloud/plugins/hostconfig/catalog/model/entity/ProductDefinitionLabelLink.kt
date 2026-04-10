package site.addzero.kcloud.plugins.hostconfig.catalog.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

/**
 * 产品定义与标签定义的关联实体。
 *
 * 独立建模关联表，便于后续在标签关系上继续扩展排序、
 * 来源、权重等元信息，而不是把多对多关系写死成隐式连接。
 */
@Entity
@Table(name = "host_config_product_definition_label")
interface ProductDefinitionLabelLink : BaseEntity {

    /** 同一产品下标签展示顺序。 */
    val sortIndex: Int

    /** 关联的产品定义。 */
    @ManyToOne
    val product: ProductDefinition

    /** 关联的标签定义。 */
    @ManyToOne
    val label: LabelDefinition
}
