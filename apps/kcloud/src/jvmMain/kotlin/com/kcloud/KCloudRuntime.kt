package com.kcloud

import com.kcloud.app.KCloudHttpServer
import com.kcloud.app.KCloudPluginRegistry
import com.kcloud.app.KCloudShellState
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudServerPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.withConfiguration

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
