package com.kcloud.plugins.transferhistory.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.transferhistory.TransferHistoryService
import com.kcloud.server.routes.installTransferHistoryRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val transferHistoryServerPluginModule = module {
    single<TransferHistoryService> { TransferHistoryServiceImpl() }
    singleOf(::TransferHistoryServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object TransferHistoryServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(transferHistoryServerPluginModule)
}

class TransferHistoryServerPlugin : KCloudServerPlugin {
    override val pluginId = "transfer-history-server-plugin"
    override val order = 40

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installTransferHistoryRoutes(koin.get())
    }
}
