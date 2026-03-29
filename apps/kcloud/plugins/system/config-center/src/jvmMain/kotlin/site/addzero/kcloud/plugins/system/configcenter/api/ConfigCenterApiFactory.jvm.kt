package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildConfigCenterApi(
    baseUrl: String,
    httpClient: HttpClient,
): ConfigCenterApi {
    val ktorfit = Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
    return ktorfit.create<ConfigCenterApi>()
}
