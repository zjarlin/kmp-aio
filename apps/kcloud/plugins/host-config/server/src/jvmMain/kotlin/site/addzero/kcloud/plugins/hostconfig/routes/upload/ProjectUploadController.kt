package site.addzero.kcloud.plugins.hostconfig.routes.upload

import org.koin.core.annotation.Single
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadOperationResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteAction
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRemoteActionRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectUploadRequest
import site.addzero.kcloud.plugins.hostconfig.service.ProjectConfigService

/**
 * 宿主配置里的上传与备份路由，负责上传状态和备份动作。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class ProjectUploadController(
    private val projectConfigService: ProjectConfigService,
) {
    /** 读取工程上传状态，供项目页工具条轮询展示。 */
    @GetMapping("/projects/{projectId}/upload-project")
    fun getProjectUploadStatus(
        @PathVariable projectId: Long,
    ): ProjectUploadOperationResponse = projectConfigService.getProjectUploadStatus(projectId)

    /** 接收上传工程请求，当前版本先持久化动作状态。 */
    @PostMapping("/projects/{projectId}/upload-project")
    fun uploadProject(
        @PathVariable projectId: Long,
        @RequestBody request: ProjectUploadRequest,
    ): ProjectUploadOperationResponse = projectConfigService.uploadProject(projectId, request)

    /** 触发上传相关远程动作，备份会同步生成可下载文件。 */
    @PostMapping("/projects/{projectId}/upload-project/actions/{action}")
    fun triggerProjectUploadRemoteAction(
        @PathVariable projectId: Long,
        @PathVariable action: ProjectUploadRemoteAction,
        @RequestBody request: ProjectUploadRemoteActionRequest,
    ): ProjectUploadOperationResponse =
        projectConfigService.triggerProjectUploadRemoteAction(projectId, action, request)

    /** 下载最近一次备份文件，供桌面端直接保存。 */
    @GetMapping("/projects/{projectId}/upload-project/backup")
    fun downloadProjectBackup(
        @PathVariable projectId: Long,
    ): ResponseEntity<Resource> = projectConfigService.downloadProjectBackup(projectId)
}
