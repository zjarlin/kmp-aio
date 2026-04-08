package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * MCU 设备配置文件
 *
 * 备注、厂商信息和最近一次识别到的串口信息都按稳定设备标识持久化，
 * 不能直接依赖当前瞬时串口号。
 */
@Serializable
data class McuDeviceProfileIso(
    val id: Long = 0L,
    @Contextual val createTime: Instant = kotlinx.datetime.Clock.System.now(),
    @Contextual val updateTime: Instant? = null,
    val deviceKey: String = "",
    val manufacturer: String? = null,
    val remark: String? = null
)