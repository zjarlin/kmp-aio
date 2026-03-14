package com.kcloud.plugin

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class KCloudMenuEntry(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val visible: Boolean = true,
    val content: (@Composable () -> Unit)? = null
) {
    val isGroup: Boolean
        get() = content == null
}

data class KCloudMenuNode(
    val entry: KCloudMenuEntry,
    val children: List<KCloudMenuNode> = emptyList(),
    val ancestorIds: List<String> = emptyList()
) {
    val id: String
        get() = entry.id

    val title: String
        get() = entry.title

    val icon: ImageVector?
        get() = entry.icon

    val parentId: String?
        get() = entry.parentId

    val visible: Boolean
        get() = entry.visible

    val level: Int
        get() = ancestorIds.size

    val isLeaf: Boolean
        get() = children.isEmpty() && entry.content != null

    val isGroup: Boolean
        get() = entry.isGroup
}

object KCloudMenuGroups {
    const val SYNC = "group.sync"
    const val MANAGEMENT = "group.management"
    const val SYSTEM = "group.system"
}

object KCloudMenuTreeBuilder {
    fun buildTree(entries: List<KCloudMenuEntry>): List<KCloudMenuNode> {
        if (entries.isEmpty()) {
            return emptyList()
        }

        val duplicates = entries.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) {
            "检测到重复菜单 id: ${duplicates.joinToString()}"
        }

        val entriesById = entries.associateBy { it.id }

        entries.forEach { entry ->
            if (entry.parentId != null) {
                require(entriesById.containsKey(entry.parentId)) {
                    "菜单 ${entry.id} 的父节点 ${entry.parentId} 不存在"
                }
            }
        }

        detectCycles(entriesById)

        val childrenByParent = entries.groupBy { it.parentId }
        entries.forEach { entry ->
            val childEntries = childrenByParent[entry.id].orEmpty()
            if (childEntries.isNotEmpty()) {
                require(entry.content == null) {
                    "菜单 ${entry.id} 同时定义了 children 和 content，父节点必须是分组节点"
                }
            }
        }

        fun buildNode(entry: KCloudMenuEntry, ancestorIds: List<String>): KCloudMenuNode {
            val children = childrenByParent[entry.id]
                .orEmpty()
                .sortedBy { it.sortOrder }
                .map { child -> buildNode(child, ancestorIds + entry.id) }

            return KCloudMenuNode(
                entry = entry,
                children = children,
                ancestorIds = ancestorIds
            )
        }

        return childrenByParent[null]
            .orEmpty()
            .sortedBy { it.sortOrder }
            .map { root -> buildNode(root, emptyList()) }
    }

    fun flattenVisibleLeaves(nodes: List<KCloudMenuNode>): List<KCloudMenuNode> {
        val leaves = mutableListOf<KCloudMenuNode>()

        fun visit(node: KCloudMenuNode) {
            if (!node.visible) {
                return
            }

            if (node.isLeaf) {
                leaves += node
                return
            }

            node.children.forEach(::visit)
        }

        nodes.forEach(::visit)
        return leaves
    }

    fun flatten(nodes: List<KCloudMenuNode>): List<KCloudMenuNode> {
        val all = mutableListOf<KCloudMenuNode>()

        fun visit(node: KCloudMenuNode) {
            all += node
            node.children.forEach(::visit)
        }

        nodes.forEach(::visit)
        return all
    }

    private fun detectCycles(entriesById: Map<String, KCloudMenuEntry>) {
        val confirmed = mutableSetOf<String>()

        for ((id, entry) in entriesById) {
            if (id in confirmed) {
                continue
            }

            val visited = mutableSetOf<String>()
            val path = mutableListOf<String>()
            var current: KCloudMenuEntry? = entry

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
                current = current.parentId?.let(entriesById::get)
            }

            confirmed += visited
        }
    }
}
