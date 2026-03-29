package site.addzero.kbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration
import site.addzero.kbox.ui.MainWindow

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KBox",
    ) {
        window.minimumSize = java.awt.Dimension(1440, 920)
        KoinApplication(
            configuration = koinConfiguration<KboxComposeKoinApplication>(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                MainWindow()
            }
        }
    }
}
