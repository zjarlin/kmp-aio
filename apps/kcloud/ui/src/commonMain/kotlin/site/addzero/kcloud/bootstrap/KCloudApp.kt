package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import site.addzero.kcloud.window.spi.MainWindowSpi

@Composable
fun KCloudApp() {
    koinInject<MainWindowSpi>().Render()
}
