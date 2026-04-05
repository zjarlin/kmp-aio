package site.addzero.kcloud.bootstrap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.fetch.Response
import site.addzero.core.network.json.strictJson
import site.addzero.kcloud.config.KCLOUD_FRONTEND_RUNTIME_CONFIG_FILE_NAME
import site.addzero.kcloud.config.KcloudFrontendRuntimeConfig

private val wasmRuntimeConfigJson = strictJson

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        runCatching {
            loadFrontendRuntimeConfig()
        }.onSuccess { runtimeConfig ->
            bootstrapKcloudFrontendRuntimeConfig(runtimeConfig)
            ComposeViewport {
                App()
            }
        }.onFailure { throwable ->
            renderBootstrapFailure(throwable)
            throw throwable
        }
    }
}

private suspend fun loadFrontendRuntimeConfig(): KcloudFrontendRuntimeConfig {
    val response: Response = window.fetch("./$KCLOUD_FRONTEND_RUNTIME_CONFIG_FILE_NAME").await()
    check(response.ok) {
        "缺少前端 bootstrap 配置文件 ./$KCLOUD_FRONTEND_RUNTIME_CONFIG_FILE_NAME，无法启动 KCloud Wasm。"
    }
    val payload: String = response.text().await()
    return wasmRuntimeConfigJson.decodeFromString<KcloudFrontendRuntimeConfig>(payload)
}

private fun renderBootstrapFailure(
    throwable: Throwable,
) {
    val message = throwable.message
        ?.ifBlank { null }
        ?: "KCloud Wasm 启动失败。"
    document.body?.innerHTML = """
        <pre style="margin:0;padding:16px;white-space:pre-wrap;font-family:ui-monospace, SFMono-Regular, monospace;">$message</pre>
    """.trimIndent()
}
