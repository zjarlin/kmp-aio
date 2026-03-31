package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentArtifact
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentJob
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginImportRecord
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPackage
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPresetBinding
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginSourceFile
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentArtifactDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentStatus
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginImportRecordDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetBindingDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto

@Single
class PluginPackageAggregateSupport(
    private val catalog: PluginMarketCatalogSupport,
    private val workspace: PluginPackageWorkspaceSupport,
) {
    fun packageDto(pluginPackage: PluginPackage): PluginPackageDto {
        return PluginPackageDto(
            id = pluginPackage.id,
            pluginId = pluginPackage.pluginId,
            name = pluginPackage.name,
            pluginGroup = pluginPackage.pluginGroup,
            description = pluginPackage.description,
            version = pluginPackage.version,
            enabled = pluginPackage.enabled,
            moduleDir = pluginPackage.moduleDir,
            basePackage = pluginPackage.basePackage,
            managedByDb = pluginPackage.managedByDb,
            composeKoinModuleClass = pluginPackage.composeKoinModuleClass,
            serverKoinModuleClass = pluginPackage.serverKoinModuleClass,
            routeRegistrarImport = pluginPackage.routeRegistrarImport,
            routeRegistrarCall = pluginPackage.routeRegistrarCall,
            activationState = workspace.activationStateOf(pluginPackage),
            moduleInstalled = workspace.isModuleInstalled(pluginPackage),
            disabledByMarker = workspace.isDisabledByMarker(pluginPackage),
            createdAt = pluginPackage.createdAt.toString(),
            updatedAt = pluginPackage.updatedAt.toString(),
        )
    }

    fun buildAggregate(packageId: String): PluginPackageAggregateDto {
        val pluginPackage = catalog.packageOrThrow(packageId)
        return PluginPackageAggregateDto(
            pluginPackage = packageDto(pluginPackage),
            files = catalog.listFiles(packageId).map { it.toDto() },
            presetBindings = catalog.listPresetBindings(packageId).map { it.toDto() },
            jobs = catalog.listJobs(packageId).map { it.toDto() },
            artifacts = catalog.listArtifacts(packageId = packageId).map { it.toDto() },
            importRecords = catalog.listImportRecords(packageId).map { it.toDto() },
        )
    }
}

internal fun PluginSourceFile.toDto(): PluginSourceFileDto {
    return PluginSourceFileDto(
        id = id,
        packageId = pluginPackage.id,
        relativePath = relativePath,
        content = content,
        contentHash = contentHash,
        fileGroup = fileGroup,
        readOnly = readOnly,
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

internal fun PluginPresetBinding.toDto(): PluginPresetBindingDto {
    return PluginPresetBindingDto(
        id = id,
        packageId = pluginPackage.id,
        presetKind = PluginPresetKind.valueOf(presetKind),
        appliedAt = appliedAt.toString(),
    )
}

internal fun PluginDeploymentJob.toDto(): PluginDeploymentJobDto {
    return PluginDeploymentJobDto(
        id = id,
        packageId = pluginPackage.id,
        status = PluginDeploymentStatus.valueOf(status),
        exportedModuleDir = exportedModuleDir,
        buildCommand = buildCommand,
        stdoutText = stdoutText,
        stderrText = stderrText,
        summaryText = summaryText,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

internal fun PluginDeploymentArtifact.toDto(): PluginDeploymentArtifactDto {
    return PluginDeploymentArtifactDto(
        id = id,
        jobId = deploymentJob.id,
        relativePath = relativePath,
        absolutePath = absolutePath,
        contentHash = contentHash,
        createdAt = createdAt.toString(),
    )
}

internal fun PluginImportRecord.toDto(): PluginImportRecordDto {
    return PluginImportRecordDto(
        id = id,
        packageId = pluginPackage.id,
        sourceModuleDir = sourceModuleDir,
        sourceGradlePath = sourceGradlePath,
        importedAt = importedAt.toString(),
    )
}
