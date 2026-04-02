package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.serialization.Serializable

/**
 * Modbus 状态指示灯写请求。
 */
@Serializable
data class McuModbusIndicatorLightsRequest(
    val faultLightOn: Boolean = false,
    val runLightOn: Boolean = false,
)
