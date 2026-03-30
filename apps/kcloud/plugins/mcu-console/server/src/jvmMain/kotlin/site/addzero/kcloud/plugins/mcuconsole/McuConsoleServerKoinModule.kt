package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.device.driver.modbus.rtu.J2modModbusRtuExecutor
import site.addzero.device.driver.modbus.rtu.ModbusRtuConfigRegistry
import site.addzero.device.driver.modbus.rtu.ModbusRtuExecutor
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuConfigProvider
import site.addzero.esp32_host_computer.generated.modbus.rtu.AutomicModbusApiGeneratedRtuGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.JSerialCommSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.*
import site.addzero.kcloud.plugins.mcuconsole.service.modbus.AutomicModbusService
import org.babyfish.jimmer.sql.kt.KSqlClient

@Module
class McuConsoleServerKoinModule {
    @Single
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Single
    fun provideProtocolCodec(
        json: Json,
    ): McuVmProtocolCodec {
        return McuVmProtocolCodec(json)
    }

    @Single
    fun provideSerialGateway(): SerialPortGateway {
        return JSerialCommSerialPortGateway()
    }

    @Single
    fun provideFlashProfileCatalog(): McuFlashProfileCatalog {
        return McuFlashProfileCatalog()
    }

    @Single
    fun provideFlashCommandRunner(): McuFlashCommandRunner {
        return JvmMcuFlashCommandRunner()
    }

    @Single
    fun provideRuntimeBundleCatalog(
        json: Json,
    ): McuRuntimeBundleCatalog {
        return McuRuntimeBundleCatalog(json)
    }

    @Single
    fun provideSettingsService(
        sqlClient: KSqlClient,
    ): McuConsoleSettingsService {
        return McuConsoleSettingsService(sqlClient)
    }

    @Single
    fun provideRuntimeAssetExtractor(
        bundleCatalog: McuRuntimeBundleCatalog,
    ): McuRuntimeAssetExtractor {
        return McuRuntimeAssetExtractor(bundleCatalog)
    }

    @Single
    fun provideSessionService(
        gateway: SerialPortGateway,
        protocolCodec: McuVmProtocolCodec,
        settingsService: McuConsoleSettingsService,
    ): McuConsoleSessionService {
        return McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = protocolCodec,
            settingsService = settingsService,
        )
    }

    @Single
    fun provideScriptService(
        sessionService: McuConsoleSessionService,
        protocolCodec: McuVmProtocolCodec,
    ): McuScriptService {
        return McuScriptService(
            sessionService = sessionService,
            protocolCodec = protocolCodec,
        )
    }

    @Single
    fun provideFlashService(
        gateway: SerialPortGateway,
        sessionService: McuConsoleSessionService,
        profileCatalog: McuFlashProfileCatalog,
        commandRunner: McuFlashCommandRunner,
    ): McuFlashService {
        return McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
            profileCatalog = profileCatalog,
            commandRunner = commandRunner,
        )
    }

    @Single
    fun provideRuntimeEnsureService(
        bundleCatalog: McuRuntimeBundleCatalog,
        assetExtractor: McuRuntimeAssetExtractor,
        flashService: McuFlashService,
        sessionService: McuConsoleSessionService,
        protocolCodec: McuVmProtocolCodec,
    ): McuRuntimeEnsureService {
        return McuRuntimeEnsureService(
            bundleCatalog = bundleCatalog,
            assetExtractor = assetExtractor,
            flashService = flashService,
            sessionService = sessionService,
            protocolCodec = protocolCodec,
        )
    }

    /**
     * 提供 MCU 设备信息轮询服务。
     */
    @Single
    fun provideDeviceInfoService(
        sessionService: McuConsoleSessionService,
        protocolCodec: McuVmProtocolCodec,
    ): McuDeviceInfoService {
        return McuDeviceInfoService(
            sessionService = sessionService,
            protocolCodec = protocolCodec,
        )
    }

    @Single
    fun provideTransportProbeService(
        settingsService: McuConsoleSettingsService,
    ): McuTransportProbeService {
        return McuTransportProbeService(settingsService)
    }

    @Single
    fun provideAutomicModbusConfigProvider(): AutomicModbusApiGeneratedRtuConfigProvider {
        return AutomicModbusApiGeneratedRtuConfigProvider()
    }

    @Single
    fun provideModbusRtuExecutor(): ModbusRtuExecutor {
        return J2modModbusRtuExecutor()
    }

    @Single
    fun provideModbusRtuConfigRegistry(
        configProvider: AutomicModbusApiGeneratedRtuConfigProvider,
    ): ModbusRtuConfigRegistry {
        return ModbusRtuConfigRegistry(listOf(configProvider))
    }

    @Single
    fun provideAutomicModbusGateway(
        configRegistry: ModbusRtuConfigRegistry,
        executor: ModbusRtuExecutor,
    ): AutomicModbusApiGeneratedRtuGateway {
        return AutomicModbusApiGeneratedRtuGateway(
            configRegistry = configRegistry,
            executor = executor,
        )
    }

    @Single
    fun provideAutomicModbusService(
        sessionService: McuConsoleSessionService,
        gateway: AutomicModbusApiGeneratedRtuGateway,
    ): AutomicModbusService {
        return AutomicModbusService(
            sessionService = sessionService,
            gateway = gateway,
        )
    }
}
