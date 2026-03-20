package site.addzero.liquiddemo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

@Immutable
data class LiquidGlassSpec(
    val frost: Dp,
    val refraction: Float,
    val curve: Float,
    val edge: Float,
    val tint: Color,
    val saturation: Float,
    val dispersion: Float,
    val shape: Shape,
    val surfaceColor: Color,
    val borderColor: Color,
)

object LiquidGlassDefaults {
    val accent = Color(0xFF8CEBFF)
    val accentStrong = Color(0xFF65DFFF)
    val textPrimary = Color.White.copy(alpha = 0.96f)
    val textSecondary = Color.White.copy(alpha = 0.72f)
    val textMuted = Color.White.copy(alpha = 0.56f)
    val divider = Color.White.copy(alpha = 0.10f)

    val backgroundPalette = listOf(
        Color(0xFF0A1021),
        Color(0xFF112744),
        Color(0xFF23557A),
        Color(0xFF6A43F5),
    )

    val button = LiquidGlassSpec(
        frost = 18.dp,
        refraction = 0.72f,
        curve = 0.36f,
        edge = 0.05f,
        tint = Color.White.copy(alpha = 0.05f),
        saturation = 1.08f,
        dispersion = 0.048f,
        shape = RoundedCornerShape(999.dp),
        surfaceColor = Color.White.copy(alpha = 0.06f),
        borderColor = Color.White.copy(alpha = 0.16f),
    )

    val primaryButton = button.copy(
        frost = 20.dp,
        refraction = 0.79f,
        curve = 0.40f,
        edge = 0.06f,
        tint = accent.copy(alpha = 0.10f),
        saturation = 1.14f,
        dispersion = 0.082f,
        surfaceColor = Color.White.copy(alpha = 0.075f),
        borderColor = Color.White.copy(alpha = 0.20f),
    )

    val card = LiquidGlassSpec(
        frost = 22.dp,
        refraction = 0.68f,
        curve = 0.50f,
        edge = 0.05f,
        tint = Color.White.copy(alpha = 0.045f),
        saturation = 1.08f,
        dispersion = 0.036f,
        shape = RoundedCornerShape(30.dp),
        surfaceColor = Color.White.copy(alpha = 0.05f),
        borderColor = Color.White.copy(alpha = 0.14f),
    )

    val heroCard = card.copy(
        frost = 24.dp,
        refraction = 0.74f,
        curve = 0.56f,
        edge = 0.06f,
        tint = accent.copy(alpha = 0.045f),
        saturation = 1.12f,
        dispersion = 0.052f,
        shape = RoundedCornerShape(36.dp),
        surfaceColor = Color.White.copy(alpha = 0.045f),
        borderColor = Color.White.copy(alpha = 0.16f),
    )

    val sidebar = LiquidGlassSpec(
        frost = 22.dp,
        refraction = 0.66f,
        curve = 0.46f,
        edge = 0.05f,
        tint = Color.White.copy(alpha = 0.04f),
        saturation = 1.05f,
        dispersion = 0.030f,
        shape = RoundedCornerShape(32.dp),
        surfaceColor = Color.White.copy(alpha = 0.045f),
        borderColor = Color.White.copy(alpha = 0.14f),
    )

    val sidebarItem = LiquidGlassSpec(
        frost = 16.dp,
        refraction = 0.56f,
        curve = 0.28f,
        edge = 0.04f,
        tint = Color.White.copy(alpha = 0.03f),
        saturation = 1.03f,
        dispersion = 0.022f,
        shape = RoundedCornerShape(22.dp),
        surfaceColor = Color.Transparent,
        borderColor = Color.Transparent,
    )

    val sidebarItemSelected = sidebarItem.copy(
        frost = 18.dp,
        refraction = 0.72f,
        curve = 0.36f,
        edge = 0.05f,
        tint = accent.copy(alpha = 0.085f),
        saturation = 1.12f,
        dispersion = 0.060f,
        surfaceColor = Color.White.copy(alpha = 0.065f),
        borderColor = Color.White.copy(alpha = 0.16f),
    )
}

val LocalLiquidGlassState = staticCompositionLocalOf<LiquidState> {
    error("LiquidGlassRoot is required before using LiquidGlass components.")
}

fun Modifier.liquidGlassMaterial(
    spec: LiquidGlassSpec,
): Modifier = composed {
    val liquidState = LocalLiquidGlassState.current
    liquid(liquidState) {
        frost = spec.frost
        shape = spec.shape
        refraction = spec.refraction
        curve = spec.curve
        edge = spec.edge
        tint = spec.tint
        saturation = spec.saturation
        dispersion = spec.dispersion
    }
}

fun Modifier.liquidGlassSurface(
    spec: LiquidGlassSpec,
): Modifier = liquidGlassMaterial(spec)
    .clip(spec.shape)
    .background(spec.surfaceColor, spec.shape)
    .border(1.dp, spec.borderColor, spec.shape)

@Composable
fun LiquidGlassRoot(
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val liquidState = rememberLiquidState()
    CompositionLocalProvider(LocalLiquidGlassState provides liquidState) {
        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquefiable(liquidState),
                content = background,
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content,
            )
        }
    }
}
