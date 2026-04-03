package site.addzero.kcloud.plugins.system.aichat.api

import io.ktor.client.HttpClient
import org.koin.mp.KoinPlatform
import site.addzero.core.network.HttpClientFactory

internal expect fun buildAiChatApi(
    baseUrl: String,
    httpClient: HttpClient,
): AiChatApi

object AiChatApiClient {
    private const val httpClientProfile = "kcloud-system"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: HttpClientFactory
        get() = KoinPlatform.getKoin().get()

    private var baseUrl: String = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    fun createApi(
        customBaseUrl: String,
    ): AiChatApi {
        return buildAiChatApi(
            baseUrl = normalizeBaseUrl(customBaseUrl),
            httpClient = httpClientFactory.get(httpClientProfile),
        )
    }

    val aiChatApi: AiChatApi
        get() = buildAiChatApi(
            baseUrl = normalizeBaseUrl(baseUrl),
            httpClient = httpClientFactory.get(httpClientProfile),
        )

    private fun normalizeBaseUrl(
        value: String,
    ): String {
        return value.trim()
            .ifBlank { defaultBaseUrl }
            .trimEnd('/') + "/"
    }
}
