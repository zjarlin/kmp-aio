package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.JSerialCommSerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.driver.serial.SerialPortGateway
import site.addzero.kcloud.plugins.mcuconsole.protocol.mcuvm.McuVmProtocolCodec
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService
import site.addzero.kcloud.plugins.mcuconsole.service.McuFlashService
import site.addzero.kcloud.plugins.mcuconsole.service.McuScriptService

@Module
@Configuration("mcuconsole-server")
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
    ): McuFlashService {
        return McuFlashService(
            gateway = gateway,
            sessionService = sessionService,
        )
    }
}
