package site.addzero.kcloud.plugins.mcuconsole.model

import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity

/**
 * MCU 设备配置文件
 *
 * 备注、厂商信息和最近一次识别到的串口信息都按稳定设备标识持久化，
 * 不能直接依赖当前瞬时串口号。
 */
@Entity
@Table(name = "mcu_device_profile")
interface McuDeviceProfile : BaseEntity {

    /** 设备稳定主键，优先用于备注和配置回填。 */
    @Key
    val deviceKey: String

    /** 设备厂商名称。 */
    val manufacturer: String?

    /** 用户填写的设备备注。 */
    val remark: String?

}
