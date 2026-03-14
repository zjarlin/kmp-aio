package site.addzero.vibepocket.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import site.addzero.vibepocket.ui.StudioEmptyState

@Composable
fun PlaceholderScreen(icon: String, subtitle: String) {
    StudioEmptyState(
        icon = icon,
        title = "模块重构中",
        description = subtitle,
        modifier = Modifier.fillMaxWidth(),
    )
}
