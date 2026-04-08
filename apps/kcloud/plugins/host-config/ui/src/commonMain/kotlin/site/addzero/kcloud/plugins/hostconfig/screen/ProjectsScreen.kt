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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
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
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
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
    var deviceEditor by remember { mutableStateOf<DeviceEditorSeed?>(null) }
    var tagEditor by remember { mutableStateOf<TagEditorSeed?>(null) }
    var moveSeed by remember { mutableStateOf<MoveNodeSeed?>(null) }
    var uploadSeed by remember { mutableStateOf<UploadSeed?>(null) }

    val childCreateSpec = resolveChildCreateSpec(state)

    fun openCreateAction(target: CreateTarget) {
        when (target) {
            CreateTarget.PROJECT -> {
                projectEditor = ProjectEditorSeed()
            }

            CreateTarget.MODULE -> {
                val projectId = state.selectedProjectId ?: return
                moduleEditor = ModuleEditorSeed(
                    projectId = projectId,
                    availableTemplates = resolveModuleTemplatesForSelection(state),
                )
            }

            CreateTarget.DEVICE -> {
                val projectId = state.selectedProjectId ?: return
                val module = state.selectedModule ?: return
                deviceEditor = DeviceEditorSeed(
                    projectId = projectId,
                    moduleId = module.id,
                )
            }

            CreateTarget.TAG -> {
                val projectId = state.selectedProjectId ?: return
                val deviceId = state.activeDeviceId ?: return
                tagEditor = TagEditorSeed(
                    projectId = projectId,
                    deviceId = deviceId,
                )
            }
        }
    }

    fun editCurrentSelection() {
        when (state.selectedNode?.kind) {
            HostConfigNodeKind.PROJECT, null -> {
                state.selectedProject?.let { project ->
                    projectEditor = ProjectEditorSeed(existingId = project.id)
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                protocolEditor = ProtocolEditorSeed(
                    projectId = state.selectedProjectId ?: return,
                    existing = state.selectedProtocol,
                )
            }

            HostConfigNodeKind.MODULE -> {
                moduleEditor = ModuleEditorSeed(
                    projectId = state.selectedProjectId ?: return,
                    protocolId = state.selectedModule?.protocolId,
                    existing = state.selectedModule,
                    availableTemplates = resolveModuleTemplatesForSelection(state),
                )
            }

            HostConfigNodeKind.DEVICE -> {
                val moduleId = state.selectedNode?.parentEntityId ?: return
                deviceEditor = DeviceEditorSeed(
                    projectId = state.selectedProjectId ?: return,
                    moduleId = moduleId,
                    existing = state.selectedDevice,
                )
            }

            HostConfigNodeKind.TAG -> {
                val deviceId = state.selectedNode?.parentEntityId ?: return
                tagEditor = TagEditorSeed(
                    projectId = state.selectedProjectId ?: return,
                    deviceId = deviceId,
                    existing = state.selectedTagDetail,
                )
            }
        }
    }

    fun deleteCurrentSelection() {
        when (state.selectedNode?.kind) {
            HostConfigNodeKind.PROJECT, null -> {
                state.selectedProject?.let { project ->
                    viewModel.deleteProject(project.id)
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                state.selectedProjectId?.let { projectId ->
                    state.selectedProtocol?.let { protocol ->
                        viewModel.deleteProtocol(projectId, protocol.id)
                    }
                }
            }

            HostConfigNodeKind.MODULE -> {
                state.selectedProjectId?.let { projectId ->
                    state.selectedModule?.let { module ->
                        viewModel.deleteModule(projectId, module.id)
                    }
                }
            }

            HostConfigNodeKind.DEVICE -> {
                state.selectedProjectId?.let { projectId ->
                    state.selectedDevice?.let { device ->
                        viewModel.deleteDevice(projectId, device.id)
                    }
                }
            }

            HostConfigNodeKind.TAG -> {
                state.selectedProjectId?.let { projectId ->
                    val deviceId = state.selectedNode?.parentEntityId ?: return@let
                    state.selectedTagDetail?.let { tag ->
                        viewModel.deleteTag(projectId, deviceId, tag.id)
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.treeNodes,
            selectedId = state.selectedNodeId,
            onNodeClick = { node ->
                viewModel.selectNode(node.id)
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.30f),
            searchPlaceholder = "搜索工程树",
            getId = { node -> node.id },
            getLabel = { node -> node.label },
            getChildren = { node -> node.children },
            getIcon = { node -> node.kind.icon() },
            header = {
                state.errorMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                state.noticeMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WorkbenchActionButton(
                        text = childCreateSpec.label,
                        onClick = {
                            viewModel.clearNotice()
                            childCreateSpec.target?.let(::openCreateAction)
                        },
                        variant = WorkbenchButtonVariant.Default,
                        enabled = childCreateSpec.enabled,
                    )
                    WorkbenchActionButton(
                        text = "新建工程",
                        onClick = {
                            viewModel.clearNotice()
                            openCreateAction(CreateTarget.PROJECT)
                        },
                        variant = WorkbenchButtonVariant.Outline,
                    )
                    WorkbenchActionButton(
                        text = if (state.loading) "加载中" else "刷新",
                        onClick = viewModel::refresh,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                }
            },
        )

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
                        onEditCurrent = ::editCurrentSelection,
                        onMoveCurrent = {
                            state.selectedNode
                                ?.takeIf { node -> node.kind != HostConfigNodeKind.PROJECT }
                                ?.let { node -> moveSeed = MoveNodeSeed(node) }
                        },
                        onDeleteCurrent = ::deleteCurrentSelection,
                        onUploadProject = {
                            state.selectedProjectId?.let { projectId ->
                                uploadSeed = UploadSeed(projectId)
                            }
                        },
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
            protocolTemplates = state.protocolTemplates.map { item ->
                HostConfigOption(
                    value = item.id,
                    label = item.name,
                    caption = item.description,
                )
            },
            existing = seed.existing,
            saving = state.busy,
            onDismissRequest = {
                protocolEditor = null
            },
            onSave = { draft ->
                val request = ProtocolCreateRequest(
                    name = draft.name,
                    protocolTemplateId = draft.protocolTemplateId ?: return@ProtocolEditorDialog,
                    pollingIntervalMs = draft.pollingIntervalMs.toIntOrNull() ?: 1000,
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
            options = resolveLinkableProtocols(state).map { item ->
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

    moduleEditor?.let { seed ->
        ModuleEditorDialog(
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
                    if (seed.protocolId != null) {
                        viewModel.createModuleUnderProtocol(
                            projectId = seed.projectId,
                            protocolId = seed.protocolId,
                            request = request,
                        )
                    } else {
                        viewModel.createModuleUnderProject(
                            projectId = seed.projectId,
                            request = request,
                        )
                    }
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

    uploadSeed?.let { seed ->
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
    onEditCurrent: () -> Unit,
    onMoveCurrent: () -> Unit,
    onDeleteCurrent: () -> Unit,
    onUploadProject: () -> Unit,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkbenchActionButton(
                text = "编辑当前",
                onClick = onEditCurrent,
                imageVector = Icons.Outlined.Edit,
                enabled = state.selectedProjectId != null,
            )
            WorkbenchActionButton(
                text = "变更上级",
                onClick = onMoveCurrent,
                imageVector = Icons.Outlined.MoveDown,
                variant = WorkbenchButtonVariant.Outline,
                enabled = state.selectedNode?.kind != null && state.selectedNode?.kind != HostConfigNodeKind.PROJECT,
            )
            WorkbenchActionButton(
                text = "删除当前",
                onClick = onDeleteCurrent,
                imageVector = Icons.Outlined.Delete,
                variant = WorkbenchButtonVariant.Destructive,
                enabled = state.selectedProjectId != null,
            )
            WorkbenchActionButton(
                text = "上传工程",
                onClick = onUploadProject,
                imageVector = Icons.Outlined.Upload,
                variant = WorkbenchButtonVariant.Secondary,
                enabled = nodeKind == HostConfigNodeKind.PROJECT && state.selectedProjectId != null,
            )
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
            }

            HostConfigNodeKind.MODULE -> {
                val module = state.selectedModule
                val moduleProtocol = state.selectedModuleProtocol
                val transportConfig = moduleProtocol?.transportConfig
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
                HostConfigKeyValueRow("通信串口", transportConfig?.portName.orDash())
                HostConfigKeyValueRow("波特率", transportConfig?.baudRate?.toString() ?: "-")
                HostConfigKeyValueRow("数据位", transportConfig?.dataBits?.toString() ?: "-")
                HostConfigKeyValueRow("停止位", transportConfig?.stopBits?.toString() ?: "-")
                HostConfigKeyValueRow("校验位", transportConfig?.parity?.label() ?: "-")
                HostConfigKeyValueRow("响应超时(ms)", transportConfig?.responseTimeoutMs?.toString() ?: "-")
                HostConfigKeyValueRow("排序", module?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("设备数量", module?.devices?.size?.toString() ?: "0")
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
            HostConfigNodeKind.PROJECT -> "当前工程直接挂载的协议和根模块。"
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
                val modules = projectTree.allModules()
                if (modules.isEmpty()) {
                    HostConfigStatusStrip("当前工程还没有下级节点。")
                    return@HostConfigPanel
                }
                HostConfigSectionTitle("模块")
                modules.forEach { module ->
                    val transportConfig = projectTree.protocols
                        .firstOrNull { protocol -> protocol.id == module.protocolId }
                        ?.transportConfig
                    ChildNodeCard(
                        title = module.name,
                        subtitle = "${module.moduleTemplateName} · ${module.moduleTemplateCode}",
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildModuleNodeId(projectTree.id, module.id),
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
                        HostConfigKeyValueRow("通信串口", transportConfig?.portName.orDash())
                        HostConfigKeyValueRow("设备数量", module.devices.size.toString())
                        HostConfigKeyValueRow("排序", module.sortIndex.toString())
                    }
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                HostConfigStatusStrip("协议节点已从主树工作台隐藏，请直接在工程下操作模块。")
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
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var remark by remember(existing?.id) { mutableStateOf(existing?.remark.orEmpty()) }
    var sortIndex by remember(existing?.id) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

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
    protocolTemplates: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProtocolDraft) -> Unit,
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var templateId by remember(existing?.id) { mutableStateOf(existing?.protocolTemplateId ?: protocolTemplates.firstOrNull()?.value) }
    var pollingIntervalMs by remember(existing?.id) { mutableStateOf(existing?.pollingIntervalMs?.toString() ?: "1000") }
    var sortIndex by remember(existing?.id) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

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
            options = protocolTemplates,
            selectedValue = templateId,
            onSelected = { templateId = it },
        )
        HostConfigTextField("轮询时间(ms)", pollingIntervalMs, { pollingIntervalMs = it })
        HostConfigTextField("排序", sortIndex, { sortIndex = it })
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
    templates: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ModuleDraft) -> Unit,
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var moduleTemplateId by remember(existing?.id) { mutableStateOf(existing?.moduleTemplateId ?: templates.firstOrNull()?.value) }
    var sortIndex by remember(existing?.id) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

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
            HostConfigStatusStrip("当前工程没有可用模块模板，请先关联协议。")
        } else {
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
private fun DeviceEditorDialog(
    deviceTypes: List<HostConfigOption<Long>>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode?,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (DeviceDraft) -> Unit,
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var deviceTypeId by remember(existing?.id) { mutableStateOf(existing?.deviceTypeId ?: deviceTypes.firstOrNull()?.value) }
    var stationNo by remember(existing?.id) { mutableStateOf(existing?.stationNo?.toString() ?: "1") }
    var requestIntervalMs by remember(existing?.id) { mutableStateOf(existing?.requestIntervalMs?.toString().orEmpty()) }
    var writeIntervalMs by remember(existing?.id) { mutableStateOf(existing?.writeIntervalMs?.toString().orEmpty()) }
    var byteOrder2 by remember(existing?.id) { mutableStateOf(existing?.byteOrder2) }
    var byteOrder4 by remember(existing?.id) { mutableStateOf(existing?.byteOrder4) }
    var floatOrder by remember(existing?.id) { mutableStateOf(existing?.floatOrder) }
    var batchAnalogStart by remember(existing?.id) { mutableStateOf(existing?.batchAnalogStart?.toString().orEmpty()) }
    var batchAnalogLength by remember(existing?.id) { mutableStateOf(existing?.batchAnalogLength?.toString().orEmpty()) }
    var batchDigitalStart by remember(existing?.id) { mutableStateOf(existing?.batchDigitalStart?.toString().orEmpty()) }
    var batchDigitalLength by remember(existing?.id) { mutableStateOf(existing?.batchDigitalLength?.toString().orEmpty()) }
    var disabled by remember(existing?.id) { mutableStateOf(existing?.disabled ?: false) }
    var sortIndex by remember(existing?.id) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }

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
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var dataTypeId by remember(existing?.id) { mutableStateOf(existing?.dataTypeId ?: dataTypes.firstOrNull()?.value) }
    var registerTypeId by remember(existing?.id) { mutableStateOf(existing?.registerTypeId ?: registerTypes.firstOrNull()?.value) }
    var registerAddress by remember(existing?.id) { mutableStateOf(existing?.registerAddress?.toString() ?: "1") }
    var enabled by remember(existing?.id) { mutableStateOf(existing?.enabled ?: true) }
    var defaultValue by remember(existing?.id) { mutableStateOf(existing?.defaultValue.orEmpty()) }
    var exceptionValue by remember(existing?.id) { mutableStateOf(existing?.exceptionValue.orEmpty()) }
    var pointType by remember(existing?.id) { mutableStateOf(existing?.pointType) }
    var debounceMs by remember(existing?.id) { mutableStateOf(existing?.debounceMs?.toString().orEmpty()) }
    var sortIndex by remember(existing?.id) { mutableStateOf(existing?.sortIndex?.toString() ?: "0") }
    var scalingEnabled by remember(existing?.id) { mutableStateOf(existing?.scalingEnabled ?: false) }
    var scalingOffset by remember(existing?.id) { mutableStateOf(existing?.scalingOffset.orEmpty()) }
    var rawMin by remember(existing?.id) { mutableStateOf(existing?.rawMin.orEmpty()) }
    var rawMax by remember(existing?.id) { mutableStateOf(existing?.rawMax.orEmpty()) }
    var engMin by remember(existing?.id) { mutableStateOf(existing?.engMin.orEmpty()) }
    var engMax by remember(existing?.id) { mutableStateOf(existing?.engMax.orEmpty()) }
    var valueTexts by remember(existing?.id) {
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
    var ipAddress by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.ipAddress.orEmpty()) }
    var projectPath by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.projectPath.orEmpty()) }
    var selectedFileName by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.selectedFileName.orEmpty()) }
    var includeDriverConfig by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.includeDriverConfig ?: true) }
    var includeFirmwareUpgrade by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.includeFirmwareUpgrade ?: false) }
    var fastMode by remember(uploadStatus?.projectId) { mutableStateOf(uploadStatus?.fastMode ?: false) }

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

private fun resolveModuleTemplatesForSelection(
    state: ProjectsScreenState,
): List<ModuleTemplateOptionResponse> {
    return state.moduleTemplates
}

private fun resolveLinkableProtocols(
    state: ProjectsScreenState,
): List<site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse> {
    val linkedIds = state.selectedProjectTree?.protocols?.map { protocol -> protocol.id }?.toSet().orEmpty()
    return state.protocolCatalog.filter { protocol ->
        protocol.id !in linkedIds
    }
}

private fun resolveMoveOptions(
    state: ProjectsScreenState,
    node: site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode,
): List<HostConfigOption<String>> {
    return when (node.kind) {
        HostConfigNodeKind.PROJECT -> emptyList()
        HostConfigNodeKind.PROTOCOL -> emptyList()
        HostConfigNodeKind.MODULE -> state.projects.map { project ->
            HostConfigOption(
                value = "project:${project.id}",
                label = project.name,
            )
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

private fun resolveChildCreateSpec(
    state: ProjectsScreenState,
): ChildCreateSpec {
    val nodeKind = resolveSelectedNodeKind(state)
    return when (nodeKind) {
        HostConfigNodeKind.PROJECT -> ChildCreateSpec(
            label = "新建模块",
            target = CreateTarget.MODULE,
            enabled = true,
        )
        HostConfigNodeKind.PROTOCOL -> ChildCreateSpec(
            label = "新建模块",
            target = CreateTarget.MODULE,
            enabled = true,
        )
        HostConfigNodeKind.MODULE -> ChildCreateSpec(
            label = "新建设备",
            target = CreateTarget.DEVICE,
            enabled = true,
        )
        HostConfigNodeKind.DEVICE,
        HostConfigNodeKind.TAG,
        -> ChildCreateSpec(
            label = "新建标签",
            target = CreateTarget.TAG,
            enabled = state.activeDeviceId != null,
        )
        null -> ChildCreateSpec(
            label = "选择节点后新建",
            target = null,
            enabled = false,
        )
    }
}

private fun resolveSelectedNodeKind(
    state: ProjectsScreenState,
): HostConfigNodeKind? {
    return state.selectedNode?.kind
        ?: state.selectedProject?.let { HostConfigNodeKind.PROJECT }
}

private enum class CreateTarget {
    PROJECT,
    MODULE,
    DEVICE,
    TAG,
}

private data class ChildCreateSpec(
    val label: String,
    val target: CreateTarget?,
    val enabled: Boolean,
)

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

private fun HostConfigNodeKind.label(): String {
    return when (this) {
        HostConfigNodeKind.PROJECT -> "工程"
        HostConfigNodeKind.PROTOCOL -> "协议"
        HostConfigNodeKind.MODULE -> "模块"
        HostConfigNodeKind.DEVICE -> "设备"
        HostConfigNodeKind.TAG -> "点位"
    }
}
