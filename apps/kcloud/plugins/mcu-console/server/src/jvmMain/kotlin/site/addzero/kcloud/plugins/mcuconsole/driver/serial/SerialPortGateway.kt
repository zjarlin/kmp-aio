package site.addzero.kcloud.plugins.mcuconsole.driver.serial

import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary

/**
 * 串口网关
 * @author zjarlin
 * @date 2026/03/30
 * @constructor 创建[SerialPortGateway]
 */
interface SerialPortGateway {
    fun listPorts(): List<McuPortSummary>

    fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection
}

