package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.context.CodegenGenerationSettingsEditorState

@Composable
/**
 * 处理生成文件列表panel。
 *
 * @param generatedFiles 生成文件列表。
 */
internal fun GeneratedFilesPanel(
    generatedFiles: List<String>,
) {
    CupertinoPanel(
        title = "生成结果",
        subtitle = "这次生成实际落盘的文件列表，方便直接核对 Kotlin/C/文档产物。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            generatedFiles.forEach { file -> CupertinoStatusStrip(file) }
        }
    }
}

@Composable
/**
 * 处理上下文摘要panel。
 *
 * @param state 状态。
 * @param selectedProtocolTemplate 选中协议模板。
 * @param protocolTemplates 协议模板。
 * @param viewModel 视图模型。
 * @param saving saving。
 * @param generating generating。
 */
internal fun ContextSummaryPanel(
    state: CodegenContextEditorState,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
    protocolTemplates: List<ProtocolTemplateOptionDto>,
    viewModel: CodegenContextViewModel,
    saving: Boolean,
    generating: Boolean,
) {
    CupertinoPanel(
        title = state.name.ifBlank { "未命名代码生成上下文" },
        subtitle = "界面维护的是 class / method / property / context 关系，生成器再按协议上下文落到具体 Kotlin / C 产物。",
        actions = {
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = viewModel::save,
                enabled = !saving,
            )
            WorkbenchActionButton(
                text = if (generating) "生成中" else "生成",
                onClick = viewModel::generateSelected,
                enabled = state.protocolTemplateId != null && !saving && !generating,
                variant = WorkbenchButtonVariant.Secondary,
            )
        },
    ) {
        CupertinoFormGrid {
            item {
                CupertinoTextField(
                    label = "上下文编码",
                    value = state.code,
                    onValueChange = { value -> viewModel.updateContext { it.copy(code = value) } },
                    placeholder = "例如 MCU_DEVICE_DEFAULT",
                    description = "稳定标识一组可生成上下文，建议全局唯一。",
                )
            }
            item {
                CupertinoTextField(
                    label = "上下文名称",
                    value = state.name,
                    onValueChange = { value -> viewModel.updateContext { it.copy(name = value) } },
                    placeholder = "例如 Flash 持久化配置",
                    description = "列表里展示的名字，也会进入生成日志。",
                )
            }
            item {
                CupertinoSelectionField(
                    label = "协议模板",
                    options = protocolTemplates.map { CupertinoOption(it.id, it.name, it.code) },
                    selectedValue = state.protocolTemplateId,
                    onSelected = viewModel::selectProtocolTemplate,
                    allowClear = true,
                    description = selectedProtocolTemplate?.description ?: "协议模板决定当前可编辑的上下文定义。",
                )
            }
            item {
                CupertinoBooleanField(
                    label = "启用",
                    checked = state.enabled,
                    onCheckedChange = { checked -> viewModel.updateContext { it.copy(enabled = checked) } },
                    description = "禁用后元数据仍保留，但默认不参与当前生成快照。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "上下文说明",
                    value = state.description,
                    onValueChange = { value -> viewModel.updateContext { it.copy(description = value) } },
                    singleLine = false,
                    description = "建议写清设备型号、场景边界、生成目标和协议约束。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "外部 C 工程根目录",
                    value = state.externalCOutputRoot,
                    onValueChange = { value -> viewModel.updateContext { it.copy(externalCOutputRoot = value) } },
                    placeholder = "/abs/path/to/peer-project",
                    description = "点“生成”后，可把 C 侧与协议文档产物直接投递到外部工程目录。",
                )
            }
        }
        CupertinoStatusStrip("方法实体约定：每个方法都会约定为 <MethodName>Request / <MethodName>Response。")
        CupertinoStatusStrip("当前属性池 ${state.properties.size} 个，方法 ${state.methods.size} 个。属性与方法关系通过穿梭框维护。")
        selectedProtocolTemplate?.protocolContextHint()?.let { CupertinoStatusStrip(it) }
        state.externalCOutputRoot.takeIf { it.isNotBlank() }?.let { CupertinoStatusStrip(it.externalOutputHint()) }
    }
}

@Composable
/**
 * 处理生成设置panel。
 *
 * @param state 状态。
 * @param selectedProtocolTemplate 选中协议模板。
 * @param viewModel 视图模型。
 */
internal fun GenerationSettingsPanel(
    state: CodegenGenerationSettingsEditorState,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = "外部代码生成",
        subtitle = "这里配置生成器额外需要的目录和 transport 默认参数，给 Kotlin/C/文档生成共用。",
    ) {
        selectedProtocolTemplate?.let { template ->
            CupertinoStatusStrip("当前主协议模板：${template.name}（${template.code}）")
        }
        CupertinoFormGrid {
            item { CupertinoTextField("服务端输出根目录", state.serverOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(serverOutputRoot = value) } }, placeholder = "留空使用后台默认 build/generated 目录") }
            item { CupertinoTextField("共享输出根目录", state.sharedOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(sharedOutputRoot = value) } }, placeholder = "留空使用后台默认 build/generated 目录") }
            item { CupertinoTextField("Gateway 输出根目录", state.gatewayOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(gatewayOutputRoot = value) } }, placeholder = "按需指定 transport gateway 代码目录") }
            item { CupertinoTextField("API Client 输出根目录", state.apiClientOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(apiClientOutputRoot = value) } }, placeholder = "建议指向 apps/kcloud/plugins/<plugin>/api/build/generated/source/controller2api/commonMain/kotlin") }
            item { CupertinoTextField("API Client 包名", state.apiClientPackageName, { value -> viewModel.updateGenerationSettings { it.copy(apiClientPackageName = value) } }, placeholder = "建议使用 site.addzero.<plugin>.api.external.generated") }
            item { CupertinoTextField("Spring Route 输出根目录", state.springRouteOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(springRouteOutputRoot = value) } }, placeholder = "按需指定 route 生成目录") }
            item { CupertinoTextField("C 代码输出根目录", state.cOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(cOutputRoot = value) } }, placeholder = "/abs/path/to/generated/c") }
            item { CupertinoTextField("Markdown 输出根目录", state.markdownOutputRoot, { value -> viewModel.updateGenerationSettings { it.copy(markdownOutputRoot = value) } }, placeholder = "/abs/path/to/generated/docs") }
        }
        GenerationDefaultsPanel(
            title = "RTU 默认参数",
            subtitle = "当协议上下文需要 RTU 相关默认值时，生成器直接取这里。",
        ) {
            item { CupertinoTextField("串口路径", state.rtuDefaults.portPath, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(portPath = value)) } }) }
            item { CupertinoTextField("Unit ID", state.rtuDefaults.unitIdText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(unitIdText = value)) } }) }
            item { CupertinoTextField("波特率", state.rtuDefaults.baudRateText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(baudRateText = value)) } }) }
            item { CupertinoTextField("数据位", state.rtuDefaults.dataBitsText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(dataBitsText = value)) } }) }
            item { CupertinoTextField("停止位", state.rtuDefaults.stopBitsText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(stopBitsText = value)) } }) }
            item { CupertinoTextField("校验位", state.rtuDefaults.parity, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(parity = value)) } }) }
            item { CupertinoTextField("超时(ms)", state.rtuDefaults.timeoutMsText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(timeoutMsText = value)) } }) }
            item { CupertinoTextField("重试次数", state.rtuDefaults.retriesText, { value -> viewModel.updateGenerationSettings { it.copy(rtuDefaults = it.rtuDefaults.copy(retriesText = value)) } }) }
        }
        GenerationDefaultsPanel(
            title = "TCP 默认参数",
            subtitle = "生成 TCP 侧产物时使用。",
        ) {
            item { CupertinoTextField("Host", state.tcpDefaults.host, { value -> viewModel.updateGenerationSettings { it.copy(tcpDefaults = it.tcpDefaults.copy(host = value)) } }) }
            item { CupertinoTextField("Port", state.tcpDefaults.portText, { value -> viewModel.updateGenerationSettings { it.copy(tcpDefaults = it.tcpDefaults.copy(portText = value)) } }) }
            item { CupertinoTextField("Unit ID", state.tcpDefaults.unitIdText, { value -> viewModel.updateGenerationSettings { it.copy(tcpDefaults = it.tcpDefaults.copy(unitIdText = value)) } }) }
            item { CupertinoTextField("超时(ms)", state.tcpDefaults.timeoutMsText, { value -> viewModel.updateGenerationSettings { it.copy(tcpDefaults = it.tcpDefaults.copy(timeoutMsText = value)) } }) }
            item { CupertinoTextField("重试次数", state.tcpDefaults.retriesText, { value -> viewModel.updateGenerationSettings { it.copy(tcpDefaults = it.tcpDefaults.copy(retriesText = value)) } }) }
        }
        GenerationDefaultsPanel(
            title = "MQTT 默认参数",
            subtitle = "生成 MQTT 侧产物时使用。",
        ) {
            item { CupertinoTextField("Broker URL", state.mqttDefaults.brokerUrl, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(brokerUrl = value)) } }) }
            item { CupertinoTextField("Client ID", state.mqttDefaults.clientId, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(clientId = value)) } }) }
            item { CupertinoTextField("Request Topic", state.mqttDefaults.requestTopic, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(requestTopic = value)) } }) }
            item { CupertinoTextField("Response Topic", state.mqttDefaults.responseTopic, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(responseTopic = value)) } }) }
            item { CupertinoTextField("QoS", state.mqttDefaults.qosText, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(qosText = value)) } }) }
            item { CupertinoTextField("超时(ms)", state.mqttDefaults.timeoutMsText, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(timeoutMsText = value)) } }) }
            item { CupertinoTextField("重试次数", state.mqttDefaults.retriesText, { value -> viewModel.updateGenerationSettings { it.copy(mqttDefaults = it.mqttDefaults.copy(retriesText = value)) } }) }
        }
    }
}

@Composable
/**
 * 处理生成默认panel。
 *
 * @param title title。
 * @param subtitle subtitle。
 * @param content content。
 */
private fun GenerationDefaultsPanel(
    title: String,
    subtitle: String,
    content: site.addzero.cupertino.workbench.components.form.CupertinoFormGridScope.() -> Unit,
) {
    CupertinoPanel(title = title, subtitle = subtitle) {
        CupertinoFormGrid(content = content)
    }
}

@Composable
/**
 * 处理上下文定义panel。
 *
 * @param definitions 定义。
 * @param selectedProtocolTemplate 选中协议模板。
 */
internal fun ContextDefinitionsPanel(
    definitions: List<CodegenContextDefinitionDto>,
    selectedProtocolTemplate: ProtocolTemplateOptionDto?,
) {
    CupertinoPanel(
        title = "上下文定义",
        subtitle = "这里展示当前协议模板可用的 context definition，方法和字段编辑器都会按这里的参数动态渲染。",
    ) {
        if (selectedProtocolTemplate == null) {
            CupertinoStatusStrip("先选择协议模板，才能加载对应的上下文定义。")
            return@CupertinoPanel
        }
        if (definitions.isEmpty()) {
            CupertinoStatusStrip("当前协议模板还没有任何可用的上下文定义。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            definitions.forEach { definition ->
                CupertinoPanel(title = definition.name, subtitle = definition.description ?: definition.code) {
                    CupertinoStatusStrip(
                        "作用域：${definition.targetKind.name} · 绑定模式：${definition.bindingTargetMode.name} · 来源：${definition.sourceKind.name}",
                    )
                    if (definition.params.isEmpty()) {
                        CupertinoStatusStrip("该定义没有额外参数。")
                    } else {
                        definition.params.forEach { param ->
                            CupertinoStatusStrip(
                                buildString {
                                    append(param.name)
                                    append("（${param.code}） · ${param.valueType.name}")
                                    if (param.required) append(" · 必填")
                                    if (!param.defaultValue.isNullOrBlank()) append(" · 默认值 ${param.defaultValue}")
                                    if (param.enumOptions.isNotEmpty()) append(" · 选项 ${param.enumOptions.joinToString()}")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
