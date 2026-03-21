package site.addzero.vibepocket.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import site.addzero.liquidglass.LiquidGlassContentColors
import site.addzero.liquidglass.LiquidGlassDefaults
import site.addzero.liquidglass.LiquidGlassRoot
import site.addzero.liquidglass.LiquidGlassSpec
import vibepocket.apps.vibepocket.generated.resources.Res
import vibepocket.apps.vibepocket.generated.resources.vibepocket_music_wallpaper


internal object VibePocketLiquidGlass {
    val sidebarColors = LiquidGlassContentColors(
        textPrimary = Color(0xFF162235),
        textSecondary = Color(0xFF42516D),
        textMuted = Color(0xFF71809D),
    )

    val sidebarSpec = LiquidGlassDefaults.sidebar.copy(
        frost = 18.dp,
        refraction = 0.66f,
        curve = 0.34f,
        edge = 0.006f,
        tint = Color(0xFF8BB8FF).copy(alpha = 0.026f),
        saturation = 1.02f,
        dispersion = 0.010f,
        shape = RoundedCornerShape(30.dp),
        surfaceColor = Color.White.copy(alpha = 0.17f),
        borderColor = Color.White.copy(alpha = 0.15f),
    )

    val sidebarItemSpec = LiquidGlassDefaults.sidebarItem.copy(
        frost = 14.dp,
        refraction = 0.58f,
        curve = 0.20f,
        edge = 0.004f,
        tint = Color(0xFF9AC5FF).copy(alpha = 0.010f),
        saturation = 1.01f,
        dispersion = 0.008f,
        shape = RoundedCornerShape(20.dp),
        surfaceColor = Color.White.copy(alpha = 0.08f),
        borderColor = Color.White.copy(alpha = 0.08f),
    )

    val sidebarItemSelectedSpec = LiquidGlassDefaults.sidebarItemSelected.copy(
        frost = 16.dp,
        refraction = 0.70f,
        curve = 0.28f,
        edge = 0.006f,
        tint = Color(0xFF71A8FF).copy(alpha = 0.052f),
        saturation = 1.05f,
        dispersion = 0.016f,
        shape = RoundedCornerShape(20.dp),
        surfaceColor = Color.White.copy(alpha = 0.13f),
        borderColor = Color.White.copy(alpha = 0.15f),
    )

    val sectionSpec = LiquidGlassDefaults.card.copy(
        frost = 18.dp,
        refraction = 0.50f,
        curve = 0.16f,
        edge = 0.0012f,
        tint = Color(0xFFF7FBFF).copy(alpha = 0.008f),
        saturation = 1.01f,
        dispersion = 0.0022f,
        shape = RoundedCornerShape(22.dp),
        surfaceColor = Color.White.copy(alpha = 0.11f),
        borderColor = Color.White.copy(alpha = 0.08f),
    )

    val workspaceSpec = LiquidGlassDefaults.heroCard.copy(
        frost = 20.dp,
        refraction = 0.54f,
        curve = 0.20f,
        edge = 0.0014f,
        tint = Color(0xFFF5FAFF).copy(alpha = 0.010f),
        saturation = 1.01f,
        dispersion = 0.0028f,
        shape = RoundedCornerShape(30.dp),
        surfaceColor = Color.White.copy(alpha = 0.10f),
        borderColor = Color.White.copy(alpha = 0.08f),
    )
}

internal fun metricCardSpec(accent: Color): LiquidGlassSpec {
    return LiquidGlassDefaults.sidebarItemSelected.copy(
        frost = 15.dp,
        refraction = 0.60f,
        curve = 0.20f,
        edge = 0.005f,
        tint = accent.copy(alpha = 0.048f),
        saturation = 1.03f,
        dispersion = 0.010f,
        shape = RoundedCornerShape(18.dp),
        surfaceColor = Color.White.copy(alpha = 0.12f),
        borderColor = Color.White.copy(alpha = 0.10f),
    )
}

internal fun pillSpec(accent: Color): LiquidGlassSpec {
    return LiquidGlassDefaults.button.copy(
        frost = 14.dp,
        refraction = 0.56f,
        curve = 0.20f,
        edge = 0.005f,
        tint = accent.copy(alpha = 0.042f),
        saturation = 1.02f,
        dispersion = 0.009f,
        shape = RoundedCornerShape(999.dp),
        surfaceColor = Color.White.copy(alpha = 0.11f),
        borderColor = Color.White.copy(alpha = 0.10f),
    )
}

@Composable
internal fun VibePocketLiquidGlassRoot(
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidGlassRoot(
        modifier = Modifier.fillMaxSize(),
        background = {
            VibePocketLiquidGlassBackdrop()
        },
        content = content,
    )
}

@Composable
private fun BoxScope.VibePocketLiquidGlassBackdrop() {
    VibePocketWallpaper()
    Box(modifier = Modifier.fillMaxSize().vibePocketBackdropBase())
    Box(modifier = topLeftAtmosphere())
    Box(modifier = topRightAtmosphere())
    Box(modifier = bottomGlowAtmosphere())
}

/** 壁纸底片：直接铺音乐氛围图，让液态玻璃真正有东西可以折射。 */
@Composable
private fun BoxScope.VibePocketWallpaper() {
    Image(
        painter = painterResource(Res.drawable.vibepocket_music_wallpaper),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = 0.98f,
    )
}

/** 桌面蒙版：用更轻的冷色水雾压住背景，而不是整块奶白提亮。 */
private fun Modifier.vibePocketBackdropBase(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF06101D).copy(alpha = 0.24f),
                Color(0xFF081425).copy(alpha = 0.34f),
                Color(0xFF0A1930).copy(alpha = 0.44f),
            ),
        ),
    )
}

/** 左上主光斑：给舞台一个更柔和的启动焦点。 */
private fun BoxScope.topLeftAtmosphere(): Modifier {
    return Modifier.align(Alignment.TopStart)
        .offset(x = (-90).dp, y = (-110).dp)
        .size(340.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8FE7FF).copy(alpha = 0.10f),
                    Color(0xFF6AAFFF).copy(alpha = 0.07f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(80.dp)
}

/** 右上冷色云团：拉出一点工作台的科技感，不让背景太平。 */
private fun BoxScope.topRightAtmosphere(): Modifier {
    return Modifier.align(Alignment.TopEnd)
        .offset(x = 54.dp, y = (-40).dp)
        .width(320.dp)
        .height(250.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFAA88FF).copy(alpha = 0.10f),
                    Color(0xFF73C7FF).copy(alpha = 0.06f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(72.dp)
}

/** 底部景深光带：把大工作区托起来，避免页面像平铺白板。 */
private fun BoxScope.bottomGlowAtmosphere(): Modifier {
    return Modifier.align(Alignment.BottomCenter)
        .offset(y = 120.dp)
        .width(760.dp)
        .height(320.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF58D8FF).copy(alpha = 0.08f),
                    Color(0xFF786DFF).copy(alpha = 0.06f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(88.dp)
}
