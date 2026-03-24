package site.addzero.kcloud

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import site.addzero.kcloud.features.desktop.system.isSystemTraySupported
import site.addzero.kcloud.ui.MainWindow
import site.addzero.kcloud.ui.tray.KCloudTrayPanelWindow

fun main() {
    val runtime = createKCloudRuntime()
    runtime.startDesktop()

    application {
        val shellState = runtime.shellState
        val windowVisible by shellState.windowVisible.collectAsState()
        val exitRequested by shellState.exitRequested.collectAsState()
        val trayPanelVisible by shellState.trayPanelVisible.collectAsState()

        if (exitRequested) {
            LaunchedEffect(Unit) {
                runtime.stopDesktop()
                exitApplication()
            }
        }

        if (windowVisible && !exitRequested) {
            Window(
                onCloseRequest = {
                    if (isSystemTraySupported()) {
                        shellState.hideWindow()
                    } else {
                        shellState.requestExit()
                    }
                },
                title = "KCloud",
                state = WindowState(width = 1280.dp, height = 860.dp),
            ) {
                MainWindow()
            }
        }

        if (trayPanelVisible && !exitRequested) {
            Window(
                onCloseRequest = {
                    shellState.hideTrayPanel()
                },
                title = "KCloud 传输面板",
                state = WindowState(width = 420.dp, height = 560.dp),
                resizable = false,
                alwaysOnTop = true,
            ) {
                KCloudTrayPanelWindow()
            }
        }
    }
}
