package com.kcloud.plugins.ssh.server

import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.ssh.SshWorkspaceService
import com.kcloud.plugins.ssh.server.routes.installSshRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class SshServerPlugin(
    private val service: SshWorkspaceService
) : KCloudServerPlugin {
    override val pluginId = "ssh-server-plugin"
    override val order = 60

    override fun installHttp(routing: Routing) {
        routing.installSshRoutes(service)
    }
}
