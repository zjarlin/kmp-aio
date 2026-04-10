package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "host_config_module_template")
/**
 * 定义模块模板实体。
 */
interface ModuleTemplate : BaseEntity {

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

    /**
     * channelcount。
     */
    val channelCount: Int?

    @ManyToOne
    /**
     * 协议模板。
     */
    val protocolTemplate: ProtocolTemplate
}
