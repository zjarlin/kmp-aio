package site.addzero.kcloud.plugins.system.configcenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.*

@Single
class ConfigCenterWorkbenchState(
    private val remoteService: ConfigCenterRemoteService,
) {
    val projects = mutableStateListOf<ConfigCenterProjectDto>()
    val environments = mutableStateListOf<ConfigCenterEnvironmentDto>()
    val configs = mutableStateListOf<ConfigCenterConfigDto>()
    val secrets = mutableStateListOf<ConfigCenterSecretDto>()
    val versions = mutableStateListOf<ConfigCenterSecretVersionDto>()
    val tokens = mutableStateListOf<ConfigCenterServiceTokenDto>()
    val activities = mutableStateListOf<ConfigCenterActivityLogDto>()

    var selectedProjectId by mutableStateOf<Long?>(null)
    var selectedEnvironmentId by mutableStateOf<Long?>(null)
    var selectedConfigId by mutableStateOf<Long?>(null)
    var selectedSecretId by mutableStateOf<Long?>(null)

    var projectSlug by mutableStateOf("")
    var projectName by mutableStateOf("")
    var projectDescription by mutableStateOf("")
    var projectEnabled by mutableStateOf(true)

    var environmentSlug by mutableStateOf("")
    var environmentName by mutableStateOf("")
    var environmentDescription by mutableStateOf("")
    var environmentSortOrderText by mutableStateOf("10")
    var environmentIsDefault by mutableStateOf(false)
    var environmentPersonalEnabled by mutableStateOf(true)

    var configSlug by mutableStateOf("")
    var configName by mutableStateOf("")
    var configDescription by mutableStateOf("")
    var configType by mutableStateOf(ConfigCenterConfigType.BRANCH)
    var configLocked by mutableStateOf(false)
    var configEnabled by mutableStateOf(true)

    var secretName by mutableStateOf("")
    var secretValue by mutableStateOf("")
    var secretNote by mutableStateOf("")
    var secretValueType by mutableStateOf(ConfigCenterValueType.STRING)
    var secretSensitive by mutableStateOf(true)
    var secretEnabled by mutableStateOf(true)
    var includeInheritedSecrets by mutableStateOf(true)

    var tokenName by mutableStateOf("")
    var tokenDescription by mutableStateOf("")
    var tokenWriteAccess by mutableStateOf(false)
    var tokenExpireTimeText by mutableStateOf("")
    var issuedTokenText by mutableStateOf("")

    var statusMessage by mutableStateOf("")
        private set
    var isBusy by mutableStateOf(false)
        private set

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refreshProjects()
        loaded = true
    }

    suspend fun refreshProjects() {
        runBusy("已刷新配置中心") {
            val loadedProjects = remoteService.listProjects()
            projects.resetWith(loadedProjects)
            val nextProjectId = selectedProjectId?.takeIf { currentId ->
                loadedProjects.any { project -> project.id == currentId }
            } ?: loadedProjects.firstOrNull()?.id
            if (nextProjectId == null) {
                clearProjectScope()
            } else {
                loadProjectScope(nextProjectId)
            }
        }
    }

    suspend fun selectProject(
        projectId: Long,
    ) {
        runBusy("已切换项目") {
            loadProjectScope(projectId)
        }
    }

    suspend fun selectConfig(
        configId: Long,
    ) {
        runBusy("已切换配置") {
            loadConfigScope(configId)
        }
    }

    suspend fun refreshProjectScope() {
        val projectId = selectedProjectId ?: return
        runBusy("已刷新项目数据") {
            loadProjectScope(projectId)
        }
    }

    suspend fun refreshSecrets() {
        val configId = selectedConfigId ?: return
        runBusy("已刷新 Secret 列表") {
            loadConfigScope(configId)
        }
    }

    suspend fun refreshActivities() {
        val projectId = selectedProjectId ?: return
        runBusy("已刷新审计记录") {
            activities.resetWith(remoteService.listActivities(projectId))
        }
    }

    suspend fun saveProject() {
        require(projectSlug.isNotBlank()) { "项目 slug 不能为空" }
        require(projectName.isNotBlank()) { "项目名称不能为空" }
        runBusy("已保存项目") {
            val request = ConfigCenterProjectMutationRequest(
                slug = projectSlug,
                name = projectName,
                description = projectDescription.ifBlank { null },
                enabled = projectEnabled,
            )
            val saved = if (selectedProjectId == null) {
                remoteService.createProject(request)
            } else {
                remoteService.updateProject(selectedProjectId!!, request)
            }
            refreshProjects()
            loadProjectScope(saved.id)
        }
    }

    suspend fun saveEnvironment() {
        val projectId = selectedProjectId ?: run {
            statusMessage = "请先选择项目"
            return
        }
        require(environmentSlug.isNotBlank()) { "环境 slug 不能为空" }
        require(environmentName.isNotBlank()) { "环境名称不能为空" }
        runBusy("已保存环境") {
            val request = ConfigCenterEnvironmentMutationRequest(
                slug = environmentSlug,
                name = environmentName,
                description = environmentDescription.ifBlank { null },
                sortOrder = environmentSortOrderText.toIntOrNull() ?: 10,
                isDefault = environmentIsDefault,
                personalConfigEnabled = environmentPersonalEnabled,
            )
            val saved = if (selectedEnvironmentId == null) {
                remoteService.createEnvironment(projectId, request)
            } else {
                remoteService.updateEnvironment(projectId, selectedEnvironmentId!!, request)
            }
            loadProjectScope(projectId)
            selectedEnvironmentId = saved.id
        }
    }

    suspend fun saveConfig() {
        val projectId = selectedProjectId ?: run {
            statusMessage = "请先选择项目"
            return
        }
        val environmentId = selectedEnvironmentId ?: run {
            statusMessage = "请先选择环境"
            return
        }
        require(configSlug.isNotBlank()) { "配置 slug 不能为空" }
        require(configName.isNotBlank()) { "配置名称不能为空" }
        runBusy("已保存配置") {
            val request = ConfigCenterConfigMutationRequest(
                environmentId = environmentId,
                slug = configSlug,
                name = configName,
                configType = configType,
                description = configDescription.ifBlank { null },
                locked = configLocked,
                enabled = configEnabled,
                sourceConfigId = rootConfigIdOfSelectedEnvironment(),
            )
            val saved = if (selectedConfigId == null) {
                remoteService.createConfig(projectId, request)
            } else {
                remoteService.updateConfig(selectedConfigId!!, request)
            }
            loadProjectScope(projectId)
            loadConfigScope(saved.id)
        }
    }

    suspend fun saveSecret() {
        val configId = selectedConfigId ?: run {
            statusMessage = "请先选择配置"
            return
        }
        require(secretName.isNotBlank()) { "Secret 名称不能为空" }
        runBusy("已保存 Secret") {
            val request = ConfigCenterSecretMutationRequest(
                configId = configId,
                name = secretName,
                value = secretValue,
                note = secretNote.ifBlank { null },
                valueType = secretValueType,
                sensitive = secretSensitive,
                enabled = secretEnabled,
                changeComment = secretNote.ifBlank { null },
            )
            val saved = if (selectedSecretId == null) {
                remoteService.createSecret(request)
            } else {
                remoteService.updateSecret(selectedSecretId!!, request)
            }
            loadConfigScope(configId)
            loadSecretScope(saved.id)
        }
    }

    suspend fun deleteSelectedSecret() {
        val secretId = selectedSecretId ?: return
        val configId = selectedConfigId ?: return
        runBusy("已删除 Secret") {
            remoteService.deleteSecret(secretId)
            selectedSecretId = null
            loadConfigScope(configId)
        }
    }

    suspend fun issueToken() {
        val configId = selectedConfigId ?: run {
            statusMessage = "请先选择配置"
            return
        }
        require(tokenName.isNotBlank()) { "令牌名称不能为空" }
        runBusy("已签发令牌") {
            val issued = remoteService.issueToken(
                ConfigCenterServiceTokenIssueRequest(
                    configId = configId,
                    name = tokenName,
                    description = tokenDescription.ifBlank { null },
                    writeAccess = tokenWriteAccess,
                    expireTimeMillis = tokenExpireTimeText.toLongOrNull(),
                ),
            )
            issuedTokenText = issued.plainTextToken
            tokenName = ""
            tokenDescription = ""
            tokenWriteAccess = false
            tokenExpireTimeText = ""
            tokens.resetWith(remoteService.listTokens(configId))
            selectedProjectId?.let { projectId ->
                activities.resetWith(remoteService.listActivities(projectId))
            }
        }
    }

    suspend fun revokeToken(
        tokenId: Long,
    ) {
        val configId = selectedConfigId ?: return
        runBusy("已吊销令牌") {
            remoteService.revokeToken(tokenId)
            tokens.resetWith(remoteService.listTokens(configId))
            selectedProjectId?.let { projectId ->
                activities.resetWith(remoteService.listActivities(projectId))
            }
        }
    }

    fun beginCreateProject() {
        selectedProjectId = null
        projectSlug = ""
        projectName = ""
        projectDescription = ""
        projectEnabled = true
    }

    fun beginCreateEnvironment() {
        selectedEnvironmentId = null
        environmentSlug = ""
        environmentName = ""
        environmentDescription = ""
        environmentSortOrderText = "10"
        environmentIsDefault = false
        environmentPersonalEnabled = true
    }

    fun beginCreateConfig() {
        selectedConfigId = null
        configSlug = ""
        configName = ""
        configDescription = ""
        configType = ConfigCenterConfigType.BRANCH
        configLocked = false
        configEnabled = true
        clearSecretScope()
    }

    fun beginCreateSecret() {
        selectedSecretId = null
        secretName = ""
        secretValue = ""
        secretNote = ""
        secretValueType = ConfigCenterValueType.STRING
        secretSensitive = true
        secretEnabled = true
        versions.clear()
    }

    suspend fun toggleInheritedSecrets(
        enabled: Boolean,
    ) {
        includeInheritedSecrets = enabled
        refreshSecrets()
    }

    private suspend fun loadProjectScope(
        projectId: Long,
    ) {
        selectedProjectId = projectId
        projects.firstOrNull { project -> project.id == projectId }?.let { project ->
            projectSlug = project.slug
            projectName = project.name
            projectDescription = project.description.orEmpty()
            projectEnabled = project.enabled
        }

        val loadedEnvironments = remoteService.listEnvironments(projectId)
        environments.resetWith(loadedEnvironments)
        val nextEnvironmentId = selectedEnvironmentId?.takeIf { currentId ->
            loadedEnvironments.any { environment -> environment.id == currentId }
        } ?: loadedEnvironments.firstOrNull()?.id
        if (nextEnvironmentId == null) {
            beginCreateEnvironment()
        } else {
            selectedEnvironmentId = nextEnvironmentId
            loadedEnvironments.firstOrNull { environment -> environment.id == nextEnvironmentId }?.let { environment ->
                environmentSlug = environment.slug
                environmentName = environment.name
                environmentDescription = environment.description.orEmpty()
                environmentSortOrderText = environment.sortOrder.toString()
                environmentIsDefault = environment.isDefault
                environmentPersonalEnabled = environment.personalConfigEnabled
            }
        }

        val loadedConfigs = remoteService.listConfigs(projectId)
        configs.resetWith(loadedConfigs)
        val nextConfigId = selectedConfigId?.takeIf { currentId ->
            loadedConfigs.any { config -> config.id == currentId }
        } ?: loadedConfigs.firstOrNull()?.id
        activities.resetWith(remoteService.listActivities(projectId))
        if (nextConfigId == null) {
            beginCreateConfig()
        } else {
            loadConfigScope(nextConfigId)
        }
    }

    private suspend fun loadConfigScope(
        configId: Long,
    ) {
        selectedConfigId = configId
        configs.firstOrNull { config -> config.id == configId }?.let { config ->
            selectedEnvironmentId = config.environmentId
            configSlug = config.slug
            configName = config.name
            configDescription = config.description.orEmpty()
            configType = config.configType
            configLocked = config.locked
            configEnabled = config.enabled
        }

        val loadedSecrets = remoteService.listSecrets(configId, includeInheritedSecrets)
        secrets.resetWith(loadedSecrets)
        tokens.resetWith(remoteService.listTokens(configId))
        val nextSecretId = selectedSecretId?.takeIf { currentId ->
            loadedSecrets.any { secret -> secret.id == currentId }
        } ?: loadedSecrets.firstOrNull()?.id
        if (nextSecretId == null) {
            beginCreateSecret()
        } else {
            loadSecretScope(nextSecretId)
        }
    }

    private suspend fun loadSecretScope(
        secretId: Long,
    ) {
        selectedSecretId = secretId
        val secret = secrets.firstOrNull { item -> item.id == secretId } ?: run {
            beginCreateSecret()
            return
        }
        secretName = secret.name
        secretValue = secret.value
        secretNote = secret.note.orEmpty()
        secretValueType = secret.valueType
        secretSensitive = secret.sensitive
        secretEnabled = secret.enabled
        versions.resetWith(remoteService.listSecretVersions(secretId))
    }

    private fun clearProjectScope() {
        beginCreateProject()
        environments.clear()
        configs.clear()
        activities.clear()
        clearConfigScope()
    }

    private fun clearConfigScope() {
        beginCreateEnvironment()
        beginCreateConfig()
        clearSecretScope()
        tokens.clear()
    }

    private fun clearSecretScope() {
        beginCreateSecret()
        secrets.clear()
        versions.clear()
        issuedTokenText = ""
    }

    private fun rootConfigIdOfSelectedEnvironment(): Long? {
        return environments.firstOrNull { environment ->
            environment.id == selectedEnvironmentId
        }?.rootConfigId
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            statusMessage = successMessage
        }.onFailure { throwable ->
            statusMessage = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}

private fun <T> MutableList<T>.resetWith(
    newItems: List<T>,
) {
    clear()
    addAll(newItems)
}
