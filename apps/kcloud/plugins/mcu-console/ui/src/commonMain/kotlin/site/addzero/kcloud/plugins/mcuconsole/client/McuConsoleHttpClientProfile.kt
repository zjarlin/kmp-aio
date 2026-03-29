package site.addzero.kcloud.plugins.mcuconsole.client

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientProfileSpi
import site.addzero.core.network.HttpClientRequestContribution

@Single
class McuConsoleHttpClientProfile : HttpClientProfileSpi {
    override val profile: String = "kcloud-mcu-console"

    override fun requestContribution(): HttpClientRequestContribution {
        return HttpClientRequestContribution(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.Application.Json.toString(),
            ),
        )
    }
}
