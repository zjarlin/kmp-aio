package site.addzero.starter.statuspages

import io.ktor.server.application.*
import org.koin.core.annotation.*
import site.addzero.starter.KtorAppStarter
import site.addzero.starter.statuspages.spi.StatusPagesSpi

//@Module
//@ComponentScan("site.addzero.starter.statuspages")
//class StatusPagesStarterKoinModule

@Single
class StatusPagesStarter(
    private val contributors: List<StatusPagesSpi>,
) : KtorAppStarter {
    override val enable
        get() = true

    override fun Application.onInstall() {
        installDefaultStatusPages(contributors)
    }
}
