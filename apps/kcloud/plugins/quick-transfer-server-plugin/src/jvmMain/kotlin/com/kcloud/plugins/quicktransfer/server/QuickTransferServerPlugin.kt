package com.kcloud.plugins.quicktransfer.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.quicktransfer.QuickTransferService
import com.kcloud.server.routes.installQuickTransferRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val quickTransferServerPluginModule = module {
    single<QuickTransferService> { QuickTransferServiceImpl() }
    singleOf(::QuickTransferServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object QuickTransferServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(quickTransferServerPluginModule)
}

class QuickTransferServerPlugin : KCloudServerPlugin {
    override val pluginId = "quick-transfer-server-plugin"
    override val order = 10

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installQuickTransferRoutes(koin.get())
    }
}
