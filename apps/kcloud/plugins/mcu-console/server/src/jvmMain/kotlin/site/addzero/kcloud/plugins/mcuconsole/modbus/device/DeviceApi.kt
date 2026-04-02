package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusOperation
import site.addzero.device.protocol.modbus.model.ModbusFunctionCode

/**
 * 板卡基础信息的 Modbus RTU 契约。
 *
 * 当前 `get24PowerLights()` 与 `getDeviceInfo()` 的地址布局，
 * 先对齐仓库里已有的 Modbus smoke 契约与 24 路线圈读测试：
 * - 24 路灯状态: `READ_COILS @ 0`
 * - 设备运行信息: `READ_INPUT_REGISTERS @ 100`
 *
 * 如果固件真实寄存器表不同，需要和下位机一起同步调整这里。
 */
interface DeviceApi {
    /**
     * 前端按钮: Modbus 页“读取 24 路电源灯”。
     * 读取 24 路电源灯状态。
     */
    @ModbusOperation(
        address = 0,
        functionCode = ModbusFunctionCode.READ_COILS,
    )
    suspend fun get24PowerLights(): Device24PowerLights

    /**
     * 前端按钮: Modbus 页“读取设备信息”。
     * 读取板子的运行信息。
     */
    @ModbusOperation(
        address = 100,
        functionCode = ModbusFunctionCode.READ_INPUT_REGISTERS,
    )
    suspend fun getDeviceInfo(): DeviceRuntimeInfo
}
