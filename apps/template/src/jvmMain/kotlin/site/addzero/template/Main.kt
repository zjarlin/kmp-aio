package site.addzero.template

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * APP_TEMPLATE_NAME 主入口
 *
 * 复制模板后:
 * 1. 修改包名为你的应用包名
 * 2. 修改应用标题
 * 3. 实现业务逻辑
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "APP_TEMPLATE_NAME"
    ) {
        App()
    }
}

@Composable
fun App() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("APP_TEMPLATE_NAME") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hello, APP_TEMPLATE_NAME!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Edit this file to start building your app.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
