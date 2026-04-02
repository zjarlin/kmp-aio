package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.*
import site.addzero.configcenter.boolean
import site.addzero.configcenter.string
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.starter.openapi")
class OpenApiStarterKoinModule

@Single
class OpenApiStarter : AppStarter {

    override fun Application.enable(): Boolean {
        return effectiveConfig().boolean(OpenApiConfigKeys.enabled) != false
    }

    override fun Application.onInstall() {
        val config = effectiveConfig()
        val path = config.string(OpenApiConfigKeys.path) ?: "/swagger"
        val spec = config.string(OpenApiConfigKeys.spec) ?: "openapi/documentation.yaml"
        routing {
            swaggerUI(path, spec)
        }
    }
}
