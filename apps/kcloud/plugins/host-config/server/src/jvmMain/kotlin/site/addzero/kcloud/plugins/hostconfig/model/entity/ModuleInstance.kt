package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_module_instance")
/**
 * 定义模块instance实体。
 */
interface ModuleInstance : EpochBaseEntity {

    /**
     * 名称。
     */
    val name: String

    /**
     * 排序序号。
     */
    val sortIndex: Int

    @ManyToOne
    /**
     * 协议。
     */
    val protocol: ProtocolInstance

    @ManyToOne
    /**
     * 模块模板。
     */
    val moduleTemplate: ModuleTemplate

    @OneToMany(mappedBy = "module")
    /**
     * 设备。
     */
    val devices: List<Device>
}
