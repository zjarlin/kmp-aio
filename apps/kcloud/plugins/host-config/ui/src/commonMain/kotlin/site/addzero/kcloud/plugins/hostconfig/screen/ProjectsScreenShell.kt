package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.tree.TreeViewModel
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.icon
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel

@Composable
/**
 * 处理项目workbenchcontent。
 *
 * @param state 状态。
 * @param treeViewModel 树视图模型。
 * @param currentNodePanelCollapsed 当前nodepanelcollapsed。
 * @param editingNodeId editingnode ID。
 * @param nodeActionMenu nodeactionmenu。
 * @param onCreateProject on创建项目。
 * @param onRefresh on刷新。
 * @param onSelectNode on选择node。
 * @param onNodeAction onnodeaction。
 * @param onOpenNodeActionMenu on打开nodeactionmenu。
 * @param onStartEditing on开始editing。
 * @param onStopEditing on停止editing。
 * @param onTogglePanelCollapsed ontogglepanelcollapsed。
 * @param onSaveProject on保存项目。
 * @param onSaveProtocol on保存协议。
 * @param onSaveModule on保存模块。
 * @param onSaveDevice on保存设备。
 * @param onSaveTag on保存标签。
 * @param onPrevTagPage onprev标签分页。
 * @param onNextTagPage onnext标签分页。
 * @param onDismissNodeActionMenu ondismissnodeactionmenu。
 */
internal fun ProjectsWorkbenchContent(
    state: ProjectsScreenState,
    treeViewModel: TreeViewModel<HostConfigTreeNode>,
    currentNodePanelCollapsed: Boolean,
    editingNodeId: String?,
    nodeActionMenu: NodeActionMenuSeed?,
    onCreateProject: () -> Unit,
    onDownloadProject: () -> Unit,
    onImportProject: () -> Unit,
    onRefresh: () -> Unit,
    onSelectNode: (String) -> Unit,
    onNodeAction: (HostConfigTreeNode, NodeActionType) -> Unit,
    onOpenNodeActionMenu: (HostConfigTreeNode) -> Unit,
    onStartEditing: (String) -> Unit,
    onStopEditing: () -> Unit,
    onTogglePanelCollapsed: () -> Unit,
    onSaveProject: (site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse, ProjectDraft) -> Unit,
    onSaveProtocol: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode, ProtocolDraft) -> Unit,
    onSaveModule: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode, ModuleDraft) -> Unit,
    onSaveDevice: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode, DeviceDraft) -> Unit,
    onSaveTag: (Long, Long, site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse, TagDraft) -> Unit,
    onPrevTagPage: () -> Unit,
    onNextTagPage: () -> Unit,
    onDismissNodeActionMenu: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WorkbenchTreeSidebar(
            items = state.treeNodes,
            selectedId = state.selectedNodeId,
            onNodeClick = { node -> onSelectNode(node.id) },
            onNodeContextMenu = { node ->
                onSelectNode(node.id)
                onOpenNodeActionMenu(node)
            },
            modifier = Modifier.fillMaxHeight().widthIn(min = 280.dp, max = 320.dp),
            searchPlaceholder = "搜索工程树",
            treeViewModel = treeViewModel,
            header = {
                state.errorMessage?.let { CupertinoStatusStrip(it) }
                state.noticeMessage?.let { CupertinoStatusStrip(it) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkbenchActionButton(
                        text = "新建工程",
                        onClick = onCreateProject,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                    WorkbenchActionButton(
                        text = "下载工程",
                        onClick = onDownloadProject,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                    WorkbenchActionButton(
                        text = "导入工程",
                        onClick = onImportProject,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                    WorkbenchActionButton(
                        text = if (state.loading) "加载中" else "刷新",
                        onClick = onRefresh,
                        variant = WorkbenchButtonVariant.Secondary,
                    )
                }
            },
            getId = { it.id },
            getLabel = { it.label },
            getCaption = { it.caption },
            getChildren = { it.children },
            getIcon = { it.kind.icon() },
            nodeTrailingContent = { node ->
                Box(contentAlignment = Alignment.TopEnd) {
                    WorkbenchIconButton(
                        onClick = {
                            onSelectNode(node.id)
                            onOpenNodeActionMenu(node)
                        },
                        tooltip = "节点操作",
                    ) {
                        Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = null)
                    }
                    nodeActionMenu?.takeIf { it.node.id == node.id }?.let { seed ->
                        NodeActionDropdownMenu(
                            seed = seed,
                            onDismissRequest = onDismissNodeActionMenu,
                            onAction = { actionType ->
                                onDismissNodeActionMenu()
                                onNodeAction(seed.node, actionType)
                            },
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CurrentNodePanel(
                state = state,
                onSelectNode = onSelectNode,
                collapsed = currentNodePanelCollapsed,
                saving = state.busy,
                editingNodeId = editingNodeId,
                onStartEditing = onStartEditing,
                onStopEditing = onStopEditing,
                onSaveProject = onSaveProject,
                onSaveProtocol = onSaveProtocol,
                onSaveModule = onSaveModule,
                onSaveDevice = onSaveDevice,
                onSaveTag = onSaveTag,
                onToggleCollapsed = onTogglePanelCollapsed,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    NodeChildrenPanel(
                        state = state,
                        onSelectNode = onSelectNode,
                        onPrevTagPage = onPrevTagPage,
                        onNextTagPage = onNextTagPage,
                    )
                }
            }
        }
    }
}

@Composable
/**
 * 处理项目dialog主机。
 *
 * @param state 状态。
 * @param viewModel 视图模型。
 * @param createProject 创建项目。
 * @param linkProtocolSeed 关联协议seed。
 * @param chooseModuleProtocolSeed choose模块协议seed。
 * @param createModuleSeed 创建模块seed。
 * @param createDeviceSeed 创建设备seed。
 * @param createTagSeed 创建标签seed。
 * @param moveSeed 移动seed。
 * @param uploadSeed 上传seed。
 * @param onDismissCreateProject ondismiss创建项目。
 * @param onDismissLinkProtocol ondismiss关联协议。
 * @param onDismissChooseModuleProtocol ondismisschoose模块协议。
 * @param onDismissCreateModule ondismiss创建模块。
 * @param onDismissCreateDevice ondismiss创建设备。
 * @param onDismissCreateTag ondismiss创建标签。
 * @param onDismissMoveNode ondismiss移动node。
 * @param onDismissUpload ondismiss上传。
 * @param onOpenCreateModule on打开创建模块。
 */
internal fun ProjectsDialogHost(
    state: ProjectsScreenState,
    viewModel: ProjectsViewModel,
    createProject: Boolean,
    linkProtocolSeed: LinkProtocolSeed?,
    chooseModuleProtocolSeed: ChooseModuleProtocolSeed?,
    createModuleSeed: CreateModuleSeed?,
    createDeviceSeed: CreateDeviceSeed?,
    createTagSeed: CreateTagSeed?,
    moveSeed: MoveNodeSeed?,
    uploadSeed: UploadSeed?,
    onDismissCreateProject: () -> Unit,
    onDismissLinkProtocol: () -> Unit,
    onDismissChooseModuleProtocol: () -> Unit,
    onDismissCreateModule: () -> Unit,
    onDismissCreateDevice: () -> Unit,
    onDismissCreateTag: () -> Unit,
    onDismissMoveNode: () -> Unit,
    onDismissUpload: () -> Unit,
    onOpenCreateModule: (Long, site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode) -> Unit,
) {
    if (createProject) {
        CreateProjectDialog(
            saving = state.busy,
            onDismissRequest = onDismissCreateProject,
            onSave = { draft ->
                viewModel.createProject(draft.toProjectCreateRequest())
                onDismissCreateProject()
            },
        )
    }

    linkProtocolSeed?.let { seed ->
        LinkProtocolDialog(
            options = resolveLinkableProtocolTemplates(state, seed.projectId).map { item ->
                CupertinoOption(item.id, item.name, item.description)
            },
            saving = state.busy,
            onDismissRequest = onDismissLinkProtocol,
            onSave = { templateId, sortIndex ->
                val template = state.protocolTemplates.firstOrNull { it.id == templateId } ?: return@LinkProtocolDialog
                viewModel.createProtocol(seed.projectId, buildProjectLinkedProtocolRequest(seed.projectId, template, sortIndex))
                onDismissLinkProtocol()
            },
        )
    }

    chooseModuleProtocolSeed?.let { seed ->
        ChooseModuleProtocolDialog(
            seed = seed,
            saving = state.busy,
            onDismissRequest = onDismissChooseModuleProtocol,
            onSave = { protocolId ->
                val protocol = state.projectTrees.findProtocol(protocolId) ?: return@ChooseModuleProtocolDialog
                onOpenCreateModule(seed.projectId, protocol)
                onDismissChooseModuleProtocol()
            },
        )
    }

    createModuleSeed?.let { seed ->
        CreateModuleDialog(
            protocolName = seed.protocolName,
            protocolTemplateName = seed.protocolTemplateName,
            templates = seed.availableTemplates.map { item -> CupertinoOption(item.id, item.name, item.description) },
            saving = state.busy,
            onDismissRequest = onDismissCreateModule,
            onSave = { draft ->
                viewModel.createModuleUnderProtocol(
                    projectId = seed.projectId,
                    protocolId = seed.protocolId ?: return@CreateModuleDialog,
                    request = draft.toModuleCreateRequest(),
                )
                onDismissCreateModule()
            },
        )
    }

    createDeviceSeed?.let { seed ->
        CreateDeviceDialog(
            deviceTypes = state.deviceTypes.map { item -> CupertinoOption(item.id, item.name, item.description) },
            saving = state.busy,
            onDismissRequest = onDismissCreateDevice,
            onSave = { draft ->
                viewModel.createDevice(seed.projectId, seed.moduleId, draft.toDeviceCreateRequest())
                onDismissCreateDevice()
            },
        )
    }

    createTagSeed?.let { seed ->
        CreateTagDialog(
            dataTypes = state.dataTypes.map { item -> CupertinoOption(item.id, item.name, item.description) },
            registerTypes = state.registerTypes.map { item -> CupertinoOption(item.id, item.name, item.description) },
            saving = state.busy,
            onDismissRequest = onDismissCreateTag,
            onSave = { draft ->
                viewModel.createTag(
                    projectId = seed.projectId,
                    deviceId = seed.deviceId,
                    request = draft.toTagCreateRequest(),
                    valueTexts = draft.toTagValueTextInputs(),
                )
                onDismissCreateTag()
            },
        )
    }

    moveSeed?.let { seed ->
        MoveNodeDialog(
            nodeKind = seed.node.kind,
            options = resolveMoveOptions(state, seed.node),
            saving = state.busy,
            onDismissRequest = onDismissMoveNode,
            onSave = { targetKey, sortIndex ->
                when (seed.node.kind) {
                    HostConfigNodeKind.PROTOCOL -> {
                        val targetProjectId = targetKey.substringAfter("project:").toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveProtocol(seed.node.entityId, seed.node.projectId, targetProjectId, sortIndex)
                    }

                    HostConfigNodeKind.MODULE -> {
                        val parts = targetKey.split(":")
                        val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                        val targetProtocolId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveModuleToProtocol(targetProjectId, seed.node.entityId, targetProtocolId, sortIndex)
                    }

                    HostConfigNodeKind.DEVICE -> {
                        val parts = targetKey.split(":")
                        val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                        val targetModuleId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveDevice(targetProjectId, seed.node.entityId, targetModuleId, sortIndex)
                    }

                    HostConfigNodeKind.TAG -> {
                        val parts = targetKey.split(":")
                        val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                        val targetDeviceId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveTag(targetProjectId, targetDeviceId, seed.node.entityId, sortIndex)
                    }

                    HostConfigNodeKind.PROJECT -> Unit
                }
                onDismissMoveNode()
            },
        )
    }

    uploadSeed?.let {
        UploadProjectDialog(
            state = state,
            saving = state.busy,
            onDismissRequest = onDismissUpload,
            onSubmit = { draft ->
                viewModel.submitUpload(
                    ProjectUploadRequest(
                        ipAddress = draft.ipAddress,
                        includeDriverConfig = draft.includeDriverConfig,
                        includeFirmwareUpgrade = draft.includeFirmwareUpgrade,
                        projectPath = draft.projectPath.ifBlank { null },
                        selectedFileName = draft.selectedFileName.ifBlank { null },
                        fastMode = draft.fastMode,
                    ),
                )
                onDismissUpload()
            },
            onTriggerAction = { action, ipAddress ->
                viewModel.triggerUploadAction(
                    action = action,
                    request = ProjectUploadRemoteActionRequest(ipAddress = ipAddress),
                )
            },
        )
    }
}
