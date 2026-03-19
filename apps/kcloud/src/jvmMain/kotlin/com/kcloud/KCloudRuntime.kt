package com.kcloud

import com.kcloud.app.KCloudHttpServer
import com.kcloud.app.KCloudFeatureRegistry
import com.kcloud.app.KCloudShellState
import com.kcloud.db.Database
import com.kcloud.feature.KCloudFeature
import com.kcloud.feature.KCloudServerFeature
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.withConfiguration

class KCloudRuntime(
    private val koinApplication: KoinApplication,
    val shellState: KCloudShellState,
    val featureRegistry: KCloudFeatureRegistry,
    val serverFeatures: List<KCloudServerFeature>,
    private val httpServer: KCloudHttpServer
) {
    val koin: Koin
        get() = koinApplication.koin

    fun startDesktop() {
        httpServer.start(wait = false)
        serverFeatures.forEach { feature ->
            feature.onStart()
        }
        featureRegistry.features.forEach { feature ->
            feature.onStart(koin)
        }
    }

    fun startServer(wait: Boolean) {
        httpServer.start(wait = wait)
        serverFeatures.forEach { feature ->
            feature.onStart()
        }
    }

    fun stopDesktop() {
        featureRegistry.features
            .asReversed()
            .forEach { feature -> feature.onStop(koin) }
        httpServer.stop()
        serverFeatures
            .asReversed()
            .forEach { feature -> feature.onStop() }
        koin.get<Database>().close()
        koinApplication.close()
    }

    fun stopServer() {
        httpServer.stop()
        serverFeatures
            .asReversed()
            .forEach { feature -> feature.onStop() }
        koin.get<Database>().close()
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
        featureRegistry = koin.get(),
        serverFeatures = koin.getAll<KCloudServerFeature>().sortedBy { it.order },
        httpServer = koin.get()
    )
}
