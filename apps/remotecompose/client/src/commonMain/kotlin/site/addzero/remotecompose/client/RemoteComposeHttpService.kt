package site.addzero.remotecompose.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import site.addzero.remotecompose.shared.RemoteComposeLocale
import site.addzero.remotecompose.shared.RemoteComposeJson
import site.addzero.remotecompose.shared.RemoteComposeScreenPayload
import site.addzero.remotecompose.shared.RemoteComposeScreenSummary

interface RemoteComposeDemoService {
    suspend fun fetchScreens(locale: RemoteComposeLocale): List<RemoteComposeScreenSummary>
    suspend fun fetchScreen(
        screenId: String,
        locale: RemoteComposeLocale,
    ): RemoteComposeScreenPayload
}

class RemoteComposeHttpService(
    private val config: RemoteComposeClientConfig,
) : RemoteComposeDemoService {
    private val httpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(RemoteComposeJson.instance)
        }
    }

    override suspend fun fetchScreens(locale: RemoteComposeLocale): List<RemoteComposeScreenSummary> {
        return httpClient
            .get {
                url(config.baseUrl + "api/remote-compose/screens")
                parameter("locale", locale.code)
                accept(ContentType.Application.Json)
            }
            .body()
    }

    override suspend fun fetchScreen(
        screenId: String,
        locale: RemoteComposeLocale,
    ): RemoteComposeScreenPayload {
        return httpClient
            .get {
                url(config.baseUrl + "api/remote-compose/screens/$screenId")
                parameter("locale", locale.code)
                accept(ContentType.Application.Json)
            }
            .body()
    }
}
