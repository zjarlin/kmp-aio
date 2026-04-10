package site.addzero.kcloud.plugins.hostconfig.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteImportRequest
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
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectUploadApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.TagApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.TemplateApi
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.ModuleBoardRuntimeSnapshot
import site.addzero.kcloud.plugins.hostconfig.common.findNode
import site.addzero.kcloud.plugins.mcuconsole.api.external.generated.DeviceInfoApi

@KoinViewModel
/**
 * 管理项目界面的状态与交互逻辑。
 *
 * @property projectApi 项目API。
 * @property tagApi 标签API。
 * @property templateApi 模板API。
 * @property projectSqliteApi 工程 sqlite 传输API。
 * @property deviceInfoApi 设备infoAPI。
 */
class ProjectsViewModel(
    private val projectApi: ProjectApi,
    private val tagApi: TagApi,
    private val templateApi: TemplateApi,
    private val projectSqliteApi: ProjectUploadApi,
    private val deviceInfoApi: DeviceInfoApi,
) : ViewModel() {
    var screenState by mutableStateOf(ProjectsScreenState())
        private set

    init {
        refresh()
    }

    /**
     * 刷新当前界面数据。
     */
    fun refresh() {
        viewModelScope.launch {
            loadPage()
        }
    }

    /**
     * 处理clear提示。
     */
    fun clearNotice() {
        screenState = screenState.copy(
            noticeMessage = null,
            errorMessage = null,
        )
    }

    /**
     * 显示提示消息。
     *
     * @param message 提示消息。
     */
    fun showNotice(message: String) {
        screenState = screenState.copy(
            noticeMessage = message,
            errorMessage = null,
        )
    }

    /**
     * 显示错误消息。
     *
     * @param message 错误消息。
     */
    fun showError(message: String) {
        screenState = screenState.copy(
            errorMessage = message,
            noticeMessage = null,
        )
    }

    /**
     * 选择项目。
     *
     * @param projectId 项目 ID。
     */
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

    /**
     * 选择node。
     *
     * @param nodeId node ID。
     */
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
            )
        }
    }

    /**
     * 加载previous标签分页。
     */
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

    /**
     * 加载next标签分页。
     */
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

    /**
     * 创建项目。
     *
     * @param request 请求参数。
     */
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

    /**
     * 更新项目。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
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

    /**
     * 更新项目位置。
     *
     * @param projectId 项目 ID。
     * @param sortIndex 目标排序序号。
     */
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

    /**
     * 删除项目。
     *
     * @param projectId 项目 ID。
     */
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

    /**
     * 创建协议。
     *
     * @param projectId 项目 ID。
     * @param request 请求参数。
     */
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

    /**
     * 更新协议。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     * @param request 请求参数。
     */
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

    /**
     * 移动协议。
     *
     * @param protocolId 协议 ID。
     * @param sourceProjectId 来源项目 ID。
     * @param targetProjectId 目标项目 ID。
     * @param sortIndex 目标排序序号。
     */
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

    /**
     * 删除协议。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     */
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

    /**
     * 在设备下创建模块。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
    fun createModule(
        projectId: Long,
        deviceId: Long,
        request: ModuleCreateRequest,
    ) {
        mutate("模块已创建") {
            val created = projectApi.createModule(deviceId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildModuleNodeId(projectId, created.id),
                noticeMessage = "模块已创建",
            )
        }
    }

    /**
     * 更新模块。
     *
     * @param projectId 项目 ID。
     * @param moduleId 模块 ID。
     * @param request 请求参数。
     */
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

    /**
     * 移动模块到设备。
     *
     * @param projectId 项目 ID。
     * @param moduleId 模块 ID。
     * @param deviceId 设备 ID。
     * @param sortIndex 目标排序序号。
     */
    fun moveModule(
        projectId: Long,
        moduleId: Long,
        deviceId: Long,
        sortIndex: Int,
    ) {
        mutate("模块位置已更新") {
            projectApi.updateModulePosition(
                moduleId = moduleId,
                request = ModulePositionUpdateRequest(
                    deviceId = deviceId,
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

    /**
     * 删除模块。
     *
     * @param projectId 项目 ID。
     * @param moduleId 模块 ID。
     */
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

    /**
     * 在协议下创建设备。
     *
     * @param projectId 项目 ID。
     * @param protocolId 协议 ID。
     * @param request 请求参数。
     */
    fun createDevice(
        projectId: Long,
        protocolId: Long,
        request: DeviceCreateRequest,
    ) {
        mutate("设备已创建") {
            val created = projectApi.createDevice(protocolId, request)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, created.id),
                noticeMessage = "设备已创建",
            )
        }
    }

    /**
     * 更新设备。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
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

    /**
     * 移动设备。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param protocolId 协议 ID。
     * @param sortIndex 目标排序序号。
     */
    fun moveDevice(
        projectId: Long,
        deviceId: Long,
        protocolId: Long,
        sortIndex: Int,
    ) {
        mutate("设备位置已更新") {
            projectApi.updateDevicePosition(
                deviceId = deviceId,
                request = DevicePositionUpdateRequest(
                    protocolId = protocolId,
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

    /**
     * 删除设备。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     */
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

    /**
     * 创建标签。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     * @param valueTexts 值文本。
     */
    fun createTag(
        projectId: Long,
        deviceId: Long,
        request: TagCreateRequest,
        valueTexts: List<TagValueTextInput>,
    ) {
        mutate("标签已创建") {
            val created = tagApi.createTag(deviceId, request)
            tagApi.replaceValueTexts(
                tagId = created.id,
                request = ReplaceTagValueTextsRequest(items = valueTexts),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildTagNodeId(projectId, deviceId, created.id),
                noticeMessage = "标签已创建",
            )
        }
    }

    /**
     * 更新标签。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param tagId 标签 ID。
     * @param request 请求参数。
     * @param valueTexts 值文本。
     */
    fun updateTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
        request: TagUpdateRequest,
        valueTexts: List<TagValueTextInput>,
    ) {
        mutate("标签已更新") {
            tagApi.updateTag(tagId, request)
            tagApi.replaceValueTexts(
                tagId = tagId,
                request = ReplaceTagValueTextsRequest(items = valueTexts),
            )
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildTagNodeId(projectId, deviceId, tagId),
                noticeMessage = "标签已更新",
            )
        }
    }

    /**
     * 移动标签。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param tagId 标签 ID。
     * @param sortIndex 目标排序序号。
     */
    fun moveTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
        sortIndex: Int,
    ) {
        mutate("标签位置已更新") {
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
                noticeMessage = "标签位置已更新",
            )
        }
    }

    /**
     * 删除标签。
     *
     * @param projectId 项目 ID。
     * @param deviceId 设备 ID。
     * @param tagId 标签 ID。
     */
    fun deleteTag(
        projectId: Long,
        deviceId: Long,
        tagId: Long,
    ) {
        mutate("标签已删除") {
            tagApi.deleteTag(tagId)
            loadPage(
                preferredProjectId = projectId,
                preferredNodeId = buildDeviceNodeId(projectId, deviceId),
                noticeMessage = "标签已删除",
            )
        }
    }

    /**
     * 导出当前选中工程为 sqlite 文件。
     */
    fun exportSelectedProjectSqlite() {
        val projectId = screenState.selectedProjectId
        if (projectId == null) {
            showError("请先在左侧选择一个工程，再导出 sqlite 文件")
            return
        }
        mutate("工程 sqlite 已导出") {
            val response = projectSqliteApi.exportProjectSqlite(projectId)
            screenState = screenState.copy(
                noticeMessage = "已导出 SQLite 到 ${response.filePath}",
                errorMessage = null,
            )
        }
    }

    /**
     * 导入本地 sqlite 工程文件到数据目录。
     *
     * @param sourceFilePath 源文件路径。
     */
    fun importProjectSqlite(sourceFilePath: String) {
        mutate("工程 sqlite 已导入") {
            val response = projectSqliteApi.importProjectSqlite(
                ProjectSqliteImportRequest(sourceFilePath = sourceFilePath),
            )
            screenState = screenState.copy(
                noticeMessage = "已导入 SQLite 到 ${response.filePath}",
                errorMessage = null,
            )
        }
    }

    /**
     * 处理mutate。
     *
     * @param successMessage success消息。
     * @param block block。
     */
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

    /**
     * 加载分页。
     *
     * @param preferredProjectId preferred项目 ID。
     * @param preferredNodeId preferrednode ID。
     * @param noticeMessage 提示消息。
     */
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
            val protocolTemplates = templateApi.listProtocolTemplates()
            val deviceTypes = templateApi.listDeviceTypes()
            val registerTypes = templateApi.listRegisterTypes()
            val dataTypes = templateApi.listDataTypes()

            if (projects.isEmpty()) {
                screenState = ProjectsScreenState(
                    loading = false,
                    noticeMessage = noticeMessage,
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
            )
        }.onFailure { throwable ->
            screenState = screenState.copy(
                loading = false,
                busy = false,
                errorMessage = throwable.message ?: "加载元数据配置失败",
            )
        }
    }

    /**
     * 处理applyselection。
     *
     * @param selectedProjectId 选中项目 ID。
     * @param selectedNodeId 选中node ID。
     */
    private suspend fun applySelection(
        selectedProjectId: Long,
        selectedNodeId: String,
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

        if (selectedNode?.kind == HostConfigNodeKind.MODULE) {
            loadModuleBoardRuntime(selectedNode.entityId)
        } else {
            clearModuleBoardRuntime()
        }
    }

    /**
     * 加载标签。
     *
     * @param deviceId 设备 ID。
     * @param offset offset。
     * @param preferredTagId preferred标签 ID。
     */
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

    /**
     * 加载模块模板目录。
     *
     * @param projectTrees 项目树。
     */
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

    /**
     * 处理clear模块板卡runtime。
     */
    private fun clearModuleBoardRuntime() {
        screenState = screenState.copy(
            moduleBoardRuntime = null,
            moduleBoardLoading = false,
            moduleBoardErrorMessage = null,
        )
    }

    /**
     * 加载模块板卡runtime。
     *
     * @param moduleId 模块 ID。
     */
    private suspend fun loadModuleBoardRuntime(
        moduleId: Long,
    ) {
        screenState = screenState.copy(
            moduleBoardRuntime = null,
            moduleBoardLoading = true,
            moduleBoardErrorMessage = null,
        )

        val loadResult = coroutineScope {
            val deviceInfoDeferred = async {
                readRuntimePart("设备信息") {
                    deviceInfoApi.getDeviceInfo()
                }
            }
            val powerLightsDeferred = async {
                readRuntimePart("24 路电源灯") {
                    deviceInfoApi.get24PowerLights()
                }
            }
            val flashConfigDeferred = async {
                readRuntimePart("Flash 配置") {
                    deviceInfoApi.getFlashConfig()
                }
            }

            val deviceInfoPart = deviceInfoDeferred.await()
            val powerLightsPart = powerLightsDeferred.await()
            val flashConfigPart = flashConfigDeferred.await()

            val snapshot = ModuleBoardRuntimeSnapshot(
                deviceInfo = deviceInfoPart.value,
                powerLights = powerLightsPart.value,
                flashConfig = flashConfigPart.value,
            )
            val failedLabels = listOfNotNull(
                deviceInfoPart.failedLabel,
                powerLightsPart.failedLabel,
                flashConfigPart.failedLabel,
            )

            ModuleBoardRuntimeLoadResult(
                snapshot = snapshot.takeIf { it.hasAnyData },
                errorMessage = failedLabels
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(
                        prefix = "以下在线数据未能读取：",
                        separator = "、",
                    ),
            )
        }

        if (screenState.selectedModule?.id != moduleId) {
            return
        }

        screenState = screenState.copy(
            moduleBoardRuntime = loadResult.snapshot,
            moduleBoardLoading = false,
            moduleBoardErrorMessage = loadResult.errorMessage,
        )
    }

    private suspend fun <T> readRuntimePart(
        label: String,
        block: suspend () -> T,
    ): RuntimePartResult<T> {
        return runCatching {
            block()
        }.fold(
            onSuccess = { value ->
                RuntimePartResult(
                    value = value,
                    failedLabel = null,
                )
            },
            onFailure = {
                RuntimePartResult(
                    value = null,
                    failedLabel = label,
                )
            },
        )
    }

    /**
     * 构建树nodes。
     *
     * @param projectTrees 项目树。
     */
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

    /**
     * 构建协议nodes。
     *
     * @param project 项目。
     */
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
                label = protocol.displayName(),
                caption = protocol.protocolTemplateName,
                children = buildDeviceNodes(
                    projectId = project.id,
                    devices = protocol.devices,
                ),
            )
        }
    }

    /**
     * 构建设备nodes。
     *
     * @param projectId 项目 ID。
     * @param devices 设备。
     */
    private fun buildDeviceNodes(
        projectId: Long,
        devices: List<site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode>,
    ): List<HostConfigTreeNode> {
        return devices.map { device ->
            HostConfigTreeNode(
                id = buildDeviceNodeId(projectId, device.id),
                kind = HostConfigNodeKind.DEVICE,
                projectId = projectId,
                entityId = device.id,
                parentEntityId = device.protocolId,
                label = device.name,
                caption = device.deviceTypeName,
                children = buildList {
                    device.modules.forEach { module ->
                        add(
                            HostConfigTreeNode(
                                id = buildModuleNodeId(projectId, module.id),
                                kind = HostConfigNodeKind.MODULE,
                                projectId = projectId,
                                entityId = module.id,
                                parentEntityId = device.id,
                                label = module.name,
                                caption = module.moduleTemplateName,
                            ),
                        )
                    }
                    device.tags.forEach { tag ->
                        add(
                            HostConfigTreeNode(
                                id = buildTagNodeId(projectId, device.id, tag.id),
                                kind = HostConfigNodeKind.TAG,
                                projectId = projectId,
                                entityId = tag.id,
                                parentEntityId = device.id,
                                label = tag.name,
                            ),
                        )
                    }
                },
            )
        }
    }

    companion object {
        /**
         * 构建项目nodeID。
         *
         * @param projectId 项目 ID。
         */
        fun buildProjectNodeId(projectId: Long): String = "project/$projectId"

        /**
         * 构建协议nodeID。
         *
         * @param projectId 项目 ID。
         * @param protocolId 协议 ID。
         */
        fun buildProtocolNodeId(projectId: Long, protocolId: Long): String =
            "protocol/$projectId/$protocolId"

        /**
         * 构建模块nodeID。
         *
         * @param projectId 项目 ID。
         * @param moduleId 模块 ID。
         */
        fun buildModuleNodeId(projectId: Long, moduleId: Long): String =
            "module/$projectId/$moduleId"

        /**
         * 构建设备nodeID。
         *
         * @param projectId 项目 ID。
         * @param deviceId 设备 ID。
         */
        fun buildDeviceNodeId(projectId: Long, deviceId: Long): String =
            "device/$projectId/$deviceId"

        /**
         * 构建标签nodeID。
         *
         * @param projectId 项目 ID。
         * @param deviceId 设备 ID。
         * @param tagId 标签 ID。
         */
        fun buildTagNodeId(projectId: Long, deviceId: Long, tagId: Long): String =
            "tag/$projectId/$deviceId/$tagId"
    }
}

/**
 * 表示模块板卡runtime加载result。
 *
 * @property snapshot 快照。
 * @property errorMessage 错误消息。
 */
private data class ModuleBoardRuntimeLoadResult(
    val snapshot: ModuleBoardRuntimeSnapshot?,
    val errorMessage: String?,
)

/**
 * 表示runtimepartresult。
 *
 * @property value 值。
 * @property failedLabel failedlabel。
 */
private data class RuntimePartResult<T>(
    val value: T?,
    val failedLabel: String?,
)

/**
 * 处理主机配置树node。
 *
 * @param nodeId node ID。
 */
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
