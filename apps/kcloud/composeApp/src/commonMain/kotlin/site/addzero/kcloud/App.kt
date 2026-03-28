package site.addzero.kcloud

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration
import site.addzero.kcloud.ui.MainWindow

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<KCloudComposeKoinApplication>(),
    ) {
        MainWindow()
    }
}
