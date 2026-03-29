package site.addzero.kcloud.api

import io.ktor.client.HttpClient
import site.addzero.core.network.AddZeroHttpClientFactory

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
    private const val httpClientProfile = "kcloud-vibepocket"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: AddZeroHttpClientFactory
        get() = AddZeroHttpClientFactory.shared()

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    val configApi: ConfigApi
        get() = buildConfigApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val favoriteApi: FavoriteApi
        get() = buildFavoriteApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val personaApi: PersonaApi
        get() = buildPersonaApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val historyApi: HistoryApi
        get() = buildHistoryApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val musicApi: MusicSearchApi
        get() = buildMusicSearchApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val sunoTaskResourceApi: SunoTaskResourceApi
        get() = buildSunoTaskResourceApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

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
