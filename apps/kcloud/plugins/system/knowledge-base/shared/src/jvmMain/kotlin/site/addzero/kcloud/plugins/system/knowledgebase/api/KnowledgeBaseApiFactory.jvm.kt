package site.addzero.kcloud.plugins.system.knowledgebase.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

internal actual fun buildKnowledgeBaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): KnowledgeBaseApi {
    val ktorfit = Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
    return ktorfit.create<KnowledgeBaseApi>()
}
