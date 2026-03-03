package site.addzero.vibepocket.navigation

import kotlinx.serialization.Serializable

/**
 * 菜单元数据，描述导航菜单项的数据结构。
 *
 * 使用扁平列表 + parentRouteKey 引用构建树形结构，
 * 便于序列化和未来从注解处理器 / 低代码 GUI 生成。
 */
@Serializable
data class MenuMetadata(
    /** 全限定 Composable 函数名，作为菜单项的唯一标识符 */
    val routeKey: String,
    /** 菜单显示名称 */
    val menuNameAlias: String,
    /** 图标标识（emoji 或 Material Icon 名），可选 */
    val icon: String? = null,
    /** 父节点 routeKey，null 表示顶层节点 */
    val parentRouteKey: String? = null,
    /** 是否在侧边栏中可见 */
    val visible: Boolean = true,
    /** 排序权重，升序排列 */
    val sortOrder: Int = 0
)

/**
 * 菜单树节点，由 [MenuMetadata] 构建的运行时树形结构。
 *
 * 不需要序列化——树结构在运行时由扁平 [MenuMetadata] 列表构建。
 */
data class MenuNode(
    /** 当前节点的元数据 */
    val metadata: MenuMetadata,
    /** 子节点列表，按 sortOrder 升序排列 */
    val children: List<MenuNode> = emptyList(),
    /** 是否为虚拟父节点（无对应 Composable 的分组目录节点） */
    val isVirtualParent: Boolean = false
)
