package site.addzero.kcpi18ndemo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kcp-i18n-demo",
    ) {
        App()
    }
}
