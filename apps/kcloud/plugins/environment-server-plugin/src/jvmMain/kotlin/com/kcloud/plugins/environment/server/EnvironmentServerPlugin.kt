package com.kcloud.plugins.environment.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.server.routes.installEnvironmentRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val environmentServerPluginModule = module {
    single<EnvironmentSetupService> { EnvironmentSetupServiceImpl() }
    singleOf(::EnvironmentServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object EnvironmentServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(environmentServerPluginModule)
}

class EnvironmentServerPlugin : KCloudServerPlugin {
    override val pluginId = "environment-server-plugin"
    override val order = 90

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installEnvironmentRoutes(koin.get())
    }
}
