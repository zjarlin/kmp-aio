package com.kcloud.plugins.packages.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.packages.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class PackageOrganizerServerPlugin : KCloudServerPlugin {
    override val pluginId = "package-organizer-server-plugin"
    override val order = 45

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
