package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.koinInject
import site.addzero.cupertino.workbench.components.dialog.CupertinoDialog
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.form.CupertinoFormSection
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTemplateMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldKey
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.common.orDash
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.projects.findDevice
import site.addzero.kcloud.plugins.hostconfig.projects.findModule
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsDialogFooterActionState
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsDialogFooterActions
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsDialogFooterActionsSpi
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsInteractiveSurfaceActions
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsInteractiveSurfaceSpi
import kotlin.math.max

/**
 * 处理创建项目dialog。
 *
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun CreateProjectDialog(
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectDraft) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var draft by remember { mutableStateOf(ProjectDraft()) }
    CupertinoDialog(
        title = "新建工程",
        onDismissRequest = onDismissRequest,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "保存中" else "保存",
                    confirmEnabled = !saving && draft.canSave(),
                ),
                actions = remember(draft, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = { onSave(draft) },
                    )
                },
            )
        },
    ) {
        ProjectEditorForm(draft = draft, onDraftChange = { draft = it })
    }
}

/**
 * 处理关联协议dialog。
 *
 * @param options 选项。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun LinkProtocolDialog(
    options: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long, Int) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var selectedId by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }
    CupertinoDialog(
        title = "关联协议字典",
        onDismissRequest = onDismissRequest,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "关联中" else "关联",
                    confirmEnabled = !saving && selectedId != null,
                ),
                actions = remember(selectedId, sortIndex, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = {
                            selectedId?.let { onSave(it, sortIndex.toIntOrNull() ?: 0) }
                        },
                    )
                },
            )
        },
    ) {
        if (options.isEmpty()) {
            CupertinoStatusStrip("当前工程已关联全部协议字典。")
        } else {
            CupertinoFormSection(
                title = "关联设置",
                subtitle = "从系统协议字典选择一个模板，并在当前工程里生成对应的协议关联。",
            ) {
                item {
                    CupertinoSelectionField(
                        label = "协议字典",
                        options = options,
                        selectedValue = selectedId,
                        onSelected = { selectedId = it },
                    )
                }
                item {
                    CupertinoTextField("排序", sortIndex, { sortIndex = it })
                }
            }
        }
    }
}

/**
 * 处理choose协议dialog。
 *
 * @param seed seed。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun ChooseProtocolDialog(
    seed: ChooseProtocolSeed,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var selectedProtocolId by remember(seed) { mutableStateOf(seed.candidates.firstOrNull()?.protocolId) }
    CupertinoDialog(
        title = seed.title,
        onDismissRequest = onDismissRequest,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "处理中" else "下一步",
                    confirmEnabled = !saving && selectedProtocolId != null,
                ),
                actions = remember(selectedProtocolId, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = { selectedProtocolId?.let(onSave) },
                    )
                },
            )
        },
    ) {
        CupertinoStatusStrip("工程 ${seed.projectName.ifBlank { "当前工程" }} 下有多个候选协议，请先明确归属。")
        CupertinoFormSection(
            title = "承载协议",
            subtitle = seed.subtitle,
        ) {
            item {
                CupertinoSelectionField(
                    label = "目标协议",
                    options = seed.candidates.map { candidate ->
                        CupertinoOption(
                            value = candidate.protocolId,
                            label = candidate.protocolName,
                            caption = candidate.transportSummary ?: candidate.protocolTemplateName,
                        )
                    },
                    selectedValue = selectedProtocolId,
                    onSelected = { selectedProtocolId = it },
                )
            }
        }
        seed.candidates.firstOrNull { it.protocolId == selectedProtocolId }?.let { candidate ->
            CupertinoPanel(title = "协议预览", subtitle = "确认协议模板与通信摘要。") {
                CupertinoKeyValueRow("协议模板", candidate.protocolTemplateName)
                candidate.transportSummary?.let { CupertinoKeyValueRow("通信摘要", it) }
            }
        }
    }
}

/**
 * 处理创建模块dialog。
 *
 * @param deviceName 设备名称。
 * @param protocolName 协议名称。
 * @param protocolTemplateName 协议模板名称。
 * @param templates 模板。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun CreateModuleDialog(
    deviceName: String,
    protocolName: String,
    protocolTemplateName: String,
    templates: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ModuleDraft) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var draft by remember(templates) { mutableStateOf(ModuleDraft(moduleTemplateId = templates.firstOrNull()?.value)) }
    CupertinoDialog(
        title = "新建模块",
        onDismissRequest = onDismissRequest,
        width = 900.dp,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "保存中" else "保存",
                    confirmEnabled = !saving && draft.canSave(),
                ),
                actions = remember(draft, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = { onSave(draft) },
                    )
                },
            )
        },
    ) {
        CupertinoStatusStrip("模块将挂载到设备 ${deviceName.ifBlank { "当前设备" }} 下，通信协议沿用 ${protocolName.ifBlank { protocolTemplateName }}。")
        ModuleEditorForm(
            protocolName = protocolName,
            protocolTemplateName = protocolTemplateName,
            templates = templates,
            draft = draft,
            onDraftChange = { draft = it },
        )
    }
}

/**
 * 处理创建设备dialog。
 *
 * @param protocolName 协议名称。
 * @param protocolTemplateName 协议模板名称。
 * @param deviceTypes 设备类型。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun CreateDeviceDialog(
    protocolName: String,
    protocolTemplateName: String,
    deviceTypes: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (DeviceDraft) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var draft by remember(deviceTypes) { mutableStateOf(DeviceDraft(deviceTypeId = deviceTypes.firstOrNull()?.value)) }
    CupertinoDialog(
        title = "新建设备",
        onDismissRequest = onDismissRequest,
        width = 980.dp,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "保存中" else "保存",
                    confirmEnabled = !saving && draft.canSave(),
                ),
                actions = remember(draft, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = { onSave(draft) },
                    )
                },
            )
        },
    ) {
        CupertinoStatusStrip("设备将直接挂到协议 ${protocolName.ifBlank { protocolTemplateName }} 下，模块后续再在设备内创建。")
        DeviceEditorForm(deviceTypes = deviceTypes, draft = draft, onDraftChange = { draft = it })
    }
}

/**
 * 处理创建标签dialog。
 *
 * @param dataTypes 数据类型。
 * @param registerTypes 寄存器类型。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun CreateTagDialog(
    dataTypes: List<CupertinoOption<Long>>,
    registerTypes: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (TagDraft) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var draft by remember(dataTypes, registerTypes) {
        mutableStateOf(
            TagDraft(
                dataTypeId = dataTypes.firstOrNull()?.value,
                registerTypeId = registerTypes.firstOrNull()?.value,
            ),
        )
    }
    CupertinoDialog(
        title = "新建标签",
        onDismissRequest = onDismissRequest,
        width = 860.dp,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "保存中" else "保存",
                    confirmEnabled = !saving && draft.canSave(),
                ),
                actions = remember(draft, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = { onSave(draft) },
                    )
                },
            )
        },
    ) {
        TagEditorForm(
            dataTypes = dataTypes,
            registerTypes = registerTypes,
            draft = draft,
            onDraftChange = { draft = it },
        )
    }
}

/**
 * 处理移动nodedialog。
 *
 * @param nodeKind node类型。
 * @param options 选项。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
@Composable
internal fun MoveNodeDialog(
    nodeKind: HostConfigNodeKind,
    options: List<CupertinoOption<String>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    val dialogFooterActionsSpi = koinInject<ProjectsDialogFooterActionsSpi>()
    var targetKey by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }
    CupertinoDialog(
        title = "变更${nodeKind.label()}上级",
        onDismissRequest = onDismissRequest,
        actions = {
            dialogFooterActionsSpi.Render(
                state = ProjectsDialogFooterActionState(
                    confirmText = if (saving) "保存中" else "保存",
                    confirmEnabled = !saving && targetKey != null,
                ),
                actions = remember(targetKey, sortIndex, onDismissRequest, onSave) {
                    ProjectsDialogFooterActions(
                        onDismiss = onDismissRequest,
                        onConfirm = {
                            targetKey?.let { onSave(it, sortIndex.toIntOrNull() ?: 0) }
                        },
                    )
                },
            )
        },
    ) {
        CupertinoFormSection(
            title = "迁移设置",
            subtitle = "先选目标上级，再确定在新父节点下的排序。",
        ) {
            item {
                CupertinoSelectionField(
                    label = "目标上级",
                    options = options,
                    selectedValue = targetKey,
                    onSelected = { targetKey = it },
                )
            }
            item {
                CupertinoTextField("排序", sortIndex, { sortIndex = it })
            }
        }
    }
}

/**
 * 处理nodeactiondropdownmenu。
 *
 * @param seed seed。
 * @param onDismissRequest ondismiss请求。
 * @param onAction onaction。
 */
@Composable
internal fun NodeActionDropdownMenu(
    seed: NodeActionMenuSeed,
    onDismissRequest: () -> Unit,
    onAction: (NodeActionType) -> Unit,
) {
    val interactiveSurfaceSpi = koinInject<ProjectsInteractiveSurfaceSpi>()
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        NodeActionDropdownPositionProvider(
            horizontalGapPx = with(density) { 8.dp.roundToPx() },
            verticalOffsetPx = with(density) { 4.dp.roundToPx() },
        )
    }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, clippingEnabled = false),
    ) {
        CupertinoSurface(
            modifier = Modifier
                .width(220.dp)
                .border(
                    width = 1.dp,
                    color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.22f),
                    shape = CupertinoTheme.shapes.large,
                ),
            color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
            shape = CupertinoTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    CupertinoText(text = seed.title, style = CupertinoTheme.typography.subhead)
                    seed.subtitle.takeIf { it.isNotBlank() }?.let { subtitle ->
                        CupertinoText(
                            text = subtitle,
                            style = CupertinoTheme.typography.footnote,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CupertinoTheme.colorScheme.separator.copy(alpha = 0.14f)),
                )
                seed.items.forEach { item ->
                    val titleColor = when {
                        !item.enabled -> CupertinoTheme.colorScheme.secondaryLabel.copy(alpha = 0.62f)
                        item.destructive -> Color(0xFFC83C35)
                        else -> CupertinoTheme.colorScheme.label
                    }
                    interactiveSurfaceSpi.Render(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CupertinoTheme.shapes.medium)
                            .background(
                                if (item.enabled) {
                                    CupertinoTheme.colorScheme.tertiarySystemGroupedBackground
                                } else {
                                    CupertinoTheme.colorScheme.secondarySystemGroupedBackground
                                },
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        enabled = item.enabled,
                        actions = ProjectsInteractiveSurfaceActions(
                            onClick = { onAction(item.type) },
                        ),
                    ) { interactiveModifier ->
                        Column(
                            modifier = interactiveModifier,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            CupertinoText(text = item.title, color = titleColor)
                            item.note?.takeIf { it.isNotBlank() }?.let { note ->
                                CupertinoText(
                                    text = note,
                                    style = CupertinoTheme.typography.footnote,
                                    color = CupertinoTheme.colorScheme.secondaryLabel.copy(
                                        alpha = if (item.enabled) 1f else 0.7f,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 表示nodeactiondropdown位置provider。
 *
 * @property horizontalGapPx horizontalgappx。
 * @property verticalOffsetPx verticaloffsetpx。
 */
private class NodeActionDropdownPositionProvider(
    private val horizontalGapPx: Int,
    private val verticalOffsetPx: Int,
) : PopupPositionProvider {
    /**
     * 处理calculate位置。
     *
     * @param anchorBounds anchorbounds。
     * @param windowSize windowsize。
     * @param layoutDirection layoutdirection。
     * @param popupContentSize popupcontentsize。
     */
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredRightX = anchorBounds.right + horizontalGapPx
        val fallbackLeftX = anchorBounds.left - popupContentSize.width - horizontalGapPx
        val maxX = max(windowSize.width - popupContentSize.width, 0)
        val resolvedX = when {
            preferredRightX + popupContentSize.width <= windowSize.width -> preferredRightX
            fallbackLeftX >= 0 -> fallbackLeftX
            else -> preferredRightX.coerceIn(0, maxX)
        }
        val maxY = max(windowSize.height - popupContentSize.height, 0)
        val resolvedY = (anchorBounds.top + verticalOffsetPx).coerceIn(0, maxY)
        return IntOffset(x = resolvedX, y = resolvedY)
    }
}

/**
 * 处理render传输配置rows。
 *
 * @param transportConfig 传输配置。
 */
@Composable
internal fun renderTransportConfigRows(
    transportConfig: ProtocolTransportConfig?,
    metadata: ProtocolTemplateMetadataResponse? = null,
) {
    if (transportConfig == null) {
        CupertinoStatusStrip("当前没有通信参数。")
        return
    }
    transportConfig.toDisplayRows(metadata).forEach { (label, value) ->
        CupertinoKeyValueRow(label, value)
    }
}

/**
 * 处理协议传输配置。
 */
internal fun ProtocolTransportConfig.toDisplayRows(): List<Pair<String, String>> {
    return toDisplayRows(metadata = null)
}

/**
 * 处理协议传输配置。
 *
 * @param metadata 协议模板元数据。
 */
internal fun ProtocolTransportConfig.toDisplayRows(
    metadata: ProtocolTemplateMetadataResponse?,
): List<Pair<String, String>> {
    val dynamicFields = metadata?.transportForm?.fields.orEmpty()
    if (dynamicFields.isNotEmpty()) {
        return buildList {
            add("传输类型" to transportType.label())
            dynamicFields.forEach { field ->
                add(field.label to displayFieldValue(field.key))
            }
        }
    }
    return when (transportType) {
        TransportType.RTU -> listOf(
            "传输类型" to transportType.label(),
            "串口" to portName.orDash(),
            "波特率" to (baudRate?.toString() ?: "-"),
            "数据位" to (dataBits?.toString() ?: "-"),
            "停止位" to (stopBits?.toString() ?: "-"),
            "校验位" to (parity?.label() ?: "-"),
            "响应超时(ms)" to (responseTimeoutMs?.toString() ?: "-"),
        )

        TransportType.TCP -> listOf(
            "传输类型" to transportType.label(),
            "主机地址" to host.orDash(),
            "TCP 端口" to (tcpPort?.toString() ?: "-"),
            "响应超时(ms)" to (responseTimeoutMs?.toString() ?: "-"),
        )
    }
}

/**
 * 处理协议传输配置。
 */
internal fun ProtocolTransportConfig.toSummary(): String {
    return toSummary(metadata = null)
}

/**
 * 处理协议传输配置。
 *
 * @param metadata 协议模板元数据。
 */
internal fun ProtocolTransportConfig.toSummary(
    metadata: ProtocolTemplateMetadataResponse?,
): String {
    val summaryKeys = metadata?.transportForm?.summaryKeys.orEmpty()
    if (summaryKeys.isNotEmpty()) {
        return summaryKeys
            .map { key -> displayFieldValue(key) }
            .joinToString(" / ")
    }
    return when (transportType) {
        TransportType.RTU -> {
            listOf(
                portName.orDash(),
                baudRate?.let { "${it}bps" } ?: "-",
                parity?.label() ?: "-",
            ).joinToString(" / ")
        }

        TransportType.TCP -> {
            listOf(host.orDash(), tcpPort?.toString() ?: "-").joinToString(":")
        }
    }
}

/**
 * 读取传输配置显示值。
 *
 * @param key 字段键。
 */
private fun ProtocolTransportConfig.displayFieldValue(
    key: ProtocolTransportFieldKey,
): String {
    return when (key) {
        ProtocolTransportFieldKey.HOST -> host.orDash()
        ProtocolTransportFieldKey.TCP_PORT -> tcpPort?.toString() ?: "-"
        ProtocolTransportFieldKey.PORT_NAME -> portName.orDash()
        ProtocolTransportFieldKey.BAUD_RATE -> baudRate?.toString() ?: "-"
        ProtocolTransportFieldKey.DATA_BITS -> dataBits?.toString() ?: "-"
        ProtocolTransportFieldKey.STOP_BITS -> stopBits?.toString() ?: "-"
        ProtocolTransportFieldKey.PARITY -> parity?.label() ?: "-"
        ProtocolTransportFieldKey.RESPONSE_TIMEOUT_MS -> responseTimeoutMs?.toString() ?: "-"
    }
}

/**
 * 解析模块模板for协议。
 *
 * @param state 状态。
 * @param protocolId 协议 ID。
 */
internal fun resolveModuleTemplatesForProtocol(
    state: ProjectsScreenState,
    protocolId: Long,
): List<ModuleTemplateOptionResponse> {
    val protocolTemplateId = state.projectTrees.findProtocol(protocolId)?.protocolTemplateId ?: return emptyList()
    return state.moduleTemplateCatalog[protocolTemplateId].orEmpty()
}

/**
 * 解析linkable协议模板。
 *
 * @param state 状态。
 * @param projectId 项目 ID。
 */
internal fun resolveLinkableProtocolTemplates(
    state: ProjectsScreenState,
    projectId: Long,
): List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse> {
    val linkedTemplateIds = state.projectTrees
        .firstOrNull { it.id == projectId }
        ?.protocols
        ?.map { it.protocolTemplateId }
        ?.toSet()
        .orEmpty()
    return state.protocolTemplates.filter { template -> template.id !in linkedTemplateIds }
}

/**
 * 解析协议模板元数据。
 *
 * @param state 状态。
 * @param protocolTemplateId 协议模板 ID。
 */
internal fun resolveProtocolTemplateMetadata(
    state: ProjectsScreenState,
    protocolTemplateId: Long,
): ProtocolTemplateMetadataResponse? {
    return state.protocolTemplates
        .firstOrNull { template -> template.id == protocolTemplateId }
        ?.metadata
}

/**
 * 解析项目协议candidates。
 *
 * @param state 状态。
 * @param projectId 项目 ID。
 */
internal fun resolveProjectProtocolCandidates(
    state: ProjectsScreenState,
    projectId: Long,
): List<ProtocolCandidate> {
    val projectTree = state.projectTrees.firstOrNull { it.id == projectId } ?: return emptyList()
    return projectTree.protocols.map { protocol ->
        ProtocolCandidate(
            protocolId = protocol.id,
            protocolName = protocol.displayName(),
            protocolTemplateId = protocol.protocolTemplateId,
            protocolTemplateName = protocol.protocolTemplateName,
            transportSummary = protocol.transportConfig?.toSummary(
                resolveProtocolTemplateMetadata(state, protocol.protocolTemplateId),
            ),
        )
    }
}

/**
 * 解析移动选项。
 *
 * @param state 状态。
 * @param node node。
 */
internal fun resolveMoveOptions(
    state: ProjectsScreenState,
    node: HostConfigTreeNode,
): List<CupertinoOption<String>> {
    return when (node.kind) {
        HostConfigNodeKind.PROJECT -> emptyList()
        HostConfigNodeKind.PROTOCOL -> {
            val currentProtocol = state.projectTrees.findProtocol(node.entityId)
            state.projects
                .filter { project ->
                    currentProtocol == null ||
                        project.id == node.projectId ||
                        state.projectTrees
                            .firstOrNull { it.id == project.id }
                            ?.protocols
                            ?.none { protocol ->
                                protocol.id != currentProtocol.id &&
                                    protocol.protocolTemplateId == currentProtocol.protocolTemplateId
                            }
                            ?: true
                }
                .map { project -> CupertinoOption(value = "project:${project.id}", label = project.name) }
        }

        HostConfigNodeKind.MODULE -> {
            val currentModule = state.projectTrees.findModule(node.entityId)
            val currentProtocol = currentModule?.let { module -> state.projectTrees.findProtocol(module.protocolId) }
            state.projectTrees.flatMap { project ->
                project.protocols.flatMap { protocol ->
                    protocol.devices
                        .filter { device ->
                            currentProtocol == null || protocol.protocolTemplateId == currentProtocol.protocolTemplateId
                        }
                        .map { device ->
                            CupertinoOption(
                                value = "device:${project.id}:${device.id}",
                                label = "${project.name} / ${protocol.displayName()} / ${device.name}",
                                caption = device.deviceTypeName,
                            )
                        }
                }
            }
        }

        HostConfigNodeKind.DEVICE -> state.projectTrees.flatMap { project ->
            val currentProtocol = state.projectTrees.findDevice(node.entityId)?.let { device ->
                state.projectTrees.findProtocol(device.protocolId)
            }
            project.protocols
                .filter { protocol ->
                    currentProtocol == null || protocol.protocolTemplateId == currentProtocol.protocolTemplateId
                }
                .map { protocol ->
                    CupertinoOption(
                        value = "protocol:${project.id}:${protocol.id}",
                        label = "${project.name} / ${protocol.displayName()}",
                        caption = protocol.protocolTemplateName,
                    )
                }
        }

        HostConfigNodeKind.TAG -> state.projectTrees.flatMap { project ->
            buildList {
                project.protocols.forEach { protocol ->
                    protocol.devices.forEach { device ->
                        add(
                            CupertinoOption(
                                value = "device:${project.id}:${device.id}",
                                label = "${project.name} / ${protocol.displayName()} / ${device.name}",
                            ),
                        )
                    }
                }
            }
        }
    }
}
