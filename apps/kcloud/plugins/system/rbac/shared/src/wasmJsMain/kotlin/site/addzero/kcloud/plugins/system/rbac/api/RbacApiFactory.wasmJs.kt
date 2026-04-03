package site.addzero.kcloud.plugins.system.rbac.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildUserCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): UserCenterApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createUserCenterApi()
}

internal actual fun buildRbacApi(
    baseUrl: String,
    httpClient: HttpClient,
): RbacApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createRbacApi()
}
