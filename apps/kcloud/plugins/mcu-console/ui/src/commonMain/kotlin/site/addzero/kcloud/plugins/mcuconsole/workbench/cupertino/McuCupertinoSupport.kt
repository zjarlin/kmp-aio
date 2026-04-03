package site.addzero.kcloud.plugins.mcuconsole.workbench.cupertino

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.cupertino.ExperimentalCupertinoApi
import io.github.alexzhirkevich.cupertino.CupertinoBorderedTextField
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveSlider
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTextButton
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveSwitch
import io.github.alexzhirkevich.cupertino.adaptive.CupertinoThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.MaterialThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.Theme as AdaptiveThemeTarget
import io.github.alexzhirkevich.cupertino.section.CupertinoSection
import io.github.alexzhirkevich.cupertino.section.ProvideSectionStyle
import io.github.alexzhirkevich.cupertino.section.SectionItem
import io.github.alexzhirkevich.cupertino.section.SectionStyle

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun McuCupertinoScene(
    content: @Composable () -> Unit,
) {
    AdaptiveTheme(
        target = AdaptiveThemeTarget.Cupertino,
        material = MaterialThemeSpec.Default(),
        cupertino = CupertinoThemeSpec.Default(),
        content = content,
    )
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun McuCupertinoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AdaptiveButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 38.dp),
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun McuCupertinoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AdaptiveTextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 38.dp),
    ) {
        Text(text)
    }
}

@Composable
internal fun McuCupertinoField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    supportingText: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        CupertinoBorderedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            placeholder = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
        )
        supportingText
            ?.takeIf { it.isNotBlank() }
            ?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (singleLine) 1 else 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun McuCupertinoSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        AdaptiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
internal fun McuCupertinoSliderField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "$label: ${value.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AdaptiveSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
internal fun McuCupertinoSummarySection(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    ProvideSectionStyle(SectionStyle.InsetGrouped) {
        CupertinoSection(
            modifier = modifier.fillMaxWidth(),
        ) {
            rows.forEach { (label, value) ->
                SectionItem(
                    title = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingContent = {
                        Text(
                            text = value.ifBlank { "-" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = if (
                                value.contains('/') ||
                                    value.contains('\\') ||
                                    value.contains('{') ||
                                    value.contains(':')
                            ) {
                                FontFamily.Monospace
                            } else {
                                FontFamily.Default
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}
