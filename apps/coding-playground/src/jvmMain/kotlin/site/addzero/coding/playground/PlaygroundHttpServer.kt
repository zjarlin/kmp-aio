package site.addzero.coding.playground

import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.config.PlaygroundServerSettings
import site.addzero.coding.playground.server.generated.springktor.registerGeneratedSpringRoutes

@Single
class PlaygroundHttpServer(
    private val settings: PlaygroundServerSettings,
) {
    private var server: EmbeddedServer<*, *>? = null

    fun start(wait: Boolean = false) {
        if (server != null) {
            return
        }
        server = embeddedServer(Netty, host = settings.serverHost, port = settings.serverPort) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                    },
                )
            }
            routing {
                registerGeneratedSpringRoutes()
            }
        }.start(wait = wait)
    }

    fun stop() {
        server?.stop(500, 1_000)
        server = null
    }
}
