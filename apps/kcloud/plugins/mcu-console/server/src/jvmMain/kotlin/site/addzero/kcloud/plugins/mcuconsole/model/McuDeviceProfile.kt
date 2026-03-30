package site.addzero.kcloud.plugins.mcuconsole.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import java.time.Instant

/**
 * MCU 设备档案。
 *
 * 备注、厂商信息和最近一次识别到的串口信息都按稳定设备标识持久化，
 * 不能直接依赖当前瞬时串口号。
 */
@Entity
@Table(name = "mcu_device_profile")
interface McuDeviceProfile : BaseEntity {
    /** 设备稳定主键，优先用于备注和配置回填。 */
    @Key
    @Column(name = "device_key")
    val deviceKey: String

    /** 设备序列号。 */
    @Column(name = "serial_number")
    val serialNumber: String?

    /** 设备厂商名称。 */
    val manufacturer: String?

    /** USB Vendor ID。 */
    @Column(name = "vendor_id")
    val vendorId: Int?

    /** USB Product ID。 */
    @Column(name = "product_id")
    val productId: Int?

    /** 用户填写的设备备注。 */
    val remark: String?

    /** 最近一次观测到的串口路径，仅用于回显和兜底。 */
    @Column(name = "last_port_path")
    val lastPortPath: String?

    /** 最近一次观测到的串口显示名。 */
    @Column(name = "last_port_name")
    val lastPortName: String?

    /** 最近一次发现该设备的时间。 */
    @Column(name = "last_seen_at")
    val lastSeenAt: Instant?
}
