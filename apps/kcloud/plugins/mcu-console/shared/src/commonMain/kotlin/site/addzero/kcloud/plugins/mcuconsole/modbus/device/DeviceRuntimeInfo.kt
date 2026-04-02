package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusField
import site.addzero.device.protocol.modbus.model.ModbusCodec

/**
 * 板卡运行信息。
 */
data class DeviceRuntimeInfo(
    /** 协议版本。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 0)
    val protocolVersion: Int,
    /** 通道总数。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 1)
    val channelCount: Int,
    /** Modbus 从站地址。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 2)
    val unitId: Int,
    /** 波特率编码。 */
    @ModbusField(codec = ModbusCodec.U16, registerOffset = 3)
    val baudRateCode: Int,
    /** 设备名称。 */
    @ModbusField(codec = ModbusCodec.STRING_UTF8, registerOffset = 4, length = 16)
    val deviceName: String,
)
