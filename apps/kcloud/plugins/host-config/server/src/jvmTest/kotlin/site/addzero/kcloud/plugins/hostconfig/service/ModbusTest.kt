import com.ghgande.j2mod.modbus.facade.ModbusRTU
import com.ghgande.j2mod.modbus.procimg.Register

class ModbusRtuUtil(
    private val port: String,
    private val baudRate: Int,
    private val dataBits: Int = 8,
    private val stopBits: Int = 1,
    private val parity: Char = 'N'
) {
    private var modbus: ModbusRTU? = null

    // 打开连接
    fun connect(slaveId: Int): Boolean {
        return try {
            modbus = ModbusRTU(port, baudRate, dataBits, stopBits, parity)
            modbus?.connect()
            modbus?.setUnitIdentifier(slaveId)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 读 DI
    fun readDI(addr: Int, count: Int): BooleanArray {
        return modbus?.readInputDiscretes(addr, count) ?: booleanArrayOf()
    }

    // 读 AI
    fun readAI(addr: Int, count: Int): IntArray {
        return modbus?.readInputRegisters(addr, count)?.map { it.value }?.toIntArray() ?: intArrayOf()
    }

    // 写 DO
    fun writeDO(addr: Int, value: Boolean): Boolean {
        return modbus?.writeCoil(addr, value) ?: false
    }

    fun disconnect() {
        modbus?.disconnect()
    }
}