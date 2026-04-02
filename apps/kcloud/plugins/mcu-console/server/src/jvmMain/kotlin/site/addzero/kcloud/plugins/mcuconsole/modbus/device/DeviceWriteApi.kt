package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.annotation.ModbusParam
import site.addzero.device.protocol.modbus.model.ModbusCodec
import site.addzero.device.protocol.modbus.model.ModbusCommandResult
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 板卡写侧的 Modbus RTU 契约。
 *
 * 当前把故障灯/运行灯写线圈放在 24 路电源灯之后的 `24..25`，
 * 目的是和现有 24 路线圈读布局连续对齐。
 * 如果固件寄存器表不同，需要和下位机一起同步调整这里。
 */
interface DeviceWriteApi {
    /**
     * 前端按钮: Modbus 页“设置故障灯/运行灯”。
     * 写入板卡状态指示灯。
     */
    @ModbusOperation(
        address = 24,
        functionCode = ModbusFunctionCode.WRITE_MULTIPLE_COILS,
    )
    suspend fun writeIndicatorLights(
        @ModbusParam(order = 0, codec = ModbusCodec.BOOL_COIL)
        faultLightOn: Boolean,
        @ModbusParam(order = 1, codec = ModbusCodec.BOOL_COIL)
        runLightOn: Boolean,
    ): ModbusCommandResult
}
