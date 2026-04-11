package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoSegmentedControl
import io.github.robinpcrd.cupertino.CupertinoSegmentedControlTab
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataResolvedFunctionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataResolvedPropertyDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextWorkbenchTab
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextDeviceFunctionItemActionsSpi
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextDeviceFunctionsHeaderSpi
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextThingPropertiesHeaderSpi
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextThingPropertyItemActionsSpi
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextWorkbenchTabSwitchSpi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
internal fun WorkbenchTabsPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val tabSwitchSpi = koinInject<CodegenContextWorkbenchTabSwitchSpi>()
    val tabs =
        listOf(
            CodegenContextWorkbenchTab.DEVICE_FUNCTIONS to "设备功能",
            CodegenContextWorkbenchTab.THING_PROPERTIES to "物模型字段",
        )
    CupertinoPanel(
        title = "建模工作区",
        subtitle = "这里只编辑设备功能和物模型字段两个维度，不再混入生成参数配置。",
    ) {
        tabSwitchSpi.Render(
            tabs = tabs,
            state = state,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun MetadataPreviewSummaryPanel(
    state: CodegenContextScreenState,
) {
    val preview = state.preview
    CupertinoPanel(
        title = "解析预览",
        subtitle = "这里展示后端按当前草稿解析出的结果、问题和导出计划，不需要再去翻生成文件。",
    ) {
        when {
            state.previewing -> CupertinoStatusStrip("正在根据当前草稿重新解析 metadata …")
            state.previewErrorMessage != null -> CupertinoStatusStrip(state.previewErrorMessage, tone = Color(0xFFFFE8E6))
            preview == null -> CupertinoStatusStrip("当前还没有解析预览。")
            else -> {
                if (preview.issues.isEmpty() && preview.exportPlans.isEmpty()) {
                    CupertinoStatusStrip("当前草稿已完成解析，暂时没有额外问题。")
                }
                if (preview.issues.isNotEmpty()) {
                    CupertinoPanel(title = "预检问题", subtitle = "这里显示会影响导出或命名推导的校验结果。") {
                        preview.issues.forEach { issue ->
                            CupertinoStatusStrip(
                                text = "${issue.location}: ${issue.message}",
                                tone = issueTone(issue.severity),
                            )
                        }
                    }
                }
                if (preview.exportPlans.isNotEmpty()) {
                    CupertinoPanel(title = "导出计划", subtitle = "这里显示当前草稿理论上会产出哪些目标。") {
                        preview.exportPlans.forEach { plan ->
                            val tone = if (plan.ready) Color(0xFFEAF4FF) else Color(0xFFFFF5D6)
                            val transport = plan.transport?.name ?: "-"
                            val readiness = if (plan.ready) "可导出" else "待补齐"
                            CupertinoStatusStrip(
                                text = "$readiness / $transport / ${plan.artifactKind} · ${plan.summary}",
                                tone = tone,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceFunctionsPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val headerActionsSpi = koinInject<CodegenContextDeviceFunctionsHeaderSpi>()
    val itemActionsSpi = koinInject<CodegenContextDeviceFunctionItemActionsSpi>()
    val draft = state.draft
    val definitions = state.availableContextDefinitions
    val methodDefinitions = definitions.filter { definition -> definition.targetKind == CodegenNodeKind.METHOD }
    val resolvedFunctions = state.preview?.resolvedFunctions.orEmpty().associateBy(CodegenMetadataResolvedFunctionDto::key)
    CupertinoPanel(
        title = "设备功能",
        subtitle = "这里只维护功能定义、字段关联和协议上下文；命名推导与导出细节交给后端。",
        actions = {
            headerActionsSpi.Render(viewModel = viewModel)
        },
    ) {
        if (draft.deviceFunctions.isEmpty()) {
            CupertinoStatusStrip("当前还没有设备功能。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            draft.deviceFunctions.forEachIndexed { index, function ->
                CupertinoPanel(
                    title = function.name.ifBlank { "未命名设备功能" },
                    subtitle = "key=${function.key}",
                    actions = {
                        itemActionsSpi.Render(index = index, viewModel = viewModel)
                    },
                ) {
                    CupertinoFormGrid {
                        item {
                            CupertinoTextField(
                                label = "功能名称",
                                value = function.name,
                                onValueChange = { value -> viewModel.updateDeviceFunction(index) { it.copy(name = value) } },
                                placeholder = "例如 读取 Flash 配置",
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "排序",
                                value = function.sortIndex.toString(),
                                onValueChange = { value -> viewModel.updateDeviceFunction(index) { it.copy(sortIndex = value.toIntOrNull() ?: 0) } },
                            )
                        }
                        fullWidth {
                            CupertinoTextField(
                                label = "功能说明",
                                value = function.description.orEmpty(),
                                onValueChange = { value -> viewModel.updateDeviceFunction(index) { it.copy(description = value) } },
                                singleLine = false,
                            )
                        }
                    }
                    ResolvedFunctionPreviewPanel(
                        resolved = resolvedFunctions[function.key],
                        previewing = state.previewing,
                    )
                    CupertinoPanel(
                        title = "关联物模型字段",
                        subtitle = "只选择当前功能要使用的字段集合，不在前端拼装请求/响应模型。",
                    ) {
                        if (draft.thingProperties.isEmpty()) {
                            CupertinoStatusStrip("还没有可选物模型字段。")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                draft.thingProperties.forEach { property ->
                                    CupertinoBooleanField(
                                        label = property.name.ifBlank { property.key },
                                        checked = property.key in function.thingPropertyKeys,
                                        onCheckedChange = { viewModel.toggleFunctionPropertySelection(index, property.key) },
                                        description = property.description,
                                    )
                                }
                            }
                        }
                    }
                    BindingEditorSection(
                        title = "功能上下文",
                        definitions = methodDefinitions,
                        bindings = function.bindings,
                        onValueChange = { definitionCode, paramCode, value ->
                            viewModel.updateDeviceFunctionBindingValue(index, definitionCode, paramCode, value)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ThingPropertiesPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val headerActionsSpi = koinInject<CodegenContextThingPropertiesHeaderSpi>()
    val itemActionsSpi = koinInject<CodegenContextThingPropertyItemActionsSpi>()
    val draft = state.draft
    val definitions = state.availableContextDefinitions
    val propertyDefinitions = definitions.filter { definition -> definition.targetKind == CodegenNodeKind.FIELD }
    val resolvedProperties = state.preview?.resolvedProperties.orEmpty().associateBy(CodegenMetadataResolvedPropertyDto::key)
    CupertinoPanel(
        title = "物模型字段",
        subtitle = "这里只维护字段定义和协议绑定；属性命名与类型推导全部由后端处理。",
        actions = {
            headerActionsSpi.Render(viewModel = viewModel)
        },
    ) {
        if (draft.thingProperties.isEmpty()) {
            CupertinoStatusStrip("当前还没有物模型字段。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            draft.thingProperties.forEachIndexed { index, property ->
                CupertinoPanel(
                    title = property.name.ifBlank { "未命名物模型字段" },
                    subtitle = "key=${property.key}",
                    actions = {
                        itemActionsSpi.Render(index = index, viewModel = viewModel)
                    },
                ) {
                    CupertinoFormGrid {
                        item {
                            CupertinoTextField(
                                label = "字段名称",
                                value = property.name,
                                onValueChange = { value -> viewModel.updateThingProperty(index) { it.copy(name = value) } },
                                placeholder = "例如 设备地址",
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "排序",
                                value = property.sortIndex.toString(),
                                onValueChange = { value -> viewModel.updateThingProperty(index) { it.copy(sortIndex = value.toIntOrNull() ?: 0) } },
                            )
                        }
                        item {
                            CupertinoBooleanField(
                                label = "可空",
                                checked = property.nullable,
                                onCheckedChange = { checked -> viewModel.updateThingProperty(index) { it.copy(nullable = checked) } },
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "默认值字面量",
                                value = property.defaultLiteral.orEmpty(),
                                onValueChange = { value -> viewModel.updateThingProperty(index) { it.copy(defaultLiteral = value) } },
                            )
                        }
                        fullWidth {
                            CupertinoTextField(
                                label = "字段说明",
                                value = property.description.orEmpty(),
                                onValueChange = { value -> viewModel.updateThingProperty(index) { it.copy(description = value) } },
                                singleLine = false,
                            )
                        }
                    }
                    ResolvedPropertyPreviewPanel(
                        resolved = resolvedProperties[property.key],
                        previewing = state.previewing,
                    )
                    BindingEditorSection(
                        title = "字段上下文",
                        definitions = propertyDefinitions,
                        bindings = property.bindings,
                        onValueChange = { definitionCode, paramCode, value ->
                            viewModel.updateThingPropertyBindingValue(index, definitionCode, paramCode, value)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResolvedFunctionPreviewPanel(
    resolved: CodegenMetadataResolvedFunctionDto?,
    previewing: Boolean,
) {
    CupertinoPanel(
        title = "解析结果",
        subtitle = "后端会根据功能上下文和字段绑定推导最终方法信息。",
    ) {
        when {
            resolved != null -> {
                CupertinoStatusStrip("最终方法名: ${resolved.resolvedMethodName ?: "-"}")
                CupertinoStatusStrip("方向: ${resolved.direction ?: "-"} / 关联字段数: ${resolved.thingPropertyCount}")
                resolved.requestModelName?.let { CupertinoStatusStrip("请求模型: $it") }
                resolved.responseModelName?.let { CupertinoStatusStrip("响应模型: $it") }
                resolved.layoutSummary?.let { CupertinoStatusStrip("布局摘要: $it") }
            }
            previewing -> CupertinoStatusStrip("正在重新解析当前功能 …")
            else -> CupertinoStatusStrip("当前功能还没有可展示的解析结果。")
        }
    }
}

@Composable
private fun ResolvedPropertyPreviewPanel(
    resolved: CodegenMetadataResolvedPropertyDto?,
    previewing: Boolean,
) {
    CupertinoPanel(
        title = "解析结果",
        subtitle = "后端会根据字段上下文推导最终字段名、类型和布局摘要。",
    ) {
        when {
            resolved != null -> {
                CupertinoStatusStrip("最终字段名: ${resolved.resolvedPropertyName ?: "-"}")
                CupertinoStatusStrip("最终类型: ${resolved.resolvedTypeName ?: "-"} / 被功能引用: ${resolved.usageCount} 次")
                resolved.layoutSummary?.let { CupertinoStatusStrip("布局摘要: $it") }
            }
            previewing -> CupertinoStatusStrip("正在重新解析当前字段 …")
            else -> CupertinoStatusStrip("当前字段还没有可展示的解析结果。")
        }
    }
}

private fun issueTone(
    severity: CodegenMetadataIssueSeverity,
): Color =
    when (severity) {
        CodegenMetadataIssueSeverity.ERROR -> Color(0xFFFFE8E6)
        CodegenMetadataIssueSeverity.WARNING -> Color(0xFFFFF5D6)
        CodegenMetadataIssueSeverity.INFO -> Color(0xFFEAF4FF)
    }
