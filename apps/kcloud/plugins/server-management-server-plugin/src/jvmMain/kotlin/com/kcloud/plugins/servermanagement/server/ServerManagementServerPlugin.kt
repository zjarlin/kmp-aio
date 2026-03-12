package com.kcloud.plugins.servermanagement.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.servermanagement.ServerManagementService
import com.kcloud.server.routes.installServerManagementRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val serverManagementServerPluginModule = module {
    single<ServerManagementService> { ServerManagementServiceImpl() }
    singleOf(::ServerManagementServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object ServerManagementServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(serverManagementServerPluginModule)
}

class ServerManagementServerPlugin : KCloudServerPlugin {
    override val pluginId = "server-management-server-plugin"
    override val order = 20

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installServerManagementRoutes(koin.get())
    }
}
