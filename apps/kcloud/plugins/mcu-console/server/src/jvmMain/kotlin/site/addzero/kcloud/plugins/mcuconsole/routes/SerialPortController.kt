package site.addzero.kcloud.plugins.mcuconsole.routes

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortDescriptor
import site.addzero.serial.SerialPortTool

@RestController("/serialPorts")
@RequestMapping("/mcu-console/router/ports")
class SerialPortController {
    @GetMapping("/list")
    fun getSerialPorts(): List<SerialPortDescriptor> {
        val listPorts = SerialPortTool.listPorts()
        return listPorts
    }

    @PostMapping("/open")
    fun open(@RequestBody serialPortConfig: SerialPortConfig): List<SerialPortDescriptor> {
        SerialPortConfig(
            portName = TODO(),
            baudRate = TODO(),
            dataBits = TODO(),
            stopBits = TODO(),
            parity = TODO(),
            flowControl = TODO(),
            readTimeoutMs = TODO(),
            writeTimeoutMs = TODO(),
            openSafetySleepTimeMs = TODO()
        )

        val open = SerialPortTool.open(
            config = TODO()
        )
        val listPorts = SerialPortTool.listPorts()
        return listPorts
    }

}


