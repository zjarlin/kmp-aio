package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.*
import site.addzero.starter.AppStarter
import site.addzero.starter.openapi.spi.OpenApiSpi

@Single
class OpenApiStarter(val spi: OpenApiSpi) : AppStarter<Application> {

    override fun Application.enable(): Boolean {
        return spi.enabled
    }

    override fun Application.onInstall() {
        routing {
            swaggerUI(spi.openapiPath, spi.openapiSpec)
        }
    }
}
