package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.editor.CupertinoEditorSaveBar
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.orDash
import site.addzero.kcloud.plugins.hostconfig.common.resolveModuleBoardModel
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.common.label

@Composable
/**
 * 处理当前nodepanel。
 *
 * @param state 状态。
 * @param onSelectNode on选择node。
 * @param collapsed collapsed。
 * @param saving saving。
 * @param editingNodeId editingnode ID。
 * @param onStartEditing on开始editing。
 * @param onStopEditing on停止editing。
 * @param onSaveProject on保存项目。
 * @param onSaveProtocol on保存协议。
 * @param onSaveModule on保存模块。
 * @param onSaveDevice on保存设备。
 * @param onSaveTag on保存标签。
 * @param onToggleCollapsed ontogglecollapsed。
 */
internal fun CurrentNodePanel(
    state: ProjectsScreenState,
    onSelectNode: (String) -> Unit,
    collapsed: Boolean,
    saving: Boolean,
    editingNodeId: String?,
    onStartEditing: (String) -> Unit,
    onStopEditing: () -> Unit,
    onSaveProject: (site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse, ProjectDraft) -> Unit,
    onSaveProtocol: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode, ProtocolDraft) -> Unit,
    onSaveModule: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode, ModuleDraft) -> Unit,
    onSaveDevice: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode, DeviceDraft) -> Unit,
    onSaveTag: (Long, Long, site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse, TagDraft) -> Unit,
    onToggleCollapsed: () -> Unit,
) {
    val selectedNode = state.selectedNode
    val nodeKind = resolveSelectedNodeKind(state)
    val isEditing = !collapsed && selectedNode?.id != null && selectedNode.id == editingNodeId

    CupertinoPanel(
        title = "当前节点",
        actions = {
            if (selectedNode != null) {
                WorkbenchActionButton(
                    text = if (isEditing) "取消编辑" else "编辑",
                    onClick = {
                        if (isEditing) onStopEditing() else onStartEditing(selectedNode.id)
                    },
                    variant = WorkbenchButtonVariant.Secondary,
                )
            }
            WorkbenchActionButton(
                text = if (collapsed) "展开" else "折叠",
                onClick = onToggleCollapsed,
                variant = WorkbenchButtonVariant.Outline,
            )
        },
    ) {
        if (collapsed) {
            return@CupertinoPanel
        }
        if (nodeKind == null) {
            CupertinoStatusStrip("当前还没有可展示的工程节点，先点击左侧的新建。")
            return@CupertinoPanel
        }

        when (nodeKind) {
            HostConfigNodeKind.PROJECT -> {
                val project = state.selectedProject
                val projectTree = state.selectedProjectTree
                val modules = projectTree?.allModules().orEmpty()
                val protocolCount = projectTree?.protocols?.size ?: 0
                val deviceCount = modules.sumOf { module -> module.devices.size }
                val tagCount = modules.sumOf { module -> module.devices.sumOf { device -> device.tags.size } }

                HostConfigNodeSummary(
                    title = project?.name.orDash(),
                    subtitle = project?.description?.takeIf { it.isNotBlank() } ?: "工程级配置与模块资源概览",
                    kind = nodeKind,
                    badges = listOfNotNull(
                        project?.remark?.takeIf { it.isNotBlank() }?.let { "已写备注" },
                    ),
                    metrics = listOf(
                        NodeMetricItem("协议", protocolCount.toString()),
                        NodeMetricItem("模块", modules.size.toString()),
                        NodeMetricItem("设备", deviceCount.toString()),
                        NodeMetricItem("标签", tagCount.toString()),
                    ),
                )

                if (isEditing) {
                    if (project == null) {
                        CupertinoStatusStrip("当前工程详情尚未加载完成。")
                    } else {
                        var draft by remember(project) { mutableStateOf(project.toProjectDraft()) }
                        CurrentNodeInlineEditorBar(
                            saving = saving,
                            enabled = draft.canSave(),
                            onSave = { onSaveProject(project, draft) },
                        )
                        ProjectEditorForm(draft = draft, onDraftChange = { draft = it })
                    }
                } else {
                    HostConfigDenseInfoSection(
                        title = "工程资料",
                        entries = listOf(
                            "工程名称" to project?.name.orDash(),
                            "描述" to project?.description.orDash(),
                            "备注" to project?.remark.orDash(),
                            "排序" to (project?.sortIndex?.toString() ?: "-"),
                        ),
                    )
                    if (modules.isNotEmpty()) {
                        HostConfigDenseSection(
                            title = "模块总览",
                            subtitle = "按模块快速查看板卡形态与设备承载情况。",
                        ) {
                            ProjectModuleRack(
                                projectId = project?.id,
                                modules = modules,
                                moduleTemplates = state.moduleTemplates,
                                onSelectNode = onSelectNode,
                            )
                        }
                    }
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                val protocol = state.selectedProtocol
                val transportConfig = protocol?.transportConfig
                HostConfigNodeSummary(
                    title = protocol?.displayName().orDash(),
                    subtitle = protocol?.protocolTemplateCode.orDash(),
                    kind = nodeKind,
                    badges = listOfNotNull(transportConfig?.transportType?.label()),
                    metrics = listOf(
                        NodeMetricItem("模块", protocol?.modules?.size?.toString() ?: "0"),
                        NodeMetricItem("轮询", protocol?.pollingIntervalMs?.let { "${it}ms" } ?: "-"),
                        NodeMetricItem("链路", transportConfig?.toSummary() ?: "未配置"),
                    ),
                )
                if (isEditing) {
                    val projectId = selectedNode?.projectId
                    if (protocol == null || projectId == null) {
                        CupertinoStatusStrip("当前协议详情尚未加载完成。")
                    } else {
                        var draft by remember(protocol) { mutableStateOf(protocol.toProtocolDraft()) }
                        CurrentNodeInlineEditorBar(
                            saving = saving,
                            enabled = draft.canSaveProtocol(protocol),
                            onSave = { onSaveProtocol(projectId, protocol, draft) },
                        )
                        ProtocolEditorForm(
                            protocolTemplates = state.protocolTemplates,
                            existing = protocol,
                            draft = draft,
                            onDraftChange = { draft = it },
                        )
                    }
                } else {
                    HostConfigDenseInfoSection(
                        title = "协议资料",
                        entries = listOf(
                            "协议字典" to protocol?.protocolTemplateName.orDash(),
                            "模板编码" to protocol?.protocolTemplateCode.orDash(),
                            "轮询间隔(ms)" to (protocol?.pollingIntervalMs?.toString() ?: "-"),
                            "排序" to (protocol?.sortIndex?.toString() ?: "-"),
                            "模块数量" to (protocol?.modules?.size?.toString() ?: "0"),
                        ),
                    )
                    HostConfigDenseInfoSection(
                        title = "通信参数",
                        entries = transportConfig?.toDisplayRows().orEmpty(),
                        emptyText = "当前没有通信参数。",
                    )
                }
            }

            HostConfigNodeKind.MODULE -> {
                val module = state.selectedModule
                val moduleProtocol = state.selectedModuleProtocol
                val boardModel = module?.let { item ->
                    resolveModuleBoardModel(
                        module = item,
                        moduleTemplates = state.moduleTemplates,
                        runtime = state.moduleBoardRuntime,
                    )
                }
                HostConfigNodeSummary(
                    title = module?.name.orDash(),
                    subtitle = module?.moduleTemplateName.orDash(),
                    kind = nodeKind,
                    badges = listOfNotNull(
                        module?.moduleTemplateCode?.takeIf { it.isNotBlank() },
                        moduleProtocol?.displayName()?.takeIf { it.isNotBlank() }?.let { "协议 $it" },
                    ),
                    metrics = listOf(
                        NodeMetricItem("通道", boardModel?.channelCount?.toString() ?: "-"),
                        NodeMetricItem("设备", module?.devices?.size?.toString() ?: "0"),
                        NodeMetricItem("排序", module?.sortIndex?.toString() ?: "-"),
                    ),
                )
                if (isEditing) {
                    val projectId = selectedNode?.projectId
                    if (module == null || moduleProtocol == null || projectId == null) {
                        CupertinoStatusStrip("当前模块详情尚未加载完成。")
                    } else {
                        var draft by remember(module) { mutableStateOf(module.toModuleDraft()) }
                        CurrentNodeInlineEditorBar(
                            saving = saving,
                            enabled = draft.canSave(),
                            onSave = { onSaveModule(projectId, module, draft) },
                        )
                        ModuleEditorForm(
                            protocolName = moduleProtocol.displayName(),
                            protocolTemplateName = moduleProtocol.protocolTemplateName,
                            templates = resolveModuleTemplatesForProtocol(state, moduleProtocol.protocolTemplateId).map { item ->
                                CupertinoOption(item.id, item.name, item.description)
                            },
                            draft = draft,
                            onDraftChange = { draft = it },
                        )
                    }
                } else {
                    boardModel?.let { board ->
                        HostConfigDenseSection(
                            title = "模块视图",
                            subtitle = "只渲染当前在线板卡实际返回的寄存器字段，没有返回的数据不会展示。",
                        ) {
                            site.addzero.kcloud.plugins.hostconfig.common.HostConfigModuleBoard(
                                model = board,
                                loading = state.moduleBoardLoading,
                                errorMessage = state.moduleBoardErrorMessage,
                            )
                        }
                    }
                    HostConfigDenseInfoSection(
                        title = "模块资料",
                        entries = listOf(
                            "模块名称" to module?.name.orDash(),
                            "模块模板" to module?.moduleTemplateName.orDash(),
                            "模板编码" to module?.moduleTemplateCode.orDash(),
                            "所属协议" to moduleProtocol?.displayName().orDash(),
                            "排序" to (module?.sortIndex?.toString() ?: "-"),
                            "设备数量" to (module?.devices?.size?.toString() ?: "0"),
                        ),
                    )
                    HostConfigDenseInfoSection(
                        title = "继承通信",
                        entries = moduleProtocol?.transportConfig?.toDisplayRows().orEmpty(),
                        emptyText = "当前没有继承到通信参数。",
                    )
                }
            }

            HostConfigNodeKind.DEVICE -> {
                val device = state.selectedDevice
                HostConfigNodeSummary(
                    title = device?.name.orDash(),
                    subtitle = device?.deviceTypeName.orDash(),
                    kind = nodeKind,
                    badges = listOfNotNull(
                        device?.deviceTypeCode?.takeIf { it.isNotBlank() },
                        if (device?.disabled == true) "已禁用" else "已启用",
                    ),
                    metrics = listOf(
                        NodeMetricItem("站号", device?.stationNo?.toString() ?: "-"),
                        NodeMetricItem("标签", device?.tags?.size?.toString() ?: "0"),
                        NodeMetricItem("排序", device?.sortIndex?.toString() ?: "-"),
                    ),
                )
                if (isEditing) {
                    val projectId = selectedNode?.projectId
                    if (device == null || projectId == null) {
                        CupertinoStatusStrip("当前设备详情尚未加载完成。")
                    } else {
                        var draft by remember(device) { mutableStateOf(device.toDeviceDraft()) }
                        CurrentNodeInlineEditorBar(
                            saving = saving,
                            enabled = draft.canSave(),
                            onSave = { onSaveDevice(projectId, device, draft) },
                        )
                        DeviceEditorForm(
                            deviceTypes = state.deviceTypes.map { item ->
                                CupertinoOption(item.id, item.name, item.description)
                            },
                            draft = draft,
                            onDraftChange = { draft = it },
                        )
                    }
                } else {
                    HostConfigDenseInfoSection(
                        title = "设备资料",
                        entries = listOf(
                            "设备名称" to device?.name.orDash(),
                            "设备类型" to device?.deviceTypeName.orDash(),
                            "类型编码" to device?.deviceTypeCode.orDash(),
                            "站号" to (device?.stationNo?.toString() ?: "-"),
                            "请求间隔(ms)" to (device?.requestIntervalMs?.toString() ?: "-"),
                            "写值间隔(ms)" to (device?.writeIntervalMs?.toString() ?: "-"),
                            "禁用" to if (device?.disabled == true) "是" else "否",
                            "标签数量" to (device?.tags?.size?.toString() ?: "0"),
                        ),
                    )
                    HostConfigDenseInfoSection(
                        title = "字节与批量",
                        entries = listOf(
                            "2 字节顺序" to (device?.byteOrder2?.label() ?: "-"),
                            "4 字节顺序" to (device?.byteOrder4?.label() ?: "-"),
                            "浮点顺序" to (device?.floatOrder?.label() ?: "-"),
                            "模拟量批量" to listOf(device?.batchAnalogStart, device?.batchAnalogLength).joinToString(" / ") { it?.toString() ?: "-" },
                            "数字量批量" to listOf(device?.batchDigitalStart, device?.batchDigitalLength).joinToString(" / ") { it?.toString() ?: "-" },
                        ),
                    )
                }
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                if (tag == null) {
                    CupertinoStatusStrip("当前标签详情尚未加载完成。")
                    return@CupertinoPanel
                }
                HostConfigNodeSummary(
                    title = tag.name,
                    subtitle = "${tag.dataTypeName} · ${tag.registerTypeName}",
                    kind = nodeKind,
                    badges = listOf(
                        if (tag.enabled) "已启用" else "未启用",
                        tag.pointType?.label() ?: "未分组",
                    ),
                    metrics = listOf(
                        NodeMetricItem("地址", tag.registerAddress.toString()),
                        NodeMetricItem("值文本", tag.valueTexts.size.toString()),
                        NodeMetricItem("缩放", if (tag.scalingEnabled) "开启" else "关闭"),
                    ),
                )
                if (isEditing) {
                    val projectId = selectedNode?.projectId
                    val deviceId = selectedNode?.parentEntityId
                    if (projectId == null || deviceId == null) {
                        CupertinoStatusStrip("当前标签上下文缺失，暂时无法保存。")
                    } else {
                        var draft by remember(tag) { mutableStateOf(tag.toTagDraft()) }
                        CurrentNodeInlineEditorBar(
                            saving = saving,
                            enabled = draft.canSave(),
                            onSave = { onSaveTag(projectId, deviceId, tag, draft) },
                        )
                        TagEditorForm(
                            dataTypes = state.dataTypes.map { item -> CupertinoOption(item.id, item.name, item.description) },
                            registerTypes = state.registerTypes.map { item -> CupertinoOption(item.id, item.name, item.description) },
                            draft = draft,
                            onDraftChange = { draft = it },
                        )
                    }
                } else {
                    HostConfigDenseInfoSection(
                        title = "标签资料",
                        entries = listOf(
                            "标签名称" to tag.name,
                            "描述" to tag.description.orDash(),
                            "数据类型" to tag.dataTypeName,
                            "寄存器类型" to tag.registerTypeName,
                            "寄存器地址" to tag.registerAddress.toString(),
                            "启用" to if (tag.enabled) "是" else "否",
                            "标签类型" to (tag.pointType?.label() ?: "-"),
                            "排序" to tag.sortIndex.toString(),
                        ),
                    )
                    HostConfigDenseInfoSection(
                        title = "采集策略",
                        entries = listOf(
                            "默认值" to tag.defaultValue.orDash(),
                            "异常值" to tag.exceptionValue.orDash(),
                            "防抖(ms)" to (tag.debounceMs?.toString() ?: "-"),
                            "线性转换" to if (tag.scalingEnabled) "已启用" else "未启用",
                        ),
                    )
                    if (tag.scalingEnabled) {
                        HostConfigDenseInfoSection(
                            title = "线性转换",
                            entries = listOf(
                                "偏移量" to tag.scalingOffset.orDash(),
                                "原始范围" to "${tag.rawMin.orDash()} ~ ${tag.rawMax.orDash()}",
                                "工程范围" to "${tag.engMin.orDash()} ~ ${tag.engMax.orDash()}",
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
/**
 * 处理当前nodeinlineeditorbar。
 *
 * @param saving saving。
 * @param enabled 启用状态。
 * @param onSave on保存。
 */
internal fun CurrentNodeInlineEditorBar(
    saving: Boolean,
    enabled: Boolean,
    onSave: () -> Unit,
) {
    CupertinoEditorSaveBar(
        saving = saving,
        enabled = enabled,
        onSave = onSave,
    )
}
