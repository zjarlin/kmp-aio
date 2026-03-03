package site.addzero.vibepocket.navigation

/**
 * 菜单树构建器，从扁平 [MenuMetadata] 列表构建树形 [MenuNode] 结构。
 *
 * 功能：
 * - 按 parentRouteKey 分组构建父子关系
 * - 子节点按 sortOrder 升序排序
 * - 检测循环引用，抛出 [IllegalArgumentException]
 * - parentRouteKey 不在列表中的节点自动创建虚拟父节点（[MenuNode.isVirtualParent] = true）
 */
object MenuTreeBuilder {

    /**
     * 从扁平 [MenuMetadata] 列表构建树形结构。
     *
     * @param items 扁平菜单元数据列表
     * @return 顶层 [MenuNode] 列表（按 sortOrder 升序排列）
     * @throws IllegalArgumentException 如果检测到循环父引用
     */
    fun buildTree(items: List<MenuMetadata>): List<MenuNode> {
        if (items.isEmpty()) return emptyList()

        // 1. 检测循环引用
        detectCycles(items)

        // 2. 按 routeKey 建立索引
        val itemsByRouteKey = items.associateBy { it.routeKey }

        // 3. 按 parentRouteKey 分组子节点
        val childrenByParent: Map<String?, List<MenuMetadata>> = items.groupBy { it.parentRouteKey }

        // 4. 收集所有被引用但不在列表中的 parentRouteKey（需要创建虚拟父节点）
        val referencedParentKeys = items.mapNotNull { it.parentRouteKey }.toSet()
        val existingKeys = items.map { it.routeKey }.toSet()
        val virtualParentKeys = referencedParentKeys - existingKeys

        // 5. 递归构建子树
        fun buildNode(metadata: MenuMetadata, isVirtual: Boolean): MenuNode {
            val childItems = childrenByParent[metadata.routeKey] ?: emptyList()
            val childNodes = childItems
                .sortedBy { it.sortOrder }
                .map { buildNode(it, isVirtual = false) }
            return MenuNode(
                metadata = metadata,
                children = childNodes,
                isVirtualParent = isVirtual
            )
        }

        // 6. 构建虚拟父节点
        val virtualParentNodes = virtualParentKeys.map { vpKey ->
            val virtualMetadata = MenuMetadata(
                routeKey = vpKey,
                menuNameAlias = vpKey.substringAfterLast('.'),
                parentRouteKey = null,
                visible = true,
                sortOrder = 0
            )
            val childItems = childrenByParent[vpKey] ?: emptyList()
            val childNodes = childItems
                .sortedBy { it.sortOrder }
                .map { buildNode(it, isVirtual = false) }
            MenuNode(
                metadata = virtualMetadata,
                children = childNodes,
                isVirtualParent = true
            )
        }

        // 7. 构建真实顶层节点（parentRouteKey == null）
        val topLevelItems = childrenByParent[null] ?: emptyList()
        val topLevelNodes = topLevelItems
            .sortedBy { it.sortOrder }
            .map { buildNode(it, isVirtual = false) }

        // 8. 合并虚拟父节点和真实顶层节点，按 sortOrder 排序
        return (virtualParentNodes + topLevelNodes).sortedBy { it.metadata.sortOrder }
    }

    /**
     * 将树扁平化为可见叶节点列表（用于导航路由匹配）。
     *
     * 规则：
     * - 不可见节点及其所有后代均被排除
     * - 叶节点 = 没有子节点的节点
     * - 返回顺序为深度优先遍历顺序
     *
     * @param roots 顶层 [MenuNode] 列表
     * @return 可见叶节点的 [MenuMetadata] 列表
     */
    fun flattenVisibleLeaves(roots: List<MenuNode>): List<MenuMetadata> {
        val result = mutableListOf<MenuMetadata>()

        fun traverse(node: MenuNode) {
            // 不可见节点及其所有后代均被排除
            if (!node.metadata.visible) return

            if (node.children.isEmpty()) {
                // 叶节点：收集
                result.add(node.metadata)
            } else {
                // 非叶节点：递归遍历子节点
                for (child in node.children) {
                    traverse(child)
                }
            }
        }

        for (root in roots) {
            traverse(root)
        }

        return result
    }

    /**
     * 检测扁平列表中的循环父引用。
     *
     * 算法：对每个节点，沿 parentRouteKey 链向上追溯，
     * 如果在追溯过程中遇到已访问的节点，则存在循环。
     *
     * @throws IllegalArgumentException 如果检测到循环引用，附带循环路径描述
     */
    private fun detectCycles(items: List<MenuMetadata>) {
        val itemsByRouteKey = items.associateBy { it.routeKey }

        // 追踪全局已确认无环的节点
        val confirmed = mutableSetOf<String>()

        for (item in items) {
            if (item.routeKey in confirmed) continue

            val visited = mutableSetOf<String>()
            val path = mutableListOf<String>()
            var current: MenuMetadata? = item

            while (current != null) {
                if (current.routeKey in confirmed) {
                    // 已确认无环，当前路径上的所有节点也无环
                    break
                }
                if (current.routeKey in visited) {
                    // 找到循环，构建循环路径描述
                    val cycleStart = path.indexOf(current.routeKey)
                    val cyclePath = path.subList(cycleStart, path.size) + current.routeKey
                    throw IllegalArgumentException(
                        "检测到循环父引用: ${cyclePath.joinToString(" -> ")}"
                    )
                }
                visited.add(current.routeKey)
                path.add(current.routeKey)
                current = current.parentRouteKey?.let { itemsByRouteKey[it] }
            }

            // 路径上所有节点确认无环
            confirmed.addAll(visited)
        }
    }
}
