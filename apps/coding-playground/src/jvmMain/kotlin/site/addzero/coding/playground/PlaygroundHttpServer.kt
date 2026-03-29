package site.addzero.coding.playground

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
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
