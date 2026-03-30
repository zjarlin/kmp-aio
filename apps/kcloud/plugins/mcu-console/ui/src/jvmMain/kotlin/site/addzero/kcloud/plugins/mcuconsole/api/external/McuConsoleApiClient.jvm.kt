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

    actual val sessionApi: McuSessionApi
        get() = Ktorfit.Builder()
            .baseUrl(baseUrl.ifBlank { defaultBaseUrl })
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
            .createMcuSessionApi()

    actual val scriptApi: McuScriptApi
        get() = Ktorfit.Builder()
            .baseUrl(baseUrl.ifBlank { defaultBaseUrl })
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
            .createMcuScriptApi()

    actual val flashApi: McuFlashApi
        get() = Ktorfit.Builder()
            .baseUrl(baseUrl.ifBlank { defaultBaseUrl })
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
            .createMcuFlashApi()

    actual val runtimeApi: McuRuntimeApi
        get() = Ktorfit.Builder()
            .baseUrl(baseUrl.ifBlank { defaultBaseUrl })
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
            .createMcuRuntimeApi()
}
