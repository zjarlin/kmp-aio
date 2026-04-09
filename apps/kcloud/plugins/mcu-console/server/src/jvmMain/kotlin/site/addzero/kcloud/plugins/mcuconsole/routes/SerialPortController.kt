package site.addzero.kcloud.plugins.mcuconsole.routes

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialFlowControl
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialParity
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortConfig
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialPortDescriptor
import site.addzero.kcloud.plugins.mcuconsole.serial.McuSerialStopBits
import site.addzero.serial.SerialFlowControl
import site.addzero.serial.SerialParity
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortDescriptor
import site.addzero.serial.SerialStopBits
import site.addzero.serial.SerialPortTool

@RestController("/serialPorts")
@RequestMapping("/mcu-console/router/ports")
class SerialPortController {
    @GetMapping("/list")
    fun getSerialPorts(): List<McuSerialPortDescriptor> =
        SerialPortTool.listPorts().map(SerialPortDescriptor::toMcuDescriptor)

    @PostMapping("/open")
    fun open(@RequestBody serialPortConfig: McuSerialPortConfig): List<McuSerialPortDescriptor> {
        SerialPortTool.open(serialPortConfig.toSerialPortConfig()).use { connection ->
            connection.clearBuffers()
        }
        return getSerialPorts()
    }
}

private fun McuSerialPortConfig.toSerialPortConfig(): SerialPortConfig =
    SerialPortConfig(
        portName = portName,
        baudRate = baudRate,
        dataBits = dataBits,
        stopBits = stopBits.toSerialStopBits(),
        parity = parity.toSerialParity(),
        flowControl = flowControl.toSerialFlowControl(),
        readTimeoutMs = readTimeoutMs,
        writeTimeoutMs = writeTimeoutMs,
        openSafetySleepTimeMs = openSafetySleepTimeMs,
    )

private fun SerialPortDescriptor.toMcuDescriptor(): McuSerialPortDescriptor =
    McuSerialPortDescriptor(
        systemPortName = systemPortName,
        systemPortPath = systemPortPath,
        descriptivePortName = descriptivePortName,
        portDescription = portDescription,
        portLocation = portLocation,
        manufacturer = manufacturer,
        serialNumber = serialNumber,
        vendorId = vendorId,
        productId = productId,
    )

private fun McuSerialStopBits.toSerialStopBits(): SerialStopBits =
    when (this) {
        McuSerialStopBits.ONE -> SerialStopBits.ONE
        McuSerialStopBits.ONE_POINT_FIVE -> SerialStopBits.ONE_POINT_FIVE
        McuSerialStopBits.TWO -> SerialStopBits.TWO
    }

private fun McuSerialParity.toSerialParity(): SerialParity =
    when (this) {
        McuSerialParity.NONE -> SerialParity.NONE
        McuSerialParity.EVEN -> SerialParity.EVEN
        McuSerialParity.ODD -> SerialParity.ODD
        McuSerialParity.MARK -> SerialParity.MARK
        McuSerialParity.SPACE -> SerialParity.SPACE
    }

private fun McuSerialFlowControl.toSerialFlowControl(): SerialFlowControl =
    when (this) {
        McuSerialFlowControl.NONE -> SerialFlowControl.NONE
        McuSerialFlowControl.RTS_CTS -> SerialFlowControl.RTS_CTS
        McuSerialFlowControl.DTR_DSR -> SerialFlowControl.DTR_DSR
        McuSerialFlowControl.XON_XOFF -> SerialFlowControl.XON_XOFF
    }

