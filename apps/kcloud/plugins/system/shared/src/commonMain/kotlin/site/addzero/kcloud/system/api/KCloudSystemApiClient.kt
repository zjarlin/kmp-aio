package site.addzero.kcloud.system.api

import io.ktor.client.HttpClient
import site.addzero.core.network.AddZeroHttpClientFactory

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

internal expect fun buildRbacApi(
    baseUrl: String,
    httpClient: HttpClient,
): RbacApi

/**
 * 系统插件统一使用的后端 API 客户端。
 */
object KCloudSystemApiClient {
    private const val httpClientProfile = "kcloud-system"
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

    val userCenterApi: UserCenterApi
        get() = buildUserCenterApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val aiChatApi: AiChatApi
        get() = buildAiChatApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val knowledgeBaseApi: KnowledgeBaseApi
        get() = buildKnowledgeBaseApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    val rbacApi: RbacApi
        get() = buildRbacApi(resolveBaseUrl(), httpClientFactory.get(httpClientProfile))

    private fun resolveBaseUrl(): String {
        return baseUrl.ifBlank { defaultBaseUrl }
    }
}
