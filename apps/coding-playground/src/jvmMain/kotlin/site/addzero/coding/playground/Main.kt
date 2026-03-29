package site.addzero.coding.playground

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import site.addzero.coding.playground.ui.PlaygroundApp

fun main() {
    val runtime = createCodingPlaygroundRuntime()
    runtime.start()
    application {
        LaunchedEffect(Unit) {
            runtime.state.refreshAll()
        }
        Window(
            onCloseRequest = {
                runtime.stop()
                exitApplication()
            },
            title = "Kotlin 声明式代码生成台",
            state = WindowState(width = 1420.dp, height = 920.dp),
        ) {
            PlaygroundApp(
                state = runtime.state,
                neteaseDemoState = runtime.neteaseDemoState,
            )
        }
    }
}
