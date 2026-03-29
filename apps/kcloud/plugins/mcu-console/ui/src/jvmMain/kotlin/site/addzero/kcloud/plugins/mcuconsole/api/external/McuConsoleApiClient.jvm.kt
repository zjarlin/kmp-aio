package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import site.addzero.core.network.AddZeroHttpClientFactory

actual object McuConsoleApiClient {
    private const val httpClientProfile = "kcloud-mcu-console"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: AddZeroHttpClientFactory
        get() = AddZeroHttpClientFactory.shared()

    @Volatile
    private var cachedBaseUrl: String = defaultBaseUrl

    @Volatile
    private var cachedApis: CachedApis? = null

    actual fun configureBaseUrl(value: String) {
        cachedBaseUrl = value.ifBlank { defaultBaseUrl }
        cachedApis = null
    }

    actual val sessionApi: McuSessionApi
        get() = apis().sessionApi

    actual val scriptApi: McuScriptApi
        get() = apis().scriptApi

    actual val flashApi: McuFlashApi
        get() = apis().flashApi

    actual val runtimeApi: McuRuntimeApi
        get() = apis().runtimeApi

    private fun apis(): CachedApis {
        val currentBaseUrl = cachedBaseUrl
        cachedApis?.takeIf { it.baseUrl == currentBaseUrl }?.let { return it }
        return synchronized(this) {
            cachedApis?.takeIf { it.baseUrl == currentBaseUrl }
                ?: createApis(currentBaseUrl).also { createdApis ->
                    cachedBaseUrl = currentBaseUrl
                    cachedApis = createdApis
                }
        }
    }

    private fun createApis(baseUrl: String): CachedApis {
        val ktorfit = Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClientFactory.get(httpClientProfile))
            .build()
        return CachedApis(
            baseUrl = baseUrl,
            sessionApi = ktorfit.createMcuSessionApi(),
            scriptApi = ktorfit.createMcuScriptApi(),
            flashApi = ktorfit.createMcuFlashApi(),
            runtimeApi = ktorfit.createMcuRuntimeApi(),
        )
    }

    private data class CachedApis(
        val baseUrl: String,
        val sessionApi: McuSessionApi,
        val scriptApi: McuScriptApi,
        val flashApi: McuFlashApi,
        val runtimeApi: McuRuntimeApi,
    )
}
