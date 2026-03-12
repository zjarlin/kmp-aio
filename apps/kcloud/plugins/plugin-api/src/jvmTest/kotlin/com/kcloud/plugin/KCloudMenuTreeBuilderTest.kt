package com.kcloud.plugin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KCloudMenuTreeBuilderTest {
    @Test
    fun `builds tree and computes level from ancestors`() {
        val roots = KCloudMenuTreeBuilder.buildTree(
            listOf(
                KCloudMenuEntry(id = "root", title = "Root"),
                KCloudMenuEntry(id = "child", title = "Child", parentId = "root"),
                KCloudMenuEntry(id = "leaf", title = "Leaf", parentId = "child", content = {})
            )
        )

        val leaf = roots.single().children.single().children.single()
        assertEquals(2, leaf.level)
        assertEquals(listOf("root", "child"), leaf.ancestorIds)
    }

    @Test
    fun `rejects dangling parent id`() {
        assertFailsWith<IllegalArgumentException> {
            KCloudMenuTreeBuilder.buildTree(
                listOf(
                    KCloudMenuEntry(id = "leaf", title = "Leaf", parentId = "missing", content = {})
                )
            )
        }
    }

    @Test
    fun `rejects duplicate ids`() {
        assertFailsWith<IllegalArgumentException> {
            KCloudMenuTreeBuilder.buildTree(
                listOf(
                    KCloudMenuEntry(id = "dup", title = "A"),
                    KCloudMenuEntry(id = "dup", title = "B")
                )
            )
        }
    }

    @Test
    fun `rejects content on non leaf node`() {
        assertFailsWith<IllegalArgumentException> {
            KCloudMenuTreeBuilder.buildTree(
                listOf(
                    KCloudMenuEntry(id = "root", title = "Root", content = {}),
                    KCloudMenuEntry(id = "child", title = "Child", parentId = "root", content = {})
                )
            )
        }
    }

    @Test
    fun `skips hidden leaves when flattening visible nodes`() {
        val roots = KCloudMenuTreeBuilder.buildTree(
            listOf(
                KCloudMenuEntry(id = "root", title = "Root"),
                KCloudMenuEntry(id = "visible", title = "Visible", parentId = "root", content = {}),
                KCloudMenuEntry(id = "hidden", title = "Hidden", parentId = "root", visible = false, content = {})
            )
        )

        assertEquals(
            listOf("visible"),
            KCloudMenuTreeBuilder.flattenVisibleLeaves(roots).map { node -> node.id }
        )
    }
}
