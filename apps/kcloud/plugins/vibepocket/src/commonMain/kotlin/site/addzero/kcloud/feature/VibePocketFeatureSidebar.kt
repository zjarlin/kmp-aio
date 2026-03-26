package site.addzero.vibepocket.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.appsidebar.AppSidebarConfig
import site.addzero.workbenchshell.ScreenSidebar
import site.addzero.workbenchshell.ScreenTree

@Composable
fun VibePocketFeatureSidebar(
    screenTree: ScreenTree,
    selectedId: String,
    onLeafClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenSidebar(
        title = "Vibepocket",
        items = screenTree.roots,
        selectedId = selectedId,
        onLeafClick = { node ->
            onLeafClick(node.id)
        },
        config = AppSidebarConfig(
            supportText = "只保留音乐工作台、创作资产、设置三页，其余能力都下沉成组件。",
        ),
        modifier = modifier,
    )
}
