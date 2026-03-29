package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 系统插件统一使用的后端 API 客户端。
 */
object KCloudSystemApiClient {
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

    val userCenterApi: UserCenterApi = ktorfit.createUserCenterApi()
    val aiChatApi: AiChatApi = ktorfit.createAiChatApi()
    val knowledgeBaseApi: KnowledgeBaseApi = ktorfit.createKnowledgeBaseApi()
}
