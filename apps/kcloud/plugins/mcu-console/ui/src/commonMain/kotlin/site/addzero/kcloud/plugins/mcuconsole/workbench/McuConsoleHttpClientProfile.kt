package site.addzero.kcloud.plugins.mcuconsole.workbench

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Single
import site.addzero.core.network.HttpClientProfileSpi
import site.addzero.core.network.HttpClientRequestContribution

@Single
class McuConsoleHttpClientProfile : HttpClientProfileSpi {
    override val profile = "kcloud-mcu-console"
    override val enableCurlLogging = false

    override fun requestContribution(): HttpClientRequestContribution {
        return HttpClientRequestContribution(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.Application.Json.toString(),
            ),
        )
    }
}
