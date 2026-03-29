package site.addzero.kcloud.plugins.mcuconsole.driver.serial

import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary

interface SerialPortGateway {
    fun listPorts(): List<McuPortSummary>

    fun openConnection(
        portPath: String,
        baudRate: Int,
    ): SerialPortConnection
}

interface SerialPortConnection : AutoCloseable {
    val portPath: String
    val portName: String
    val baudRate: Int
    val isOpen: Boolean

    fun writeUtf8(
        text: String,
    )

    fun writeBytes(
        bytes: ByteArray,
        length: Int = bytes.size,
    )

    fun read(
        buffer: ByteArray,
        timeoutMs: Int = 200,
    ): Int

    fun setDtr(
        enabled: Boolean,
    )

    fun setRts(
        enabled: Boolean,
    )

    override fun close()
}
