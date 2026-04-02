package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.serialization.Serializable

/**
 * Modbus 状态指示灯写结果。
 */
@Serializable
data class McuModbusIndicatorLightsResponse(
    val success: Boolean = false,
    val portPath: String? = null,
    val faultLightOn: Boolean = false,
    val runLightOn: Boolean = false,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)
