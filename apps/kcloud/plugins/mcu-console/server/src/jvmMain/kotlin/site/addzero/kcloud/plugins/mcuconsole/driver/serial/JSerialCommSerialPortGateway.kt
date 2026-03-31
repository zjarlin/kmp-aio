package site.addzero.kcloud.plugins.mcuconsole.driver.serial

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.serial.SerialConnection
import site.addzero.serial.SerialPortConfig
import site.addzero.serial.SerialPortDescriptor
import site.addzero.serial.SerialPortTool

@Single(
    binds = [
        SerialPortGateway::class,
    ],
)
class JSerialCommSerialPortGateway : SerialPortGateway {
    override fun listPorts(): List<McuPortSummary> {
        return SerialPortTool.listPorts()
            .map(SerialPortDescriptor::toPortSummary)
            .sortedBy { it.portPath }
    }

    override fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection {
        return ToolSerialPortConnection(
            delegate = SerialPortTool.open(
                SerialPortConfig(
                    portName = portPath,
                    baudRate = baudRate,
                    readTimeoutMs = 250,
                    writeTimeoutMs = 1_000,
                ),
            ),
        )
    }
}

private fun SerialPortDescriptor.toPortSummary(): McuPortSummary {
    val serialNumber = serialNumber.normalizedPortMeta()
    val manufacturer = manufacturer.normalizedPortMeta()
    val portLocation = portLocation.normalizedPortMeta().orEmpty()
    return McuPortSummary(
        portPath = systemPortPath,
        portName = portDescription.takeIf { it.isNotBlank() } ?: systemPortName,
        systemPortName = systemPortName,
        descriptiveName = descriptivePortName,
        description = portDescription,
        kind = portLocation,
        portLocation = portLocation,
        serialNumber = serialNumber.orEmpty(),
        manufacturer = manufacturer.orEmpty(),
        vendorId = vendorId,
        productId = productId,
        deviceKey = buildDeviceKey(
            serialNumber = serialNumber,
            vendorId = vendorId,
            productId = productId,
            manufacturer = manufacturer,
            description = portDescription.normalizedPortMeta()
                ?: descriptivePortName.normalizedPortMeta()
                ?: systemPortName.normalizedPortMeta(),
            portLocation = portLocation,
        ),
    )
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

private class ToolSerialPortConnection(
    private val delegate: SerialConnection,
) : SerialPortConnection {
    override val portPath: String = delegate.config.portName

    override val portName: String = delegate.systemPortName

    override val baudRate: Int = delegate.config.baudRate

    override val isOpen: Boolean
        get() = delegate.isOpen

    override fun writeUtf8(
        text: String,
    ) {
        delegate.write(text)
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
        delegate.write(bytes.copyOf(length))
    }

    override fun read(
        buffer: ByteArray,
        timeoutMs: Int,
    ): Int {
        val read = delegate.read(maxBytes = buffer.size)
        val count = read.size
        read.copyInto(buffer, endIndex = count)
        return count
    }

    override fun setDtr(
        enabled: Boolean,
    ) {
        delegate.setDtr(enabled)
    }

    override fun setRts(
        enabled: Boolean,
    ) {
        delegate.setRts(enabled)
    }

    override fun close() {
        delegate.close()
    }
}
