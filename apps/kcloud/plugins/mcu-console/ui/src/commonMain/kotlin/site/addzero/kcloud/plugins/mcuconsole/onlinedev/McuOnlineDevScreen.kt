package site.addzero.kcloud.plugins.mcuconsole.onlinedev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuAtomicCommandDefinition
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExample
import site.addzero.kcloud.plugins.mcuconsole.McuWidgetBinding
import site.addzero.kcloud.plugins.mcuconsole.McuWidgetFieldKind
import site.addzero.kcloud.plugins.mcuconsole.McuWidgetTemplate
import site.addzero.kcloud.plugins.mcuconsole.McuWidgetTemplateKind
import site.addzero.kcloud.plugins.mcuconsole.workbench.*
import site.addzero.kcloud.plugins.mcuconsole.workbench.cupertino.*

@Route(
    value = "开发工具",
    title = "在线开发",
    routePath = "mcu/online-dev",
    icon = "Code",
    order = 15.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "设备",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuOnlineDevScreen() {
    val state: McuConsoleWorkbenchState = koinInject()
    val workbenchState = rememberMcuWorkbenchState(state)
    val runAction = rememberMcuActionRunner()

    McuCupertinoScene {
        McuWorkbenchFrame(
            state = workbenchState,
            actions = {
                McuCupertinoSecondaryButton(
                    text = "刷新",
                    onClick = {
                        runAction {
                            workbenchState.refreshAll()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "确保运行时",
                    enabled = workbenchState.hasActiveSession && workbenchState.selectedRuntimeBundle != null,
                    onClick = {
                        runAction {
                            workbenchState.ensureRuntime(forceReflash = false)
                        }
                    },
                )
                McuCupertinoPrimaryButton(
                    text = "执行脚本",
                    enabled = workbenchState.hasActiveSession && workbenchState.isRuntimeReady && !workbenchState.isScriptRunning,
                    onClick = {
                        runAction {
                            workbenchState.executeScript()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "停止脚本",
                    enabled = workbenchState.hasActiveSession && workbenchState.isScriptRunning,
                    onClick = {
                        runAction {
                            workbenchState.stopScript()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "清空日志",
                    onClick = {
                        workbenchState.clearVisibleEvents()
                    },
                )
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuPanel(
                    title = "Runtime / 模板",
                    modifier = Modifier.width(360.dp).fillMaxHeight(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            McuRuntimeBundleBrowser(
                                bundles = workbenchState.runtimeBundles,
                                selectedBundleId = workbenchState.selectedRuntimeBundleId,
                                onSelect = { bundleId -> workbenchState.selectRuntimeBundle(bundleId) },
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                            )
                        }
                        item {
                            McuSummaryTable(
                                rows = listOf(
                                    "Bundle" to (workbenchState.runtimeStatus.bundleTitle ?: workbenchState.selectedRuntimeBundle?.title.orEmpty()),
                                    "运行时" to workbenchState.runtimeStatus.state.name,
                                    "语言" to workbenchState.scriptLanguage,
                                    "Profile" to (workbenchState.runtimeStatus.defaultFlashProfileId ?: workbenchState.selectedRuntimeBundle?.defaultFlashProfileId.orEmpty()),
                                ),
                            )
                        }
                        item {
                            McuAtomicCommandSection(
                                commands = workbenchState.selectedRuntimeBundle?.atomicCommands.orEmpty(),
                                selectedCommandId = workbenchState.selectedAtomicCommandId,
                                onSelect = { commandId -> workbenchState.selectAtomicCommand(commandId) },
                            )
                        }
                        item {
                            McuScriptExampleSection(
                                examples = workbenchState.selectedRuntimeBundle?.scriptExamples.orEmpty(),
                                selectedExampleId = workbenchState.selectedScriptExampleId,
                                onSelect = { exampleId -> workbenchState.selectScriptExample(exampleId) },
                            )
                        }
                        item {
                            McuWidgetTemplateSection(
                                templates = workbenchState.selectedRuntimeBundle?.widgetTemplates.orEmpty(),
                                onAdd = { templateId -> workbenchState.addWidgetFromTemplate(templateId) },
                            )
                        }
                    }
                }

                McuPanel(
                    title = "控件面板",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    if (workbenchState.widgetInstances.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "暂无控件实例",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(
                                items = workbenchState.widgetInstances,
                                key = { widget -> widget.instanceId },
                            ) { widget ->
                                McuWidgetInstanceCard(
                                    widget = widget,
                                    canExecute = workbenchState.canUseWidgets && !workbenchState.isScriptRunning,
                                    scriptPreview = workbenchState.previewWidgetScript(widget.instanceId),
                                    onExecute = {
                                        runAction {
                                            workbenchState.executeWidget(widget.instanceId)
                                        }
                                    },
                                    onRemove = { workbenchState.removeWidgetInstance(widget.instanceId) },
                                    onValueChange = { key, value ->
                                        workbenchState.updateWidgetValue(widget.instanceId, key, value)
                                    },
                                )
                            }
                        }
                    }
                }

                McuPanel(
                    title = "脚本 / 结果",
                    modifier = Modifier.width(420.dp).fillMaxHeight(),
                ) {
                    McuCupertinoField(
                        value = workbenchState.timeoutMsText,
                        onValueChange = { workbenchState.timeoutMsText = it },
                        label = "timeoutMs",
                    )
                    McuCupertinoSummarySection(
                        rows = listOf(
                            "语言" to workbenchState.scriptLanguage,
                            "会话" to if (workbenchState.session.isOpen) "OPEN" else "CLOSED",
                            "运行时" to workbenchState.runtimeStatus.state.name,
                            "脚本状态" to workbenchState.scriptStatus.state.name,
                            "Frame" to workbenchState.scriptStatus.lastFrameType.orEmpty(),
                        ),
                    )
                    McuCupertinoField(
                        value = workbenchState.scriptText,
                        onValueChange = { workbenchState.scriptText = it },
                        label = workbenchState.scriptLanguage,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        singleLine = false,
                        minLines = 8,
                        maxLines = 18,
                    )
                    McuScriptPreview(
                        script = listOfNotNull(
                            state.scriptStatus.lastMessage,
                            state.scriptStatus.lastPayload?.toString(),
                        ).joinToString("\n").ifBlank { "-" },
                    )
                    McuEventFeed(
                        events = state.events.filter { event ->
                            event.kind == McuEventKind.TX_FRAME ||
                                event.kind == McuEventKind.RX_FRAME ||
                                event.kind == McuEventKind.LOG ||
                                event.kind == McuEventKind.ERROR
                        }.takeLast(60),
                        modifier = Modifier.weight(0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun McuAtomicCommandSection(
    commands: List<McuAtomicCommandDefinition>,
    selectedCommandId: String?,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "原子指令",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (commands.isEmpty()) {
            Text(
                text = "暂无指令",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        commands.forEach { command ->
            McuSelectableItem(
                title = command.title,
                subtitle = command.signature,
                detail = command.description,
                selected = command.id == selectedCommandId,
                onClick = { onSelect(command.id) },
            )
        }
    }
}

@Composable
private fun McuScriptExampleSection(
    examples: List<McuScriptExample>,
    selectedExampleId: String?,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "示例脚本",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (examples.isEmpty()) {
            Text(
                text = "暂无示例",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        examples.forEach { example ->
            McuSelectableItem(
                title = example.title,
                subtitle = example.language,
                detail = example.description,
                selected = example.id == selectedExampleId,
                onClick = { onSelect(example.id) },
            )
        }
    }
}

@Composable
private fun McuWidgetTemplateSection(
    templates: List<McuWidgetTemplate>,
    onAdd: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "控件模板",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (templates.isEmpty()) {
            Text(
                text = "暂无模板",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        templates.forEach { template ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = template.description.ifBlank { template.kind.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    McuCupertinoSecondaryButton(
                        text = "添加",
                        onClick = { onAdd(template.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun McuSelectableItem(
    title: String,
    subtitle: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.76f),
            )
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun McuWidgetInstanceCard(
    widget: McuWidgetInstanceState,
    canExecute: Boolean,
    scriptPreview: String,
    onExecute: () -> Unit,
    onRemove: () -> Unit,
    onValueChange: (String, String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "${widget.kind.name} / ${widget.language}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                McuCupertinoPrimaryButton(
                    text = "执行",
                    enabled = canExecute,
                    onClick = { if (canExecute) onExecute() },
                )
                McuCupertinoSecondaryButton(
                    text = "移除",
                    onClick = onRemove,
                )
            }
            widget.bindings.forEach { binding ->
                McuWidgetBindingField(
                    widgetKind = widget.kind,
                    binding = binding,
                    value = widget.values[binding.key].orEmpty(),
                    onValueChange = { nextValue ->
                        onValueChange(binding.key, nextValue)
                    },
                )
            }
            McuScriptPreview(script = scriptPreview)
            if (!widget.lastFrameType.isNullOrBlank() || !widget.lastMessage.isNullOrBlank()) {
                McuSummaryTable(
                    rows = listOf(
                        "Frame" to widget.lastFrameType.orEmpty(),
                        "消息" to widget.lastMessage.orEmpty(),
                        "Payload" to widget.lastPayloadText.orEmpty(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun McuWidgetBindingField(
    widgetKind: McuWidgetTemplateKind,
    binding: McuWidgetBinding,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when {
        binding.fieldKind == McuWidgetFieldKind.BOOLEAN -> {
            McuCupertinoSwitchRow(
                label = binding.label,
                checked = value.equals("true", ignoreCase = true),
                onCheckedChange = { checked ->
                    onValueChange(checked.toString())
                },
            )
        }

        widgetKind == McuWidgetTemplateKind.PWM_SLIDER &&
            binding.fieldKind == McuWidgetFieldKind.NUMBER &&
            binding.min != null &&
            binding.max != null -> {
            val minValue = binding.min!!.toFloat()
            val maxValue = binding.max!!.toFloat()
            val sliderValue = value.toFloatOrNull() ?: binding.defaultValue.toFloatOrNull() ?: 0f
            McuCupertinoSliderField(
                label = binding.label,
                value = sliderValue,
                valueRange = minValue..maxValue,
                onValueChange = { nextValue ->
                    onValueChange(nextValue.toInt().toString())
                },
            )
        }

        else -> {
            McuCupertinoField(
                value = value,
                onValueChange = onValueChange,
                label = binding.label,
                singleLine = binding.fieldKind != McuWidgetFieldKind.MULTILINE,
                maxLines = if (binding.fieldKind == McuWidgetFieldKind.MULTILINE) 5 else 1,
                supportingText = binding.placeholder.takeIf { it.isNotBlank() },
            )
        }
    }
}
