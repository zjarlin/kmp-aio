package site.addzero.kcloud.plugins.hostconfig.projects

import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.ModuleBoardRuntimeSnapshot
import site.addzero.kcloud.plugins.hostconfig.common.findNode

private val EmptyTagPage = PageResponse<TagResponse>(
    d = emptyList(),
    t = 0,
    p = 0,
)

/**
 * 表示项目界面状态。
 *
 * @property loading 加载状态。
 * @property busy 繁忙状态。
 * @property errorMessage 错误消息。
 * @property noticeMessage 提示消息。
 * @property projects 项目列表。
 * @property projectTrees 项目树。
 * @property treeNodes 树nodes。
 * @property protocolTemplates 协议模板。
 * @property moduleTemplateCatalog 模块模板目录。
 * @property deviceTypes 设备类型。
 * @property registerTypes 寄存器类型。
 * @property dataTypes 数据类型。
 * @property selectedProjectId 选中项目 ID。
 * @property selectedNodeId 选中node ID。
 * @property tagOffset 标签offset。
 * @property tagSize 标签size。
 * @property tagPage 标签分页。
 * @property selectedTagDetail 选中标签详情。
 * @property moduleBoardRuntime 模块boardruntime。
 * @property moduleBoardLoading 模块board加载。
 * @property moduleBoardErrorMessage 模块board错误消息。
 */
data class ProjectsScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val projects: List<ProjectResponse> = emptyList(),
    val projectTrees: List<ProjectTreeResponse> = emptyList(),
    val treeNodes: List<HostConfigTreeNode> = emptyList(),
    val protocolTemplates: List<TemplateOptionResponse> = emptyList(),
    val moduleTemplateCatalog: Map<Long, List<ModuleTemplateOptionResponse>> = emptyMap(),
    val deviceTypes: List<TemplateOptionResponse> = emptyList(),
    val registerTypes: List<TemplateOptionResponse> = emptyList(),
    val dataTypes: List<TemplateOptionResponse> = emptyList(),
    val selectedProjectId: Long? = null,
    val selectedNodeId: String? = null,
    val tagOffset: Int = 0,
    val tagSize: Int = 12,
    val tagPage: PageResponse<TagResponse> = EmptyTagPage,
    val selectedTagDetail: TagResponse? = null,
    val moduleBoardRuntime: ModuleBoardRuntimeSnapshot? = null,
    val moduleBoardLoading: Boolean = false,
    val moduleBoardErrorMessage: String? = null,
) {
    val moduleTemplates: List<ModuleTemplateOptionResponse>
        get() = moduleTemplateCatalog.values.flatten().distinctBy { template -> template.id }

    val selectedNode: HostConfigTreeNode?
        get() = treeNodes.findNode(selectedNodeId)

    val selectedProject: ProjectResponse?
        get() = projects.firstOrNull { it.id == selectedProjectId }

    val selectedProjectTree: ProjectTreeResponse?
        get() = projectTrees.firstOrNull { it.id == selectedProjectId }

    val selectedProtocol: ProtocolTreeNode?
        get() = selectedNode
            ?.takeIf { it.kind == HostConfigNodeKind.PROTOCOL }
            ?.let { node -> projectTrees.findProtocol(node.entityId) }

    val selectedModule: ModuleTreeNode?
        get() = selectedNode
            ?.takeIf { it.kind == HostConfigNodeKind.MODULE }
            ?.let { node -> projectTrees.findModule(node.entityId) }

    val selectedModuleProtocol: ProtocolTreeNode?
        get() = selectedModule?.let { module ->
            projectTrees.findProtocol(module.protocolId)
        }

    val selectedDevice: DeviceTreeNode?
        get() = selectedNode
            ?.takeIf { it.kind == HostConfigNodeKind.DEVICE }
            ?.let { node -> projectTrees.findDevice(node.entityId) }

    val activeDevice: DeviceTreeNode?
        get() = activeDeviceId?.let { deviceId -> projectTrees.findDevice(deviceId) }

    val activeDeviceId: Long?
        get() {
            val node = selectedNode ?: return null
            return when (node.kind) {
                HostConfigNodeKind.DEVICE -> node.entityId
                HostConfigNodeKind.MODULE -> node.parentEntityId
                HostConfigNodeKind.TAG -> node.parentEntityId
                else -> null
            }
        }
}

/**
 * 处理列表。
 *
 * @param protocolId 协议 ID。
 */
internal fun List<ProjectTreeResponse>.findProtocol(
    protocolId: Long,
): ProtocolTreeNode? {
    return asSequence()
        .flatMap { project -> project.protocols.asSequence() }
        .firstOrNull { protocol -> protocol.id == protocolId }
}

/**
 * 处理列表。
 *
 * @param moduleId 模块 ID。
 */
internal fun List<ProjectTreeResponse>.findModule(
    moduleId: Long,
): ModuleTreeNode? {
    return asSequence()
        .flatMap { project -> project.protocols.asSequence() }
        .flatMap { protocol -> protocol.devices.asSequence() }
        .flatMap { device -> device.modules.asSequence() }
        .firstOrNull { module -> module.id == moduleId }
}

/**
 * 处理列表。
 *
 * @param deviceId 设备 ID。
 */
internal fun List<ProjectTreeResponse>.findDevice(
    deviceId: Long,
): DeviceTreeNode? {
    return asSequence().mapNotNull { project ->
        project.protocols
            .asSequence()
            .flatMap { protocol -> protocol.devices.asSequence() }
            .firstOrNull { device -> device.id == deviceId }
    }.firstOrNull()
}

/**
 * 处理协议树node。
 */
internal fun ProtocolTreeNode.displayName(): String {
    return protocolTemplateName.takeIf { it.isNotBlank() } ?: name
}
