package site.addzero.kcloud.plugins.mcuconsole.modbus.atomic

import kotlinx.coroutines.runBlocking
import site.addzero.core.network.json.json as sharedJson
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigProvider
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.device.protocol.modbus.model.ModbusCommandResult
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.FakeSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutomicModbusServiceTest {
    private val json = sharedJson

    @Test
    fun `gpio write uses active session port and register payload`() = runBlocking {
        val executor = RecordingModbusRtuExecutor()
        val service = newService(executor)

        service.gpioWrite(pin = 4, high = true)

        val invocation = executor.lastWriteMultiple ?: error("expected write invocation")
        assertEquals(1024, invocation.address)
        assertEquals(listOf(4, 1), invocation.values)
        assertEquals("COM9", invocation.config.portPath)
        assertEquals(57600, invocation.config.baudRate)
        assertEquals(1, invocation.config.unitId)
    }

    @Test
    fun `gpio mode and pwm keep semantic validation`() = runBlocking {
        val executor = RecordingModbusRtuExecutor()
        val service = newService(executor)

        service.gpioMode(pin = 12, mode = AutomicGpioMode.OUTPUT)
        service.pwmDuty(pin = 12, dutyU16 = 4096)

        assertEquals(1026, executor.writeAddresses[0])
        assertEquals(listOf(12, AutomicGpioMode.OUTPUT.code), executor.writePayloads[0])
        assertEquals(1028, executor.writeAddresses[1])
        assertEquals(listOf(12, 4096), executor.writePayloads[1])
    }

    @Test
    fun `servo angle rejects out of range input`() {
        val executor = RecordingModbusRtuExecutor()
        val service = newService(executor)

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                service.servoAngle(pin = 5, angle = 181)
            }
        }
    }

    private fun newService(
        executor: RecordingModbusRtuExecutor,
    ): AutomicModbusService {
        val sessionService = McuConsoleSessionService(
            gateway = FakeSerialPortGateway(),
            protocolCodec = McuVmProtocolCodec(json),
        )
        sessionService.openSession(
            McuSessionOpenRequest(
                portPath = "COM9",
                baudRate = 57600,
            ),
        )
        val registry = ModbusRtuConfigRegistry(
            listOf(
                object : ModbusRtuConfigProvider {
                    override val serviceId = "automic-modbus"

                    override fun defaultConfig(): ModbusRtuEndpointConfig {
                        return ModbusRtuEndpointConfig(
                            serviceId = serviceId,
                            portPath = "/dev/ttyUSB0",
                            unitId = 1,
                            baudRate = 115200,
                            timeoutMs = 1_000,
                            retries = 2,
                        )
                    }
                },
            ),
        )
        return AutomicModbusService(
            sessionService = sessionService,
            gateway = AutomicModbusApiGeneratedRtuGateway(
                configRegistry = registry,
                executor = executor,
            ),
        )
    }
}

private class RecordingModbusRtuExecutor : ModbusRtuExecutor {
    var lastWriteMultiple: WriteInvocation? = null
    val writeAddresses = mutableListOf<Int>()
    val writePayloads = mutableListOf<List<Int>>()

    override suspend fun readCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun readDiscreteInputs(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun readHoldingRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun readInputRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        quantity: Int,
    ): List<Int> = error("not used")

    override suspend fun writeSingleCoil(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Boolean,
    ) = error("not used")

    override suspend fun writeMultipleCoils(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Boolean>,
    ) = error("not used")

    override suspend fun writeSingleRegister(
        config: ModbusRtuEndpointConfig,
        address: Int,
        value: Int,
    ) = error("not used")

    override suspend fun writeMultipleRegisters(
        config: ModbusRtuEndpointConfig,
        address: Int,
        values: List<Int>,
    ) {
        writeAddresses += address
        writePayloads += values
        lastWriteMultiple = WriteInvocation(
            config = config,
            address = address,
            values = values,
        )
    }
}

private data class WriteInvocation(
    val config: ModbusRtuEndpointConfig,
    val address: Int,
    val values: List<Int>,
    val result: ModbusCommandResult = ModbusCommandResult(
        accepted = true,
        summary = "ok",
    ),
)
