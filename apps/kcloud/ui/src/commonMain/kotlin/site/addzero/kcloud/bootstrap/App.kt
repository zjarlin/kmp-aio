package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import site.addzero.kcloud.di.initUiKoin
import site.addzero.kcloud.window.main.RenderWorkbenchWindow
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

enum class UiKoinBootstrapMode {
    EnsureStarted,
    AlreadyStarted,
}

@Composable
fun App(
    koinBootstrapMode: UiKoinBootstrapMode = UiKoinBootstrapMode.EnsureStarted,
) {
    remember(koinBootstrapMode) {
        if (koinBootstrapMode == UiKoinBootstrapMode.EnsureStarted) {
            initUiKoin()
        }
    }
    val scaffolding = koinInject<ScaffoldingSpi>()
    RenderWorkbenchWindow(
        scaffolding = scaffolding,
    )
}
