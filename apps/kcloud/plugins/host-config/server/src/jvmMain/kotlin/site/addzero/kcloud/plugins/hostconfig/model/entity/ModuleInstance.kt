package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

@Entity
@Table(name = "host_config_module_instance")
/**
 * 定义模块instance实体。
 */
interface ModuleInstance : BaseEntity {

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
     * 模块模板。
     */
    val moduleTemplate: ModuleTemplate

    @ManyToOne
    /**
     * 设备。
     */
    val device: Device

    @ManyToOne
    /**
     * 协议。
     *
     * 这里保留协议外键，作为模块归属设备后的冗余上下文，
     * 便于历史数据迁移、跨表导出以及协议模板一致性校验。
     */
    val protocol: ProtocolInstance
}
