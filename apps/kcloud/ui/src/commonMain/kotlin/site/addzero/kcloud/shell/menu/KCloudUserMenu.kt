package site.addzero.kcloud.shell.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.design.button.KCloudButtonVariant
import site.addzero.kcloud.design.button.KCloudIconButton
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath

@Composable
fun RowScope.KCloudShellActions(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    shellState: KCloudShellState = koinInject(),
    aiOverlayState: AddChatOverlayState = koinInject(),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KCloudShellIconButton(
            tooltip = if (aiOverlayState.visible) "关闭 AI 助手" else "打开 AI 助手",
            onClick = aiOverlayState::toggle,
            variant = if (aiOverlayState.visible) KCloudButtonVariant.Secondary else KCloudButtonVariant.Outline,
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
            )
        }
        KCloudShellIconButton(
            tooltip = if (darkTheme) "切换到浅色" else "切换到深色",
            onClick = onThemeToggle,
            variant = if (darkTheme) KCloudButtonVariant.Secondary else KCloudButtonVariant.Outline,
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
            )
        }
        KCloudUserMenu(shellState = shellState)
    }
}

@Composable
fun KCloudUserMenu(
    shellState: KCloudShellState = koinInject(),
    routeCatalog: KCloudRouteCatalog = koinInject(),
) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember(routeCatalog) {
        routeCatalog.findScene(SYSTEM_SCENE_ID)?.menuNodes.orEmpty()
    }

    Box {
        KCloudShellIconButton(
            tooltip = "本地工作台",
            onClick = { expanded = !expanded },
            variant = if (expanded) KCloudButtonVariant.Secondary else KCloudButtonVariant.Outline,
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (shellState.sidebarVisible) {
                            "隐藏菜单"
                        } else {
                            "显示菜单"
                        },
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    shellState.toggleSidebar()
                },
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        val routePath = item.firstLeafRoutePath() ?: return@DropdownMenuItem
                        expanded = false
                        shellState.selectRoute(routePath)
                    },
                )
            }
        }
    }
}

@Composable
private fun KCloudShellIconButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KCloudButtonVariant = KCloudButtonVariant.Outline,
    content: @Composable RowScope.() -> Unit,
) {
    KCloudIconButton(
        onClick = onClick,
        modifier = modifier,
        tooltip = tooltip,
        variant = variant,
        content = content,
    )
}

private const val SYSTEM_SCENE_ID = "系统管理"
