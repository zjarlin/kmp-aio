package site.addzero.kcloud.plugins.system.aichat.api

import io.ktor.client.HttpClient
import site.addzero.core.network.HttpClientFactory

internal expect fun buildAiChatApi(
    baseUrl: String,
    httpClient: HttpClient,
): AiChatApi

object AiChatApiClient {
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

    val aiChatApi: AiChatApi
        get() = buildAiChatApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )
}
