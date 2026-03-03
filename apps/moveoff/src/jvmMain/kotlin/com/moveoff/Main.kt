package com.moveoff

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.moveoff.system.SystemTrayManager
import com.moveoff.ui.MainWindow
import java.awt.Dimension

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    val trayManager = remember { SystemTrayManager() }

    Window(
        onCloseRequest = {
            // Minimize to tray instead of exit
            // exitApplication()
        },
        title = "MoveOff - 文件迁移工具",
        state = windowState,
        visible = true
    ) {
        window.minimumSize = Dimension(900, 600)

        // Install system tray on first composition
        androidx.compose.runtime.LaunchedEffect(Unit) {
            trayManager.install(
                onShowWindow = { windowState.isMinimized = false },
                onExit = { exitApplication() }
            )
        }

        // Dispose tray on exit
        androidx.compose.runtime.DisposableEffect(Unit) {
            onDispose {
                trayManager.uninstall()
            }
        }

        MainWindow()
    }
}
