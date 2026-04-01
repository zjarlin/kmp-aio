package site.addzero.kcloud.plugins.mcuconsole.service.modbus

import site.addzero.device.protocol.modbus.annotation.ModbusField
import site.addzero.device.protocol.modbus.model.ModbusCodec

/**
 * 24 路电源灯状态快照。
 */
data class Device24PowerLights(
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 0)
    val light1: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 1)
    val light2: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 2)
    val light3: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 3)
    val light4: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 4)
    val light5: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 5)
    val light6: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 6)
    val light7: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 7)
    val light8: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 8)
    val light9: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 9)
    val light10: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 10)
    val light11: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 11)
    val light12: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 12)
    val light13: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 13)
    val light14: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 14)
    val light15: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 15)
    val light16: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 16)
    val light17: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 17)
    val light18: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 18)
    val light19: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 19)
    val light20: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 20)
    val light21: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 21)
    val light22: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 22)
    val light23: Boolean,
    @ModbusField(codec = ModbusCodec.BOOL_COIL, registerOffset = 23)
    val light24: Boolean,
) {
    fun asList(): List<Boolean> {
        return listOf(
            light1,
            light2,
            light3,
            light4,
            light5,
            light6,
            light7,
            light8,
            light9,
            light10,
            light11,
            light12,
            light13,
            light14,
            light15,
            light16,
            light17,
            light18,
            light19,
            light20,
            light21,
            light22,
            light23,
            light24,
        )
    }
}
