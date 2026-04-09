package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示mcumodbus设备info响应结果。
 *
 * @property success success。
 * @property portPath 端口路径。
 * @property firmwareVersion 固件version。
 * @property cpuModel cpu模型。
 * @property xtalFrequencyHz xtalfrequencyhz。
 * @property flashSizeBytes 烧录size字节。
 * @property macAddress mac地址。
 * @property lastMessage 最近一条消息。
 * @property updatedAt 更新时间戳。
 */
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
