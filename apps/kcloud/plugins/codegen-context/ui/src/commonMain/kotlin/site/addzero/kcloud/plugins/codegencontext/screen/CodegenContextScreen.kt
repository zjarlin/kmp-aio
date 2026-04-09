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
import site.addzero.kcloud.plugins.codegencontext.context.CodegenFieldEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenSchemaEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
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
    val selectedProtocolTemplate =
        state.protocolTemplates.firstOrNull { template ->
            template.id == state.editor.protocolTemplateId
        }

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

            if (state.generatedFiles.isNotEmpty()) {
                CodegenPanel(
                    title = "Generated Artifacts",
                    subtitle = "当前生成按钮已写入这些文件，下一次 mcu-console 构建会优先消费 metadata 快照。",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.generatedFiles.forEach { file ->
                            CodegenStatusStrip(file)
                        }
                    }
                }
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
                    description = "上下文唯一编码，用来标识一组可生成的协议元数据快照。",
                )
                CodegenTextField(
                    label = "Context Name",
                    value = state.editor.name,
                    onValueChange = { value -> viewModel.updateContext { it.copy(name = value) } },
                    description = "给页面、列表和生成日志看的可读名称。",
                )
                CodegenTextField(
                    label = "Description",
                    value = state.editor.description,
                    onValueChange = { value -> viewModel.updateContext { it.copy(description = value) } },
                    singleLine = false,
                    description = "会随 context 一起持久化，适合写用途、设备型号和约束说明。",
                )
                CodegenSelectionField(
                    label = "Consumer Target",
                    options =
                        CodegenConsumerTarget.entries.map { option ->
                            CodegenOption(
                                value = option,
                                label = option.displayLabel(),
                                caption = option.consumerHint(),
                            )
                        },
                    selectedValue = state.editor.consumerTarget,
                    onSelected = { selected ->
                        viewModel.updateContext { current ->
                            current.copy(consumerTarget = selected ?: CodegenConsumerTarget.MCU_CONSOLE)
                        }
                    },
                    description = "决定生成结果最终服务于哪个消费端；当前 V1 只支持 MCU Console。",
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
                    description = selectedProtocolTemplate?.description ?: "协议模板决定 transport 和基础功能码约束。",
                )
                CodegenBooleanField(
                    label = "Enabled",
                    checked = state.editor.enabled,
                    onCheckedChange = { checked -> viewModel.updateContext { it.copy(enabled = checked) } },
                    description = "关闭后仍保留在目录里，但不会作为当前有效快照参与生成。",
                )
                selectedProtocolTemplate?.let { template ->
                    CodegenStatusStrip("模板代码：${template.code}；模板说明：${template.description ?: "未填写说明"}")
                }
            }

            CodegenPanel(
                title = "Schemas",
                subtitle = "一个 schema 对应一个生成方法；READ 必填 modelName，WRITE 则直接生成写接口。",
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
                                CodegenStatusStrip(schema.schemaHint())
                                CodegenTextField(
                                    label = "Schema Name",
                                    value = schema.name,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(name = value) }
                                    },
                                    description = "面向业务的展示名称，会进入协议文档和 DTO 注释。",
                                )
                                CodegenTextField(
                                    label = "Description",
                                    value = schema.description,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(description = value) }
                                    },
                                    singleLine = false,
                                    description = "建议写清读取/写入语义、寄存器块用途和任何设备限制。",
                                )
                                CodegenTextField(
                                    label = "Sort Index",
                                    value = schema.sortIndexText,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(sortIndexText = value) }
                                    },
                                    description = "控制 schema 在页面和生成文档里的稳定顺序。",
                                )
                                CodegenSelectionField(
                                    label = "Direction",
                                    options =
                                        CodegenSchemaDirection.entries.map { option ->
                                            CodegenOption(option, option.name, option.directionHint())
                                        },
                                    selectedValue = schema.direction,
                                    onSelected = { selected ->
                                        viewModel.updateSchema(schemaIndex) { current ->
                                            current.copy(direction = selected ?: CodegenSchemaDirection.READ)
                                        }
                                    },
                                    description = schema.direction.directionHint(),
                                )
                                CodegenSelectionField(
                                    label = "Function Code",
                                    options =
                                        schema.direction.allowedFunctionCodes().map { option ->
                                            CodegenOption(option, option.name, option.functionCodeHint())
                                        },
                                    selectedValue = schema.functionCode,
                                    onSelected = { selected ->
                                        viewModel.updateSchema(schemaIndex) { current ->
                                            current.copy(functionCode = selected ?: current.direction.allowedFunctionCodes().first())
                                        }
                                    },
                                    description = schema.functionCode.functionCodeHint(),
                                )
                                CodegenTextField(
                                    label = "Base Address",
                                    value = schema.baseAddressText,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(baseAddressText = value) }
                                    },
                                    description = "schema 的起始地址，字段的 registerOffset 都是相对这里继续偏移。",
                                )
                                CodegenTextField(
                                    label = "Method Name",
                                    value = schema.methodName,
                                    onValueChange = { value ->
                                        viewModel.updateSchema(schemaIndex) { current -> current.copy(methodName = value) }
                                    },
                                    description = "必须是合法 Kotlin 标识符，最终决定生成接口的方法名。",
                                )
                                if (schema.direction == CodegenSchemaDirection.READ) {
                                    CodegenTextField(
                                        label = "Model Name",
                                        value = schema.modelName,
                                        onValueChange = { value ->
                                            viewModel.updateSchema(schemaIndex) { current -> current.copy(modelName = value) }
                                        },
                                        placeholder = "例如 DeviceRuntimeInfo",
                                        description = "READ schema 必填，最终会生成 `${schema.modelName.ifBlank { "ModelName" }}Registers` DTO。",
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
                                                CodegenStatusStrip(
                                                    text = field.layoutHint(schema.baseAddressText, schema.functionCode),
                                                )
                                                CodegenTextField(
                                                    label = "Field Name",
                                                    value = field.name,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(name = value) }
                                                    },
                                                    description = "字段展示名称，会进入文档表格和生成注释。",
                                                )
                                                CodegenTextField(
                                                    label = "Description",
                                                    value = field.description,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(description = value) }
                                                    },
                                                    singleLine = false,
                                                    description = "建议写清这个字段承载的业务语义、单位和范围。",
                                                )
                                                CodegenTextField(
                                                    label = "Sort Index",
                                                    value = field.sortIndexText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(sortIndexText = value) }
                                                    },
                                                    description = "控制字段在 DTO、C 结构体和协议文档中的顺序。",
                                                )
                                                CodegenTextField(
                                                    label = "Property Name",
                                                    value = field.propertyName,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(propertyName = value) }
                                                    },
                                                    description = "必须是合法 Kotlin 标识符，会直接成为 Kotlin/C 侧生成属性名。",
                                                )
                                                CodegenSelectionField(
                                                    label = "Transport Type",
                                                    options =
                                                        CodegenTransportType.entries.map { option ->
                                                            CodegenOption(option, option.name, option.transportHint())
                                                        },
                                                    selectedValue = field.transportType,
                                                    onSelected = { selected ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current ->
                                                            current.copy(transportType = selected ?: CodegenTransportType.BOOL_COIL)
                                                        }
                                                    },
                                                    description = field.transportType.transportLengthHint(schema.functionCode),
                                                )
                                                CodegenTextField(
                                                    label = "Register Offset",
                                                    value = field.registerOffsetText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(registerOffsetText = value) }
                                                    },
                                                    description = field.registerOffsetHint(schema.baseAddressText, schema.functionCode),
                                                )
                                                CodegenTextField(
                                                    label = "Bit Offset",
                                                    value = field.bitOffsetText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(bitOffsetText = value) }
                                                    },
                                                    description = "V1 当前固定要求为 0，暂不开放寄存器内 bit 打包写法。",
                                                )
                                                CodegenTextField(
                                                    label = "Length",
                                                    value = field.lengthText,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(lengthText = value) }
                                                    },
                                                    description = field.transportType.lengthFieldHint(),
                                                )
                                                CodegenTextField(
                                                    label = "Translation Hint",
                                                    value = field.translationHint,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(translationHint = value) }
                                                    },
                                                    singleLine = false,
                                                    description = "预留给翻译、别名或提示词等元数据，当前不会直接改变生成代码。",
                                                )
                                                CodegenTextField(
                                                    label = "Default Literal",
                                                    value = field.defaultLiteral,
                                                    onValueChange = { value ->
                                                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(defaultLiteral = value) }
                                                    },
                                                    description = "预留示例值或默认字面量，当前主要作为元数据随快照保存。",
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

private fun CodegenConsumerTarget.displayLabel(): String =
    when (this) {
        CodegenConsumerTarget.MCU_CONSOLE -> "MCU Console"
    }

private fun CodegenConsumerTarget.consumerHint(): String =
    when (this) {
        CodegenConsumerTarget.MCU_CONSOLE -> "当前唯一正式接入的消费端，会生成 mcu-console 契约和 metadata 快照。"
    }

private fun CodegenSchemaDirection.directionHint(): String =
    when (this) {
        CodegenSchemaDirection.READ -> "读取类 schema，会生成返回 DTO。"
        CodegenSchemaDirection.WRITE -> "写入类 schema，会生成写方法和请求参数定义。"
    }

private fun CodegenFunctionCode.functionCodeHint(): String =
    when (this) {
        CodegenFunctionCode.READ_COILS -> "读线圈，字段类型必须使用 BOOL_COIL。"
        CodegenFunctionCode.READ_DISCRETE_INPUTS -> "读离散输入，字段类型必须使用 BOOL_COIL。"
        CodegenFunctionCode.READ_INPUT_REGISTERS -> "读输入寄存器，适合只读寄存器块。"
        CodegenFunctionCode.READ_HOLDING_REGISTERS -> "读保持寄存器，适合可持久化配置和状态快照。"
        CodegenFunctionCode.WRITE_SINGLE_COIL -> "单线圈写入，必须只有一个 BOOL_COIL 字段。"
        CodegenFunctionCode.WRITE_MULTIPLE_COILS -> "批量线圈写入，所有字段都必须使用 BOOL_COIL。"
        CodegenFunctionCode.WRITE_SINGLE_REGISTER -> "单寄存器写入，必须只有一个 U16 字段。"
        CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS -> "批量寄存器写入，适合结构化配置写回。"
    }

private fun CodegenTransportType.transportHint(): String =
    when (this) {
        CodegenTransportType.BOOL_COIL -> "线圈布尔值，占 1 个 coil。"
        CodegenTransportType.U8 -> "单寄存器低 8 位。"
        CodegenTransportType.U16 -> "单个 16 位寄存器。"
        CodegenTransportType.U32_BE -> "2 个寄存器组成的大端 32 位值。"
        CodegenTransportType.BYTE_ARRAY -> "原始字节数组，按字节数填写长度。"
        CodegenTransportType.STRING_ASCII -> "ASCII 字符串，长度按寄存器个数填写。"
        CodegenTransportType.STRING_UTF8 -> "UTF-8 字符串，长度按寄存器个数填写。"
    }

private fun CodegenTransportType.lengthFieldHint(): String =
    when (this) {
        CodegenTransportType.BOOL_COIL,
        CodegenTransportType.U8,
        CodegenTransportType.U16,
        CodegenTransportType.U32_BE,
        -> "V1 标量类型固定填 1。"
        CodegenTransportType.BYTE_ARRAY -> "按字节数填写，例如 24 表示 24 字节，会占用 12 个寄存器。"
        CodegenTransportType.STRING_ASCII,
        CodegenTransportType.STRING_UTF8,
        -> "按寄存器个数填写，每个寄存器承载 2 个字符字节。"
    }

private fun CodegenTransportType.transportLengthHint(
    functionCode: CodegenFunctionCode,
): String {
    if (functionCode.expectsCoilSpace()) {
        return "当前功能码使用线圈空间，因此字段类型只能选 BOOL_COIL。"
    }
    return when (this) {
        CodegenTransportType.BOOL_COIL -> "只有 READ_COILS / READ_DISCRETE_INPUTS / WRITE_*_COILS 才允许 BOOL_COIL。"
        else -> "${transportHint()} ${lengthFieldHint()}"
    }
}

private fun CodegenSchemaEditorState.schemaHint(): String =
    when (functionCode) {
        CodegenFunctionCode.WRITE_SINGLE_COIL -> "当前 schema 只能保留 1 个 BOOL_COIL 字段。"
        CodegenFunctionCode.WRITE_SINGLE_REGISTER -> "当前 schema 只能保留 1 个 U16 字段。"
        else ->
            if (direction == CodegenSchemaDirection.READ) {
                "READ schema 生成返回 DTO，记得补齐 modelName 和字段注释。"
            } else {
                "WRITE schema 生成写入请求定义，字段顺序会直接影响寄存器布局。"
            }
    }

private fun CodegenFieldEditorState.layoutHint(
    baseAddressText: String,
    functionCode: CodegenFunctionCode,
): String {
    val baseAddress = baseAddressText.toIntOrNull()
    val offset = registerOffsetText.toIntOrNull()
    val length = lengthText.toIntOrNull() ?: 1
    if (offset == null) {
        return "当前寄存器偏移还不是有效数字，保存前请补成非负整数。"
    }
    if (functionCode.expectsCoilSpace()) {
        val absoluteAddress = baseAddress?.plus(offset)
        return if (absoluteAddress == null) {
            "当前字段会占用 coil 偏移 $offset。"
        } else {
            "当前字段会占用绝对 coil 地址 $absoluteAddress。"
        }
    }
    val width = transportType.registerWidth(length)
    val rangeEnd = offset + width - 1
    val absoluteStart = baseAddress?.plus(offset)
    val absoluteEnd = absoluteStart?.plus(width - 1)
    return if (absoluteStart == null || absoluteEnd == null) {
        "当前字段会占用相对寄存器区间 $offset..$rangeEnd。"
    } else {
        "当前字段会占用绝对寄存器区间 $absoluteStart..$absoluteEnd。"
    }
}

private fun CodegenFieldEditorState.registerOffsetHint(
    baseAddressText: String,
    functionCode: CodegenFunctionCode,
): String {
    val baseAddress = baseAddressText.toIntOrNull()
    val offset = registerOffsetText.toIntOrNull()
    val absoluteAddress = if (baseAddress != null && offset != null) baseAddress + offset else null
    return if (functionCode.expectsCoilSpace()) {
        if (absoluteAddress == null) {
            "相对 schema 起始 coil 的偏移，保存前必须是非负整数。"
        } else {
            "相对 schema 起始 coil 的偏移；当前绝对 coil 地址为 $absoluteAddress。"
        }
    } else {
        if (absoluteAddress == null) {
            "相对 schema baseAddress 的寄存器偏移，保存前必须是非负整数。"
        } else {
            "相对 schema baseAddress 的寄存器偏移；当前绝对起始地址为 $absoluteAddress。"
        }
    }
}

private fun CodegenFunctionCode.expectsCoilSpace(): Boolean =
    when (this) {
        CodegenFunctionCode.READ_COILS,
        CodegenFunctionCode.READ_DISCRETE_INPUTS,
        CodegenFunctionCode.WRITE_SINGLE_COIL,
        CodegenFunctionCode.WRITE_MULTIPLE_COILS,
        -> true
        CodegenFunctionCode.READ_INPUT_REGISTERS,
        CodegenFunctionCode.READ_HOLDING_REGISTERS,
        CodegenFunctionCode.WRITE_SINGLE_REGISTER,
        CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
        -> false
    }

private fun CodegenTransportType.registerWidth(length: Int): Int =
    when (this) {
        CodegenTransportType.BOOL_COIL,
        CodegenTransportType.U8,
        CodegenTransportType.U16,
        -> 1
        CodegenTransportType.U32_BE -> 2
        CodegenTransportType.BYTE_ARRAY -> (length + 1) / 2
        CodegenTransportType.STRING_ASCII,
        CodegenTransportType.STRING_UTF8,
        -> length
    }
