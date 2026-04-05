package site.addzero.kcloud.plugins.mcuconsole.modbus.atomic

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * MCU Modbus 原子动作路由，同时作为客户端 API 生成源。
 *
 * 当前原子动作统一复用已经打开的串口会话。
 * 请求体里保留的连接参数字段仅用于兼容现有前端模型，不在这里直接参与会话建立。
 */

/**
 * 前端按钮: Modbus 页“执行 GPIO 电平”。
 * 作用: 基于当前会话执行单次 GPIO 电平写入。
 */
@PostMapping("/api/mcu/modbus/rtu/automic-modbus/gpio-write")
suspend fun writeMcuModbusGpio(
    @RequestBody request: McuModbusGpioWriteRequest,
): McuModbusCommandResponse {
    val result = atomicModbusService().gpioWrite(
        pin = request.pin,
        high = request.high,
    )
    return result.toResponse()
}

/**
 * 前端按钮: Modbus 页“执行 GPIO 模式”。
 * 作用: 基于当前会话设置 GPIO 模式。
 */
@PostMapping("/api/mcu/modbus/rtu/automic-modbus/gpio-mode")
suspend fun writeMcuModbusGpioMode(
    @RequestBody request: McuModbusGpioModeRequest,
): McuModbusCommandResponse {
    val result = atomicModbusService().gpioMode(
        pin = request.pin,
        mode = request.mode.toAutomicGpioMode(),
    )
    return result.toResponse()
}

/**
 * 前端按钮: Modbus 页“执行 PWM 占空比”。
 * 作用: 基于当前会话设置 PWM 占空比。
 */
@PostMapping("/api/mcu/modbus/rtu/automic-modbus/pwm-duty")
suspend fun writeMcuModbusPwmDuty(
    @RequestBody request: McuModbusPwmDutyRequest,
): McuModbusCommandResponse {
    val result = atomicModbusService().pwmDuty(
        pin = request.pin,
        dutyU16 = request.dutyU16,
    )
    return result.toResponse()
}

/**
 * 前端按钮: Modbus 页“执行舵机角度”。
 * 作用: 基于当前会话设置舵机角度。
 */
@PostMapping("/api/mcu/modbus/rtu/automic-modbus/servo-angle")
suspend fun writeMcuModbusServoAngle(
    @RequestBody request: McuModbusServoAngleRequest,
): McuModbusCommandResponse {
    val result = atomicModbusService().servoAngle(
        pin = request.pin,
        angle = request.angle,
    )
    return result.toResponse()
}

private fun atomicModbusService(): AutomicModbusService {
    return KoinPlatform.getKoin().get()
}

private fun Int.toAutomicGpioMode(): AutomicGpioMode {
    return when (this) {
        0 -> AutomicGpioMode.INPUT
        1 -> AutomicGpioMode.OUTPUT
        2 -> AutomicGpioMode.INPUT_PULL_UP
        else -> error("不支持的 GPIO 模式编码: $this")
    }
}

private fun site.addzero.device.protocol.modbus.model.ModbusCommandResult.toResponse(): McuModbusCommandResponse {
    return McuModbusCommandResponse(
        accepted = accepted,
        summary = summary,
    )
}
