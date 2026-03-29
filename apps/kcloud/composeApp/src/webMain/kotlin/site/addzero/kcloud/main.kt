package site.addzero.kcloud

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.plugin.module.dsl.koinConfiguration as kcpConfiguration

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (GlobalContext.getKoinApplicationOrNull() == null) {
        startKoin(
            kcpConfiguration<KCloudComposeKoinApplication>(),
        )
    }
    ComposeViewport {
        App()
    }
}
