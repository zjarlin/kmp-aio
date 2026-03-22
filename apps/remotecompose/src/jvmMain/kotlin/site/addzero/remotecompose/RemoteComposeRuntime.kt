package site.addzero.remotecompose

import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import site.addzero.remotecompose.client.RemoteComposeLocalePreferences
import site.addzero.remotecompose.client.remoteComposeClientModule
import site.addzero.remotecompose.server.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.remotecompose.server.remoteComposeServerModule
import site.addzero.remotecompose.shared.RemoteComposeJson
import java.net.InetSocketAddress
import java.net.ServerSocket

private const val DEFAULT_DEMO_PORT = 18361

class RemoteComposeRuntime(
    private val koinApplication: KoinApplication,
    private val server: EmbeddedServer<*, *>,
) {
    fun start() {
        server.start(wait = false)
    }

    fun stop() {
        server.stop(500, 1_000)
        koinApplication.close()
    }
}

fun createRemoteComposeRuntime(): RemoteComposeRuntime {
    val port = resolveAvailablePort(DEFAULT_DEMO_PORT)
    val baseUrl = "http://127.0.0.1:$port/"
    val localePreferences: RemoteComposeLocalePreferences = JvmRemoteComposeLocalePreferences()

    val koinApplication = startKoin {
        modules(
            remoteComposeServerModule(),
            remoteComposeClientModule(
                baseUrl = baseUrl,
                localePreferences = localePreferences,
            ),
        )
    }

    val server = embeddedServer(
        factory = CIO,
        host = "127.0.0.1",
        port = port,
    ) {
        install(ContentNegotiation) {
            json(RemoteComposeJson.instance)
        }
        routing {
            registerGeneratedSpringRoutes()
        }
    }

    return RemoteComposeRuntime(
        koinApplication = koinApplication,
        server = server,
    )
}

private fun resolveAvailablePort(preferredPort: Int): Int {
    if (isPortAvailable(preferredPort)) {
        return preferredPort
    }

    return ServerSocket(0).use { socket ->
        socket.localPort
    }
}

private fun isPortAvailable(port: Int): Boolean {
    return runCatching {
        ServerSocket().use { socket ->
            socket.reuseAddress = true
            socket.bind(InetSocketAddress("127.0.0.1", port))
        }
        true
    }.getOrDefault(false)
}
