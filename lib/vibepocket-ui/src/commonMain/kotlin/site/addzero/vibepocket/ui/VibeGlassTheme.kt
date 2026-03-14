package site.addzero.vibepocket.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class VibeGlassPalette(
    val midnight: Color,
    val abyss: Color,
    val panelTop: Color,
    val panelBottom: Color,
    val panelEdge: Color,
    val panelInnerGlow: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkMuted: Color,
    val aqua: Color,
    val glacier: Color,
    val mist: Color,
    val coral: Color,
    val sunrise: Color,
    val success: Color,
    val danger: Color,
)

@Immutable
data class VibeGlassShapes(
    val frame: RoundedCornerShape,
    val panel: RoundedCornerShape,
    val control: RoundedCornerShape,
    val pill: RoundedCornerShape,
)

private val defaultPalette = VibeGlassPalette(
    midnight = Color(0xFFF4F8FF),
    abyss = Color(0xFFE6EEF9),
    panelTop = Color(0xFFFFFFFF),
    panelBottom = Color(0xFFF7FAFF),
    panelEdge = Color(0xFFD3E0F3),
    panelInnerGlow = Color(0xFFDBE8FF),
    ink = Color(0xFF162033),
    inkSoft = Color(0xFF3E4C67),
    inkMuted = Color(0xFF66758F),
    aqua = Color(0xFF2563EB),
    glacier = Color(0xFF60A5FA),
    mist = Color(0xFFDCE8FF),
    coral = Color(0xFF1D4ED8),
    sunrise = Color(0xFF93C5FD),
    success = Color(0xFF15803D),
    danger = Color(0xFFDC2626),
)

private val defaultShapes = VibeGlassShapes(
    frame = RoundedCornerShape(24.dp),
    panel = RoundedCornerShape(20.dp),
    control = RoundedCornerShape(16.dp),
    pill = RoundedCornerShape(999.dp),
)

private val LocalVibeGlassPalette = staticCompositionLocalOf { defaultPalette }
private val LocalVibeGlassShapes = staticCompositionLocalOf { defaultShapes }

object VibeGlassTheme {
    val palette: VibeGlassPalette
        @Composable get() = LocalVibeGlassPalette.current

    val shapes: VibeGlassShapes
        @Composable get() = LocalVibeGlassShapes.current
}

@Composable
fun VibeGlassAppTheme(content: @Composable () -> Unit) {
    val palette = defaultPalette
    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 46.sp,
            lineHeight = 52.sp,
            letterSpacing = (-1.0).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.3).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.4).sp,
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
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.15.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
    )
    val colorScheme = lightColorScheme(
        primary = palette.aqua,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD8E7FF),
        onPrimaryContainer = Color(0xFF001A41),
        inversePrimary = Color(0xFFA8C7FF),
        secondary = palette.coral,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDCE8FF),
        onSecondaryContainer = Color(0xFF0E1D38),
        tertiary = palette.glacier,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE0EBFF),
        onTertiaryContainer = Color(0xFF10203B),
        background = palette.midnight,
        onBackground = palette.ink,
        surface = palette.panelTop,
        onSurface = palette.ink,
        surfaceVariant = palette.abyss,
        onSurfaceVariant = palette.inkSoft,
        surfaceTint = palette.aqua,
        inverseSurface = Color(0xFF223248),
        inverseOnSurface = Color(0xFFF3F7FD),
        outline = palette.panelEdge,
        outlineVariant = Color(0xFFC5D5EA),
        error = palette.danger,
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        scrim = Color(0x66000000),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFDCE6F2),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7FAFF),
        surfaceContainer = Color(0xFFF0F5FD),
        surfaceContainerHigh = Color(0xFFEAF1FB),
        surfaceContainerHighest = Color(0xFFE3EBF6),
    )

    CompositionLocalProvider(
        LocalVibeGlassPalette provides palette,
        LocalVibeGlassShapes provides defaultShapes,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
