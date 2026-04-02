package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.device.protocol.modbus.annotation.ModbusField
import site.addzero.device.protocol.modbus.model.ModbusCodec

/**
 * 板卡运行信息。
 */
data class DeviceRuntimeInfo(
    /** 板子固件版本号。 */
    @ModbusField(codec = ModbusCodec.STRING_ASCII, registerOffset = 0, length = 8)
    val firmwareVersion: String,
    /** CPU 型号。 */
    @ModbusField(codec = ModbusCodec.STRING_ASCII, registerOffset = 8, length = 8)
    val cpuModel: String,
    /** 晶振频率，单位 Hz。 */
    @ModbusField(codec = ModbusCodec.U32_BE, registerOffset = 16)
    val xtalFrequencyHz: Int,
    /** Flash 容量，单位字节。 */
    @ModbusField(codec = ModbusCodec.U32_BE, registerOffset = 18)
    val flashSizeBytes: Int,
    /** MAC 地址。 */
    @ModbusField(codec = ModbusCodec.STRING_ASCII, registerOffset = 20, length = 9)
    val macAddress: String,
)
