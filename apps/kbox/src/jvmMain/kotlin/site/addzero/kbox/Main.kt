package site.addzero.kbox

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.kbox.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KBox",
    ) {
        window.minimumSize = java.awt.Dimension(1280, 820)
        App(defaultWindowPadding = 12.dp)
    }
}
