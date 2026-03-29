package site.addzero.kcloud.plugins.system.knowledgebase.api

import io.ktor.client.HttpClient
import site.addzero.core.network.HttpClientFactory

internal expect fun buildKnowledgeBaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): KnowledgeBaseApi

object KnowledgeBaseApiClient {
    private const val httpClientProfile = "kcloud-system"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: HttpClientFactory
        get() = HttpClientFactory.shared()

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    val knowledgeBaseApi: KnowledgeBaseApi
        get() = buildKnowledgeBaseApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )
}
