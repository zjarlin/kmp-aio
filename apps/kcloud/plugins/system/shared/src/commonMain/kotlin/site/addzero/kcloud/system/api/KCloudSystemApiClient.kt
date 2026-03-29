package site.addzero.kcloud.system.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun buildUserCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): UserCenterApi

internal expect fun buildAiChatApi(
    baseUrl: String,
    httpClient: HttpClient,
): AiChatApi

internal expect fun buildKnowledgeBaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): KnowledgeBaseApi

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

    @Volatile
    private var baseUrl: String = "http://localhost:8080/"

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { "http://localhost:8080/" }
    }

    val userCenterApi: UserCenterApi
        get() = buildUserCenterApi(baseUrl, httpClient)

    val aiChatApi: AiChatApi
        get() = buildAiChatApi(baseUrl, httpClient)

    val knowledgeBaseApi: KnowledgeBaseApi
        get() = buildKnowledgeBaseApi(baseUrl, httpClient)
}
