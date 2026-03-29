package site.addzero.kcloud.plugins.system.pluginmarket.model

import kotlinx.serialization.Serializable

@Serializable
enum class PluginPresetKind {
    BLANK,
    TOOL,
    ADMIN,
}

@Serializable
enum class PluginDeploymentStatus {
    PENDING,
    EXPORTED,
    BUILDING,
    SUCCESS,
    FAILED,
    RESTART_REQUIRED,
}

@Serializable
enum class PluginActivationState {
    NOT_INSTALLED,
    ENABLED,
    DISABLED,
}

@Serializable
data class PluginPackageDto(
    val id: String,
    val pluginId: String,
    val name: String,
    val pluginGroup: String? = null,
    val description: String? = null,
    val version: String = "0.1.0",
    val enabled: Boolean = true,
    val moduleDir: String,
    val basePackage: String,
    val managedByDb: Boolean = true,
    val composeKoinModuleClass: String? = null,
    val serverKoinModuleClass: String? = null,
    val routeRegistrarImport: String? = null,
    val routeRegistrarCall: String? = null,
    val activationState: PluginActivationState = PluginActivationState.NOT_INSTALLED,
    val moduleInstalled: Boolean = false,
    val disabledByMarker: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginSourceFileDto(
    val id: String,
    val packageId: String,
    val relativePath: String,
    val content: String,
    val contentHash: String,
    val fileGroup: String,
    val readOnly: Boolean = false,
    val orderIndex: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginPresetBindingDto(
    val id: String,
    val packageId: String,
    val presetKind: PluginPresetKind,
    val appliedAt: String,
)

@Serializable
data class PluginDeploymentArtifactDto(
    val id: String,
    val jobId: String,
    val relativePath: String,
    val absolutePath: String,
    val contentHash: String,
    val createdAt: String,
)

@Serializable
data class PluginDeploymentJobDto(
    val id: String,
    val packageId: String,
    val status: PluginDeploymentStatus,
    val exportedModuleDir: String,
    val buildCommand: String? = null,
    val stdoutText: String? = null,
    val stderrText: String? = null,
    val summaryText: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginImportRecordDto(
    val id: String,
    val packageId: String,
    val sourceModuleDir: String,
    val sourceGradlePath: String,
    val importedAt: String,
)

@Serializable
data class PluginPackageAggregateDto(
    val pluginPackage: PluginPackageDto,
    val files: List<PluginSourceFileDto> = emptyList(),
    val presetBindings: List<PluginPresetBindingDto> = emptyList(),
    val jobs: List<PluginDeploymentJobDto> = emptyList(),
    val artifacts: List<PluginDeploymentArtifactDto> = emptyList(),
    val importRecords: List<PluginImportRecordDto> = emptyList(),
)

@Serializable
data class PluginDiscoveryItemDto(
    val discoveryId: String,
    val pluginId: String,
    val pluginGroup: String? = null,
    val moduleDir: String,
    val gradlePath: String,
    val packageName: String? = null,
    val composeKoinModuleClass: String? = null,
    val serverKoinModuleClass: String? = null,
    val routeRegistrarImport: String? = null,
    val routeRegistrarCall: String? = null,
    val managedByDb: Boolean = false,
    val issues: List<String> = emptyList(),
)

@Serializable
data class PluginMarketConfigDto(
    val exportRootDir: String = "apps/kcloud/plugins",
    val gradleCommand: String = "./gradlew",
    val gradleTasks: List<String> = emptyList(),
    val javaHome: String? = null,
    val environmentLines: List<String> = emptyList(),
    val autoBuildEnabled: Boolean = false,
)

@Serializable
data class PluginDeleteCheckResultDto(
    val id: String,
    val canDelete: Boolean,
    val warnings: List<String> = emptyList(),
    val blockers: List<String> = emptyList(),
)

@Serializable
data class PluginMarketSearchRequest(
    val query: String? = null,
    val managedOnly: Boolean = false,
)

@Serializable
data class CreatePluginPackageRequest(
    val pluginId: String,
    val name: String,
    val pluginGroup: String? = null,
    val description: String? = null,
    val version: String = "0.1.0",
    val basePackage: String,
    val moduleDir: String? = null,
    val enabled: Boolean = true,
    val presetKind: PluginPresetKind = PluginPresetKind.BLANK,
    val composeKoinModuleClass: String? = null,
    val serverKoinModuleClass: String? = null,
    val routeRegistrarImport: String? = null,
    val routeRegistrarCall: String? = null,
)

@Serializable
data class UpdatePluginPackageRequest(
    val name: String,
    val pluginGroup: String? = null,
    val description: String? = null,
    val version: String = "0.1.0",
    val basePackage: String,
    val moduleDir: String,
    val enabled: Boolean = true,
    val composeKoinModuleClass: String? = null,
    val serverKoinModuleClass: String? = null,
    val routeRegistrarImport: String? = null,
    val routeRegistrarCall: String? = null,
)

@Serializable
data class SavePluginSourceFileRequest(
    val packageId: String,
    val relativePath: String,
    val content: String,
    val fileGroup: String = "source",
)

@Serializable
data class ImportDiscoveredPluginRequest(
    val discoveryId: String,
    val managedPluginId: String,
    val managedName: String,
)

@Serializable
data class DeployPluginPackageRequest(
    val packageId: String,
    val runBuild: Boolean? = null,
)

@Serializable
data class RunPluginBuildRequest(
    val packageId: String,
)

@Serializable
data class UpdatePluginMarketConfigRequest(
    val exportRootDir: String,
    val gradleCommand: String,
    val gradleTasks: List<String> = emptyList(),
    val javaHome: String? = null,
    val environmentLines: List<String> = emptyList(),
    val autoBuildEnabled: Boolean = false,
)
