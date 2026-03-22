package site.addzero.workbenchshell.spi.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface WorkbenchHeaderRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    )
}
