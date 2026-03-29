package site.addzero.kcloud.plugins.system.rbac.api

import io.ktor.client.HttpClient
import site.addzero.core.network.HttpClientFactory

internal expect fun buildUserCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): UserCenterApi

internal expect fun buildRbacApi(
    baseUrl: String,
    httpClient: HttpClient,
): RbacApi

object RbacApiClient {
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

    val userCenterApi: UserCenterApi
        get() = buildUserCenterApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )

    val rbacApi: RbacApi
        get() = buildRbacApi(
            baseUrl = baseUrl,
            httpClient = httpClientFactory.get(httpClientProfile),
        )
}
