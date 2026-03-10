package site.addzero.notes.server

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import site.addzero.notes.server.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.notes.server.store.NoteStoreRegistry
import java.io.File

fun main(args: Array<String>) {
    NotesEnv.initialize()
    EngineMain.main(args)
}

fun Application.module() {
    val stores = NoteStoreRegistry()
    monitor.subscribe(ApplicationStopping) {
        stores.close()
    }

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
        registerGeneratedSpringRoutes()
    }
}

fun notesKtorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    NotesEnv.initialize()
    val config = loadApplicationConfig(configPath)

    val finalHost = host
        ?: NotesEnv.read("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: NotesEnv.read("SERVER_PORT")?.toIntOrNull()
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
    NotesEnv.initialize()

    if (configPath.isNullOrBlank()) {
        return HoconApplicationConfig(ConfigFactory.load())
    }

    val parsed = ConfigFactory
        .parseFile(File(configPath))
        .withFallback(ConfigFactory.load())
        .resolve()

    return HoconApplicationConfig(parsed)
}

