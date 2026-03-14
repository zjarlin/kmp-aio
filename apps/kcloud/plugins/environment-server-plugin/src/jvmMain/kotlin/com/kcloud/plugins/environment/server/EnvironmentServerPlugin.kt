package com.kcloud.plugins.environment.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.environment.EnvironmentSetupService
import com.kcloud.plugins.environment.server.routes.installEnvironmentRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class EnvironmentServerPlugin(
    private val service: EnvironmentSetupService
) : KCloudServerPlugin {
    override val pluginId = "environment-server-plugin"
    override val order = 90

    override fun installHttp(routing: Routing) {
        routing.installEnvironmentRoutes(service)
    }
}
