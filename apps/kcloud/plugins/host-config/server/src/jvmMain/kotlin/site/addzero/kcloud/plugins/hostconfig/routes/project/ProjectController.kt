package site.addzero.kcloud.plugins.hostconfig.routes.project

import org.koin.core.annotation.Single
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DevicePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.LinkExistingProtocolRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModulePositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCatalogItemResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.service.ProjectService

/**
 * 宿主配置里的工程树、协议、模块与设备 CRUD 路由。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class ProjectController(
    private val projectService: ProjectService,
) {
    /** 返回工程列表，供工程页左侧入口初始化使用。 */
    @GetMapping("/projects")
    fun listProjects(): List<ProjectResponse> = projectService.listProjects()

    /** 创建工程节点，保持旧宿主配置的基础工程建模能力。 */
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProject(
        @RequestBody request: ProjectCreateRequest,
    ): ProjectResponse = projectService.createProject(request)

    /** 读取单个工程详情，用于右侧详情面板回填。 */
    @GetMapping("/projects/{projectId}")
    fun getProject(
        @PathVariable projectId: Long,
    ): ProjectResponse = projectService.getProject(projectId)

    /** 更新工程基本信息，不额外拆出独立表单 DTO。 */
    @PutMapping("/projects/{projectId}")
    fun updateProject(
        @PathVariable projectId: Long,
        @RequestBody request: ProjectUpdateRequest,
    ): ProjectResponse = projectService.updateProject(projectId, request)

    /** 调整工程排序，支撑树形拖拽后的稳定顺序落库。 */
    @PutMapping("/projects/{projectId}/position")
    fun updateProjectPosition(
        @PathVariable projectId: Long,
        @RequestBody request: ProjectPositionUpdateRequest,
    ): ProjectResponse = projectService.updateProjectPosition(projectId, request)

    /** 删除工程，并级联清理其挂接的协议关系。 */
    @DeleteMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProject(
        @PathVariable projectId: Long,
    ) {
        projectService.deleteProject(projectId)
    }

    /** 加载工程树，用于项目页的完整层级渲染。 */
    @GetMapping("/projects/{projectId}/tree")
    fun getProjectTree(
        @PathVariable projectId: Long,
    ): ProjectTreeResponse = projectService.getProjectTree(projectId)

    /** 返回可复用协议实例目录，供工程内挂接已有协议。 */
    @GetMapping("/protocols")
    fun listProtocols(): List<ProtocolCatalogItemResponse> = projectService.listProtocols()

    /** 在指定工程下创建协议实例并建立挂接关系。 */
    @PostMapping("/projects/{projectId}/protocols")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProtocol(
        @PathVariable projectId: Long,
        @RequestBody request: ProtocolCreateRequest,
    ): ProtocolResponse = projectService.createProtocol(projectId, request)

    /** 把已有协议实例挂接到目标工程，避免重复录入。 */
    @PostMapping("/projects/{projectId}/protocol-links")
    @ResponseStatus(HttpStatus.CREATED)
    fun linkProtocol(
        @PathVariable projectId: Long,
        @RequestBody request: LinkExistingProtocolRequest,
    ): ProtocolResponse = projectService.linkProtocol(projectId, request)

    /** 修改协议实例信息，同时允许调整模板与轮询参数。 */
    @PutMapping("/protocols/{protocolId}")
    fun updateProtocol(
        @PathVariable protocolId: Long,
        @RequestBody request: ProtocolUpdateRequest,
    ): ProtocolResponse = projectService.updateProtocol(protocolId, request)

    /** 在工程之间移动协议，或在同一工程内重排协议顺序。 */
    @PutMapping("/protocols/{protocolId}/position")
    fun updateProtocolPosition(
        @PathVariable protocolId: Long,
        @RequestBody request: ProtocolPositionUpdateRequest,
    ): ProtocolResponse = projectService.updateProtocolPosition(protocolId, request)

    /** 删除工程里的协议挂接，并在协议孤立时清理其主体数据。 */
    @DeleteMapping("/projects/{projectId}/protocols/{protocolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProtocol(
        @PathVariable projectId: Long,
        @PathVariable protocolId: Long,
    ) {
        projectService.deleteProtocol(projectId, protocolId)
    }

    /** 在设备下创建模块。 */
    @PostMapping("/devices/{deviceId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createModule(
        @PathVariable deviceId: Long,
        @RequestBody request: ModuleCreateRequest,
    ): ModuleResponse = projectService.createModule(deviceId, request)

    /** 更新模块通讯参数与模板映射。 */
    @PutMapping("/modules/{moduleId}")
    fun updateModule(
        @PathVariable moduleId: Long,
        @RequestBody request: ModuleUpdateRequest,
    ): ModuleResponse = projectService.updateModule(moduleId, request)

    /** 调整模块在设备内的顺序或移动到其他设备。 */
    @PutMapping("/modules/{moduleId}/position")
    fun updateModulePosition(
        @PathVariable moduleId: Long,
        @RequestBody request: ModulePositionUpdateRequest,
    ): ModuleResponse = projectService.updateModulePosition(moduleId, request)

    /** 删除模块，并依赖数据库级联清理设备与点位。 */
    @DeleteMapping("/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteModule(
        @PathVariable moduleId: Long,
    ) {
        projectService.deleteModule(moduleId)
    }

    /** 在协议下创建设备节点。 */
    @PostMapping("/protocols/{protocolId}/devices")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(
        @PathVariable protocolId: Long,
        @RequestBody request: DeviceCreateRequest,
    ): DeviceResponse = projectService.createDevice(protocolId, request)

    /** 更新设备参数，保持与旧宿主配置的字段兼容。 */
    @PutMapping("/devices/{deviceId}")
    fun updateDevice(
        @PathVariable deviceId: Long,
        @RequestBody request: DeviceUpdateRequest,
    ): DeviceResponse = projectService.updateDevice(deviceId, request)

    /** 调整设备在协议内的顺序或移动到其他协议。 */
    @PutMapping("/devices/{deviceId}/position")
    fun updateDevicePosition(
        @PathVariable deviceId: Long,
        @RequestBody request: DevicePositionUpdateRequest,
    ): DeviceResponse = projectService.updateDevicePosition(deviceId, request)

    /** 删除设备，让数据库级联清理关联点位。 */
    @DeleteMapping("/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(
        @PathVariable deviceId: Long,
    ) {
        projectService.deleteDevice(deviceId)
    }
}
