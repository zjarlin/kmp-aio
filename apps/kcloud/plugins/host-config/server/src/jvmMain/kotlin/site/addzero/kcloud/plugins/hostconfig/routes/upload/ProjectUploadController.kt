package site.addzero.kcloud.plugins.hostconfig.routes.upload

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteFileResponse
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectSqliteImportRequest
import site.addzero.kcloud.plugins.hostconfig.service.ProjectConfigService

/**
 * 宿主配置里的工程 sqlite 传输路由。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class ProjectUploadController(
    private val projectConfigService: ProjectConfigService,
) {
    /** 导出当前工程为 sqlite 文件，并落到本地数据目录。 */
    @PostMapping("/projects/{projectId}/project-sqlite/export")
    fun exportProjectSqlite(
        @PathVariable projectId: Long,
    ): ProjectSqliteFileResponse = projectConfigService.exportProjectSqlite(projectId)

    /** 导入本地 sqlite 工程文件到数据目录。 */
    @PostMapping("/project-sqlite/import")
    fun importProjectSqlite(
        @RequestBody request: ProjectSqliteImportRequest,
    ): ProjectSqliteFileResponse = projectConfigService.importProjectSqlite(request)
}
