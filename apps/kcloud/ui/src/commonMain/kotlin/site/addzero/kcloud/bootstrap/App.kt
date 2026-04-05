package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import site.addzero.kcloud.window.main.RenderWorkbenchWindow
import site.addzero.kcloud.window.main.ScaffoldingImpl
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun App() {
    val scaffolding = koinInject<ScaffoldingSpi>() as ScaffoldingImpl
    RenderWorkbenchWindow(
        scaffolding = scaffolding,
    )
}
