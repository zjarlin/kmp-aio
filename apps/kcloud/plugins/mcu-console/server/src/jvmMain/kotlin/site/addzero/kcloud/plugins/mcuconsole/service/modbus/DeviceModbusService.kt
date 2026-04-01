package site.addzero.kcloud.plugins.mcuconsole.service.modbus

import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.ModbusRtuEndpointConfig
import site.addzero.esp32_host_computer.generated.modbus.rtu.DeviceApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusDeviceInfoResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.model.McuModbusPowerLightsResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import java.time.Instant

@Single
class DeviceModbusService(
    private val sessionService: McuConsoleSessionService,
    private val gateway: DeviceApiGeneratedRtuGateway,
) {
    suspend fun get24PowerLights(): McuModbusPowerLightsResponse {
        val config = activeConfig()
        val lights = gateway.get24PowerLights(config = config).asList()
        return McuModbusPowerLightsResponse(
            success = true,
            portPath = config.portPath,
            lights = lights,
            onCount = lights.count { it },
            updatedAt = Instant.now().toString(),
        )
    }

    suspend fun getDeviceInfo(): McuModbusDeviceInfoResponse {
        val config = activeConfig()
        val info = gateway.getDeviceInfo(config = config)
        return McuModbusDeviceInfoResponse(
            success = true,
            portPath = config.portPath,
            firmwareVersion = info.firmwareVersion,
            cpuModel = info.cpuModel,
            xtalFrequencyHz = info.xtalFrequencyHz,
            flashSizeBytes = info.flashSizeBytes.toLong(),
            macAddress = info.macAddress,
            updatedAt = Instant.now().toString(),
        )
    }

    private fun activeConfig(): ModbusRtuEndpointConfig {
        val session = sessionService.getSessionSnapshot()
        val portPath = session.portPath?.takeIf { value -> value.isNotBlank() }
            ?: error("请先打开串口会话，再读取 Modbus 设备信息")
        return gateway.defaultConfig().copy(
            portPath = portPath,
            baudRate = session.baudRate,
        )
    }
}
