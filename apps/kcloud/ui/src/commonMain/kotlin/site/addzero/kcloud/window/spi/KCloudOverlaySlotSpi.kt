package site.addzero.kcloud.window.spi

import androidx.compose.runtime.Composable

interface KCloudOverlaySlotSpi {
    // [浮层位] 悬在主页面上方的一层，像舞台上方吊着的幕布，通常放弹窗、助手面板、全局覆盖层。
    @Composable
    fun Render()
}
