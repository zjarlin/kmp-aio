package site.addzero.kcloud.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import site.addzero.cupertino.workbench.theme.CupertinoWorkbenchTheme

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CupertinoWorkbenchTheme(
        darkTheme = darkTheme,
        content = content,
    )
}
