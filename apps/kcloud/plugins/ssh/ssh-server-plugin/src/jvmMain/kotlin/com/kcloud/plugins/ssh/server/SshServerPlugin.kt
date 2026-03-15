package com.kcloud.plugins.ssh.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.ssh.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class SshServerPlugin : KCloudServerPlugin {
    override val pluginId = "ssh-server-plugin"
    override val order = 60

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
