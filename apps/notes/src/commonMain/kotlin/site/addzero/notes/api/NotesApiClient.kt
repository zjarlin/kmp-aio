package site.addzero.notes.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import site.addzero.core.network.apiClient

expect fun platformDefaultApiBaseUrl(): String

object NotesApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val httpClient = HttpClient {
        expectSuccess = true
        defaultRequest {
            contentType(ContentType.Application.Json)
            headers.remove(HttpHeaders.Accept)
            headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
        install(ContentNegotiation) {
            json(json)
        }
    }


    private var baseUrl = normalizeBaseUrl(platformDefaultApiBaseUrl())
    private var api = createApi(baseUrl)

    fun currentBaseUrl(): String {
        apiClient.config {
            defaultRequest {
                url(baseUrl)
            }
        }

        return baseUrl

    }

    fun setBaseUrl(value: String) {
        val normalized = normalizeBaseUrl(value)
        if (normalized == baseUrl) {
            return
        }
        baseUrl = normalized
        api = createApi(baseUrl)
    }

    fun notesApi(): NotesApi {
        return api
    }

    private fun createApi(baseUrl: String): NotesApi {
        val build = Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .build()
        return build
            .createNotesApi()
    }

    private fun normalizeBaseUrl(value: String): String {
        val trimmed = value.trim().ifBlank { "http://127.0.0.1:18080/" }
        return if (trimmed.endsWith("/")) {
            trimmed
        } else {
            "$trimmed/"
        }
    }
}
