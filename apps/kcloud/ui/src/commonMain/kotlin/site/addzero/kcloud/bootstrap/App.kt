package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.kcloud.di.initKoin
import site.addzero.kcloud.window.main.RenderWorkbenchWindow
import site.addzero.kcloud.shell.spi_impl.ScaffoldingImpl
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun App() {
    initKoin()

    val scaffolding = koinInject<ScaffoldingSpi>() as ScaffoldingImpl
    RenderWorkbenchWindow(
        scaffolding = scaffolding,
    )
}


