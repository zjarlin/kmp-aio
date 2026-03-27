package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class ShowcasePageTone {
    Dark,
    Light,
}

private data class ShowcasePagePalette(
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val bulletColor: Color,
    val sidebarBadgeBackground: Color,
    val metricGradientTop: Color,
    val metricGradientBottom: Color,
    val metricBorder: Color,
    val factBackground: Color,
    val highlightBackground: Color,
    val highlightBorder: Color,
)

private val LocalShowcasePagePalette = staticCompositionLocalOf {
    ShowcasePagePalettes.dark
}

@Composable
internal fun ProvideShowcasePageTone(
    tone: ShowcasePageTone,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalShowcasePagePalette provides if (tone == ShowcasePageTone.Light) {
            ShowcasePagePalettes.light
        } else {
            ShowcasePagePalettes.dark
        },
        content = content,
    )
}

@Composable
internal fun ShowcasePage(
    eyebrow: String,
    title: String,
    summary: String,
    metrics: List<Pair<String, String>>,
    highlights: List<String>,
    primaryActionLabel: String,
    secondaryActionLabel: String,
) {
    val palette = LocalShowcasePagePalette.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textMuted,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = {}) {
                Text(primaryActionLabel)
            }
            OutlinedButton(onClick = {}) {
                Text(secondaryActionLabel)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            metrics.forEach { (label, value) ->
                ShowcaseMetricCard(
                    label = label,
                    value = value,
                )
            }
        }

        ShowcaseHighlightsCard(
            title = "当前焦点",
            lines = highlights,
        )
    }
}

@Composable
internal fun ShowcaseInspector(
    title: String,
    summary: String,
    facts: List<Pair<String, String>>,
    tasks: List<String>,
) {
    val palette = LocalShowcasePagePalette.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }

        facts.forEach { (label, value) ->
            ShowcaseFactRow(
                label = label,
                value = value,
            )
        }

        if (tasks.isNotEmpty()) {
            ShowcaseHighlightsCard(
                title = "待办动作",
                lines = tasks,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun ColumnScope.ShowcaseSidebarInfo(
    title: String,
    value: String,
) {
    val palette = LocalShowcasePagePalette.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = palette.sidebarBadgeBackground,
                shape = RoundedCornerShape(18.dp),
            )
            .border(
                width = 1.dp,
                color = palette.metricBorder,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textPrimary,
            )
        }
    }
}

@Composable
private fun ShowcaseMetricCard(
    label: String,
    value: String,
) {
    val palette = LocalShowcasePagePalette.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.metricGradientTop,
                        palette.metricGradientBottom,
                    ),
                ),
                shape = RoundedCornerShape(22.dp),
            )
            .border(
                width = 1.dp,
                color = palette.metricBorder,
                shape = RoundedCornerShape(22.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ShowcaseFactRow(
    label: String,
    value: String,
) {
    val palette = LocalShowcasePagePalette.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = palette.factBackground,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun ShowcaseHighlightsCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    val palette = LocalShowcasePagePalette.current
    Box(
        modifier = modifier.fillMaxWidth()
            .background(
                color = palette.highlightBackground,
                shape = RoundedCornerShape(22.dp),
            )
            .border(
                width = 1.dp,
                color = palette.highlightBorder,
                shape = RoundedCornerShape(22.dp),
            )
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            lines.forEach { line ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.padding(top = 8.dp)
                            .width(6.dp)
                            .height(6.dp)
                            .background(
                                color = palette.bulletColor,
                                shape = CircleShape,
                            )
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }
        }
    }
}

private object ShowcasePagePalettes {
    val dark = ShowcasePagePalette(
        textPrimary = Color(0xFFE8EEF8),
        textSecondary = Color(0xFFB6C6DA),
        textMuted = Color(0xFF8C9DB3),
        bulletColor = Color(0xFF8DC7FF),
        sidebarBadgeBackground = Color(0x33223753),
        metricGradientTop = Color(0x88202B3E),
        metricGradientBottom = Color(0x66111B2B),
        metricBorder = Color.White.copy(alpha = 0.06f),
        factBackground = Color.White.copy(alpha = 0.03f),
        highlightBackground = Color.White.copy(alpha = 0.03f),
        highlightBorder = Color.White.copy(alpha = 0.05f),
    )

    val light = ShowcasePagePalette(
        textPrimary = Color(0xFF1F2937),
        textSecondary = Color(0xFF4B5563),
        textMuted = Color(0xFF6B7280),
        bulletColor = Color(0xFF2C8BF2),
        sidebarBadgeBackground = Color(0xFFF8FAFC),
        metricGradientTop = Color(0xFFFFFFFF),
        metricGradientBottom = Color(0xFFF8FBFF),
        metricBorder = Color(0xFFE5ECF3),
        factBackground = Color(0xFFF8FAFC),
        highlightBackground = Color(0xFFFFFFFF),
        highlightBorder = Color(0xFFE5ECF3),
    )
}
