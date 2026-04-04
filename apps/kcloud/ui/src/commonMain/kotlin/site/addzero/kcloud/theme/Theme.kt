package site.addzero.kcloud.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import site.addzero.themes.ShadcnTheme

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ShadcnTheme(
        isDarkTheme = darkTheme,
        content = content,
    )
}
