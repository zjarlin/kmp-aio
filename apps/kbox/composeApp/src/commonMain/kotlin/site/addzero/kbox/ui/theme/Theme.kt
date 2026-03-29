package site.addzero.kbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2067A8),
    onPrimary = Color(0xFFF6FBFF),
    secondary = Color(0xFF007F86),
    background = Color(0xFFF4F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE4EDF6),
    onBackground = Color(0xFF10202F),
    onSurface = Color(0xFF10202F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8EC8FF),
    onPrimary = Color(0xFF08233D),
    secondary = Color(0xFF74E0D4),
    background = Color(0xFF061019),
    surface = Color(0xFF0B1624),
    surfaceVariant = Color(0xFF102033),
    onBackground = Color(0xFFE7F0FB),
    onSurface = Color(0xFFE7F0FB),
)

@Composable
fun KboxTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
