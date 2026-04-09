package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_tag_value_text")
/**
 * 定义标签值text实体。
 */
interface TagValueText : EpochBaseEntity {

    /**
     * raw值。
     */
    val rawValue: String

    /**
     * displaytext。
     */
    val displayText: String

    /**
     * 排序序号。
     */
    val sortIndex: Int

    @ManyToOne
    /**
     * 标签。
     */
    val tag: Tag
}
