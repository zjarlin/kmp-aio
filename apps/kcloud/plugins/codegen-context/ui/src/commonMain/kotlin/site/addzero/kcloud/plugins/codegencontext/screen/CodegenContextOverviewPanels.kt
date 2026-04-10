package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataExportResultDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataFirmwareSyncDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataMqttDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataPreviewDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataRtuDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMetadataTcpDefaultsDraftDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextScreenState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataArtifactKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataIssueSeverity
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenMetadataTransportKind
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextDraftPanelActionsSpi

@Composable
internal fun ContextDraftPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val draftPanelActionsSpi = koinInject<CodegenContextDraftPanelActionsSpi>()
    val draft = state.draft
    val protocolTemplates = state.protocolTemplates
    val selectedProtocolTemplateDescription = protocolTemplates
        .firstOrNull { item -> item.id == draft.protocolTemplateId }
        ?.description
    CupertinoPanel(
        title = "基础信息",
        subtitle = "页面只维护元数据草稿；方法名、属性名、类型推导与导出可行性全部交给后端预检。",
        actions = {
            draftPanelActionsSpi.Render(
                state = state,
                viewModel = viewModel,
            )
        },
    ) {
        CupertinoFormGrid {
            item {
                CupertinoTextField(
                    label = "上下文编码",
                    value = draft.code,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(code = value) } },
                    placeholder = "例如 MCU_DEVICE_DEFAULT",
                    description = "稳定编码，供 metadata snapshot 和导出结果引用。",
                )
            }
            item {
                CupertinoTextField(
                    label = "上下文名称",
                    value = draft.name,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(name = value) } },
                    placeholder = "例如 控制器寄存器协议",
                    description = "人可读标题。",
                )
            }
            item {
                CupertinoSelectionField(
                    label = "协议模板",
                    options = protocolTemplates.map { option -> CupertinoOption(option.id, option.name, option.code) },
                    selectedValue = draft.protocolTemplateId.takeIf { id -> id > 0L },
                    onSelected = viewModel::selectProtocolTemplate,
                    allowClear = true,
                    description = selectedProtocolTemplateDescription ?: "协议模板决定可编辑的 definition 集合。",
                )
            }
            item {
                CupertinoBooleanField(
                    label = "启用",
                    checked = draft.enabled,
                    onCheckedChange = { value -> viewModel.updateDraft { it.copy(enabled = value) } },
                    description = "停用后不会作为默认 selected snapshot 使用。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "上下文说明",
                    value = draft.description.orEmpty(),
                    onValueChange = { value -> viewModel.updateDraft { it.copy(description = value) } },
                    singleLine = false,
                )
            }
        }
        CupertinoStatusStrip("Kotlin 调用侧只输出 metadata snapshot，Kotlin 源码仍由 addzero-lib-jvm 的 KSP database provider 消费。")
        CupertinoStatusStrip("C 暴露侧直接导出 C contract、dispatch、Markdown 和工程同步；compose 端不做命名、类型或请求/响应模型拼装。")
    }
}

@Composable
internal fun ExportWorkbenchPanel(
    state: CodegenContextScreenState,
    viewModel: CodegenContextViewModel,
) {
    val draft = state.draft
    CupertinoPanel(
        title = "导出与预检",
        subtitle = "导出目标、两组 transport 和默认参数都是原始输入；必填项、解析结果和导出计划来自后端 preview。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TransportSelectionPanel(
                title = "Kotlin 调用侧",
                subtitle = "这里只声明后续 KSP database provider 要消费哪些 transport 的 metadata snapshot。",
                selectedTransports = draft.exportSettings.kotlinClientTransports,
                onToggle = viewModel::toggleKotlinClientTransport,
            )
            CupertinoStatusStrip("KSP 侧推荐查询：select payload from codegen_context_modbus_contract where selected = 1 and transport = '${'$'}{transport}'。")
            CupertinoPanel(
                title = "C 暴露侧",
                subtitle = "当前页面只负责固件侧合同、dispatch、协议文档和工程同步。",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TransportSelectionPanel(
                        title = "暴露 transport",
                        subtitle = "选择本次要输出到固件工程的 transport。",
                        selectedTransports = draft.exportSettings.cExposeTransports,
                        onToggle = viewModel::toggleCExposeTransport,
                    )
                    ArtifactSelectionPanel(
                        selectedArtifactKinds = draft.exportSettings.artifactKinds,
                        onToggle = viewModel::toggleArtifactKind,
                    )
                    FirmwareSyncPanel(
                        firmwareSync = draft.exportSettings.firmwareSync,
                        onUpdate = viewModel::updateFirmwareSync,
                    )
                }
            }
            TransportDefaultsPanel(
                rtuDefaults = draft.exportSettings.rtuDefaults,
                tcpDefaults = draft.exportSettings.tcpDefaults,
                mqttDefaults = draft.exportSettings.mqttDefaults,
                onUpdateRtuDefaults = viewModel::updateRtuDefaults,
                onUpdateTcpDefaults = viewModel::updateTcpDefaults,
                onUpdateMqttDefaults = viewModel::updateMqttDefaults,
            )
            PreviewPanel(state.preview)
            ExportResultPanel(state.exportResult)
        }
    }
}

@Composable
private fun TransportSelectionPanel(
    title: String,
    subtitle: String,
    selectedTransports: Set<CodegenMetadataTransportKind>,
    onToggle: (CodegenMetadataTransportKind) -> Unit,
) {
    CupertinoPanel(title = title, subtitle = subtitle) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CodegenMetadataTransportKind.entries.forEach { transport ->
                CupertinoBooleanField(
                    label = transport.name,
                    checked = transport in selectedTransports,
                    onCheckedChange = { onToggle(transport) },
                )
            }
        }
    }
}

@Composable
private fun ArtifactSelectionPanel(
    selectedArtifactKinds: Set<CodegenMetadataArtifactKind>,
    onToggle: (CodegenMetadataArtifactKind) -> Unit,
) {
    CupertinoPanel(
        title = "导出目标",
        subtitle = "目标集是多选；不再是单一“生成”动作。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CodegenMetadataArtifactKind.entries.forEach { artifactKind ->
                CupertinoBooleanField(
                    label = artifactKind.name,
                    checked = artifactKind in selectedArtifactKinds,
                    onCheckedChange = { onToggle(artifactKind) },
                )
            }
        }
    }
}

@Composable
private fun FirmwareSyncPanel(
    firmwareSync: CodegenMetadataFirmwareSyncDto,
    onUpdate: (transform: (CodegenMetadataFirmwareSyncDto) -> CodegenMetadataFirmwareSyncDto) -> Unit,
) {
    CupertinoPanel(
        title = "固件工程同步",
        subtitle = "Keil/CubeMX 路径只做透传，后台复用 addzero-lib-jvm 的同步工具。",
    ) {
        CupertinoFormGrid {
            fullWidth {
                CupertinoTextField(
                    label = "C 工程根目录",
                    value = firmwareSync.cOutputProjectDir,
                    onValueChange = { value -> onUpdate { it.copy(cOutputProjectDir = value) } },
                    placeholder = "/abs/path/to/firmware-project",
                )
            }
            item {
                CupertinoTextField(
                    label = "bridge 实现路径",
                    value = firmwareSync.bridgeImplPath,
                    onValueChange = { value -> onUpdate { it.copy(bridgeImplPath = value) } },
                    placeholder = "Core/Src/modbus",
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil uvprojx",
                    value = firmwareSync.keilUvprojxPath,
                    onValueChange = { value -> onUpdate { it.copy(keilUvprojxPath = value) } },
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil target",
                    value = firmwareSync.keilTargetName,
                    onValueChange = { value -> onUpdate { it.copy(keilTargetName = value) } },
                )
            }
            item {
                CupertinoTextField(
                    label = "Keil group",
                    value = firmwareSync.keilGroupName,
                    onValueChange = { value -> onUpdate { it.copy(keilGroupName = value) } },
                )
            }
            item {
                CupertinoTextField(
                    label = "CubeMX mxproject",
                    value = firmwareSync.mxprojectPath,
                    onValueChange = { value -> onUpdate { it.copy(mxprojectPath = value) } },
                )
            }
        }
    }
}

@Composable
private fun TransportDefaultsPanel(
    rtuDefaults: CodegenMetadataRtuDefaultsDraftDto,
    tcpDefaults: CodegenMetadataTcpDefaultsDraftDto,
    mqttDefaults: CodegenMetadataMqttDefaultsDraftDto,
    onUpdateRtuDefaults: (transform: (CodegenMetadataRtuDefaultsDraftDto) -> CodegenMetadataRtuDefaultsDraftDto) -> Unit,
    onUpdateTcpDefaults: (transform: (CodegenMetadataTcpDefaultsDraftDto) -> CodegenMetadataTcpDefaultsDraftDto) -> Unit,
    onUpdateMqttDefaults: (transform: (CodegenMetadataMqttDefaultsDraftDto) -> CodegenMetadataMqttDefaultsDraftDto) -> Unit,
) {
    CupertinoPanel(
        title = "默认参数",
        subtitle = "RTU/TCP/MQTT 默认值直接透传给后端，用于渲染 C 导出和 metadata snapshot 说明。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CupertinoPanel(title = "RTU", subtitle = "串口默认参数") {
                TwoColumnDefaultsForm(
                    left = {
                        CupertinoTextField(label = "端口", value = rtuDefaults.portPath, onValueChange = { value -> onUpdateRtuDefaults { it.copy(portPath = value) } })
                        CupertinoTextField(label = "unitId", value = rtuDefaults.unitId, onValueChange = { value -> onUpdateRtuDefaults { it.copy(unitId = value) } })
                        CupertinoTextField(label = "baudRate", value = rtuDefaults.baudRate, onValueChange = { value -> onUpdateRtuDefaults { it.copy(baudRate = value) } })
                        CupertinoTextField(label = "dataBits", value = rtuDefaults.dataBits, onValueChange = { value -> onUpdateRtuDefaults { it.copy(dataBits = value) } })
                    },
                    right = {
                        CupertinoTextField(label = "stopBits", value = rtuDefaults.stopBits, onValueChange = { value -> onUpdateRtuDefaults { it.copy(stopBits = value) } })
                        CupertinoTextField(label = "parity", value = rtuDefaults.parity, onValueChange = { value -> onUpdateRtuDefaults { it.copy(parity = value) } })
                        CupertinoTextField(label = "timeoutMs", value = rtuDefaults.timeoutMs, onValueChange = { value -> onUpdateRtuDefaults { it.copy(timeoutMs = value) } })
                        CupertinoTextField(label = "retries", value = rtuDefaults.retries, onValueChange = { value -> onUpdateRtuDefaults { it.copy(retries = value) } })
                    },
                )
            }
            CupertinoPanel(title = "TCP", subtitle = "网络默认参数") {
                TwoColumnDefaultsForm(
                    left = {
                        CupertinoTextField(label = "host", value = tcpDefaults.host, onValueChange = { value -> onUpdateTcpDefaults { it.copy(host = value) } })
                        CupertinoTextField(label = "port", value = tcpDefaults.port, onValueChange = { value -> onUpdateTcpDefaults { it.copy(port = value) } })
                        CupertinoTextField(label = "unitId", value = tcpDefaults.unitId, onValueChange = { value -> onUpdateTcpDefaults { it.copy(unitId = value) } })
                    },
                    right = {
                        CupertinoTextField(label = "timeoutMs", value = tcpDefaults.timeoutMs, onValueChange = { value -> onUpdateTcpDefaults { it.copy(timeoutMs = value) } })
                        CupertinoTextField(label = "retries", value = tcpDefaults.retries, onValueChange = { value -> onUpdateTcpDefaults { it.copy(retries = value) } })
                    },
                )
            }
            CupertinoPanel(title = "MQTT", subtitle = "消息通道默认参数") {
                CupertinoFormGrid {
                    item {
                        CupertinoTextField(label = "brokerUrl", value = mqttDefaults.brokerUrl, onValueChange = { value -> onUpdateMqttDefaults { it.copy(brokerUrl = value) } })
                    }
                    item {
                        CupertinoTextField(label = "clientId", value = mqttDefaults.clientId, onValueChange = { value -> onUpdateMqttDefaults { it.copy(clientId = value) } })
                    }
                    item {
                        CupertinoTextField(label = "requestTopic", value = mqttDefaults.requestTopic, onValueChange = { value -> onUpdateMqttDefaults { it.copy(requestTopic = value) } })
                    }
                    item {
                        CupertinoTextField(label = "responseTopic", value = mqttDefaults.responseTopic, onValueChange = { value -> onUpdateMqttDefaults { it.copy(responseTopic = value) } })
                    }
                    item {
                        CupertinoTextField(label = "qos", value = mqttDefaults.qos, onValueChange = { value -> onUpdateMqttDefaults { it.copy(qos = value) } })
                    }
                    item {
                        CupertinoTextField(label = "timeoutMs", value = mqttDefaults.timeoutMs, onValueChange = { value -> onUpdateMqttDefaults { it.copy(timeoutMs = value) } })
                    }
                    item {
                        CupertinoTextField(label = "retries", value = mqttDefaults.retries, onValueChange = { value -> onUpdateMqttDefaults { it.copy(retries = value) } })
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoColumnDefaultsForm(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            left()
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            right()
        }
    }
}

@Composable
private fun PreviewPanel(
    preview: CodegenMetadataPreviewDto?,
) {
    CupertinoPanel(
        title = "预检结果",
        subtitle = "resolved 名称、类型、导出计划和错误提示全部来自服务端 preview。",
    ) {
        if (preview == null) {
            CupertinoStatusStrip("还没有预检结果。点击“预检”后由后端返回解析结果。")
            return@CupertinoPanel
        }
        if (preview.issues.isEmpty()) {
            CupertinoStatusStrip("当前预检没有返回错误。")
        } else {
            preview.issues.forEach { issue ->
                val tone =
                    when (issue.severity) {
                        CodegenMetadataIssueSeverity.ERROR -> androidx.compose.ui.graphics.Color(0xFFFFE8E6)
                        CodegenMetadataIssueSeverity.WARNING -> androidx.compose.ui.graphics.Color(0xFFFFF5D6)
                        CodegenMetadataIssueSeverity.INFO -> androidx.compose.ui.graphics.Color(0xFFEAF4FF)
                    }
                CupertinoStatusStrip(text = "${issue.location}: ${issue.message}", tone = tone)
            }
        }
        if (preview.resolvedFunctions.isNotEmpty()) {
            CupertinoPanel(title = "设备功能解析", subtitle = "方法名、请求/响应模型由后端推导。") {
                preview.resolvedFunctions.forEach { item ->
                    CupertinoKeyValueRow(item.name, item.resolvedMethodName ?: "-")
                    item.requestModelName?.let { value -> CupertinoKeyValueRow("请求模型", value) }
                    item.responseModelName?.let { value -> CupertinoKeyValueRow("响应模型", value) }
                    item.layoutSummary?.let { value -> CupertinoStatusStrip(value) }
                }
            }
        }
        if (preview.resolvedProperties.isNotEmpty()) {
            CupertinoPanel(title = "物模型字段解析", subtitle = "属性名、类型由后端推导。") {
                preview.resolvedProperties.forEach { item ->
                    CupertinoKeyValueRow(item.name, "${item.resolvedPropertyName ?: "-"} : ${item.resolvedTypeName ?: "-"}")
                    item.layoutSummary?.let { value -> CupertinoStatusStrip(value) }
                }
            }
        }
        if (preview.exportPlans.isNotEmpty()) {
            CupertinoPanel(title = "导出计划", subtitle = "按当前多选目标和 transport 生成的计划。") {
                preview.exportPlans.forEach { item ->
                    if (item.ready) {
                        CupertinoStatusStrip(
                            text = "${item.artifactKind}${item.transport?.let { transport -> " / $transport" } ?: ""} · ${item.summary}",
                        )
                    } else {
                        CupertinoStatusStrip(
                            text = "${item.artifactKind}${item.transport?.let { transport -> " / $transport" } ?: ""} · ${item.summary}",
                            tone = androidx.compose.ui.graphics.Color(0xFFFFF5D6),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportResultPanel(
    exportResult: CodegenMetadataExportResultDto?,
) {
    CupertinoPanel(
        title = "导出结果",
        subtitle = "结构化返回 metadata snapshot、外部文件和工程同步结果。",
    ) {
        if (exportResult == null) {
            CupertinoStatusStrip("还没有导出结果。")
            return@CupertinoPanel
        }
        CupertinoStatusStrip(exportResult.message)
        if (exportResult.metadataSnapshots.isNotEmpty()) {
            CupertinoPanel(title = "Metadata Snapshot", subtitle = "供 Kotlin/KSP database provider 消费。") {
                exportResult.metadataSnapshots.forEach { item ->
                    CupertinoStatusStrip("${item.transport} -> ${item.tableName} · ${item.queryHint}")
                }
            }
        }
        if (exportResult.generatedArtifacts.isNotEmpty()) {
            CupertinoPanel(title = "外部产物", subtitle = "已写入固件工程或文档目录的文件。") {
                exportResult.generatedArtifacts.forEach { item ->
                    CupertinoStatusStrip("${item.artifactKind}${item.transport?.let { transport -> " / $transport" } ?: ""} · ${item.path}")
                }
            }
        }
        if (exportResult.projectSyncResults.isNotEmpty()) {
            CupertinoPanel(title = "工程同步", subtitle = "Keil / CubeMX 同步状态。") {
                exportResult.projectSyncResults.forEach { item ->
                    CupertinoStatusStrip("${item.toolId} / ${item.transport} · ${item.message}")
                }
            }
        }
    }
}
