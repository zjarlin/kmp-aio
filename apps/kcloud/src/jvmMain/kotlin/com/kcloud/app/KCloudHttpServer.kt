package com.kcloud.app

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.feature.ShellLocalServerService
import com.kcloud.app.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.starter.statuspages.installDefaultStatusPages
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.logging.Logger

@Single(binds = [ShellLocalServerService::class])
class KCloudHttpServer(
    serverFeatures: List<KCloudServerFeature>
) : ShellLocalServerService {
    private val logger = Logger.getLogger(KCloudHttpServer::class.java.name)
    private val serverFeatures = serverFeatures.sortedBy { it.order }
    private var server: EmbeddedServer<*, *>? = null
    private var port: Int? = null
    private val _baseUrl = MutableStateFlow<String?>(null)

    override val baseUrl: StateFlow<String?> = _baseUrl.asStateFlow()

    fun start(wait: Boolean = false) {
        if (server != null) {
            return
        }

        val preferredPort = resolvePreferredPort()
        val resolvedPort = resolveAvailablePort(preferredPort)
        if (resolvedPort != preferredPort) {
            logger.warning("Port $preferredPort is in use, fallback to $resolvedPort")
        }

        server = embeddedServer(CIO, host = "127.0.0.1", port = resolvedPort) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
            installDefaultStatusPages()
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.ContentType)
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
            }
            routing {
                registerGeneratedSpringRoutes()
                serverFeatures.forEach { feature ->
                    feature.installHttp(this)
                }
            }
        }.start(wait = wait)
        port = resolvedPort
        _baseUrl.value = "http://127.0.0.1:$resolvedPort"
        logger.info("KCloud local server started at ${_baseUrl.value.orEmpty()}")
    }

    fun stop() {
        server?.stop(500, 1_000)
        server = null
        port = null
        _baseUrl.value = null
    }

    private fun resolvePreferredPort(): Int {
        return System.getProperty("kcloud.localServer.port")?.toIntOrNull()
            ?: System.getenv("KCLOUD_LOCAL_SERVER_PORT")?.toIntOrNull()
            ?: 18080
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
}
