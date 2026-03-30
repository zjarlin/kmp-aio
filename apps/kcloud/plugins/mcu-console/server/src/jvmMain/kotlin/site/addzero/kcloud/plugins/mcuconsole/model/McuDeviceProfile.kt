package site.addzero.kcloud.plugins.mcuconsole.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import java.time.Instant

@Entity
@Table(name = "mcu_device_profile")
interface McuDeviceProfile : BaseEntity {
    @Key
    @Column(name = "device_key")
    val deviceKey: String

    @Column(name = "serial_number")
    val serialNumber: String?

    val manufacturer: String?

    @Column(name = "vendor_id")
    val vendorId: Int?

    @Column(name = "product_id")
    val productId: Int?

    val remark: String?

    @Column(name = "last_port_path")
    val lastPortPath: String?

    @Column(name = "last_port_name")
    val lastPortName: String?

    @Column(name = "last_seen_at")
    val lastSeenAt: Instant?
}
