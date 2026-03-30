package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import site.addzero.core.network.HttpClientFactory

actual object McuConsoleApiClient {
    private const val httpClientProfile = "kcloud-mcu-console"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: HttpClientFactory
        get() = HttpClientFactory.shared()

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    actual fun configureBaseUrl(value: String) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    private fun createKtorfit(): Ktorfit {
        return Ktorfit.Builder()
            .baseUrl(baseUrl.ifBlank { defaultBaseUrl })
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
    }

    actual val sessionApi: McuSessionApi
        get() = createKtorfit().createMcuSessionApi()

    actual val scriptApi: McuScriptApi
        get() = createKtorfit().createMcuScriptApi()

    actual val flashApi: McuFlashApi
        get() = createKtorfit().createMcuFlashApi()

    actual val runtimeApi: McuRuntimeApi
        get() = createKtorfit().createMcuRuntimeApi()

    actual val modbusApi: McuModbusApi
        get() = createKtorfit().createMcuModbusApi()
}
