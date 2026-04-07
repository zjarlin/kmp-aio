@file:Suppress("SpellCheckingInspection")

package site.addzero.kcloud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.kcloud.bootstrap.App
import site.addzero.kcloud.server.startServer

fun main() {
    val serverEngine = startServer(wait = false)

    application {
        Window(
            onCloseRequest = {
                serverEngine.stop(
                    gracePeriodMillis = 1_000,
                    timeoutMillis = 2_000,
                )
                exitApplication()
            },
            title = "OKMY DICS",
        ) {
            App()
        }
    }
}
