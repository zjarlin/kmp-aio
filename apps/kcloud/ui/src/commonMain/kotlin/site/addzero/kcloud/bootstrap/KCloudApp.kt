package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.kcloud.window.main.RenderKCloudWindow
import site.addzero.kcloud.window.main.ScaffoldingImpl

@Composable
fun KCloudApp() {
    RenderKCloudWindow(
        scaffolding = koinInject<ScaffoldingImpl>(),
    )
}
