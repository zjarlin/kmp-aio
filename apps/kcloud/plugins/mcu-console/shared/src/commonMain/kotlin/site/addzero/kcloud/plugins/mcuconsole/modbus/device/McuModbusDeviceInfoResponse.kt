package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.serialization.Serializable

@Serializable
data class McuModbusDeviceInfoResponse(
    val success: Boolean = false,
    val portPath: String? = null,
    val protocolVersion: Int? = null,
    val channelCount: Int? = null,
    val unitId: Int? = null,
    val baudRateCode: Int? = null,
    val deviceName: String? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)
