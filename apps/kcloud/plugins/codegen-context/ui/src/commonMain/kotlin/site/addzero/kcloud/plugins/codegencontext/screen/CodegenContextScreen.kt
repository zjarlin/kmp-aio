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
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.common.CodegenBooleanField
import site.addzero.kcloud.plugins.codegencontext.common.CodegenFormGrid
import site.addzero.kcloud.plugins.codegencontext.common.CodegenOption
import site.addzero.kcloud.plugins.codegencontext.common.CodegenPanel
import site.addzero.kcloud.plugins.codegencontext.common.CodegenSelectionField
import site.addzero.kcloud.plugins.codegencontext.common.CodegenStatusStrip
import site.addzero.kcloud.plugins.codegencontext.common.CodegenTextField
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.context.CodegenFieldEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenGenerationSettingsEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenSchemaEditorState
import site.addzero.kcloud.plugins.codegencontext.context.toGeneratedMethodName
import site.addzero.kcloud.plugins.codegencontext.context.toGeneratedPropertyName
import site.addzero.kcloud.plugins.codegencontext.context.toGeneratedTypeName
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

@Route(
    title = "代码生成上下文",
    routePath = "codegen-context/contexts",
    icon = "Code",
    order = 40.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "宿主配置",
            icon = "SettingsApplications",
            order = 10,
        ),
        defaultInScene = false,
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
    val readSchemas = state.editor.schemas.withIndex().filter { indexed -> indexed.value.direction == CodegenSchemaDirection.READ }
    val writeSchemas = state.editor.schemas.withIndex().filter { indexed -> indexed.value.direction == CodegenSchemaDirection.WRITE }

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
            searchPlaceholder = "搜索上下文",
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
                    title = "生成结果",
                    subtitle = "这些文件就是当前上下文生成出来的接口契约与产物快照。",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.generatedFiles.forEach { file ->
                            CodegenStatusStrip(file)
                        }
                    }
                }
            }

            ContextSummaryPanel(
                state = state.editor,
                selectedProtocolTemplate = selectedProtocolTemplate,
                viewModel = viewModel,
                saving = state.saving,
                generating = state.generating,
            )

            GenerationSettingsPanel(
                state = state.editor.generationSettings,
                selectedProtocolTemplate = selectedProtocolTemplate,
                viewModel = viewModel,
            )

            ApiOverviewPanel(
                readSchemas = readSchemas.map { indexed -> indexed.value },
                writeSchemas = writeSchemas.map { indexed -> indexed.value },
                selectedProtocolTemplate = selectedProtocolTemplate,
            )

            MethodGroupPanel(
                title = "DeviceApi",
                subtitle = "这里定义读取接口，界面直接对应生成出来的 DeviceApi.kt。",
                emptyHint = "当前还没有读取方法。",
                addActionText = "新增读取方法",
                direction = CodegenSchemaDirection.READ,
                schemas = readSchemas,
                viewModel = viewModel,
            )

            MethodGroupPanel(
                title = "DeviceWriteApi",
                subtitle = "这里定义写入接口，界面直接对应生成出来的 DeviceWriteApi.kt。",
                emptyHint = "当前还没有写入方法。",
                addActionText = "新增写入方法",
                direction = CodegenSchemaDirection.WRITE,
                schemas = writeSchemas,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun ContextSummaryPanel(
    state: CodegenContextEditorState,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
    viewModel: CodegenContextViewModel,
    saving: Boolean,
    generating: Boolean,
) {
    CodegenPanel(
        title = state.name.ifBlank { "未命名上下文" },
        subtitle = "当前上下文会生成 MCU Console 的 DeviceApi / DeviceWriteApi 契约。",
        actions = {
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = viewModel::save,
                enabled = !saving,
            )
            WorkbenchActionButton(
                text = if (generating) "生成中" else "生成",
                onClick = viewModel::generateSelected,
                enabled = state.protocolTemplateId != null && !generating && !saving,
                variant = WorkbenchButtonVariant.Secondary,
            )
        },
    ) {
        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = "上下文编码",
                    value = state.code,
                    onValueChange = { value -> viewModel.updateContext { it.copy(code = value) } },
                    placeholder = "例如 MCU_DEVICE_DEFAULT",
                    description = "用于标识一组可生成的协议上下文。",
                )
            }
            item {
                CodegenTextField(
                    label = "上下文名称",
                    value = state.name,
                    onValueChange = { value -> viewModel.updateContext { it.copy(name = value) } },
                    description = "列表里看到的名称，也会进入生成日志。",
                )
            }
            item {
                CodegenSelectionField(
                    label = "协议模板",
                    options =
                        viewModel.screenState.protocolTemplates.map { item ->
                            CodegenOption(
                                value = item.id,
                                label = item.name,
                                caption = item.code,
                            )
                        },
                    selectedValue = state.protocolTemplateId,
                    onSelected = { selected ->
                        viewModel.updateContext { it.copy(protocolTemplateId = selected) }
                    },
                    description = selectedProtocolTemplate?.description ?: "协议模板决定 transport 和功能码约束。",
                )
            }
            item {
                CodegenBooleanField(
                    label = "启用",
                    checked = state.enabled,
                    onCheckedChange = { checked -> viewModel.updateContext { it.copy(enabled = checked) } },
                    description = "关闭后保留元数据，但不会作为当前有效快照参与生成。",
                )
            }
            fullWidth {
                CodegenTextField(
                    label = "上下文说明",
                    value = state.description,
                    onValueChange = { value -> viewModel.updateContext { it.copy(description = value) } },
                    singleLine = false,
                    description = "建议写清设备型号、约束和使用场景。",
                )
            }
            fullWidth {
                CodegenTextField(
                    label = "旧版外部 C 输出根",
                    value = state.externalCOutputRoot,
                    onValueChange = { value -> viewModel.updateContext { it.copy(externalCOutputRoot = value) } },
                    placeholder = "例如 /Volumes/peer-workspace/device-fw/Docs/generated/modbus-metadata",
                    description = "兼容旧配置。若未单独填写 cOutputRoot / markdownOutputRoot，后台会从这里派生子目录。",
                )
            }
            fullWidth {
                CodegenStatusStrip("消费端固定为 MCU Console；当前界面只配置会进入 DeviceApi / DeviceWriteApi 的契约元数据。")
            }
        }

        selectedProtocolTemplate?.let { template ->
            CodegenStatusStrip("协议模板：${template.name}（${template.code}）")
            template.protocolContextHint()?.let { hint ->
                CodegenStatusStrip(hint)
            }
        }
        state.externalCOutputRoot.takeIf { it.isNotBlank() }?.let { outputRoot ->
            CodegenStatusStrip(outputRoot.externalOutputHint())
        }
    }
}

@Composable
private fun GenerationSettingsPanel(
    state: CodegenGenerationSettingsEditorState,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
    viewModel: CodegenContextViewModel,
) {
    CodegenPanel(
        title = "生成器设置",
        subtitle = "这些字段直接对应后台代码生成器的参数；保存后点击“生成”会按这里的路径和 transport 默认值落盘。",
    ) {
        CodegenStatusStrip("等价参数：server/shared/gateway/api-client/spring-route/c/markdown 输出根，以及 RTU/TCP 默认参数。")

        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = "Server 输出根",
                    value = state.serverOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(serverOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../server/generated/jvmMain/kotlin",
                    description = "对应 --server-output-root；用于 DeviceApi / DeviceWriteApi 所在的 Kotlin 源码根。",
                )
            }
            item {
                CodegenTextField(
                    label = "Shared 输出根",
                    value = state.sharedOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(sharedOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../shared/generated/commonMain/kotlin",
                    description = "对应 --shared-output-root；用于 *Registers DTO 源码根。",
                )
            }
            item {
                CodegenTextField(
                    label = "Gateway 输出根",
                    value = state.gatewayOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(gatewayOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../server/generated/jvmMain/kotlin",
                    description = "对应 --gateway-output-root；生成 transport gateway / dispatcher。",
                )
            }
            item {
                CodegenTextField(
                    label = "Spring Route 输出根",
                    value = state.springRouteOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(springRouteOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../server/generated/jvmMain/kotlin",
                    description = "对应 --spring-route-output-root；生成 spring2ktor 顶层路由源码。",
                )
            }
            item {
                CodegenTextField(
                    label = "API Client 输出根",
                    value = state.apiClientOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(apiClientOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../ui/generated/commonMain/kotlin",
                    description = "对应 Ktorfit client 生成目录；和包名一起配置才会生效。",
                )
            }
            item {
                CodegenTextField(
                    label = "API Client 包名",
                    value = state.apiClientPackageName,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(apiClientPackageName = value) }
                    },
                    placeholder = "site.addzero...generated",
                    description = "对应生成 client 的 packageName；必须和输出根成对出现。",
                )
            }
            item {
                CodegenTextField(
                    label = "C 输出根",
                    value = state.cOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(cOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../Docs/generated/modbus-metadata/c",
                    description = "对应 --c-output-root；可直接指向别人电脑挂载过来的 C 工程目录。",
                )
            }
            item {
                CodegenTextField(
                    label = "Markdown 输出根",
                    value = state.markdownOutputRoot,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current -> current.copy(markdownOutputRoot = value) }
                    },
                    placeholder = "/abs/path/.../Docs/generated/modbus-metadata/markdown",
                    description = "对应 --markdown-output-root；生成协议说明文档。",
                )
            }
        }

        CodegenStatusStrip("RTU 默认参数会带进 RTU gateway / C 契约 / 文档产物。")
        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = "RTU 串口路径",
                    value = state.rtuDefaults.portPath,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(portPath = value))
                        }
                    },
                    description = "对应默认 portPath，例如 /dev/ttyUSB0。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 单元 ID",
                    value = state.rtuDefaults.unitIdText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(unitIdText = value))
                        }
                    },
                    description = "对应 unitId。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 波特率",
                    value = state.rtuDefaults.baudRateText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(baudRateText = value))
                        }
                    },
                    description = "对应 baudRate。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 数据位",
                    value = state.rtuDefaults.dataBitsText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(dataBitsText = value))
                        }
                    },
                    description = "对应 dataBits。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 停止位",
                    value = state.rtuDefaults.stopBitsText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(stopBitsText = value))
                        }
                    },
                    description = "对应 stopBits。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 校验",
                    value = state.rtuDefaults.parity,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(parity = value))
                        }
                    },
                    description = "对应 parity，例如 none / even / odd。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 超时(ms)",
                    value = state.rtuDefaults.timeoutMsText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(timeoutMsText = value))
                        }
                    },
                    description = "对应 timeoutMs。",
                )
            }
            item {
                CodegenTextField(
                    label = "RTU 重试次数",
                    value = state.rtuDefaults.retriesText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(rtuDefaults = current.rtuDefaults.copy(retriesText = value))
                        }
                    },
                    description = "对应 retries。",
                )
            }
        }

        if (selectedProtocolTemplate?.code == "MODBUS_TCP_CLIENT") {
            CodegenStatusStrip("当前协议模板是 TCP；下面这些默认参数会进入 TCP gateway / client / 文档产物。")
        } else {
            CodegenStatusStrip("即使当前模板是 RTU，也可以提前把 TCP 默认参数一并配好，便于后续切换模板直接生成。")
        }
        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = "TCP Host",
                    value = state.tcpDefaults.host,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(tcpDefaults = current.tcpDefaults.copy(host = value))
                        }
                    },
                    description = "对应 host。",
                )
            }
            item {
                CodegenTextField(
                    label = "TCP Port",
                    value = state.tcpDefaults.portText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(tcpDefaults = current.tcpDefaults.copy(portText = value))
                        }
                    },
                    description = "对应 port。",
                )
            }
            item {
                CodegenTextField(
                    label = "TCP 单元 ID",
                    value = state.tcpDefaults.unitIdText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(tcpDefaults = current.tcpDefaults.copy(unitIdText = value))
                        }
                    },
                    description = "对应 unitId。",
                )
            }
            item {
                CodegenTextField(
                    label = "TCP 超时(ms)",
                    value = state.tcpDefaults.timeoutMsText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(tcpDefaults = current.tcpDefaults.copy(timeoutMsText = value))
                        }
                    },
                    description = "对应 timeoutMs。",
                )
            }
            item {
                CodegenTextField(
                    label = "TCP 重试次数",
                    value = state.tcpDefaults.retriesText,
                    onValueChange = { value ->
                        viewModel.updateGenerationSettings { current ->
                            current.copy(tcpDefaults = current.tcpDefaults.copy(retriesText = value))
                        }
                    },
                    description = "对应 retries。",
                )
            }
        }
    }
}

@Composable
private fun ApiOverviewPanel(
    readSchemas: List<CodegenSchemaEditorState>,
    writeSchemas: List<CodegenSchemaEditorState>,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
) {
    CodegenPanel(
        title = "接口概览",
        subtitle = "这里直接预览最终生成的顶层 API 形状，避免只盯着底层寄存器块。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CodegenStatusStrip("读取接口 ${readSchemas.size} 个，写入接口 ${writeSchemas.size} 个。")
            selectedProtocolTemplate?.let { template ->
                CodegenStatusStrip("当前协议上下文来自 ${template.name}，生成器会按该模板约束功能码与 transport 语义。")
            }
            CodegenStatusStrip("当前顶层契约仍以现有生成链路为准：READ 方法无 request 参数，WRITE 方法按字段展开成参数。")

            if (readSchemas.isEmpty()) {
                CodegenStatusStrip("DeviceApi 还没有任何读取方法。")
            } else {
                readSchemas.forEach { schema ->
                    CodegenStatusStrip("DeviceApi.${schema.topLevelSignaturePreview()}")
                }
            }

            if (writeSchemas.isEmpty()) {
                CodegenStatusStrip("DeviceWriteApi 还没有任何写入方法。")
            } else {
                writeSchemas.forEach { schema ->
                    CodegenStatusStrip("DeviceWriteApi.${schema.topLevelSignaturePreview()}")
                }
            }
        }
    }
}

@Composable
private fun MethodGroupPanel(
    title: String,
    subtitle: String,
    emptyHint: String,
    addActionText: String,
    direction: CodegenSchemaDirection,
    schemas: List<IndexedValue<CodegenSchemaEditorState>>,
    viewModel: CodegenContextViewModel,
) {
    CodegenPanel(
        title = title,
        subtitle = subtitle,
        actions = {
            WorkbenchActionButton(
                text = addActionText,
                onClick = { viewModel.addSchema(direction) },
                imageVector = Icons.Outlined.Code,
            )
        },
    ) {
        if (schemas.isEmpty()) {
            CodegenStatusStrip(emptyHint)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                schemas.forEachIndexed { position, indexed ->
                    MethodEditorCard(
                        position = position,
                        schemaIndex = indexed.index,
                        schema = indexed.value,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodEditorCard(
    position: Int,
    schemaIndex: Int,
    schema: CodegenSchemaEditorState,
    viewModel: CodegenContextViewModel,
) {
    CodegenPanel(
        title = schema.name.ifBlank { schema.direction.defaultMethodTitle(position) },
        subtitle = schema.topLevelSignaturePreview(),
        actions = {
            WorkbenchActionButton(
                text = schema.direction.addFieldActionLabel(),
                onClick = { viewModel.addField(schemaIndex) },
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = "删除方法",
                onClick = { viewModel.removeSchema(schemaIndex) },
                variant = WorkbenchButtonVariant.Destructive,
            )
        },
    ) {
        CodegenStatusStrip("接口归属：${schema.ownerInterfaceName()}；当前生成签名：${schema.topLevelSignaturePreview()}")
        CodegenStatusStrip(schema.methodBindingHint())

        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = "方法说明",
                    value = schema.name,
                    onValueChange = { value -> viewModel.updateSchemaName(schemaIndex, value) },
                    placeholder = "例如 读取 Flash 持久化配置",
                    description = "可直接写中文说明；方法名会按约定自动派生。",
                )
            }
            item {
                CodegenTextField(
                    label = "方法名",
                    value = schema.methodName,
                    onValueChange = { value -> viewModel.updateSchemaMethodName(schemaIndex, value) },
                    placeholder = schema.name.toGeneratedMethodName(),
                    description = "最终进入 ${schema.ownerInterfaceName()} 的 Kotlin 方法名。",
                )
            }
            fullWidth {
                CodegenTextField(
                    label = "方法备注",
                    value = schema.description,
                    onValueChange = { value ->
                        viewModel.updateSchema(schemaIndex) { current -> current.copy(description = value) }
                    },
                    singleLine = false,
                    description = "建议写清读取/写入语义、设备限制和寄存器块用途。",
                )
            }
            item {
                CodegenSelectionField(
                    label = "接口归属",
                    options =
                        CodegenSchemaDirection.entries.map { option ->
                            CodegenOption(option, option.interfaceLabel(), option.directionHint())
                        },
                    selectedValue = schema.direction,
                    onSelected = { selected ->
                        viewModel.updateSchemaDirection(schemaIndex, selected ?: CodegenSchemaDirection.READ)
                    },
                    description = schema.direction.directionHint(),
                )
            }
            item {
                CodegenSelectionField(
                    label = "功能码",
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
            }
            item {
                CodegenTextField(
                    label = if (schema.functionCode.expectsCoilSpace()) "起始 Coil 地址" else "起始寄存器地址",
                    value = schema.baseAddressText,
                    onValueChange = { value ->
                        viewModel.updateSchema(schemaIndex) { current -> current.copy(baseAddressText = value) }
                    },
                    description = "字段偏移都会相对这里继续展开。",
                )
            }
            item {
                CodegenStatusStrip(schema.responseBindingHint())
            }
        }

        if (schema.fields.isEmpty()) {
            CodegenStatusStrip(schema.direction.emptyFieldHint())
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                schema.fields.forEachIndexed { fieldIndex, field ->
                    FieldEditorCard(
                        schemaIndex = schemaIndex,
                        fieldIndex = fieldIndex,
                        schema = schema,
                        field = field,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldEditorCard(
    schemaIndex: Int,
    fieldIndex: Int,
    schema: CodegenSchemaEditorState,
    field: CodegenFieldEditorState,
    viewModel: CodegenContextViewModel,
) {
    CodegenPanel(
        title = field.name.ifBlank { schema.direction.defaultFieldTitle(fieldIndex) },
        subtitle = field.parameterPreview(schema),
        actions = {
            WorkbenchActionButton(
                text = "删除${schema.direction.fieldEntityLabel()}",
                onClick = { viewModel.removeField(schemaIndex, fieldIndex) },
                variant = WorkbenchButtonVariant.Destructive,
            )
        },
    ) {
        CodegenStatusStrip(field.layoutHint(schema.baseAddressText, schema.functionCode))

        CodegenFormGrid {
            item {
                CodegenTextField(
                    label = schema.direction.fieldDescriptionLabel(),
                    value = field.name,
                    onValueChange = { value -> viewModel.updateFieldName(schemaIndex, fieldIndex, value) },
                    placeholder = "例如 魔术字 / 串口参数 / 故障灯开关",
                    description = "可直接写中文说明；属性名会按约定自动派生。",
                )
            }
            item {
                CodegenTextField(
                    label = schema.direction.fieldIdentifierLabel(),
                    value = field.propertyName,
                    onValueChange = { value ->
                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(propertyName = value) }
                    },
                    placeholder = field.name.toGeneratedPropertyName(),
                    description = "最终进入 ${schema.direction.fieldContainerName(schema)} 的 Kotlin 标识符。",
                )
            }
            fullWidth {
                CodegenTextField(
                    label = "${schema.direction.fieldEntityLabel()}备注",
                    value = field.description,
                    onValueChange = { value ->
                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(description = value) }
                    },
                    singleLine = false,
                    description = "建议写清单位、范围和业务语义。",
                )
            }
            item {
                CodegenSelectionField(
                    label = "传输类型",
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
            }
            item {
                CodegenTextField(
                    label = if (schema.functionCode.expectsCoilSpace()) "Coil 偏移" else "寄存器偏移",
                    value = field.registerOffsetText,
                    onValueChange = { value ->
                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(registerOffsetText = value) }
                    },
                    description = field.registerOffsetHint(schema.baseAddressText, schema.functionCode),
                )
            }
            item {
                CodegenTextField(
                    label = "长度",
                    value = field.lengthText,
                    onValueChange = { value ->
                        viewModel.updateField(schemaIndex, fieldIndex) { current -> current.copy(lengthText = value) }
                    },
                    description = field.transportType.lengthFieldHint(),
                )
            }
            item {
                CodegenStatusStrip(field.transportType.kotlinTypeHint())
            }
        }
    }
}

private fun CodegenSchemaDirection.allowedFunctionCodes(): List<CodegenFunctionCode> {
    return when (this) {
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
}

private fun CodegenSchemaDirection.interfaceLabel(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "DeviceApi"
        CodegenSchemaDirection.WRITE -> "DeviceWriteApi"
    }
}

private fun CodegenSchemaDirection.directionHint(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "读取类方法，会生成到 DeviceApi。"
        CodegenSchemaDirection.WRITE -> "写入类方法，会生成到 DeviceWriteApi。"
    }
}

private fun CodegenSchemaDirection.defaultMethodTitle(position: Int): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "读取方法 ${position + 1}"
        CodegenSchemaDirection.WRITE -> "写入方法 ${position + 1}"
    }
}

private fun CodegenSchemaDirection.addFieldActionLabel(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "新增响应字段"
        CodegenSchemaDirection.WRITE -> "新增写入字段"
    }
}

private fun CodegenSchemaDirection.emptyFieldHint(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "当前读取方法还没有任何响应字段。"
        CodegenSchemaDirection.WRITE -> "当前写入方法还没有任何写入字段。"
    }
}

private fun CodegenSchemaDirection.fieldDescriptionLabel(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "字段说明"
        CodegenSchemaDirection.WRITE -> "参数说明"
    }
}

private fun CodegenSchemaDirection.fieldIdentifierLabel(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "属性名"
        CodegenSchemaDirection.WRITE -> "参数名"
    }
}

private fun CodegenSchemaDirection.fieldEntityLabel(): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "字段"
        CodegenSchemaDirection.WRITE -> "参数"
    }
}

private fun CodegenSchemaDirection.defaultFieldTitle(fieldIndex: Int): String {
    return when (this) {
        CodegenSchemaDirection.READ -> "响应字段 ${fieldIndex + 1}"
        CodegenSchemaDirection.WRITE -> "写入参数 ${fieldIndex + 1}"
    }
}

private fun CodegenSchemaDirection.fieldContainerName(schema: CodegenSchemaEditorState): String {
    return when (this) {
        CodegenSchemaDirection.READ -> schema.responseTypeName()
        CodegenSchemaDirection.WRITE -> schema.ownerInterfaceName()
    }
}

private fun CodegenSchemaEditorState.ownerInterfaceName(): String {
    return when (direction) {
        CodegenSchemaDirection.READ -> "DeviceApi"
        CodegenSchemaDirection.WRITE -> "DeviceWriteApi"
    }
}

private fun CodegenSchemaEditorState.effectiveMethodName(): String {
    return methodName.takeIf { it.isNotBlank() } ?: name.toGeneratedMethodName()
}

private fun CodegenSchemaEditorState.responseTypeName(): String {
    val modelTypeName = modelName.takeIf { it.isNotBlank() } ?: effectiveMethodName().toGeneratedTypeName(defaultName = "GeneratedModel")
    return "${modelTypeName}Registers"
}

private fun CodegenSchemaEditorState.methodBindingHint(): String {
    return when (direction) {
        CodegenSchemaDirection.READ ->
            "响应会进入 ${responseTypeName()}；当前顶层读取方法不带 request 参数。"

        CodegenSchemaDirection.WRITE ->
            "当前顶层写入方法会把每个字段直接展开成参数，并返回 ModbusCommandResult。"
    }
}

private fun CodegenSchemaEditorState.responseBindingHint(): String {
    return when (direction) {
        CodegenSchemaDirection.READ ->
            "当前响应 DTO：${responseTypeName()}"

        CodegenSchemaDirection.WRITE ->
            "当前写接口参数预览：${parameterPreview()}"
    }
}

private fun CodegenSchemaEditorState.topLevelSignaturePreview(): String {
    return when (direction) {
        CodegenSchemaDirection.READ -> "suspend fun ${effectiveMethodName()}(): ${responseTypeName()}"
        CodegenSchemaDirection.WRITE -> {
            val parameters = parameterPreview()
            "suspend fun ${effectiveMethodName()}($parameters): ModbusCommandResult"
        }
    }
}

private fun CodegenSchemaEditorState.parameterPreview(): String {
    return fields.joinToString(separator = ", ") { field ->
        "${field.effectivePropertyName()}: ${field.transportType.kotlinTypeName()}"
    }
}

private fun CodegenFieldEditorState.effectivePropertyName(): String {
    return propertyName.takeIf { it.isNotBlank() } ?: name.toGeneratedPropertyName()
}

private fun CodegenFieldEditorState.parameterPreview(schema: CodegenSchemaEditorState): String {
    val prefix =
        when (schema.direction) {
            CodegenSchemaDirection.READ -> "DTO 属性"
            CodegenSchemaDirection.WRITE -> "方法参数"
        }
    return "$prefix ${effectivePropertyName()}: ${transportType.kotlinTypeName()}"
}

private fun CodegenFunctionCode.functionCodeHint(): String {
    return when (this) {
        CodegenFunctionCode.READ_COILS -> "读线圈，字段类型必须使用 BOOL_COIL。"
        CodegenFunctionCode.READ_DISCRETE_INPUTS -> "读离散输入，字段类型必须使用 BOOL_COIL。"
        CodegenFunctionCode.READ_INPUT_REGISTERS -> "读输入寄存器，适合只读寄存器块。"
        CodegenFunctionCode.READ_HOLDING_REGISTERS -> "读保持寄存器，适合持久化配置和状态快照。"
        CodegenFunctionCode.WRITE_SINGLE_COIL -> "单线圈写入，必须只有一个 BOOL_COIL 字段。"
        CodegenFunctionCode.WRITE_MULTIPLE_COILS -> "批量线圈写入，所有字段都必须使用 BOOL_COIL。"
        CodegenFunctionCode.WRITE_SINGLE_REGISTER -> "单寄存器写入，必须只有一个 U16 字段。"
        CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS -> "批量寄存器写入，适合结构化配置写回。"
    }
}

private fun CodegenTransportType.transportHint(): String {
    return when (this) {
        CodegenTransportType.BOOL_COIL -> "线圈布尔值，占 1 个 coil。"
        CodegenTransportType.U8 -> "单寄存器低 8 位。"
        CodegenTransportType.U16 -> "单个 16 位寄存器。"
        CodegenTransportType.U32_BE -> "2 个寄存器组成的大端 32 位值。"
        CodegenTransportType.BYTE_ARRAY -> "原始字节数组，按字节数填写长度。"
        CodegenTransportType.STRING_ASCII -> "ASCII 字符串，长度按寄存器个数填写。"
        CodegenTransportType.STRING_UTF8 -> "UTF-8 字符串，长度按寄存器个数填写。"
    }
}

private fun CodegenTransportType.lengthFieldHint(): String {
    return when (this) {
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

private fun CodegenTransportType.kotlinTypeHint(): String {
    return "当前 Kotlin 类型：${kotlinTypeName()}"
}

private fun CodegenTransportType.kotlinTypeName(): String {
    return when (this) {
        CodegenTransportType.BOOL_COIL -> "Boolean"
        CodegenTransportType.U8,
        CodegenTransportType.U16,
        CodegenTransportType.U32_BE,
        -> "Int"

        CodegenTransportType.BYTE_ARRAY -> "ByteArray"
        CodegenTransportType.STRING_ASCII,
        CodegenTransportType.STRING_UTF8,
        -> "String"
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
        return "当前偏移还不是有效数字，保存前请补成非负整数。"
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
            "相对 schema 起始寄存器的偏移，保存前必须是非负整数。"
        } else {
            "相对 schema 起始寄存器的偏移；当前绝对起始地址为 $absoluteAddress。"
        }
    }
}

private fun CodegenFunctionCode.expectsCoilSpace(): Boolean {
    return when (this) {
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
}

private fun CodegenTransportType.registerWidth(length: Int): Int {
    return when (this) {
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
}

private fun ProtocolTemplateOptionDto.protocolContextHint(): String? {
    return when {
        code.contains("rtu", ignoreCase = true) ->
            "当前模板是 Modbus RTU。transport gateway 会自动补齐串口上下文字段，这里只编辑 DeviceApi / DeviceWriteApi 的业务元数据。"

        code.contains("tcp", ignoreCase = true) ->
            "当前模板是 Modbus TCP。transport 层网络上下文会在生成实现里补齐，这里聚焦业务方法和字段布局。"

        else -> null
    }
}

private fun String.externalOutputHint(): String {
    return "外部产物会写到 $this/c/generated/modbus/<transport> 和 $this/markdown/generated/modbus/protocols。"
}
