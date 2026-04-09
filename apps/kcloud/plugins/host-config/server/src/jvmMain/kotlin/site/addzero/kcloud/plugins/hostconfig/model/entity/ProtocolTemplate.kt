package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_protocol_template")
/**
 * 定义协议模板实体。
 */
interface ProtocolTemplate : EpochBaseEntity {

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

    @OneToMany(mappedBy = "protocolTemplate")
    /**
     * 模块模板。
     */
    val moduleTemplates: List<ModuleTemplate>
}
