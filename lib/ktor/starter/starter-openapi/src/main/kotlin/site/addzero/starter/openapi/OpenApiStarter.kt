package site.addzero.starter.openapi

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.core.annotation.*
import site.addzero.configcenter.ConfigCenter
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig

@Module
@ComponentScan("site.addzero.starter.openapi")
class OpenApiStarterKoinModule

@Single
class OpenApiStarter : AppStarter<Application> {

    override fun Application.enable(): Boolean {
        return ConfigCenter.getEnv(effectiveConfig())
            .path("openapi")
            .boolean("enabled", true) != false
    }

    override fun Application.onInstall() {
        val env = ConfigCenter.getEnv(effectiveConfig()).path("openapi")
        val path = env.string("path", "/swagger") ?: "/swagger"
        val spec = env.string("spec", "openapi/documentation.yaml") ?: "openapi/documentation.yaml"
        routing {
            swaggerUI(path, spec)
        }
    }
}
