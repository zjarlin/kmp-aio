package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.kcloud.window.main.RenderWorkbenchWindow
import site.addzero.kcloud.window.main.ScaffoldingImpl
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun App() {
    RenderWorkbenchWindow(
        scaffolding = koinInject<ScaffoldingSpi>() as ScaffoldingImpl,
    )
}
