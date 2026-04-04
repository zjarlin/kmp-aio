package site.addzero.kcloud.shell.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Work
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
import site.addzero.kcloud.design.button.KCloudButtonVariant
import site.addzero.kcloud.design.button.KCloudIconButton
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.shell.navigation.firstLeafRoutePath

@Composable
fun KCloudUserMenu(
    shellState: KCloudShellState,
    routeCatalog: KCloudRouteCatalog,
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
internal fun KCloudShellIconButton(
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
