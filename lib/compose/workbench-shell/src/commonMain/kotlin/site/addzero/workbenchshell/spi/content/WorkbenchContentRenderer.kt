package site.addzero.workbenchshell.spi.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

interface WorkbenchContentRenderer {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    ){
    }
}
