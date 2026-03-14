package site.addzero.vibepocket.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class VibeGlassButtonStyle {
    Primary,
    Secondary,
    Ghost,
}

@Composable
fun VibeGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: VibeGlassButtonStyle = VibeGlassButtonStyle.Primary,
    enabled: Boolean = true,
    leading: String? = null,
) {
    val palette = VibeGlassTheme.palette
    val buttonContent: @Composable RowScope.() -> Unit = {
        if (leading != null) {
            Text(
                text = leading,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }

    when (style) {
        VibeGlassButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = VibeGlassTheme.shapes.control,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.aqua,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            content = buttonContent,
        )

        VibeGlassButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = VibeGlassTheme.shapes.control,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = palette.coral,
            ),
            border = BorderStroke(1.dp, palette.coral.copy(alpha = 0.4f)),
            content = buttonContent,
        )

        VibeGlassButtonStyle.Ghost -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = VibeGlassTheme.shapes.control,
            colors = ButtonDefaults.textButtonColors(
                contentColor = palette.inkSoft,
            ),
            content = buttonContent,
        )
    }
}

@Composable
fun VibeGlassChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: String? = null,
) {
    val palette = VibeGlassTheme.palette
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = buildString {
                    if (leading != null) {
                        append(leading)
                        append(' ')
                    }
                    append(label)
                },
            )
        },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = palette.aqua.copy(alpha = 0.12f),
            selectedLabelColor = palette.aqua,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = palette.inkSoft,
        ),
    )
}

@Composable
fun RowScope.VibeGlassToolbarButton(
    text: String,
    onClick: () -> Unit,
    leading: String? = null,
    modifier: Modifier = Modifier,
) {
    VibeGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = VibeGlassButtonStyle.Ghost,
        leading = leading,
    )
}
