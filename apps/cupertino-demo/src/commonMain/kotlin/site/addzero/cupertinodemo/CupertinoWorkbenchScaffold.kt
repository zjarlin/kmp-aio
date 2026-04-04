@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.cupertinodemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoButton
import io.github.robinpcrd.cupertino.CupertinoButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoIcon
import io.github.robinpcrd.cupertino.CupertinoIconButton
import io.github.robinpcrd.cupertino.CupertinoIconButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoScaffold
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.CupertinoTopAppBar
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.icons.CupertinoIcons
import io.github.robinpcrd.cupertino.icons.outlined.SidebarLeft
import io.github.robinpcrd.cupertino.section.CupertinoSection
import io.github.robinpcrd.cupertino.section.ProvideSectionStyle
import io.github.robinpcrd.cupertino.section.SectionLink
import io.github.robinpcrd.cupertino.section.SectionStyle
import io.github.robinpcrd.cupertino.theme.CupertinoTheme

@Immutable
data class CupertinoWorkbenchNavItem(
    val key: String,
    val title: String,
    val caption: String,
    val icon: ImageVector,
)

@Immutable
data class CupertinoWorkbenchScaffoldConfig(
    val appTitle: String,
    val pageTitle: String,
    val pageCaption: String,
    val navItems: List<CupertinoWorkbenchNavItem>,
    val selectedKey: String,
    val sidebarCollapsed: Boolean,
    val sidebarQuery: String,
    val statusText: String,
)

data class CupertinoWorkbenchScaffoldActions(
    val onSelectItem: (String) -> Unit,
    val onToggleSidebar: () -> Unit,
    val onSidebarQueryChange: (String) -> Unit,
)

@Composable
fun CupertinoWorkbenchScaffold(
    config: CupertinoWorkbenchScaffoldConfig,
    actions: CupertinoWorkbenchScaffoldActions,
    topBarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    CupertinoScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CupertinoTopAppBar(
                title = {
                    CupertinoText(
                        text = config.appTitle,
                        style = CupertinoTheme.typography.headline,
                    )
                },
                navigationIcon = {
                    CupertinoIconButton(
                        onClick = actions.onToggleSidebar,
                        colors = CupertinoIconButtonDefaults.bezeledGrayButtonColors(),
                    ) {
                        CupertinoIcon(
                            imageVector = CupertinoIcons.Outlined.SidebarLeft,
                            contentDescription = "切换侧边栏",
                        )
                    }
                },
                actions = topBarActions,
            )
        },
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WorkbenchSidebar(
                modifier = Modifier.fillMaxHeight(),
                config = config,
                actions = actions,
            )
            WorkbenchContent(
                modifier = Modifier.weight(1f),
                config = config,
                content = content,
            )
        }
    }
}

@Composable
private fun WorkbenchSidebar(
    modifier: Modifier,
    config: CupertinoWorkbenchScaffoldConfig,
    actions: CupertinoWorkbenchScaffoldActions,
) {
    val sidebarWidth = if (config.sidebarCollapsed) 88.dp else 280.dp
    val filteredItems = config.navItems.filter { item ->
        if (config.sidebarQuery.isBlank()) {
            true
        } else {
            val keyword = config.sidebarQuery.trim()
            item.title.contains(keyword, ignoreCase = true) ||
                item.caption.contains(keyword, ignoreCase = true)
        }
    }

    CupertinoSurface(
        modifier = modifier.width(sidebarWidth),
        shape = CupertinoTheme.shapes.large,
        color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!config.sidebarCollapsed) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CupertinoText(
                        text = "Starter",
                        style = CupertinoTheme.typography.title3,
                    )
                    CupertinoText(
                        text = "纯 Cupertino 桌面脚手架",
                        style = CupertinoTheme.typography.footnote,
                        color = CupertinoTheme.colorScheme.secondaryLabel,
                    )
                }
                CupertinoBorderedTextField(
                    value = config.sidebarQuery,
                    onValueChange = actions.onSidebarQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        CupertinoText("搜索导航")
                    },
                )
            }

            if (config.sidebarCollapsed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    filteredItems.forEach { item ->
                        val selected = item.key == config.selectedKey
                        CupertinoIconButton(
                            onClick = {
                                actions.onSelectItem(item.key)
                            },
                            colors = if (selected) {
                                CupertinoIconButtonDefaults.bezeledFilledButtonColors()
                            } else {
                                CupertinoIconButtonDefaults.bezeledGrayButtonColors()
                            },
                        ) {
                            CupertinoIcon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                            )
                        }
                    }
                }
            } else {
                ProvideSectionStyle(SectionStyle.Sidebar) {
                    CupertinoSection(
                        modifier = Modifier.fillMaxWidth(),
                        style = SectionStyle.Sidebar,
                        title = {
                            CupertinoText("导航")
                        },
                    ) {
                        if (filteredItems.isEmpty()) {
                            SectionLink(
                                onClick = {},
                                enabled = false,
                                chevron = {},
                                title = {
                                    CupertinoText(
                                        text = "没有匹配结果",
                                        color = CupertinoTheme.colorScheme.tertiaryLabel,
                                    )
                                },
                            )
                        } else {
                            filteredItems.forEach { item ->
                                val selected = item.key == config.selectedKey
                                SectionLink(
                                    onClick = {
                                        actions.onSelectItem(item.key)
                                    },
                                    chevron = {},
                                    icon = {
                                        CupertinoIcon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (selected) {
                                                CupertinoTheme.colorScheme.accent
                                            } else {
                                                CupertinoTheme.colorScheme.secondaryLabel
                                            },
                                        )
                                    },
                                    caption = {
                                        if (selected) {
                                            SidebarBadge("当前")
                                        }
                                    },
                                    title = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            CupertinoText(
                                                text = item.title,
                                                color = if (selected) {
                                                    CupertinoTheme.colorScheme.accent
                                                } else {
                                                    Color.Unspecified
                                                },
                                            )
                                            CupertinoText(
                                                text = item.caption,
                                                style = CupertinoTheme.typography.caption2,
                                                color = CupertinoTheme.colorScheme.tertiaryLabel,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f))

            if (!config.sidebarCollapsed) {
                CupertinoSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CupertinoTheme.shapes.medium,
                    color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CupertinoText(
                            text = "状态",
                            style = CupertinoTheme.typography.caption1,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                        )
                        CupertinoText(
                            text = config.statusText,
                            style = CupertinoTheme.typography.footnote,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchContent(
    modifier: Modifier,
    config: CupertinoWorkbenchScaffoldConfig,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CupertinoSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = CupertinoTheme.shapes.large,
            color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CupertinoText(
                    text = config.pageTitle,
                    style = CupertinoTheme.typography.largeTitle,
                )
                CupertinoText(
                    text = config.pageCaption,
                    style = CupertinoTheme.typography.body,
                    color = CupertinoTheme.colorScheme.secondaryLabel,
                )
            }
        }

        CupertinoSurface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = CupertinoTheme.shapes.large,
            color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun SidebarBadge(
    text: String,
) {
    CupertinoSurface(
        shape = CupertinoTheme.shapes.small,
        color = CupertinoTheme.colorScheme.accent.copy(alpha = 0.14f),
    ) {
        CupertinoText(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = CupertinoTheme.typography.caption2,
            color = CupertinoTheme.colorScheme.accent,
        )
    }
}

@Composable
fun CupertinoTopBarTextAction(
    label: String,
    onClick: () -> Unit,
) {
    CupertinoButton(
        onClick = onClick,
        colors = CupertinoButtonDefaults.plainButtonColors(),
    ) {
        CupertinoText(label)
    }
}
