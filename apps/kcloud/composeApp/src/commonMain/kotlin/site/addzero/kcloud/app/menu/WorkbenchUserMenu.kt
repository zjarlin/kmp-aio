package site.addzero.kcloud.app.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import site.addzero.appsidebar.WorkbenchUserButton
import site.addzero.kcloud.app.WorkbenchRouteCatalog
import site.addzero.kcloud.app.WorkbenchSidebarNode
import site.addzero.kcloud.app.WorkbenchShellState

@Composable
fun WorkbenchUserMenu(
    shellState: WorkbenchShellState = koinInject(),
    routeCatalog: WorkbenchRouteCatalog = koinInject(),
) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember(routeCatalog) {
        routeCatalog.findScene(SYSTEM_SCENE_ID)?.menuNodes.orEmpty()
    }
    val displayName = "本地工作台"
    val avatarInitials = remember(displayName) {
        displayName.toAvatarInitials()
    }

    Box {
        WorkbenchUserButton(
            label = displayName,
            avatarInitials = avatarInitials,
            onClick = { expanded = !expanded },
        )

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

private fun WorkbenchSidebarNode.firstLeafRoutePath(): String? {
    routePath?.let { route ->
        return route
    }
    return children.firstNotNullOfOrNull { child ->
        child.firstLeafRoutePath()
    }
}

private fun String.toAvatarInitials(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) {
        return "U"
    }
    val segments = trimmed.substringBefore("@")
        .split('.', '-', '_', ' ')
        .filter(String::isNotBlank)
    if (segments.isEmpty()) {
        return trimmed.take(2).uppercase()
    }
    return segments.take(2)
        .joinToString(separator = "") { part ->
            part.take(1).uppercase()
        }
        .ifBlank { "U" }
}

private const val SYSTEM_SCENE_ID = "系统"
