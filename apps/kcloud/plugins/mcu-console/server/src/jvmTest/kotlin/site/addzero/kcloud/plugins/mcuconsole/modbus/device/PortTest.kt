package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.serial.SerialPortTool
import kotlin.test.Test

class PortTest {
    @Test
    fun test(): Unit {
        val listPorts = SerialPortTool.listPorts()
        println()

    }
}