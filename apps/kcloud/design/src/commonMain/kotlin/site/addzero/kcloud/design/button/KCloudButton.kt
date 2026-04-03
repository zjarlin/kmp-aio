package site.addzero.kcloud.design.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import site.addzero.themes.radius
import site.addzero.component.Button as ShadcnButton
import site.addzero.component.ButtonSize as ShadcnButtonSize
import site.addzero.component.ButtonVariant as ShadcnButtonVariant

enum class KCloudButtonVariant {
    Default,
    Destructive,
    Outline,
    Secondary,
    Ghost,
    Link,
}

enum class KCloudButtonSize {
    Default,
    Sm,
    Lg,
    Icon,
}

@Composable
fun KCloudButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KCloudButtonVariant = KCloudButtonVariant.Default,
    size: KCloudButtonSize = KCloudButtonSize.Default,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
    content: @Composable RowScope.() -> Unit,
) {
    ShadcnButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant.toShadcnVariant(),
        size = size.toShadcnSize(),
        enabled = enabled,
        shape = shape,
        content = content,
    )
}

@Composable
fun KCloudPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KCloudButtonVariant = KCloudButtonVariant.Default,
    size: KCloudButtonSize = KCloudButtonSize.Default,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    KCloudButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        content = content,
    )
}

@Composable
fun KCloudOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
    content: @Composable RowScope.() -> Unit,
) {
    KCloudButton(
        onClick = onClick,
        modifier = modifier,
        variant = KCloudButtonVariant.Outline,
        enabled = enabled,
        shape = shape,
        content = content,
    )
}

@Composable
fun KCloudFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
    content: @Composable RowScope.() -> Unit,
) {
    KCloudButton(
        onClick = onClick,
        modifier = modifier,
        variant = KCloudButtonVariant.Secondary,
        enabled = enabled,
        shape = shape,
        content = content,
    )
}

@Composable
fun KCloudTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    KCloudButton(
        onClick = onClick,
        modifier = modifier,
        variant = KCloudButtonVariant.Ghost,
        enabled = enabled,
        content = content,
    )
}

@Composable
fun KCloudActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    variant: KCloudButtonVariant = KCloudButtonVariant.Default,
    size: KCloudButtonSize = KCloudButtonSize.Default,
    enabled: Boolean = true,
    pill: Boolean = false,
) {
    val buttonContent: @Composable RowScope.() -> Unit = {
        imageVector?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }

    if (pill) {
        KCloudPillButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = size,
            enabled = enabled,
            content = buttonContent,
        )
    } else {
        KCloudButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = size,
            enabled = enabled,
            content = buttonContent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KCloudIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    variant: KCloudButtonVariant = KCloudButtonVariant.Ghost,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val anchor: @Composable () -> Unit = {
        KCloudPillButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = KCloudButtonSize.Icon,
            enabled = enabled,
            content = content,
        )
    }

    if (tooltip.isNullOrBlank()) {
        anchor()
    } else {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(tooltip)
                }
            },
            state = rememberTooltipState(),
        ) {
            anchor()
        }
    }
}

private fun KCloudButtonVariant.toShadcnVariant(): ShadcnButtonVariant =
    when (this) {
        KCloudButtonVariant.Default -> ShadcnButtonVariant.Default
        KCloudButtonVariant.Destructive -> ShadcnButtonVariant.Destructive
        KCloudButtonVariant.Outline -> ShadcnButtonVariant.Outline
        KCloudButtonVariant.Secondary -> ShadcnButtonVariant.Secondary
        KCloudButtonVariant.Ghost -> ShadcnButtonVariant.Ghost
        KCloudButtonVariant.Link -> ShadcnButtonVariant.Link
    }

private fun KCloudButtonSize.toShadcnSize(): ShadcnButtonSize =
    when (this) {
        KCloudButtonSize.Default -> ShadcnButtonSize.Default
        KCloudButtonSize.Sm -> ShadcnButtonSize.Sm
        KCloudButtonSize.Lg -> ShadcnButtonSize.Lg
        KCloudButtonSize.Icon -> ShadcnButtonSize.Icon
    }
