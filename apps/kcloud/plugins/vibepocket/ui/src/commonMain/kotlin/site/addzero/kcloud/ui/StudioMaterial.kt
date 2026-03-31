package site.addzero.kcloud.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.addzero.liquidglass.LiquidGlassCard
import site.addzero.liquidglass.LiquidGlassWorkbenchDefaults
import site.addzero.liquidglass.liquidGlassSurface

enum class StudioTone {
    Primary,
    Secondary,
    Tertiary,
    Error,
    Surface,
}

@Composable
fun StudioSectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassCard(
        modifier = modifier,
        spec = LiquidGlassWorkbenchDefaults.section,
        contentPadding = StudioSectionCardPadding,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (title != null || subtitle != null || action != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (action != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            content = action,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun StudioMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: StudioTone = StudioTone.Primary,
) {
    Box(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.metricCardSurface(tone.metricContainerColor()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun StudioPill(
    text: String,
    modifier: Modifier = Modifier,
    tone: StudioTone = StudioTone.Secondary,
) {
    val colors = tone.pillColors()
    Box(
        modifier = modifier.pillSurface(colors.containerColor),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = colors.contentColor,
        )
    }
}

@Composable
fun StudioEmptyState(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    StudioSectionCard(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val StudioSectionCardPadding = androidx.compose.foundation.layout.PaddingValues(
    horizontal = 16.dp,
    vertical = 14.dp,
)

private data class StudioPillColors(
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
private fun StudioTone.metricContainerColor(): Color {
    return when (this) {
        StudioTone.Primary -> MaterialTheme.colorScheme.primaryContainer
        StudioTone.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        StudioTone.Tertiary -> MaterialTheme.colorScheme.tertiaryContainer
        StudioTone.Error -> MaterialTheme.colorScheme.errorContainer
        StudioTone.Surface -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun StudioTone.pillColors(): StudioPillColors {
    return when (this) {
        StudioTone.Primary -> StudioPillColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        StudioTone.Secondary -> StudioPillColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        StudioTone.Tertiary -> StudioPillColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        StudioTone.Error -> StudioPillColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        StudioTone.Surface -> StudioPillColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 指标卡片表面：用更紧凑的玻璃小面板承接短指标。 */
private fun Modifier.metricCardSurface(accent: Color): Modifier {
    return fillMaxWidth()
        .liquidGlassSurface(LiquidGlassWorkbenchDefaults.metric(accent))
        .padding(horizontal = 12.dp, vertical = 10.dp)
}

/** 胶囊标签表面：把状态标签收成一颗轻量的高光小药丸。 */
private fun Modifier.pillSurface(accent: Color): Modifier {
    return liquidGlassSurface(LiquidGlassWorkbenchDefaults.pill(accent))
        .padding(horizontal = 1.dp, vertical = 1.dp)
}
