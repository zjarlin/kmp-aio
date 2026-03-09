package site.addzero.component.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import com.kyant.shapes.RoundedRectangle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GlassButton — 玻璃风格按钮
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感的按钮。
 * 支持启用/禁用状态，禁用时降低透明度。
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param enabled 是否启用，默认 true
 * @param shape 按钮形状，默认 12dp 圆角
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedRectangle(12.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .glassEffect(shape = shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) GlassTheme.TextPrimary else GlassTheme.TextDisabled,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * NeonGlassButton — 霓虹发光玻璃按钮
 *
 * 使用 [neonGlassEffect] 渲染带霓虹发光边框的按钮。
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param glowColor 霓虹发光颜色，默认 [GlassTheme.NeonCyan]
 * @param enabled 是否启用，默认 true
 * @param intensity 发光强度 (0.0 ~ 1.0)，默认 0.6
 * @param shape 按钮形状，默认 12dp 圆角
 */
@Composable
fun NeonGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = GlassTheme.NeonCyan,
    enabled: Boolean = true,
    intensity: Float = 0.6f,
    shape: Shape = RoundedRectangle(12.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .neonGlassEffect(
                shape = shape,
                glowColor = glowColor,
                intensity = intensity,
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) glowColor else GlassTheme.TextDisabled,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * LiquidGlassButton — 液态玻璃按钮
 *
 * 使用 [liquidGlassEffect] 渲染多层渐变和光折射边框高光的按钮。
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 外部修饰符
 * @param enabled 是否启用，默认 true
 * @param primaryColor 主表面色，默认 [GlassTheme.WaterSurfacePrimary]
 * @param secondaryColor 次表面色，默认 [GlassTheme.WaterSurfaceSecondary]
 * @param shape 按钮形状，默认 16dp 圆角
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primaryColor: Color = GlassTheme.WaterSurfacePrimary,
    secondaryColor: Color = GlassTheme.WaterSurfaceSecondary,
    shape: Shape = RoundedRectangle(16.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .liquidGlassEffect(
                shape = shape,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) GlassTheme.TextPrimary else GlassTheme.TextDisabled,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
