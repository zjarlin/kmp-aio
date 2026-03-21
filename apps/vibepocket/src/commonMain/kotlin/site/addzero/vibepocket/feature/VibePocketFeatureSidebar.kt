package site.addzero.vibepocket.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kcloud.feature.KCloudMenuNode
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.AppSidebarItem
import site.addzero.appsidebar.rememberAppSidebarState

@Composable
fun VibePocketFeatureSidebar(
    nodes: List<KCloudMenuNode>,
    selectedId: String,
    onLeafClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(nodes) {
        nodes.toAppSidebarItems()
    }
    val state = rememberAppSidebarState()

    LaunchedEffect(selectedId) {
        state.updateSelectedId(selectedId)
    }

    AppSidebar(
        title = "Vibepocket",
        supportText = "把音乐创作、生成管理和系统配置压进一个更轻的工作台侧栏。",
        items = items,
        modifier = modifier,
        state = state,
        onItemClick = { item ->
            onLeafClick(item.id)
        },
    )
}

/** 菜单树转侧栏模型：叶子节点可选中，分组节点只负责展开收纳。 */
private fun List<KCloudMenuNode>.toAppSidebarItems(): List<AppSidebarItem> {
    return mapIndexedNotNull { index, node ->
        node.toAppSidebarItem(order = index)
    }
}

/** 单个菜单节点映射：保留层级结构，但把交互语义转换成组件库模型。 */
private fun KCloudMenuNode.toAppSidebarItem(
    order: Int,
): AppSidebarItem? {
    if (!visible) {
        return null
    }

    val children = children.mapIndexedNotNull { index, child ->
        child.toAppSidebarItem(order = index)
    }

    return AppSidebarItem(
        id = id,
        title = title,
        icon = icon,
        order = order,
        children = children,
        initiallyExpanded = true,
    )
}
