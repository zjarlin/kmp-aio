package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_module_instance")
/**
 * 定义模块instance实体。
 */
interface ModuleInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * 名称。
     */
    val name: String

    /**
     * 排序序号。
     */
    val sortIndex: Int

    /**
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long

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
