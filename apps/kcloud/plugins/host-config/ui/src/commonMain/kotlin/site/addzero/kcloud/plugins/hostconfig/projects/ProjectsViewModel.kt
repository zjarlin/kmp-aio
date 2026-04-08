package site.addzero.kcloud.plugins.hostconfig.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextInput
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.external.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.api.external.ProjectUploadApi
import site.addzero.kcloud.plugins.hostconfig.api.external.TagApi
import site.addzero.kcloud.plugins.hostconfig.api.external.TemplateApi
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.findNode

@KoinViewModel
class ProjectsViewModel(
    private val projectApi: ProjectApi,
    private val tagApi: TagApi,
    private val templateApi: TemplateApi,
    private val projectUploadApi: ProjectUploadApi,
) : ViewModel() {
    var screenState by mutableStateOf(ProjectsScreenState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadPage()
        }
    }

    fun clearNotice() {
        screenState = screenState.copy(
            noticeMessage = null,
            errorMessage = null,
        )
    }

    fun selectProject(
        projectId: Long,
    ) {
        val targetNodeId = buildProjectNodeId(projectId)
        viewModelScope.launch {
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = targetNodeId,
            )
        }
    }

    fun selectNode(
        nodeId: String,
    ) {
        val node = screenState.treeNodes.find { it.id == nodeId }
            ?: screenState.treeNodes.firstNotNullOfOrNull { root -> root.findChild(nodeId) }
            ?: return
        viewModelScope.launch {
            applySelection(
                selectedProjectId = node.projectId,
                selectedNodeId = node.id,
                uploadStatusProjectId = node.projectId,
            )
        }
    }

    fun loadPreviousTagPage() {
        val deviceId = screenState.activeDeviceId ?: return
        if (screenState.tagOffset <= 0) {
            return
        }
        viewModelScope.launch {
            loadTags(
                deviceId = deviceId,
                offset = (screenState.tagOffset - screenState.tagSize).coerceAtLeast(0),
            )
        }
    }

    fun loadNextTagPage() {
        val deviceId = screenState.activeDeviceId ?: return
        if (screenState.tagOffset + screenState.tagSize >= screenState.tagPage.t.toInt()) {
            return
        }
        viewModelScope.launch {
            loadTags(
                deviceId = deviceId,
                offset = screenState.tagOffset + screenState.tagSize,
            )
        }
    }

    fun createProject(
        request: ProjectCreateRequest,
    ) {
        mutate("工程已创建") {
            val created = projectApi.createProject(request)
            loadPage(
                preferredProjectId = created.id,
                preferredNodeId = buildProjectNodeId(created.id),
                noticeMessage = "工程已创建",
            )
        }
    }

    fun updateProject(
        projectId: Long,
        request: ProjectUpdateRequest,
    ) {
        mutate("工程已更新") {
            projectApi.updateProject(projectId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProjectNodeId(projectId),
                noticeMessage = "工程已更新",
            )
        }
    }

    fun updateProjectPosition(
        projectId: Long,
        sortIndex: Int,
    ) {
        mutate("工程顺序已更新") {
            projectApi.updateProjectPosition(
                projectId = projectId,
                request = ProjectPositionUpdateRequest(sortIndex = sortIndex),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProjectNodeId(projectId),
                noticeMessage = "工程顺序已更新",
            )
        }
    }

    fun deleteProject(
        projectId: Long,
    ) {
        mutate("工程已删除") {
            projectApi.deleteProject(projectId)
            loadPage(
                preferredProjectId = null,
                preferredNodeId = null,
                noticeMessage = "工程已删除",
            )
        }
    }

    fun createProtocol(
        projectId: Long,
        request: ProtocolCreateRequest,
    ) {
        mutate("协议已创建") {
            val created = projectApi.createProtocol(projectId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProtocolNodeId(projectId, created.id),
                noticeMessage = "协议已创建",
            )
        }
    }

    fun updateProtocol(
        projectId: Long,
        protocolId: Long,
        request: ProtocolUpdateRequest,
    ) {
        mutate("协议已更新") {
            projectApi.updateProtocol(
                protocolId = protocolId,
                request = request.copy(projectId = projectId),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProtocolNodeId(projectId, protocolId),
                noticeMessage = "协议已更新",
            )
        }
    }

    fun linkProtocol(
        projectId: Long,
        request: LinkExistingProtocolRequest,
    ) {
        mutate("协议已关联") {
            val linked = projectApi.linkProtocol(projectId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProtocolNodeId(projectId, linked.id),
                noticeMessage = "协议已关联",
            )
        }
    }

    fun moveProtocol(
        protocolId: Long,
        sourceProjectId: Long,
        targetProjectId: Long,
        sortIndex: Int,
    ) {
        mutate("协议位置已更新") {
            projectApi.updateProtocolPosition(
                protocolId = protocolId,
                request = ProtocolPositionUpdateRequest(
                    sourceProjectId = sourceProjectId,
                    targetProjectId = targetProjectId,
                    sortIndex = sortIndex,
                ),
            )
            loadPage(
                preferredProjectId = targetProjectId,
                preferredNodeId = buildProtocolNodeId(targetProjectId, protocolId),
                noticeMessage = "协议位置已更新",
            )
        }
    }

    fun deleteProtocol(
        projectId: Long,
        protocolId: Long,
    ) {
        mutate("协议已移除") {
            projectApi.deleteProtocol(
                projectId = projectId,
                protocolId = protocolId,
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProjectNodeId(projectId),
                noticeMessage = "协议已移除",
            )
        }
    }

    fun createModuleUnderProtocol(
        projectId: Long,
        protocolId: Long,
        request: ModuleCreateRequest,
    ) {
        mutate("模块已创建") {
            val created = projectApi.createModule(protocolId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildModuleNodeId(projectId, created.id),
                noticeMessage = "模块已创建",
            )
        }
    }

    fun createModuleUnderProject(
        projectId: Long,
        request: ModuleCreateRequest,
    ) {
        mutate("模块已创建") {
            val created = projectApi.createProjectModule(projectId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildModuleNodeId(projectId, created.id),
                noticeMessage = "模块已创建",
            )
        }
    }

    fun updateModule(
        projectId: Long,
        moduleId: Long,
        request: ModuleUpdateRequest,
    ) {
        mutate("模块已更新") {
            projectApi.updateModule(moduleId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildModuleNodeId(projectId, moduleId),
                noticeMessage = "模块已更新",
            )
        }
    }

    fun moveModuleToProtocol(
        projectId: Long,
        moduleId: Long,
        protocolId: Long,
        sortIndex: Int,
    ) {
        mutate("模块位置已更新") {
            projectApi.updateModulePosition(
                moduleId = moduleId,
                request = ModulePositionUpdateRequest(
                    protocolId = protocolId,
                    sortIndex = sortIndex,
                ),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildModuleNodeId(projectId, moduleId),
                noticeMessage = "模块位置已更新",
            )
        }
    }

    fun moveModuleToProject(
        sourceProjectId: Long,
        targetProjectId: Long,
        moduleId: Long,
        sortIndex: Int,
    ) {
        mutate("模块位置已更新") {
            projectApi.updateModulePosition(
                moduleId = moduleId,
                request = ModulePositionUpdateRequest(
                    projectId = targetProjectId,
                    sourceProjectId = sourceProjectId,
                    sortIndex = sortIndex,
                ),
            )
            loadPage(
                preferredProjectId = targetProjectId,
                preferredNodeId = buildModuleNodeId(targetProjectId, moduleId),
                noticeMessage = "模块位置已更新",
            )
        }
    }

    fun deleteModule(
        projectId: Long,
        moduleId: Long,
    ) {
        mutate("模块已删除") {
            projectApi.deleteModule(moduleId)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProjectNodeId(projectId),
                noticeMessage = "模块已删除",
            )
        }
    }

    fun createDevice(
        projectId: Long,
        moduleId: Long,
        request: DeviceCreateRequest,
    ) {
        mutate("设备已创建") {
            val created = projectApi.createDevice(moduleId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, created.id),
                noticeMessage = "设备已创建",
            )
        }
    }

    fun updateDevice(
        projectId: Long,
        deviceId: Long,
        request: DeviceUpdateRequest,
    ) {
        mutate("设备已更新") {
            projectApi.updateDevice(deviceId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, deviceId),
                noticeMessage = "设备已更新",
            )
        }
    }

    fun moveDevice(
        projectId: Long,
        deviceId: Long,
        moduleId: Long,
        sortIndex: Int,
    ) {
        mutate("设备位置已更新") {
            projectApi.updateDevicePosition(
                deviceId = deviceId,
                request = DevicePositionUpdateRequest(
                    moduleId = moduleId,
                    sortIndex = sortIndex,
                ),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, deviceId),
                noticeMessage = "设备位置已更新",
            )
        }
    }

    fun deleteDevice(
        projectId: Long,
        deviceId: Long,
    ) {
        mutate("设备已删除") {
            projectApi.deleteDevice(deviceId)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildProjectNodeId(projectId),
                noticeMessage = "设备已删除",
            )
        }
    }

    fun createTag(
        projectId: Long,
        deviceId: Long,
        request: TagCreateRequest,
        valueTexts: List<TagValueTextInput>,
    ) {
        mutate("点位已创建") {
            val created = tagApi.createTag(deviceId, request)
            tagApi.replaceValueTexts(
                tagId = created.id,
                request = ReplaceTagValueTextsRequest(items = valueTexts),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildTagNodeId(projectId, deviceId, created.id),
                noticeMessage = "点位已创建",
            )
        }
    }

    fun updateTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
        request: TagUpdateRequest,
        valueTexts: List<TagValueTextInput>,
    ) {
        mutate("点位已更新") {
            tagApi.updateTag(tagId, request)
            tagApi.replaceValueTexts(
                tagId = tagId,
                request = ReplaceTagValueTextsRequest(items = valueTexts),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildTagNodeId(projectId, deviceId, tagId),
                noticeMessage = "点位已更新",
            )
        }
    }

    fun moveTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
        sortIndex: Int,
    ) {
        mutate("点位位置已更新") {
            tagApi.updateTagPosition(
                tagId = tagId,
                request = TagPositionUpdateRequest(
                    deviceId = deviceId,
                    sortIndex = sortIndex,
                ),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildTagNodeId(projectId, deviceId, tagId),
                noticeMessage = "点位位置已更新",
            )
        }
    }

    fun deleteTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
    ) {
        mutate("点位已删除") {
            tagApi.deleteTag(tagId)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, deviceId),
                noticeMessage = "点位已删除",
            )
        }
    }

    fun updateUploadStatus() {
        val projectId = screenState.selectedProjectId ?: return
        viewModelScope.launch {
            val uploadStatus = projectUploadApi.getProjectUploadStatus(projectId)
            screenState = screenState.copy(
                uploadStatus = uploadStatus,
            )
        }
    }

    fun submitUpload(
        request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest,
    ) {
        val projectId = screenState.selectedProjectId ?: return
        mutate("上传请求已提交") {
            val uploadStatus = projectUploadApi.uploadProject(projectId, request)
            screenState = screenState.copy(
                uploadStatus = uploadStatus,
                noticeMessage = "上传请求已提交",
            )
        }
    }

    fun triggerUploadAction(
        action: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction,
        request: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest,
    ) {
        val projectId = screenState.selectedProjectId ?: return
        mutate("${action.name} 操作已提交") {
            val uploadStatus = projectUploadApi.triggerProjectUploadRemoteAction(
                projectId = projectId,
                action = action,
                request = request,
            )
            screenState = screenState.copy(
                uploadStatus = uploadStatus,
                noticeMessage = "${action.name} 操作已提交",
            )
        }
    }

    private fun mutate(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            screenState = screenState.copy(
                busy = true,
                errorMessage = null,
            )
            runCatching {
                block()
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = throwable.message ?: "操作失败",
                )
            }.onSuccess {
                if (!screenState.busy) {
                    return@onSuccess
                }
                screenState = screenState.copy(
                    busy = false,
                    noticeMessage = screenState.noticeMessage ?: successMessage,
                )
            }
        }
    }

    private suspend fun loadPage(
        preferredProjectId: Long? = screenState.selectedProjectId,
        preferredNodeId: String? = screenState.selectedNodeId,
        noticeMessage: String? = screenState.noticeMessage,
    ) {
        screenState = screenState.copy(
            loading = true,
            errorMessage = null,
        )
        runCatching {
            val projects = projectApi.listProjects()
            val protocolCatalog = projectApi.listProtocols()
            val protocolTemplates = templateApi.listProtocolTemplates()
            val deviceTypes = templateApi.listDeviceTypes()
            val registerTypes = templateApi.listRegisterTypes()
            val dataTypes = templateApi.listDataTypes()

            if (projects.isEmpty()) {
                screenState = ProjectsScreenState(
                    loading = false,
                    noticeMessage = noticeMessage,
                    protocolCatalog = protocolCatalog,
                    protocolTemplates = protocolTemplates,
                    deviceTypes = deviceTypes,
                    registerTypes = registerTypes,
                    dataTypes = dataTypes,
                )
                return
            }

            val selectedProjectId = preferredProjectId
                ?.takeIf { candidate -> projects.any { project -> project.id == candidate } }
                ?: projects.first().id

            val projectTrees = projects.map { project ->
                projectApi.getProjectTree(project.id)
            }
            val treeNodes = buildTreeNodes(projectTrees)
            val moduleTemplateCatalog = loadModuleTemplateCatalog(
                projectTrees = projectTrees,
            )
            val selectedNodeId = treeNodes.findNode(preferredNodeId)?.id ?: buildProjectNodeId(selectedProjectId)

            screenState = ProjectsScreenState(
                loading = false,
                busy = screenState.busy,
                errorMessage = null,
                noticeMessage = noticeMessage,
                projects = projects,
                projectTrees = projectTrees,
                treeNodes = treeNodes,
                protocolCatalog = protocolCatalog,
                protocolTemplates = protocolTemplates,
                moduleTemplateCatalog = moduleTemplateCatalog,
                deviceTypes = deviceTypes,
                registerTypes = registerTypes,
                dataTypes = dataTypes,
                selectedProjectId = selectedProjectId,
                selectedNodeId = selectedNodeId,
                tagOffset = 0,
            )

            applySelection(
                selectedProjectId = selectedProjectId,
                selectedNodeId = selectedNodeId,
                uploadStatusProjectId = selectedProjectId,
            )
        }.onFailure { throwable ->
            screenState = screenState.copy(
                loading = false,
                busy = false,
                errorMessage = throwable.message ?: "加载宿主配置失败",
            )
        }
    }

    private suspend fun applySelection(
        selectedProjectId: Long,
        selectedNodeId: String,
        uploadStatusProjectId: Long,
    ) {
        val nextState = screenState.copy(
            selectedProjectId = selectedProjectId,
            selectedNodeId = selectedNodeId,
        )
        screenState = nextState

        val selectedNode = nextState.treeNodes.findNode(selectedNodeId)
        val activeDeviceId = when (selectedNode?.kind) {
            HostConfigNodeKind.DEVICE -> selectedNode.entityId
            HostConfigNodeKind.TAG -> selectedNode.parentEntityId
            else -> null
        }
        val preferredTagId = selectedNode
            ?.takeIf { node -> node.kind == HostConfigNodeKind.TAG }
            ?.entityId
        val tagOffset = preferredTagId
            ?.let { tagId ->
                nextState.activeDevice
                    ?.tags
                    ?.indexOfFirst { tag -> tag.id == tagId }
                    ?.takeIf { index -> index >= 0 }
                    ?.let { index -> (index / nextState.tagSize) * nextState.tagSize }
            }
            ?: 0

        if (activeDeviceId != null) {
            loadTags(
                deviceId = activeDeviceId,
                offset = tagOffset,
                preferredTagId = preferredTagId,
            )
        } else {
            screenState = screenState.copy(
                tagOffset = 0,
                tagPage = site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse(
                    d = emptyList(),
                    t = 0,
                    p = 0,
                ),
                selectedTagDetail = null,
            )
        }

        screenState = screenState.copy(
            uploadStatus = projectUploadApi.getProjectUploadStatus(uploadStatusProjectId),
        )
    }

    private suspend fun loadTags(
        deviceId: Long,
        offset: Int,
        preferredTagId: Long? = null,
    ) {
        val tagPage = tagApi.listTags(
            deviceId = deviceId,
            offset = offset,
            size = screenState.tagSize,
        )
        val selectedTagDetail = preferredTagId
            ?.let { tagId -> tagApi.getTag(tagId) }
            ?: screenState.selectedNode
                ?.takeIf { node -> node.kind == HostConfigNodeKind.TAG && tagPage.d.any { tag -> tag.id == node.entityId } }
                ?.let { node -> tagApi.getTag(node.entityId) }

        screenState = screenState.copy(
            tagOffset = offset,
            tagPage = tagPage,
            selectedTagDetail = selectedTagDetail,
        )
    }

    private suspend fun loadModuleTemplateCatalog(
        projectTrees: List<ProjectTreeResponse>,
    ): Map<Long, List<ModuleTemplateOptionResponse>> {
        val protocolTemplateIds = projectTrees.asSequence()
            .flatMap { projectTree -> projectTree.protocols.asSequence() }
            .map { protocol -> protocol.protocolTemplateId }
            .distinct()
            .toList()
        return protocolTemplateIds.associateWith { protocolTemplateId ->
            emptyList<ModuleTemplateOptionResponse>()
        }.toMutableMap().apply {
            protocolTemplateIds.forEach { protocolTemplateId ->
                this[protocolTemplateId] = templateApi.listModuleTemplates(protocolTemplateId)
                    .distinctBy { template -> template.id }
            }
        }
    }

    private fun buildTreeNodes(
        projectTrees: List<ProjectTreeResponse>,
    ): List<HostConfigTreeNode> {
        return projectTrees.map { project ->
            HostConfigTreeNode(
                id = buildProjectNodeId(project.id),
                kind = HostConfigNodeKind.PROJECT,
                projectId = project.id,
                entityId = project.id,
                label = project.name,
                caption = project.description,
                children = buildProtocolNodes(project),
            )
        }
    }

    private fun buildProtocolNodes(
        project: ProjectTreeResponse,
    ): List<HostConfigTreeNode> {
        return project.protocols.map { protocol ->
            HostConfigTreeNode(
                id = buildProtocolNodeId(project.id, protocol.id),
                kind = HostConfigNodeKind.PROTOCOL,
                projectId = project.id,
                entityId = protocol.id,
                parentEntityId = project.id,
                label = protocol.name,
                caption = protocol.protocolTemplateName,
                children = buildModuleNodes(
                    projectId = project.id,
                    modules = protocol.modules,
                ),
            )
        }
    }

    private fun buildModuleNodes(
        projectId: Long,
        modules: List<ModuleTreeNode>,
    ): List<HostConfigTreeNode> {
        return modules.map { module ->
            HostConfigTreeNode(
                id = buildModuleNodeId(projectId, module.id),
                kind = HostConfigNodeKind.MODULE,
                projectId = projectId,
                entityId = module.id,
                parentEntityId = module.protocolId,
                label = module.name,
                caption = module.moduleTemplateName,
                children = module.devices.map { device ->
                    HostConfigTreeNode(
                        id = buildDeviceNodeId(projectId, device.id),
                        kind = HostConfigNodeKind.DEVICE,
                        projectId = projectId,
                        entityId = device.id,
                        parentEntityId = module.id,
                        label = device.name,
                        caption = device.deviceTypeName,
                        children = device.tags.map { tag ->
                            HostConfigTreeNode(
                                id = buildTagNodeId(projectId, device.id, tag.id),
                                kind = HostConfigNodeKind.TAG,
                                projectId = projectId,
                                entityId = tag.id,
                                parentEntityId = device.id,
                                label = tag.name,
                            )
                        },
                    )
                },
            )
        }
    }

    companion object {
        fun buildProjectNodeId(projectId: Long): String = "project/$projectId"

        fun buildProtocolNodeId(projectId: Long, protocolId: Long): String =
            "protocol/$projectId/$protocolId"

        fun buildModuleNodeId(projectId: Long, moduleId: Long): String =
            "module/$projectId/$moduleId"

        fun buildDeviceNodeId(projectId: Long, deviceId: Long): String =
            "device/$projectId/$deviceId"

        fun buildTagNodeId(projectId: Long, deviceId: Long, tagId: Long): String =
            "tag/$projectId/$deviceId/$tagId"
    }
}

private fun HostConfigTreeNode.findChild(
    nodeId: String,
): HostConfigTreeNode? {
    if (id == nodeId) {
        return this
    }
    children.forEach { child ->
        val match = child.findChild(nodeId)
        if (match != null) {
            return match
        }
    }
    return null
}
