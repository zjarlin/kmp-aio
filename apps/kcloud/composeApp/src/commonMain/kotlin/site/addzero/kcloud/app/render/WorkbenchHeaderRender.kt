package site.addzero.kcloud.app.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.kcloud.app.WorkbenchRouteCatalog
import site.addzero.kcloud.app.WorkbenchShellState
import site.addzero.workbenchshell.spi.header.HeaderRender

class WorkbenchHeaderRender(
    private val routeCatalog: WorkbenchRouteCatalog,
    private val shellState: WorkbenchShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
    }
}
