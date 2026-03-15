package com.kcloud.plugins.quicktransfer.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.quicktransfer.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class QuickTransferServerPlugin : KCloudServerPlugin {
    override val pluginId = "quick-transfer-server-plugin"
    override val order = 10

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
