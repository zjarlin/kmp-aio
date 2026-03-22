package site.addzero.vibepocket.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.rememberAppSidebarState
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.toAppSidebarItems

@Composable
fun VibePocketFeatureSidebar(
    screenCatalog: ScreenCatalog,
    selectedId: String,
    onLeafClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(screenCatalog) {
        screenCatalog.toAppSidebarItems()
    }
    val state = rememberAppSidebarState(
        initialSelectedId = selectedId,
    )

    LaunchedEffect(selectedId, items) {
        state.updateSelectedId(selectedId)
        state.revealSelection(
            items = items,
            selectedId = selectedId,
        )
    }

    AppSidebar(
        title = "Vibepocket",
        supportText = "只保留音乐工作台、创作资产、设置三页，其余能力都下沉成组件。",
        items = items,
        modifier = modifier,
        state = state,
        onItemClick = { item ->
            onLeafClick(item.id)
        },
    )
}
