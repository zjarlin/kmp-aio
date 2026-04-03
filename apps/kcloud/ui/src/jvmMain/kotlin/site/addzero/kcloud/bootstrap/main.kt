package site.addzero.kcloud.bootstrap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.disableTitleBar
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.appsidebar.LocalWorkbenchWindowFrame
import site.addzero.appsidebar.WorkbenchWindowFrame
import site.addzero.kcloud.plugins.mcuconsole.api.external.McuConsoleApiClient
import site.addzero.kcloud.server.startEmbeddedDesktopServer
import site.addzero.kcloud.theme.KCloudUiPresets
import site.addzero.kcloud.theme.LocalKCloudUiMetrics
import java.awt.Container
import javax.swing.JComponent

private val desktopUiMetrics = KCloudUiPresets.DesktopCompact
private val macCaptionBarHeight = desktopUiMetrics.topBarHeight
private val macCaptionBarLeadingInset = desktopUiMetrics.topBarLeadingInset

fun main() {
    configureDesktopRuntime()
    application {
        val embeddedServer = remember {
            startEmbeddedDesktopServer(
                configureKoin = {
                    withConfiguration<KCloudUiKoinApplication>()
                },
            )
        }
        configureLocalApiClients(embeddedServer.baseUrl)

        DisposableEffect(embeddedServer) {
            onDispose {
                embeddedServer.close()
            }
        }

        val windowState = rememberWindowState(
            width = desktopUiMetrics.defaultWindowWidth,
            height = desktopUiMetrics.defaultWindowHeight,
        )

        Window(
            onCloseRequest = { exitApplication() },
            title = "kcloud",
            state = windowState,
        ) {
            ProvideKCloudWindowFrame(
                state = windowState,
            ) {
                CompositionLocalProvider(
                    LocalKCloudUiMetrics provides desktopUiMetrics,
                ) {
                    KCloudApp()
                }
            }
        }
    }
}

private fun configureLocalApiClients(
    baseUrl: String,
) {
    configureKCloudApiClients(baseUrl)
    McuConsoleApiClient.configureBaseUrl(baseUrl)
}

@Composable
private fun FrameWindowScope.ProvideKCloudWindowFrame(
    state: WindowState,
    content: @Composable () -> Unit,
) {
    val immersiveEnabled = shouldEnableImmersiveTopBar(state)

    DisposableEffect(window, immersiveEnabled) {
        if (immersiveEnabled) {
            window.findSkiaLayer()?.disableTitleBar(macCaptionBarHeight.value)
            window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }
        onDispose {}
    }

    if (!immersiveEnabled) {
        content()
        return
    }

    CompositionLocalProvider(
        LocalWorkbenchWindowFrame provides WorkbenchWindowFrame(
            immersiveTopBar = true,
            topBarHeight = macCaptionBarHeight,
            leadingInset = macCaptionBarLeadingInset,
        ),
    ) {
        content()
    }
}

private fun isMacOs(): Boolean {
    return System.getProperty("os.name")
        ?.contains("Mac", ignoreCase = true) == true
}

private fun configureDesktopRuntime() {
    System.setProperty("sun.java2d.metal", System.getProperty("sun.java2d.metal") ?: "false")
}

private fun shouldEnableImmersiveTopBar(state: WindowState): Boolean {
    if (!isMacOs() || state.placement == WindowPlacement.Fullscreen) {
        return false
    }
    val explicitToggle = System.getProperty("kcloud.window.macImmersive.enabled")
        ?.toBooleanStrictOrNull()
    if (explicitToggle != null) {
        return explicitToggle
    }
    return isMacImmersiveHostCompatible()
}

private fun isMacImmersiveHostCompatible(): Boolean {
    val javaFeature = Runtime.version().feature()
    val macMajorVersion = System.getProperty("os.version")
        ?.substringBefore('.')
        ?.toIntOrNull()
    return javaFeature < 25 && (macMajorVersion == null || macMajorVersion < 26)
}

private fun <T : JComponent> findComponent(
    container: Container,
    klass: Class<T>,
): T? {
    val componentSequence = container.components.asSequence()
    return componentSequence.filter { klass.isInstance(it) }
        .ifEmpty {
            componentSequence.filterIsInstance<Container>()
                .mapNotNull { child -> findComponent(child, klass) }
        }
        .map { component -> klass.cast(component) }
        .firstOrNull()
}

private inline fun <reified T : JComponent> Container.findComponent(): T? {
    return findComponent(this, T::class.java)
}

private fun ComposeWindow.findSkiaLayer(): SkiaLayer? {
    return findComponent()
}
