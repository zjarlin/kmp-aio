package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.serialization.Serializable

@Serializable
data class McuModbusDeviceInfoResponse(
    val success: Boolean = false,
    val portPath: String? = null,
    val firmwareVersion: String? = null,
    val cpuModel: String? = null,
    val xtalFrequencyHz: Int? = null,
    val flashSizeBytes: Int? = null,
    val macAddress: String? = null,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)
