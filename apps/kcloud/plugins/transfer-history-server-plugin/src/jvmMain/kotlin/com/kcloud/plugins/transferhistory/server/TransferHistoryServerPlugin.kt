package com.kcloud.plugins.transferhistory.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.transferhistory.TransferHistoryService
import com.kcloud.plugins.transferhistory.server.routes.installTransferHistoryRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class TransferHistoryServerPlugin(
    private val service: TransferHistoryService
) : KCloudServerPlugin {
    override val pluginId = "transfer-history-server-plugin"
    override val order = 40

    override fun installHttp(routing: Routing) {
        routing.installTransferHistoryRoutes(service)
    }
}
