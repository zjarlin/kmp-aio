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
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel

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
/**
 * 处理项目界面。
 */
fun ProjectsScreen() {
    val viewModel = koinViewModel<ProjectsViewModel>()
    val state = viewModel.screenState
    val treeViewModel = rememberTreeViewModel<HostConfigTreeNode>()

    var createProject by remember { mutableStateOf(false) }
    var linkProtocolSeed by remember { mutableStateOf<LinkProtocolSeed?>(null) }
    var createModuleSeed by remember { mutableStateOf<CreateModuleSeed?>(null) }
    var chooseModuleProtocolSeed by remember { mutableStateOf<ChooseModuleProtocolSeed?>(null) }
    var createDeviceSeed by remember { mutableStateOf<CreateDeviceSeed?>(null) }
    var createTagSeed by remember { mutableStateOf<CreateTagSeed?>(null) }
    var moveSeed by remember { mutableStateOf<MoveNodeSeed?>(null) }
    var uploadSeed by remember { mutableStateOf<UploadSeed?>(null) }
    var nodeActionMenu by remember { mutableStateOf<NodeActionMenuSeed?>(null) }
    var currentNodePanelCollapsed by remember { mutableStateOf(false) }
    var editingNodeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.selectedNodeId) {
        if (editingNodeId != null && editingNodeId != state.selectedNodeId) {
            editingNodeId = null
        }
    }

    /**
     * 处理打开创建模块。
     *
     * @param projectId 项目 ID。
     * @param protocol 协议。
     */
    fun openCreateModule(
        projectId: Long,
        protocol: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode,
    ) {
        createModuleSeed = CreateModuleSeed(
            projectId = projectId,
            protocolId = protocol.id,
            availableTemplates = resolveModuleTemplatesForProtocol(state, protocol.protocolTemplateId),
            protocolName = protocol.displayName(),
            protocolTemplateName = protocol.protocolTemplateName,
        )
    }

    /**
     * 处理打开创建模块from项目。
     *
     * @param projectId 项目 ID。
     */
    fun openCreateModuleFromProject(projectId: Long) {
        val candidates = resolveModuleProtocolCandidates(state, projectId)
        when (candidates.size) {
            0 -> Unit
            1 -> {
                val protocol = state.projectTrees.findProtocol(candidates.first().protocolId) ?: return
                openCreateModule(projectId, protocol)
            }

            else -> {
                chooseModuleProtocolSeed = ChooseModuleProtocolSeed(
                    projectId = projectId,
                    projectName = state.projectTrees.firstOrNull { it.id == projectId }?.name.orEmpty(),
                    candidates = candidates,
                )
            }
        }
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
                    HostConfigNodeKind.PROJECT -> openCreateModuleFromProject(node.projectId)
                    HostConfigNodeKind.PROTOCOL -> {
                        val protocol = state.projectTrees.findProtocol(node.entityId) ?: return
                        openCreateModule(node.projectId, protocol)
                    }

                    else -> Unit
                }
            }

            NodeActionType.LINK_PROTOCOL -> linkProtocolSeed = LinkProtocolSeed(node.projectId)
            NodeActionType.CREATE_DEVICE -> createDeviceSeed = CreateDeviceSeed(node.projectId, node.entityId)
            NodeActionType.CREATE_TAG -> {
                val deviceId = when (node.kind) {
                    HostConfigNodeKind.DEVICE -> node.entityId
                    HostConfigNodeKind.TAG -> node.parentEntityId
                    else -> null
                } ?: return
                createTagSeed = CreateTagSeed(node.projectId, deviceId)
            }

            NodeActionType.MOVE -> moveSeed = MoveNodeSeed(node)
            NodeActionType.DELETE -> deleteNode(node)
            NodeActionType.UPLOAD_PROJECT -> uploadSeed = UploadSeed(node.projectId)
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
        onDownloadProject = {
            viewModel.showNotice("下载工程入口已添加，后续接入 sqlite 导出。")
        },
        onImportProject = {
            viewModel.showNotice("导入工程入口已添加，后续接入本地 sqlite 文件选择。")
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
            viewModel.updateProtocol(projectId, protocol.id, draft.toProtocolUpdateRequest(projectId, protocol))
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
        chooseModuleProtocolSeed = chooseModuleProtocolSeed,
        createModuleSeed = createModuleSeed,
        createDeviceSeed = createDeviceSeed,
        createTagSeed = createTagSeed,
        moveSeed = moveSeed,
        uploadSeed = uploadSeed,
        onDismissCreateProject = { createProject = false },
        onDismissLinkProtocol = { linkProtocolSeed = null },
        onDismissChooseModuleProtocol = { chooseModuleProtocolSeed = null },
        onDismissCreateModule = { createModuleSeed = null },
        onDismissCreateDevice = { createDeviceSeed = null },
        onDismissCreateTag = { createTagSeed = null },
        onDismissMoveNode = { moveSeed = null },
        onDismissUpload = { uploadSeed = null },
        onOpenCreateModule = ::openCreateModule,
    )
}
