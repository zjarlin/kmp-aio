package site.addzero.kcloud.shell.header

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.spi_impl.sys_stats.ShellState
import site.addzero.kcloud.shell.navigation.RouteCatalog
import site.addzero.cupertino.workbench.header.WorkbenchSceneTabs
import site.addzero.workbenchshell.spi.header.HeaderRender

@Single
class HeaderRenderer(
    private val routeCatalog: RouteCatalog,
    private val shellState: ShellState,
) : HeaderRender {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        WorkbenchSceneTabs(
            items = routeCatalog.scenes,
            selectedId = shellState.selectedSceneId,
            onItemClick = { scene ->
                shellState.selectScene(scene.id)
            },
            modifier = modifier,
            itemId = { scene -> scene.id },
            itemLabel = { scene -> scene.name },
            itemIcon = { scene -> scene.icon },
        )
    }
}
