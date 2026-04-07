@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigOption
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigPanel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigSelectionField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTextField
import site.addzero.kcloud.plugins.hostconfig.common.icon
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.common.orDash
import site.addzero.kcloud.plugins.hostconfig.common.toSizeLabel
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel

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
                        text = "新建工程",
                        onClick = {
                            projectEditor = ProjectEditorSeed()
                            viewModel.clearNotice()
                        },
                        variant = WorkbenchButtonVariant.Default,
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
            ProjectsToolbar(
                state = state,
                onCreateProtocol = {
                    state.selectedProjectId?.let { projectId ->
                        protocolEditor = ProtocolEditorSeed(
                            projectId = projectId,
                        )
                    }
                },
                onLinkProtocol = {
                    state.selectedProjectId?.let { projectId ->
                        linkProtocolSeed = LinkProtocolSeed(projectId)
                    }
                },
                onCreateModule = {
                    state.selectedProjectId?.let { projectId ->
                        val availableTemplates = resolveModuleTemplatesForSelection(state)
                        moduleEditor = ModuleEditorSeed(
                            projectId = projectId,
                            protocolId = state.selectedProtocol?.id,
                            availableTemplates = availableTemplates,
                        )
                    }
                },
                onCreateDevice = {
                    val selectedModule = state.selectedModule ?: return@ProjectsToolbar
                    deviceEditor = DeviceEditorSeed(
                        projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                        moduleId = selectedModule.id,
                    )
                },
                onCreateTag = {
                    val deviceId = state.activeDeviceId ?: return@ProjectsToolbar
                    tagEditor = TagEditorSeed(
                        projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                        deviceId = deviceId,
                    )
                },
                onEditCurrent = {
                    when (state.selectedNode?.kind) {
                        HostConfigNodeKind.PROJECT, null -> {
                            state.selectedProject?.let { project ->
                                projectEditor = ProjectEditorSeed(existingId = project.id)
                            }
                        }

                        HostConfigNodeKind.PROTOCOL -> {
                            protocolEditor = ProtocolEditorSeed(
                                projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                                existing = state.selectedProtocol,
                            )
                        }

                        HostConfigNodeKind.MODULE -> {
                            moduleEditor = ModuleEditorSeed(
                                projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                                protocolId = state.selectedModule?.protocolId,
                                existing = state.selectedModule,
                                availableTemplates = resolveModuleTemplatesForSelection(state),
                            )
                        }

                        HostConfigNodeKind.DEVICE -> {
                            val moduleId = state.selectedNode?.parentEntityId ?: return@ProjectsToolbar
                            deviceEditor = DeviceEditorSeed(
                                projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                                moduleId = moduleId,
                                existing = state.selectedDevice,
                            )
                        }

                        HostConfigNodeKind.TAG -> {
                            val deviceId = state.selectedNode?.parentEntityId ?: return@ProjectsToolbar
                            tagEditor = TagEditorSeed(
                                projectId = state.selectedProjectId ?: return@ProjectsToolbar,
                                deviceId = deviceId,
                                existing = state.selectedTagDetail,
                            )
                        }
                    }
                },
                onDeleteCurrent = {
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
                },
                onMoveCurrent = {
                    state.selectedNode?.takeIf { node -> node.kind != HostConfigNodeKind.PROJECT }?.let { node ->
                        moveSeed = MoveNodeSeed(node = node)
                    }
                },
                onUploadProject = {
                    state.selectedProjectId?.let { projectId ->
                        uploadSeed = UploadSeed(projectId)
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.48f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProjectDetailPanel(state)
                    if (state.selectedProjectId != null) {
                        ProjectUploadPanel(
                            state = state,
                            onRefresh = viewModel::updateUploadStatus,
                            onOpen = {
                                uploadSeed = UploadSeed(it)
                            },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.activeDeviceId != null) {
                        TagListPanel(
                            state = state,
                            onPrev = viewModel::loadPreviousTagPage,
                            onNext = viewModel::loadNextTagPage,
                            onSelectTag = { tag ->
                                val projectId = state.selectedProjectId ?: return@TagListPanel
                                val deviceId = state.activeDeviceId ?: return@TagListPanel
                                viewModel.selectNode(
                                    ProjectsViewModel.buildTagNodeId(
                                        projectId = projectId,
                                        deviceId = deviceId,
                                        tagId = tag.id,
                                    ),
                                )
                            },
                            onEditTag = {
                                tagEditor = TagEditorSeed(
                                    projectId = state.selectedProjectId ?: return@TagListPanel,
                                    deviceId = state.activeDeviceId ?: return@TagListPanel,
                                    existing = it,
                                )
                            },
                            onDeleteTag = { tag ->
                                val projectId = state.selectedProjectId ?: return@TagListPanel
                                val deviceId = state.activeDeviceId ?: return@TagListPanel
                                viewModel.deleteTag(projectId, deviceId, tag.id)
                            },
                        )
                    } else {
                        ProjectOverviewPanel(state)
                    }
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
                    portName = draft.portName.ifBlank { null },
                    baudRate = draft.baudRate.toIntOrNull(),
                    dataBits = draft.dataBits.toIntOrNull(),
                    stopBits = draft.stopBits.toIntOrNull(),
                    parity = draft.parity,
                    responseTimeoutMs = draft.responseTimeoutMs.toIntOrNull(),
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
                            portName = request.portName,
                            baudRate = request.baudRate,
                            dataBits = request.dataBits,
                            stopBits = request.stopBits,
                            parity = request.parity,
                            responseTimeoutMs = request.responseTimeoutMs,
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
private fun ProjectsToolbar(
    state: ProjectsScreenState,
    onCreateProtocol: () -> Unit,
    onLinkProtocol: () -> Unit,
    onCreateModule: () -> Unit,
    onCreateDevice: () -> Unit,
    onCreateTag: () -> Unit,
    onEditCurrent: () -> Unit,
    onDeleteCurrent: () -> Unit,
    onMoveCurrent: () -> Unit,
    onUploadProject: () -> Unit,
) {
    HostConfigPanel(
        title = "操作台",
        subtitle = state.selectedNode?.label ?: state.selectedProject?.name ?: "请选择工程树节点",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkbenchActionButton("新建协议", onCreateProtocol, enabled = state.selectedProjectId != null)
            WorkbenchActionButton("关联协议", onLinkProtocol, variant = WorkbenchButtonVariant.Outline, enabled = state.selectedProjectId != null)
            WorkbenchActionButton("新建模块", onCreateModule, enabled = state.selectedProjectId != null)
            WorkbenchActionButton("新建设备", onCreateDevice, enabled = state.selectedModule != null)
            WorkbenchActionButton("新建点位", onCreateTag, enabled = state.activeDeviceId != null)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkbenchActionButton("编辑当前", onEditCurrent, imageVector = Icons.Outlined.Edit)
            WorkbenchActionButton("变更上级", onMoveCurrent, imageVector = Icons.Outlined.MoveDown, variant = WorkbenchButtonVariant.Outline, enabled = state.selectedNode?.kind != null && state.selectedNode?.kind != HostConfigNodeKind.PROJECT)
            WorkbenchActionButton("删除当前", onDeleteCurrent, imageVector = Icons.Outlined.Delete, variant = WorkbenchButtonVariant.Destructive)
            WorkbenchActionButton("上传工程", onUploadProject, imageVector = Icons.Outlined.Upload, variant = WorkbenchButtonVariant.Secondary, enabled = state.selectedProjectId != null)
            WorkbenchActionButton("刷新状态", onUploadProject, imageVector = Icons.Outlined.Refresh, variant = WorkbenchButtonVariant.Outline, enabled = false)
        }
    }
}

@Composable
private fun ProjectDetailPanel(
    state: ProjectsScreenState,
) {
    val node = state.selectedNode
    val title = node?.label ?: state.selectedProject?.name ?: "未选择节点"
    val subtitle = when (node?.kind ?: HostConfigNodeKind.PROJECT) {
        HostConfigNodeKind.PROJECT -> "工程详情"
        HostConfigNodeKind.PROTOCOL -> "协议详情"
        HostConfigNodeKind.MODULE -> "模块详情"
        HostConfigNodeKind.DEVICE -> "设备详情"
        HostConfigNodeKind.TAG -> "点位详情"
    }

    HostConfigPanel(
        title = title,
        subtitle = subtitle,
    ) {
        when (node?.kind ?: HostConfigNodeKind.PROJECT) {
            HostConfigNodeKind.PROJECT -> {
                val project = state.selectedProject
                val projectTree = state.selectedProjectTree
                HostConfigKeyValueRow("工程名称", project?.name.orDash())
                HostConfigKeyValueRow("描述", project?.description.orDash())
                HostConfigKeyValueRow("备注", project?.remark.orDash())
                HostConfigKeyValueRow("协议数量", projectTree?.protocols?.size?.toString() ?: "0")
                HostConfigKeyValueRow("根模块数量", projectTree?.modules?.size?.toString() ?: "0")
            }

            HostConfigNodeKind.PROTOCOL -> {
                val protocol = state.selectedProtocol
                HostConfigKeyValueRow("协议名称", protocol?.name.orDash())
                HostConfigKeyValueRow("协议模板", protocol?.protocolTemplateName.orDash())
                HostConfigKeyValueRow("轮询时间", protocol?.pollingIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("模块数量", protocol?.modules?.size?.toString() ?: "0")
            }

            HostConfigNodeKind.MODULE -> {
                val module = state.selectedModule
                HostConfigKeyValueRow("模块名称", module?.name.orDash())
                HostConfigKeyValueRow("模块模板", module?.moduleTemplateName.orDash())
                HostConfigKeyValueRow("串口", module?.portName.orDash())
                HostConfigKeyValueRow("波特率", module?.baudRate?.toString() ?: "-")
                HostConfigKeyValueRow("响应超时", module?.responseTimeoutMs?.toString() ?: "-")
                HostConfigKeyValueRow("设备数量", module?.devices?.size?.toString() ?: "0")
            }

            HostConfigNodeKind.DEVICE -> {
                val device = state.selectedDevice
                HostConfigKeyValueRow("设备名称", device?.name.orDash())
                HostConfigKeyValueRow("设备类型", device?.deviceTypeName.orDash())
                HostConfigKeyValueRow("站号", device?.stationNo?.toString() ?: "-")
                HostConfigKeyValueRow("请求间隔", device?.requestIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("写值间隔", device?.writeIntervalMs?.toString() ?: "-")
                HostConfigKeyValueRow("点位数量", device?.tags?.size?.toString() ?: "0")
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                HostConfigKeyValueRow("点位名称", tag?.name.orDash())
                HostConfigKeyValueRow("数据类型", tag?.dataTypeName.orDash())
                HostConfigKeyValueRow("寄存器类型", tag?.registerTypeName.orDash())
                HostConfigKeyValueRow("寄存器地址", tag?.registerAddress?.toString() ?: "-")
                HostConfigKeyValueRow("启用", if (tag?.enabled == true) "是" else "否")
                HostConfigKeyValueRow("点类型", tag?.pointType?.label() ?: "-")
                HostConfigKeyValueRow("值文本条目", tag?.valueTexts?.size?.toString() ?: "0")
            }
        }
    }
}

@Composable
private fun ProjectOverviewPanel(
    state: ProjectsScreenState,
) {
    HostConfigPanel(
        title = "工程概览",
        subtitle = "当前选中工程的协议、模块与设备概况。",
    ) {
        val projectTree = state.selectedProjectTree
        if (projectTree == null) {
            HostConfigStatusStrip("请先在左侧选择工程。")
            return@HostConfigPanel
        }
        HostConfigKeyValueRow("工程名称", projectTree.name)
        HostConfigKeyValueRow("协议数量", projectTree.protocols.size.toString())
        HostConfigKeyValueRow("根模块数量", projectTree.modules.size.toString())
        HostConfigKeyValueRow(
            "设备总数",
            (
                projectTree.modules.sumOf { module -> module.devices.size } +
                    projectTree.protocols.sumOf { protocol -> protocol.modules.sumOf { module -> module.devices.size } }
                ).toString(),
        )
        HostConfigKeyValueRow(
            "点位总数",
            (
                projectTree.modules.sumOf { module -> module.devices.sumOf { device -> device.tags.size } } +
                    projectTree.protocols.sumOf { protocol ->
                        protocol.modules.sumOf { module ->
                            module.devices.sumOf { device -> device.tags.size }
                        }
                    }
                ).toString(),
        )
    }
}

@Composable
private fun ProjectUploadPanel(
    state: ProjectsScreenState,
    onRefresh: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val uploadStatus = state.uploadStatus
    HostConfigPanel(
        title = "上传与备份",
        subtitle = "保留旧宿主配置的上传工程、远程动作和备份状态。",
        actions = {
            WorkbenchActionButton(
                text = "刷新",
                onClick = onRefresh,
                variant = WorkbenchButtonVariant.Outline,
            )
            state.selectedProjectId?.let { projectId ->
                WorkbenchActionButton(
                    text = "打开操作",
                    onClick = {
                        onOpen(projectId)
                    },
                )
            }
        },
    ) {
        HostConfigKeyValueRow("当前状态", uploadStatus?.statusText ?: "待开始")
        HostConfigKeyValueRow("进度", uploadStatus?.progress?.let { "$it%" } ?: "0%")
        HostConfigKeyValueRow("目标 IP", uploadStatus?.ipAddress.orDash())
        HostConfigKeyValueRow("工程路径", uploadStatus?.projectPath.orDash())
        HostConfigKeyValueRow("已选文件", uploadStatus?.selectedFileName.orDash())
        HostConfigKeyValueRow("备份文件", uploadStatus?.backupFileName.orDash())
        HostConfigKeyValueRow("备份大小", uploadStatus?.backupSizeBytes.toSizeLabel())
        HostConfigKeyValueRow("下载地址", uploadStatus?.backupDownloadUrl.orDash())
        uploadStatus?.detailText?.takeIf { it.isNotBlank() }?.let { detail ->
            HostConfigStatusStrip(detail)
        }
    }
}

@Composable
private fun TagListPanel(
    state: ProjectsScreenState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectTag: (site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse) -> Unit,
    onEditTag: (site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse) -> Unit,
    onDeleteTag: (site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse) -> Unit,
) {
    HostConfigPanel(
        title = "点位分页",
        subtitle = "保持旧宿主配置的点位分页读取与编辑流。",
        actions = {
            WorkbenchActionButton(
                text = "上一页",
                onClick = onPrev,
                variant = WorkbenchButtonVariant.Outline,
                enabled = state.tagOffset > 0,
            )
            WorkbenchActionButton(
                text = "下一页",
                onClick = onNext,
                variant = WorkbenchButtonVariant.Outline,
                enabled = state.tagOffset + state.tagSize < state.tagPage.t.toInt(),
            )
        },
    ) {
        HostConfigKeyValueRow(
            "分页状态",
            "偏移 ${state.tagOffset} / 共 ${state.tagPage.t} 条",
        )
        if (state.tagPage.d.isEmpty()) {
            HostConfigStatusStrip("当前设备下还没有点位。")
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.tagPage.d.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectTag(tag)
                            },
                    ) {
                        HostConfigPanel(
                            title = tag.name,
                            subtitle = "${tag.registerTypeName} / ${tag.registerAddress}",
                        ) {
                            HostConfigKeyValueRow("数据类型", tag.dataTypeName)
                            HostConfigKeyValueRow("启用", if (tag.enabled) "是" else "否")
                            HostConfigKeyValueRow("值文本", tag.valueTexts.size.toString())
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                WorkbenchActionButton(
                                    text = "编辑",
                                    onClick = {
                                        onEditTag(tag)
                                    },
                                    variant = WorkbenchButtonVariant.Outline,
                                )
                                WorkbenchActionButton(
                                    text = "删除",
                                    onClick = {
                                        onDeleteTag(tag)
                                    },
                                    variant = WorkbenchButtonVariant.Destructive,
                                )
                            }
                        }
                    }
                }
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
    var portName by remember(existing?.id) { mutableStateOf(existing?.portName.orEmpty()) }
    var baudRate by remember(existing?.id) { mutableStateOf(existing?.baudRate?.toString() ?: "9600") }
    var dataBits by remember(existing?.id) { mutableStateOf(existing?.dataBits?.toString() ?: "8") }
    var stopBits by remember(existing?.id) { mutableStateOf(existing?.stopBits?.toString() ?: "1") }
    var parity by remember(existing?.id) { mutableStateOf(existing?.parity ?: Parity.NONE) }
    var responseTimeoutMs by remember(existing?.id) { mutableStateOf(existing?.responseTimeoutMs?.toString() ?: "10") }
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
                            portName = portName,
                            baudRate = baudRate,
                            dataBits = dataBits,
                            stopBits = stopBits,
                            parity = parity,
                            responseTimeoutMs = responseTimeoutMs,
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
            HostConfigTextField("串口", portName, { portName = it })
            HostConfigTextField("波特率", baudRate, { baudRate = it })
            HostConfigTextField("数据位", dataBits, { dataBits = it })
            HostConfigTextField("停止位", stopBits, { stopBits = it })
            HostConfigTextField("响应超时(ms)", responseTimeoutMs, { responseTimeoutMs = it })
            HostConfigTextField("排序", sortIndex, { sortIndex = it })
            HostConfigPanel(
                title = "校验位",
                subtitle = "点击切换当前模块串口校验位。",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Parity.NONE, Parity.ODD, Parity.EVEN).forEach { option ->
                        WorkbenchActionButton(
                            text = option.label(),
                            onClick = {
                                parity = option
                            },
                            variant = if (parity == option) {
                                WorkbenchButtonVariant.Default
                            } else {
                                WorkbenchButtonVariant.Outline
                            },
                        )
                    }
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
    val selectedProtocol = state.selectedProtocol
    if (selectedProtocol == null) {
        return state.moduleTemplates
    }
    return state.moduleTemplates.filter { template ->
        template.protocolTemplateId == selectedProtocol.protocolTemplateId
    }
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
        HostConfigNodeKind.PROTOCOL -> state.projects.map { project ->
            HostConfigOption(
                value = "project:${project.id}",
                label = project.name,
            )
        }

        HostConfigNodeKind.MODULE -> {
            val projectOptions = state.projects.map { project ->
                HostConfigOption(
                    value = "project:${project.id}",
                    label = "${project.name} / 工程根",
                )
            }
            val protocolOptions = state.projectTrees.flatMap { project ->
                project.protocols.map { protocol ->
                    HostConfigOption(
                        value = "protocol:${project.id}:${protocol.id}",
                        label = "${project.name} / ${protocol.name}",
                    )
                }
            }
            projectOptions + protocolOptions
        }

        HostConfigNodeKind.DEVICE -> state.projectTrees.flatMap { project ->
            buildList {
                project.modules.forEach { module ->
                    add(
                        HostConfigOption(
                            value = "module:${project.id}:${module.id}",
                            label = "${project.name} / ${module.name}",
                        ),
                    )
                }
                project.protocols.forEach { protocol ->
                    protocol.modules.forEach { module ->
                        add(
                            HostConfigOption(
                                value = "module:${project.id}:${module.id}",
                                label = "${project.name} / ${protocol.name} / ${module.name}",
                            ),
                        )
                    }
                }
            }
        }

        HostConfigNodeKind.TAG -> state.projectTrees.flatMap { project ->
            buildList {
                project.modules.forEach { module ->
                    module.devices.forEach { device ->
                        add(
                            HostConfigOption(
                                value = "device:${project.id}:${device.id}",
                                label = "${project.name} / ${module.name} / ${device.name}",
                            ),
                        )
                    }
                }
                project.protocols.forEach { protocol ->
                    protocol.modules.forEach { module ->
                        module.devices.forEach { device ->
                            add(
                                HostConfigOption(
                                    value = "device:${project.id}:${device.id}",
                                    label = "${project.name} / ${protocol.name} / ${module.name} / ${device.name}",
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
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
    val portName: String,
    val baudRate: String,
    val dataBits: String,
    val stopBits: String,
    val parity: Parity?,
    val responseTimeoutMs: String,
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
