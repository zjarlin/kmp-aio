package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity

/**
 * MCU 连接档案。
 *
 * 用于保存串口连接草稿和 Modbus RTU 复用参数，供页面编辑、回显和最近使用记录复用。
 */
@Serializable
data class McuTransportProfileIso(
    val id: Long = 0L,
    @Contextual val createTime: Instant = kotlinx.datetime.Clock.System.now(),
    @Contextual val updateTime: Instant? = null,
    val profileKey: String = "",
    val name: String = "",
    val transportKind: McuTransportKind = McuTransportKind.entries.first(),
    val deviceKey: String? = null,
    val portPathHint: String? = null,
    val baudRate: Int? = null,
    val unitId: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: McuModbusSerialParity? = null,
    val timeoutMs: Int? = null,
    val retries: Int? = null,
    val host: String? = null,
    val port: Int? = null,
    val clientId: String? = null,
    val username: String? = null,
    val password: String? = null,
    val publishTopic: String? = null,
    val subscribeTopic: String? = null,
    val qos: Int? = null,
    val keepAliveSeconds: Int? = null,
    @Contextual val lastUsedAt: Instant? = null
)