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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                color = ShowcasePageTokens.textMuted,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = ShowcasePageTokens.textPrimary,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = ShowcasePageTokens.textSecondary,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ShowcasePageTokens.textPrimary,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = ShowcasePageTokens.textSecondary,
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
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = ShowcasePageTokens.sidebarBadgeBackground,
                shape = RoundedCornerShape(18.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.07f),
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
                color = ShowcasePageTokens.textMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = ShowcasePageTokens.textPrimary,
            )
        }
    }
}

@Composable
private fun ShowcaseMetricCard(
    label: String,
    value: String,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x88202B3E),
                        Color(0x66111B2B),
                    ),
                ),
                shape = RoundedCornerShape(22.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.06f),
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
                color = ShowcasePageTokens.textMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ShowcasePageTokens.textPrimary,
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
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ShowcasePageTokens.textMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = ShowcasePageTokens.textPrimary,
        )
    }
}

@Composable
private fun ShowcaseHighlightsCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(22.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
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
                color = ShowcasePageTokens.textPrimary,
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
                                color = ShowcasePageTokens.bulletColor,
                                shape = CircleShape,
                            )
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShowcasePageTokens.textSecondary,
                    )
                }
            }
        }
    }
}

private object ShowcasePageTokens {
    val textPrimary = Color(0xFFE8EEF8)
    val textSecondary = Color(0xFFB6C6DA)
    val textMuted = Color(0xFF8C9DB3)
    val bulletColor = Color(0xFF8DC7FF)
    val sidebarBadgeBackground = Color(0x33223753)
}
