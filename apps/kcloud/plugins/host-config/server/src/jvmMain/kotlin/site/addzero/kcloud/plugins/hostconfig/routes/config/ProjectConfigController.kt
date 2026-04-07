package site.addzero.kcloud.plugins.hostconfig.api.config

import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType
import site.addzero.kcloud.plugins.hostconfig.service.ProjectConfigService

@RestController
@RequestMapping("/api/v1")
class ProjectConfigController(
    private val projectConfigService: ProjectConfigService,
) {

    @GetMapping("/projects/{projectId}/mqtt-config")
    fun getMqttConfig(@PathVariable projectId: Long): ProjectMqttConfigResponse =
        projectConfigService.getMqttConfig(projectId)

    @PutMapping("/projects/{projectId}/mqtt-config")
    fun updateMqttConfig(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectMqttConfigRequest,
    ): ProjectMqttConfigResponse = projectConfigService.updateMqttConfig(projectId, request)

    @GetMapping("/projects/{projectId}/modbus-servers/{transportType}")
    fun getModbusServerConfig(
        @PathVariable projectId: Long,
        @PathVariable transportType: TransportType,
    ): ProjectModbusServerConfigResponse = projectConfigService.getModbusServerConfig(projectId, transportType)

    @PutMapping("/projects/{projectId}/modbus-servers/{transportType}")
    fun updateModbusServerConfig(
        @PathVariable projectId: Long,
        @PathVariable transportType: TransportType,
        @Valid @RequestBody request: ProjectModbusServerConfigRequest,
    ): ProjectModbusServerConfigResponse =
        projectConfigService.updateModbusServerConfig(projectId, transportType, request)

    @GetMapping("/projects/{projectId}/upload-project")
    fun getProjectUploadStatus(@PathVariable projectId: Long): ProjectUploadOperationResponse =
        projectConfigService.getProjectUploadStatus(projectId)

    @PostMapping("/projects/{projectId}/upload-project")
    fun uploadProject(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectUploadRequest,
    ): ProjectUploadOperationResponse = projectConfigService.uploadProject(projectId, request)

    @PostMapping("/projects/{projectId}/upload-project/actions/{action}")
    fun triggerProjectUploadRemoteAction(
        @PathVariable projectId: Long,
        @PathVariable action: ProjectUploadRemoteAction,
        @Valid @RequestBody request: ProjectUploadRemoteActionRequest,
    ): ProjectUploadOperationResponse =
        projectConfigService.triggerProjectUploadRemoteAction(projectId, action, request)

    @GetMapping("/projects/{projectId}/upload-project/backup")
    fun downloadProjectBackup(@PathVariable projectId: Long): ResponseEntity<Resource> =
        projectConfigService.downloadProjectBackup(projectId)
}
