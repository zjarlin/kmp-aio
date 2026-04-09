package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_device_type")
/**
 * 定义设备类型实体。
 */
interface DeviceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

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
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long
}
