package com.kcloud

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.kcloud.features.desktop.system.isSystemTraySupported
import com.kcloud.ui.MainWindow

fun main() {
    val runtime = createKCloudRuntime()
    runtime.startDesktop()

    application {
        val shellState = runtime.shellState
        val windowVisible by shellState.windowVisible.collectAsState()
        val exitRequested by shellState.exitRequested.collectAsState()

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
                state = WindowState(width = 1280.dp, height = 860.dp)
            ) {
                MainWindow()
            }
        }
    }
}
