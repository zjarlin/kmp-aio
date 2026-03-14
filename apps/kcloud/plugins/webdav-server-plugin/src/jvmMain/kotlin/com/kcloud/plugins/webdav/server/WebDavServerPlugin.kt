package com.kcloud.plugins.webdav.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.webdav.WebDavWorkspaceService
import com.kcloud.plugins.webdav.server.routes.installWebDavRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class WebDavServerPlugin(
    private val service: WebDavWorkspaceService
) : KCloudServerPlugin {
    override val pluginId = "webdav-server-plugin"
    override val order = 70

    override fun installHttp(routing: Routing) {
        routing.installWebDavRoutes(service)
    }
}
