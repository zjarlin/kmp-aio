package site.addzero.kcloud.plugins.mcuconsole.service.modbus

import site.addzero.device.protocol.modbus.annotation.GenerateModbusRtuServer
import site.addzero.device.protocol.modbus.annotation.ModbusDeviceApi
import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.annotation.ModbusParam
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusCommandResult

/**
 * MCU 原子动作的 RTU 契约。
 *
 * 这里只保留单次可执行的硬件动作，不把批量脚本逻辑揉进 Modbus 层。
 */
@GenerateModbusRtuServer
@ModbusDeviceApi(
    serviceId = "automic-modbus",
    summary = "MCU GPIO/PWM 原子动作",
    basePath = "/api/mcu/modbus",
)
interface AutomicModbusApi {
    /**
     * 前端按钮: Modbus 页“执行 GPIO 电平”。
     * 设置 GPIO 输出电平。
     */
    @ModbusOperation(
        operationId = "gpio-write",
        address = 1024,
        quantity = 2,
    )
    suspend fun gpioWrite(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        pin: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.BOOL_COIL, registerOffset = 1)
        high: Boolean,
    ): ModbusCommandResult

    /**
     * 前端按钮: Modbus 页“执行 GPIO 模式”。
     * 设置 GPIO 模式。
     */
    @ModbusOperation(
        operationId = "gpio-mode",
        address = 1026,
        quantity = 2,
    )
    suspend fun gpioMode(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        pin: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.U16, registerOffset = 1)
        mode: Int,
    ): ModbusCommandResult

    /**
     * 前端按钮: Modbus 页“执行 PWM 占空比”。
     * 设置 PWM 占空比。
     */
    @ModbusOperation(
        operationId = "pwm-duty",
        address = 1028,
        quantity = 2,
    )
    suspend fun pwmDuty(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        pin: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.U16, registerOffset = 1)
        dutyU16: Int,
    ): ModbusCommandResult

    /**
     * 前端按钮: Modbus 页“执行舵机角度”。
     * 设置舵机角度。
     */
    @ModbusOperation(
        operationId = "servo-angle",
        address = 1030,
        quantity = 2,
    )
    suspend fun servoAngle(
        @ModbusParam(order = 0, codec = ModbusCodec.U16)
        pin: Int,
        @ModbusParam(order = 1, codec = ModbusCodec.U16, registerOffset = 1)
        angle: Int,
    ): ModbusCommandResult
}
