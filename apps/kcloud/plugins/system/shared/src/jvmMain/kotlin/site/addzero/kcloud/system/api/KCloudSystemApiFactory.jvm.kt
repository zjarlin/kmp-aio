package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*

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

internal actual fun buildAiChatApi(
    baseUrl: String,
    httpClient: HttpClient,
): AiChatApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createAiChatApi()
}

internal actual fun buildKnowledgeBaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): KnowledgeBaseApi {
    return Ktorfit.Builder()
        .baseUrl(baseUrl)
        .httpClient(httpClient)
        .build()
        .createKnowledgeBaseApi()
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
