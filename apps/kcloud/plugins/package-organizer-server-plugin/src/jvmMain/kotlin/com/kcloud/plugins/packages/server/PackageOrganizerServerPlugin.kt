package com.kcloud.plugins.packages.server

import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.plugin.KCloudServerPlugin
import com.kcloud.plugins.packages.PackageOrganizerService
import com.kcloud.server.routes.installPackageOrganizerRoutes
import io.ktor.server.routing.Routing
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private val packageOrganizerServerPluginModule = module {
    single<PackageOrganizerService> { LocalPackageOrganizerService() }
    singleOf(::PackageOrganizerServerPlugin) withOptions {
        bind<KCloudServerPlugin>()
    }
}

object PackageOrganizerServerPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(packageOrganizerServerPluginModule)
}

class PackageOrganizerServerPlugin : KCloudServerPlugin {
    override val pluginId = "package-organizer-server-plugin"
    override val order = 45

    override fun installHttp(routing: Routing, koin: org.koin.core.Koin) {
        routing.installPackageOrganizerRoutes(koin.get())
    }
}
