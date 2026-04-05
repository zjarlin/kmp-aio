package site.addzero.starter.statuspages

import io.ktor.server.application.*
import org.koin.core.annotation.*
import site.addzero.starter.AppStarter

@Module
@ComponentScan("site.addzero.starter.statuspages")
class StatusPagesStarterKoinModule

@Named("statusPagesStarter")
@Single
class StatusPagesStarter : AppStarter<Application> {

    override fun Application.onInstall() {
        installDefaultStatusPages()
    }
}
