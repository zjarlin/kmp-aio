package site.addzero.kcloud.ui.app.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.kcloud.ui.app.KCloudRouteCatalog
import site.addzero.kcloud.ui.app.KCloudShellState
import site.addzero.workbenchshell.spi.header.HeaderRender

@Single
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
