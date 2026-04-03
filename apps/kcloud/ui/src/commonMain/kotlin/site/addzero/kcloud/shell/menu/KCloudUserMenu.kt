package site.addzero.kcloud.shell.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath
import site.addzero.component.Button as ShadcnButton
import site.addzero.component.ButtonSize as ShadcnButtonSize
import site.addzero.component.ButtonVariant as ShadcnButtonVariant

@Composable
fun RowScope.KCloudShellActions(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    shellState: KCloudShellState = koinInject(),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KCloudShellIconButton(
            tooltip = if (darkTheme) "切换到浅色" else "切换到深色",
            onClick = onThemeToggle,
            variant = if (darkTheme) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Outline,
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
            )
        }
        KCloudShellIconButton(
            tooltip = if (shellState.sidebarVisible) "隐藏菜单" else "显示菜单",
            onClick = shellState::toggleSidebar,
            variant = if (shellState.sidebarVisible) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Outline,
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
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
            variant = if (expanded) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Outline,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KCloudShellIconButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Outline,
    content: @Composable RowScope.() -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = rememberTooltipState(),
    ) {
        ShadcnButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = ShadcnButtonSize.Icon,
            shape = RoundedCornerShape(999.dp),
            content = content,
        )
    }
}

private const val SYSTEM_SCENE_ID = "系统管理"
