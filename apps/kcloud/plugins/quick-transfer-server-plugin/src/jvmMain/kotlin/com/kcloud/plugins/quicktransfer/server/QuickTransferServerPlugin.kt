package com.kcloud.plugins.quicktransfer.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.quicktransfer.QuickTransferDropService
import com.kcloud.plugins.quicktransfer.server.routes.installQuickTransferRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class QuickTransferServerPlugin(
    private val service: QuickTransferDropService
) : KCloudServerPlugin {
    override val pluginId = "quick-transfer-server-plugin"
    override val order = 10

    override fun installHttp(routing: Routing) {
        routing.installQuickTransferRoutes(service)
    }
}
