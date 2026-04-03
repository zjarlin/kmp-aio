package site.addzero.kcloud.window.spi

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

interface KCloudUserSlotSpi {
    // [驾驶舱右手边] 顶栏最右侧的操作带，像驾驶位右手边按钮区，通常放主题、AI、个人菜单。
    @Composable
    fun RowScope.Render(
        darkTheme: Boolean,
        onThemeToggle: () -> Unit,
    )
}
