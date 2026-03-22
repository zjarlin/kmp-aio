package com.kcloud.feature

import org.koin.core.Koin

interface DesktopLifecycleContributor {
    val order: Int get() = Int.MAX_VALUE

    fun onStart(koin: Koin) {
    }

    fun onStop(koin: Koin) {
    }
}

interface ServerLifecycleContributor {
    val order: Int get() = 100

    fun onStart() {
    }

    fun onStop() {
    }
}
