package site.addzero.liquiddemo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    spec: LiquidGlassSpec = LiquidGlassDefaults.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .liquidGlassSurface(spec)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
fun LiquidGlassCardHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = LiquidGlassDefaults.textSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (badge != null) {
            Text(
                text = badge,
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .liquidGlassSurface(LiquidGlassDefaults.sidebarItemSelected)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
