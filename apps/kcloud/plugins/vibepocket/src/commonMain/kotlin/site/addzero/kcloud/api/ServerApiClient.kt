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
    private var baseUrl: String = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    val configApi: ConfigApi
        get() = buildConfigApi(resolveBaseUrl(), httpClient)

    val favoriteApi: FavoriteApi
        get() = buildFavoriteApi(resolveBaseUrl(), httpClient)

    val personaApi: PersonaApi
        get() = buildPersonaApi(resolveBaseUrl(), httpClient)

    val historyApi: HistoryApi
        get() = buildHistoryApi(resolveBaseUrl(), httpClient)

    val musicApi: MusicSearchApi
        get() = buildMusicSearchApi(resolveBaseUrl(), httpClient)

    val sunoTaskResourceApi: SunoTaskResourceApi
        get() = buildSunoTaskResourceApi(resolveBaseUrl(), httpClient)

    suspend fun getConfig(key: String): String? {
        return try {
            configApi.getConfig(key).value
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveBaseUrl(): String {
        return baseUrl.ifBlank { defaultBaseUrl }
    }
}
