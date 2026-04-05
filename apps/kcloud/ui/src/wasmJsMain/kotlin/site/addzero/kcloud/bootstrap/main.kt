package site.addzero.kcloud.bootstrap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureKcloudApiBaseUrl(window.location.origin)
    ComposeViewport {
        App()
    }
}
