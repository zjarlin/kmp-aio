package site.addzero.starter.statuspages

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.starter.statuspages.spi.StatusPagesSpi

@Module
@Configuration
@ComponentScan("site.addzero.starter.statuspages")
class StatusPagesStarterKoinModule

@Single
class StatusPagesStarter(
    private val contributors: List<StatusPagesSpi>,
) : AppStarter {
    override val enable
        get() = true

    override fun onInstall(application: Application) {
        application.installDefaultStatusPages(contributors)
    }
}
