package site.addzero.workbenchshell

import site.addzero.appsidebar.AppSidebarItem

fun ScreenCatalog.toAppSidebarItems(): List<AppSidebarItem> {
    return tree.toAppSidebarItems(
        expandedIds = defaultExpandedIds,
    )
}

fun List<ScreenNode>.toAppSidebarItems(
    expandedIds: Set<String> = emptySet(),
): List<AppSidebarItem> {
    return mapNotNull { node -> node.toAppSidebarItem(expandedIds) }
}

private fun ScreenNode.toAppSidebarItem(
    expandedIds: Set<String>,
): AppSidebarItem? {
    if (!visible) {
        return null
    }

    val childItems = children.toAppSidebarItems(expandedIds)
    if (content == null && childItems.isEmpty()) {
        return null
    }

    return AppSidebarItem(
        id = id,
        title = name,
        icon = icon,
        order = sort,
        keywords = keywords,
        children = childItems,
        initiallyExpanded = id in expandedIds,
    )
}
