package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig

@Single(binds = [AppStarter::class])
class OpenApiStarter : AppStarter {

    override fun Application.enable(): Boolean {
        return effectiveConfig().propertyOrNull("openapi.enabled")?.getString()?.toBoolean() != false
    }
    override fun Application.onInstall() {
        val config = effectiveConfig().config("openapi")
        val path = config.propertyOrNull("path")?.getString() ?: "/swagger"
        val spec = config.propertyOrNull("spec")?.getString() ?: "openapi/documentation.yaml"
        routing {
            swaggerUI(path, spec)
        }
    }
}
