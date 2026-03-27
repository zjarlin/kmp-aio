package site.addzero.workbenchshell

fun ScreenTree.normalizeScreenId(screenId: String): String {
    val normalized = screenId.trim()
    return when {
        normalized.isBlank() -> defaultLeafId
        normalized in nodesById -> normalized
        else -> defaultLeafId
    }
}

fun ScreenTree.findNode(screenId: String): ScreenNode? {
    return nodesById[normalizeScreenId(screenId)]
}

fun ScreenTree.findLeaf(screenId: String): ScreenNode? {
    val normalized = normalizeScreenId(screenId)
    val node = nodesById[normalized]
    return when {
        node?.isLeaf == true -> node
        else -> visibleLeafNodes.firstOrNull()
    }
}

fun ScreenTree.ancestorIdsFor(screenId: String): List<String> {
    return findNode(screenId)?.ancestorIds.orEmpty()
}

fun ScreenTree.breadcrumbNamesFor(screenId: String): List<String> {
    val node = findNode(screenId) ?: return emptyList()
    return node.ancestorIds.mapNotNull { ancestorId ->
        nodesById[ancestorId]?.name
    }
}

fun ScreenTree.visibleLeafNodesUnder(screenId: String): List<ScreenNode> {
    val node = nodesById[normalizeScreenId(screenId)] ?: return emptyList()
    return buildList {
        collectVisibleLeaves(node)
    }
}
