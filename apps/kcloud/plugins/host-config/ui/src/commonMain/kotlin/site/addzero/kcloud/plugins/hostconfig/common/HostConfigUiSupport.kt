@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.robinpcrd.cupertino.CupertinoActionSheet
import io.github.robinpcrd.cupertino.CupertinoBorderedTextField
import io.github.robinpcrd.cupertino.CupertinoHorizontalDivider
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.cancel
import io.github.robinpcrd.cupertino.default
import io.github.robinpcrd.cupertino.destructive
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.form.WorkbenchBorderedTextField
import site.addzero.cupertino.workbench.form.WorkbenchSwitch
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

enum class HostConfigNodeKind {
    PROJECT,
    PROTOCOL,
    MODULE,
    DEVICE,
    TAG,
}

data class HostConfigTreeNode(
    val id: String,
    val kind: HostConfigNodeKind,
    val projectId: Long,
    val entityId: Long,
    val parentEntityId: Long? = null,
    val label: String,
    val caption: String? = null,
    val children: List<HostConfigTreeNode> = emptyList(),
)

data class HostConfigOption<T>(
    val value: T,
    val label: String,
    val caption: String? = null,
)

fun HostConfigNodeKind.icon(): ImageVector {
    return when (this) {
        HostConfigNodeKind.PROJECT -> Icons.Outlined.SettingsApplications
        HostConfigNodeKind.PROTOCOL -> Icons.Outlined.CloudQueue
        HostConfigNodeKind.MODULE -> Icons.Outlined.Memory
        HostConfigNodeKind.DEVICE -> Icons.Outlined.DeviceHub
        HostConfigNodeKind.TAG -> Icons.Outlined.Tag
    }
}

fun Parity.label(): String {
    return when (this) {
        Parity.NONE -> "无校验"
        Parity.ODD -> "奇校验"
        Parity.EVEN -> "偶校验"
    }
}

fun PointType.label(): String {
    return when (this) {
        PointType.NORMAL -> "普通点"
        PointType.PULSE -> "脉冲点"
    }
}

fun TransportType.label(): String {
    return when (this) {
        TransportType.TCP -> "TCP"
        TransportType.RTU -> "RTU"
    }
}

fun ByteOrder2.label(): String {
    return when (this) {
        ByteOrder2.ORDER_21 -> "21"
        ByteOrder2.ORDER_12 -> "12"
    }
}

fun ByteOrder4.label(): String {
    return when (this) {
        ByteOrder4.ORDER_4321 -> "4321"
        ByteOrder4.ORDER_1234 -> "1234"
        ByteOrder4.ORDER_2143 -> "2143"
        ByteOrder4.ORDER_3412 -> "3412"
    }
}

fun FloatOrder.label(): String {
    return when (this) {
        FloatOrder.ORDER_4321 -> "4321"
        FloatOrder.ORDER_1234 -> "1234"
        FloatOrder.ORDER_2143 -> "2143"
        FloatOrder.ORDER_3412 -> "3412"
    }
}

fun String?.orDash(): String {
    return this?.takeIf { it.isNotBlank() } ?: "未设置"
}

fun Long?.toSizeLabel(): String {
    val size = this ?: return "-"
    if (size < 1024) {
        return "$size B"
    }
    val kiloBytes = size / 1024.0
    if (kiloBytes < 1024) {
        return "${kiloBytes.toSingleDecimalLabel()} KB"
    }
    val megaBytes = kiloBytes / 1024.0
    return "${megaBytes.toSingleDecimalLabel()} MB"
}

private fun Double.toSingleDecimalLabel(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return rounded.toString()
}

fun List<HostConfigTreeNode>.findNode(
    nodeId: String?,
): HostConfigTreeNode? {
    if (nodeId == null) {
        return null
    }
    fun search(nodes: List<HostConfigTreeNode>): HostConfigTreeNode? {
        nodes.forEach { node ->
            if (node.id == nodeId) {
                return node
            }
            val child = search(node.children)
            if (child != null) {
                return child
            }
        }
        return null
    }
    return search(this)
}

@Composable
fun HostConfigPanel(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
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
                    CupertinoText(
                        text = title,
                        style = CupertinoTheme.typography.title3,
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let { text ->
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
fun HostConfigKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CupertinoText(
            text = label,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        CupertinoText(
            text = value,
        )
    }
}

@Composable
fun HostConfigSectionTitle(
    text: String,
) {
    CupertinoText(
        text = text,
        style = CupertinoTheme.typography.headline,
    )
}

@Composable
fun HostConfigStatusStrip(
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
fun HostConfigDialog(
    title: String,
    onDismissRequest: () -> Unit,
    width: Dp = 760.dp,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        CupertinoSurface(
            modifier = Modifier.widthIn(max = width),
            color = CupertinoTheme.colorScheme.systemGroupedBackground,
            shape = CupertinoTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CupertinoText(
                    text = title,
                    style = CupertinoTheme.typography.title2,
                )
                CupertinoHorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 620.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
                CupertinoHorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
fun HostConfigTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
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
                {
                    CupertinoText(placeholder)
                }
            },
        )
    }
}

@Composable
fun HostConfigBooleanField(
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
                CupertinoText(
                    text = label,
                    style = CupertinoTheme.typography.body,
                )
                description?.takeIf { it.isNotBlank() }?.let { text ->
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
fun <T> HostConfigSelectionField(
    label: String,
    options: List<HostConfigOption<T>>,
    selectedValue: T?,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请选择",
    allowClear: Boolean = false,
) {
    var sheetVisible by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selectedValue }?.label.orEmpty()

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
            placeholder = {
                CupertinoText(placeholder)
            },
            trailingIcon = {
                WorkbenchActionButton(
                    text = if (selectedLabel.isBlank()) "选择" else "变更",
                    onClick = {
                        sheetVisible = true
                    },
                    variant = WorkbenchButtonVariant.Outline,
                )
            },
        )
    }

    CupertinoActionSheet(
        visible = sheetVisible,
        onDismissRequest = {
            sheetVisible = false
        },
        title = {
            CupertinoText(label)
        },
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
                        option.caption?.takeIf { it.isNotBlank() }?.let { text ->
                            CupertinoText(
                                text = text,
                                style = CupertinoTheme.typography.footnote,
                                color = CupertinoTheme.colorScheme.secondaryLabel,
                            )
                        }
                    }
                }
            }
            if (allowClear) {
                destructive(
                    onClick = {
                        onSelected(null)
                        sheetVisible = false
                    },
                ) {
                    CupertinoText("清空")
                }
            }
            cancel(
                onClick = {
                    sheetVisible = false
                },
            ) {
                CupertinoText("取消")
            }
        },
    )
}
