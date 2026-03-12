package com.kcloud.plugins.file.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.server.routes.installFileRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val fileServerPluginModule = module {
    single<FileWorkspaceService> { FileWorkspaceServiceImpl() }
    singleOf(::FileServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object FileServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(fileServerPluginModule)
}

class FileServerPlugin : KCloudServerPlugin {
    override val pluginId = "file-server-plugin"
    override val order = 30

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installFileRoutes(koin.get())
    }
}
