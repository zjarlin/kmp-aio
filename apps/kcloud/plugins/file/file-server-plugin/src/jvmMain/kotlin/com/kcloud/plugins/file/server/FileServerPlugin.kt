package com.kcloud.plugins.file.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.file.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class FileServerPlugin : KCloudServerPlugin {
    override val pluginId = "file-server-plugin"
    override val order = 30

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
