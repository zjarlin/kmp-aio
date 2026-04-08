package site.addzero.kcloud.plugins.hostconfig.projects

import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTreeNode
import site.addzero.kcloud.plugins.hostconfig.common.findNode

private val EmptyTagPage = PageResponse<TagResponse>(
    d = emptyList(),
    t = 0,
    p = 0,
)

data class ProjectsScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val projects: List<ProjectResponse> = emptyList(),
    val projectTrees: List<ProjectTreeResponse> = emptyList(),
    val treeNodes: List<HostConfigTreeNode> = emptyList(),
    val protocolCatalog: List<ProtocolCatalogItemResponse> = emptyList(),
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
    val uploadStatus: ProjectUploadOperationResponse? = null,
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
                HostConfigNodeKind.TAG -> node.parentEntityId
                else -> null
            }
        }
}

internal fun List<ProjectTreeResponse>.findProtocol(
    protocolId: Long,
): ProtocolTreeNode? {
    return asSequence()
        .flatMap { project -> project.protocols.asSequence() }
        .firstOrNull { protocol -> protocol.id == protocolId }
}

internal fun List<ProjectTreeResponse>.findModule(
    moduleId: Long,
): ModuleTreeNode? {
    fun search(modules: List<ModuleTreeNode>): ModuleTreeNode? {
        modules.forEach { module ->
            if (module.id == moduleId) {
                return module
            }
        }
        return null
    }

    return asSequence().mapNotNull { project ->
        search(project.modules) ?: project.protocols.firstNotNullOfOrNull { protocol ->
            search(protocol.modules)
        }
    }.firstOrNull()
}

internal fun List<ProjectTreeResponse>.findDevice(
    deviceId: Long,
): DeviceTreeNode? {
    fun search(modules: List<ModuleTreeNode>): DeviceTreeNode? {
        modules.forEach { module ->
            module.devices.firstOrNull { device -> device.id == deviceId }?.let { device ->
                return device
            }
        }
        return null
    }

    return asSequence().mapNotNull { project ->
        search(project.modules) ?: project.protocols.firstNotNullOfOrNull { protocol ->
            search(protocol.modules)
        }
    }.firstOrNull()
}
