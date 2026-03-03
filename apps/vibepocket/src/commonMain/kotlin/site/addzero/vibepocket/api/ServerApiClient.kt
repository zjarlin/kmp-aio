package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * 统一的后端 API 客户端
 */
object ServerApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val ktorfit = Ktorfit.Builder()
        .baseUrl("http://localhost:8080/") // 后端地址
        .httpClient(httpClient)
        .build()

    val configApi: ConfigApi = ktorfit.createConfigApi()
    val favoriteApi: FavoriteApi = ktorfit.createFavoriteApi()
    val personaApi: PersonaApi = ktorfit.createPersonaApi()
    val historyApi: HistoryApi = ktorfit.createHistoryApi()

    // ── 兼容旧代码的便捷方法 ──────────────────────────────────────────────

    suspend fun getConfig(key: String): String? {
        return try {
            configApi.getConfig(key).value
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getHistory() = historyApi.getHistory()

    suspend fun saveHistory(request: site.addzero.vibepocket.model.MusicHistorySaveRequest) = 
        historyApi.saveHistory(request)

    suspend fun getFavorites() = favoriteApi.getFavorites()

    suspend fun addFavorite(request: site.addzero.vibepocket.model.FavoriteRequest) = 
        favoriteApi.addFavorite(request)

    suspend fun removeFavorite(trackId: String) = 
        favoriteApi.removeFavorite(trackId)

    suspend fun savePersona(request: site.addzero.vibepocket.model.PersonaSaveRequest) = 
        personaApi.savePersona(request)

    suspend fun getPersonas() = personaApi.getPersonas()
}
