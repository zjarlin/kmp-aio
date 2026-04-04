package site.addzero.liquiddemo

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AppSidebarScaffoldShell

@Serializable
data class SidebarShowcaseSceneConfig(
    val sceneId: String = "",
    val subtitle: String = "",
    val shell: AppSidebarScaffoldShell = AppSidebarScaffoldShell.Workbench,
    val initialLeafId: String = "",
    val headerInfo: SidebarShowcaseInfoConfig = SidebarShowcaseInfoConfig(),
    val footerInfo: SidebarShowcaseInfoConfig = SidebarShowcaseInfoConfig(),
    val pagePrimaryActionLabel: String = "",
    val pageSecondaryActionLabel: String = "",
    val languageLabel: String = "简体中文",
    val userLabel: String = "demo@addzero.site",
    val notificationCount: Int = 0,
    val isDarkTheme: Boolean = true,
)

@Serializable
data class SidebarShowcaseInfoConfig(
    val title: String = "",
    val value: String = "",
)

@Serializable
data class SidebarShowcaseDetailConfig(
    val title: String = "",
    val summary: String = "",
    val facts: List<SidebarShowcaseFactConfig> = emptyList(),
    val tasks: List<String> = emptyList(),
)

@Serializable
data class SidebarShowcaseFactConfig(
    val label: String = "",
    val value: String = "",
)

data class SidebarShowcaseLeaf(
    val id: String,
    val name: String,
    val content: @Composable () -> Unit,
)

data class SidebarShowcaseSceneDefinition(
    val id: String,
    val name: String,
    val config: SidebarShowcaseSceneConfig,
    val leaves: List<SidebarShowcaseLeaf>,
    val details: Map<String, SidebarShowcaseDetailConfig> = emptyMap(),
)

@Single
class SidebarShowcaseCatalog {
    val scenes = listOf(
        createGlassScene(),
        createAdminScene(),
    )

    private val scenesById = scenes.associateBy { scene ->
        scene.id
    }
    private val leavesById = scenes
        .flatMap { scene ->
            scene.leaves.map { leaf -> leaf.id to leaf }
        }
        .toMap()
    private val sceneIdByLeafId = scenes
        .flatMap { scene ->
            scene.leaves.map { leaf -> leaf.id to scene.id }
        }
        .toMap()

    init {
        validateScenes()
    }

    val defaultSceneId = scenes.firstOrNull()?.id.orEmpty()
    val defaultLeafId = scenes.firstOrNull()?.let { scene ->
        scene.config.initialLeafId
            .takeIf(String::isNotBlank)
            ?.takeIf { leafId -> scene.leaves.any { leaf -> leaf.id == leafId } }
            ?: scene.leaves.firstOrNull()?.id
    }.orEmpty()

    fun findScene(sceneId: String): SidebarShowcaseSceneDefinition? {
        return scenesById[sceneId]
    }

    fun findLeaf(leafId: String): SidebarShowcaseLeaf? {
        return leavesById[leafId]
    }

    fun findSceneForLeaf(leafId: String): SidebarShowcaseSceneDefinition? {
        return sceneIdByLeafId[leafId]?.let(scenesById::get)
    }

    fun breadcrumbNamesFor(leafId: String): List<String> {
        val scene = findSceneForLeaf(leafId) ?: return emptyList()
        val leaf = findLeaf(leafId) ?: return listOf(scene.name)
        return listOf(scene.name, leaf.name)
    }

    private fun validateScenes() {
        require(scenes.isNotEmpty()) {
            "liquiddemo 至少需要一个场景定义。"
        }

        val duplicateSceneIds = scenes
            .groupBy { scene -> scene.id }
            .filterValues { sceneGroup -> sceneGroup.size > 1 }
            .keys
        require(duplicateSceneIds.isEmpty()) {
            "检测到重复场景 id: ${duplicateSceneIds.joinToString()}"
        }

        val duplicateLeafIds = scenes
            .flatMap { scene -> scene.leaves }
            .groupBy { leaf -> leaf.id }
            .filterValues { leafGroup -> leafGroup.size > 1 }
            .keys
        require(duplicateLeafIds.isEmpty()) {
            "检测到重复页面 id: ${duplicateLeafIds.joinToString()}"
        }

        scenes.forEach { scene ->
            require(scene.leaves.isNotEmpty()) {
                "场景 ${scene.id} 至少需要一个页面。"
            }
            require(scene.config.sceneId == scene.id) {
                "场景 ${scene.id} 的 config.sceneId 必须与场景 id 一致。"
            }
            scene.config.initialLeafId.takeIf(String::isNotBlank)?.let { leafId ->
                require(scene.leaves.any { leaf -> leaf.id == leafId }) {
                    "场景 ${scene.id} 的初始页面 $leafId 不存在。"
                }
            }
        }
    }
}

private fun createGlassScene(): SidebarShowcaseSceneDefinition {
    val overview = showcaseLeaf(
        id = "glass-overview",
        name = "玻璃总览",
        eyebrow = "Liquid Glass",
        title = "低成本展示液态玻璃壳层的关键观感",
        summary = "用统一的工作台布局，把模糊、透光、层次和内容密度一起展示出来，避免 demo 只剩孤立控件。",
        metrics = listOf(
            "玻璃层级" to "3 级",
            "主内容密度" to "紧凑",
            "详情面板" to "常驻右侧",
        ),
        highlights = listOf(
            "壳层负责背景和气氛，不把视觉责任推给页面调用点。",
            "页面信息密度靠内容编排，而不是靠超大留白硬撑。",
            "右侧概览区复用同一套事实卡片，便于对比不同页面状态。",
        ),
        primaryActionLabel = "查看壳层",
        secondaryActionLabel = "复制样式",
    )
    val navigation = showcaseLeaf(
        id = "glass-navigation",
        name = "导航密度",
        eyebrow = "Navigation",
        title = "把页面切换压缩成短路径，而不是通用树抽象",
        summary = "这个 demo 只需要场景和页面两层结构，因此直接建模为 scene + leaf，避免继续维护通用 Screen 树。",
        metrics = listOf(
            "层级数" to "2 层",
            "切换成本" to "1 次点击",
            "共享抽象" to "0 个",
        ),
        highlights = listOf(
            "场景用于切换壳层风格，页面用于切换内容。",
            "本地 sidebar 直接渲染 leaf 列表，不再依赖外部 ScreenSidebar。",
            "所有展示文案和页面入口都跟着 liquiddemo 自身走，不污染 scaffold-spi。",
        ),
        primaryActionLabel = "检查导航",
        secondaryActionLabel = "对比旧版",
    )
    val detail = showcaseLeaf(
        id = "glass-detail",
        name = "详情吸附",
        eyebrow = "Detail Panel",
        title = "右侧概览面板承担说明，不让正文反复自解释",
        summary = "页面主区只保留关键指标和当前焦点，说明性文字统一放进 detail，便于社区后续扩展更多场景。",
        metrics = listOf(
            "面板角色" to "说明区",
            "事实卡片" to "4 条",
            "操作提示" to "3 条",
        ),
        highlights = listOf(
            "概览面板可收起，方便验证主内容在不同宽度下的稳定性。",
            "面板内容跟随当前页面切换，避免把说明硬编码进工作台壳层。",
            "后续如果需要扩展，优先继续加场景数据，不再补一层接口。",
        ),
        primaryActionLabel = "查看面板",
        secondaryActionLabel = "收敛文案",
    )
    return SidebarShowcaseSceneDefinition(
        id = "liquid-glass",
        name = "玻璃工作台",
        config = SidebarShowcaseSceneConfig(
            sceneId = "liquid-glass",
            subtitle = "液态玻璃风格工作台壳层",
            shell = AppSidebarScaffoldShell.Workbench,
            initialLeafId = overview.id,
            headerInfo = SidebarShowcaseInfoConfig(
                title = "风格基线",
                value = "Glass / Dark",
            ),
            footerInfo = SidebarShowcaseInfoConfig(
                title = "组件来源",
                value = "lib/compose/liquid-glass",
            ),
            pagePrimaryActionLabel = "切到全景",
            pageSecondaryActionLabel = "复制参数",
            notificationCount = 3,
            isDarkTheme = true,
        ),
        leaves = listOf(
            overview,
            navigation,
            detail,
        ),
        details = mapOf(
            overview.id to SidebarShowcaseDetailConfig(
                title = "玻璃总览",
                summary = "这页用于确认整体视觉语言是否成立，而不是验证某一个控件。",
                facts = listOf(
                    SidebarShowcaseFactConfig("背景策略", "渐变 + 径向透光"),
                    SidebarShowcaseFactConfig("容器边界", "统一圆角 + 细描边"),
                    SidebarShowcaseFactConfig("交互重点", "导航、内容、说明三区"),
                ),
                tasks = listOf(
                    "检查壳层背景是否只在宿主里定义一次。",
                    "确认正文和概览区的视觉层级没有打架。",
                    "保证色彩和描边不会依赖页面自己补丁式覆盖。",
                ),
            ),
            navigation.id to SidebarShowcaseDetailConfig(
                title = "导航密度",
                summary = "这里重点验证两层结构是否足够表达 liquiddemo 的展示目标。",
                facts = listOf(
                    SidebarShowcaseFactConfig("场景数", "2"),
                    SidebarShowcaseFactConfig("当前页层级", "scene / leaf"),
                    SidebarShowcaseFactConfig("历史抽象", "已移除 ScreenTree"),
                ),
                tasks = listOf(
                    "只在 liquiddemo 内维护页面目录。",
                    "不要把 demo 专用结构再抽回通用模块。",
                    "如果未来要扩展多层树，再以真实需求重建模型。",
                ),
            ),
            detail.id to SidebarShowcaseDetailConfig(
                title = "详情吸附",
                summary = "概览面板展示设计说明、验证要点和下一步动作，避免正文又做一份说明书。",
                facts = listOf(
                    SidebarShowcaseFactConfig("展示位置", "右侧 detail"),
                    SidebarShowcaseFactConfig("切换方式", "跟随 leaf"),
                    SidebarShowcaseFactConfig("默认状态", "展开"),
                ),
                tasks = listOf(
                    "确认 detail 隐藏后正文仍然完整。",
                    "保持说明内容对页面和壳层都可读。",
                    "把泛化逻辑留在数据层，不再补接口层。",
                ),
            ),
        ),
    )
}

private fun createAdminScene(): SidebarShowcaseSceneDefinition {
    val dashboard = showcaseLeaf(
        id = "admin-dashboard",
        name = "后台总览",
        eyebrow = "Admin Workbench",
        title = "同一套展示数据切换到后台壳层",
        summary = "后台模式验证 breadcrumb、页头动作和浅色内容区能否与同一批页面说明协同工作。",
        metrics = listOf(
            "壳层模式" to "Admin",
            "面包屑" to "启用",
            "通知角标" to "9",
        ),
        highlights = listOf(
            "后台壳层强调信息组织和动作位，而不是玻璃氛围。",
            "相同页面模型可以驱动不同壳层，但不需要通用 Screen 接口。",
            "页头动作直接来自场景配置，避免额外 contributor 层。",
        ),
        primaryActionLabel = "查看后台页头",
        secondaryActionLabel = "检查动作位",
    )
    val approval = showcaseLeaf(
        id = "admin-approval",
        name = "审批列表",
        eyebrow = "Approval",
        title = "用紧凑列表感验证后台布局密度",
        summary = "这页聚焦后台工作台的列表型阅读节奏，确认标题、动作和概览区不会彼此争抢注意力。",
        metrics = listOf(
            "列表状态" to "待处理 18",
            "批量动作" to "已预留",
            "阅读节奏" to "短行优先",
        ),
        highlights = listOf(
            "页头按钮只保留当前场景最重要的两个动作。",
            "说明区用 facts + tasks 组合补足列表型页面的上下文。",
            "同一套内容组件在浅色壳层下仍需保持对比度。",
        ),
        primaryActionLabel = "处理审批",
        secondaryActionLabel = "导出队列",
    )
    val audit = showcaseLeaf(
        id = "admin-audit",
        name = "审计详情",
        eyebrow = "Audit",
        title = "详情页强调事实、追踪和责任边界",
        summary = "审计型页面更依赖清晰事实，而不是花哨效果，因此更适合验证 admin workbench 的稳定性。",
        metrics = listOf(
            "事实字段" to "6 组",
            "追踪跨度" to "7 天",
            "说明面板" to "聚焦风险",
        ),
        highlights = listOf(
            "breadcrumb 应该稳定，不跟着局部刷新跳动。",
            "详情区的事实卡片要能承接审计、审批、配置等多种后台场景。",
            "如果未来接真实数据，优先替换页面内容，不必更改宿主结构。",
        ),
        primaryActionLabel = "查看链路",
        secondaryActionLabel = "导出报告",
    )
    return SidebarShowcaseSceneDefinition(
        id = "admin-workbench",
        name = "后台工作台",
        config = SidebarShowcaseSceneConfig(
            sceneId = "admin-workbench",
            subtitle = "后台工作台布局与动作位",
            shell = AppSidebarScaffoldShell.AdminWorkbench,
            initialLeafId = dashboard.id,
            headerInfo = SidebarShowcaseInfoConfig(
                title = "风格基线",
                value = "Admin / Light",
            ),
            footerInfo = SidebarShowcaseInfoConfig(
                title = "目标",
                value = "验证后台壳层承载能力",
            ),
            pagePrimaryActionLabel = "打开操作台",
            pageSecondaryActionLabel = "查看审计",
            userLabel = "admin@addzero.site",
            notificationCount = 9,
            isDarkTheme = false,
        ),
        leaves = listOf(
            dashboard,
            approval,
            audit,
        ),
        details = mapOf(
            dashboard.id to SidebarShowcaseDetailConfig(
                title = "后台总览",
                summary = "这页用来确认后台壳层的标题、动作、breadcrumb 和正文三者关系。",
                facts = listOf(
                    SidebarShowcaseFactConfig("主色基调", "浅底深字"),
                    SidebarShowcaseFactConfig("主操作位", "页头右侧"),
                    SidebarShowcaseFactConfig("壳层职责", "组织布局，不承担业务"),
                ),
                tasks = listOf(
                    "检查 breadcrumb 是否和当前 leaf 一致。",
                    "确认动作位在窄宽度下仍然可读。",
                    "避免把业务说明写死进 scaffold-spi。",
                ),
            ),
            approval.id to SidebarShowcaseDetailConfig(
                title = "审批列表",
                summary = "审批场景验证后台页头动作、通知入口和详情面板的协同关系。",
                facts = listOf(
                    SidebarShowcaseFactConfig("待处理项", "18"),
                    SidebarShowcaseFactConfig("队列优先级", "高"),
                    SidebarShowcaseFactConfig("页面类型", "列表 + 操作"),
                ),
                tasks = listOf(
                    "优先观察动作按钮是否过多。",
                    "确保说明区能解释列表状态，而不是重复正文。",
                    "保留扩展到真实数据表的空间。",
                ),
            ),
            audit.id to SidebarShowcaseDetailConfig(
                title = "审计详情",
                summary = "审计页面重点不是视觉风格，而是事实密度、追踪能力和边界清晰。",
                facts = listOf(
                    SidebarShowcaseFactConfig("审计跨度", "7 天"),
                    SidebarShowcaseFactConfig("关键事实", "6 组"),
                    SidebarShowcaseFactConfig("导出方式", "页头动作"),
                ),
                tasks = listOf(
                    "确认当前页名、subtitle 和 breadcrumb 不冲突。",
                    "评估 detail 面板对高密度事实是否足够。",
                    "后续接入真实数据时继续复用场景定义。",
                ),
            ),
        ),
    )
}

private fun showcaseLeaf(
    id: String,
    name: String,
    eyebrow: String,
    title: String,
    summary: String,
    metrics: List<Pair<String, String>>,
    highlights: List<String>,
    primaryActionLabel: String,
    secondaryActionLabel: String,
): SidebarShowcaseLeaf {
    return SidebarShowcaseLeaf(
        id = id,
        name = name,
    ) {
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
}
