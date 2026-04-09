package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
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
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.common.orDash
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.projects.findModule
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol
import kotlin.math.max

@Composable
/**
 * 处理创建项目dialog。
 *
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun CreateProjectDialog(
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(ProjectDraft()) }
    CupertinoDialog(
        title = "新建工程",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = { onSave(draft) },
                enabled = !saving && draft.canSave(),
            )
        },
    ) {
        ProjectEditorForm(draft = draft, onDraftChange = { draft = it })
    }
}

@Composable
/**
 * 处理关联协议dialog。
 *
 * @param options 选项。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun LinkProtocolDialog(
    options: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long, Int) -> Unit,
) {
    var selectedId by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }
    CupertinoDialog(
        title = "关联协议字典",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "关联中" else "关联",
                onClick = { selectedId?.let { onSave(it, sortIndex.toIntOrNull() ?: 0) } },
                enabled = !saving && selectedId != null,
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

@Composable
/**
 * 处理choose模块协议dialog。
 *
 * @param seed seed。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun ChooseModuleProtocolDialog(
    seed: ChooseModuleProtocolSeed,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var selectedProtocolId by remember(seed) { mutableStateOf(seed.candidates.firstOrNull()?.protocolId) }
    CupertinoDialog(
        title = "选择承载协议",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "处理中" else "下一步",
                onClick = { selectedProtocolId?.let(onSave) },
                enabled = !saving && selectedProtocolId != null,
            )
        },
    ) {
        CupertinoStatusStrip("工程 ${seed.projectName.ifBlank { "当前工程" }} 下有多个可承载协议，请先明确模块归属。")
        CupertinoFormSection(
            title = "承载协议",
            subtitle = "先选协议，再决定允许的模块模板集合。",
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
            CupertinoPanel(title = "协议预览", subtitle = "模块模板只会展示这个协议模板下允许的模块。") {
                CupertinoKeyValueRow("协议模板", candidate.protocolTemplateName)
                CupertinoKeyValueRow("模块模板数", candidate.availableTemplateCount.toString())
                candidate.transportSummary?.let { CupertinoKeyValueRow("通信摘要", it) }
            }
        }
    }
}

@Composable
/**
 * 处理创建模块dialog。
 *
 * @param protocolName 协议名称。
 * @param protocolTemplateName 协议模板名称。
 * @param templates 模板。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun CreateModuleDialog(
    protocolName: String,
    protocolTemplateName: String,
    templates: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ModuleDraft) -> Unit,
) {
    var draft by remember(templates) { mutableStateOf(ModuleDraft(moduleTemplateId = templates.firstOrNull()?.value)) }
    CupertinoDialog(
        title = "新建模块",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = { onSave(draft) },
                enabled = !saving && draft.canSave(),
            )
        },
    ) {
        ModuleEditorForm(
            protocolName = protocolName,
            protocolTemplateName = protocolTemplateName,
            templates = templates,
            draft = draft,
            onDraftChange = { draft = it },
        )
    }
}

@Composable
/**
 * 处理创建设备dialog。
 *
 * @param deviceTypes 设备类型。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun CreateDeviceDialog(
    deviceTypes: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (DeviceDraft) -> Unit,
) {
    var draft by remember(deviceTypes) { mutableStateOf(DeviceDraft(deviceTypeId = deviceTypes.firstOrNull()?.value)) }
    CupertinoDialog(
        title = "新建设备",
        onDismissRequest = onDismissRequest,
        width = 860.dp,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = { onSave(draft) },
                enabled = !saving && draft.canSave(),
            )
        },
    ) {
        DeviceEditorForm(deviceTypes = deviceTypes, draft = draft, onDraftChange = { draft = it })
    }
}

@Composable
/**
 * 处理创建标签dialog。
 *
 * @param dataTypes 数据类型。
 * @param registerTypes 寄存器类型。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun CreateTagDialog(
    dataTypes: List<CupertinoOption<Long>>,
    registerTypes: List<CupertinoOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (TagDraft) -> Unit,
) {
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
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = { onSave(draft) },
                enabled = !saving && draft.canSave(),
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

@Composable
/**
 * 处理移动nodedialog。
 *
 * @param nodeKind node类型。
 * @param options 选项。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
internal fun MoveNodeDialog(
    nodeKind: HostConfigNodeKind,
    options: List<CupertinoOption<String>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var targetKey by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }
    CupertinoDialog(
        title = "变更${nodeKind.label()}上级",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = { targetKey?.let { onSave(it, sortIndex.toIntOrNull() ?: 0) } },
                enabled = !saving && targetKey != null,
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

@Composable
/**
 * 处理nodeactiondropdownmenu。
 *
 * @param seed seed。
 * @param onDismissRequest ondismiss请求。
 * @param onAction onaction。
 */
internal fun NodeActionDropdownMenu(
    seed: NodeActionMenuSeed,
    onDismissRequest: () -> Unit,
    onAction: (NodeActionType) -> Unit,
) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CupertinoTheme.shapes.medium)
                            .clickable(enabled = item.enabled) { onAction(item.type) }
                            .background(
                                if (item.enabled) CupertinoTheme.colorScheme.tertiarySystemGroupedBackground
                                else CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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

@Composable
/**
 * 处理render传输配置rows。
 *
 * @param transportConfig 传输配置。
 */
internal fun renderTransportConfigRows(
    transportConfig: ProtocolTransportConfig?,
) {
    if (transportConfig == null) {
        CupertinoStatusStrip("当前没有通信参数。")
        return
    }
    transportConfig.toDisplayRows().forEach { (label, value) ->
        CupertinoKeyValueRow(label, value)
    }
}

/**
 * 处理协议传输配置。
 */
internal fun ProtocolTransportConfig.toDisplayRows(): List<Pair<String, String>> {
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
 * 解析模块模板for协议。
 *
 * @param state 状态。
 * @param protocolTemplateId 协议模板 ID。
 */
internal fun resolveModuleTemplatesForProtocol(
    state: ProjectsScreenState,
    protocolTemplateId: Long,
): List<ModuleTemplateOptionResponse> {
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
 * 解析模块协议candidates。
 *
 * @param state 状态。
 * @param projectId 项目 ID。
 */
internal fun resolveModuleProtocolCandidates(
    state: ProjectsScreenState,
    projectId: Long,
): List<ModuleProtocolCandidate> {
    val projectTree = state.projectTrees.firstOrNull { it.id == projectId } ?: return emptyList()
    return projectTree.protocols.mapNotNull { protocol ->
        val availableTemplates = resolveModuleTemplatesForProtocol(state, protocol.protocolTemplateId)
        if (availableTemplates.isEmpty()) {
            return@mapNotNull null
        }
        ModuleProtocolCandidate(
            protocolId = protocol.id,
            protocolName = protocol.displayName(),
            protocolTemplateId = protocol.protocolTemplateId,
            protocolTemplateName = protocol.protocolTemplateName,
            availableTemplateCount = availableTemplates.size,
            transportSummary = protocol.transportConfig?.toSummary(),
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
            val currentProtocol = state.projectTrees.findModule(node.entityId)?.let { module ->
                state.projectTrees.findProtocol(module.protocolId)
            }
            state.projectTrees.flatMap { project ->
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
        }

        HostConfigNodeKind.DEVICE -> state.projectTrees.flatMap { project ->
            project.allModules().map { module ->
                CupertinoOption(
                    value = "module:${project.id}:${module.id}",
                    label = "${project.name} / ${module.name}",
                )
            }
        }

        HostConfigNodeKind.TAG -> state.projectTrees.flatMap { project ->
            buildList {
                project.allModules().forEach { module ->
                    module.devices.forEach { device ->
                        add(
                            CupertinoOption(
                                value = "device:${project.id}:${device.id}",
                                label = "${project.name} / ${module.name} / ${device.name}",
                            ),
                        )
                    }
                }
            }
        }
    }
}
