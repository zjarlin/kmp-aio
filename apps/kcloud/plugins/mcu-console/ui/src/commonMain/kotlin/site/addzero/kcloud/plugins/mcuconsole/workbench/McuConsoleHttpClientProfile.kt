package site.addzero.kcloud.plugins.mcuconsole.workbench

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.koin.core.annotation.Single
import site.addzero.core.network.spi.HttpClientProfileSpi

@Single
class McuConsoleHttpClientProfile : HttpClientProfileSpi {
    override val profile = "kcloud-api"
    override val enableCurlLogging = false
    override val headers: Map<String, String> = mapOf(
        HttpHeaders.ContentType to ContentType.Application.Json.toString(),
    )
}
