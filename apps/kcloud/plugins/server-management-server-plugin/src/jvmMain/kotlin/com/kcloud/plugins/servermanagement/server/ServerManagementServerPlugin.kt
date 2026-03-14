package com.kcloud.plugins.servermanagement.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.servermanagement.ServerManagementService
import com.kcloud.plugins.servermanagement.server.routes.installServerManagementRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class ServerManagementServerPlugin(
    private val service: ServerManagementService
) : KCloudServerPlugin {
    override val pluginId = "server-management-server-plugin"
    override val order = 20

    override fun installHttp(routing: Routing) {
        routing.installServerManagementRoutes(service)
    }
}
