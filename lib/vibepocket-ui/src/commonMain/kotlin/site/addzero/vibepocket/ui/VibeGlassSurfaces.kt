package site.addzero.vibepocket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun VibeGlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibeGlassTheme.palette.midnight),
        content = content,
    )
}

@Composable
fun VibeGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = VibeGlassTheme.palette
    val panelShape = shape ?: VibeGlassTheme.shapes.panel
    Surface(
        modifier = modifier,
        shape = panelShape,
        color = palette.panelTop,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .clip(panelShape)
                .border(1.dp, palette.panelEdge, panelShape)
                .background(palette.panelTop)
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun VibeGlassScreenPanel(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = VibeGlassTheme.palette
    VibeGlassPanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (badge != null) {
                    VibeGlassTag(text = badge, accent = palette.aqua)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.inkSoft,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        content()
    }
}

@Composable
fun VibeGlassTag(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val palette = VibeGlassTheme.palette
    Row(
        modifier = modifier
            .clip(VibeGlassTheme.shapes.pill)
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.24f), VibeGlassTheme.shapes.pill)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = palette.inkSoft,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun VibeGlassMetricCard(
    label: String,
    value: String,
    supporting: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val palette = VibeGlassTheme.palette
    VibeGlassPanel(
        modifier = modifier,
        shape = VibeGlassTheme.shapes.control,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.inkMuted,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accent,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.inkSoft,
        )
    }
}

@Composable
fun VibeGlassEmptyState(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = VibeGlassTheme.palette
    VibeGlassPanel(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 34.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = icon, style = MaterialTheme.typography.displayLarge)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.ink,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.inkSoft,
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(8.dp))
                action()
            }
        }
    }
}
