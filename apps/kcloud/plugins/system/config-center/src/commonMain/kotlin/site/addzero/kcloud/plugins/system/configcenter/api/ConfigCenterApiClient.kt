package site.addzero.kcloud.plugins.system.configcenter.api

import io.ktor.client.HttpClient
import org.koin.mp.KoinPlatform
import site.addzero.core.network.HttpClientFactory

internal expect fun buildConfigCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigCenterApi

object ConfigCenterApiClient {
    private const val httpClientProfile = "kcloud-system"
    private const val defaultBaseUrl = "http://localhost:18080/"
    private val httpClientFactory: HttpClientFactory
        get() = KoinPlatform.getKoin().get()

    @Volatile
    private var baseUrl: String = defaultBaseUrl

    fun configureBaseUrl(
        value: String,
    ) {
        baseUrl = value.ifBlank { defaultBaseUrl }
    }

    val configCenterApi: ConfigCenterApi
        get() = buildConfigCenterApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )
}
