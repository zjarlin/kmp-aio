package site.addzero.kcloud

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.koinConfiguration as kcpConfiguration
import site.addzero.kcloud.ui.MainWindow

@Composable
fun App() {
    if (KoinPlatform.getKoinOrNull() != null) {
        MainWindow()
        return
    }

    KoinApplication(
        configuration = kcpConfiguration<KCloudComposeKoinApplication>(),
    ) {
        MainWindow()
    }
}
