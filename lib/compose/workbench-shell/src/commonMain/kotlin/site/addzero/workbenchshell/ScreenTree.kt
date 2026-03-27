package site.addzero.workbenchshell

class ScreenTree(
    screens: List<Screen>,
) {
    val screens: List<Screen> = screens
        .sortedWith(compareBy<Screen> { screen: Screen -> screen.sort }.thenBy { screen: Screen -> screen.name })

    private val screensById: Map<String, Screen> = this.screens.associateBy { screen: Screen -> screen.id }
    private val childrenByParentId: Map<String?, List<Screen>> = this.screens
        .groupBy { screen: Screen -> screen.pid }
        .mapValues { (_, siblings) ->
            siblings.sortedWith(compareBy<Screen> { screen: Screen -> screen.sort }.thenBy { screen: Screen -> screen.name })
        }

    init {
        validateDuplicateIds()
        validateParentReferences()
        validateCycles()
        validateLeafRules()
    }

    val roots: List<ScreenNode> = childrenByParentId[null]
        .orEmpty()
        .map { screen: Screen -> buildNode(screen, emptyList()) }

    internal val allNodes: List<ScreenNode> = buildList {
        roots.forEach { node -> addSubtree(node) }
    }
    internal val nodesById: Map<String, ScreenNode> = allNodes.associateBy { node: ScreenNode -> node.id }

    val visibleLeafNodes: List<ScreenNode> = buildList {
        roots.forEach { node -> collectVisibleLeaves(node) }
    }

    val defaultLeafId: String = visibleLeafNodes.firstOrNull()?.id.orEmpty()

    val defaultExpandedIds: Set<String> = allNodes
        .filter { node: ScreenNode -> node.children.isNotEmpty() && node.visible }
        .map { node: ScreenNode -> node.id }
        .toSet()

    private fun validateDuplicateIds() {
        val duplicateIds = screens
            .groupBy { screen: Screen -> screen.id }
            .filterValues { duplicates: List<Screen> -> duplicates.size > 1 }
            .keys
        require(duplicateIds.isEmpty()) {
            "检测到重复 screen id: ${duplicateIds.joinToString()}"
        }
    }

    private fun validateParentReferences() {
        screens.forEach { screen ->
            val parentId = screen.pid ?: return@forEach
            require(screensById.containsKey(parentId)) {
                "screen ${screen.id} 的父节点 ${screen.pid} 不存在"
            }
        }
    }

    private fun validateCycles() {
        val confirmed = mutableSetOf<String>()

        screensById.forEach { (_, screen) ->
            if (screen.id in confirmed) {
                return@forEach
            }

            val visited = mutableSetOf<String>()
            val path = mutableListOf<String>()
            var current: Screen? = screen

            while (current != null) {
                if (current.id in confirmed) {
                    break
                }

                if (!visited.add(current.id)) {
                    val cycleStart = path.indexOf(current.id)
                    val cyclePath = path.subList(cycleStart, path.size) + current.id
                    error("检测到循环父引用: ${cyclePath.joinToString(" -> ")}")
                }

                path += current.id
                current = current.pid?.let(screensById::get)
            }

            confirmed += visited
        }
    }

    private fun validateLeafRules() {
        screens.forEach { screen ->
            val children = childrenByParentId[screen.id].orEmpty()
            if (children.isNotEmpty()) {
                require(screen.content == null) {
                    "screen ${screen.id} 同时定义了 children 和 content，父节点必须是纯容器"
                }
            }
        }
    }

    private fun buildNode(
        screen: Screen,
        ancestorIds: List<String>,
    ): ScreenNode {
        val children = childrenByParentId[screen.id]
            .orEmpty()
            .map { child -> buildNode(child, ancestorIds + screen.id) }
        return ScreenNode(
            screen = screen,
            children = children,
            ancestorIds = ancestorIds,
        )
    }

    private fun MutableList<ScreenNode>.addSubtree(
        node: ScreenNode,
    ) {
        add(node)
        node.children.forEach { child -> addSubtree(child) }
    }

    internal fun MutableList<ScreenNode>.collectVisibleLeaves(
        node: ScreenNode,
    ) {
        if (!node.visible) {
            return
        }
        if (node.isLeaf) {
            add(node)
            return
        }
        node.children.forEach { child -> collectVisibleLeaves(child) }
    }
}

data class ScreenNode(
    val screen: Screen,
    val children: List<ScreenNode> = emptyList(),
    val ancestorIds: List<String> = emptyList(),
) {
    val id: String
        get() = screen.id

    val pid: String?
        get() = screen.pid

    val name: String
        get() = screen.name

    val icon = screen.icon

    val sort: Int
        get() = screen.sort

    val visible: Boolean
        get() = screen.visible

    val content = screen.content

    val level: Int
        get() = ancestorIds.size

    val isLeaf: Boolean
        get() = children.isEmpty() && content != null
}
