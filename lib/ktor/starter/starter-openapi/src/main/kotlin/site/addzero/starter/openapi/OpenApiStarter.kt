package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.starter.openapi.spi.OpenApiSpi

@Module
@ComponentScan("site.addzero.starter.openapi")
class OpenApiStarterKoinModule

@Single
class OpenApiStarter(val spi: OpenApiSpi) : AppStarter<Application> {
    override val enable: Boolean
        get() = spi.enabled

    override fun Application.onInstall() {
        routing {
            swaggerUI(spi.openapiPath, spi.openapiSpec)
        }
    }
}
