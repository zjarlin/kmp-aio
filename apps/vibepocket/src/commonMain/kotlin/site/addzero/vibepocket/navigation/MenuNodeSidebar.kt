package site.addzero.vibepocket.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.component.glass.GlassSidebar

/**
 * MenuNodeSidebar — 基于 MenuNode 树的玻璃侧边栏
 *
 * 将 [MenuNode] 树映射到 glass-components 的泛型树形 [GlassSidebar]，
 * 实现菜单元数据驱动的侧边栏渲染。
 *
 * 功能：
 * - 树形缩进显示，缩进层级对应节点深度
 * - 虚拟父节点（[MenuNode.isVirtualParent]）点击展开/折叠
 * - 叶节点点击触发导航回调
 * - 选中状态高亮
 *
 * @param menuTree 顶层 [MenuNode] 列表（由 [MenuTreeBuilder.buildTree] 构建）
 * @param selectedRouteKey 当前选中节点的 routeKey
 * @param onLeafClick 叶节点点击回调，参数为被点击节点的 [MenuNode]
 * @param modifier 外部修饰符
 * @param title 侧边栏标题（显示在顶部）
 * @param width 侧边栏宽度，默认 220dp
 */
@Composable
fun MenuNodeSidebar(
    menuTree: List<MenuNode>,
    selectedRouteKey: String,
    onLeafClick: (MenuNode) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    width: Dp = 220.dp,
) {
    GlassSidebar(
        roots = menuTree,
        nodeKey = { it.metadata.routeKey },
        nodeTitle = { it.metadata.menuNameAlias },
        nodeIcon = { it.metadata.icon },
        nodeChildren = { it.children },
        nodeVisible = { it.metadata.visible },
        selectedKey = selectedRouteKey,
        onLeafClick = onLeafClick,
        modifier = modifier,
        title = title,
        width = width,
    )
}
