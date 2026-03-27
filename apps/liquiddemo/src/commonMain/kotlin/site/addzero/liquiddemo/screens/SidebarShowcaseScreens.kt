package site.addzero.liquiddemo.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.liquiddemo.ShowcasePage
import site.addzero.liquiddemo.SidebarShowcaseDetailConfig
import site.addzero.liquiddemo.SidebarShowcaseFactConfig
import site.addzero.liquiddemo.SidebarShowcaseInfoConfig
import site.addzero.liquiddemo.SidebarShowcaseSceneConfig
import site.addzero.liquiddemo.SidebarShowcaseSlot
import site.addzero.workbenchshell.Screen

@Serializable
private data class ShowcaseScreenConfig(
    val id: String = "",
    val pid: String? = null,
    val name: String = "",
    val sort: Int = Int.MAX_VALUE,
)

private class ShowcaseScreen(
    private val config: ShowcaseScreenConfig,
    private val iconValue: ImageVector? = null,
    private val pageContent: (@Composable () -> Unit)? = null,
) : Screen {
    override val id = config.id
    override val pid = config.pid
    override val name = config.name
    override val icon = iconValue
    override val sort = config.sort
    override val content = pageContent
}

private object ProjectIds {
    const val scene = "scene/project"
    val home = buildSidebarShowcaseScreenId(scene, "home")
    val folders = buildSidebarShowcaseScreenId(scene, "folders")
    val overview = buildSidebarShowcaseScreenId(scene, "overview")
    val specs = buildSidebarShowcaseScreenId(scene, "specs")
    val team = buildSidebarShowcaseScreenId(scene, "team")
    val assets = buildSidebarShowcaseScreenId(scene, "assets")
    val archive = buildSidebarShowcaseScreenId(scene, "archive")
    val delivery = buildSidebarShowcaseScreenId(scene, "delivery")
}

private object MusicIds {
    const val scene = "scene/music"
    val create = buildSidebarShowcaseScreenId(scene, "create")
    val library = buildSidebarShowcaseScreenId(scene, "library")
    val generate = buildSidebarShowcaseScreenId(scene, "generate")
    val lyrics = buildSidebarShowcaseScreenId(scene, "lyrics")
    val tracks = buildSidebarShowcaseScreenId(scene, "tracks")
    val voices = buildSidebarShowcaseScreenId(scene, "voices")
    val settings = buildSidebarShowcaseScreenId(scene, "settings")
}

private object AdminIds {
    const val scene = "scene/admin"
    val dashboard = buildSidebarShowcaseScreenId(scene, "dashboard")
    val catalog = buildSidebarShowcaseScreenId(scene, "catalog")
    val users = buildSidebarShowcaseScreenId(scene, "users")
    val overview = buildSidebarShowcaseScreenId(scene, "overview")
    val orders = buildSidebarShowcaseScreenId(scene, "orders")
    val inventory = buildSidebarShowcaseScreenId(scene, "inventory")
    val products = buildSidebarShowcaseScreenId(scene, "products")
    val members = buildSidebarShowcaseScreenId(scene, "members")
    val roles = buildSidebarShowcaseScreenId(scene, "roles")
    val settings = buildSidebarShowcaseScreenId(scene, "settings")
}

private object SettingsIds {
    const val scene = "scene/settings"
    val general = buildSidebarShowcaseScreenId(scene, "general")
    val ai = buildSidebarShowcaseScreenId(scene, "ai")
    val appearance = buildSidebarShowcaseScreenId(scene, "appearance")
    val shortcuts = buildSidebarShowcaseScreenId(scene, "shortcuts")
    val security = buildSidebarShowcaseScreenId(scene, "security")
    val models = buildSidebarShowcaseScreenId(scene, "models")
    val providers = buildSidebarShowcaseScreenId(scene, "providers")
}

@Single(binds = [Screen::class])
class ProjectWorkbenchSceneScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.scene,
        name = "项目工作台",
        sort = 10,
    ),
)

@Single(binds = [Screen::class])
class ProjectHomeGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.home,
        pid = ProjectIds.scene,
        name = "主页",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Home,
)

@Single(binds = [Screen::class])
class ProjectFoldersGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.folders,
        pid = ProjectIds.scene,
        name = "目录",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Folder,
)

@Single(binds = [Screen::class])
class ProjectOverviewScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.overview,
        pid = ProjectIds.home,
        name = "概览",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Home,
    pageContent = showcasePageContent(
        eyebrow = "Project Workbench",
        title = "跨角色项目总览",
        summary = "把里程碑、阻塞项和发布节奏压缩到一个高密度桌面工作区里。",
        metrics = listOf("活跃任务" to "18", "待评审 MR" to "4", "风险项" to "2"),
        highlights = listOf("需求、设计、交付三条泳道共用同一棵 Screen 树。", "侧栏分组和当前内容完全由 ScreenTree 决定。", "调用侧只管启动 Koin，不再手搓 SidebarItem。"),
        primaryActionLabel = "同步看板",
        secondaryActionLabel = "查看日报",
    ),
)

@Single(binds = [Screen::class])
class ProjectSpecsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.specs,
        pid = ProjectIds.home,
        name = "规格",
        sort = 20,
    ),
    iconValue = Icons.AutoMirrored.Rounded.ShortText,
    pageContent = showcasePageContent(
        eyebrow = "Project Specs",
        title = "文档与实现一体化视图",
        summary = "规格页强调需求拆解、实现边界和验收规则的一致性。",
        metrics = listOf("规格文档" to "12", "待确认项" to "3", "自动校验" to "92%"),
        highlights = listOf("页面内容和详情区都挂在同一个 Screen 节点上。", "可以继续接文档驱动或远程配置，但导航模型不变。", "对业务来说只需声明 Screen，不需要再包一层导航 DTO。"),
        primaryActionLabel = "打开规范",
        secondaryActionLabel = "导出清单",
    ),
)

@Single(binds = [Screen::class])
class ProjectTeamScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.team,
        pid = ProjectIds.home,
        name = "团队",
        sort = 30,
    ),
    iconValue = Icons.Rounded.People,
    pageContent = showcasePageContent(
        eyebrow = "Project Team",
        title = "团队协作密度面板",
        summary = "把负责人、设计评审、研发排期和交付观察点收在同一屏里。",
        metrics = listOf("参与角色" to "7", "待确认 owner" to "1", "共享看板" to "3"),
        highlights = listOf("适合演示树形导航切换不同工作区。", "右侧 detail 区保持固定语义，不需要页面各自造壳。", "纯函数式 sidebar 让 Screen 本体直接进入渲染层。"),
        primaryActionLabel = "查看排班",
        secondaryActionLabel = "同步日历",
    ),
)

@Single(binds = [Screen::class])
class ProjectAssetsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.assets,
        pid = ProjectIds.folders,
        name = "素材",
        sort = 10,
    ),
    iconValue = Icons.Rounded.CollectionsBookmark,
    pageContent = showcasePageContent(
        eyebrow = "Project Assets",
        title = "资产与交付物总库",
        summary = "适合展示文件、设计稿、合同附件这种层级明显的目录。",
        metrics = listOf("素材包" to "42", "待替换" to "5", "最新版本" to "v18"),
        highlights = listOf("叶子节点渲染内容，父节点只做容器。", "选中态和展开态都交给通用树状态管理。", "搜索命中时树会自动展开到对应叶子。"),
        primaryActionLabel = "上传素材",
        secondaryActionLabel = "对比版本",
    ),
)

@Single(binds = [Screen::class])
class ProjectArchiveScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.archive,
        pid = ProjectIds.folders,
        name = "归档",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Storage,
    pageContent = showcasePageContent(
        eyebrow = "Project Archive",
        title = "归档与回滚索引",
        summary = "把已交付版本、冻结包和回滚包聚合成可检索的桌面视图。",
        metrics = listOf("归档批次" to "16", "可回滚版本" to "3", "冷存储" to "2.4 TB"),
        highlights = listOf("Screen 结构天然适合版本库、目录库、配置库。", "不需要再额外设计一套导航实体。", "Koin 收集全量 Screen 后再统一做层级校验。"),
        primaryActionLabel = "生成归档",
        secondaryActionLabel = "检查回滚",
    ),
)

@Single(binds = [Screen::class])
class ProjectDeliveryScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = ProjectIds.delivery,
        pid = ProjectIds.folders,
        name = "交付",
        sort = 30,
    ),
    iconValue = Icons.Rounded.Build,
    pageContent = showcasePageContent(
        eyebrow = "Project Delivery",
        title = "交付编排与发布窗口",
        summary = "适合演示工作台壳层里的主操作区、正文区和详情区联动。",
        metrics = listOf("待发布" to "2", "灰度窗口" to "今晚 20:00", "阻塞工单" to "1"),
        highlights = listOf("Workbench 壳层和 Admin 壳层都能复用同一棵 Screen 树。", "区别只在外层渲染器，不在页面声明模型。", "这就是把复杂参数收回组件内部后的调用面。"),
        primaryActionLabel = "开始交付",
        secondaryActionLabel = "查看风险",
    ),
)

@Single(binds = [Screen::class])
class MusicStudioSceneScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.scene,
        name = "音乐工作台",
        sort = 20,
    ),
)

@Single(binds = [Screen::class])
class MusicCreateGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.create,
        pid = MusicIds.scene,
        name = "创作",
        sort = 10,
    ),
    iconValue = Icons.Rounded.AutoAwesome,
)

@Single(binds = [Screen::class])
class MusicLibraryGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.library,
        pid = MusicIds.scene,
        name = "曲库",
        sort = 20,
    ),
    iconValue = Icons.Rounded.LibraryMusic,
)

@Single(binds = [Screen::class])
class MusicGenerateScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.generate,
        pid = MusicIds.create,
        name = "生成",
        sort = 10,
    ),
    iconValue = Icons.Rounded.AutoAwesome,
    pageContent = showcasePageContent(
        eyebrow = "Music Studio",
        title = "歌词、旋律与风格提示词面板",
        summary = "用同一个工作台骨架承接创作流、产物流和配置流。",
        metrics = listOf("并发任务" to "6", "风格模版" to "24", "命中率" to "88%"),
        highlights = listOf("Screen 既是导航模型，也是页面内容入口。", "Koin 收集到 Screen 列表后，侧栏自动拿到完整树。", "业务不再写一层 `Screen -> SidebarItem` 适配器。"),
        primaryActionLabel = "开始生成",
        secondaryActionLabel = "复用模版",
    ),
)

@Single(binds = [Screen::class])
class MusicLyricsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.lyrics,
        pid = MusicIds.create,
        name = "歌词",
        sort = 20,
    ),
    iconValue = Icons.AutoMirrored.Rounded.ShortText,
    pageContent = showcasePageContent(
        eyebrow = "Music Lyrics",
        title = "结构化歌词工作区",
        summary = "适合放 Verse / Hook / Bridge 分段和版本草稿比较。",
        metrics = listOf("草稿版本" to "9", "待润色段落" to "3", "品牌词校验" to "通过"),
        highlights = listOf("工作台内容和详情区都是组件内建，不需要 App 再包一层壳。", "叶子页只声明自己的内容。", "额外行为则全部由 Slot SPI 拼装。"),
        primaryActionLabel = "续写段落",
        secondaryActionLabel = "打开版本",
    ),
)

@Single(binds = [Screen::class])
class MusicTracksScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.tracks,
        pid = MusicIds.library,
        name = "曲目",
        sort = 10,
    ),
    iconValue = Icons.AutoMirrored.Rounded.PlaylistPlay,
    pageContent = showcasePageContent(
        eyebrow = "Music Tracks",
        title = "曲目库与状态筛选",
        summary = "适合演示树形侧栏在中后台和创作工具之间的通用性。",
        metrics = listOf("已生成" to "128", "收藏" to "31", "待混音" to "7"),
        highlights = listOf("父节点只展开收起，叶子节点才进入真实内容。", "通用组件已经把这些交互统一内建。", "调用方只剩 `Screen` 定义和 `@Single` 注入。"),
        primaryActionLabel = "刷新曲库",
        secondaryActionLabel = "创建播放单",
    ),
)

@Single(binds = [Screen::class])
class MusicVoicesScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.voices,
        pid = MusicIds.library,
        name = "人声",
        sort = 20,
    ),
    iconValue = Icons.Rounded.MusicNote,
    pageContent = showcasePageContent(
        eyebrow = "Music Voices",
        title = "人声库与试唱预设",
        summary = "把音色、语速、咬字和情绪这类信息放进 detail 区特别直观。",
        metrics = listOf("音色预设" to "26", "可商用" to "12", "试唱队列" to "5"),
        highlights = listOf("detail 不是页面自己重造，而是 Slot 在壳层里统一接管。", "这比把样式和容器层层暴露给调用方更稳。", "也更符合你提的 compose 规范。"),
        primaryActionLabel = "开始试唱",
        secondaryActionLabel = "对比音色",
    ),
)

@Single(binds = [Screen::class])
class MusicSettingsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = MusicIds.settings,
        pid = MusicIds.library,
        name = "设置",
        sort = 30,
    ),
    iconValue = Icons.Rounded.Settings,
    pageContent = showcasePageContent(
        eyebrow = "Music Settings",
        title = "创作侧配置总览",
        summary = "展示 provider、额度和默认参数这类配置页也能走同一套 Screen 模型。",
        metrics = listOf("Providers" to "4", "默认模板" to "8", "预算警戒" to "80%"),
        highlights = listOf("一套规范统一 sidebar/tree/workbench。", "以后项目里再遇到树菜单，直接套泛型组件。", "不要再重新声明实体类包装 compose 参数。"),
        primaryActionLabel = "保存配置",
        secondaryActionLabel = "重置默认",
    ),
)

@Single(binds = [Screen::class])
class AdminBackendSceneScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.scene,
        name = "后台管理",
        sort = 30,
    ),
)

@Single(binds = [Screen::class])
class AdminDashboardGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.dashboard,
        pid = AdminIds.scene,
        name = "仪表盘",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Home,
)

@Single(binds = [Screen::class])
class AdminCatalogGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.catalog,
        pid = AdminIds.scene,
        name = "目录",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Inventory2,
)

@Single(binds = [Screen::class])
class AdminUsersGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.users,
        pid = AdminIds.scene,
        name = "成员",
        sort = 30,
    ),
    iconValue = Icons.Rounded.People,
)

@Single(binds = [Screen::class])
class AdminOverviewScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.overview,
        pid = AdminIds.dashboard,
        name = "总览",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Home,
    pageContent = showcasePageContent(
        eyebrow = "Admin Overview",
        title = "后台运营总览",
        summary = "管理壳层内建页面标题、动作区和全局工具位，调用面比手写参数短很多。",
        metrics = listOf("今日请求" to "84k", "异常工单" to "3", "告警规则" to "18"),
        highlights = listOf("App 侧不用再 `if else` 区分壳层，只传当前 scene 的 shell。", "对应 renderer 由 Koin 自动收集。", "这正是你前面指出的问题点。"),
        primaryActionLabel = "导出日报",
        secondaryActionLabel = "查看告警",
    ),
)

@Single(binds = [Screen::class])
class AdminOrdersScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.orders,
        pid = AdminIds.dashboard,
        name = "订单",
        sort = 20,
    ),
    iconValue = Icons.Rounded.ShoppingCart,
    pageContent = showcasePageContent(
        eyebrow = "Admin Orders",
        title = "订单与资金流水",
        summary = "后台场景同样不需要额外导航 DTO，直接还是 Screen。",
        metrics = listOf("待处理退款" to "6", "风控命中" to "2", "支付成功率" to "98.7%"),
        highlights = listOf("导航层和页面层没有额外包装层。", "Koin 聚合出来的 ScreenList 就是源数据。", "这样后续插件化页面也能自然扩展。"),
        primaryActionLabel = "批量审核",
        secondaryActionLabel = "查看流水",
    ),
)

@Single(binds = [Screen::class])
class AdminInventoryScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.inventory,
        pid = AdminIds.catalog,
        name = "库存",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Inventory2,
    pageContent = showcasePageContent(
        eyebrow = "Admin Inventory",
        title = "库存与批次状态",
        summary = "目录类树节点非常适合用纯函数式 sidebar 递归渲染。",
        metrics = listOf("SKU" to "1,248", "缺货预警" to "9", "在途批次" to "14"),
        highlights = listOf("children lambda 让组件直接递归真实业务节点。", "没有任何 `SidebarItem(children = ...)` 中间包装。", "这能明显减少 compose 参数重复包装。"),
        primaryActionLabel = "补货计划",
        secondaryActionLabel = "查看批次",
    ),
)

@Single(binds = [Screen::class])
class AdminProductsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.products,
        pid = AdminIds.catalog,
        name = "商品",
        sort = 20,
    ),
    iconValue = Icons.Rounded.CollectionsBookmark,
    pageContent = showcasePageContent(
        eyebrow = "Admin Products",
        title = "商品目录与上架策略",
        summary = "适合演示列表、筛选器和详情面板的稳定布局。",
        metrics = listOf("上架中" to "312", "待审核" to "17", "限时活动" to "8"),
        highlights = listOf("如果后面要插件化扩展页面，只需要新增 `@Single Screen`。", "根应用完全不需要维护硬编码菜单列表。", "这也是 `getAll<Screen>()` 的价值。"),
        primaryActionLabel = "创建商品",
        secondaryActionLabel = "批量导入",
    ),
)

@Single(binds = [Screen::class])
class AdminMembersScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.members,
        pid = AdminIds.users,
        name = "成员",
        sort = 10,
    ),
    iconValue = Icons.Rounded.People,
    pageContent = showcasePageContent(
        eyebrow = "Admin Members",
        title = "成员与租户关系",
        summary = "后台人资、权限和审计也可以继续复用同一个树形壳层。",
        metrics = listOf("活跃成员" to "184", "待邀请" to "11", "冻结账号" to "2"),
        highlights = listOf("Screen 作为统一页面契约，比散落的菜单配置更稳。", "尤其适合多模块、多插件的项目。", "后面接 RBAC 场景也顺手。"),
        primaryActionLabel = "邀请成员",
        secondaryActionLabel = "导出清单",
    ),
)

@Single(binds = [Screen::class])
class AdminRolesScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.roles,
        pid = AdminIds.users,
        name = "角色",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Security,
    pageContent = showcasePageContent(
        eyebrow = "Admin Roles",
        title = "角色与授权矩阵",
        summary = "非常适合展示层级菜单切换不同授权分区。",
        metrics = listOf("角色数" to "26", "自定义角色" to "8", "待审批变更" to "2"),
        highlights = listOf("你要求的 Koin 注入集合页，在这里就很自然。", "新增一类角色页，只需新加 Screen。", "侧栏自动出现，无需手工注册菜单。"),
        primaryActionLabel = "新建角色",
        secondaryActionLabel = "比较权限",
    ),
)

@Single(binds = [Screen::class])
class AdminSettingsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = AdminIds.settings,
        pid = AdminIds.users,
        name = "设置",
        sort = 30,
    ),
    iconValue = Icons.Rounded.Settings,
    pageContent = showcasePageContent(
        eyebrow = "Admin Settings",
        title = "后台策略与系统阈值",
        summary = "后台模式下头部工具和正文布局都由壳层接管，不再散落在 App.kt。",
        metrics = listOf("全局策略" to "14", "审计保留" to "365d", "切换延迟" to "0 ms"),
        highlights = listOf("把壳内置到组件内部后，调用面就变薄了。", "这也减少了重复 Surface / Box / 背景包装。", "和你前面指出的痛点是对齐的。"),
        primaryActionLabel = "保存策略",
        secondaryActionLabel = "导出配置",
    ),
)

@Single(binds = [Screen::class])
class SettingsConsoleSceneScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.scene,
        name = "设置控制台",
        sort = 40,
    ),
)

@Single(binds = [Screen::class])
class SettingsGeneralGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.general,
        pid = SettingsIds.scene,
        name = "通用",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Settings,
)

@Single(binds = [Screen::class])
class SettingsAiGroupScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.ai,
        pid = SettingsIds.scene,
        name = "AI",
        sort = 20,
    ),
    iconValue = Icons.Rounded.AutoAwesome,
)

@Single(binds = [Screen::class])
class SettingsAppearanceScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.appearance,
        pid = SettingsIds.general,
        name = "外观",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Palette,
    pageContent = showcasePageContent(
        eyebrow = "Settings Appearance",
        title = "桌面工作台外观策略",
        summary = "强调紧凑桌面风、专业冷色调和组件壳层内聚。",
        metrics = listOf("主题变量" to "42", "密度档位" to "3", "品牌色集" to "2"),
        highlights = listOf("视觉壳层放组件内部，不要求 App 手工包外壳。", "这是当前 compose 规范的一部分。", "也和你提的 ‘不要把调用方代码写厚’ 一致。"),
        primaryActionLabel = "保存主题",
        secondaryActionLabel = "预览密度",
    ),
)

@Single(binds = [Screen::class])
class SettingsShortcutsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.shortcuts,
        pid = SettingsIds.general,
        name = "快捷键",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Terminal,
    pageContent = showcasePageContent(
        eyebrow = "Settings Shortcuts",
        title = "命令与快捷键编排",
        summary = "把高频操作做成桌面工作台默认能力，而不是堆大按钮。",
        metrics = listOf("命令数" to "36", "冲突" to "1", "已同步设备" to "4"),
        highlights = listOf("紧凑桌面风格优先工具条、下拉和快捷入口。", "这也是 skill 里已经补过的规范。", "页面声明还是统一回到 Screen。"),
        primaryActionLabel = "保存映射",
        secondaryActionLabel = "导入方案",
    ),
)

@Single(binds = [Screen::class])
class SettingsSecurityScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.security,
        pid = SettingsIds.general,
        name = "安全",
        sort = 30,
    ),
    iconValue = Icons.Rounded.Security,
    pageContent = showcasePageContent(
        eyebrow = "Settings Security",
        title = "安全策略与本地权限",
        summary = "演示设置页也可以继续享受通用工作台壳层和 detail 区。",
        metrics = listOf("受保护资源" to "12", "最近审计" to "0 风险", "策略版本" to "v7"),
        highlights = listOf("Screen 定义是页面唯一来源。", "不是单独维护一个导航数组，再维护一个页面注册表。", "这样扩展成本最低。"),
        primaryActionLabel = "更新策略",
        secondaryActionLabel = "查看审计",
    ),
)

@Single(binds = [Screen::class])
class SettingsModelsScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.models,
        pid = SettingsIds.ai,
        name = "模型",
        sort = 10,
    ),
    iconValue = Icons.Rounded.Tune,
    pageContent = showcasePageContent(
        eyebrow = "Settings Models",
        title = "模型路由与默认策略",
        summary = "适合展示 provider、路由、预算和失败回退的组合配置。",
        metrics = listOf("模型" to "9", "回退链" to "3", "预算阈值" to "85%"),
        highlights = listOf("这类配置树也不要再先包一层 Menu DTO。", "直接 `Screen + @Single` 就够了。", "后续模块化后依旧能被 getAll<Screen>() 收集。"),
        primaryActionLabel = "保存路由",
        secondaryActionLabel = "查看成本",
    ),
)

@Single(binds = [Screen::class])
class SettingsProvidersScreen : Screen by ShowcaseScreen(
    config = ShowcaseScreenConfig(
        id = SettingsIds.providers,
        pid = SettingsIds.ai,
        name = "提供商",
        sort = 20,
    ),
    iconValue = Icons.Rounded.Storage,
    pageContent = showcasePageContent(
        eyebrow = "Settings Providers",
        title = "Provider 管理与健康探针",
        summary = "这里强调的是插件化 provider 页面接入时，导航层零改动。",
        metrics = listOf("Provider" to "6", "健康检查" to "5 / 6", "备用通道" to "2"),
        highlights = listOf("新增 provider 模块时，只要贡献新的 Screen。", "根应用不维护硬编码集合。", "这也是你要求用 Koin 注入页面集合的理由。"),
        primaryActionLabel = "刷新探针",
        secondaryActionLabel = "编辑凭据",
    ),
)

@Single(binds = [SidebarShowcaseSlot::class])
class ProjectShowcaseSlot : SidebarShowcaseSlot {
    override val config = SidebarShowcaseSceneConfig(
        sceneId = ProjectIds.scene,
        subtitle = "多级导航、目录分区和右侧详情区都直接来自 Screen 树。",
        shell = AppSidebarScaffoldShell.Workbench,
        initialLeafId = ProjectIds.overview,
        headerInfo = SidebarShowcaseInfoConfig(
            title = "活跃空间",
            value = "Design Ops / Delivery",
        ),
        footerInfo = SidebarShowcaseInfoConfig(
            title = "节奏",
            value = "Draft -> Review -> Ship",
        ),
        pagePrimaryActionLabel = "同步看板",
        pageSecondaryActionLabel = "查看日报",
    )

    override val details = mapOf(
        ProjectIds.overview to showcaseDetail(
            title = "概览面板",
            summary = "适合放负责人、节奏和交付阈值。",
            facts = listOf("负责人" to "Alice", "迭代" to "Sprint 24", "冻结时间" to "周五 18:00"),
            tasks = listOf("刷新聚合状态", "导出汇总面板", "跳转当前阻塞"),
        ),
        ProjectIds.specs to showcaseDetail(
            title = "规格清单",
            summary = "适合塞状态、文档路径和最后修改人。",
            facts = listOf("主文档" to "docs/specs/sidebar.md", "评审状态" to "Approved", "修改人" to "Mia"),
            tasks = listOf("查看设计差异", "打开验收项", "复制规格链接"),
        ),
        ProjectIds.team to showcaseDetail(
            title = "团队状态",
            summary = "用来放联系人、值班和协作 SLA。",
            facts = listOf("当日值班" to "Nina", "设计联络" to "Ray", "SLA" to "< 2h"),
            tasks = listOf("发起 standup", "同步责任人", "打开名册"),
        ),
        ProjectIds.assets to showcaseDetail(
            title = "素材信息",
            summary = "给文件类页面一个稳定的详情容器。",
            facts = listOf("存储桶" to "assets-eu-1", "最近同步" to "8 分钟前", "命名规范" to "已通过"),
            tasks = listOf("生成预览图", "打开 CDN 链接", "复制目录结构"),
        ),
        ProjectIds.archive to showcaseDetail(
            title = "归档侧栏",
            summary = "这里适合放版本指纹和冷存储策略。",
            facts = listOf("保留策略" to "180 天", "最近封板" to "2026-03-24", "恢复耗时" to "11 min"),
            tasks = listOf("校验签名", "导出 manifest", "同步冷备"),
        ),
        ProjectIds.delivery to showcaseDetail(
            title = "交付检查单",
            summary = "适合塞 SOP、灰度范围和回退条件。",
            facts = listOf("责任人" to "Leo", "灰度比例" to "10%", "回退阈值" to "错误率 > 2%"),
            tasks = listOf("锁定变更", "广播窗口", "更新状态页"),
        ),
    )
}

@Single(binds = [SidebarShowcaseSlot::class])
class MusicShowcaseSlot : SidebarShowcaseSlot {
    override val config = SidebarShowcaseSceneConfig(
        sceneId = MusicIds.scene,
        subtitle = "同一套纯函数式侧栏也能直接喂音乐创作树，不需要模型转换。",
        shell = AppSidebarScaffoldShell.Workbench,
        initialLeafId = MusicIds.generate,
        headerInfo = SidebarShowcaseInfoConfig(
            title = "当前模式",
            value = "Creative Batch",
        ),
        footerInfo = SidebarShowcaseInfoConfig(
            title = "本周产能",
            value = "14 首草稿",
        ),
        pagePrimaryActionLabel = "开始生成",
        pageSecondaryActionLabel = "打开曲库",
    )

    override val details = mapOf(
        MusicIds.generate to showcaseDetail(
            title = "生成参数",
            summary = "可以放模型、节拍和成本预算。",
            facts = listOf("模型" to "Suno / v4", "目标节拍" to "118 BPM", "预算" to "12 credits"),
            tasks = listOf("复用上次配置", "查看失败任务", "打开 prompt 库"),
        ),
        MusicIds.lyrics to showcaseDetail(
            title = "歌词元数据",
            summary = "用来放结构、风格词和违禁检查。",
            facts = listOf("语言" to "ZH-CN", "结构" to "V / H / V / H", "审核" to "Clean"),
            tasks = listOf("同步旋律草稿", "复制 hook", "打开 rhymes"),
        ),
        MusicIds.tracks to showcaseDetail(
            title = "曲目信息",
            summary = "适合放 BPM、调式和版权状态。",
            facts = listOf("调式" to "A Minor", "时长" to "03:12", "版权" to "Internal"),
            tasks = listOf("打开波形", "复制分享链接", "生成 stem"),
        ),
        MusicIds.voices to showcaseDetail(
            title = "音色参数",
            summary = "适合放音域、口音和授权类型。",
            facts = listOf("音域" to "Mezzo", "口音" to "Neutral", "授权" to "Project Only"),
            tasks = listOf("试听样本", "应用到当前曲目", "导出人声卡"),
        ),
        MusicIds.settings to showcaseDetail(
            title = "创作配置",
            summary = "适合放限额、路由策略和调试开关。",
            facts = listOf("默认 provider" to "Suno", "额度提醒" to "On", "Debug" to "Off"),
            tasks = listOf("切换模型", "同步额度", "导出配置"),
        ),
    )
}

@Single(binds = [SidebarShowcaseSlot::class])
class AdminShowcaseSlot : SidebarShowcaseSlot {
    override val config = SidebarShowcaseSceneConfig(
        sceneId = AdminIds.scene,
        subtitle = "同一棵 Screen 树换成 Admin 壳层后，页头工具和后台动作自动接管。",
        shell = AppSidebarScaffoldShell.AdminWorkbench,
        initialLeafId = AdminIds.overview,
        headerInfo = SidebarShowcaseInfoConfig(
            title = "租户",
            value = "addzero-demo",
        ),
        footerInfo = SidebarShowcaseInfoConfig(
            title = "权限模式",
            value = "RBAC + 审计",
        ),
        pagePrimaryActionLabel = "发布变更",
        pageSecondaryActionLabel = "导出报表",
        notificationCount = 6,
    )

    override val details = mapOf(
        AdminIds.overview to showcaseDetail(
            title = "运营详情",
            summary = "适合放 SLA、租户和事件窗口。",
            facts = listOf("SLA" to "99.95%", "主要租户" to "28", "值班窗口" to "24/7"),
            tasks = listOf("打开状态页", "同步告警值班", "刷新缓存"),
        ),
        AdminIds.orders to showcaseDetail(
            title = "订单详情",
            summary = "适合挂风控、退款和人工审核入口。",
            facts = listOf("风险等级" to "Low", "异常渠道" to "0", "结算周期" to "T+1"),
            tasks = listOf("打开退款队列", "切换商户视角", "复制审计号"),
        ),
        AdminIds.inventory to showcaseDetail(
            title = "库存信息",
            summary = "适合放仓库、批次和阈值。",
            facts = listOf("主仓" to "HZ-3", "安全库存" to "15%", "周转天数" to "21"),
            tasks = listOf("生成补货单", "查看异常批次", "同步 WMS"),
        ),
        AdminIds.products to showcaseDetail(
            title = "商品元数据",
            summary = "适合放分类、渠道和审核状态。",
            facts = listOf("主分类" to "Digital", "审核流" to "2-step", "渠道数" to "6"),
            tasks = listOf("打开审核台", "查看变更记录", "导出商品目录"),
        ),
        AdminIds.members to showcaseDetail(
            title = "成员状态",
            summary = "适合挂角色、最近登录和安全状态。",
            facts = listOf("高权限账号" to "9", "MFA 覆盖" to "97%", "异常登录" to "0"),
            tasks = listOf("打开权限矩阵", "批量失效会话", "查看审计"),
        ),
        AdminIds.roles to showcaseDetail(
            title = "角色详情",
            summary = "适合放继承链、作用域和审批流。",
            facts = listOf("默认角色" to "5", "跨租户角色" to "1", "审批流" to "Enabled"),
            tasks = listOf("复制权限模板", "打开继承树", "导出授权单"),
        ),
        AdminIds.settings to showcaseDetail(
            title = "系统设置",
            summary = "适合挂默认语言、主题和审计策略。",
            facts = listOf("默认语言" to "zh-CN", "主题" to "Dark", "审计保留" to "1 年"),
            tasks = listOf("切换租户", "重建索引", "打开变更记录"),
        ),
    )
}

@Single(binds = [SidebarShowcaseSlot::class])
class SettingsShowcaseSlot : SidebarShowcaseSlot {
    override val config = SidebarShowcaseSceneConfig(
        sceneId = SettingsIds.scene,
        subtitle = "再换一套 Screen 分组结构，侧栏和壳层逻辑依旧不变。",
        shell = AppSidebarScaffoldShell.Workbench,
        initialLeafId = SettingsIds.appearance,
        headerInfo = SidebarShowcaseInfoConfig(
            title = "环境",
            value = "Desktop / CommonMain",
        ),
        footerInfo = SidebarShowcaseInfoConfig(
            title = "同步策略",
            value = "Local first",
        ),
        pagePrimaryActionLabel = "保存配置",
        pageSecondaryActionLabel = "打开文档",
    )

    override val details = mapOf(
        SettingsIds.appearance to showcaseDetail(
            title = "外观参数",
            summary = "适合挂配色、圆角和密度。",
            facts = listOf("默认配色" to "Sky / Slate", "圆角" to "26dp", "密度" to "Compact"),
            tasks = listOf("重置变量", "导出主题", "打开 token 面板"),
        ),
        SettingsIds.shortcuts to showcaseDetail(
            title = "快捷键概览",
            summary = "适合放冲突检测和同步状态。",
            facts = listOf("最近修改" to "今天", "同步状态" to "Up to date", "平台" to "Desktop"),
            tasks = listOf("打开冲突列表", "重置默认", "导出快捷键"),
        ),
        SettingsIds.security to showcaseDetail(
            title = "安全详情",
            summary = "适合挂权限级别和日志保留。",
            facts = listOf("访问级别" to "Strict", "日志保留" to "90d", "本地凭据" to "Encrypted"),
            tasks = listOf("轮换密钥", "打开审计日志", "导出策略"),
        ),
        SettingsIds.models to showcaseDetail(
            title = "模型路由",
            summary = "适合挂默认 provider 和失败回退链。",
            facts = listOf("默认模型" to "gpt-5", "回退" to "gpt-4.1 -> local", "预算模式" to "Adaptive"),
            tasks = listOf("查看令牌消耗", "切换 provider", "导出路由表"),
        ),
        SettingsIds.providers to showcaseDetail(
            title = "Provider 状态",
            summary = "适合放连通性、凭据状态和 fallback。",
            facts = listOf("健康状态" to "Healthy", "凭据" to "3 已配置", "备用链路" to "Enabled"),
            tasks = listOf("复制健康报告", "打开凭据库", "切换主通道"),
        ),
    )
}

private fun showcasePageContent(
    eyebrow: String,
    title: String,
    summary: String,
    metrics: List<Pair<String, String>>,
    highlights: List<String>,
    primaryActionLabel: String,
    secondaryActionLabel: String,
): @Composable () -> Unit = {
    ShowcasePage(
        eyebrow = eyebrow,
        title = title,
        summary = summary,
        metrics = metrics,
        highlights = highlights,
        primaryActionLabel = primaryActionLabel,
        secondaryActionLabel = secondaryActionLabel,
    )
}

private fun showcaseDetail(
    title: String,
    summary: String,
    facts: List<Pair<String, String>>,
    tasks: List<String>,
): SidebarShowcaseDetailConfig {
    return SidebarShowcaseDetailConfig(
        title = title,
        summary = summary,
        facts = facts.map { fact ->
            SidebarShowcaseFactConfig(
                label = fact.first,
                value = fact.second,
            )
        },
        tasks = tasks,
    )
}

private fun buildSidebarShowcaseScreenId(
    sceneId: String,
    vararg path: String,
): String {
    return (listOf(sceneId) + path.toList()).joinToString("/")
}
