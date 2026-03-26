package site.addzero.liquiddemo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "App Sidebar Demo",
    ) {
        App()
    }
}
