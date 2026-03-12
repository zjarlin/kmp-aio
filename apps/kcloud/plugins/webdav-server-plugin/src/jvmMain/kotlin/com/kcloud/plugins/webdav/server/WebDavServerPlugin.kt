package com.kcloud.plugins.webdav.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.webdav.WebDavWorkspaceService
import com.kcloud.server.routes.installWebDavRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val webDavServerPluginModule = module {
    single<WebDavWorkspaceService> { WebDavWorkspaceServiceImpl() }
    singleOf(::WebDavServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object WebDavServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(webDavServerPluginModule)
}

class WebDavServerPlugin : KCloudServerPlugin {
    override val pluginId = "webdav-server-plugin"
    override val order = 70

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installWebDavRoutes(koin.get())
    }
}
