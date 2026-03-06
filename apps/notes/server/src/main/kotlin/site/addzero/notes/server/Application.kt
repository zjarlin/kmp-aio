package site.addzero.notes.server

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import site.addzero.notes.server.routes.noteRoutes
import site.addzero.notes.server.store.NoteStoreRegistry
import java.io.File

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val stores = NoteStoreRegistry()

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        )
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/documentation.yaml"
        )
        noteRoutes(stores)
    }
}

fun notesKtorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    val config = loadApplicationConfig(configPath)

    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        ?: 18080

    return embeddedServer(
        factory = Netty,
        host = finalHost,
        port = finalPort,
        module = Application::module
    )
}

private fun loadApplicationConfig(configPath: String?): ApplicationConfig {
    if (configPath.isNullOrBlank()) {
        return HoconApplicationConfig(ConfigFactory.load())
    }

    val parsed = ConfigFactory
        .parseFile(File(configPath))
        .withFallback(ConfigFactory.load())
        .resolve()

    return HoconApplicationConfig(parsed)
}
