package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual object McuConsoleApiClient {
    private const val defaultBaseUrl = "http://localhost:18080/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val httpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
    }

    @Volatile
    private var cachedBaseUrl: String = defaultBaseUrl

    @Volatile
    private var cachedApi: McuConsoleApi? = null

    actual fun configureBaseUrl(value: String) {
        cachedBaseUrl = value.ifBlank { defaultBaseUrl }
        cachedApi = null
    }

    actual val api: McuConsoleApi
        get() {
            val currentBaseUrl = cachedBaseUrl
            cachedApi?.takeIf { cachedBaseUrl == currentBaseUrl }?.let { return it }
            return synchronized(this) {
                cachedApi?.takeIf { cachedBaseUrl == currentBaseUrl }
                    ?: Ktorfit.Builder()
                        .baseUrl(currentBaseUrl)
                        .httpClient(httpClient)
                        .build()
                        .createMcuConsoleApi()
                        .also { createdApi ->
                            cachedBaseUrl = currentBaseUrl
                            cachedApi = createdApi
                        }
            }
        }
}
