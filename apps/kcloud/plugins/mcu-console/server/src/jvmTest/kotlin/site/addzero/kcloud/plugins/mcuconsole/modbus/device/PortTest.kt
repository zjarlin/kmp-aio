package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import site.addzero.serial.SerialPortTool
import kotlin.test.Test

/**
 * 验证端口相关场景。
 */
class PortTest {
    @Test
    /**
     * 处理test。
     */
    fun test(): Unit {
        val listPorts = SerialPortTool.listPorts()
        println()

    }
}