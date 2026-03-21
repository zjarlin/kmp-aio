package com.kcloud.feature

import io.ktor.server.routing.*
import org.koin.core.Koin

interface DesktopLifecycleContributor {
    val order: Int get() = Int.MAX_VALUE

    fun onStart(koin: Koin) {
    }

    fun onStop(koin: Koin) {
    }
}

interface KCloudServerFeature {
    val featureId: String
    val order: Int get() = 100

    fun onStart() {
    }

    fun onStop() {
    }

    fun installHttp(routing: Routing) {
    }
}
