package site.addzero.starter.binding

import io.ktor.server.application.Application
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.koinApplication
import site.addzero.starter.AppStarter

class AppStarterBindingTest {
    @Test
    fun `koin should resolve starters through the app starter contract`() {
        val app = koinApplication {
            modules(AppStarterBindingTestModule().module())
        }

        try {
            val applicationStarters = app.koin.getAll<AppStarter>()

            assertEquals(
                listOf(KtorApplicationStarter::class),
                applicationStarters.map { it::class },
            )
        } finally {
            app.close()
        }
    }
}

@Module
@ComponentScan("site.addzero.starter.binding")
class AppStarterBindingTestModule

@Single
class KtorApplicationStarter : AppStarter {
    override val enable: Boolean = true

    override fun onInstall(application: Application) {
    }
}
