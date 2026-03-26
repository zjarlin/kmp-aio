package site.addzero.workbenchshell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.AppSidebarConfig
import site.addzero.appsidebar.AppSidebarEvents
import site.addzero.appsidebar.AppSidebarSlots
import site.addzero.appsidebar.AppSidebarState
import site.addzero.appsidebar.rememberAppSidebarState

@Composable
fun ScreenSidebar(
    title: String,
    items: List<ScreenNode>,
    selectedId: String?,
    onLeafClick: (ScreenNode) -> Unit,
    modifier: Modifier = Modifier,
    state: AppSidebarState = rememberAppSidebarState(initialSelectedId = selectedId),
    config: AppSidebarConfig = AppSidebarConfig(),
    slots: AppSidebarSlots<ScreenNode> = AppSidebarSlots(),
    events: AppSidebarEvents<ScreenNode> = AppSidebarEvents(),
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
        config = config,
        icon = ScreenNode::icon,
        children = ScreenNode::children,
        initiallyExpanded = { true },
        selectable = ScreenNode::isLeaf,
        slots = slots,
        events = AppSidebarEvents(
            onKeywordChange = events.onKeywordChange,
            onItemClick = { node ->
                onLeafClick(node)
                events.onItemClick(node)
            },
        ),
    )
}
