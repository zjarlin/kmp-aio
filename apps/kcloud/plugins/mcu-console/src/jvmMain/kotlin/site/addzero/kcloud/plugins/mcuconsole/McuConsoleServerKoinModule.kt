package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.JSerialCommSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.JvmMcuFlashCommandRunner
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashCommandRunner
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashProfileCatalog
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService
import site.addzero.kcloud.plugins.mcuconsole.service.McuScriptService

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
    fun provideSessionService(
        gateway: SerialPortGateway,
        protocolCodec: McuVmProtocolCodec,
    ): McuConsoleSessionService {
        return McuConsoleSessionService(
            gateway = gateway,
            protocolCodec = protocolCodec,
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
}
