@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.codegencontext.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoActionSheet
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.cancel
import io.github.robinpcrd.cupertino.default
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.form.WorkbenchBorderedTextField
import site.addzero.cupertino.workbench.form.WorkbenchSwitch

data class CodegenOption<T>(
    val value: T,
    val label: String,
    val caption: String? = null,
)

enum class CodegenFormSpan {
    HALF,
    FULL,
}

internal data class CodegenFormItem(
    val span: CodegenFormSpan,
    val content: @Composable () -> Unit,
)

class CodegenFormGridScope internal constructor() {
    internal val items = mutableListOf<CodegenFormItem>()

    fun item(
        span: CodegenFormSpan = CodegenFormSpan.HALF,
        content: @Composable () -> Unit,
    ) {
        items += CodegenFormItem(span = span, content = content)
    }

    fun fullWidth(
        content: @Composable () -> Unit,
    ) {
        item(span = CodegenFormSpan.FULL, content = content)
    }
}

@Composable
fun CodegenPanel(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    CupertinoSurface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, CupertinoTheme.colorScheme.separator.copy(alpha = 0.35f)),
                shape = CupertinoTheme.shapes.large,
            ),
        color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
        shape = CupertinoTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CupertinoText(text = title, style = CupertinoTheme.typography.title3)
                    subtitle?.takeIf(String::isNotBlank)?.let { text ->
                        CupertinoText(
                            text = text,
                            style = CupertinoTheme.typography.footnote,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actions,
                )
            }
            content()
        }
    }
}

@Composable
fun CodegenStatusStrip(
    text: String,
    modifier: Modifier = Modifier,
    tone: Color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = tone,
                shape = CupertinoTheme.shapes.medium,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        CupertinoText(
            text = text,
            style = CupertinoTheme.typography.footnote,
        )
    }
}

@Composable
fun CodegenFormGrid(
    modifier: Modifier = Modifier,
    twoColumnMinWidth: Dp = 720.dp,
    horizontalGap: Dp = 12.dp,
    verticalGap: Dp = 12.dp,
    content: CodegenFormGridScope.() -> Unit,
) {
    val items = CodegenFormGridScope().apply(content).items.toList()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val singleColumn = maxWidth < twoColumnMinWidth

        Column(
            verticalArrangement = Arrangement.spacedBy(verticalGap),
        ) {
            if (singleColumn) {
                items.forEach { item ->
                    item.content()
                }
            } else {
                var pendingHalf: CodegenFormItem? = null
                items.forEach { item ->
                    when (item.span) {
                        CodegenFormSpan.FULL -> {
                            pendingHalf?.let { left ->
                                CodegenFormHalfRow(
                                    left = left,
                                    right = null,
                                    horizontalGap = horizontalGap,
                                )
                                pendingHalf = null
                            }
                            item.content()
                        }

                        CodegenFormSpan.HALF -> {
                            val left = pendingHalf
                            if (left == null) {
                                pendingHalf = item
                            } else {
                                CodegenFormHalfRow(
                                    left = left,
                                    right = item,
                                    horizontalGap = horizontalGap,
                                )
                                pendingHalf = null
                            }
                        }
                    }
                }
                pendingHalf?.let { left ->
                    CodegenFormHalfRow(
                        left = left,
                        right = null,
                        horizontalGap = horizontalGap,
                    )
                }
            }
        }
    }
}

@Composable
private fun CodegenFormHalfRow(
    left: CodegenFormItem,
    right: CodegenFormItem?,
    horizontalGap: Dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.weight(1f),
        ) {
            left.content()
        }
        if (right == null) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                right.content()
            }
        }
    }
}

@Composable
fun CodegenTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    description: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CupertinoText(
            text = label,
            style = CupertinoTheme.typography.subhead,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        WorkbenchBorderedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            maxLines = if (singleLine) 1 else 5,
            placeholder = if (placeholder.isBlank()) {
                null
            } else {
                { CupertinoText(placeholder) }
            },
        )
        description?.takeIf(String::isNotBlank)?.let { text ->
            CupertinoText(
                text = text,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }
    }
}

@Composable
fun CodegenBooleanField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    CupertinoSurface(
        modifier = modifier.fillMaxWidth(),
        color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
        shape = CupertinoTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CupertinoText(text = label, style = CupertinoTheme.typography.body)
                description?.takeIf(String::isNotBlank)?.let { text ->
                    CupertinoText(
                        text = text,
                        style = CupertinoTheme.typography.footnote,
                        color = CupertinoTheme.colorScheme.secondaryLabel,
                    )
                }
            }
            WorkbenchSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
fun <T> CodegenSelectionField(
    label: String,
    options: List<CodegenOption<T>>,
    selectedValue: T?,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请选择",
    allowClear: Boolean = false,
    description: String? = null,
) {
    var sheetVisible by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { option -> option.value == selectedValue }
    val selectedLabel = selectedOption?.label.orEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CupertinoText(
            text = label,
            style = CupertinoTheme.typography.subhead,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        CupertinoBorderedTextField(
            value = selectedLabel,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            placeholder = { CupertinoText(placeholder) },
            trailingIcon = {
                WorkbenchActionButton(
                    text = if (selectedLabel.isBlank()) "选择" else "变更",
                    onClick = { sheetVisible = true },
                    variant = WorkbenchButtonVariant.Outline,
                )
            },
        )
        selectedOption?.caption?.takeIf(String::isNotBlank)?.let { caption ->
            CupertinoText(
                text = caption,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }
        description?.takeIf(String::isNotBlank)?.let { text ->
            CupertinoText(
                text = text,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }
    }

    CupertinoActionSheet(
        visible = sheetVisible,
        onDismissRequest = { sheetVisible = false },
        title = { CupertinoText(label) },
        buttons = {
            options.forEach { option ->
                default(
                    onClick = {
                        onSelected(option.value)
                        sheetVisible = false
                    },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CupertinoText(option.label)
                        option.caption?.takeIf(String::isNotBlank)?.let { caption ->
                            CupertinoText(
                                text = caption,
                                style = CupertinoTheme.typography.footnote,
                                color = CupertinoTheme.colorScheme.secondaryLabel,
                            )
                        }
                    }
                }
            }
            if (allowClear) {
                default(
                    onClick = {
                        onSelected(null)
                        sheetVisible = false
                    },
                ) {
                    CupertinoText("清空")
                }
            }
            cancel(onClick = { sheetVisible = false }) {
                CupertinoText("取消")
            }
        },
    )
}
