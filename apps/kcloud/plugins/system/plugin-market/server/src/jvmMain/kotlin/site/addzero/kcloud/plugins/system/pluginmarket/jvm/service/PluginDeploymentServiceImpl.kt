package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.DeployPluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentStatus
import site.addzero.kcloud.plugins.system.pluginmarket.model.RunPluginBuildRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginDeploymentService
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginMarketConfigService
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Single(
    binds = [
        PluginDeploymentService::class,
    ],
)
class PluginDeploymentServiceImpl(
    private val sqlClient: KSqlClient,
    private val catalog: PluginMarketCatalogSupport,
    private val workspaceSupport: PluginPackageWorkspaceSupport,
    private val managedIntegrationSupport: PluginManagedIntegrationSupport,
    private val configService: PluginMarketConfigService,
    private val commandRunner: PluginBuildCommandRunner,
) : PluginDeploymentService {
    override suspend fun deploy(request: DeployPluginPackageRequest): PluginDeploymentJobDto {
        val pluginPackage = catalog.packageOrThrow(request.packageId)
        val config = configService.read()
        val moduleRoot = resolveModuleRoot(pluginPackage.moduleDir, config.exportRootDir)
        val files = catalog.listFiles(pluginPackage.id)
        val now = catalog.now()

        moduleRoot.createDirectories()
        files.forEach { file ->
            val targetFile = moduleRoot.resolve(file.relativePath)
            targetFile.parent?.createDirectories()
            targetFile.writeText(file.content)
        }
        workspaceSupport.syncEnabledMarker(pluginPackage)
        val integration = managedIntegrationSupport.renderManagedBlocks(
            catalog.listPackages().filter { it.enabled },
        )
        val integrationResult = managedIntegrationSupport.updateManagedIntegrationFiles(integration)

        val buildRequested = request.runBuild ?: config.autoBuildEnabled
        val status = if (buildRequested) PluginDeploymentStatus.BUILDING else PluginDeploymentStatus.EXPORTED
        val job = new(PluginDeploymentJob::class).by {
            id = catalog.newId()
            this.pluginPackage = catalog.packageRef(pluginPackage.id)
            this.status = status.name
            exportedModuleDir = moduleRoot.toString()
            buildCommand = null
            stdoutText = null
            stderrText = null
            summaryText = buildString {
                appendLine("已导出 ${files.size} 个文件到 $moduleRoot")
                appendLine()
                append(integrationResult.diffText)
            }
            createdAt = now
            updatedAt = now
        }
        val savedJob = sqlClient.save(job).modifiedEntity
        files.forEach { file ->
            sqlClient.save(
                new(PluginDeploymentArtifact::class).by {
                    id = catalog.newId()
                    this.deploymentJob = catalog.jobRef(savedJob.id)
                    relativePath = file.relativePath
                    absolutePath = moduleRoot.resolve(file.relativePath).toString()
                    contentHash = file.contentHash
                    createdAt = now
                },
            )
        }
        if (buildRequested) {
            return runBuild(RunPluginBuildRequest(pluginPackage.id))
        }
        return savedJob.toDto()
    }

    override suspend fun runBuild(request: RunPluginBuildRequest): PluginDeploymentJobDto {
        val pluginPackage = catalog.packageOrThrow(request.packageId)
        val config = configService.read()
        val command = buildString {
            append(config.gradleCommand)
            if (config.gradleTasks.isNotEmpty()) {
                append(" ")
                append(config.gradleTasks.joinToString(" "))
            }
        }
        val result = commandRunner.run(
            commandLine = command,
            workingDirectory = Paths.get(".").toAbsolutePath().normalize().toFile(),
            environment = parseEnvironmentLines(config.environmentLines),
            javaHome = config.javaHome,
        )
        val now = catalog.now()
        val job = new(PluginDeploymentJob::class).by {
            id = catalog.newId()
            this.pluginPackage = catalog.packageRef(pluginPackage.id)
            status = if (result.exitCode == 0) {
                PluginDeploymentStatus.RESTART_REQUIRED.name
            } else {
                PluginDeploymentStatus.FAILED.name
            }
            exportedModuleDir = pluginPackage.moduleDir
            buildCommand = result.command
            stdoutText = result.stdout
            stderrText = result.stderr
            summaryText = if (result.exitCode == 0) {
                "Gradle 构建完成，等待重启 KCloud 生效"
            } else {
                "Gradle 构建失败，退出码 ${result.exitCode}"
            }
            createdAt = now
            updatedAt = now
        }
        return sqlClient.save(job).modifiedEntity.toDto()
    }

    override suspend fun listJobs(packageId: String?): List<PluginDeploymentJobDto> {
        return catalog.listJobs(packageId).map { it.toDto() }
    }

    private fun resolveModuleRoot(moduleDir: String, exportRootDir: String): Path {
        val modulePath = Paths.get(moduleDir)
        if (modulePath.isAbsolute) {
            return modulePath
        }
        val exportRoot = Paths.get(exportRootDir)
        if (moduleDir.startsWith("apps/")) {
            return Paths.get(moduleDir)
        }
        return exportRoot.resolve(moduleDir.substringAfterLast("plugins/"))
    }

    private fun parseEnvironmentLines(lines: List<String>): Map<String, String> {
        return lines.mapNotNull { line ->
            val index = line.indexOf("=")
            if (index <= 0) {
                null
            } else {
                line.substring(0, index).trim() to line.substring(index + 1).trim()
            }
        }.toMap()
    }
}
