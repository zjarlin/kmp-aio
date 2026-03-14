package com.kcloud.plugins.file.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.plugins.file.server.routes.installFileRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class FileServerPlugin(
    private val service: FileWorkspaceService
) : KCloudServerPlugin {
    override val pluginId = "file-server-plugin"
    override val order = 30

    override fun installHttp(routing: Routing) {
        routing.installFileRoutes(service)
    }
}
