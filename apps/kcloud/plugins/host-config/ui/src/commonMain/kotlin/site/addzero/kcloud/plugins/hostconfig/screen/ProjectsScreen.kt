@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.projects.findDevice
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel
import site.addzero.kcloud.plugins.hostconfig.projects.findModule

/**
 * 处理项目界面。
 */
@Route(
    title = "工程配置",
    routePath = "host-config/projects",
    icon = "SettingsApplications",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun ProjectsScreen() {
    val viewModel = koinViewModel<ProjectsViewModel>()
    val state = viewModel.screenState
    val treeViewModel = rememberTreeViewModel<HostConfigTreeNode>()
    val sqliteFilePicker = remember { HostConfigProjectSqliteFilePicker() }

    var createProject by remember { mutableStateOf(false) }
    var linkProtocolSeed by remember { mutableStateOf<LinkProtocolSeed?>(null) }
    var createModuleSeed by remember { mutableStateOf<CreateModuleSeed?>(null) }
    var chooseProtocolSeed by remember { mutableStateOf<ChooseProtocolSeed?>(null) }
    var createDeviceSeed by remember { mutableStateOf<CreateDeviceSeed?>(null) }
    var createTagSeed by remember { mutableStateOf<CreateTagSeed?>(null) }
    var moveSeed by remember { mutableStateOf<MoveNodeSeed?>(null) }
    var nodeActionMenu by remember { mutableStateOf<NodeActionMenuSeed?>(null) }
    var currentNodePanelCollapsed by remember { mutableStateOf(false) }
    var editingNodeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.selectedNodeId) {
        if (editingNodeId != null && editingNodeId != state.selectedNodeId) {
            editingNodeId = null
        }
    }

    /**
     * 处理打开创建设备。
     *
     * @param projectId 项目 ID。
     * @param protocol 协议。
     */
    fun openCreateDevice(
        projectId: Long,
        protocol: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode,
    ) {
        createDeviceSeed = CreateDeviceSeed(
            projectId = projectId,
            protocolId = protocol.id,
            protocolName = protocol.displayName(),
            protocolTemplateName = protocol.protocolTemplateName,
        )
    }

    /**
     * 处理打开创建设备from项目。
     *
     * @param projectId 项目 ID。
     */
    fun openCreateDeviceFromProject(projectId: Long) {
        val candidates = resolveProjectProtocolCandidates(state, projectId)
        when (candidates.size) {
            0 -> Unit
            1 -> {
                val protocol = state.projectTrees.findProtocol(candidates.first().protocolId) ?: return
                openCreateDevice(projectId, protocol)
            }

            else -> {
                chooseProtocolSeed = ChooseProtocolSeed(
                    projectId = projectId,
                    projectName = state.projectTrees.firstOrNull { it.id == projectId }?.name.orEmpty(),
                    title = "选择承载协议",
                    subtitle = "设备直接挂在协议下，先选择协议再录入设备参数。",
                    candidates = candidates,
                )
            }
        }
    }

    /**
     * 处理打开创建模块。
     *
     * @param projectId 项目 ID。
     * @param device 设备。
     */
    fun openCreateModule(
        projectId: Long,
        device: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode,
    ) {
        val protocol = state.projectTrees.findProtocol(device.protocolId) ?: return
        createModuleSeed = CreateModuleSeed(
            projectId = projectId,
            deviceId = device.id,
            availableTemplates = resolveModuleTemplatesForProtocol(state, device.protocolId),
            deviceName = device.name,
            protocolId = protocol.id,
            protocolName = protocol.displayName(),
            protocolTemplateName = protocol.protocolTemplateName,
        )
    }

    /**
     * 删除node。
     *
     * @param node node。
     */
    fun deleteNode(node: HostConfigTreeNode) {
        when (node.kind) {
            HostConfigNodeKind.PROJECT -> viewModel.deleteProject(node.entityId)
            HostConfigNodeKind.PROTOCOL -> viewModel.deleteProtocol(node.projectId, node.entityId)
            HostConfigNodeKind.MODULE -> viewModel.deleteModule(node.projectId, node.entityId)
            HostConfigNodeKind.DEVICE -> viewModel.deleteDevice(node.projectId, node.entityId)
            HostConfigNodeKind.TAG -> {
                viewModel.deleteTag(
                    projectId = node.projectId,
                    deviceId = node.parentEntityId ?: return,
                    tagId = node.entityId,
                )
            }
        }
    }

    /**
     * 处理handlenodeaction。
     *
     * @param node node。
     * @param actionType action类型。
     */
    fun handleNodeAction(node: HostConfigTreeNode, actionType: NodeActionType) {
        viewModel.clearNotice()
        when (actionType) {
            NodeActionType.CREATE_MODULE -> {
                when (node.kind) {
                    HostConfigNodeKind.DEVICE -> {
                        val device = state.projectTrees.findDevice(node.entityId) ?: return
                        openCreateModule(node.projectId, device)
                    }

                    else -> Unit
                }
            }

            NodeActionType.LINK_PROTOCOL -> linkProtocolSeed = LinkProtocolSeed(node.projectId)
            NodeActionType.CREATE_DEVICE -> {
                when (node.kind) {
                    HostConfigNodeKind.PROJECT -> openCreateDeviceFromProject(node.projectId)
                    HostConfigNodeKind.PROTOCOL -> {
                        val protocol = state.projectTrees.findProtocol(node.entityId) ?: return
                        openCreateDevice(node.projectId, protocol)
                    }

                    else -> Unit
                }
            }
            NodeActionType.CREATE_TAG -> {
                if (node.kind != HostConfigNodeKind.MODULE) {
                    return
                }
                val module = state.projectTrees.findModule(node.entityId) ?: return
                createTagSeed = CreateTagSeed(node.projectId, module.deviceId)
            }

            NodeActionType.MOVE -> moveSeed = MoveNodeSeed(node)
            NodeActionType.DELETE -> deleteNode(node)
        }
    }

    /**
     * 处理打开nodeactionmenu。
     *
     * @param node node。
     */
    fun openNodeActionMenu(node: HostConfigTreeNode) {
        if (nodeActionMenu?.node?.id == node.id) {
            nodeActionMenu = null
            return
        }
        val menuSeed = resolveNodeActionMenu(state, node)
        if (menuSeed.items.isNotEmpty()) {
            nodeActionMenu = menuSeed
        }
    }

    /**
     * 选择nodefrom分页。
     *
     * @param nodeId node ID。
     */
    fun selectNodeFromPage(nodeId: String) {
        if (nodeId != state.selectedNodeId) {
            editingNodeId = null
        }
        viewModel.selectNode(nodeId)
    }

    ProjectsWorkbenchContent(
        state = state,
        treeViewModel = treeViewModel,
        currentNodePanelCollapsed = currentNodePanelCollapsed,
        editingNodeId = editingNodeId,
        nodeActionMenu = nodeActionMenu,
        onCreateProject = {
            viewModel.clearNotice()
            createProject = true
        },
        onExportProjectSqlite = {
            viewModel.exportSelectedProjectSqlite()
        },
        onImportProjectSqlite = {
            sqliteFilePicker.pickSqliteFile { selectedPath ->
                selectedPath?.let(viewModel::importProjectSqlite)
            }
        },
        onRefresh = viewModel::refresh,
        onSelectNode = ::selectNodeFromPage,
        onNodeAction = ::handleNodeAction,
        onOpenNodeActionMenu = ::openNodeActionMenu,
        onStartEditing = {
            currentNodePanelCollapsed = false
            editingNodeId = it
        },
        onStopEditing = { editingNodeId = null },
        onTogglePanelCollapsed = {
            if (!currentNodePanelCollapsed) {
                editingNodeId = null
            }
            currentNodePanelCollapsed = !currentNodePanelCollapsed
        },
        onSaveProject = { project, draft -> viewModel.updateProject(project.id, draft.toProjectUpdateRequest()) },
        onSaveProtocol = { projectId, protocol, draft ->
            val template = state.protocolTemplates.firstOrNull { item -> item.id == protocol.protocolTemplateId }
            viewModel.updateProtocol(projectId, protocol.id, draft.toProtocolUpdateRequest(projectId, protocol, template))
        },
        onSaveModule = { projectId, module, draft ->
            viewModel.updateModule(projectId, module.id, draft.toModuleUpdateRequest())
        },
        onSaveDevice = { projectId, device, draft ->
            viewModel.updateDevice(projectId, device.id, draft.toDeviceUpdateRequest())
        },
        onSaveTag = { projectId, deviceId, tag, draft ->
            viewModel.updateTag(projectId, deviceId, tag.id, draft.toTagUpdateRequest(), draft.toTagValueTextInputs())
        },
        onPrevTagPage = viewModel::loadPreviousTagPage,
        onNextTagPage = viewModel::loadNextTagPage,
        onDismissNodeActionMenu = { nodeActionMenu = null },
    )

    ProjectsDialogHost(
        state = state,
        viewModel = viewModel,
        createProject = createProject,
        linkProtocolSeed = linkProtocolSeed,
        chooseProtocolSeed = chooseProtocolSeed,
        createModuleSeed = createModuleSeed,
        createDeviceSeed = createDeviceSeed,
        createTagSeed = createTagSeed,
        moveSeed = moveSeed,
        onDismissCreateProject = { createProject = false },
        onDismissLinkProtocol = { linkProtocolSeed = null },
        onDismissChooseProtocol = { chooseProtocolSeed = null },
        onDismissCreateModule = { createModuleSeed = null },
        onDismissCreateDevice = { createDeviceSeed = null },
        onDismissCreateTag = { createTagSeed = null },
        onDismissMoveNode = { moveSeed = null },
        onOpenCreateModule = ::openCreateModule,
        onOpenCreateDevice = ::openCreateDevice,
    )
}
