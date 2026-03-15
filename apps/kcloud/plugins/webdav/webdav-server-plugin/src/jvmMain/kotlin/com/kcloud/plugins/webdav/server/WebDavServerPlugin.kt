package com.kcloud.plugins.webdav.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.webdav.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class WebDavServerPlugin : KCloudServerPlugin {
    override val pluginId = "webdav-server-plugin"
    override val order = 70

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
