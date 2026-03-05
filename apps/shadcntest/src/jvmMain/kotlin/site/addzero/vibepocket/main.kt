package site.addzero.vibepocket

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
//    // 内嵌 Ktor server，桌面端自带后端
//    val server = ktorApplication()
//
//    LaunchedEffect(Unit) {
//        launch(Dispatchers.IO) {
//            server.start(wait = false)
//        }
//    }

    Window(
        onCloseRequest = {
//            server.stop(1000, 2000)
            exitApplication()
        },
        title = "Vibepocket",
    ) {
        App()
    }
}
