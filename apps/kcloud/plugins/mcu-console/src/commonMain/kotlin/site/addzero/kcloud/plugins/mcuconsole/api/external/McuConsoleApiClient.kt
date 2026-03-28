package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object McuConsoleApiClient {
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

    private val ktorfit = Ktorfit.Builder()
        .baseUrl("http://localhost:8080/")
        .httpClient(httpClient)
        .build()

    val api: McuConsoleApi = ktorfit.createMcuConsoleApi()
}
