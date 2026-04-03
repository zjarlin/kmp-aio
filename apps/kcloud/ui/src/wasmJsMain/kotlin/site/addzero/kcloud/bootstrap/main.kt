package site.addzero.kcloud.bootstrap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureKCloudApiClients(resolveBrowserBaseUrl())
    ComposeViewport {
        KCloudApp()
    }
}

private fun resolveBrowserBaseUrl(): String {
    val origin = window.location.origin
        .takeUnless { value -> value.isBlank() || value == "null" }
        ?: "http://localhost:18080"
    return origin.trimEnd('/') + "/"
}
