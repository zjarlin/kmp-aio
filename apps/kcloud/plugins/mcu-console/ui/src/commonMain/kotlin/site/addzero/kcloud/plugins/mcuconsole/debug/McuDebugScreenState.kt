package site.addzero.kcloud.plugins.mcuconsole.debug

import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortConfig
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortDescriptor

/**
 * MCU 调试页状态。
 */
data class McuDebugScreenState(
    val portName: String = "/dev/cu.usbserial-2140",
    val baudRateInput: String = "115200",
    val ports: List<McuSerialPortDescriptor> = emptyList(),
    val logs: List<String> = emptyList(),
    val connecting: Boolean = false,
    val streaming: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = "默认按真实板卡 `/dev/cu.usbserial-2140 + 115200` 连接。",
) {
    /**
     * 生成当前串口配置。
     */
    fun toSerialPortConfigOrNull(): McuSerialPortConfig? {
        val baudRate = baudRateInput.toIntOrNull() ?: return null
        if (portName.isBlank()) {
            return null
        }
        return McuSerialPortConfig(
            portName = portName,
            baudRate = baudRate,
            readTimeoutMs = 200,
        )
    }
}
