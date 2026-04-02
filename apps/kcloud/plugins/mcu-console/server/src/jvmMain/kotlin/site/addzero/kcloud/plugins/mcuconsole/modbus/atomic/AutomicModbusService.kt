package site.addzero.kcloud.plugins.mcuconsole.modbus.atomic

import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.protocol.modbus.model.ModbusCommandResult
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService

enum class AutomicGpioMode(
    val code: Int,
) {
    INPUT(0),
    OUTPUT(1),
    INPUT_PULL_UP(2),
}

@Single
class AutomicModbusService(
    private val sessionService: McuConsoleSessionService,
    private val gateway: AutomicModbusApiGeneratedRtuGateway,
) {
    suspend fun gpioWrite(
        pin: Int,
        high: Boolean,
    ): ModbusCommandResult {
        return gateway.gpioWrite(
            config = activeConfig(),
            pin = normalizePin(pin),
            high = high,
        )
    }

    suspend fun gpioHigh(
        pin: Int,
    ): ModbusCommandResult {
        return gpioWrite(pin = pin, high = true)
    }

    suspend fun gpioLow(
        pin: Int,
    ): ModbusCommandResult {
        return gpioWrite(pin = pin, high = false)
    }

    suspend fun gpioMode(
        pin: Int,
        mode: AutomicGpioMode,
    ): ModbusCommandResult {
        return gateway.gpioMode(
            config = activeConfig(),
            pin = normalizePin(pin),
            mode = mode.code,
        )
    }

    suspend fun pwmDuty(
        pin: Int,
        dutyU16: Int,
    ): ModbusCommandResult {
        require(dutyU16 in 0..0xFFFF) { "dutyU16 must be in 0..65535" }
        return gateway.pwmDuty(
            config = activeConfig(),
            pin = normalizePin(pin),
            dutyU16 = dutyU16,
        )
    }

    suspend fun servoAngle(
        pin: Int,
        angle: Int,
    ): ModbusCommandResult {
        require(angle in 0..180) { "angle must be in 0..180" }
        return gateway.servoAngle(
            config = activeConfig(),
            pin = normalizePin(pin),
            angle = angle,
        )
    }

    private fun activeConfig(): ModbusRtuEndpointConfig {
        val session = sessionService.getSessionSnapshot()
        val portPath = session.portPath?.takeIf { value -> value.isNotBlank() }
            ?: error("请先打开串口会话，再执行 Modbus 原子操作")
        return gateway.defaultConfig().copy(
            portPath = portPath,
            baudRate = session.baudRate,
        )
    }

    private fun normalizePin(
        pin: Int,
    ): Int {
        require(pin in 0..255) { "pin must be in 0..255" }
        return pin
    }
}
