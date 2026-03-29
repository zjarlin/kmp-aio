package site.addzero.kcloud.plugins.mcuconsole

import kotlin.math.min
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortConnection
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway

internal class FakeSerialPortGateway(
    private val ports: List<McuPortSummary> = listOf(
        McuPortSummary(
            portPath = "COM9",
            portName = "Mock Port",
            systemPortName = "COM9",
            descriptiveName = "Mock MCU",
        ),
    ),
    runtimeInstalledInitially: Boolean = true,
) : SerialPortGateway {
    val openedConnections = mutableListOf<FakeSerialPortConnection>()

    @Volatile
    var runtimeInstalled: Boolean = runtimeInstalledInitially

    override fun listPorts(): List<McuPortSummary> = ports

    override fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection {
        return FakeSerialPortConnection(
            gateway = this,
            portPath = portPath,
            baudRate = baudRate,
        ).also { connection ->
            openedConnections += connection
        }
    }
}

internal class FakeSerialPortConnection(
    private val gateway: FakeSerialPortGateway,
    override val portPath: String,
    override val baudRate: Int,
) : SerialPortConnection {
    override val portName: String = "Fake-$portPath"

    private val pendingReads = ArrayDeque<ByteArray>()
    private var flashMode = false

    val textWrites = mutableListOf<String>()
    val byteWrites = mutableListOf<Byte>()

    override var isOpen: Boolean = true
        private set

    var dtrEnabled: Boolean = false
        private set

    var rtsEnabled: Boolean = false
        private set

    override fun writeUtf8(
        text: String,
    ) {
        textWrites += text
        val requestId = REQUEST_ID_REGEX.find(text)?.groupValues?.getOrNull(1)
        when {
            gateway.runtimeInstalled && text.contains("\"command\":\"vm.execute\"") -> {
                enqueueIncomingText(
                    """{"requestId":"${requestId.orEmpty()}","type":"ack","success":true,"message":"ready"}""" + "\n",
                )
            }

            gateway.runtimeInstalled && text.contains("\"command\":\"vm.stop\"") -> {
                enqueueIncomingText(
                    """{"requestId":"${requestId.orEmpty()}","type":"status","success":true,"message":"stopping","payload":{"state":"STOPPING"}}""" + "\n",
                )
            }

            gateway.runtimeInstalled && text.contains("\"command\":\"vm.ping\"") -> {
                enqueueIncomingText(
                    """{"requestId":"${requestId.orEmpty()}","type":"ack","success":true,"message":"runtime-ready","payload":{"runtime":"rhai"}}""" + "\n",
                )
            }

            gateway.runtimeInstalled && text.contains("\"command\":\"vm.status\"") -> {
                enqueueIncomingText(
                    """{"requestId":"${requestId.orEmpty()}","type":"status","success":true,"message":"runtime-ready","payload":{"state":"IDLE","runtime":"rhai"}}""" + "\n",
                )
            }

            text == "START_FLASH\r\n" -> {
                flashMode = true
            }

            text == "DONE\r\n" -> {
                gateway.runtimeInstalled = true
                flashMode = false
                enqueueIncomingText("SUCCESS")
            }
        }
    }

    override fun writeBytes(
        bytes: ByteArray,
        length: Int,
    ) {
        repeat(length) { index ->
            byteWrites += bytes[index]
            if (flashMode) {
                enqueueIncomingText("ACK")
            }
        }
    }

    override fun read(
        buffer: ByteArray,
        timeoutMs: Int,
    ): Int {
        val next = pendingReads.removeFirstOrNull() ?: return 0
        val count = min(buffer.size, next.size)
        next.copyInto(
            destination = buffer,
            endIndex = count,
        )
        if (count < next.size) {
            pendingReads.addFirst(next.copyOfRange(count, next.size))
        }
        return count
    }

    override fun setDtr(
        enabled: Boolean,
    ) {
        dtrEnabled = enabled
    }

    override fun setRts(
        enabled: Boolean,
    ) {
        rtsEnabled = enabled
    }

    override fun close() {
        isOpen = false
    }

    fun enqueueIncomingText(
        text: String,
    ) {
        pendingReads += text.toByteArray()
    }

    private companion object {
        val REQUEST_ID_REGEX = Regex("\"requestId\":\"([^\"]+)\"")
    }
}
