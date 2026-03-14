package com.kcloud.plugin

import io.ktor.server.routing.*
import org.koin.core.Koin

interface KCloudPlugin {
    val pluginId: String
    val order: Int get() = Int.MAX_VALUE
    val menuEntries: List<KCloudMenuEntry> get() = emptyList()

    fun onStart(koin: Koin) {
    }

    fun onStop(koin: Koin) {
    }
}

interface KCloudServerPlugin {
    val pluginId: String
    val order: Int get() = 100

    fun onStart() {
    }

    fun onStop() {
    }

    fun installHttp(routing: Routing) {
    }
}
