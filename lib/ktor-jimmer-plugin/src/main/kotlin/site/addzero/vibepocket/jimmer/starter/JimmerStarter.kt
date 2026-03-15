package site.addzero.vibepocket.jimmer.starter

import io.ktor.server.application.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.vibepocket.jimmer.plugin.JimmerPlugin

@Named("jimmerStarter")
@Single(binds = [AppStarter::class])
class JimmerStarter : AppStarter {

    override fun Application.onInstall() {
        install(JimmerPlugin)
    }
}
