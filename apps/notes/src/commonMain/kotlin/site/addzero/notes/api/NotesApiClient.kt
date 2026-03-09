package site.addzero.notes.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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

    private var baseUrl: String = normalizeBaseUrl(platformDefaultApiBaseUrl())
    private var api: NotesApi = createApi(baseUrl)

    fun currentBaseUrl(): String {
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
        return Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .build()
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
