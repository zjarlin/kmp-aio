package com.kcloud

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudMenuNode
import com.kcloud.plugin.KCloudMenuTreeBuilder
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugin.ShellLocalServerService
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.ShellWindowController
import com.kcloud.plugins.environment.EnvironmentPluginMenus
import com.kcloud.plugins.file.FilePluginMenus
import com.kcloud.plugins.notes.NotesPluginMenus
import com.kcloud.plugins.packages.PackageOrganizerPluginMenus
import com.kcloud.plugins.quicktransfer.QuickTransferPluginMenus
import com.kcloud.plugins.servermanagement.ServerManagementPluginMenus
import com.kcloud.plugins.ssh.SshPluginMenus
import com.kcloud.plugins.settings.SettingsPluginMenus
import com.kcloud.plugins.transferhistory.TransferHistoryPluginMenus
import com.kcloud.plugins.webdav.WebDavPluginMenus
import com.kcloud.plugins.dotfiles.DotfilesPluginMenus
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
import org.koin.dsl.module
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.logging.Logger

private val shellModule = module {
    single { KCloudPluginRegistry(getAll<KCloudPlugin>()) }
    single { KCloudServerPluginRegistry(getAll<KCloudServerPlugin>()) }
    single { KCloudShellState(get()) }
    single<ShellWindowController> { get<KCloudShellState>() }
    single { KCloudHttpServer(get()) }
    single<ShellLocalServerService> { get<KCloudHttpServer>() }
}

class KCloudPluginRegistry(
    plugins: List<KCloudPlugin>
) {
    val plugins: List<KCloudPlugin> = plugins.sortedBy { it.order }

    private val shellGroups = listOf(
        KCloudMenuEntry(
            id = KCloudMenuGroups.SYNC,
            title = "同步",
            icon = Icons.Default.Sync,
            sortOrder = 0
        ),
        KCloudMenuEntry(
            id = KCloudMenuGroups.MANAGEMENT,
            title = "管理",
            icon = Icons.Default.Folder,
            sortOrder = 1
        ),
        KCloudMenuEntry(
            id = KCloudMenuGroups.SYSTEM,
            title = "系统",
            icon = Icons.Default.Settings,
            sortOrder = 2
        )
    )

    val menuTree: List<KCloudMenuNode> = KCloudMenuTreeBuilder.buildTree(
        shellGroups + this.plugins.flatMap { plugin -> plugin.menuEntries }
    )

    private val allNodes = KCloudMenuTreeBuilder.flatten(menuTree)
    private val nodesById = allNodes.associateBy { node -> node.id }
    val visibleLeaves: List<KCloudMenuNode> = KCloudMenuTreeBuilder.flattenVisibleLeaves(menuTree)
    val defaultLeafId: String = visibleLeaves.firstOrNull()?.id.orEmpty()
    val defaultExpandedIds: Set<String> = allNodes
        .filter { node -> node.children.isNotEmpty() }
        .map { node -> node.id }
        .toSet()

    private val legacyAliases = mapOf(
        "quick" to QuickTransferPluginMenus.QUICK_TRANSFER,
        "file" to FilePluginMenus.FILE_MANAGER,
        "notes" to NotesPluginMenus.NOTES,
        "packages" to PackageOrganizerPluginMenus.PACKAGES,
        "server" to ServerManagementPluginMenus.SERVER_MANAGEMENT,
        "ssh" to SshPluginMenus.SSH,
        "history" to TransferHistoryPluginMenus.TRANSFER_HISTORY,
        "webdav" to WebDavPluginMenus.WEBDAV,
        "dotfiles" to DotfilesPluginMenus.DOTFILES,
        "environment" to EnvironmentPluginMenus.ENVIRONMENT_SETUP,
        "settings" to SettingsPluginMenus.SETTINGS
    )

    fun normalizeMenuId(menuId: String): String {
        val normalized = legacyAliases[menuId] ?: menuId
        return when {
            normalized.isBlank() -> defaultLeafId
            normalized in nodesById -> normalized
            else -> defaultLeafId
        }
    }

    fun findNode(menuId: String): KCloudMenuNode? {
        return nodesById[normalizeMenuId(menuId)]
    }

    fun findLeaf(menuId: String): KCloudMenuNode? {
        val normalized = normalizeMenuId(menuId)
        val node = nodesById[normalized]
        return when {
            node?.isLeaf == true -> node
            else -> visibleLeaves.firstOrNull()
        }
    }

    fun ancestorIdsFor(menuId: String): List<String> {
        return findNode(menuId)?.ancestorIds.orEmpty()
    }
}

class KCloudServerPluginRegistry(
    plugins: List<KCloudServerPlugin>
) {
    val plugins: List<KCloudServerPlugin> = plugins.sortedBy { it.order }
}

class KCloudShellState(
    private val pluginRegistry: KCloudPluginRegistry
) : ShellWindowController {
    private val _selectedMenuId = MutableStateFlow(pluginRegistry.defaultLeafId)
    val selectedMenuId: StateFlow<String> = _selectedMenuId.asStateFlow()

    private val _expandedMenuIds = MutableStateFlow(pluginRegistry.defaultExpandedIds)
    val expandedMenuIds: StateFlow<Set<String>> = _expandedMenuIds.asStateFlow()

    private val _windowVisible = MutableStateFlow(true)
    val windowVisible: StateFlow<Boolean> = _windowVisible.asStateFlow()

    private val _exitRequested = MutableStateFlow(false)
    val exitRequested: StateFlow<Boolean> = _exitRequested.asStateFlow()

    fun selectMenu(menuId: String) {
        val leaf = pluginRegistry.findLeaf(menuId) ?: return
        _selectedMenuId.value = leaf.id
        expandAncestors(leaf.id)
    }

    fun toggleGroup(menuId: String) {
        val normalized = pluginRegistry.normalizeMenuId(menuId)
        _expandedMenuIds.value = _expandedMenuIds.value.toMutableSet().apply {
            if (!add(normalized)) {
                remove(normalized)
            }
        }
    }

    private fun expandAncestors(menuId: String) {
        _expandedMenuIds.value = _expandedMenuIds.value + pluginRegistry.ancestorIdsFor(menuId)
    }

    override fun showWindow() {
        _windowVisible.value = true
    }

    override fun hideWindow() {
        _windowVisible.value = false
    }

    override fun toggleWindow() {
        _windowVisible.value = !_windowVisible.value
    }

    override fun requestExit() {
        _exitRequested.value = true
    }
}

class KCloudHttpServer(
    private val serverPluginRegistry: KCloudServerPluginRegistry
) : ShellLocalServerService {
    private val logger = Logger.getLogger(KCloudHttpServer::class.java.name)
    private var server: EmbeddedServer<*, *>? = null
    private var port: Int? = null
    private val _baseUrl = MutableStateFlow<String?>(null)

    override val baseUrl: StateFlow<String?> = _baseUrl.asStateFlow()

    fun start(koin: Koin, wait: Boolean = false) {
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
                serverPluginRegistry.plugins.forEach { plugin ->
                    plugin.installHttp(this, koin)
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
    private val pluginRegistry: KCloudPluginRegistry,
    private val serverPluginRegistry: KCloudServerPluginRegistry,
    private val httpServer: KCloudHttpServer
) {
    val koin: Koin
        get() = koinApplication.koin

    fun startDesktop() {
        httpServer.start(koin = koin, wait = false)
        serverPluginRegistry.plugins.forEach { plugin ->
            plugin.onStart(koin)
        }
        pluginRegistry.plugins.forEach { plugin ->
            plugin.onStart(koin)
        }
    }

    fun startServer(wait: Boolean) {
        httpServer.start(koin = koin, wait = wait)
        serverPluginRegistry.plugins.forEach { plugin ->
            plugin.onStart(koin)
        }
    }

    fun stopDesktop() {
        pluginRegistry.plugins
            .asReversed()
            .forEach { plugin -> plugin.onStop(koin) }
        httpServer.stop()
        serverPluginRegistry.plugins
            .asReversed()
            .forEach { plugin -> plugin.onStop(koin) }
        koinApplication.close()
    }

    fun stopServer() {
        httpServer.stop()
        serverPluginRegistry.plugins
            .asReversed()
            .forEach { plugin -> plugin.onStop(koin) }
        koinApplication.close()
    }
}

fun createKCloudRuntime(): KCloudRuntime {
    val koinApplication = startKoin {
        modules(
            listOf(shellModule) + allKCloudPluginBundles.flatMap { bundle -> bundle.koinModules }
        )
    }
    val koin = koinApplication.koin
    return KCloudRuntime(
        koinApplication = koinApplication,
        pluginRegistry = koin.get(),
        serverPluginRegistry = koin.get(),
        httpServer = koin.get()
    )
}
