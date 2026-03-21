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
        supportText = "把音乐创作、生成管理和系统配置压进一个更轻的工作台侧栏。",
        items = items,
        modifier = modifier,
        state = state,
        onItemClick = { item ->
            onLeafClick(item.id)
        },
    )
}
