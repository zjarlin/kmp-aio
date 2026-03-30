@file:OptIn(ExperimentalTime::class)


package site.addzero.kcloud.plugins.mcuconsole

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.mcuconsole.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind

@Serializable
data class McuTransportProfileIso(
    val id: Long] = TODO(),
    @Contextual val createTime: Instant = Clock.System.now(),
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