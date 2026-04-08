@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.codegencontext.common.CodegenBooleanField
import site.addzero.kcloud.plugins.codegencontext.common.CodegenOption
import site.addzero.kcloud.plugins.codegencontext.common.CodegenPanel
import site.addzero.kcloud.plugins.codegencontext.common.CodegenSelectionField
import site.addzero.kcloud.plugins.codegencontext.common.CodegenStatusStrip
import site.addzero.kcloud.plugins.codegencontext.common.CodegenTextField
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

@Route(
    title = "代码生成上下文",
    routePath = "codegen-context/contexts",
    icon = "Code",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "开发工具",
            icon = "Code",
            order = 40,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun CodegenContextScreen() {
    val viewModel = koinViewModel<CodegenContextViewModel>()
    val state = viewModel.screenState

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.contexts,
            selectedId = state.selectedContextId,
            onNodeClick = { item -> viewModel.selectContext(item.id) },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f),
            searchPlaceholder = "搜索 context",
            getId = { item -> item.id },
            getLabel = { item -> item.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.DataObject },
            header = {
                if (state.errorMessage != null) {
                    CodegenStatusStrip(state.errorMessage)
                }
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    imageVector = Icons.Outlined.Refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
                WorkbenchActionButton(
                    text = "新建",
                    onClick = viewModel::newContext,
                )
                WorkbenchActionButton(
                    text = "删除",
                    onClick = viewModel::deleteSelected,
                    enabled = state.selectedContextId != null && !state.deleting,
                    variant = WorkbenchButtonVariant.Destructive,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(0.7f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.statusMessage?.let { message ->
                CodegenStatusStrip(message)
            }

            CodegenPanel(
                title = state.editor.name.ifBlank { "未命名 context" },
                subtitle = "协议感知 schema 上下文，当前只生成 mcu-console Modbus 契约。",
                actions = {
                    WorkbenchActionButton(
                        text = if (state.saving) "保存中" else "保存",
                        onClick = viewModel::save,
                        enabled = !state.saving,
                    )
                    WorkbenchActionButton(
                        text = if (state.generating) "生成中" else "生成",
                        onClick = viewModel::generateSelected,
                        enabled = state.selectedContextId != null && !state.generating,
                        variant = WorkbenchButtonVariant.Secondary,
                    )
                },
            ) {
                CodegenTextField(
                    label = "Context Code",
                    value = state.editor.code,
                    onValueChange = { value -> viewModel.updateContext { it.copy(code = value) } },
                    placeholder = "例如 MCU_DEVICE_DEFAULT",
                )
                CodegenTextField(
                    label = "Context Name",
                    value = state.editor.name,
                    onValueChange = { value -> viewModel.updateContext { it.copy(name = value) } },
                )
                CodegenTextField(
                    label = "Description",
                    value = state.editor.description,
                    onValueChange = { value -> viewModel.updateContext { it.copy(description = value) } },
                    singleLine = false,
                )
                CodegenSelectionField(
                    label = "Protocol Template",
                    options =
                        state.protocolTemplates.map { item ->
                            CodegenOption(
                                value = item.id,
                                label = item.name,
                                caption = item.code,
                            )
                        },
                    selectedValue = state.editor.protocolTemplateId,
                    onSelected = { selected ->
                        viewModel.updateContext { it.copy(protocolTemplateId = selected) }
                    },
                )
                CodegenBooleanField(
                    label = "Enabled",
                    checked = state.editor.enabled,
                    onCheckedChange = { checked -> viewModel.updateContext { it.copy(enabled = checked) } },
                    description = "Disabled contexts stay in the catalog but should not be used for generation.",
                )
            }

            CodegenPanel(
                title = "Schemas",
                subtitle = "One schema maps to one generated Modbus method.",
                actions = {
                    WorkbenchActionButton(
                        text = "新增 Schema",
                        onClick = viewModel::addSchema,
                        imageVector = Icons.Outlined.Code,
                    )
                },
            ) {
                if (state.editor.schemas.isEmpty()) {
                    CodegenStatusStrip("当前 context 还没有 schema。")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.editor.schemas.forEachIndexed { schemaIndex, schema ->
                            CodegenPanel(
                                title = schema.name.ifBlank { "Schema ${schemaIndex + 1}" },
                                subtitle = schema.methodName.ifBlank { "未设置 methodName" },
                                actions = {
                                    WorkbenchActionButton(
                                        text = "新增字段",
                                        onClick = { viewModel.addField(schemaIndex) },
                                        variant = WorkbenchButtonVariant.Outline,
                                    )
                                    WorkbenchActionButton(
                                        text = "删除 Schema",
                                        onClick = { viewModel.removeSchema(schemaIndex) },
                                        variant = WorkbenchButtonVariant.Destructive,
                                    )
                                },
                            ) {
                                CodegenTextField(
                                    label = "Schema Name",
                                    value = schema.name,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(name = value) }
                                    },
                                )
                                CodegenTextField(
                                    label = "Description",
                                    value = schema.description,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(description = value) }
                                    },
                                    singleLine = false,
                                )
                                CodegenTextField(
                                    label = "Sort Index",
                                    value = schema.sortIndexText,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(sortIndexText = value) }
                                    },
                                )
                                CodegenSelectionField(
                                    label = "Direction",
                                    options =
                                        CodegenSchemaDirection.entries.map { option ->
                                            CodegenOption(option, option.name)
                                        },
                                    selectedValue = schema.direction,
                                    onSelected = { selected ->
                                        viewModel.updateSchema(schemaIndex) { current ->
                                            current.copy(direction = selected ?: CodegenSchemaDirection.READ)
                                        }
                                    },
                                )
                                CodegenSelectionField(
                                    label = "Function Code",
                                    options =
                                        schema.direction.allowedFunctionCodes().map { option ->
                                            CodegenOption(option, option.name)
                                        },
                                    selectedValue = schema.functionCode,
                                    onSelected = { selected ->
                                        viewModel.updateSchema(schemaIndex) { current ->
                                            current.copy(functionCode = selected ?: current.direction.allowedFunctionCodes().first())
                                        }
                                    },
                                )
                                CodegenTextField(
                                    label = "Base Address",
                                    value = schema.baseAddressText,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(baseAddressText = value) }
                                    },
                                )
                                CodegenTextField(
                                    label = "Method Name",
                                    value = schema.methodName,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(methodName = value) }
                                    },
                                )
                                if (schema.direction == CodegenSchemaDirection.READ) {
                                    CodegenTextField(
                                        label = "Model Name",
                                        value = schema.modelName,
                                        onValueChange = { value ->
                                            viewModel.updateSchema(schemaIndex) { current -> current.copy(modelName = value) }
                                        },
                                        placeholder = "例如 DeviceRuntimeInfo",
                                    )
                                }

                                if (schema.fields.isEmpty()) {
                                    CodegenStatusStrip("当前 schema 还没有字段。")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        schema.fields.forEachIndexed { fieldIndex, field ->
                                            CodegenPanel(
                                                title = field.name.ifBlank { "Field ${fieldIndex + 1}" },
                                                subtitle = field.propertyName.ifBlank { "未设置 propertyName" },
                                                actions = {
                                                    WorkbenchActionButton(
                                                        text = "删除字段",
                                                        onClick = { viewModel.removeField(schemaIndex, fieldIndex) },
                                                        variant = WorkbenchButtonVariant.Destructive,
                                                    )
                                                },
                                            ) {
                                                CodegenTextField(
                                                    label = "Field Name",
                                                    value = field.name,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(name = value) }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Description",
                                                    value = field.description,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(description = value) }
                                                    },
                                                    singleLine = false,
                                                )
                                                CodegenTextField(
                                                    label = "Sort Index",
                                                    value = field.sortIndexText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(sortIndexText = value) }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Property Name",
                                                    value = field.propertyName,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(propertyName = value) }
                                                    },
                                                )
                                                CodegenSelectionField(
                                                    label = "Transport Type",
                                                    options =
                                                        CodegenTransportType.entries.map { option ->
                                                            CodegenOption(option, option.name)
                                                        },
                                                    selectedValue = field.transportType,
                                                    onSelected = { selected ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current ->
                                                            current.copy(transportType = selected ?: CodegenTransportType.BOOL_COIL)
                                                        }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Register Offset",
                                                    value = field.registerOffsetText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(registerOffsetText = value) }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Bit Offset",
                                                    value = field.bitOffsetText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(bitOffsetText = value) }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Length",
                                                    value = field.lengthText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(lengthText = value) }
                                                    },
                                                )
                                                CodegenTextField(
                                                    label = "Translation Hint",
                                                    value = field.translationHint,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(translationHint = value) }
                                                    },
                                                    singleLine = false,
                                                )
                                                CodegenTextField(
                                                    label = "Default Literal",
                                                    value = field.defaultLiteral,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(defaultLiteral = value) }
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun CodegenSchemaDirection.allowedFunctionCodes(): List<CodegenFunctionCode> =
    when (this) {
        CodegenSchemaDirection.READ ->
            listOf(
                CodegenFunctionCode.READ_COILS,
                CodegenFunctionCode.READ_DISCRETE_INPUTS,
                CodegenFunctionCode.READ_INPUT_REGISTERS,
                CodegenFunctionCode.READ_HOLDING_REGISTERS,
            )

        CodegenSchemaDirection.WRITE ->
            listOf(
                CodegenFunctionCode.WRITE_SINGLE_COIL,
                CodegenFunctionCode.WRITE_MULTIPLE_COILS,
                CodegenFunctionCode.WRITE_SINGLE_REGISTER,
                CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
            )
    }
