package com.kcloud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp
import com.kcloud.ui.MainWindow

/**
 * KCloud 应用入口 - 简化版
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KCloud - 文件同步",
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        MainWindow()
    }
}
