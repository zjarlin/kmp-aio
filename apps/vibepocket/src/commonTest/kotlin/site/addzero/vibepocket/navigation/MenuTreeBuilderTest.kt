package site.addzero.vibepocket.navigation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Unit tests for [MenuTreeBuilder].
 *
 * Covers: empty list, single node, circular reference detection,
 * deep nesting (3+ levels), and flattenVisibleLeaves behavior.
 *
 * _Requirements: 3.3, 3.4, 3.5_
 */
class MenuTreeBuilderTest {

    // ─── Empty list ───────────────────────────────────────────────────────

    @Test
    fun buildTree_emptyList_returnsEmptyList() {
        val result = MenuTreeBuilder.buildTree(emptyList())
        result.shouldBeEmpty()
    }

    @Test
    fun flattenVisibleLeaves_emptyList_returnsEmptyList() {
        val result = MenuTreeBuilder.flattenVisibleLeaves(emptyList())
        result.shouldBeEmpty()
    }

    // ─── Single node ──────────────────────────────────────────────────────

    @Test
    fun buildTree_singleTopLevelNode_returnsSingleRoot() {
        val item = MenuMetadata(
            routeKey = "com.example.Home",
            menuNameAlias = "Home",
            icon = "🏠",
            parentRouteKey = null,
            visible = true,
            sortOrder = 0
        )

        val result = MenuTreeBuilder.buildTree(listOf(item))

        result shouldHaveSize 1
        result[0].metadata shouldBe item
        result[0].children.shouldBeEmpty()
        result[0].isVirtualParent shouldBe false
    }

    @Test
    fun flattenVisibleLeaves_singleVisibleLeaf_returnsThatLeaf() {
        val item = MenuMetadata(
            routeKey = "com.example.Home",
            menuNameAlias = "Home",
            icon = "🏠",
            visible = true,
            sortOrder = 0
        )
        val tree = MenuTreeBuilder.buildTree(listOf(item))
        val leaves = MenuTreeBuilder.flattenVisibleLeaves(tree)

        leaves shouldHaveSize 1
        leaves[0] shouldBe item
    }

    // ─── Circular reference detection ─────────────────────────────────────

    @Test
    fun buildTree_directSelfReference_throwsIllegalArgumentException() {
        val item = MenuMetadata(
            routeKey = "A",
            menuNameAlias = "A",
            parentRouteKey = "A", // self-reference
            sortOrder = 0
        )

        shouldThrow<IllegalArgumentException> {
            MenuTreeBuilder.buildTree(listOf(item))
        }
    }

    @Test
    fun buildTree_twoNodeCycle_throwsIllegalArgumentException() {
        val items = listOf(
            MenuMetadata(routeKey = "A", menuNameAlias = "A", parentRouteKey = "B", sortOrder = 0),
            MenuMetadata(routeKey = "B", menuNameAlias = "B", parentRouteKey = "A", sortOrder = 1)
        )

        shouldThrow<IllegalArgumentException> {
            MenuTreeBuilder.buildTree(items)
        }
    }

    @Test
    fun buildTree_threeNodeCycle_throwsIllegalArgumentException() {
        val items = listOf(
            MenuMetadata(routeKey = "A", menuNameAlias = "A", parentRouteKey = "C", sortOrder = 0),
            MenuMetadata(routeKey = "B", menuNameAlias = "B", parentRouteKey = "A", sortOrder = 1),
            MenuMetadata(routeKey = "C", menuNameAlias = "C", parentRouteKey = "B", sortOrder = 2)
        )

        shouldThrow<IllegalArgumentException> {
            MenuTreeBuilder.buildTree(items)
        }
    }

    // ─── Deep nesting (3+ levels) ─────────────────────────────────────────

    @Test
    fun buildTree_threeLevelNesting_buildsCorrectTree() {
        // root -> child -> grandchild
        val items = listOf(
            MenuMetadata(routeKey = "root", menuNameAlias = "Root", sortOrder = 0),
            MenuMetadata(routeKey = "child", menuNameAlias = "Child", parentRouteKey = "root", sortOrder = 0),
            MenuMetadata(routeKey = "grandchild", menuNameAlias = "Grandchild", parentRouteKey = "child", sortOrder = 0)
        )

        val tree = MenuTreeBuilder.buildTree(items)

        tree shouldHaveSize 1
        val root = tree[0]
        root.metadata.routeKey shouldBe "root"
        root.children shouldHaveSize 1

        val child = root.children[0]
        child.metadata.routeKey shouldBe "child"
        child.children shouldHaveSize 1

        val grandchild = child.children[0]
        grandchild.metadata.routeKey shouldBe "grandchild"
        grandchild.children.shouldBeEmpty()
    }

    @Test
    fun buildTree_fourLevelNesting_buildsCorrectTree() {
        // L0 -> L1 -> L2 -> L3
        val items = listOf(
            MenuMetadata(routeKey = "L0", menuNameAlias = "Level 0", sortOrder = 0),
            MenuMetadata(routeKey = "L1", menuNameAlias = "Level 1", parentRouteKey = "L0", sortOrder = 0),
            MenuMetadata(routeKey = "L2", menuNameAlias = "Level 2", parentRouteKey = "L1", sortOrder = 0),
            MenuMetadata(routeKey = "L3", menuNameAlias = "Level 3", parentRouteKey = "L2", sortOrder = 0)
        )

        val tree = MenuTreeBuilder.buildTree(items)

        tree shouldHaveSize 1
        val l0 = tree[0]
        l0.metadata.routeKey shouldBe "L0"

        val l1 = l0.children[0]
        l1.metadata.routeKey shouldBe "L1"

        val l2 = l1.children[0]
        l2.metadata.routeKey shouldBe "L2"

        val l3 = l2.children[0]
        l3.metadata.routeKey shouldBe "L3"
        l3.children.shouldBeEmpty()
    }

    @Test
    fun flattenVisibleLeaves_deepNesting_returnsOnlyLeafNodes() {
        // root -> child -> grandchild (leaf)
        val items = listOf(
            MenuMetadata(routeKey = "root", menuNameAlias = "Root", sortOrder = 0),
            MenuMetadata(routeKey = "child", menuNameAlias = "Child", parentRouteKey = "root", sortOrder = 0),
            MenuMetadata(routeKey = "grandchild", menuNameAlias = "Grandchild", parentRouteKey = "child", sortOrder = 0)
        )

        val tree = MenuTreeBuilder.buildTree(items)
        val leaves = MenuTreeBuilder.flattenVisibleLeaves(tree)

        leaves shouldHaveSize 1
        leaves[0].routeKey shouldBe "grandchild"
    }

    // ─── Children sorted by sortOrder ─────────────────────────────────────

    @Test
    fun buildTree_childrenSortedBySortOrder() {
        val items = listOf(
            MenuMetadata(routeKey = "root", menuNameAlias = "Root", sortOrder = 0),
            MenuMetadata(routeKey = "c", menuNameAlias = "C", parentRouteKey = "root", sortOrder = 30),
            MenuMetadata(routeKey = "a", menuNameAlias = "A", parentRouteKey = "root", sortOrder = 10),
            MenuMetadata(routeKey = "b", menuNameAlias = "B", parentRouteKey = "root", sortOrder = 20)
        )

        val tree = MenuTreeBuilder.buildTree(items)
        val children = tree[0].children

        children shouldHaveSize 3
        children[0].metadata.routeKey shouldBe "a"
        children[1].metadata.routeKey shouldBe "b"
        children[2].metadata.routeKey shouldBe "c"
        children.shouldBeSortedWith(compareBy { it.metadata.sortOrder })
    }

    // ─── Invisible nodes excluded ─────────────────────────────────────────

    @Test
    fun flattenVisibleLeaves_invisibleNodeExcludesDescendants() {
        // root (visible) -> invisibleChild (invisible) -> grandchild (visible)
        // grandchild should NOT appear because its parent is invisible
        val items = listOf(
            MenuMetadata(routeKey = "root", menuNameAlias = "Root", sortOrder = 0),
            MenuMetadata(routeKey = "invisibleChild", menuNameAlias = "Hidden", parentRouteKey = "root", visible = false, sortOrder = 0),
            MenuMetadata(routeKey = "grandchild", menuNameAlias = "Grandchild", parentRouteKey = "invisibleChild", sortOrder = 0),
            MenuMetadata(routeKey = "visibleChild", menuNameAlias = "Visible", parentRouteKey = "root", sortOrder = 1)
        )

        val tree = MenuTreeBuilder.buildTree(items)
        val leaves = MenuTreeBuilder.flattenVisibleLeaves(tree)

        // Only visibleChild should be a visible leaf
        leaves shouldHaveSize 1
        leaves[0].routeKey shouldBe "visibleChild"
    }

    // ─── Virtual parent nodes ─────────────────────────────────────────────

    @Test
    fun buildTree_virtualParent_markedAsVirtualParent() {
        // "child" references "virtualGroup" which is not in the list
        val items = listOf(
            MenuMetadata(routeKey = "child", menuNameAlias = "Child", parentRouteKey = "virtualGroup", sortOrder = 0)
        )

        val tree = MenuTreeBuilder.buildTree(items)

        // The virtual parent should be created as a root node
        tree shouldHaveSize 1
        val virtualNode = tree[0]
        virtualNode.isVirtualParent shouldBe true
        virtualNode.metadata.routeKey shouldBe "virtualGroup"
        virtualNode.children shouldHaveSize 1
        virtualNode.children[0].metadata.routeKey shouldBe "child"
        virtualNode.children[0].isVirtualParent shouldBe false
    }
}
