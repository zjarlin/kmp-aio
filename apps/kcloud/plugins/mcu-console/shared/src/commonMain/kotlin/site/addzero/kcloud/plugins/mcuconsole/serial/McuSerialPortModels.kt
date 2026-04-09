package site.addzero.kcloud.plugins.mcuconsole.serial

import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示mcu串口端口配置。
 *
 * @property portName 端口名。
 * @property baudRate 波特率。
 * @property dataBits 数据位。
 * @property stopBits 停止位。
 * @property parity 校验位。
 * @property flowControl flow控制。
 * @property readTimeoutMs read超时时间（毫秒）。
 * @property writeTimeoutMs write超时时间（毫秒）。
 * @property openSafetySleepTimeMs 打开safetysleeptime毫秒。
 */
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
/**
 * 表示mcu串口端口descriptor。
 *
 * @property systemPortName system端口名。
 * @property systemPortPath system端口路径。
 * @property descriptivePortName descriptive端口名。
 * @property portDescription 端口描述。
 * @property portLocation 端口location。
 * @property manufacturer manufacturer。
 * @property serialNumber 序列号。
 * @property vendorId vendor ID。
 * @property productId product ID。
 */
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
/**
 * 定义mcu串口停止位枚举。
 */
enum class McuSerialStopBits {
    ONE,
    ONE_POINT_FIVE,
    TWO,
}

@Serializable
/**
 * 定义mcu串口校验位枚举。
 */
enum class McuSerialParity {
    NONE,
    EVEN,
    ODD,
    MARK,
    SPACE,
}

@Serializable
/**
 * 定义mcu串口flow控制枚举。
 */
enum class McuSerialFlowControl {
    NONE,
    RTS_CTS,
    DTR_DSR,
    XON_XOFF,
}
