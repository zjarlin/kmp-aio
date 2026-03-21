package site.addzero.appsidebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class AppSidebarItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val order: Int = 0,
    val badge: String? = null,
    val keywords: List<String> = emptyList(),
    val children: List<AppSidebarItem> = emptyList(),
    val initiallyExpanded: Boolean = true,
    val onClick: (() -> Unit)? = null,
)

internal val AppSidebarItem.isBranch: Boolean
    get() = children.isNotEmpty()

private val AppSidebarItem.isLeaf: Boolean
    get() = children.isEmpty()

@Stable
class AppSidebarState internal constructor(
    initialSelectedId: String?,
) {
    var selectedId by mutableStateOf(initialSelectedId)
        private set

    var searchQuery by mutableStateOf("")
        private set

    internal val expandedItems = mutableStateMapOf<String, Boolean>()

    fun updateSelectedId(id: String?) {
        selectedId = id
    }

    fun select(item: AppSidebarItem) {
        if (item.isLeaf) {
            selectedId = item.id
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun clearSearch() {
        searchQuery = ""
    }

    fun toggleExpanded(id: String) {
        expandedItems[id] = !(expandedItems[id] ?: true)
    }

    fun resetExpandedState(items: List<AppSidebarItem>) {
        expandedItems.clear()
        registerInitialExpandedState(items)
    }

    fun revealSelection(
        items: List<AppSidebarItem>,
        selectedId: String? = this.selectedId,
    ) {
        items.findAncestorIds(selectedId).forEach { ancestorId ->
            expandedItems[ancestorId] = true
        }
    }

    internal fun ensureInitialized(items: List<AppSidebarItem>) {
        registerInitialExpandedState(items)
        if (selectedId == null) {
            selectedId = items.firstLeafIdOrNull()
        }
    }

    private fun registerInitialExpandedState(items: List<AppSidebarItem>) {
        items.forEach { item ->
            if (item.isBranch && item.id !in expandedItems) {
                expandedItems[item.id] = item.initiallyExpanded
            }
            registerInitialExpandedState(item.children)
        }
    }

    internal companion object {
        val Saver: Saver<AppSidebarState, Any> = listSaver(
            save = { state ->
                listOf(
                    state.selectedId,
                    state.searchQuery,
                    state.expandedItems.entries.map { entry ->
                        listOf(entry.key, entry.value)
                    },
                )
            },
            restore = { restored ->
                val state = AppSidebarState(
                    initialSelectedId = restored[0] as String?,
                )
                state.updateSearchQuery(restored[1] as String)

                @Suppress("UNCHECKED_CAST")
                val expandedEntries = restored[2] as List<List<Any?>>
                expandedEntries.forEach { entry ->
                    val id = entry[0] as? String ?: return@forEach
                    val expanded = entry[1] as? Boolean ?: return@forEach
                    state.expandedItems[id] = expanded
                }
                state
            },
        )
    }
}

@Composable
fun rememberAppSidebarState(
    initialSelectedId: String? = null,
): AppSidebarState = rememberSaveable(
    inputs = arrayOf(initialSelectedId),
    saver = AppSidebarState.Saver,
) {
    AppSidebarState(initialSelectedId = initialSelectedId)
}

internal fun List<AppSidebarItem>.sortSidebarItems(): List<AppSidebarItem> {
    return sortedBy { it.order }.map { item ->
        item.copy(children = item.children.sortSidebarItems())
    }
}

internal fun List<AppSidebarItem>.filterSidebarItems(
    query: String,
): List<AppSidebarItem> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        return this
    }

    return mapNotNull { item ->
        item.filterSidebarItem(normalizedQuery)
    }
}

private fun AppSidebarItem.filterSidebarItem(
    query: String,
): AppSidebarItem? {
    val childMatches = children.mapNotNull { child ->
        child.filterSidebarItem(query)
    }.sortSidebarItems()

    val selfMatches = title.contains(query, ignoreCase = true) ||
        keywords.any { keyword -> keyword.contains(query, ignoreCase = true) }

    if (!selfMatches && childMatches.isEmpty()) {
        return null
    }

    val visibleChildren = if (selfMatches && children.isNotEmpty()) {
        children.sortSidebarItems()
    } else {
        childMatches
    }

    return copy(children = visibleChildren)
}

fun List<AppSidebarItem>.findItemById(
    id: String?,
): AppSidebarItem? {
    if (id == null) {
        return null
    }
    return asSequence()
        .mapNotNull { item -> item.findItemById(id) }
        .firstOrNull()
}

private fun AppSidebarItem.findItemById(
    id: String,
): AppSidebarItem? {
    if (this.id == id) {
        return this
    }
    return children.asSequence()
        .mapNotNull { child -> child.findItemById(id) }
        .firstOrNull()
}

private fun List<AppSidebarItem>.findAncestorIds(
    selectedId: String?,
): List<String> {
    if (selectedId == null) {
        return emptyList()
    }
    return asSequence()
        .mapNotNull { item -> item.findAncestorIds(selectedId, emptyList()) }
        .firstOrNull()
        .orEmpty()
}

private fun AppSidebarItem.findAncestorIds(
    selectedId: String,
    ancestors: List<String>,
): List<String>? {
    if (id == selectedId) {
        return ancestors
    }
    return children.asSequence()
        .mapNotNull { child -> child.findAncestorIds(selectedId, ancestors + id) }
        .firstOrNull()
}

internal fun List<AppSidebarItem>.firstLeafIdOrNull(): String? {
    return asSequence()
        .mapNotNull { item -> item.firstLeafIdOrNull() }
        .firstOrNull()
}

private fun AppSidebarItem.firstLeafIdOrNull(): String? {
    if (isLeaf) {
        return id
    }
    return children.asSequence()
        .mapNotNull { child -> child.firstLeafIdOrNull() }
        .firstOrNull()
}

internal fun AppSidebarItem.containsSelection(
    selectedId: String?,
): Boolean {
    if (selectedId == null) {
        return false
    }
    if (id == selectedId) {
        return true
    }
    return children.any { child -> child.containsSelection(selectedId) }
}
