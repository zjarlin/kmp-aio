package site.addzero.fluentdemo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Fluent Demo",
    ) {
        App()
    }
}
