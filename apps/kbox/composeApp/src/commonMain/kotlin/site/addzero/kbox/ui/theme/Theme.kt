package site.addzero.kbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF3574F0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = Color(0xFF12316C),
    secondary = Color(0xFF58657A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7EBF2),
    onSecondaryContainer = Color(0xFF263142),
    tertiary = Color(0xFF177D69),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD8F0E8),
    onTertiaryContainer = Color(0xFF0A473B),
    error = Color(0xFFC75450),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBE0DF),
    onErrorContainer = Color(0xFF5A1D1B),
    background = Color(0xFFF3F5F8),
    onBackground = Color(0xFF1F2329),
    surface = Color(0xFFFCFCFD),
    onSurface = Color(0xFF1F2329),
    surfaceVariant = Color(0xFFE8EDF3),
    onSurfaceVariant = Color(0xFF5A6472),
    outline = Color(0xFFC8D0DA),
    outlineVariant = Color(0xFFD8DFE7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF78A8FF),
    onPrimary = Color(0xFF0C223F),
    primaryContainer = Color(0xFF173764),
    onPrimaryContainer = Color(0xFFD8E5FF),
    secondary = Color(0xFF9EAABE),
    onSecondary = Color(0xFF1E2937),
    secondaryContainer = Color(0xFF2A3341),
    onSecondaryContainer = Color(0xFFDDE3ED),
    tertiary = Color(0xFF56C9AE),
    onTertiary = Color(0xFF073B30),
    tertiaryContainer = Color(0xFF0B4B3D),
    onTertiaryContainer = Color(0xFFC4F3E6),
    error = Color(0xFFFF8B86),
    onError = Color(0xFF5D1718),
    errorContainer = Color(0xFF7A2828),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1B1F24),
    onBackground = Color(0xFFE6EAF0),
    surface = Color(0xFF23272E),
    onSurface = Color(0xFFE6EAF0),
    surfaceVariant = Color(0xFF2A3038),
    onSurfaceVariant = Color(0xFFB8C2CF),
    outline = Color(0xFF485261),
    outlineVariant = Color(0xFF363E49),
)

private val KboxShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
)

private val KboxTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun KboxTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KboxTypography,
        shapes = KboxShapes,
        content = content,
    )
}
