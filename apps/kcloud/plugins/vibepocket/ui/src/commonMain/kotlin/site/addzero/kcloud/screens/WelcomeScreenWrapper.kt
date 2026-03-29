package site.addzero.kcloud.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import site.addzero.kcloud.auth.WelcomePage
import site.addzero.kcloud.music.persistSunoRuntimeConfig

@Composable
fun WelcomeScreenWrapper(
    onSetupComplete: (token: String, baseUrl: String) -> Unit,
) {
    val scope = rememberCoroutineScope()

    WelcomePage(
        onEnter = { token, url ->
            scope.launch {
                try {
                    persistSunoRuntimeConfig(token, url)
                } catch (_: Exception) {
                    // 配置持久化失败时仍允许进入工作台，避免首次启动被卡死。
                }
                onSetupComplete(token, url)
            }
        },
    )
}
