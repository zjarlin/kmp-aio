package site.addzero.kcloud.plugins.mcuconsole.driver.serial

import com.fazecast.jSerialComm.SerialPort
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary

class JSerialCommSerialPortGateway : SerialPortGateway {
    override fun listPorts(): List<McuPortSummary> {
        return SerialPort.getCommPorts().map { port ->
            val serialNumber = port.serialNumber.normalizedPortMeta()
            val manufacturer = port.manufacturer.normalizedPortMeta()
            val portLocation = port.portLocation.normalizedPortMeta().orEmpty()
            McuPortSummary(
                portPath = port.systemPortPath,
                portName = port.portDescription?.takeIf { it.isNotBlank() } ?: port.systemPortName,
                systemPortName = port.systemPortName.orEmpty(),
                descriptiveName = port.descriptivePortName.orEmpty(),
                description = port.portDescription.orEmpty(),
                kind = portLocation,
                portLocation = portLocation,
                serialNumber = serialNumber.orEmpty(),
                manufacturer = manufacturer.orEmpty(),
                vendorId = port.vendorID.takeIf { it >= 0 },
                productId = port.productID.takeIf { it >= 0 },
                deviceKey = buildDeviceKey(
                    serialNumber = serialNumber,
                    vendorId = port.vendorID.takeIf { it >= 0 },
                    productId = port.productID.takeIf { it >= 0 },
                    manufacturer = manufacturer,
                    description = port.portDescription.normalizedPortMeta()
                        ?: port.descriptivePortName.normalizedPortMeta()
                        ?: port.systemPortName.normalizedPortMeta(),
                    portLocation = portLocation,
                ),
            )
        }.sortedBy { it.portPath }
    }

    override fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection {
        val port = SerialPort.getCommPort(portPath)
        port.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY)
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 200, 0)
        check(port.openPort()) { "打开串口失败: $portPath" }
        return JSerialCommSerialPortConnection(port, portPath, baudRate)
    }
}

private fun buildDeviceKey(
    serialNumber: String?,
    vendorId: Int?,
    productId: Int?,
    manufacturer: String?,
    description: String?,
    portLocation: String?,
): String {
    val normalizedSerial = serialNumber.normalizedPortMeta()
    if (normalizedSerial != null) {
        return "sn:$normalizedSerial"
    }
    if (vendorId != null && productId != null) {
        return buildString {
            append("usb:")
            append(vendorId.toString(16))
            append(':')
            append(productId.toString(16))
            manufacturer.normalizedPortMeta()?.let { value ->
                append(":m=")
                append(value)
            }
            description.normalizedPortMeta()?.let { value ->
                append(":d=")
                append(value)
            }
        }
    }
    description.normalizedPortMeta()?.let { value ->
        return "desc:$value"
    }
    portLocation.normalizedPortMeta()?.let { value ->
        return "loc:$value"
    }
    return ""
}

private fun String?.normalizedPortMeta(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) {
        return null
    }
    if (value.equals("unknown", ignoreCase = true)) {
        return null
    }
    return value
}

private class JSerialCommSerialPortConnection(
    private val port: SerialPort,
    override val portPath: String,
    override val baudRate: Int,
) : SerialPortConnection {
    override val portName: String = port.portDescription?.takeIf { it.isNotBlank() } ?: port.systemPortName.orEmpty()

    override val isOpen: Boolean
        get() = port.isOpen

    override fun writeUtf8(
        text: String,
    ) {
        writeBytes(text.toByteArray(Charsets.UTF_8))
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
        val written = port.writeBytes(bytes, length)
        check(written == length) { "串口写入失败: $portPath" }
    }

    override fun read(
        buffer: ByteArray,
        timeoutMs: Int,
    ): Int {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeoutMs, 0)
        val count = port.readBytes(buffer, buffer.size)
        return if (count < 0) 0 else count
    }

    override fun setDtr(
        enabled: Boolean,
    ) {
        if (enabled) {
            port.setDTR()
        } else {
            port.clearDTR()
        }
    }

    override fun setRts(
        enabled: Boolean,
    ) {
        if (enabled) {
            port.setRTS()
        } else {
            port.clearRTS()
        }
    }

    override fun close() {
        if (port.isOpen) {
            port.closePort()
        }
    }
}
