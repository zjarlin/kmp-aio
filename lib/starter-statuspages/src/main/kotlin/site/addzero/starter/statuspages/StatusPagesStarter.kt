package site.addzero.starter.statuspages

import io.ktor.server.application.*
import org.koin.core.annotation.*
import site.addzero.starter.AppStarter

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.starter.statuspages")
class StatusPagesStarterKoinModule

@Named("statusPagesStarter")
@Single(binds = [AppStarter::class])
class StatusPagesStarter : AppStarter {

    override fun Application.onInstall() {
        installDefaultStatusPages()
    }
}
