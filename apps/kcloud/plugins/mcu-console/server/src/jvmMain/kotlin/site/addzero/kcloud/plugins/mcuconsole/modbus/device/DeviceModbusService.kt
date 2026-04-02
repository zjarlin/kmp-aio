package site.addzero.kcloud.plugins.mcuconsole.modbus.device

import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.device.driver.modbus.rtu.ModbusSerialParity
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceWriteApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusCommandConfig
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import java.time.Instant

@Single
class DeviceModbusService(
    private val sessionService: McuConsoleSessionService,
    private val readGateway: DeviceApiGeneratedRtuGateway,
    private val writeGateway: DeviceWriteApiGeneratedRtuGateway,
) {
    suspend fun get24PowerLights(
        config: McuModbusCommandConfig? = null,
    ): McuModbusPowerLightsResponse {
        val endpointConfig = activeConfig(config)
        val lights = readGateway.get24PowerLights(config = endpointConfig).asList()
        return McuModbusPowerLightsResponse(
            success = true,
            portPath = endpointConfig.portPath,
            lights = lights,
            onCount = lights.count { it },
            updatedAt = Instant.now().toString(),
        )
    }

    suspend fun getDeviceInfo(
        config: McuModbusCommandConfig? = null,
    ): McuModbusDeviceInfoResponse {
        val endpointConfig = activeConfig(config)
        val info = readGateway.getDeviceInfo(config = endpointConfig)
        return McuModbusDeviceInfoResponse(
            success = true,
            portPath = endpointConfig.portPath,
            firmwareVersion = info.firmwareVersion,
            cpuModel = info.cpuModel,
            xtalFrequencyHz = info.xtalFrequencyHz,
            flashSizeBytes = info.flashSizeBytes,
            macAddress = info.macAddress,
            updatedAt = Instant.now().toString(),
        )
    }

    suspend fun writeIndicatorLights(
        faultLightOn: Boolean,
        runLightOn: Boolean,
        config: McuModbusCommandConfig? = null,
    ): McuModbusIndicatorLightsResponse {
        val endpointConfig = activeConfig(config)
        val result = writeGateway.writeIndicatorLights(
            config = endpointConfig,
            faultLightOn = faultLightOn,
            runLightOn = runLightOn,
        )
        return McuModbusIndicatorLightsResponse(
            success = result.accepted,
            portPath = endpointConfig.portPath,
            faultLightOn = faultLightOn,
            runLightOn = runLightOn,
            lastMessage = result.summary,
            updatedAt = Instant.now().toString(),
        )
    }

    private fun activeConfig(
        commandConfig: McuModbusCommandConfig?,
    ): ModbusRtuEndpointConfig {
        if (commandConfig != null) {
            val portPath = commandConfig.portPath?.takeIf { value -> value.isNotBlank() }
                ?: error("Modbus portPath is required")
            return readGateway.defaultConfig().copy(
                portPath = portPath,
                unitId = commandConfig.unitId,
                baudRate = commandConfig.baudRate,
                dataBits = commandConfig.dataBits,
                stopBits = commandConfig.stopBits,
                parity = commandConfig.parity.toRuntimeParity(),
                timeoutMs = commandConfig.timeoutMs,
                retries = commandConfig.retries,
            )
        }
        val session = sessionService.getSessionSnapshot()
        val portPath = session.portPath?.takeIf { value -> value.isNotBlank() }
            ?: error("请先打开串口会话，再执行 Modbus 设备操作")
        return readGateway.defaultConfig().copy(
            portPath = portPath,
            baudRate = session.baudRate,
        )
    }
}

private fun McuModbusSerialParity.toRuntimeParity(): ModbusSerialParity {
    return when (this) {
        McuModbusSerialParity.NONE -> ModbusSerialParity.NONE
        McuModbusSerialParity.EVEN -> ModbusSerialParity.EVEN
        McuModbusSerialParity.ODD -> ModbusSerialParity.ODD
    }
}
