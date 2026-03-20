package site.addzero.liquiddemo.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.liquiddemo.components.LiquidGlassButton
import site.addzero.liquiddemo.components.LiquidGlassButtonStyle
import site.addzero.liquiddemo.components.LiquidGlassCard
import site.addzero.liquiddemo.components.LiquidGlassCardHeader
import site.addzero.liquiddemo.components.LiquidGlassDefaults

@Composable
fun CardDemo(
    modifier: Modifier = Modifier,
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        spec = LiquidGlassDefaults.heroCard,
    ) {
        LiquidGlassCardHeader(
            title = "High-Tier Cards",
            subtitle = "卡片强调内容仍然是主角，玻璃感只负责托起层级，不喧宾夺主。",
            badge = "Card",
        )

        Text(
            text = "这些参数已经固定成更像 Apple 的默认材质：圆角更饱满、折射更克制、色散只保留一丝边缘光感。",
            color = LiquidGlassDefaults.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Lensing",
                value = "Balanced",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Dispersion",
                value = "Subtle",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Shape",
                value = "Generous",
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidGlassButton(
                text = "Install",
                icon = Icons.Rounded.Download,
                style = LiquidGlassButtonStyle.Primary,
                onClick = {},
            )
            LiquidGlassButton(
                text = "Learn More",
                icon = Icons.Rounded.AutoAwesome,
                onClick = {},
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    LiquidGlassCard(
        modifier = modifier,
    ) {
        Text(
            text = title,
            color = LiquidGlassDefaults.textMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = value,
            color = LiquidGlassDefaults.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
    }
}
