package site.addzero.workbenchshell

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScreenCatalogTest {
    @Test
    fun `builds tree and computes level from ancestors`() {
        val catalog = ScreenCatalog(
            listOf(
                TestScreen(id = "root", name = "Root"),
                TestScreen(id = "child", pid = "root", name = "Child"),
                TestScreen(id = "leaf", pid = "child", name = "Leaf", hasContent = true),
            ),
        )

        val leaf = catalog.tree.single().children.single().children.single()
        assertEquals(2, leaf.level)
        assertEquals(listOf("root", "child"), leaf.ancestorIds)
    }

    @Test
    fun `rejects dangling parent id`() {
        assertFailsWith<IllegalArgumentException> {
            ScreenCatalog(
                listOf(
                    TestScreen(id = "leaf", pid = "missing", name = "Leaf", hasContent = true),
                ),
            )
        }
    }

    @Test
    fun `rejects duplicate ids`() {
        assertFailsWith<IllegalArgumentException> {
            ScreenCatalog(
                listOf(
                    TestScreen(id = "dup", name = "A"),
                    TestScreen(id = "dup", name = "B"),
                ),
            )
        }
    }

    @Test
    fun `rejects content on non leaf node`() {
        assertFailsWith<IllegalArgumentException> {
            ScreenCatalog(
                listOf(
                    TestScreen(id = "root", name = "Root", hasContent = true),
                    TestScreen(id = "child", pid = "root", name = "Child", hasContent = true),
                ),
            )
        }
    }

    @Test
    fun `skips hidden leaves when flattening visible nodes`() {
        val catalog = ScreenCatalog(
            listOf(
                TestScreen(id = "root", name = "Root"),
                TestScreen(id = "visible", pid = "root", name = "Visible", hasContent = true),
                TestScreen(id = "hidden", pid = "root", name = "Hidden", visible = false, hasContent = true),
            ),
        )

        assertEquals(
            listOf("visible"),
            catalog.visibleLeafNodes.map { node -> node.id },
        )
    }

    @Test
    fun `sorts siblings by sort then name`() {
        val catalog = ScreenCatalog(
            listOf(
                TestScreen(id = "root", name = "Root"),
                TestScreen(id = "b", pid = "root", name = "B", sort = 10, hasContent = true),
                TestScreen(id = "a", pid = "root", name = "A", sort = 10, hasContent = true),
                TestScreen(id = "first", pid = "root", name = "First", sort = 0, hasContent = true),
            ),
        )

        assertEquals(
            listOf("first", "a", "b"),
            catalog.tree.single().children.map { node -> node.id },
        )
    }
}

private data class TestScreen(
    override val id: String,
    override val name: String,
    override val pid: String? = null,
    override val sort: Int = Int.MAX_VALUE,
    override val visible: Boolean = true,
    private val hasContent: Boolean = false,
) : Screen {
    override val content = if (hasContent) {
        @androidx.compose.runtime.Composable {}
    } else {
        null
    }
}
