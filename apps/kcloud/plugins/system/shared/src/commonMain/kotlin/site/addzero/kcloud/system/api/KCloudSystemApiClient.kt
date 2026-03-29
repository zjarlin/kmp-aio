package site.addzero.kcloud.system.api

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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

    val userCenterApi: UserCenterApi
        get() = buildUserCenterApi(resolveBaseUrl(), httpClient)

    val aiChatApi: AiChatApi
        get() = buildAiChatApi(resolveBaseUrl(), httpClient)

    val knowledgeBaseApi: KnowledgeBaseApi
        get() = buildKnowledgeBaseApi(resolveBaseUrl(), httpClient)

    private fun resolveBaseUrl(): String {
        return baseUrl.ifBlank { defaultBaseUrl }
    }
}
