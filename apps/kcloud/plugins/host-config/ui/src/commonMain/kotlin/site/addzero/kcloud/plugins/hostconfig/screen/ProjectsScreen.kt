@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoActionSheet
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.cancel
import io.github.robinpcrd.cupertino.default
import io.github.robinpcrd.cupertino.destructive
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextInput
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigBooleanField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigDialog
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigKeyValueRow
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigModuleBoard
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigOption
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigPanel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigSelectionField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigSectionTitle
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTextField
import site.addzero.kcloud.plugins.hostconfig.common.icon
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.common.orDash
import site.addzero.kcloud.plugins.hostconfig.common.resolveModuleBoardModel
import site.addzero.kcloud.plugins.hostconfig.common.toSizeLabel
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel
import site.addzero.kcloud.plugins.hostconfig.projects.findDevice
import site.addzero.kcloud.plugins.hostconfig.projects.findModule
import site.addzero.kcloud.plugins.hostconfig.projects.findProtocol

@Route(
    title = "工程配置",
    routePath = "host-config/projects",
    icon = "SettingsApplications",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "宿主配置",
            icon = "SettingsApplications",
            order = 10,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun ProjectsScreen() {
    val viewModel = koinViewModel<ProjectsViewModel>()
    val state = viewModel.screenState

    var projectEditor by remember { mutableStateOf<ProjectEditorSeed?>(null) }
    var protocolEditor by remember { mutableStateOf<ProtocolEditorSeed?>(null) }
    var linkProtocolSeed by remember { mutableStateOf<LinkProtocolSeed?>(null) }
    var moduleEditor by remember { mutableStateOf<ModuleEditorSeed?>(null) }
    var moduleProtocolPickerSeed by remember { mutableStateOf<ModuleProtocolPickerSeed?>(null) }
    var deviceEditor by remember { mutableStateOf<DeviceEditorSeed?>(null) }
    var tagEditor by remember { mutableStateOf<TagEditorSeed?>(null) }
    var moveSeed by remember { mutableStateOf<MoveNodeSeed?>(null) }
    var uploadSeed by remember { mutableStateOf<UploadSeed?>(null) }
    var nodeActionMenu by remember { mutableStateOf<NodeActionMenuSeed?>(null) }

    val currentCreateSpec = resolveCurrentCreateSpec(state)
    val treeViewModel = rememberTreeViewModel<site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode>()

    fun openModuleEditorForProtocol(
        projectId: Long,
        protocol: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode,
        existing: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode? = null,
    ) {
        moduleEditor = ModuleEditorSeed(
            projectId = projectId,
            protocolId = protocol.id,
            existing = existing,
            availableTemplates = resolveModuleTemplatesForProtocol(
                state = state,
                protocolTemplateId = protocol.protocolTemplateId,
            ),
            protocolName = protocol.name,
            protocolTemplateName = protocol.protocolTemplateName,
        )
    }

    fun openProjectModuleCreation(projectId: Long) {
        val candidates = resolveModuleProtocolCandidates(
            state = state,
            projectId = projectId,
        )
        when (candidates.size) {
            0 -> Unit
            1 -> {
                val protocol = state.projectTrees.findProtocol(candidates.first().protocolId) ?: return
                openModuleEditorForProtocol(projectId, protocol)
            }

            else -> {
                moduleProtocolPickerSeed = ModuleProtocolPickerSeed(
                    projectId = projectId,
                    projectName = state.projectTrees.firstOrNull { project -> project.id == projectId }?.name.orEmpty(),
                    candidates = candidates,
                )
            }
        }
    }

    fun editNode(
        node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
    ) {
        when (node.kind) {
            HostConfigNodeKind.PROJECT -> {
                projectEditor = ProjectEditorSeed(existingId = node.entityId)
            }

            HostConfigNodeKind.PROTOCOL -> {
                val protocol = state.projectTrees.findProtocol(node.entityId) ?: return
                protocolEditor = ProtocolEditorSeed(
                    projectId = node.projectId,
                    existing = protocol,
                )
            }

            HostConfigNodeKind.MODULE -> {
                val module = state.projectTrees.findModule(node.entityId) ?: return
                val protocol = state.projectTrees.findProtocol(module.protocolId) ?: return
                openModuleEditorForProtocol(
                    projectId = node.projectId,
                    protocol = protocol,
                    existing = module,
                )
            }

            HostConfigNodeKind.DEVICE -> {
                val device = state.projectTrees.findDevice(node.entityId) ?: return
                deviceEditor = DeviceEditorSeed(
                    projectId = node.projectId,
                    moduleId = node.parentEntityId ?: return,
                    existing = device,
                )
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                    ?.takeIf { detail -> detail.id == node.entityId }
                    ?: return
                tagEditor = TagEditorSeed(
                    projectId = node.projectId,
                    deviceId = node.parentEntityId ?: return,
                    existing = tag,
                )
            }
        }
    }

    fun deleteNode(
        node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
    ) {
        when (node.kind) {
            HostConfigNodeKind.PROJECT -> {
                viewModel.deleteProject(node.entityId)
            }

            HostConfigNodeKind.PROTOCOL -> {
                viewModel.deleteProtocol(
                    projectId = node.projectId,
                    protocolId = node.entityId,
                )
            }

            HostConfigNodeKind.MODULE -> {
                viewModel.deleteModule(
                    projectId = node.projectId,
                    moduleId = node.entityId,
                )
            }

            HostConfigNodeKind.DEVICE -> {
                viewModel.deleteDevice(
                    projectId = node.projectId,
                    deviceId = node.entityId,
                )
            }

            HostConfigNodeKind.TAG -> {
                viewModel.deleteTag(
                    projectId = node.projectId,
                    deviceId = node.parentEntityId ?: return,
                    tagId = node.entityId,
                )
            }
        }
    }

    fun handleNodeAction(
        node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
        actionType: NodeActionType,
    ) {
        viewModel.clearNotice()
        when (actionType) {
            NodeActionType.CREATE_MODULE -> {
                when (node.kind) {
                    HostConfigNodeKind.PROJECT -> {
                        openProjectModuleCreation(node.projectId)
                    }

                    HostConfigNodeKind.PROTOCOL -> {
                        val protocol = state.projectTrees.findProtocol(node.entityId) ?: return
                        openModuleEditorForProtocol(node.projectId, protocol)
                    }

                    else -> Unit
                }
            }

            NodeActionType.CREATE_PROTOCOL -> {
                protocolEditor = ProtocolEditorSeed(projectId = node.projectId)
            }

            NodeActionType.LINK_PROTOCOL -> {
                linkProtocolSeed = LinkProtocolSeed(projectId = node.projectId)
            }

            NodeActionType.CREATE_DEVICE -> {
                deviceEditor = DeviceEditorSeed(
                    projectId = node.projectId,
                    moduleId = node.entityId,
                )
            }

            NodeActionType.CREATE_TAG -> {
                val deviceId = when (node.kind) {
                    HostConfigNodeKind.DEVICE -> node.entityId
                    HostConfigNodeKind.TAG -> node.parentEntityId
                    else -> null
                } ?: return
                tagEditor = TagEditorSeed(
                    projectId = node.projectId,
                    deviceId = deviceId,
                )
            }

            NodeActionType.EDIT -> {
                editNode(node)
            }

            NodeActionType.MOVE -> {
                moveSeed = MoveNodeSeed(node)
            }

            NodeActionType.DELETE -> {
                deleteNode(node)
            }

            NodeActionType.UPLOAD_PROJECT -> {
                uploadSeed = UploadSeed(node.projectId)
            }
        }
    }

    fun openNodeActionMenu(
        node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
    ) {
        val menuSeed = resolveNodeActionMenu(
            state = state,
            node = node,
        )
        if (menuSeed.items.isNotEmpty()) {
            nodeActionMenu = menuSeed
        }
    }

    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode -> node.id },
            getLabel = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode -> node.label },
            getChildren = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode -> node.children },
            getIcon = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode -> node.kind.icon() },
        )
    }

    LaunchedEffect(treeViewModel, state.treeNodes) {
        treeViewModel.setItems(
            newItems = state.treeNodes,
            initiallyExpandedIds = state.treeNodes.allBranchIds(),
        )
    }

    LaunchedEffect(treeViewModel, state.selectedNodeId) {
        treeViewModel.selectNode(state.selectedNodeId)
    }

    SideEffect {
        treeViewModel.onNodeClick = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode ->
            viewModel.selectNode(node.id)
        }
        treeViewModel.onNodeContextMenu = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode ->
            openNodeActionMenu(node)
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.30f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.errorMessage?.let { message ->
                HostConfigStatusStrip(message)
            }
            state.noticeMessage?.let { message ->
                HostConfigStatusStrip(message)
            }
            if (
                state.errorMessage == null &&
                state.noticeMessage == null &&
                currentCreateSpec.hint != null
            ) {
                HostConfigStatusStrip(currentCreateSpec.hint)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkbenchActionButton(
                    text = "新建当前类型",
                    onClick = {
                        val node = currentCreateSpec.node ?: return@WorkbenchActionButton
                        val actionType = currentCreateSpec.actionType ?: return@WorkbenchActionButton
                        handleNodeAction(node, actionType)
                    },
                    variant = WorkbenchButtonVariant.Default,
                    enabled = currentCreateSpec.enabled,
                )
                WorkbenchActionButton(
                    text = "新建工程",
                    onClick = {
                        viewModel.clearNotice()
                        projectEditor = ProjectEditorSeed()
                    },
                    variant = WorkbenchButtonVariant.Outline,
                )
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
            }
            AddSearchBar(
                keyword = treeViewModel.searchQuery,
                onKeyWordChanged = { query: String ->
                    treeViewModel.updateSearchQuery(query)
                    if (query.isNotBlank()) {
                        treeViewModel.performSearch()
                    }
                },
                onSearch = treeViewModel::performSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "搜索工程树",
            )
            AddTree(
                viewModel = treeViewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                selectableLabel = true,
                nodeTrailingContent = { node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode ->
                    WorkbenchIconButton(
                        onClick = {
                            viewModel.selectNode(node.id)
                            openNodeActionMenu(node)
                        },
                        tooltip = "节点操作",
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = null,
                        )
                    }
                },
            )
        }

        Column(
            modifier = Modifier
                .weight(0.70f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    CurrentNodePanel(
                        state = state,
                        onSelectNode = viewModel::selectNode,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    NodeChildrenPanel(
                        state = state,
                        onSelectNode = viewModel::selectNode,
                        onPrevTagPage = viewModel::loadPreviousTagPage,
                        onNextTagPage = viewModel::loadNextTagPage,
                    )
                }
            }
        }
    }

    nodeActionMenu?.let { seed ->
        NodeActionSheet(
            seed = seed,
            onDismissRequest = {
                nodeActionMenu = null
            },
            onAction = { actionType ->
                nodeActionMenu = null
                handleNodeAction(seed.node, actionType)
            },
        )
    }

    projectEditor?.let { seed ->
        ProjectEditorDialog(
            existing = state.projects.firstOrNull { project -> project.id == seed.existingId },
            saving = state.busy,
            onDismissRequest = {
                projectEditor = null
            },
            onSave = { draft ->
                if (seed.existingId == null) {
                    viewModel.createProject(
                        ProjectCreateRequest(
                            name = draft.name,
                            description = draft.description.ifBlank { null },
                            remark = draft.remark.ifBlank { null },
                            sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                        ),
                    )
                } else {
                    viewModel.updateProject(
                        projectId = seed.existingId,
                        request = ProjectUpdateRequest(
                            name = draft.name,
                            description = draft.description.ifBlank { null },
                            remark = draft.remark.ifBlank { null },
                            sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                        ),
                    )
                }
                projectEditor = null
            },
            onUpdateSort = { sortIndex ->
                seed.existingId?.let { projectId ->
                    viewModel.updateProjectPosition(projectId, sortIndex)
                    projectEditor = null
                }
            },
        )
    }

    protocolEditor?.let { seed ->
        ProtocolEditorDialog(
            protocolTemplates = state.protocolTemplates,
            existing = seed.existing,
            saving = state.busy,
            onDismissRequest = {
                protocolEditor = null
            },
            onSave = { draft ->
                val protocolTemplateId = draft.protocolTemplateId ?: return@ProtocolEditorDialog
                val templateCode = state.protocolTemplates
                    .firstOrNull { item -> item.id == protocolTemplateId }
                    ?.code
                    ?: return@ProtocolEditorDialog
                val request = ProtocolCreateRequest(
                    name = draft.name,
                    protocolTemplateId = protocolTemplateId,
                    pollingIntervalMs = draft.pollingIntervalMs.toIntOrNull() ?: 1000,
                    transportConfig = draft.toTransportConfig(templateCode),
                    sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                )
                if (seed.existing == null) {
                    viewModel.createProtocol(seed.projectId, request)
                } else {
                    viewModel.updateProtocol(
                        projectId = seed.projectId,
                        protocolId = seed.existing.id,
                        request = ProtocolUpdateRequest(
                            projectId = seed.projectId,
                            name = request.name,
                            protocolTemplateId = request.protocolTemplateId,
                            pollingIntervalMs = request.pollingIntervalMs,
                            transportConfig = request.transportConfig,
                            sortIndex = request.sortIndex,
                        ),
                    )
                }
                protocolEditor = null
            },
        )
    }

    linkProtocolSeed?.let { seed ->
        LinkProtocolDialog(
            options = resolveLinkableProtocols(
                state = state,
                projectId = seed.projectId,
            ).map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.protocolTemplateName,
                )
            },
            saving = state.busy,
            onDismissRequest = {
                linkProtocolSeed = null
            },
            onSave = { protocolId, sortIndex ->
                viewModel.linkProtocol(
                    projectId = seed.projectId,
                    request = LinkExistingProtocolRequest(
                        protocolId = protocolId,
                        sortIndex = sortIndex,
                    ),
                )
                linkProtocolSeed = null
            },
        )
    }

    moduleProtocolPickerSeed?.let { seed ->
        ModuleProtocolPickerDialog(
            seed = seed,
            saving = state.busy,
            onDismissRequest = {
                moduleProtocolPickerSeed = null
            },
            onSave = { protocolId ->
                val protocol = state.projectTrees.findProtocol(protocolId) ?: return@ModuleProtocolPickerDialog
                openModuleEditorForProtocol(
                    projectId = seed.projectId,
                    protocol = protocol,
                )
                moduleProtocolPickerSeed = null
            },
        )
    }

    moduleEditor?.let { seed ->
        ModuleEditorDialog(
            protocolName = seed.protocolName,
            protocolTemplateName = seed.protocolTemplateName,
            templates = seed.availableTemplates.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            existing = seed.existing,
            saving = state.busy,
            onDismissRequest = {
                moduleEditor = null
            },
            onSave = { draft ->
                val request = ModuleCreateRequest(
                    name = draft.name,
                    moduleTemplateId = draft.moduleTemplateId ?: return@ModuleEditorDialog,
                    sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                )
                if (seed.existing == null) {
                    viewModel.createModuleUnderProtocol(
                        projectId = seed.projectId,
                        protocolId = seed.protocolId ?: return@ModuleEditorDialog,
                        request = request,
                    )
                } else {
                    viewModel.updateModule(
                        projectId = seed.projectId,
                        moduleId = seed.existing.id,
                        request = ModuleUpdateRequest(
                            name = request.name,
                            moduleTemplateId = request.moduleTemplateId,
                            sortIndex = request.sortIndex,
                        ),
                    )
                }
                moduleEditor = null
            },
        )
    }

    deviceEditor?.let { seed ->
        DeviceEditorDialog(
            deviceTypes = state.deviceTypes.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            existing = seed.existing,
            saving = state.busy,
            onDismissRequest = {
                deviceEditor = null
            },
            onSave = { draft ->
                val request = DeviceCreateRequest(
                    name = draft.name,
                    deviceTypeId = draft.deviceTypeId ?: return@DeviceEditorDialog,
                    stationNo = draft.stationNo.toIntOrNull() ?: 1,
                    requestIntervalMs = draft.requestIntervalMs.toIntOrNull(),
                    writeIntervalMs = draft.writeIntervalMs.toIntOrNull(),
                    byteOrder2 = draft.byteOrder2,
                    byteOrder4 = draft.byteOrder4,
                    floatOrder = draft.floatOrder,
                    batchAnalogStart = draft.batchAnalogStart.toIntOrNull(),
                    batchAnalogLength = draft.batchAnalogLength.toIntOrNull(),
                    batchDigitalStart = draft.batchDigitalStart.toIntOrNull(),
                    batchDigitalLength = draft.batchDigitalLength.toIntOrNull(),
                    disabled = draft.disabled,
                    sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                )
                if (seed.existing == null) {
                    viewModel.createDevice(
                        projectId = seed.projectId,
                        moduleId = seed.moduleId,
                        request = request,
                    )
                } else {
                    viewModel.updateDevice(
                        projectId = seed.projectId,
                        deviceId = seed.existing.id,
                        request = DeviceUpdateRequest(
                            name = request.name,
                            deviceTypeId = request.deviceTypeId,
                            stationNo = request.stationNo,
                            requestIntervalMs = request.requestIntervalMs,
                            writeIntervalMs = request.writeIntervalMs,
                            byteOrder2 = request.byteOrder2,
                            byteOrder4 = request.byteOrder4,
                            floatOrder = request.floatOrder,
                            batchAnalogStart = request.batchAnalogStart,
                            batchAnalogLength = request.batchAnalogLength,
                            batchDigitalStart = request.batchDigitalStart,
                            batchDigitalLength = request.batchDigitalLength,
                            disabled = request.disabled,
                            sortIndex = request.sortIndex,
                        ),
                    )
                }
                deviceEditor = null
            },
        )
    }

    tagEditor?.let { seed ->
        TagEditorDialog(
            dataTypes = state.dataTypes.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            registerTypes = state.registerTypes.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            existing = seed.existing,
            saving = state.busy,
            onDismissRequest = {
                tagEditor = null
            },
            onSave = { draft ->
                val request = TagCreateRequest(
                    name = draft.name,
                    description = draft.description.ifBlank { null },
                    dataTypeId = draft.dataTypeId ?: return@TagEditorDialog,
                    registerTypeId = draft.registerTypeId ?: return@TagEditorDialog,
                    registerAddress = draft.registerAddress.toIntOrNull() ?: 0,
                    enabled = draft.enabled,
                    defaultValue = draft.defaultValue.ifBlank { null },
                    exceptionValue = draft.exceptionValue.ifBlank { null },
                    pointType = draft.pointType,
                    debounceMs = draft.debounceMs.toIntOrNull(),
                    sortIndex = draft.sortIndex.toIntOrNull() ?: 0,
                    scalingEnabled = draft.scalingEnabled,
                    scalingOffset = draft.scalingOffset.ifBlank { null },
                    rawMin = draft.rawMin.ifBlank { null },
                    rawMax = draft.rawMax.ifBlank { null },
                    engMin = draft.engMin.ifBlank { null },
                    engMax = draft.engMax.ifBlank { null },
                    forwardEnabled = false,
                    forwardRegisterTypeId = null,
                    forwardRegisterAddress = null,
                )
                val valueTexts = draft.valueTexts.mapIndexed { index, item ->
                    TagValueTextInput(
                        rawValue = item.rawValue,
                        displayText = item.displayText,
                        sortIndex = index,
                    )
                }
                if (seed.existing == null) {
                    viewModel.createTag(
                        projectId = seed.projectId,
                        deviceId = seed.deviceId,
                        request = request,
                        valueTexts = valueTexts,
                    )
                } else {
                    viewModel.updateTag(
                        projectId = seed.projectId,
                        deviceId = seed.deviceId,
                        tagId = seed.existing.id,
                        request = TagUpdateRequest(
                            name = request.name,
                            description = request.description,
                            dataTypeId = request.dataTypeId,
                            registerTypeId = request.registerTypeId,
                            registerAddress = request.registerAddress,
                            enabled = request.enabled,
                            defaultValue = request.defaultValue,
                            exceptionValue = request.exceptionValue,
                            pointType = request.pointType,
                            debounceMs = request.debounceMs,
                            sortIndex = request.sortIndex,
                            scalingEnabled = request.scalingEnabled,
                            scalingOffset = request.scalingOffset,
                            rawMin = request.rawMin,
                            rawMax = request.rawMax,
                            engMin = request.engMin,
                            engMax = request.engMax,
                            forwardEnabled = false,
                            forwardRegisterTypeId = null,
                            forwardRegisterAddress = null,
                        ),
                        valueTexts = valueTexts,
                    )
                }
                tagEditor = null
            },
        )
    }

    moveSeed?.let { seed ->
        MoveNodeDialog(
            nodeKind = seed.node.kind,
            options = resolveMoveOptions(state, seed.node),
            saving = state.busy,
            onDismissRequest = {
                moveSeed = null
            },
            onSave = { targetKey, sortIndex ->
                when (seed.node.kind) {
                    HostConfigNodeKind.PROTOCOL -> {
                        val targetProjectId = targetKey.substringAfter("project:").toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveProtocol(
                            protocolId = seed.node.entityId,
                            sourceProjectId = seed.node.projectId,
                            targetProjectId = targetProjectId,
                            sortIndex = sortIndex,
                        )
                    }

                    HostConfigNodeKind.MODULE -> {
                        when {
                            targetKey.startsWith("project:") -> {
                                val targetProjectId = targetKey.substringAfter("project:").toLongOrNull() ?: return@MoveNodeDialog
                                viewModel.moveModuleToProject(
                                    sourceProjectId = seed.node.projectId,
                                    targetProjectId = targetProjectId,
                                    moduleId = seed.node.entityId,
                                    sortIndex = sortIndex,
                                )
                            }

                            targetKey.startsWith("protocol:") -> {
                                val parts = targetKey.split(":")
                                val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                                val targetProtocolId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                                viewModel.moveModuleToProtocol(
                                    projectId = targetProjectId,
                                    moduleId = seed.node.entityId,
                                    protocolId = targetProtocolId,
                                    sortIndex = sortIndex,
                                )
                            }
                        }
                    }

                    HostConfigNodeKind.DEVICE -> {
                        val parts = targetKey.split(":")
                        val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                        val targetModuleId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveDevice(
                            projectId = targetProjectId,
                            deviceId = seed.node.entityId,
                            moduleId = targetModuleId,
                            sortIndex = sortIndex,
                        )
                    }

                    HostConfigNodeKind.TAG -> {
                        val parts = targetKey.split(":")
                        val targetProjectId = parts.getOrNull(1)?.toLongOrNull() ?: return@MoveNodeDialog
                        val targetDeviceId = parts.getOrNull(2)?.toLongOrNull() ?: return@MoveNodeDialog
                        viewModel.moveTag(
                            projectId = targetProjectId,
                            deviceId = targetDeviceId,
                            tagId = seed.node.entityId,
                            sortIndex = sortIndex,
                        )
                    }

                    HostConfigNodeKind.PROJECT -> Unit
                }
                moveSeed = null
            },
        )
    }

    uploadSeed?.let { _ ->
        UploadProjectDialog(
            state = state,
            saving = state.busy,
            onDismissRequest = {
                uploadSeed = null
            },
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
                uploadSeed = null
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

@Composable
private fun CurrentNodePanel(
    state: ProjectsScreenState,
    onSelectNode: (String) -> Unit,
) {
    val nodeKind = resolveSelectedNodeKind(state)
    HostConfigPanel(
        title = "当前节点信息",
        subtitle = when (nodeKind) {
            null -> "请选择左侧节点"
            else -> "${nodeKind.label()}表单"
        },
    ) {
        if (nodeKind == null) {
            HostConfigStatusStrip("当前还没有可展示的工程节点，先点击左侧的新建。")
            return@HostConfigPanel
        }

        when (nodeKind) {
            HostConfigNodeKind.PROJECT -> {
                val project = state.selectedProject
                val projectTree = state.selectedProjectTree
                val modules = projectTree?.allModules().orEmpty()
                HostConfigSectionTitle("基础信息")
                HostConfigKeyValueRow("工程名称", project?.name.orDash())
                HostConfigKeyValueRow("描述", project?.description.orDash())
                HostConfigKeyValueRow("备注", project?.remark.orDash())
                HostConfigKeyValueRow("排序", project?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("协议数量", projectTree?.protocols?.size?.toString() ?: "0")
                HostConfigKeyValueRow("模块数量", modules.size.toString())
                HostConfigKeyValueRow(
                    "设备总数",
                    projectTree?.let { tree ->
                        (
                            tree.allModules().sumOf { module -> module.devices.size }
                            ).toString()
                    } ?: "0",
                )
                HostConfigKeyValueRow(
                    "点位总数",
                    projectTree?.let { tree ->
                        (
                            tree.allModules().sumOf { module ->
                                module.devices.sumOf { device -> device.tags.size }
                            }
                            ).toString()
                    } ?: "0",
                )
                if (modules.isNotEmpty()) {
                    HostConfigSectionTitle("板卡总览")
                    ProjectModuleRack(
                        projectId = project?.id,
                        modules = modules,
                        moduleTemplates = state.moduleTemplates,
                        onSelectNode = onSelectNode,
                    )
                }

                HostConfigSectionTitle("上传状态")
                HostConfigKeyValueRow("当前状态", state.uploadStatus?.statusText ?: "待开始")
                HostConfigKeyValueRow("进度", state.uploadStatus?.progress?.let { "$it%" } ?: "0%")
                HostConfigKeyValueRow("目标 IP", state.uploadStatus?.ipAddress.orDash())
                HostConfigKeyValueRow("工程路径", state.uploadStatus?.projectPath.orDash())
                HostConfigKeyValueRow("已选文件", state.uploadStatus?.selectedFileName.orDash())
                HostConfigKeyValueRow("备份文件", state.uploadStatus?.backupFileName.orDash())
                HostConfigKeyValueRow("备份大小", state.uploadStatus?.backupSizeBytes.toSizeLabel())
                state.uploadStatus?.detailText?.takeIf { it.isNotBlank() }?.let { detail ->
                    HostConfigStatusStrip(detail)
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                val protocol = state.selectedProtocol
                HostConfigSectionTitle("基础信息")
                HostConfigKeyValueRow("协议名称", protocol?.name.orDash())
                HostConfigKeyValueRow("协议模板", protocol?.protocolTemplateName.orDash())
                HostConfigKeyValueRow("模板编码", protocol?.protocolTemplateCode.orDash())
                HostConfigKeyValueRow("轮询间隔(ms)", protocol?.pollingIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("排序", protocol?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("模块数量", protocol?.modules?.size?.toString() ?: "0")
                HostConfigSectionTitle("通信配置")
                renderTransportConfigRows(protocol?.transportConfig)
            }

            HostConfigNodeKind.MODULE -> {
                val module = state.selectedModule
                val moduleProtocol = state.selectedModuleProtocol
                module?.let { item ->
                    HostConfigSectionTitle("板卡模型")
                    HostConfigModuleBoard(
                        model = resolveModuleBoardModel(
                            module = item,
                            moduleTemplates = state.moduleTemplates,
                        ),
                    )
                }
                HostConfigSectionTitle("基础信息")
                HostConfigKeyValueRow("模块名称", module?.name.orDash())
                HostConfigKeyValueRow("模块模板", module?.moduleTemplateName.orDash())
                HostConfigKeyValueRow("模板编码", module?.moduleTemplateCode.orDash())
                HostConfigKeyValueRow("所属协议", moduleProtocol?.name.orDash())
                HostConfigKeyValueRow("排序", module?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("设备数量", module?.devices?.size?.toString() ?: "0")
                HostConfigSectionTitle("继承通信")
                renderTransportConfigRows(moduleProtocol?.transportConfig)
            }

            HostConfigNodeKind.DEVICE -> {
                val device = state.selectedDevice
                HostConfigSectionTitle("基础信息")
                HostConfigKeyValueRow("设备名称", device?.name.orDash())
                HostConfigKeyValueRow("设备类型", device?.deviceTypeName.orDash())
                HostConfigKeyValueRow("类型编码", device?.deviceTypeCode.orDash())
                HostConfigKeyValueRow("站号", device?.stationNo?.toString() ?: "-")
                HostConfigKeyValueRow("请求间隔(ms)", device?.requestIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("写值间隔(ms)", device?.writeIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("2 字节顺序", device?.byteOrder2?.label() ?: "-")
                HostConfigKeyValueRow("4 字节顺序", device?.byteOrder4?.label() ?: "-")
                HostConfigKeyValueRow("浮点顺序", device?.floatOrder?.label() ?: "-")
                HostConfigKeyValueRow("模拟量批量", listOf(device?.batchAnalogStart, device?.batchAnalogLength).joinToString(" / ") { it?.toString() ?: "-" })
                HostConfigKeyValueRow("数字量批量", listOf(device?.batchDigitalStart, device?.batchDigitalLength).joinToString(" / ") { it?.toString() ?: "-" })
                HostConfigKeyValueRow("禁用", if (device?.disabled == true) "是" else "否")
                HostConfigKeyValueRow("排序", device?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("点位数量", device?.tags?.size?.toString() ?: "0")
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                if (tag == null) {
                    HostConfigStatusStrip("当前点位详情尚未加载完成。")
                    return@HostConfigPanel
                }
                HostConfigSectionTitle("基础信息")
                HostConfigKeyValueRow("点位名称", tag.name)
                HostConfigKeyValueRow("描述", tag.description.orDash())
                HostConfigKeyValueRow("数据类型", tag.dataTypeName)
                HostConfigKeyValueRow("寄存器类型", tag.registerTypeName)
                HostConfigKeyValueRow("寄存器地址", tag.registerAddress.toString())
                HostConfigKeyValueRow("启用", if (tag.enabled) "是" else "否")
                HostConfigKeyValueRow("默认值", tag.defaultValue.orDash())
                HostConfigKeyValueRow("异常值", tag.exceptionValue.orDash())
                HostConfigKeyValueRow("点位类型", tag.pointType?.label() ?: "-")
                HostConfigKeyValueRow("防抖(ms)", tag.debounceMs?.toString() ?: "-")
                HostConfigKeyValueRow("排序", tag.sortIndex.toString())
                HostConfigKeyValueRow("线性转换", if (tag.scalingEnabled) "已启用" else "未启用")
                if (tag.scalingEnabled) {
                    HostConfigKeyValueRow("偏移量", tag.scalingOffset.orDash())
                    HostConfigKeyValueRow("原始范围", "${tag.rawMin.orDash()} ~ ${tag.rawMax.orDash()}")
                    HostConfigKeyValueRow("工程范围", "${tag.engMin.orDash()} ~ ${tag.engMax.orDash()}")
                }
                HostConfigKeyValueRow("值文本条目", tag.valueTexts.size.toString())
            }
        }
    }
}

@Composable
private fun NodeChildrenPanel(
    state: ProjectsScreenState,
    onSelectNode: (String) -> Unit,
    onPrevTagPage: () -> Unit,
    onNextTagPage: () -> Unit,
) {
    val nodeKind = resolveSelectedNodeKind(state)
    HostConfigPanel(
        title = if (nodeKind == HostConfigNodeKind.TAG) "子项信息" else "下级节点",
        subtitle = when (nodeKind) {
            null -> "请选择左侧节点"
            HostConfigNodeKind.PROJECT -> "当前工程下挂接的协议实例。"
            HostConfigNodeKind.PROTOCOL -> "当前协议下的模块。"
            HostConfigNodeKind.MODULE -> "当前模块下的设备。"
            HostConfigNodeKind.DEVICE -> "当前设备下的标签分页。"
            HostConfigNodeKind.TAG -> "当前标签携带的值文本子项。"
        },
        actions = {
            if (nodeKind == HostConfigNodeKind.DEVICE) {
                WorkbenchActionButton(
                    text = "上一页",
                    onClick = onPrevTagPage,
                    variant = WorkbenchButtonVariant.Outline,
                    enabled = state.tagOffset > 0,
                )
                WorkbenchActionButton(
                    text = "下一页",
                    onClick = onNextTagPage,
                    variant = WorkbenchButtonVariant.Outline,
                    enabled = state.tagOffset + state.tagSize < state.tagPage.t.toInt(),
                )
            }
        },
    ) {
        when (nodeKind) {
            null -> HostConfigStatusStrip("左侧树选择后，这里会显示当前节点的 children 信息。")

            HostConfigNodeKind.PROJECT -> {
                val projectTree = state.selectedProjectTree
                if (projectTree == null) {
                    HostConfigStatusStrip("当前工程树还没有加载完成。")
                    return@HostConfigPanel
                }
                if (projectTree.protocols.isEmpty()) {
                    HostConfigStatusStrip("当前工程还没有下级节点。")
                    return@HostConfigPanel
                }
                HostConfigSectionTitle("协议")
                projectTree.protocols.forEach { protocol ->
                    ChildNodeCard(
                        title = protocol.name,
                        subtitle = "${protocol.protocolTemplateName} · ${protocol.protocolTemplateCode}",
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildProtocolNodeId(projectTree.id, protocol.id),
                            )
                        },
                    ) {
                        HostConfigKeyValueRow("轮询间隔(ms)", protocol.pollingIntervalMs.toString())
                        HostConfigKeyValueRow("承载模块", protocol.modules.size.toString())
                        HostConfigKeyValueRow("排序", protocol.sortIndex.toString())
                        renderTransportConfigRows(protocol.transportConfig)
                    }
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                val projectId = state.selectedProjectId ?: return@HostConfigPanel
                val protocol = state.selectedProtocol
                if (protocol == null || protocol.modules.isEmpty()) {
                    HostConfigStatusStrip("当前协议还没有下级模块。")
                    return@HostConfigPanel
                }
                protocol.modules.forEach { module ->
                    ChildNodeCard(
                        title = module.name,
                        subtitle = "${module.moduleTemplateName} · ${module.moduleTemplateCode}",
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildModuleNodeId(projectId, module.id),
                            )
                        },
                    ) {
                        HostConfigModuleBoard(
                            model = resolveModuleBoardModel(
                                module = module,
                                moduleTemplates = state.moduleTemplates,
                            ),
                            compact = true,
                        )
                        HostConfigKeyValueRow("设备数量", module.devices.size.toString())
                        HostConfigKeyValueRow("排序", module.sortIndex.toString())
                    }
                }
            }

            HostConfigNodeKind.MODULE -> {
                val projectId = state.selectedProjectId ?: return@HostConfigPanel
                val module = state.selectedModule
                if (module == null || module.devices.isEmpty()) {
                    HostConfigStatusStrip("当前模块还没有下级设备。")
                    return@HostConfigPanel
                }
                module.devices.forEach { device ->
                    ChildNodeCard(
                        title = device.name,
                        subtitle = device.deviceTypeName,
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildDeviceNodeId(projectId, device.id),
                            )
                        },
                    ) {
                        HostConfigKeyValueRow("站号", device.stationNo.toString())
                        HostConfigKeyValueRow("点位数量", device.tags.size.toString())
                        HostConfigKeyValueRow("禁用", if (device.disabled) "是" else "否")
                    }
                }
            }

            HostConfigNodeKind.DEVICE -> {
                val projectId = state.selectedProjectId ?: return@HostConfigPanel
                val deviceId = state.activeDeviceId ?: return@HostConfigPanel
                HostConfigKeyValueRow("分页状态", "偏移 ${state.tagOffset} / 共 ${state.tagPage.t} 条")
                if (state.tagPage.d.isEmpty()) {
                    HostConfigStatusStrip("当前设备还没有标签。")
                    return@HostConfigPanel
                }
                state.tagPage.d.forEach { tag ->
                    ChildNodeCard(
                        title = tag.name,
                        subtitle = "${tag.registerTypeName} / ${tag.registerAddress}",
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildTagNodeId(
                                    projectId = projectId,
                                    deviceId = deviceId,
                                    tagId = tag.id,
                                ),
                            )
                        },
                    ) {
                        HostConfigKeyValueRow("数据类型", tag.dataTypeName)
                        HostConfigKeyValueRow("启用", if (tag.enabled) "是" else "否")
                        HostConfigKeyValueRow("值文本条目", tag.valueTexts.size.toString())
                    }
                }
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                if (tag == null) {
                    HostConfigStatusStrip("当前标签详情尚未加载完成。")
                    return@HostConfigPanel
                }
                if (tag.valueTexts.isEmpty()) {
                    HostConfigStatusStrip("当前标签没有值文本子项。")
                    return@HostConfigPanel
                }
                tag.valueTexts.forEach { item ->
                    ChildNodeCard(
                        title = item.displayText,
                        subtitle = "排序 ${item.sortIndex}",
                    ) {
                        HostConfigKeyValueRow("原始值", item.rawValue)
                        HostConfigKeyValueRow("显示文本", item.displayText)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildNodeCard(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit),
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (onClick == null) {
                    modifier
                } else {
                    modifier.clickable(onClick = onClick)
                }
            },
    ) {
        HostConfigPanel(
            title = title,
            subtitle = subtitle,
        ) {
            content()
        }
    }
}

@Composable
private fun ProjectModuleRack(
    projectId: Long?,
    modules: List<site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode>,
    moduleTemplates: List<ModuleTemplateOptionResponse>,
    onSelectNode: (String) -> Unit,
) {
    val rackShape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rackShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
                        CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.35f),
                shape = rackShape,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CupertinoText(
                text = "远程 I/O 背板",
                style = CupertinoTheme.typography.headline,
            )
            CupertinoText(
                text = "${modules.size} 个插槽",
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF313A46),
                            Color(0xFF1F262F),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Black.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f),
                                ),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    modules.forEachIndexed { index, module ->
                        Column(
                            modifier = Modifier.width(292.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CupertinoText(
                                    text = "SLOT ${index + 1}",
                                    style = CupertinoTheme.typography.caption1,
                                    color = Color.White.copy(alpha = 0.76f),
                                )
                                CupertinoText(
                                    text = "${module.devices.size} 台设备",
                                    style = CupertinoTheme.typography.caption2,
                                    color = Color.White.copy(alpha = 0.50f),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.05f),
                                                Color.Black.copy(alpha = 0.12f),
                                            ),
                                        ),
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .padding(8.dp)
                                    .clickable(
                                        enabled = projectId != null,
                                        onClick = {
                                            projectId?.let { safeProjectId ->
                                                onSelectNode(
                                                    ProjectsViewModel.buildModuleNodeId(
                                                        projectId = safeProjectId,
                                                        moduleId = module.id,
                                                    ),
                                                )
                                            }
                                        },
                                    ),
                            ) {
                                HostConfigModuleBoard(
                                    model = resolveModuleBoardModel(
                                        module = module,
                                        moduleTemplates = moduleTemplates,
                                    ),
                                    compact = true,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.28f),
                                                Color.White.copy(alpha = 0.06f),
                                                Color.Black.copy(alpha = 0.18f),
                                            ),
                                        ),
                                    ),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.28f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.22f),
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ProjectEditorDialog(
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectDraft) -> Unit,
    onUpdateSort: (Int) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }
    var remark by remember(existing) { mutableStateOf(existing?.remark.orEmpty()) }
    var sortIndex by remember(existing) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

    HostConfigDialog(
        title = if (existing == null) "新建工程" else "编辑工程",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ProjectDraft(
                            name = name,
                            description = description,
                            remark = remark,
                            sortIndex = sortIndex,
                        ),
                    )
                },
                enabled = !saving && name.isNotBlank(),
            )
        },
    ) {
        HostConfigTextField("工程名称", name, { name = it })
        HostConfigTextField("工程描述", description, { description = it }, singleLine = false)
        HostConfigTextField("备注", remark, { remark = it }, singleLine = false)
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
        existing?.let {
            WorkbenchActionButton(
                text = "仅更新排序",
                onClick = {
                    onUpdateSort(sortIndex.toIntOrNull() ?: 0)
                },
                variant = WorkbenchButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun ProtocolEditorDialog(
    protocolTemplates: List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProtocolDraft) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var templateId by remember(existing, protocolTemplates) {
        mutableStateOf(existing?.protocolTemplateId ?: protocolTemplates.firstOrNull()?.id)
    }
    var pollingIntervalMs by remember(existing) { mutableStateOf(existing?.pollingIntervalMs?.toString() ?: "1000") }
    var sortIndex by remember(existing) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }
    var host by remember(existing) { mutableStateOf(existing?.transportConfig?.host.orEmpty()) }
    var tcpPort by remember(existing) { mutableStateOf(existing?.transportConfig?.tcpPort?.toString().orEmpty()) }
    var portName by remember(existing) { mutableStateOf(existing?.transportConfig?.portName.orEmpty()) }
    var baudRate by remember(existing) { mutableStateOf(existing?.transportConfig?.baudRate?.toString() ?: "9600") }
    var dataBits by remember(existing) { mutableStateOf(existing?.transportConfig?.dataBits?.toString() ?: "8") }
    var stopBits by remember(existing) { mutableStateOf(existing?.transportConfig?.stopBits?.toString() ?: "1") }
    var parity by remember(existing) { mutableStateOf(existing?.transportConfig?.parity ?: Parity.NONE) }
    var responseTimeoutMs by remember(existing) { mutableStateOf(existing?.transportConfig?.responseTimeoutMs?.toString() ?: "1000") }
    val templateCode = protocolTemplates.firstOrNull { item -> item.id == templateId }?.code

    HostConfigDialog(
        title = if (existing == null) "新建协议" else "编辑协议",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ProtocolDraft(
                            name = name,
                            protocolTemplateId = templateId,
                            pollingIntervalMs = pollingIntervalMs,
                            sortIndex = sortIndex,
                            host = host,
                            tcpPort = tcpPort,
                            portName = portName,
                            baudRate = baudRate,
                            dataBits = dataBits,
                            stopBits = stopBits,
                            parity = parity,
                            responseTimeoutMs = responseTimeoutMs,
                        ),
                    )
                },
                enabled = !saving && name.isNotBlank() && templateId != null,
            )
        },
    ) {
        HostConfigTextField("协议名称", name, { name = it })
        HostConfigSelectionField(
            label = "协议模板",
            options = protocolTemplates.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            selectedValue = templateId,
            onSelected = { templateId = it },
        )
        HostConfigTextField("轮询时间(ms)", pollingIntervalMs, { pollingIntervalMs = it })
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
        HostConfigPanel(
            title = "通信配置",
            subtitle = "模块通信参数已经上提到协议层，模块表单只保留硬件语义。",
        ) {
            when (templateCode) {
                "MODBUS_RTU_CLIENT" -> {
                    HostConfigTextField("串口", portName, { portName = it }, placeholder = "例如 COM3")
                    HostConfigTextField("波特率", baudRate, { baudRate = it })
                    HostConfigTextField("数据位", dataBits, { dataBits = it })
                    HostConfigTextField("停止位", stopBits, { stopBits = it })
                    HostConfigSelectionField(
                        label = "校验位",
                        options = Parity.entries.map { option -> HostConfigOption(option, option.label()) },
                        selectedValue = parity,
                        onSelected = { selected -> parity = selected ?: Parity.NONE },
                    )
                    HostConfigTextField("响应超时(ms)", responseTimeoutMs, { responseTimeoutMs = it })
                }

                "MODBUS_TCP_CLIENT" -> {
                    HostConfigTextField("主机地址", host, { host = it }, placeholder = "例如 192.168.1.10")
                    HostConfigTextField("TCP 端口", tcpPort, { tcpPort = it }, placeholder = "默认 502")
                    HostConfigTextField("响应超时(ms)", responseTimeoutMs, { responseTimeoutMs = it })
                }

                else -> {
                    HostConfigStatusStrip("当前协议模板没有额外通信字段。")
                }
            }
        }
    }
}

@Composable
private fun LinkProtocolDialog(
    options: List<HostConfigOption<Long>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long, Int) -> Unit,
) {
    var selectedId by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }

    HostConfigDialog(
        title = "关联已有协议",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "关联中" else "关联",
                onClick = {
                    selectedId?.let { protocolId ->
                        onSave(protocolId, sortIndex.toIntOrNull() ?: 0)
                    }
                },
                enabled = !saving && selectedId != null,
            )
        },
    ) {
        if (options.isEmpty()) {
            HostConfigStatusStrip("当前没有可关联的协议资产。")
        } else {
            HostConfigSelectionField(
                label = "可关联协议",
                options = options,
                selectedValue = selectedId,
                onSelected = { selectedId = it },
            )
            HostConfigTextField("排序", sortIndex, { sortIndex = it })
        }
    }
}

@Composable
private fun ModuleEditorDialog(
    protocolName: String,
    protocolTemplateName: String,
    templates: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ModuleDraft) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var moduleTemplateId by remember(existing, templates) {
        mutableStateOf(existing?.moduleTemplateId ?: templates.firstOrNull()?.value)
    }
    var sortIndex by remember(existing) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

    HostConfigDialog(
        title = if (existing == null) "新建模块" else "编辑模块",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ModuleDraft(
                            name = name,
                            moduleTemplateId = moduleTemplateId,
                            sortIndex = sortIndex,
                        ),
                    )
                },
                enabled = !saving && name.isNotBlank() && moduleTemplateId != null,
            )
        },
    ) {
        if (templates.isEmpty()) {
            HostConfigStatusStrip("当前协议模板下没有可用模块模板。")
        } else {
            HostConfigPanel(
                title = "绑定协议",
                subtitle = "模块创建后会挂到这个协议实例下。",
            ) {
                HostConfigKeyValueRow("协议名称", protocolName.orDash())
                HostConfigKeyValueRow("协议模板", protocolTemplateName.orDash())
            }
            HostConfigTextField("模块名称", name, { name = it })
            HostConfigSelectionField("模块模板", templates, moduleTemplateId, { moduleTemplateId = it })
            HostConfigTextField("排序", sortIndex, { sortIndex = it })
            HostConfigPanel(
                title = "提示",
                subtitle = "模块通信参数已提升到协议层，这里只维护模块模板和排序。",
            ) {
                HostConfigStatusStrip("如果要改串口、波特率、校验位和超时，请编辑所属协议。")
            }
        }
    }
}

@Composable
private fun ModuleProtocolPickerDialog(
    seed: ModuleProtocolPickerSeed,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var selectedProtocolId by remember(seed) {
        mutableStateOf(seed.candidates.firstOrNull()?.protocolId)
    }

    HostConfigDialog(
        title = "选择承载协议",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "处理中" else "下一步",
                onClick = {
                    selectedProtocolId?.let(onSave)
                },
                enabled = !saving && selectedProtocolId != null,
            )
        },
    ) {
        HostConfigStatusStrip("工程 ${seed.projectName.ifBlank { "当前工程" }} 下有多个可承载协议，请先明确模块归属。")
        HostConfigSelectionField(
            label = "目标协议",
            options = seed.candidates.map { candidate ->
                HostConfigOption(
                    value = candidate.protocolId,
                    label = candidate.protocolName,
                    caption = candidate.protocolTemplateName,
                )
            },
            selectedValue = selectedProtocolId,
            onSelected = { selectedProtocolId = it },
        )
        seed.candidates
            .firstOrNull { candidate -> candidate.protocolId == selectedProtocolId }
            ?.let { candidate ->
                HostConfigPanel(
                    title = "协议预览",
                    subtitle = "模块模板只会展示这个协议模板下允许的模块。",
                ) {
                    HostConfigKeyValueRow("协议模板", candidate.protocolTemplateName)
                    HostConfigKeyValueRow("模块模板数", candidate.availableTemplateCount.toString())
                    candidate.transportSummary?.let { summary ->
                        HostConfigKeyValueRow("通信摘要", summary)
                    }
                }
            }
    }
}

@Composable
private fun DeviceEditorDialog(
    deviceTypes: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (DeviceDraft) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var deviceTypeId by remember(existing, deviceTypes) {
        mutableStateOf(existing?.deviceTypeId ?: deviceTypes.firstOrNull()?.value)
    }
    var stationNo by remember(existing) { mutableStateOf(existing?.stationNo?.toString() ?: "1") }
    var requestIntervalMs by remember(existing) { mutableStateOf(existing?.requestIntervalMs?.toString().orEmpty()) }
    var writeIntervalMs by remember(existing) { mutableStateOf(existing?.writeIntervalMs?.toString().orEmpty()) }
    var byteOrder2 by remember(existing) { mutableStateOf(existing?.byteOrder2) }
    var byteOrder4 by remember(existing) { mutableStateOf(existing?.byteOrder4) }
    var floatOrder by remember(existing) { mutableStateOf(existing?.floatOrder) }
    var batchAnalogStart by remember(existing) { mutableStateOf(existing?.batchAnalogStart?.toString().orEmpty()) }
    var batchAnalogLength by remember(existing) { mutableStateOf(existing?.batchAnalogLength?.toString().orEmpty()) }
    var batchDigitalStart by remember(existing) { mutableStateOf(existing?.batchDigitalStart?.toString().orEmpty()) }
    var batchDigitalLength by remember(existing) { mutableStateOf(existing?.batchDigitalLength?.toString().orEmpty()) }
    var disabled by remember(existing) { mutableStateOf(existing?.disabled ?: false) }
    var sortIndex by remember(existing) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

    HostConfigDialog(
        title = if (existing == null) "新建设备" else "编辑设备",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        DeviceDraft(
                            name = name,
                            deviceTypeId = deviceTypeId,
                            stationNo = stationNo,
                            requestIntervalMs = requestIntervalMs,
                            writeIntervalMs = writeIntervalMs,
                            byteOrder2 = byteOrder2,
                            byteOrder4 = byteOrder4,
                            floatOrder = floatOrder,
                            batchAnalogStart = batchAnalogStart,
                            batchAnalogLength = batchAnalogLength,
                            batchDigitalStart = batchDigitalStart,
                            batchDigitalLength = batchDigitalLength,
                            disabled = disabled,
                            sortIndex = sortIndex,
                        ),
                    )
                },
                enabled = !saving && name.isNotBlank() && deviceTypeId != null,
            )
        },
    ) {
        HostConfigTextField("设备名称", name, { name = it })
        HostConfigSelectionField("设备类型", deviceTypes, deviceTypeId, { deviceTypeId = it })
        HostConfigTextField("站号", stationNo, { stationNo = it })
        HostConfigTextField("请求间隔(ms)", requestIntervalMs, { requestIntervalMs = it })
        HostConfigTextField("写值间隔(ms)", writeIntervalMs, { writeIntervalMs = it })
        HostConfigSelectionField(
            label = "2 字节顺序",
            options = ByteOrder2.entries.map { option -> HostConfigOption(option, option.label()) },
            selectedValue = byteOrder2,
            onSelected = { byteOrder2 = it },
            allowClear = true,
        )
        HostConfigSelectionField(
            label = "4 字节顺序",
            options = ByteOrder4.entries.map { option -> HostConfigOption(option, option.label()) },
            selectedValue = byteOrder4,
            onSelected = { byteOrder4 = it },
            allowClear = true,
        )
        HostConfigSelectionField(
            label = "浮点顺序",
            options = FloatOrder.entries.map { option -> HostConfigOption(option, option.label()) },
            selectedValue = floatOrder,
            onSelected = { floatOrder = it },
            allowClear = true,
        )
        HostConfigTextField("模拟量起点", batchAnalogStart, { batchAnalogStart = it })
        HostConfigTextField("模拟量长度", batchAnalogLength, { batchAnalogLength = it })
        HostConfigTextField("数字量起点", batchDigitalStart, { batchDigitalStart = it })
        HostConfigTextField("数字量长度", batchDigitalLength, { batchDigitalLength = it })
        HostConfigBooleanField("禁用设备", disabled, { disabled = it })
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
    }
}

@Composable
private fun TagEditorDialog(
    dataTypes: List<HostConfigOption<Long>>,
    registerTypes: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (TagDraft) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }
    var dataTypeId by remember(existing, dataTypes) {
        mutableStateOf(existing?.dataTypeId ?: dataTypes.firstOrNull()?.value)
    }
    var registerTypeId by remember(existing, registerTypes) {
        mutableStateOf(existing?.registerTypeId ?: registerTypes.firstOrNull()?.value)
    }
    var registerAddress by remember(existing) { mutableStateOf(existing?.registerAddress?.toString() ?: "1") }
    var enabled by remember(existing) { mutableStateOf(existing?.enabled ?: true) }
    var defaultValue by remember(existing) { mutableStateOf(existing?.defaultValue.orEmpty()) }
    var exceptionValue by remember(existing) { mutableStateOf(existing?.exceptionValue.orEmpty()) }
    var pointType by remember(existing) { mutableStateOf(existing?.pointType) }
    var debounceMs by remember(existing) { mutableStateOf(existing?.debounceMs?.toString().orEmpty()) }
    var sortIndex by remember(existing) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }
    var scalingEnabled by remember(existing) { mutableStateOf(existing?.scalingEnabled ?: false) }
    var scalingOffset by remember(existing) { mutableStateOf(existing?.scalingOffset.orEmpty()) }
    var rawMin by remember(existing) { mutableStateOf(existing?.rawMin.orEmpty()) }
    var rawMax by remember(existing) { mutableStateOf(existing?.rawMax.orEmpty()) }
    var engMin by remember(existing) { mutableStateOf(existing?.engMin.orEmpty()) }
    var engMax by remember(existing) { mutableStateOf(existing?.engMax.orEmpty()) }
    var valueTexts by remember(existing) {
        mutableStateOf(
            existing?.valueTexts?.map { item ->
                TagValueTextDraft(
                    rawValue = item.rawValue,
                    displayText = item.displayText,
                )
            } ?: listOf(
                TagValueTextDraft(rawValue = "0", displayText = "Off"),
                TagValueTextDraft(rawValue = "1", displayText = "On"),
            ),
        )
    }

    HostConfigDialog(
        title = if (existing == null) "新建点位" else "编辑点位",
        onDismissRequest = onDismissRequest,
        width = 860.dp,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        TagDraft(
                            name = name,
                            description = description,
                            dataTypeId = dataTypeId,
                            registerTypeId = registerTypeId,
                            registerAddress = registerAddress,
                            enabled = enabled,
                            defaultValue = defaultValue,
                            exceptionValue = exceptionValue,
                            pointType = pointType,
                            debounceMs = debounceMs,
                            sortIndex = sortIndex,
                            scalingEnabled = scalingEnabled,
                            scalingOffset = scalingOffset,
                            rawMin = rawMin,
                            rawMax = rawMax,
                            engMin = engMin,
                            engMax = engMax,
                            valueTexts = valueTexts,
                        ),
                    )
                },
                enabled = !saving && name.isNotBlank() && dataTypeId != null && registerTypeId != null,
            )
        },
    ) {
        HostConfigTextField("点位名称", name, { name = it })
        HostConfigTextField("描述", description, { description = it })
        HostConfigSelectionField("数据类型", dataTypes, dataTypeId, { dataTypeId = it })
        HostConfigSelectionField("寄存器类型", registerTypes, registerTypeId, { registerTypeId = it })
        HostConfigTextField("寄存器地址", registerAddress, { registerAddress = it })
        HostConfigBooleanField("启用点位", enabled, { enabled = it })
        HostConfigTextField("默认值", defaultValue, { defaultValue = it })
        HostConfigTextField("异常值", exceptionValue, { exceptionValue = it })
        HostConfigSelectionField(
            label = "点位类型",
            options = PointType.entries.map { option -> HostConfigOption(option, option.label()) },
            selectedValue = pointType,
            onSelected = { pointType = it },
            allowClear = true,
        )
        HostConfigTextField("防抖时间", debounceMs, { debounceMs = it })
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
        HostConfigBooleanField("启用线性转换", scalingEnabled, { scalingEnabled = it })
        if (scalingEnabled) {
            HostConfigTextField("偏移量", scalingOffset, { scalingOffset = it })
            HostConfigTextField("原始最小值", rawMin, { rawMin = it })
            HostConfigTextField("原始最大值", rawMax, { rawMax = it })
            HostConfigTextField("工程最小值", engMin, { engMin = it })
            HostConfigTextField("工程最大值", engMax, { engMax = it })
        }

        HostConfigPanel(
            title = "数字量文本",
            subtitle = "对应旧宿主配置里的值文本映射。",
        ) {
            valueTexts.forEachIndexed { index, item ->
                HostConfigPanel(
                    title = "映射 ${index + 1}",
                ) {
                    HostConfigTextField(
                        label = "原始值",
                        value = item.rawValue,
                        onValueChange = { text ->
                            valueTexts = valueTexts.toMutableList().apply {
                                this[index] = this[index].copy(rawValue = text)
                            }
                        },
                    )
                    HostConfigTextField(
                        label = "显示文本",
                        value = item.displayText,
                        onValueChange = { text ->
                            valueTexts = valueTexts.toMutableList().apply {
                                this[index] = this[index].copy(displayText = text)
                            }
                        },
                    )
                    WorkbenchActionButton(
                        text = "删除映射",
                        onClick = {
                            valueTexts = valueTexts.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                        variant = WorkbenchButtonVariant.Destructive,
                    )
                }
            }
            WorkbenchActionButton(
                text = "新增一行",
                onClick = {
                    valueTexts = valueTexts + TagValueTextDraft("", "")
                },
                variant = WorkbenchButtonVariant.Outline,
            )
        }
    }
}

@Composable
private fun MoveNodeDialog(
    nodeKind: HostConfigNodeKind,
    options: List<HostConfigOption<String>>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var targetKey by remember(options) { mutableStateOf(options.firstOrNull()?.value) }
    var sortIndex by remember { mutableStateOf("0") }

    HostConfigDialog(
        title = "变更${nodeKind.label()}上级",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    targetKey?.let { target ->
                        onSave(target, sortIndex.toIntOrNull() ?: 0)
                    }
                },
                enabled = !saving && targetKey != null,
            )
        },
    ) {
        HostConfigSelectionField(
            label = "目标上级",
            options = options,
            selectedValue = targetKey,
            onSelected = { targetKey = it },
        )
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
    }
}

@Composable
private fun UploadProjectDialog(
    state: ProjectsScreenState,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSubmit: (UploadDraft) -> Unit,
    onTriggerAction: (ProjectUploadRemoteAction, String) -> Unit,
) {
    val uploadStatus = state.uploadStatus
    var ipAddress by remember(uploadStatus) { mutableStateOf(uploadStatus?.ipAddress.orEmpty()) }
    var projectPath by remember(uploadStatus) { mutableStateOf(uploadStatus?.projectPath.orEmpty()) }
    var selectedFileName by remember(uploadStatus) { mutableStateOf(uploadStatus?.selectedFileName.orEmpty()) }
    var includeDriverConfig by remember(uploadStatus) { mutableStateOf(uploadStatus?.includeDriverConfig ?: true) }
    var includeFirmwareUpgrade by remember(uploadStatus) { mutableStateOf(uploadStatus?.includeFirmwareUpgrade ?: false) }
    var fastMode by remember(uploadStatus) { mutableStateOf(uploadStatus?.fastMode ?: false) }

    HostConfigDialog(
        title = "上传工程",
        onDismissRequest = onDismissRequest,
        width = 920.dp,
        actions = {
            WorkbenchActionButton("取消", onDismissRequest, variant = WorkbenchButtonVariant.Outline)
            WorkbenchActionButton(
                text = if (saving) "提交中" else "提交上传",
                onClick = {
                    onSubmit(
                        UploadDraft(
                            ipAddress = ipAddress,
                            includeDriverConfig = includeDriverConfig,
                            includeFirmwareUpgrade = includeFirmwareUpgrade,
                            projectPath = projectPath,
                            selectedFileName = selectedFileName,
                            fastMode = fastMode,
                        ),
                    )
                },
                enabled = !saving && ipAddress.isNotBlank(),
            )
        },
    ) {
        HostConfigPanel(
            title = "进度",
            subtitle = uploadStatus?.statusText ?: "待开始",
        ) {
            HostConfigKeyValueRow("进度", uploadStatus?.progress?.let { "$it%" } ?: "0%")
            HostConfigKeyValueRow("详情", uploadStatus?.detailText.orDash())
            HostConfigKeyValueRow("备份文件", uploadStatus?.backupFileName.orDash())
            HostConfigKeyValueRow("下载地址", uploadStatus?.backupDownloadUrl.orDash())
        }
        HostConfigTextField("IP 地址", ipAddress, { ipAddress = it }, placeholder = "请输入目标设备 IP")
        HostConfigTextField("工程路径", projectPath, { projectPath = it })
        HostConfigTextField("已选文件名", selectedFileName, { selectedFileName = it })
        HostConfigBooleanField("上传驱动配置", includeDriverConfig, { includeDriverConfig = it })
        HostConfigBooleanField("上传固件升级", includeFirmwareUpgrade, { includeFirmwareUpgrade = it })
        HostConfigBooleanField("极速模式", fastMode, { fastMode = it })
        HostConfigPanel(
            title = "远程动作",
            subtitle = "备份、还原、删除与远程重启会保留最近一次动作状态。",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    ProjectUploadRemoteAction.BACKUP to "备份配置工程",
                    ProjectUploadRemoteAction.RESTORE to "还原配置工程",
                    ProjectUploadRemoteAction.DELETE to "删除配置工程",
                    ProjectUploadRemoteAction.RESTART to "远程重启",
                ).forEach { (action, label) ->
                    WorkbenchActionButton(
                        text = label,
                        onClick = {
                            onTriggerAction(action, ipAddress)
                        },
                        variant = WorkbenchButtonVariant.Outline,
                        enabled = ipAddress.isNotBlank() && !saving,
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeActionSheet(
    seed: NodeActionMenuSeed,
    onDismissRequest: () -> Unit,
    onAction: (NodeActionType) -> Unit,
) {
    CupertinoActionSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CupertinoText(seed.title)
                seed.subtitle.takeIf { subtitle -> subtitle.isNotBlank() }?.let { subtitle ->
                    CupertinoText(
                        text = subtitle,
                        style = CupertinoTheme.typography.footnote,
                        color = CupertinoTheme.colorScheme.secondaryLabel,
                    )
                }
            }
        },
        buttons = {
            seed.items.forEach { item ->
                val buttonBody: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CupertinoText(item.title)
                        item.note?.takeIf { note -> note.isNotBlank() }?.let { note ->
                            CupertinoText(
                                text = note,
                                style = CupertinoTheme.typography.footnote,
                                color = CupertinoTheme.colorScheme.secondaryLabel,
                            )
                        }
                    }
                }
                if (item.destructive) {
                    destructive(
                        onClick = {
                            if (item.enabled) {
                                onAction(item.type)
                            }
                        },
                    ) {
                        buttonBody()
                    }
                } else {
                    default(
                        onClick = {
                            if (item.enabled) {
                                onAction(item.type)
                            }
                        },
                    ) {
                        buttonBody()
                    }
                }
            }
            cancel(
                onClick = onDismissRequest,
            ) {
                CupertinoText("取消")
            }
        },
    )
}

@Composable
private fun renderTransportConfigRows(
    transportConfig: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig?,
) {
    if (transportConfig == null) {
        HostConfigStatusStrip("当前没有通信参数。")
        return
    }
    transportConfig.toDisplayRows().forEach { (label, value) ->
        HostConfigKeyValueRow(label, value)
    }
}

private fun site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig.toDisplayRows():
    List<Pair<String, String>> {
    return when (transportType) {
        site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.RTU -> listOf(
            "传输类型" to transportType.label(),
            "串口" to portName.orDash(),
            "波特率" to (baudRate?.toString() ?: "-"),
            "数据位" to (dataBits?.toString() ?: "-"),
            "停止位" to (stopBits?.toString() ?: "-"),
            "校验位" to (parity?.label() ?: "-"),
            "响应超时(ms)" to (responseTimeoutMs?.toString() ?: "-"),
        )

        site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.TCP -> listOf(
            "传输类型" to transportType.label(),
            "主机地址" to host.orDash(),
            "TCP 端口" to (tcpPort?.toString() ?: "-"),
            "响应超时(ms)" to (responseTimeoutMs?.toString() ?: "-"),
        )
    }
}

private fun site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig.toSummary(): String {
    return when (transportType) {
        site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.RTU -> {
            listOf(
                portName.orDash(),
                baudRate?.let { value -> "${value}bps" } ?: "-",
                parity?.label() ?: "-",
            ).joinToString(" / ")
        }

        site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.TCP -> {
            listOf(
                host.orDash(),
                tcpPort?.toString() ?: "-",
            ).joinToString(":")
        }
    }
}

private fun resolveModuleTemplatesForProtocol(
    state: ProjectsScreenState,
    protocolTemplateId: Long,
): List<ModuleTemplateOptionResponse> {
    return state.moduleTemplateCatalog[protocolTemplateId].orEmpty()
}

private fun resolveLinkableProtocols(
    state: ProjectsScreenState,
    projectId: Long,
): List<site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse> {
    val linkedIds = state.projectTrees
        .firstOrNull { project -> project.id == projectId }
        ?.protocols
        ?.map { protocol -> protocol.id }
        ?.toSet()
        .orEmpty()
    return state.protocolCatalog.filter { protocol ->
        protocol.id !in linkedIds
    }
}

private fun resolveModuleProtocolCandidates(
    state: ProjectsScreenState,
    projectId: Long,
): List<ModuleProtocolCandidate> {
    val projectTree = state.projectTrees.firstOrNull { project -> project.id == projectId } ?: return emptyList()
    return projectTree.protocols.mapNotNull { protocol ->
        val availableTemplates = resolveModuleTemplatesForProtocol(
            state = state,
            protocolTemplateId = protocol.protocolTemplateId,
        )
        if (availableTemplates.isEmpty()) {
            return@mapNotNull null
        }
        ModuleProtocolCandidate(
            protocolId = protocol.id,
            protocolName = protocol.name,
            protocolTemplateId = protocol.protocolTemplateId,
            protocolTemplateName = protocol.protocolTemplateName,
            availableTemplateCount = availableTemplates.size,
            transportSummary = protocol.transportConfig?.toSummary(),
        )
    }
}

private fun resolveMoveOptions(
    state: ProjectsScreenState,
    node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
): List<HostConfigOption<String>> {
    return when (node.kind) {
        HostConfigNodeKind.PROJECT -> emptyList()
        HostConfigNodeKind.PROTOCOL -> state.projects.map { project ->
            HostConfigOption(
                value = "project:${project.id}",
                label = project.name,
            )
        }

        HostConfigNodeKind.MODULE -> {
            val currentProtocol = state.projectTrees.findModule(node.entityId)
                ?.let { module -> state.projectTrees.findProtocol(module.protocolId) }
            state.projectTrees.flatMap { project ->
                project.protocols
                    .filter { protocol ->
                        currentProtocol == null || protocol.protocolTemplateId == currentProtocol.protocolTemplateId
                    }
                    .map { protocol ->
                        HostConfigOption(
                            value = "protocol:${project.id}:${protocol.id}",
                            label = "${project.name} / ${protocol.name}",
                            caption = protocol.protocolTemplateName,
                        )
                    }
            }
        }

        HostConfigNodeKind.DEVICE -> state.projectTrees.flatMap { project ->
            project.allModules().map { module ->
                HostConfigOption(
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
                            HostConfigOption(
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

private fun resolveCurrentCreateSpec(
    state: ProjectsScreenState,
): CurrentCreateSpec {
    val selectedNode = state.selectedNode ?: return CurrentCreateSpec(
        enabled = false,
        hint = "请先在左侧选择一个节点。",
    )
    return when (selectedNode.kind) {
        HostConfigNodeKind.PROJECT -> {
            val candidates = resolveModuleProtocolCandidates(state, selectedNode.projectId)
            CurrentCreateSpec(
                node = selectedNode,
                actionType = NodeActionType.CREATE_MODULE,
                enabled = candidates.isNotEmpty(),
                hint = if (candidates.isEmpty()) {
                    "当前工程还没有可承载模块的协议，请先关联协议。"
                } else {
                    null
                },
            )
        }

        HostConfigNodeKind.PROTOCOL -> {
            val protocol = state.projectTrees.findProtocol(selectedNode.entityId)
            val hasTemplates = protocol != null &&
                resolveModuleTemplatesForProtocol(state, protocol.protocolTemplateId).isNotEmpty()
            CurrentCreateSpec(
                node = selectedNode,
                actionType = NodeActionType.CREATE_MODULE,
                enabled = hasTemplates,
                hint = if (hasTemplates) {
                    null
                } else {
                    "当前协议模板下没有模块模板。"
                },
            )
        }

        HostConfigNodeKind.MODULE -> CurrentCreateSpec(
            node = selectedNode,
            actionType = NodeActionType.CREATE_DEVICE,
            enabled = true,
        )

        HostConfigNodeKind.DEVICE,
        HostConfigNodeKind.TAG,
        -> CurrentCreateSpec(
            node = selectedNode,
            actionType = NodeActionType.CREATE_TAG,
            enabled = state.activeDeviceId != null,
            hint = if (state.activeDeviceId == null) {
                "当前还没有可用的设备上下文。"
            } else {
                null
            },
        )
    }
}

private fun resolveNodeActionMenu(
    state: ProjectsScreenState,
    node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
): NodeActionMenuSeed {
    val items = when (node.kind) {
        HostConfigNodeKind.PROJECT -> {
            val moduleCandidates = resolveModuleProtocolCandidates(state, node.projectId)
            val linkableProtocols = resolveLinkableProtocols(state, node.projectId)
            listOf(
                NodeActionItem(
                    type = NodeActionType.CREATE_MODULE,
                    title = "新建模块",
                    enabled = moduleCandidates.isNotEmpty(),
                    note = if (moduleCandidates.isEmpty()) "请先关联协议" else null,
                ),
                NodeActionItem(
                    type = NodeActionType.CREATE_PROTOCOL,
                    title = "新建协议",
                ),
                NodeActionItem(
                    type = NodeActionType.LINK_PROTOCOL,
                    title = "关联协议",
                    enabled = linkableProtocols.isNotEmpty(),
                    note = if (linkableProtocols.isEmpty()) "当前没有可关联的协议资产" else null,
                ),
                NodeActionItem(
                    type = NodeActionType.EDIT,
                    title = "编辑",
                ),
                NodeActionItem(
                    type = NodeActionType.UPLOAD_PROJECT,
                    title = "上传工程",
                ),
                NodeActionItem(
                    type = NodeActionType.DELETE,
                    title = "删除",
                    destructive = true,
                ),
            )
        }

        HostConfigNodeKind.PROTOCOL -> {
            val protocol = state.projectTrees.findProtocol(node.entityId)
            val hasTemplates = protocol != null &&
                resolveModuleTemplatesForProtocol(state, protocol.protocolTemplateId).isNotEmpty()
            listOf(
                NodeActionItem(
                    type = NodeActionType.CREATE_MODULE,
                    title = "新建模块",
                    enabled = hasTemplates,
                    note = if (hasTemplates) null else "当前协议模板下没有模块模板",
                ),
                NodeActionItem(
                    type = NodeActionType.EDIT,
                    title = "编辑",
                ),
                NodeActionItem(
                    type = NodeActionType.MOVE,
                    title = "变更上级",
                ),
                NodeActionItem(
                    type = NodeActionType.DELETE,
                    title = "删除",
                    destructive = true,
                ),
            )
        }

        HostConfigNodeKind.MODULE -> listOf(
            NodeActionItem(
                type = NodeActionType.CREATE_DEVICE,
                title = "新建设备",
            ),
            NodeActionItem(
                type = NodeActionType.EDIT,
                title = "编辑",
            ),
            NodeActionItem(
                type = NodeActionType.MOVE,
                title = "变更上级",
            ),
            NodeActionItem(
                type = NodeActionType.DELETE,
                title = "删除",
                destructive = true,
            ),
        )

        HostConfigNodeKind.DEVICE -> listOf(
            NodeActionItem(
                type = NodeActionType.CREATE_TAG,
                title = "新建标签",
            ),
            NodeActionItem(
                type = NodeActionType.EDIT,
                title = "编辑",
            ),
            NodeActionItem(
                type = NodeActionType.MOVE,
                title = "变更上级",
            ),
            NodeActionItem(
                type = NodeActionType.DELETE,
                title = "删除",
                destructive = true,
            ),
        )

        HostConfigNodeKind.TAG -> listOf(
            NodeActionItem(
                type = NodeActionType.EDIT,
                title = "编辑",
            ),
            NodeActionItem(
                type = NodeActionType.MOVE,
                title = "变更上级",
            ),
            NodeActionItem(
                type = NodeActionType.DELETE,
                title = "删除",
                destructive = true,
            ),
        )
    }
    return NodeActionMenuSeed(
        node = node,
        title = "${node.kind.label()}操作",
        subtitle = node.label,
        items = items,
    )
}

private fun resolveSelectedNodeKind(
    state: ProjectsScreenState,
): HostConfigNodeKind? {
    return state.selectedNode?.kind
        ?: state.selectedProject?.let { HostConfigNodeKind.PROJECT }
}

private fun site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse?.allModules():
    List<site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode> {
    val project = this ?: return emptyList()
    return (project.modules + project.protocols.flatMap { protocol -> protocol.modules })
        .sortedWith(
            compareBy<
                site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
                > { module -> module.sortIndex }
                .thenBy { module -> module.name },
        )
}

private data class ProjectEditorSeed(
    val existingId: Long? = null,
)

private data class ProtocolEditorSeed(
    val projectId: Long,
    val existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode? = null,
)

private data class LinkProtocolSeed(
    val projectId: Long,
)

private data class ModuleEditorSeed(
    val projectId: Long,
    val protocolId: Long? = null,
    val existing: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode? = null,
    val availableTemplates: List<ModuleTemplateOptionResponse> = emptyList(),
    val protocolName: String = "",
    val protocolTemplateName: String = "",
)

private data class ModuleProtocolPickerSeed(
    val projectId: Long,
    val projectName: String,
    val candidates: List<ModuleProtocolCandidate>,
)

private data class ModuleProtocolCandidate(
    val protocolId: Long,
    val protocolName: String,
    val protocolTemplateId: Long,
    val protocolTemplateName: String,
    val availableTemplateCount: Int,
    val transportSummary: String?,
)

private data class DeviceEditorSeed(
    val projectId: Long,
    val moduleId: Long,
    val existing: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode? = null,
)

private data class TagEditorSeed(
    val projectId: Long,
    val deviceId: Long,
    val existing: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse? = null,
)

private data class MoveNodeSeed(
    val node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
)

private data class UploadSeed(
    val projectId: Long,
)

private enum class NodeActionType {
    CREATE_MODULE,
    CREATE_PROTOCOL,
    LINK_PROTOCOL,
    CREATE_DEVICE,
    CREATE_TAG,
    EDIT,
    MOVE,
    DELETE,
    UPLOAD_PROJECT,
}

private data class NodeActionMenuSeed(
    val node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
    val title: String,
    val subtitle: String,
    val items: List<NodeActionItem>,
)

private data class NodeActionItem(
    val type: NodeActionType,
    val title: String,
    val enabled: Boolean = true,
    val note: String? = null,
    val destructive: Boolean = false,
)

private data class CurrentCreateSpec(
    val node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode? = null,
    val actionType: NodeActionType? = null,
    val enabled: Boolean,
    val hint: String? = null,
)

private data class ProjectDraft(
    val name: String,
    val description: String,
    val remark: String,
    val sortIndex: String,
)

private data class ProtocolDraft(
    val name: String,
    val protocolTemplateId: Long?,
    val pollingIntervalMs: String,
    val sortIndex: String,
    val host: String,
    val tcpPort: String,
    val portName: String,
    val baudRate: String,
    val dataBits: String,
    val stopBits: String,
    val parity: Parity,
    val responseTimeoutMs: String,
)

private data class ModuleDraft(
    val name: String,
    val moduleTemplateId: Long?,
    val sortIndex: String,
)

private data class DeviceDraft(
    val name: String,
    val deviceTypeId: Long?,
    val stationNo: String,
    val requestIntervalMs: String,
    val writeIntervalMs: String,
    val byteOrder2: ByteOrder2?,
    val byteOrder4: ByteOrder4?,
    val floatOrder: FloatOrder?,
    val batchAnalogStart: String,
    val batchAnalogLength: String,
    val batchDigitalStart: String,
    val batchDigitalLength: String,
    val disabled: Boolean,
    val sortIndex: String,
)

private data class TagDraft(
    val name: String,
    val description: String,
    val dataTypeId: Long?,
    val registerTypeId: Long?,
    val registerAddress: String,
    val enabled: Boolean,
    val defaultValue: String,
    val exceptionValue: String,
    val pointType: PointType?,
    val debounceMs: String,
    val sortIndex: String,
    val scalingEnabled: Boolean,
    val scalingOffset: String,
    val rawMin: String,
    val rawMax: String,
    val engMin: String,
    val engMax: String,
    val valueTexts: List<TagValueTextDraft>,
)

private data class TagValueTextDraft(
    val rawValue: String,
    val displayText: String,
)

private data class UploadDraft(
    val ipAddress: String,
    val includeDriverConfig: Boolean,
    val includeFirmwareUpgrade: Boolean,
    val projectPath: String,
    val selectedFileName: String,
    val fastMode: Boolean,
)

private fun ProtocolDraft.toTransportConfig(
    templateCode: String,
): site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig? {
    return when (templateCode) {
        "MODBUS_RTU_CLIENT" -> site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig(
            transportType = site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.RTU,
            portName = portName.ifBlank { null },
            baudRate = baudRate.toIntOrNull(),
            dataBits = dataBits.toIntOrNull(),
            stopBits = stopBits.toIntOrNull(),
            parity = parity,
            responseTimeoutMs = responseTimeoutMs.toIntOrNull(),
        )

        "MODBUS_TCP_CLIENT" -> site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTransportConfig(
            transportType = site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType.TCP,
            host = host.ifBlank { null },
            tcpPort = tcpPort.toIntOrNull(),
            responseTimeoutMs = responseTimeoutMs.toIntOrNull(),
        )

        else -> null
    }
}

private fun List<site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode>.allBranchIds(): Set<Any> {
    return buildSet {
        fun collect(nodes: List<site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode>) {
            nodes.forEach { node ->
                if (node.children.isEmpty()) {
                    return@forEach
                }
                add(node.id)
                collect(node.children)
            }
        }

        collect(this@allBranchIds)
    }
}

private fun HostConfigNodeKind.label(): String {
    return when (this) {
        HostConfigNodeKind.PROJECT -> "工程"
        HostConfigNodeKind.PROTOCOL -> "协议"
        HostConfigNodeKind.MODULE -> "模块"
        HostConfigNodeKind.DEVICE -> "设备"
        HostConfigNodeKind.TAG -> "点位"
    }
}
