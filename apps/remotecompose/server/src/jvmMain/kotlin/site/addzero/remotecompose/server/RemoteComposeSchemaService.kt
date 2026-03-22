package site.addzero.remotecompose.server

import site.addzero.remotecompose.shared.RemoteComposeAction
import site.addzero.remotecompose.shared.RemoteComposeActionButton
import site.addzero.remotecompose.shared.RemoteComposeActionRowNode
import site.addzero.remotecompose.shared.RemoteComposeBulletListNode
import site.addzero.remotecompose.shared.RemoteComposeButtonStyle
import site.addzero.remotecompose.shared.RemoteComposeCardNode
import site.addzero.remotecompose.shared.RemoteComposeColumnNode
import site.addzero.remotecompose.shared.RemoteComposeLocale
import site.addzero.remotecompose.shared.RemoteComposeRowNode
import site.addzero.remotecompose.shared.RemoteComposeScreenPayload
import site.addzero.remotecompose.shared.RemoteComposeScreenSummary
import site.addzero.remotecompose.shared.RemoteComposeStatItem
import site.addzero.remotecompose.shared.RemoteComposeStatsNode
import site.addzero.remotecompose.shared.RemoteComposeTextNode
import site.addzero.remotecompose.shared.RemoteComposeTextStyle
import site.addzero.remotecompose.shared.RemoteComposeTone

class RemoteComposeSchemaService {
    fun listScreens(locale: RemoteComposeLocale): List<RemoteComposeScreenSummary> {
        return listOf(
            RemoteComposeScreenSummary(
                id = "launchpad",
                title = text(locale, "远程工作台总览", "Remote Workbench Overview"),
                subtitle = text(locale, "先看服务端如何决定整页结构，再看客户端如何解释执行。", "See how the server defines the page and how the client interprets it."),
                badge = text(locale, "推荐先看", "Start here"),
                tone = RemoteComposeTone.ACCENT,
            ),
            RemoteComposeScreenSummary(
                id = "release",
                title = text(locale, "发布编排台", "Release Orchestrator"),
                subtitle = text(locale, "这个 screen 强调远程下发操作区、节奏卡片和上下文说明。", "This screen focuses on remotely delivered actions, stage cards and release context."),
                badge = text(locale, "动作密集", "Action-heavy"),
                tone = RemoteComposeTone.WARNING,
            ),
            RemoteComposeScreenSummary(
                id = "ops",
                title = text(locale, "值班告警面板", "Ops Alert Board"),
                subtitle = text(locale, "这里演示 server-driven stats、告警分组和跨 screen 跳转。", "This one demonstrates server-driven stats, grouped alerts and cross-screen navigation."),
                badge = text(locale, "跨 screen 跳转", "Cross-screen actions"),
                tone = RemoteComposeTone.INFO,
            ),
        )
    }

    fun loadScreen(
        screenId: String,
        locale: RemoteComposeLocale,
    ): RemoteComposeScreenPayload {
        return when (screenId) {
            "launchpad" -> launchpad(locale)
            "release" -> release(locale)
            "ops" -> ops(locale)
            else -> error("Unknown remote compose screen: $screenId")
        }
    }

    private fun launchpad(locale: RemoteComposeLocale): RemoteComposeScreenPayload {
        return RemoteComposeScreenPayload(
            screenId = "launchpad",
            title = text(locale, "服务端下发的工作台首页", "A server-delivered workbench home"),
            subtitle = text(locale, "整页结构、卡片、按钮动作都来自 schema；客户端只负责映射成 Compose。", "The full structure, cards and button actions come from the schema; the client only maps them into Compose."),
            serverNote = text(locale, "Launchpad schema 已从本地 server 刷新。", "Launchpad schema refreshed from the local server."),
            root = RemoteComposeColumnNode(
                id = "launchpad-root",
                children = listOf(
                    RemoteComposeCardNode(
                        id = "hero-card",
                        title = text(locale, "Remote Compose 不是传 lambda，而是传可解释结构。", "Remote Compose does not send lambdas, it sends interpretable structure."),
                        subtitle = text(locale, "这也是它能稳定跨端、可审计、可缓存的核心原因。", "That is also why it stays portable, auditable and cacheable across clients."),
                        tone = RemoteComposeTone.ACCENT,
                        actions = listOf(
                            actionRefresh(locale),
                            actionOpenRelease(locale),
                            RemoteComposeActionButton(
                                id = "hero-message",
                                label = text(locale, "为什么这样做", "Why this shape"),
                                style = RemoteComposeButtonStyle.GHOST,
                                action = RemoteComposeAction.ShowMessage(
                                    message = text(
                                        locale,
                                        "真正的 Compose lambda 不能安全地下发，所以这里用可序列化 schema 代表“渲染逻辑”。",
                                        "Real Compose lambdas cannot be safely delivered over the wire, so this demo uses a serializable schema to represent rendering logic.",
                                    ),
                                    tone = RemoteComposeTone.INFO,
                                ),
                            ),
                        ),
                        children = listOf(
                            RemoteComposeStatsNode(
                                id = "hero-stats",
                                items = listOf(
                                    stat(
                                        locale = locale,
                                        labelZh = "远程节点",
                                        labelEn = "Remote nodes",
                                        value = "17",
                                        captionZh = "当前 payload 里的可解释组件总数",
                                        captionEn = "Interpretable components in the current payload",
                                        tone = RemoteComposeTone.ACCENT,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "动作描述",
                                        labelEn = "Action descriptors",
                                        value = "6",
                                        captionZh = "全部来自服务端 schema",
                                        captionEn = "All delivered by the server schema",
                                        tone = RemoteComposeTone.SUCCESS,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "客户端职责",
                                        labelEn = "Client job",
                                        value = text(locale, "解释执行", "Interpret"),
                                        captionZh = "把 schema 映射成 Compose",
                                        captionEn = "Map schema into Compose",
                                        tone = RemoteComposeTone.INFO,
                                    ),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeRowNode(
                        id = "launchpad-row",
                        children = listOf(
                            RemoteComposeCardNode(
                                id = "launchpad-structure",
                                title = text(locale, "服务端决定什么", "What the server decides"),
                                subtitle = text(locale, "这一侧完全由远端 schema 控制。", "This side is entirely defined by the remote schema."),
                                tone = RemoteComposeTone.INFO,
                                children = listOf(
                                    RemoteComposeBulletListNode(
                                        id = "launchpad-structure-list",
                                        items = listOf(
                                            text(locale, "当前 screen 的标题、副标题、节奏顺序", "The current screen title, subtitle and section order"),
                                            text(locale, "每张卡片里放哪些统计项、文案、按钮", "Which stats, copy and buttons appear in each card"),
                                            text(locale, "按钮点击后是刷新、跳转还是展示消息", "Whether a button refreshes, navigates or shows a message"),
                                        ),
                                    ),
                                ),
                            ),
                            RemoteComposeCardNode(
                                id = "launchpad-client",
                                title = text(locale, "客户端负责什么", "What the client does"),
                                subtitle = text(locale, "Compose 端专注解释、排版、状态和交互反馈。", "The Compose side focuses on interpretation, layout, state and interaction feedback."),
                                tone = RemoteComposeTone.SUCCESS,
                                children = listOf(
                                    RemoteComposeBulletListNode(
                                        id = "launchpad-client-list",
                                        items = listOf(
                                            text(locale, "把 JSON schema 映射成 Compose 组件树", "Map JSON schema into a Compose component tree"),
                                            text(locale, "保留主题、动效、密度和桌面端交互能力", "Keep themes, motion, density and desktop-friendly interaction"),
                                            text(locale, "在右侧 inspector 里展示原始 payload 以便核对", "Show the raw payload in the inspector for verification"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeActionRowNode(
                        id = "launchpad-actions",
                        buttons = listOf(
                            actionOpenOps(locale),
                            RemoteComposeActionButton(
                                id = "launchpad-hint",
                                label = text(locale, "本地状态提示", "Show local hint"),
                                style = RemoteComposeButtonStyle.SECONDARY,
                                action = RemoteComposeAction.ShowMessage(
                                    message = text(
                                        locale,
                                        "客户端也可以叠加本地状态，比如缓存、离线回退和编辑态。",
                                        "The client can still layer local state on top, such as caching, offline fallback and edit mode.",
                                    ),
                                    tone = RemoteComposeTone.SUCCESS,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun release(locale: RemoteComposeLocale): RemoteComposeScreenPayload {
        return RemoteComposeScreenPayload(
            screenId = "release",
            title = text(locale, "发布编排台", "Release Orchestrator"),
            subtitle = text(locale, "这个 screen 刻意把动作、上下文、阶段卡片都交给服务端来排。", "This screen intentionally lets the server arrange actions, context and stage cards."),
            serverNote = text(locale, "Release screen schema 已更新。", "Release screen schema refreshed."),
            root = RemoteComposeColumnNode(
                id = "release-root",
                children = listOf(
                    RemoteComposeCardNode(
                        id = "release-brief",
                        title = text(locale, "今天的发布窗口", "Today's release window"),
                        subtitle = text(locale, "18:30 冻结主干，19:00 灰度，19:25 全量。", "18:30 trunk freeze, 19:00 canary, 19:25 full rollout."),
                        tone = RemoteComposeTone.WARNING,
                        actions = listOf(
                            actionRefresh(locale),
                            RemoteComposeActionButton(
                                id = "release-open-launchpad",
                                label = text(locale, "回总览", "Back to overview"),
                                style = RemoteComposeButtonStyle.GHOST,
                                action = RemoteComposeAction.OpenScreen("launchpad"),
                            ),
                        ),
                        children = listOf(
                            RemoteComposeStatsNode(
                                id = "release-stats",
                                items = listOf(
                                    stat(
                                        locale = locale,
                                        labelZh = "待发布包",
                                        labelEn = "Artifacts",
                                        value = "12",
                                        captionZh = "包含 3 个 schema 版本切换",
                                        captionEn = "Includes 3 schema-version swaps",
                                        tone = RemoteComposeTone.WARNING,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "检查项",
                                        labelEn = "Checks",
                                        value = "28/31",
                                        captionZh = "剩 3 项人工确认",
                                        captionEn = "3 manual checks remaining",
                                        tone = RemoteComposeTone.SUCCESS,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "回滚预案",
                                        labelEn = "Rollback plan",
                                        value = text(locale, "已就绪", "Ready"),
                                        captionZh = "回滚步骤也可由 schema 驱动下发",
                                        captionEn = "Rollback steps can also be delivered by schema",
                                        tone = RemoteComposeTone.INFO,
                                    ),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeRowNode(
                        id = "release-stages",
                        children = listOf(
                            stageCard(
                                locale = locale,
                                id = "freeze",
                                titleZh = "阶段 1: 冻结主干",
                                titleEn = "Stage 1: Freeze trunk",
                                subtitleZh = "确认远程 schema 与客户端解释器版本兼容。",
                                subtitleEn = "Verify remote schema and client interpreter compatibility.",
                                tone = RemoteComposeTone.INFO,
                                bullets = listOf(
                                    text(locale, "锁定 schema version `remote-compose-demo/v1`", "Lock schema version `remote-compose-demo/v1`"),
                                    text(locale, "检查旧客户端是否能优雅忽略未知字段", "Ensure older clients gracefully ignore unknown fields"),
                                ),
                            ),
                            stageCard(
                                locale = locale,
                                id = "canary",
                                titleZh = "阶段 2: 灰度观察",
                                titleEn = "Stage 2: Canary observe",
                                subtitleZh = "重点看布局异常、按钮行为和 inspector payload 是否一致。",
                                subtitleEn = "Watch for layout issues, action behavior and inspector payload parity.",
                                tone = RemoteComposeTone.SUCCESS,
                                bullets = listOf(
                                    text(locale, "抽样对比 payload 与渲染结果", "Compare payloads against rendered output"),
                                    text(locale, "确认点击动作没有退化成本地硬编码", "Confirm actions did not regress into local hardcoding"),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeCardNode(
                        id = "release-notes",
                        title = text(locale, "为什么这个 screen 适合 remote compose", "Why this screen fits remote compose"),
                        subtitle = text(locale, "发布台经常改节奏和文案，非常适合把结构放到服务端。", "Release dashboards change rhythm and copy frequently, which makes them a strong fit for server-driven structure."),
                        tone = RemoteComposeTone.ACCENT,
                        children = listOf(
                            RemoteComposeBulletListNode(
                                id = "release-notes-list",
                                items = listOf(
                                    text(locale, "运营或平台团队能只改服务端 schema 就调整工作台结构", "Ops or platform teams can update structure by only changing the server schema"),
                                    text(locale, "客户端保留统一视觉和交互，不会因为频繁改版而散掉", "The client still keeps a unified visual and interaction system"),
                                    text(locale, "需要精细权限控制时，也可以由服务端按角色下发不同节点树", "When fine-grained permission control is needed, the server can send different trees per role"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun ops(locale: RemoteComposeLocale): RemoteComposeScreenPayload {
        return RemoteComposeScreenPayload(
            screenId = "ops",
            title = text(locale, "值班告警面板", "Ops Alert Board"),
            subtitle = text(locale, "这个 screen 侧重多分区 stats、值班动作和跨页面导航。", "This screen focuses on segmented stats, duty actions and cross-screen navigation."),
            serverNote = text(locale, "Ops screen schema 已更新。", "Ops screen schema refreshed."),
            root = RemoteComposeColumnNode(
                id = "ops-root",
                children = listOf(
                    RemoteComposeCardNode(
                        id = "ops-overview",
                        title = text(locale, "当前值班态势", "Current duty posture"),
                        subtitle = text(locale, "服务端把优先级、旁路操作和跳转路径一起编排好了。", "The server orchestrates priorities, fallback actions and navigation targets together."),
                        tone = RemoteComposeTone.DANGER,
                        actions = listOf(
                            actionOpenLaunchpad(locale),
                            RemoteComposeActionButton(
                                id = "ops-ack",
                                label = text(locale, "本地确认提示", "Local acknowledge hint"),
                                style = RemoteComposeButtonStyle.PRIMARY,
                                action = RemoteComposeAction.ShowMessage(
                                    message = text(
                                        locale,
                                        "这里故意只触发本地提示，说明动作也可以由客户端二次解释。",
                                        "This intentionally only triggers a local hint, showing that actions can also be secondarily interpreted by the client.",
                                    ),
                                    tone = RemoteComposeTone.WARNING,
                                ),
                            ),
                        ),
                        children = listOf(
                            RemoteComposeStatsNode(
                                id = "ops-stats",
                                items = listOf(
                                    stat(
                                        locale = locale,
                                        labelZh = "P1 告警",
                                        labelEn = "P1 alerts",
                                        value = "2",
                                        captionZh = "支付链路与登录回调",
                                        captionEn = "Payment path and login callback",
                                        tone = RemoteComposeTone.DANGER,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "P2 告警",
                                        labelEn = "P2 alerts",
                                        value = "5",
                                        captionZh = "其中 3 项与 schema 发布有关",
                                        captionEn = "3 of them are related to schema rollout",
                                        tone = RemoteComposeTone.WARNING,
                                    ),
                                    stat(
                                        locale = locale,
                                        labelZh = "旁路策略",
                                        labelEn = "Fallback policy",
                                        value = text(locale, "已激活", "Active"),
                                        captionZh = "客户端可按服务端指令隐藏高风险入口",
                                        captionEn = "The client can hide risky entry points based on server instructions",
                                        tone = RemoteComposeTone.INFO,
                                    ),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeRowNode(
                        id = "ops-groups",
                        children = listOf(
                            stageCard(
                                locale = locale,
                                id = "ops-schema",
                                titleZh = "Schema 相关告警",
                                titleEn = "Schema related alerts",
                                subtitleZh = "优先确认客户端是否兼容新增节点类型。",
                                subtitleEn = "First verify client compatibility with newly introduced node types.",
                                tone = RemoteComposeTone.WARNING,
                                bullets = listOf(
                                    text(locale, "检查未知 `kind` 是否已做 graceful fallback", "Check whether unknown `kind` values gracefully fall back"),
                                    text(locale, "抽样确认 inspector JSON 与服务端日志一致", "Sample verify that inspector JSON matches server logs"),
                                ),
                            ),
                            stageCard(
                                locale = locale,
                                id = "ops-runtime",
                                titleZh = "运行时告警",
                                titleEn = "Runtime alerts",
                                subtitleZh = "不是所有问题都要回滚 schema，有些只需客户端本地兜底。",
                                subtitleEn = "Not every issue requires a schema rollback; some only need local client fallback."),
                                tone = RemoteComposeTone.INFO,
                                bullets = listOf(
                                    text(locale, "列表拉取失败时保留上次成功 payload", "Keep the last successful payload when refresh fails"),
                                    text(locale, "按钮动作未知时降级成提示，不直接崩溃", "If an action is unknown, degrade to a hint instead of crashing"),
                                ),
                            ),
                        ),
                    ),
                    RemoteComposeActionRowNode(
                        id = "ops-actions",
                        buttons = listOf(
                            actionOpenRelease(locale),
                            RemoteComposeActionButton(
                                id = "ops-refresh",
                                label = text(locale, "再次拉取告警 schema", "Pull alert schema again"),
                                style = RemoteComposeButtonStyle.SECONDARY,
                                action = RemoteComposeAction.Refresh,
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun stageCard(
        locale: RemoteComposeLocale,
        id: String,
        titleZh: String,
        titleEn: String,
        subtitleZh: String,
        subtitleEn: String,
        tone: RemoteComposeTone,
        bullets: List<String>,
    ): RemoteComposeCardNode {
        return RemoteComposeCardNode(
            id = id,
            title = text(locale, titleZh, titleEn),
            subtitle = text(locale, subtitleZh, subtitleEn),
            tone = tone,
            children = listOf(
                RemoteComposeBulletListNode(
                    id = "$id-bullets",
                    items = bullets,
                ),
            ),
        )
    }

    private fun stat(
        locale: RemoteComposeLocale,
        labelZh: String,
        labelEn: String,
        value: String,
        captionZh: String,
        captionEn: String,
        tone: RemoteComposeTone,
    ): RemoteComposeStatItem {
        return RemoteComposeStatItem(
            label = text(locale, labelZh, labelEn),
            value = value,
            caption = text(locale, captionZh, captionEn),
            tone = tone,
        )
    }

    private fun actionRefresh(locale: RemoteComposeLocale): RemoteComposeActionButton {
        return RemoteComposeActionButton(
            id = "refresh",
            label = text(locale, "重新拉取", "Refresh"),
            style = RemoteComposeButtonStyle.PRIMARY,
            action = RemoteComposeAction.Refresh,
        )
    }

    private fun actionOpenRelease(locale: RemoteComposeLocale): RemoteComposeActionButton {
        return RemoteComposeActionButton(
            id = "open-release",
            label = text(locale, "打开发布台", "Open release"),
            style = RemoteComposeButtonStyle.SECONDARY,
            action = RemoteComposeAction.OpenScreen("release"),
        )
    }

    private fun actionOpenOps(locale: RemoteComposeLocale): RemoteComposeActionButton {
        return RemoteComposeActionButton(
            id = "open-ops",
            label = text(locale, "打开告警面板", "Open alert board"),
            style = RemoteComposeButtonStyle.SECONDARY,
            action = RemoteComposeAction.OpenScreen("ops"),
        )
    }

    private fun actionOpenLaunchpad(locale: RemoteComposeLocale): RemoteComposeActionButton {
        return RemoteComposeActionButton(
            id = "open-launchpad",
            label = text(locale, "回到总览", "Back to overview"),
            style = RemoteComposeButtonStyle.SECONDARY,
            action = RemoteComposeAction.OpenScreen("launchpad"),
        )
    }

    private fun text(
        locale: RemoteComposeLocale,
        zh: String,
        en: String,
    ): String {
        return if (locale == RemoteComposeLocale.ZH_CN) zh else en
    }
}
