package site.addzero.remotecompose

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() {
    val runtime = createRemoteComposeRuntime()
    runtime.start()

    application {
        Window(
            onCloseRequest = {
                runtime.stop()
                exitApplication()
            },
            title = "Remote Compose Demo",
            state = WindowState(
                width = 1460.dp,
                height = 920.dp,
                position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center),
            ),
        ) {
            App()
        }
    }
}
