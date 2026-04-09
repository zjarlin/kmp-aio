package site.addzero.kcloud.plugins.mcuconsole.serial

import kotlinx.serialization.Serializable

@Serializable
data class McuSerialPortConfig(
    val portName: String,
    val baudRate: Int = 9600,
    val dataBits: Int = 8,
    val stopBits: McuSerialStopBits = McuSerialStopBits.ONE,
    val parity: McuSerialParity = McuSerialParity.NONE,
    val flowControl: McuSerialFlowControl = McuSerialFlowControl.NONE,
    val readTimeoutMs: Int = 1000,
    val writeTimeoutMs: Int = 1000,
    val openSafetySleepTimeMs: Int = 0,
)

@Serializable
data class McuSerialPortDescriptor(
    val systemPortName: String,
    val systemPortPath: String,
    val descriptivePortName: String,
    val portDescription: String,
    val portLocation: String? = null,
    val manufacturer: String? = null,
    val serialNumber: String? = null,
    val vendorId: Int? = null,
    val productId: Int? = null,
)

@Serializable
enum class McuSerialStopBits {
    ONE,
    ONE_POINT_FIVE,
    TWO,
}

@Serializable
enum class McuSerialParity {
    NONE,
    EVEN,
    ODD,
    MARK,
    SPACE,
}

@Serializable
enum class McuSerialFlowControl {
    NONE,
    RTS_CTS,
    DTR_DSR,
    XON_XOFF,
}
