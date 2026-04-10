package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "host_config_register_type")
/**
 * 定义register类型实体。
 */
interface RegisterType : BaseEntity {

    @Key
    /**
     * 编码。
     */
    val code: String

    /**
     * 名称。
     */
    val name: String

    /**
     * 描述。
     */
    val description: String?

    /**
     * 排序序号。
     */
    val sortIndex: Int

}
