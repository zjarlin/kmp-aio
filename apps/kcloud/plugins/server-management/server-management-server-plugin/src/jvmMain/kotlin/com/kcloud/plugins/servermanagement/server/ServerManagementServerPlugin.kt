package com.kcloud.plugins.servermanagement.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.servermanagement.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class ServerManagementServerPlugin : KCloudServerPlugin {
    override val pluginId = "server-management-server-plugin"
    override val order = 20

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
