package site.addzero.vibepocket

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.vibepocket.render.installVibePocketWorkbenchModule
import site.addzero.vibepocket.render.uninstallVibePocketWorkbenchModule

fun main() = application {
    // 内嵌 Ktor server，桌面端自带后端
    val server = ktorApplication()
    server.start(wait = false)
    val workbenchModule = installVibePocketWorkbenchModule()

    Window(
        onCloseRequest = {
            uninstallVibePocketWorkbenchModule(workbenchModule)
            server.stop(1000, 2000)
            exitApplication()
        },
        title = "VibePocket",
    ) {
        App()
    }
}
