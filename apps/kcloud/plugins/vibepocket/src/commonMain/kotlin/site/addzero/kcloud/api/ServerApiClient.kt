package site.addzero.vibepocket.api

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal expect fun buildConfigApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigApi

internal expect fun buildFavoriteApi(
    baseUrl: String,
    httpClient: HttpClient,
): FavoriteApi

internal expect fun buildPersonaApi(
    baseUrl: String,
    httpClient: HttpClient,
): PersonaApi

internal expect fun buildHistoryApi(
    baseUrl: String,
    httpClient: HttpClient,
): HistoryApi

internal expect fun buildMusicSearchApi(
    baseUrl: String,
    httpClient: HttpClient,
): MusicSearchApi

internal expect fun buildSunoTaskResourceApi(
    baseUrl: String,
    httpClient: HttpClient,
): SunoTaskResourceApi

/**
 * 统一的后端 API 客户端
 */
object ServerApiClient {
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

    private const val BASE_URL = "http://localhost:8080/"

    val configApi: ConfigApi = buildConfigApi(BASE_URL, httpClient)
    val favoriteApi: FavoriteApi = buildFavoriteApi(BASE_URL, httpClient)
    val personaApi: PersonaApi = buildPersonaApi(BASE_URL, httpClient)
    val historyApi: HistoryApi = buildHistoryApi(BASE_URL, httpClient)
    val musicApi: MusicSearchApi = buildMusicSearchApi(BASE_URL, httpClient)
    val sunoTaskResourceApi: SunoTaskResourceApi = buildSunoTaskResourceApi(BASE_URL, httpClient)

    suspend fun getConfig(key: String): String? {
        return try {
            configApi.getConfig(key).value
        } catch (e: Exception) {
            null
        }
    }
}
