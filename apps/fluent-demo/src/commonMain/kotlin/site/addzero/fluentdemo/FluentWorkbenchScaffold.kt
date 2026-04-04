@file:OptIn(ExperimentalFluentApi::class)

package site.addzero.fluentdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.background.Layer
import io.github.composefluent.component.CommandBar
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ListItem
import io.github.composefluent.component.MenuFlyoutItem
import io.github.composefluent.component.MenuFlyoutSeparator
import io.github.composefluent.component.MenuItem
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.NavigationState
import io.github.composefluent.component.NavigationView
import io.github.composefluent.component.SubtleButton
import io.github.composefluent.component.Text
import io.github.composefluent.component.commandBarButtonSize

@Immutable
data class FluentWorkbenchNavItem(
    val key: String,
    val title: String,
    val caption: String,
    val icon: ImageVector,
)

@Immutable
data class FluentWorkbenchCommand(
    val label: String,
    val icon: ImageVector,
    val shortcut: String? = null,
    val onClick: () -> Unit,
)

@Immutable
data class FluentWorkbenchScaffoldConfig(
    val appTitle: String,
    val pageTitle: String,
    val pageCaption: String,
    val navigationItems: List<FluentWorkbenchNavItem>,
    val footerItems: List<FluentWorkbenchNavItem> = emptyList(),
    val selectedKey: String,
    val displayMode: NavigationDisplayMode,
    val commandItems: List<FluentWorkbenchCommand>,
    val statusText: String,
)

data class FluentWorkbenchScaffoldActions(
    val onSelectItem: (String) -> Unit,
)

@Composable
fun FluentWorkbenchScaffold(
    config: FluentWorkbenchScaffoldConfig,
    actions: FluentWorkbenchScaffoldActions,
    topNotice: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val navigationState = remember(config.displayMode) {
        NavigationState(
            initialExpanded = config.displayMode != NavigationDisplayMode.LeftCompact,
            initialOffset = Offset.Zero,
        )
    }

    NavigationView(
        modifier = Modifier.fillMaxSize(),
        displayMode = config.displayMode,
        state = navigationState,
        title = {
            Text(
                text = config.appTitle,
                style = FluentTheme.typography.caption,
            )
        },
        menuItems = {
            config.navigationItems.forEach { navItem ->
                item {
                    MenuItem(
                        selected = navItem.key == config.selectedKey,
                        onClick = {
                            actions.onSelectItem(navItem.key)
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(navItem.title)
                                Text(
                                    text = navItem.caption,
                                    style = FluentTheme.typography.caption,
                                    color = FluentTheme.colors.text.text.secondary,
                                )
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title,
                            )
                        },
                    )
                }
            }
        },
        footerItems = {
            config.footerItems.forEach { navItem ->
                item {
                    MenuItem(
                        selected = navItem.key == config.selectedKey,
                        onClick = {
                            actions.onSelectItem(navItem.key)
                        },
                        text = {
                            Text(navItem.title)
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title,
                            )
                        },
                    )
                }
            }
        },
    ) {
        WorkbenchPane(
            config = config,
            topNotice = topNotice,
            content = content,
        )
    }
}

@Composable
private fun WorkbenchPane(
    config: FluentWorkbenchScaffoldConfig,
    topNotice: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Layer(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = config.pageTitle,
                    style = FluentTheme.typography.titleLarge,
                )
                Text(
                    text = config.pageCaption,
                    style = FluentTheme.typography.body,
                    color = FluentTheme.colors.text.text.secondary,
                )
            }
        }

        topNotice?.invoke()

        WorkbenchCommandBar(config.commandItems)

        Layer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }

        Layer(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = config.statusText,
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.text.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun WorkbenchCommandBar(
    commandItems: List<FluentWorkbenchCommand>,
) {
    var expanded by remember { mutableStateOf(false) }

    CommandBar(
        expanded = expanded,
        onExpandedChanged = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
        secondary = { hasOverFlowItem ->
            if (hasOverFlowItem) {
                MenuFlyoutSeparator()
            }
            commandItems.takeLast(2).forEach { item ->
                MenuFlyoutItem(
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                    text = {
                        Text(item.label)
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                        )
                    },
                )
            }
        },
    ) {
        items(commandItems.size) { index ->
            val item = commandItems[index]
            if (isOverflow) {
                ListItem(
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                    text = {
                        Text(item.label)
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                        )
                    },
                    trailing = item.shortcut?.let { shortcut ->
                        {
                            Text(shortcut)
                        }
                    },
                )
            } else {
                SubtleButton(
                    onClick = item.onClick,
                    modifier = Modifier.commandBarButtonSize(),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                    Text(item.label)
                }
            }
        }
    }
}
