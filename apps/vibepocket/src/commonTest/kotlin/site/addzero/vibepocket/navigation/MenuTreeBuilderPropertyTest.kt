package site.addzero.vibepocket.navigation

import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Property-based tests for [MenuTreeBuilder].
 *
 * // Feature: vibepocket-ui-overhaul, Property 3: 树构建子节点按 sortOrder 排序
 * // Feature: vibepocket-ui-overhaul, Property 4: 不可见节点从可见叶节点列表中排除
 * // Feature: vibepocket-ui-overhaul, Property 5: 虚拟父节点检测
 *
 * **Validates: Requirements 3.2, 3.3, 3.4, 7.3**
 */
@OptIn(ExperimentalKotest::class)
class MenuTreeBuilderPropertyTest {

    // ─── Generators ───────────────────────────────────────────────────────

    /**
     * Generate a list of [MenuMetadata] items that form a valid DAG (no cycles).
     *
     * Strategy: items are generated sequentially. Each item's parentRouteKey is
     * either null (top-level) or references a routeKey from a *previously*
     * generated item, guaranteeing no circular references.
     *
     * @param sizeRange range for the number of items to generate
     * @param invisibleProbability probability that a node has visible=false
     */
    private fun arbMenuMetadataList(
        sizeRange: IntRange = 2..15,
        invisibleProbability: Double = 0.0
    ): Arb<List<MenuMetadata>> = arbitrary {
        val size = Arb.int(sizeRange).bind()
        val items = mutableListOf<MenuMetadata>()

        for (i in 0 until size) {
            val routeKey = "route.$i"
            val parentRouteKey = if (i == 0 || Arb.boolean().bind()) {
                // ~50% chance of being top-level, first item is always top-level
                null
            } else {
                // Reference a previously generated item (ensures DAG)
                val parentIndex = Arb.int(0 until i).bind()
                items[parentIndex].routeKey
            }
            val visible = if (invisibleProbability > 0.0) {
                // Use the probability to decide visibility
                Arb.double(0.0..1.0).bind() >= invisibleProbability
            } else {
                true
            }
            items.add(
                MenuMetadata(
                    routeKey = routeKey,
                    menuNameAlias = "Menu $i",
                    icon = null,
                    parentRouteKey = parentRouteKey,
                    visible = visible,
                    sortOrder = Arb.int(-100..100).bind()
                )
            )
        }
        items.toList()
    }

    /**
     * Generate a list of [MenuMetadata] items where some parentRouteKeys
     * reference keys NOT present in the list (triggering virtual parent creation).
     *
     * Strategy: generate normal DAG items, then for some items, replace their
     * parentRouteKey with a "virtual.X" key that doesn't exist in the list.
     */
    private fun arbMenuMetadataListWithVirtualParents(): Arb<List<MenuMetadata>> = arbitrary {
        val size = Arb.int(3..15).bind()
        val items = mutableListOf<MenuMetadata>()
        // Generate at least one virtual parent key
        val virtualKeyCount = Arb.int(1..3).bind()
        val virtualKeys = (0 until virtualKeyCount).map { "virtual.parent.$it" }

        for (i in 0 until size) {
            val routeKey = "route.$i"
            val parentRouteKey = when {
                i == 0 -> null // First item is always top-level
                Arb.double(0.0..1.0).bind() < 0.35 -> {
                    // ~35% chance: reference a virtual parent key
                    virtualKeys[Arb.int(virtualKeys.indices).bind()]
                }
                Arb.boolean().bind() -> null // top-level
                else -> {
                    // Reference a previously generated item
                    val parentIndex = Arb.int(0 until i).bind()
                    items[parentIndex].routeKey
                }
            }
            items.add(
                MenuMetadata(
                    routeKey = routeKey,
                    menuNameAlias = "Menu $i",
                    icon = null,
                    parentRouteKey = parentRouteKey,
                    visible = true,
                    sortOrder = Arb.int(-100..100).bind()
                )
            )
        }
        items.toList()
    }

    // ─── Helper functions ─────────────────────────────────────────────────

    /**
     * Recursively collect all nodes in the tree (DFS).
     */
    private fun collectAllNodes(roots: List<MenuNode>): List<MenuNode> {
        val result = mutableListOf<MenuNode>()
        fun traverse(node: MenuNode) {
            result.add(node)
            node.children.forEach { traverse(it) }
        }
        roots.forEach { traverse(it) }
        return result
    }

    /**
     * Collect all routeKeys of a node and its descendants.
     */
    private fun collectDescendantKeys(node: MenuNode): Set<String> {
        val result = mutableSetOf<String>()
        fun traverse(n: MenuNode) {
            result.add(n.metadata.routeKey)
            n.children.forEach { traverse(it) }
        }
        traverse(node)
        return result
    }

    // ─── Property 3: 树构建子节点按 sortOrder 排序 ──────────────────────────

    /**
     * Property 3: 树构建子节点按 sortOrder 排序
     *
     * For any flat list of MenuMetadata items (without circular references),
     * after MenuTreeBuilder.buildTree() constructs the tree, every node's
     * children list SHALL be sorted in ascending order by sortOrder.
     *
     * // Feature: vibepocket-ui-overhaul, Property 3: 树构建子节点按 sortOrder 排序
     * **Validates: Requirements 3.3**
     */
    @Test
    fun property3_childrenSortedBySortOrder() = runTest {
        checkAll(PropTestConfig(iterations = 5), arbMenuMetadataList()) { items ->
            val tree = MenuTreeBuilder.buildTree(items)
            val allNodes = collectAllNodes(tree)

            // Every node's children must be sorted by sortOrder ascending
            for (node in allNodes) {
                if (node.children.size > 1) {
                    node.children.shouldBeSortedWith(
                        compareBy { it.metadata.sortOrder }
                    )
                }
            }

            // Top-level roots should also be sorted by sortOrder
            if (tree.size > 1) {
                tree.shouldBeSortedWith(compareBy { it.metadata.sortOrder })
            }
        }
    }

    // ─── Property 4: 不可见节点从可见叶节点列表中排除 ──────────────────────────

    /**
     * Property 4: 不可见节点从可见叶节点列表中排除
     *
     * For any flat list of MenuMetadata items where some nodes have visible=false,
     * MenuTreeBuilder.flattenVisibleLeaves() SHALL return a list that contains
     * no node whose visible is false, and no node that is a descendant of an
     * invisible node. Additionally, the first element of the returned list
     * (if non-empty) SHALL be the default active route.
     *
     * // Feature: vibepocket-ui-overhaul, Property 4: 不可见节点从可见叶节点列表中排除
     * **Validates: Requirements 3.4, 7.3**
     */
    @Test
    fun property4_invisibleNodesExcludedFromVisibleLeaves() = runTest {
        checkAll(
            PropTestConfig(iterations = 5),
            arbMenuMetadataList(sizeRange = 2..15, invisibleProbability = 0.3)
        ) { items ->
            val tree = MenuTreeBuilder.buildTree(items)
            val visibleLeaves = MenuTreeBuilder.flattenVisibleLeaves(tree)

            // 1. No invisible node should appear in the result
            for (leaf in visibleLeaves) {
                leaf.visible shouldBe true
            }

            // 2. No descendant of an invisible node should appear in the result.
            //    Collect all routeKeys that are under invisible nodes in the tree.
            val excludedKeys = mutableSetOf<String>()
            val allNodes = collectAllNodes(tree)
            for (node in allNodes) {
                if (!node.metadata.visible) {
                    excludedKeys.addAll(collectDescendantKeys(node))
                }
            }
            for (leaf in visibleLeaves) {
                (leaf.routeKey !in excludedKeys) shouldBe true
            }

            // 3. All returned items should be actual leaf nodes (no children in tree)
            val leafRouteKeys = visibleLeaves.map { it.routeKey }.toSet()
            for (node in allNodes) {
                if (node.metadata.routeKey in leafRouteKeys) {
                    node.children.isEmpty() shouldBe true
                }
            }
        }
    }

    // ─── Property 5: 虚拟父节点检测 ──────────────────────────────────────

    /**
     * Property 5: 虚拟父节点检测
     *
     * For any flat list of MenuMetadata items where some parentRouteKey values
     * reference routeKeys that are not present in the list, MenuTreeBuilder.buildTree()
     * SHALL mark those parent nodes as isVirtualParent=true in the resulting MenuNode tree.
     *
     * // Feature: vibepocket-ui-overhaul, Property 5: 虚拟父节点检测
     * **Validates: Requirements 3.2**
     */
    @Test
    fun property5_virtualParentDetection() = runTest {
        checkAll(
            PropTestConfig(iterations = 5),
            arbMenuMetadataListWithVirtualParents()
        ) { items ->
            val existingRouteKeys = items.map { it.routeKey }.toSet()
            val referencedParentKeys = items.mapNotNull { it.parentRouteKey }.toSet()
            val expectedVirtualKeys = referencedParentKeys - existingRouteKeys

            val tree = MenuTreeBuilder.buildTree(items)
            val allNodes = collectAllNodes(tree)

            // 1. Every node whose routeKey is in expectedVirtualKeys must be isVirtualParent=true
            for (node in allNodes) {
                if (node.metadata.routeKey in expectedVirtualKeys) {
                    node.isVirtualParent shouldBe true
                }
            }

            // 2. Every node marked isVirtualParent=true must have a routeKey in expectedVirtualKeys
            for (node in allNodes) {
                if (node.isVirtualParent) {
                    (node.metadata.routeKey in expectedVirtualKeys) shouldBe true
                }
            }

            // 3. All expected virtual parent keys should appear in the tree
            val allRouteKeysInTree = allNodes.map { it.metadata.routeKey }.toSet()
            for (vpKey in expectedVirtualKeys) {
                (vpKey in allRouteKeysInTree) shouldBe true
            }
        }
    }
}
