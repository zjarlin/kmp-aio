package com.kcloud.plugins.dotfiles.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.dotfiles.DotfilesService
import com.kcloud.plugins.dotfiles.server.routes.installDotfilesRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class DotfilesServerPlugin(
    private val service: DotfilesService
) : KCloudServerPlugin {
    override val pluginId = "dotfiles-server-plugin"
    override val order = 80

    override fun installHttp(routing: Routing) {
        routing.installDotfilesRoutes(service)
    }
}
