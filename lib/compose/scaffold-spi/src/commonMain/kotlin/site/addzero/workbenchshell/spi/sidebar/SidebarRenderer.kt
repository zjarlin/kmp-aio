package site.addzero.workbenchshell.spi.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface SidebarRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}
