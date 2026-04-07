package site.addzero.kcloud.plugins.hostconfig.api.project

import jakarta.validation.Valid
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
import site.addzero.kcloud.plugins.hostconfig.service.ProjectService

@RestController
@RequestMapping("/api/v1")
class ProjectController(
    private val projectService: ProjectService,
) {

    @GetMapping("/projects")
    fun listProjects(): List<ProjectResponse> = projectService.listProjects()

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProject(@Valid @RequestBody request: ProjectCreateRequest): ProjectResponse =
        projectService.createProject(request)

    @GetMapping("/projects/{projectId}")
    fun getProject(@PathVariable projectId: Long): ProjectResponse =
        projectService.getProject(projectId)

    @PutMapping("/projects/{projectId}")
    fun updateProject(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectUpdateRequest,
    ): ProjectResponse = projectService.updateProject(projectId, request)

    @PutMapping("/projects/{projectId}/position")
    fun updateProjectPosition(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectPositionUpdateRequest,
    ): ProjectResponse = projectService.updateProjectPosition(projectId, request)

    @DeleteMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable projectId: Long) {
        projectService.deleteProject(projectId)
    }

    @GetMapping("/projects/{projectId}/tree")
    fun getProjectTree(@PathVariable projectId: Long): ProjectTreeResponse =
        projectService.getProjectTree(projectId)

    @GetMapping("/protocols")
    fun listProtocols(): List<ProtocolCatalogItemResponse> =
        projectService.listProtocols()

    @PostMapping("/projects/{projectId}/protocols")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProtocol(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProtocolCreateRequest,
    ): ProtocolResponse = projectService.createProtocol(projectId, request)

    @PostMapping("/projects/{projectId}/protocol-links")
    @ResponseStatus(HttpStatus.CREATED)
    fun linkProtocol(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: LinkExistingProtocolRequest,
    ): ProtocolResponse = projectService.linkProtocol(projectId, request)

    @PutMapping("/protocols/{protocolId}")
    fun updateProtocol(
        @PathVariable protocolId: Long,
        @Valid @RequestBody request: ProtocolUpdateRequest,
    ): ProtocolResponse = projectService.updateProtocol(protocolId, request)

    @PutMapping("/protocols/{protocolId}/position")
    fun updateProtocolPosition(
        @PathVariable protocolId: Long,
        @Valid @RequestBody request: ProtocolPositionUpdateRequest,
    ): ProtocolResponse = projectService.updateProtocolPosition(protocolId, request)

    @DeleteMapping("/projects/{projectId}/protocols/{protocolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProtocol(
        @PathVariable projectId: Long,
        @PathVariable protocolId: Long,
    ) {
        projectService.deleteProtocol(projectId, protocolId)
    }

    @PostMapping("/protocols/{protocolId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createModule(
        @PathVariable protocolId: Long,
        @Valid @RequestBody request: ModuleCreateRequest,
    ): ModuleResponse = projectService.createModule(protocolId, request)

    @PostMapping("/projects/{projectId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProjectModule(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ModuleCreateRequest,
    ): ModuleResponse = projectService.createProjectModule(projectId, request)

    @PutMapping("/modules/{moduleId}")
    fun updateModule(
        @PathVariable moduleId: Long,
        @Valid @RequestBody request: ModuleUpdateRequest,
    ): ModuleResponse = projectService.updateModule(moduleId, request)

    @PutMapping("/modules/{moduleId}/position")
    fun updateModulePosition(
        @PathVariable moduleId: Long,
        @Valid @RequestBody request: ModulePositionUpdateRequest,
    ): ModuleResponse = projectService.updateModulePosition(moduleId, request)

    @DeleteMapping("/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteModule(@PathVariable moduleId: Long) {
        projectService.deleteModule(moduleId)
    }

    @PostMapping("/modules/{moduleId}/devices")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(
        @PathVariable moduleId: Long,
        @Valid @RequestBody request: DeviceCreateRequest,
    ): DeviceResponse = projectService.createDevice(moduleId, request)

    @PutMapping("/devices/{deviceId}")
    fun updateDevice(
        @PathVariable deviceId: Long,
        @Valid @RequestBody request: DeviceUpdateRequest,
    ): DeviceResponse = projectService.updateDevice(deviceId, request)

    @PutMapping("/devices/{deviceId}/position")
    fun updateDevicePosition(
        @PathVariable deviceId: Long,
        @Valid @RequestBody request: DevicePositionUpdateRequest,
    ): DeviceResponse = projectService.updateDevicePosition(deviceId, request)

    @DeleteMapping("/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(@PathVariable deviceId: Long) {
        projectService.deleteDevice(deviceId)
    }
}
