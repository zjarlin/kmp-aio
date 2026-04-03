package site.addzero.kcloud.window.spi

import androidx.compose.runtime.Composable

interface KCloudWorkbenchScaffoldingSpi {
    // [骨架位] 整个主窗口的骨架，像房子的梁柱，负责把左侧栏、顶栏、中间内容区装成一整页。
    @Composable
    fun Render(
        darkTheme: Boolean,
        onThemeToggle: () -> Unit,
    )
}
