package com.kcloud.plugin

import io.ktor.server.routing.Routing
import org.koin.core.Koin
import org.koin.core.module.Module

interface KCloudPlugin {
    val pluginId: String
    val order: Int get() = 100
    val menuEntries: List<KCloudMenuEntry> get() = emptyList()

    fun onStart(koin: Koin) {
    }

    fun onStop(koin: Koin) {
    }
}

interface KCloudServerPlugin {
    val pluginId: String
    val order: Int get() = 100

    fun onStart(koin: Koin) {
    }

    fun onStop(koin: Koin) {
    }

    fun installHttp(routing: Routing, koin: Koin) {
    }
}

interface KCloudPluginBundle {
    val koinModules: List<Module>
}
