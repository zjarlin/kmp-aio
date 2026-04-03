package site.addzero.kcloud.plugins.system.aichat.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

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
