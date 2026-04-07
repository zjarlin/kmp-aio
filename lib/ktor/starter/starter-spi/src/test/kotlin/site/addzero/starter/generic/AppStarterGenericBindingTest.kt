package site.addzero.starter.generic

import io.ktor.server.application.Application
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.koinApplication
import site.addzero.starter.AppStarter
import site.addzero.starter.KtorAppStarter

class AppStarterGenericBindingTest {
    @Test
    fun `koin should resolve ktor starters through a non generic contract`() {
        val app = koinApplication {
            modules(AppStarterGenericBindingTestModule().module())
        }

        try {
            val applicationStarters = app.koin.getAll<KtorAppStarter>()
            val stringStarters = app.koin.getAll<AppStarter<String>>()

            assertEquals(
                listOf(KtorApplicationStarter::class),
                applicationStarters.map { it::class },
            )
            assertEquals(
                listOf(StringStarter::class),
                stringStarters.map { it::class },
            )
        } finally {
            app.close()
        }
    }
}

@Module
@ComponentScan("site.addzero.starter.generic")
class AppStarterGenericBindingTestModule

@Single
class KtorApplicationStarter : KtorAppStarter {
    override val enable: Boolean = true

    override fun Application.onInstall() {
    }
}

@Single
class StringStarter : AppStarter<String> {
    override val enable: Boolean = true

    override fun String.onInstall() {
    }
}
