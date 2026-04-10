package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDeviceFunctionDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataThingPropertyDraftDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenNodeKind

@Composable
internal fun DeviceFunctionsPanel(
    draft: CodegenMetadataDraftDto,
    definitions: List<CodegenContextDefinitionDto>,
    onAddFunction: () -> Unit,
    onRemoveFunction: (Int) -> Unit,
    onUpdateFunction: (Int, (CodegenMetadataDeviceFunctionDraftDto) -> CodegenMetadataDeviceFunctionDraftDto) -> Unit,
    onToggleFunctionProperty: (Int, String) -> Unit,
    onUpdateBinding: (Int, String, String, String) -> Unit,
) {
    val methodDefinitions = definitions.filter { definition -> definition.targetKind == CodegenNodeKind.METHOD }
    CupertinoPanel(
        title = "设备功能",
        subtitle = "这里只维护中文语义、字段绑定和协议上下文；方法名与请求/响应模型统一由后端 preview 决定。",
        actions = {
            WorkbenchActionButton(text = "新增功能", onClick = onAddFunction)
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
                        WorkbenchActionButton(
                            text = "删除功能",
                            onClick = { onRemoveFunction(index) },
                            variant = WorkbenchButtonVariant.Destructive,
                        )
                    },
                ) {
                    CupertinoFormGrid {
                        item {
                            CupertinoTextField(
                                label = "功能名称",
                                value = function.name,
                                onValueChange = { value -> onUpdateFunction(index) { it.copy(name = value) } },
                                placeholder = "例如 读取 Flash 配置",
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "排序",
                                value = function.sortIndex.toString(),
                                onValueChange = { value -> onUpdateFunction(index) { it.copy(sortIndex = value.toIntOrNull() ?: 0) } },
                            )
                        }
                        fullWidth {
                            CupertinoTextField(
                                label = "功能说明",
                                value = function.description.orEmpty(),
                                onValueChange = { value -> onUpdateFunction(index) { it.copy(description = value) } },
                                singleLine = false,
                            )
                        }
                    }
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
                                        onCheckedChange = { onToggleFunctionProperty(index, property.key) },
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
                            onUpdateBinding(index, definitionCode, paramCode, value)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ThingPropertiesPanel(
    draft: CodegenMetadataDraftDto,
    definitions: List<CodegenContextDefinitionDto>,
    onAddProperty: () -> Unit,
    onRemoveProperty: (Int) -> Unit,
    onUpdateProperty: (Int, (CodegenMetadataThingPropertyDraftDto) -> CodegenMetadataThingPropertyDraftDto) -> Unit,
    onUpdateBinding: (Int, String, String, String) -> Unit,
) {
    val propertyDefinitions = definitions.filter { definition -> definition.targetKind == CodegenNodeKind.FIELD }
    CupertinoPanel(
        title = "物模型字段",
        subtitle = "字段只维护原始草稿和绑定上下文；属性名、类型推导全部由后端处理。",
        actions = {
            WorkbenchActionButton(text = "新增字段", onClick = onAddProperty)
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
                        WorkbenchActionButton(
                            text = "删除字段",
                            onClick = { onRemoveProperty(index) },
                            variant = WorkbenchButtonVariant.Destructive,
                        )
                    },
                ) {
                    CupertinoFormGrid {
                        item {
                            CupertinoTextField(
                                label = "字段名称",
                                value = property.name,
                                onValueChange = { value -> onUpdateProperty(index) { it.copy(name = value) } },
                                placeholder = "例如 设备地址",
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "排序",
                                value = property.sortIndex.toString(),
                                onValueChange = { value -> onUpdateProperty(index) { it.copy(sortIndex = value.toIntOrNull() ?: 0) } },
                            )
                        }
                        item {
                            CupertinoBooleanField(
                                label = "可空",
                                checked = property.nullable,
                                onCheckedChange = { checked -> onUpdateProperty(index) { it.copy(nullable = checked) } },
                            )
                        }
                        item {
                            CupertinoTextField(
                                label = "默认值字面量",
                                value = property.defaultLiteral.orEmpty(),
                                onValueChange = { value -> onUpdateProperty(index) { it.copy(defaultLiteral = value) } },
                            )
                        }
                        fullWidth {
                            CupertinoTextField(
                                label = "字段说明",
                                value = property.description.orEmpty(),
                                onValueChange = { value -> onUpdateProperty(index) { it.copy(description = value) } },
                                singleLine = false,
                            )
                        }
                    }
                    BindingEditorSection(
                        title = "字段上下文",
                        definitions = propertyDefinitions,
                        bindings = property.bindings,
                        onValueChange = { definitionCode, paramCode, value ->
                            onUpdateBinding(index, definitionCode, paramCode, value)
                        },
                    )
                }
            }
        }
    }
}
