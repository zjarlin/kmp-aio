package site.addzero.kcloud.app.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.kcloud.app.KCloudRouteCatalog
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.workbenchshell.spi.header.HeaderRender

class KCloudHeaderRender(
    private val routeCatalog: KCloudRouteCatalog,
    private val shellState: KCloudShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
    }
}
