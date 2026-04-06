package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import org.koin.core.KoinApplication
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.window.main.RenderWorkbenchWindow
import site.addzero.kcloud.window.main.ScaffoldingImpl
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi
@ComponentScan("site.addzero")
@Module
class KoinAio

@org.koin.core.annotation.KoinApplication
class KoinAioApp

@Composable
fun App() {
    initKoin()

    val scaffolding = koinInject<ScaffoldingSpi>() as ScaffoldingImpl
    RenderWorkbenchWindow(
        scaffolding = scaffolding,
    )
}
fun initKoin() {
    val orNull = KoinPlatformTools.defaultContext().getOrNull()
    if (orNull == null) {
        startKoin {
            withConfiguration<KoinAioApp>()
        }
    }
}

