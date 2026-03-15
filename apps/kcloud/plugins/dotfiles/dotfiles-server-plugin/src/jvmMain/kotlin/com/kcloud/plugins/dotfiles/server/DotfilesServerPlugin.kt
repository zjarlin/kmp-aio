package com.kcloud.plugins.dotfiles.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.dotfiles.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class DotfilesServerPlugin : KCloudServerPlugin {
    override val pluginId = "dotfiles-server-plugin"
    override val order = 80

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
