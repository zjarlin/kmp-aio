package com.kcloud.plugins.ssh.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.ssh.SshWorkspaceService
import com.kcloud.server.routes.installSshRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val sshServerPluginModule = module {
    single<SshWorkspaceService> { SshWorkspaceServiceImpl() }
    singleOf(::SshServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object SshServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(sshServerPluginModule)
}

class SshServerPlugin : KCloudServerPlugin {
    override val pluginId = "ssh-server-plugin"
    override val order = 60

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installSshRoutes(koin.get())
    }
}
