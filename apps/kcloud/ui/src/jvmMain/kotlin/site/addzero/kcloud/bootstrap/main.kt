package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.KoinApplication
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.cupertino.workbench.metrics.LocalWorkbenchMetrics
import site.addzero.cupertino.workbench.metrics.WorkbenchPresets
import site.addzero.kcloud.server.EmbeddedDesktopServerHandle
import site.addzero.kcloud.server.embeddedApplicationConfigOverride
import site.addzero.kcloud.server.embeddedDesktopBaseUrl
import site.addzero.kcloud.server.embeddedDesktopKoinConfigurer
import site.addzero.kcloud.server.ktorApplication
import site.addzero.workbench.immersivedesktop.MacOsImmersiveDesktopWindowConfig
import site.addzero.workbench.immersivedesktop.ProvideMacOsImmersiveDesktopWindowFrame
import site.addzero.workbench.immersivedesktop.configureImmersiveDesktopRuntime

private val desktopUiMetrics = WorkbenchPresets.DesktopCompact
private val macOsImmersiveWindowConfig = MacOsImmersiveDesktopWindowConfig(
    topBarHeight = desktopUiMetrics.topBarHeight,
    leadingInset = desktopUiMetrics.topBarLeadingInset,
    toggleSystemPropertyKey = "kcloud.window.macImmersive.enabled",
)

fun main() {
    configureImmersiveDesktopRuntime()
    application {
        val embeddedServer = remember {
            embeddedDesktopKoinConfigurer = { withConfiguration<KoinApplication>() }
            val server = ktorApplication(
            ).start(wait = false)
            val baseUrl = requireNotNull(embeddedDesktopBaseUrl) {
                "embeddedDesktopBaseUrl 尚未初始化。"
            }
            val frontendRuntimeConfig = KcloudFrontendRuntimeConfig(
                apiBaseUrl = baseUrl,
            )
            object : EmbeddedDesktopServerHandle {
                override val frontendRuntimeConfig = frontendRuntimeConfig

                override fun close() {
                    server.stop(
                        gracePeriodMillis = 1_000,
                        timeoutMillis = 5_000,
                    )
                    embeddedDesktopKoinConfigurer = null
                    embeddedApplicationConfigOverride = null
                    embeddedDesktopBaseUrl = null
                }
            }
        }

        DisposableEffect(embeddedServer) {
            onDispose {
                embeddedServer.close()
            }
        }

        val windowState = rememberWindowState(
            width = desktopUiMetrics.defaultWindowWidth,
            height = desktopUiMetrics.defaultWindowHeight,
            placement = WindowPlacement.Maximized,
        )

        Window(
            onCloseRequest = { exitApplication() },
            title = "kcloud",
            state = windowState,
        ) {
            ProvideMacOsImmersiveDesktopWindowFrame(
                state = windowState,
                config = macOsImmersiveWindowConfig,
            ) {
                CompositionLocalProvider(
                    LocalWorkbenchMetrics provides desktopUiMetrics,
                ) {
                    App()
                }
            }
        }
    }
}
