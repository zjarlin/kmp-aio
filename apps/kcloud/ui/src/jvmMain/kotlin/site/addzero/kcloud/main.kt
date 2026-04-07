package site.addzero.kcloud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.kcloud.bootstrap.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OKMY DICS",
    ) {
        App()
    }
}