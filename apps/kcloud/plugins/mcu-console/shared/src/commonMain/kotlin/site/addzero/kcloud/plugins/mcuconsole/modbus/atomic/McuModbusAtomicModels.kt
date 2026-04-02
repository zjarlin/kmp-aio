package site.addzero.kcloud.plugins.mcuconsole.modbus.atomic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity

enum class McuModbusAtomicAction {
    GPIO_WRITE,
    GPIO_MODE,
    PWM_DUTY,
    SERVO_ANGLE,
}

@Serializable
enum class McuModbusGpioMode {
    INPUT,
    OUTPUT,
    INPUT_PULL_UP,
}

@Serializable
data class McuModbusCommandResponse(
    val accepted: Boolean = false,
    val summary: String = "",
)

@Serializable
data class McuModbusGpioWriteRequest(
    val portPath: String? = null,
    val unitId: Int? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: McuModbusSerialParity? = null,
    val timeoutMs: Long? = null,
    val retries: Int? = null,
    val pin: Int = 0,
    val high: Boolean = false,
)

@Serializable
data class McuModbusGpioModeRequest(
    val portPath: String? = null,
    val unitId: Int? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: McuModbusSerialParity? = null,
    val timeoutMs: Long? = null,
    val retries: Int? = null,
    val pin: Int = 0,
    val mode: Int = 0,
)

@Serializable
data class McuModbusPwmDutyRequest(
    val portPath: String? = null,
    val unitId: Int? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: McuModbusSerialParity? = null,
    val timeoutMs: Long? = null,
    val retries: Int? = null,
    val pin: Int = 0,
    val dutyU16: Int = 0,
)

@Serializable
data class McuModbusServoAngleRequest(
    val portPath: String? = null,
    val unitId: Int? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: McuModbusSerialParity? = null,
    val timeoutMs: Long? = null,
    val retries: Int? = null,
    val pin: Int = 0,
    val angle: Int = 0,
)
