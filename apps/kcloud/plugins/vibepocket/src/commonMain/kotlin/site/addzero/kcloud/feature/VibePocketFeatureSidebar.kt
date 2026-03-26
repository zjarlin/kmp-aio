package site.addzero.vibepocket.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.ScreenSidebar

@Composable
fun VibePocketFeatureSidebar(
    screenCatalog: ScreenCatalog,
    selectedId: String,
    onLeafClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenSidebar(
        title = "Vibepocket",
        items = screenCatalog.tree,
        selectedId = selectedId,
        onLeafClick = { node ->
            onLeafClick(node.id)
        },
        supportText = "只保留音乐工作台、创作资产、设置三页，其余能力都下沉成组件。",
        modifier = modifier,
    )
}
