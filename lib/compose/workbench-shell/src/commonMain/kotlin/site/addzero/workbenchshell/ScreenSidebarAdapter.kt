package site.addzero.workbenchshell

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.AppSidebarState
import site.addzero.appsidebar.AppSidebarStyle
import site.addzero.appsidebar.rememberAppSidebarState

@Composable
fun ScreenSidebar(
    title: String,
    items: List<ScreenNode>,
    selectedId: String?,
    onLeafClick: (ScreenNode) -> Unit,
    modifier: Modifier = Modifier,
    state: AppSidebarState = rememberAppSidebarState(initialSelectedId = selectedId),
    style: AppSidebarStyle = AppSidebarStyle.Default,
    supportText: String? = null,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索菜单",
    headerSlot: @Composable ColumnScope.() -> Unit = {},
    footerSlot: @Composable ColumnScope.() -> Unit = {},
) {
    LaunchedEffect(selectedId, items) {
        state.updateSelectedId(selectedId)
        state.revealSelection(
            items = items,
            itemId = ScreenNode::id,
            children = ScreenNode::children,
            selectedId = selectedId,
        )
    }

    AppSidebar(
        title = title,
        items = items,
        itemId = ScreenNode::id,
        label = ScreenNode::name,
        modifier = modifier,
        state = state,
        style = style,
        supportText = supportText,
        searchEnabled = searchEnabled,
        searchPlaceholder = searchPlaceholder,
        icon = ScreenNode::icon,
        badge = { node -> node.screen.badge },
        keywords = ScreenNode::keywords,
        children = ScreenNode::children,
        initiallyExpanded = { true },
        selectable = ScreenNode::isLeaf,
        headerSlot = headerSlot,
        footerSlot = footerSlot,
        onItemClick = onLeafClick,
    )
}
