package site.addzero.kcloud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import site.addzero.appsidebar.LocalWorkbenchWindowFrame
import site.addzero.appsidebar.WorkbenchWindowFrame
import java.awt.Container
import javax.swing.JComponent

private val macCaptionBarHeight = 56.dp
private val macCaptionBarLeadingInset = 84.dp

fun main() = application {
    val windowState = rememberWindowState(
        width = 1440.dp,
        height = 920.dp,
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "kcloud",
        state = windowState,
    ) {
        ProvideKCloudWindowFrame(
            state = windowState,
        ) {
            App()
        }
    }
}

@Composable
private fun FrameWindowScope.ProvideKCloudWindowFrame(
    state: WindowState,
    content: @Composable () -> Unit,
) {
    val immersiveEnabled = isMacOs() && state.placement != WindowPlacement.Fullscreen

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
