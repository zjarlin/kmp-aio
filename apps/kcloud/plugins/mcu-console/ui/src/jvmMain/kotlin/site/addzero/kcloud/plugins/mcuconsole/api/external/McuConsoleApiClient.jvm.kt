package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.core.network.HttpClientFactory

actual object McuConsoleApiClient {
    private const val httpClientProfile = "kcloud-mcu-console"
    private const val defaultBaseUrl = "http://localhost:18080"
    private val httpClientFactory: HttpClientFactory
        get() = KoinPlatform.getKoin().get()

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    actual fun configureBaseUrl(value: String) {
        baseUrl = value.normalizeBaseUrl()
    }

    private fun createKtorfit(): Ktorfit {
        return Ktorfit.Builder()
            .baseUrl(baseUrl.normalizeBaseUrl())
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
    }

    actual val sessionApi: McuSessionApi
        get() = createKtorfit().createMcuSessionApi()

    actual val settingsApi: McuSettingsApi
        get() = createKtorfit().createMcuSettingsApi()

    actual val scriptApi: McuScriptApi
        get() = createKtorfit().createMcuScriptApi()

    actual val flashApi: McuFlashApi
        get() = createKtorfit().createMcuFlashApi()

    actual val runtimeApi: McuRuntimeApi
        get() = createKtorfit().createMcuRuntimeApi()

    actual val modbusApi: McuModbusApi
        get() = createKtorfit().createMcuModbusApi()

    actual val modbusDeviceApi: McuModbusDeviceApi
        get() = createKtorfit().createMcuModbusDeviceApi()

    private fun String.normalizeBaseUrl(): String {
        val normalized = trim()
            .ifBlank { defaultBaseUrl }
            .trimEnd('/')
        return "$normalized/"
    }
}
