@file:OptIn(ExperimentalFluentApi::class)

package site.addzero.fluentdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import io.github.composefluent.background.Layer
import io.github.composefluent.background.Mica
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.HyperlinkButton
import io.github.composefluent.component.Icon
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarDefaults
import io.github.composefluent.component.InfoBarSeverity
import io.github.composefluent.component.ListItem
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.SubtleButton
import io.github.composefluent.component.Switcher
import io.github.composefluent.component.Text
import io.github.composefluent.component.TextField
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Alert
import io.github.composefluent.icons.regular.ArrowSync
import io.github.composefluent.icons.regular.ArrowUpRight
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.History
import io.github.composefluent.icons.regular.Home
import io.github.composefluent.icons.regular.Info
import io.github.composefluent.icons.regular.Navigation
import io.github.composefluent.icons.regular.Save
import io.github.composefluent.icons.regular.Settings
import io.github.composefluent.icons.regular.Wrench
import io.github.composefluent.surface.Card
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.plugin.module.dsl.koinConfiguration

private val FluentAccent = Color(0xFF0F6CBD)
private const val FluentVersion = "v0.1.0"
private const val ComposeFluentRepository = "https://github.com/compose-fluent/compose-fluent-ui"

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<FluentDemoKoinApplication>(),
    ) {
        val state: FluentDemoState = koinInject()
        val language = state.language
        val isZh = language == FluentDemoLanguage.ZhCn

        FluentTheme(
            colors = if (state.darkMode) {
                darkColors(accent = FluentAccent)
            } else {
                lightColors(accent = FluentAccent)
            },
        ) {
            Mica(modifier = Modifier.fillMaxSize()) {
                if (state.dialogVisible) {
                    ContentDialog(
                        title = if (isZh) {
                            "Fluent 工作台说明"
                        } else {
                            "Fluent Workbench"
                        },
                        visible = true,
                        primaryButtonText = if (isZh) {
                            "继续"
                        } else {
                            "Continue"
                        },
                        closeButtonText = if (isZh) {
                            "关闭"
                        } else {
                            "Close"
                        },
                        onButtonClick = {
                            state.closeDialog()
                        },
                        content = {
                            Text(
                                if (isZh) {
                                    "这套工程把 NavigationView、CommandBar、InfoBar、ContentDialog 和基础表单控件收成了一套 Fluent 风格桌面脚手架。"
                                } else {
                                    "This module turns NavigationView, CommandBar, InfoBar, ContentDialog, and core form controls into a Fluent desktop starter."
                                },
                            )
                        },
                    )
                }

                FluentWorkbenchScaffold(
                    config = FluentWorkbenchScaffoldConfig(
                        appTitle = if (isZh) {
                            "Fluent Starter"
                        } else {
                            "Fluent Starter"
                        },
                        pageTitle = state.selectedPage.title(language),
                        pageCaption = state.selectedPage.caption(language),
                        navigationItems = mainNavigationItems(language),
                        footerItems = footerNavigationItems(language),
                        selectedKey = state.selectedPage.name,
                        displayMode = if (state.navigationCompact) {
                            NavigationDisplayMode.LeftCompact
                        } else {
                            NavigationDisplayMode.Left
                        },
                        commandItems = workbenchCommands(state, language),
                        statusText = buildStatusText(state, language),
                    ),
                    actions = FluentWorkbenchScaffoldActions(
                        onSelectItem = { key ->
                            state.selectPage(FluentDemoPage.valueOf(key))
                        },
                    ),
                    topNotice = {
                        if (state.noticeVisible) {
                            InfoBar(
                                title = {
                                    Text(
                                        if (isZh) {
                                            "Fluent 工作台已经准备好"
                                        } else {
                                            "Fluent workbench is ready"
                                        },
                                    )
                                },
                                message = {
                                    Text(
                                        if (isZh) {
                                            "当前工程固定使用 compose-fluent-ui 组件，不再退回 Material 3 作为主界面骨架。"
                                        } else {
                                            "This module is built around compose-fluent-ui and does not fall back to Material 3 as the primary shell."
                                        },
                                    )
                                },
                                severity = InfoBarSeverity.Success,
                                modifier = Modifier.fillMaxWidth(),
                                action = {
                                    SubtleButton(onClick = state::openDialog) {
                                        Text(
                                            if (isZh) {
                                                "查看说明"
                                            } else {
                                                "Open details"
                                            },
                                        )
                                    }
                                },
                                closeAction = {
                                    InfoBarDefaults.CloseActionButton(
                                        onClick = state::dismissNotice,
                                    )
                                },
                            )
                        }
                    },
                ) {
                    when (state.selectedPage) {
                        FluentDemoPage.Overview -> OverviewPage(state, language)
                        FluentDemoPage.Forms -> FormsPage(state, language)
                        FluentDemoPage.Commands -> CommandsPage(state, language)
                        FluentDemoPage.Dialogs -> DialogsPage(state, language)
                        FluentDemoPage.Settings -> SettingsPage(state, language)
                    }
                }
            }
        }
    }
}

private fun mainNavigationItems(
    language: FluentDemoLanguage,
): List<FluentWorkbenchNavItem> =
    listOf(
        FluentWorkbenchNavItem(
            key = FluentDemoPage.Overview.name,
            title = FluentDemoPage.Overview.title(language),
            caption = FluentDemoPage.Overview.caption(language),
            icon = Icons.Default.Home,
        ),
        FluentWorkbenchNavItem(
            key = FluentDemoPage.Forms.name,
            title = FluentDemoPage.Forms.title(language),
            caption = FluentDemoPage.Forms.caption(language),
            icon = Icons.Default.Folder,
        ),
        FluentWorkbenchNavItem(
            key = FluentDemoPage.Commands.name,
            title = FluentDemoPage.Commands.title(language),
            caption = FluentDemoPage.Commands.caption(language),
            icon = Icons.Default.Wrench,
        ),
        FluentWorkbenchNavItem(
            key = FluentDemoPage.Dialogs.name,
            title = FluentDemoPage.Dialogs.title(language),
            caption = FluentDemoPage.Dialogs.caption(language),
            icon = Icons.Default.Info,
        ),
    )

private fun footerNavigationItems(
    language: FluentDemoLanguage,
): List<FluentWorkbenchNavItem> =
    listOf(
        FluentWorkbenchNavItem(
            key = FluentDemoPage.Settings.name,
            title = FluentDemoPage.Settings.title(language),
            caption = FluentDemoPage.Settings.caption(language),
            icon = Icons.Default.Settings,
        ),
    )

private fun workbenchCommands(
    state: FluentDemoState,
    language: FluentDemoLanguage,
): List<FluentWorkbenchCommand> {
    val isZh = language == FluentDemoLanguage.ZhCn
    return listOf(
        FluentWorkbenchCommand(
            label = if (isZh) "新建工作区" else "New workspace",
            icon = Icons.Default.Save,
            shortcut = "Ctrl+N",
            onClick = state::openDialog,
        ),
        FluentWorkbenchCommand(
            label = if (isZh) "刷新状态" else "Refresh",
            icon = Icons.Default.ArrowSync,
            shortcut = "F5",
            onClick = state::showNotice,
        ),
        FluentWorkbenchCommand(
            label = if (isZh) "紧凑导航" else "Compact nav",
            icon = Icons.Default.Navigation,
            shortcut = "Ctrl+\\",
            onClick = state::toggleNavigationCompact,
        ),
        FluentWorkbenchCommand(
            label = if (isZh) "主题切换" else "Theme",
            icon = Icons.Default.Settings,
            shortcut = "Ctrl+J",
            onClick = state::toggleDarkMode,
        ),
        FluentWorkbenchCommand(
            label = if (language == FluentDemoLanguage.ZhCn) "切到 English" else "Switch to 中文",
            icon = Icons.Default.ArrowUpRight,
            onClick = state::toggleLanguage,
        ),
    )
}

private fun buildStatusText(
    state: FluentDemoState,
    language: FluentDemoLanguage,
): String {
    val themeText = if (state.darkMode) {
        if (language == FluentDemoLanguage.ZhCn) "暗色" else "Dark"
    } else {
        if (language == FluentDemoLanguage.ZhCn) "亮色" else "Light"
    }
    val navText = if (state.navigationCompact) {
        if (language == FluentDemoLanguage.ZhCn) "紧凑导航" else "Compact navigation"
    } else {
        if (language == FluentDemoLanguage.ZhCn) "完整导航" else "Full navigation"
    }
    return "compose-fluent-ui $FluentVersion · ${state.selectedPage.title(language)} · $themeText · $navText · ${state.lastAction}"
}

private fun FluentDemoPage.title(
    language: FluentDemoLanguage,
): String =
    when (this) {
        FluentDemoPage.Overview -> if (language == FluentDemoLanguage.ZhCn) "总览" else "Overview"
        FluentDemoPage.Forms -> "Forms"
        FluentDemoPage.Commands -> if (language == FluentDemoLanguage.ZhCn) "命令" else "Commands"
        FluentDemoPage.Dialogs -> if (language == FluentDemoLanguage.ZhCn) "对话" else "Dialogs"
        FluentDemoPage.Settings -> if (language == FluentDemoLanguage.ZhCn) "设置" else "Settings"
    }

private fun FluentDemoPage.caption(
    language: FluentDemoLanguage,
): String =
    when (this) {
        FluentDemoPage.Overview -> if (language == FluentDemoLanguage.ZhCn) {
            "NavigationView、Mica、CommandBar 组成的 Fluent 工作台入口"
        } else {
            "A Fluent entry shell built with NavigationView, Mica, and CommandBar"
        }

        FluentDemoPage.Forms -> if (language == FluentDemoLanguage.ZhCn) {
            "文本输入、搜索和即时开关这些高频交互"
        } else {
            "High-frequency input, search, and instant toggles"
        }

        FluentDemoPage.Commands -> if (language == FluentDemoLanguage.ZhCn) {
            "按钮、列表项和命令栏的工作台动作区"
        } else {
            "Workbench actions built with buttons, list items, and command bars"
        }

        FluentDemoPage.Dialogs -> if (language == FluentDemoLanguage.ZhCn) {
            "InfoBar 与 ContentDialog 这类状态反馈"
        } else {
            "State feedback with InfoBar and ContentDialog"
        }

        FluentDemoPage.Settings -> if (language == FluentDemoLanguage.ZhCn) {
            "主题、导航密度和语言这些壳层偏好"
        } else {
            "Shell preferences such as theme, navigation density, and language"
        }
    }

@Composable
private fun OverviewPage(
    state: FluentDemoState,
    language: FluentDemoLanguage,
) {
    val isZh = language == FluentDemoLanguage.ZhCn
    FluentSectionPanel(
        title = if (isZh) "脚手架定位" else "Scaffold focus",
        caption = if (isZh) {
            "这套工程先解决桌面壳层结构，再去放具体业务页面。"
        } else {
            "This starter settles the desktop shell first, then hosts real feature pages."
        },
    ) {
        StatusLine(
            label = if (isZh) "导航壳层" else "Navigation shell",
            value = if (state.navigationCompact) {
                if (isZh) "LeftCompact" else "LeftCompact"
            } else {
                if (isZh) "Left" else "Left"
            },
        )
        StatusLine(
            label = if (isZh) "主题入口" else "Theme root",
            value = "FluentTheme + Mica",
        )
        StatusLine(
            label = if (isZh) "运行命令" else "Run command",
            value = "./gradlew :apps:fluent-demo:run",
        )
        StatusLine(
            label = if (isZh) "当前版本" else "Current version",
            value = "io.github.compose-fluent:fluent $FluentVersion",
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            onClick = state::openDialog,
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isZh) "工作台入口" else "Workbench entry",
                    style = FluentTheme.typography.bodyStrong,
                )
                Text(
                    text = if (isZh) {
                        "直接把命令栏、状态反馈、设置和内容区放进同一个壳层。"
                    } else {
                        "Keep commands, status feedback, settings, and content in one shell."
                    },
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.text.text.secondary,
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isZh) "快速动作" else "Quick actions",
                    style = FluentTheme.typography.bodyStrong,
                )
                Text(
                    text = if (isZh) {
                        "切语言、切主题、切导航密度都放在同一组 Fluent 命令里。"
                    } else {
                        "Language, theme, and navigation density stay inside the same Fluent command group."
                    },
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.text.text.secondary,
                )
            }
        }
    }

    FluentSectionPanel(
        title = if (isZh) "快速入口" else "Quick links",
        caption = if (isZh) {
            "先把最常用的操作做成一组明确动作，不要把工作台变成空白展示页。"
        } else {
            "Start with explicit actions instead of turning the shell into an empty showcase."
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentButton(onClick = state::openDialog) {
                Text(if (isZh) "打开说明" else "Open dialog")
            }
            Button(onClick = state::showNotice) {
                Text(if (isZh) "恢复提示" else "Restore banner")
            }
            HyperlinkButton(navigateUri = ComposeFluentRepository) {
                Text(if (isZh) "查看仓库" else "Open repository")
            }
        }
    }
}

@Composable
private fun FormsPage(
    state: FluentDemoState,
    language: FluentDemoLanguage,
) {
    val isZh = language == FluentDemoLanguage.ZhCn
    FluentSectionPanel(
        title = if (isZh) "基础输入" else "Form inputs",
        caption = if (isZh) {
            "Fluent TextField 适合桌面工作台里的命名、搜索和轻配置。"
        } else {
            "Fluent text fields fit naming, search, and light configuration in desktop tools."
        },
    ) {
        TextField(
            value = state.workspaceName,
            onValueChange = state::updateWorkspaceName,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            header = {
                Text(if (isZh) "工作区名称" else "Workspace name")
            },
        )
        TextField(
            value = state.searchText,
            onValueChange = state::updateSearchText,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            header = {
                Text(if (isZh) "搜索关键词" else "Search keyword")
            },
            placeholder = {
                Text(if (isZh) "输入模块、页面或命令" else "Search pages, modules, or commands")
            },
        )
    }

    FluentSectionPanel(
        title = if (isZh) "即时开关" else "Instant toggles",
        caption = if (isZh) {
            "Fluent Switcher 比复选框更适合立即生效的桌面偏好项。"
        } else {
            "Fluent switchers fit desktop preferences that apply immediately."
        },
    ) {
        Switcher(
            checked = state.notificationsEnabled,
            onCheckStateChange = state::updateNotificationsEnabled,
            text = if (isZh) "操作完成后发送提示" else "Show completion notifications",
        )
        Switcher(
            checked = state.autoSyncEnabled,
            onCheckStateChange = state::updateAutoSyncEnabled,
            text = if (isZh) "自动同步工作区索引" else "Auto-sync workspace index",
        )
    }
}

@Composable
private fun CommandsPage(
    state: FluentDemoState,
    language: FluentDemoLanguage,
) {
    val isZh = language == FluentDemoLanguage.ZhCn
    FluentSectionPanel(
        title = if (isZh) "动作按钮" else "Action buttons",
        caption = if (isZh) {
            "Accent / Default / Subtle 三层动作足够支撑大多数桌面工具场景。"
        } else {
            "Accent, default, and subtle actions cover most desktop tool flows."
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentButton(onClick = state::openDialog) {
                Text(if (isZh) "主动作" else "Primary")
            }
            Button(onClick = state::showNotice) {
                Text(if (isZh) "次动作" else "Secondary")
            }
            SubtleButton(onClick = state::toggleDarkMode) {
                Text(if (isZh) "轻动作" else "Subtle")
            }
        }
    }

    FluentSectionPanel(
        title = if (isZh) "命令列表" else "Command list",
        caption = if (isZh) {
            "列表项可以直接作为任务或命令入口，不需要再包一层视觉容器。"
        } else {
            "List items can act as direct task or command entries without extra wrappers."
        },
    ) {
        ListItem(
            onClick = state::openDialog,
            text = {
                Text(if (isZh) "保存当前布局快照" else "Save current layout snapshot")
            },
            icon = {
                Icon(Icons.Default.Save, contentDescription = null)
            },
            trailing = {
                Text("Ctrl+S")
            },
        )
        ListItem(
            onClick = state::showNotice,
            text = {
                Text(if (isZh) "重新同步页面状态" else "Refresh page state")
            },
            icon = {
                Icon(Icons.Default.ArrowSync, contentDescription = null)
            },
            trailing = {
                Text("F5")
            },
        )
        ListItem(
            onClick = {
                state.selectPage(FluentDemoPage.Settings)
            },
            text = {
                Text(if (isZh) "打开工作台设置" else "Open workbench settings")
            },
            icon = {
                Icon(Icons.Default.Settings, contentDescription = null)
            },
        )
    }
}

@Composable
private fun DialogsPage(
    state: FluentDemoState,
    language: FluentDemoLanguage,
) {
    val isZh = language == FluentDemoLanguage.ZhCn
    InfoBar(
        title = {
            Text(if (isZh) "状态反馈建议" else "State feedback guidance")
        },
        message = {
            Text(
                if (isZh) {
                    "常驻状态优先用 InfoBar，真正打断流程的确认再用 ContentDialog。"
                } else {
                    "Use InfoBar for persistent status and ContentDialog only when the flow must pause."
                },
            )
        },
        severity = InfoBarSeverity.Warning,
        modifier = Modifier.fillMaxWidth(),
        closeAction = {
            InfoBarDefaults.CloseActionButton(
                onClick = state::dismissNotice,
            )
        },
    )

    FluentSectionPanel(
        title = if (isZh) "模态动作" else "Modal actions",
        caption = if (isZh) {
            "Dialog 不应该替代页面本身，只处理明确确认、告警和不可逆动作。"
        } else {
            "Dialogs should not replace the page itself. Keep them for confirmation, warnings, and irreversible actions."
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentButton(onClick = state::openDialog) {
                Text(if (isZh) "打开对话框" else "Open dialog")
            }
            Button(onClick = state::showNotice) {
                Text(if (isZh) "恢复顶部提示" else "Restore top banner")
            }
        }
    }
}

@Composable
private fun SettingsPage(
    state: FluentDemoState,
    language: FluentDemoLanguage,
) {
    val isZh = language == FluentDemoLanguage.ZhCn
    FluentSectionPanel(
        title = if (isZh) "外观偏好" else "Shell preferences",
        caption = if (isZh) {
            "这些开关是工作台级配置，不应该散落到每个页面里各自维护。"
        } else {
            "These switches belong to the shell, not to each feature page."
        },
    ) {
        Switcher(
            checked = state.darkMode,
            onCheckStateChange = state::updateDarkMode,
            text = if (isZh) "启用暗色模式" else "Enable dark mode",
        )
        Switcher(
            checked = state.navigationCompact,
            onCheckStateChange = state::updateNavigationCompact,
            text = if (isZh) "启用紧凑导航" else "Enable compact navigation",
        )
    }

    FluentSectionPanel(
        title = if (isZh) "语言与工程" else "Language and project",
        caption = if (isZh) {
            "脚手架默认中文，但保留显式切换入口，避免把语言写死在工作台里。"
        } else {
            "The starter defaults to Simplified Chinese but keeps an explicit language switch."
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentButton(
                onClick = {
                    if (state.language != FluentDemoLanguage.ZhCn) {
                        state.toggleLanguage()
                    }
                },
            ) {
                Text("中文")
            }
            Button(
                onClick = {
                    if (state.language != FluentDemoLanguage.EnUs) {
                        state.toggleLanguage()
                    }
                },
            ) {
                Text("English")
            }
        }
        StatusLine(
            label = if (isZh) "仓库地址" else "Repository",
            value = "compose-fluent-ui",
        )
        StatusLine(
            label = if (isZh) "最近动作" else "Last action",
            value = state.lastAction,
        )
    }
}

@Composable
private fun FluentSectionPanel(
    title: String,
    caption: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Layer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = FluentTheme.typography.subtitle,
            )
            Text(
                text = caption,
                style = FluentTheme.typography.caption,
                color = FluentTheme.colors.text.text.secondary,
            )
            content()
        }
    }
}

@Composable
private fun StatusLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = FluentTheme.typography.caption,
            color = FluentTheme.colors.text.text.secondary,
        )
        Text(
            text = value,
            style = FluentTheme.typography.bodyStrong,
        )
    }
}
