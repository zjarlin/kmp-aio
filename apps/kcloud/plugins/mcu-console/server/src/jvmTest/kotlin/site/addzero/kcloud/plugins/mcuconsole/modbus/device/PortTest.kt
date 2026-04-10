package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import io.github.jeadyx.jserialport.SerialPort
import io.github.jeadyx.jserialport.SerialPortException
import io.github.jeadyx.jserialport.SerialPortFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import site.addzero.serial.SerialPortTool
import kotlin.test.Test

/**
 * 验证端口相关场景。
 */
class PortTest {
    @Test
    fun listVisiblePorts() {
        println(SerialPortTool.listPorts())
    }

    @Test
    /**
     * 处理test。
     */
    fun test() = runBlocking {
        val targetPort = "/dev/cu.usbserial-2140"
        val targetPortName = targetPort.substringAfterLast("/")
        val availablePorts = SerialPortTool.listPorts()
        val targetDescriptor = availablePorts.firstOrNull { descriptor ->
            descriptor.systemPortPath == targetPort || descriptor.systemPortName == targetPortName
        }
        if (targetDescriptor == null) {
            println("Skip PortTest: port $targetPort not found.")
            return@runBlocking
        }

        val serialPort = SerialPortFactory.create()
        var opened = false
        try {
            try {
                serialPort.open(
                    portName = targetDescriptor.systemPortPath,
                    baudRate = 9600,
                    dataBits = 8,
                    stopBits = 1,
                    parity = SerialPort.PARITY_NONE,
                )
                opened = true
            } catch (exception: SerialPortException) {
                println("Skip PortTest: failed to open ${targetDescriptor.systemPortPath}: ${exception.message}")
                return@runBlocking
            }
            serialPort.write("Hello".toByteArray())
            val data = withTimeoutOrNull(3_000) {
                val read = serialPort.read()
                read.first()
            }
            println(data?.let { "Received: ${String(it)}" } ?: "No response within timeout.")
        } finally {
            if (opened) {
                serialPort.close()
            }
        }
    }
}
