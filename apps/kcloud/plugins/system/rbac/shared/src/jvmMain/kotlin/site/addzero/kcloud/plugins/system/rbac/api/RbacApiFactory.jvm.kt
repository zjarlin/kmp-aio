package site.addzero.kcloud.plugins.system.rbac.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildUserCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): UserCenterApi {
    val ktorfit = Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
    return ktorfit.create<UserCenterApi>()
}

internal actual fun buildRbacApi(
    baseUrl: String,
    httpClient: HttpClient,
): RbacApi {
    val ktorfit = Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
    return ktorfit.create<RbacApi>()
}
