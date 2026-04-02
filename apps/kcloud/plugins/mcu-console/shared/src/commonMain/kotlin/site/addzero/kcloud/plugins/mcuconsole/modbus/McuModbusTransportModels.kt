package site.addzero.kcloud.plugins.mcuconsole.modbus

import kotlinx.serialization.Serializable

@Serializable
enum class McuModbusSerialParity {
    NONE,
    EVEN,
    ODD,
}

@Serializable
enum class McuModbusFrameFormat {
    RTU,
    ASCII,
}

@Serializable
data class McuModbusCommandConfig(
    val portPath: String? = null,
    val baudRate: Int = 115200,
    val unitId: Int = 1,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: McuModbusSerialParity = McuModbusSerialParity.NONE,
    val timeoutMs: Long = 1000,
    val retries: Int = 2,
)
