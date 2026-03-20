package site.addzero.liquiddemo.demos

import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.liquiddemo.components.LiquidGlassButton
import site.addzero.liquiddemo.components.LiquidGlassButtonStyle
import site.addzero.liquiddemo.components.LiquidGlassCard
import site.addzero.liquiddemo.components.LiquidGlassCardHeader

@Composable
fun ButtonDemo(
    modifier: Modifier = Modifier,
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        LiquidGlassCardHeader(
            title = "High-Tier Buttons",
            subtitle = "默认就是 Apple 风格的 capsule glass：低噪声、轻色散、主次层级清晰。",
            badge = "Button",
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidGlassButton(
                text = "Create",
                icon = Icons.Rounded.AutoAwesome,
                style = LiquidGlassButtonStyle.Primary,
                onClick = {},
            )
            LiquidGlassButton(
                text = "Preview",
                icon = Icons.Rounded.PlayArrow,
                onClick = {},
            )
            LiquidGlassButton(
                text = "Continue",
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                onClick = {},
            )
        }
    }
}
