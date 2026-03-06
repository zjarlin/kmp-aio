package site.addzero.notes

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.notes.server.notesKtorApplication

fun main() = application {
    val server = notesKtorApplication().also { it.start(wait = false) }

    Window(
        onCloseRequest = {
            server.stop(gracePeriodMillis = 1000, timeoutMillis = 3000)
            exitApplication()
        },
        title = "VibeNotes"
    ) {
        App()
    }
}
