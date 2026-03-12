package com.kcloud.plugins.dotfiles.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.dotfiles.DotfilesService
import com.kcloud.server.routes.installDotfilesRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val dotfilesServerPluginModule = module {
    single<DotfilesService> { DotfilesServiceImpl() }
    singleOf(::DotfilesServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object DotfilesServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(dotfilesServerPluginModule)
}

class DotfilesServerPlugin : KCloudServerPlugin {
    override val pluginId = "dotfiles-server-plugin"
    override val order = 80

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installDotfilesRoutes(koin.get())
    }
}
