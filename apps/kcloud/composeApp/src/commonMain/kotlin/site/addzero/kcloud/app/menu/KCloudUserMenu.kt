package site.addzero.kcloud.app.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import site.addzero.appsidebar.WorkbenchUserButton
import site.addzero.kcloud.app.resolveKCloudIcon

@Composable
fun KCloudUserMenu(
    state: KCloudUserMenuState = koinInject(),
) {
    LaunchedEffect(state) {
        state.ensureLoaded()
    }

    Box {
        WorkbenchUserButton(
            label = state.displayName,
            avatarInitials = state.avatarInitials,
            onClick = state::toggle,
        )

        DropdownMenu(
            expanded = state.expanded,
            onDismissRequest = state::dismiss,
        ) {
            state.items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = resolveKCloudIcon(item.iconName),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        state.navigateTo(item.routePath)
                    },
                )
            }
        }
    }
}
