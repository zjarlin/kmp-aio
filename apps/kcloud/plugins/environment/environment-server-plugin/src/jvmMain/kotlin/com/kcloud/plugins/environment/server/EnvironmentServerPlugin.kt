package com.kcloud.plugins.environment.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.environment.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class EnvironmentServerPlugin : KCloudServerPlugin {
    override val pluginId = "environment-server-plugin"
    override val order = 90

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
