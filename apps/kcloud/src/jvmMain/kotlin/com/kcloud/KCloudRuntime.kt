package com.kcloud

import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudKoinModuleProvider
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugin.ShellLocalServerService
import com.kcloud.plugin.ShellWindowController
import com.kcloud.server.model.HealthResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.annotation.Single
import org.koin.core.module.Module
import org.koin.plugin.module.dsl.withConfiguration
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.ServiceLoader
import java.util.logging.Logger

@Single
class KCloudHttpServer(
    serverPlugins: List<KCloudServerPlugin>
) : ShellLocalServerService {
    private val logger = Logger.getLogger(KCloudHttpServer::class.java.name)
    private val serverPlugins = serverPlugins.sortedBy { it.order }
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
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.ContentType)
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
            }
            routing {
                get("/api/health") {
                    call.respond(
                        HealthResponse(
                            status = "ok",
                            version = "dev",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                serverPlugins.forEach { plugin ->
                    plugin.installHttp(this)
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

class KCloudRuntime(
    private val koinApplication: KoinApplication,
    val shellState: KCloudShellState,
    val pluginRegistry: KCloudPluginRegistry,
    val serverPlugins: List<KCloudServerPlugin>,
    private val httpServer: KCloudHttpServer
) {
    val koin: Koin
        get() = koinApplication.koin

    fun startDesktop() {
        httpServer.start(wait = false)
        serverPlugins.forEach { plugin ->
            plugin.onStart()
        }
        pluginRegistry.plugins.forEach { plugin ->
            plugin.onStart(koin)
        }
    }

    fun startServer(wait: Boolean) {
        httpServer.start(wait = wait)
        serverPlugins.forEach { plugin ->
            plugin.onStart()
        }
    }

    fun stopDesktop() {
        pluginRegistry.plugins
            .asReversed()
            .forEach { plugin -> plugin.onStop(koin) }
        httpServer.stop()
        serverPlugins
            .asReversed()
            .forEach { plugin -> plugin.onStop() }
        koinApplication.close()
    }

    fun stopServer() {
        httpServer.stop()
        serverPlugins
            .asReversed()
            .forEach { plugin -> plugin.onStop() }
        koinApplication.close()
    }
}

fun createKCloudRuntime(): KCloudRuntime {
    val koinApplication = startKoin {
        withConfiguration<KCloudKoinApplication>()
        modules(loadPluginKoinModules())
    }
    val koin = koinApplication.koin
    return KCloudRuntime(
        koinApplication = koinApplication,
        shellState = koin.get(),
        pluginRegistry = koin.get(),
        serverPlugins = koin.getAll<KCloudServerPlugin>().sortedBy { it.order },
        httpServer = koin.get()
    )
}

private fun loadPluginKoinModules(): List<Module> {
    return ServiceLoader.load(
        KCloudKoinModuleProvider::class.java,
        Thread.currentThread().contextClassLoader
    )
        .toList()
        .flatMap { provider -> provider.modules() }
}
