package site.addzero.kcloud.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.kcloud.ui.StudioEmptyState

@Composable
fun PlaceholderScreen(icon: String, subtitle: String) {
    StudioEmptyState(
        icon = icon,
        title = "模块重构中",
        description = subtitle,
        modifier = Modifier.fillMaxWidth(),
    )
}
