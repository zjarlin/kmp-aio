package site.addzero.kcloud.plugins.system.configcenter

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.*
import site.addzero.kcloud.plugins.system.configcenter.model.*
import site.addzero.kcloud.plugins.system.configcenter.model.by
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Single
class ConfigCenterService(
    private val sqlClient: KSqlClient,
) {
    fun listProjects(): List<ConfigCenterProjectDto> {
        return allProjects()
            .sortedBy { it.slug }
            .map { project ->
                project.toDto(
                    environments = environmentsOfProject(project.id),
                    configs = configsOfProject(project.id),
                    secrets = secretsOfProject(project.id),
                )
            }
    }

    fun createProject(
        request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto {
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "项目 slug 不能为空" }
        require(request.name.isNotBlank()) { "项目名称不能为空" }
        require(allProjects().none { it.slug == slug }) { "项目 slug 已存在: $slug" }

        val saved = sqlClient.save(
            new(ConfigCenterProject::class).by {
                projectKey = UUID.randomUUID().toString()
                this.slug = slug
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                enabled = request.enabled
            },
        ).modifiedEntity

        seedDefaultEnvironments(saved)
        logActivity(
            project = saved,
            config = null,
            action = ConfigCenterActivityType.PROJECT_CREATED,
            resourceType = "project",
            resourceKey = saved.slug,
            summary = "创建项目 ${saved.name}",
        )
        return projectOrThrow(saved.id).toDto(
            environments = environmentsOfProject(saved.id),
            configs = configsOfProject(saved.id),
            secrets = secretsOfProject(saved.id),
        )
    }

    fun updateProject(
        projectId: Long,
        request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto {
        val existing = projectOrThrow(projectId)
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "项目 slug 不能为空" }
        require(request.name.isNotBlank()) { "项目名称不能为空" }
        require(
            allProjects().none { project -> project.id != projectId && project.slug == slug },
        ) { "项目 slug 已存在: $slug" }

        val saved = sqlClient.save(
            new(ConfigCenterProject::class).by {
                id = existing.id
                projectKey = existing.projectKey
                this.slug = slug
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                enabled = request.enabled
                createTime = existing.createTime
            },
        ).modifiedEntity

        logActivity(
            project = saved,
            config = null,
            action = ConfigCenterActivityType.PROJECT_UPDATED,
            resourceType = "project",
            resourceKey = saved.slug,
            summary = "更新项目 ${saved.name}",
        )
        return saved.toDto(
            environments = environmentsOfProject(saved.id),
            configs = configsOfProject(saved.id),
            secrets = secretsOfProject(saved.id),
        )
    }

    fun listEnvironments(
        projectId: Long,
    ): List<ConfigCenterEnvironmentDto> {
        projectOrThrow(projectId)
        val configs = configsOfProject(projectId)
        val rootsByEnvironmentId = configs.filter { config ->
            config.configType.asConfigType() == ConfigCenterConfigType.ROOT
        }.associateBy { config -> config.environment.id }
        val globalRootSecretNames = rootsByEnvironmentId.values
            .flatMap { config -> localActiveSecrets(config.id) }
            .map { secret -> secret.name }
            .toSet()

        return environmentsOfProject(projectId)
            .sortedWith(compareBy<ConfigCenterEnvironment> { it.sortOrder }.thenBy { it.slug })
            .map { environment ->
                val rootConfig = rootsByEnvironmentId[environment.id]
                val localSecretNames = rootConfig
                    ?.let { config -> localActiveSecrets(config.id).map { secret -> secret.name }.toSet() }
                    ?: emptySet()
                environment.toDto(
                    rootConfig = rootConfig,
                    missingSecretCount = globalRootSecretNames.count { key -> key !in localSecretNames },
                )
            }
    }

    fun createEnvironment(
        projectId: Long,
        request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto {
        val project = projectOrThrow(projectId)
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "环境 slug 不能为空" }
        require(request.name.isNotBlank()) { "环境名称不能为空" }
        require(
            environmentsOfProject(projectId).none { environment -> environment.slug == slug },
        ) { "环境 slug 已存在: $slug" }

        val saved = sqlClient.save(
            new(ConfigCenterEnvironment::class).by {
                environmentKey = UUID.randomUUID().toString()
                this.project = projectRef(project.id)
                this.slug = slug
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                sortOrder = request.sortOrder
                isDefault = request.isDefault
                personalConfigEnabled = request.personalConfigEnabled
            },
        ).modifiedEntity

        createRootConfig(project = project, environment = saved)
        logActivity(
            project = project,
            config = null,
            action = ConfigCenterActivityType.ENVIRONMENT_CREATED,
            resourceType = "environment",
            resourceKey = saved.slug,
            summary = "创建环境 ${saved.name}",
        )
        return saved.toDto(
            rootConfig = rootConfigOfEnvironment(saved.id),
            missingSecretCount = 0,
        )
    }

    fun updateEnvironment(
        projectId: Long,
        environmentId: Long,
        request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto {
        val project = projectOrThrow(projectId)
        val existing = environmentOrThrow(environmentId)
        require(existing.project.id == project.id) { "环境不属于当前项目" }
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "环境 slug 不能为空" }
        require(request.name.isNotBlank()) { "环境名称不能为空" }
        require(
            environmentsOfProject(projectId).none { environment ->
                environment.id != environmentId && environment.slug == slug
            },
        ) { "环境 slug 已存在: $slug" }

        val saved = sqlClient.save(
            new(ConfigCenterEnvironment::class).by {
                id = existing.id
                environmentKey = existing.environmentKey
                this.project = projectRef(project.id)
                this.slug = slug
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                sortOrder = request.sortOrder
                isDefault = request.isDefault
                personalConfigEnabled = request.personalConfigEnabled
                createTime = existing.createTime
            },
        ).modifiedEntity

        rootConfigOfEnvironment(saved.id)?.let { rootConfig ->
            sqlClient.save(
                new(ConfigCenterConfig::class).by {
                    id = rootConfig.id
                    configKey = rootConfig.configKey
                    this.project = projectRef(project.id)
                    this.environment = environmentRef(saved.id)
                    this.slug = slug
                    name = "${saved.name} Root"
                    configType = ConfigCenterConfigType.ROOT.name
                    description = rootConfig.description
                    locked = true
                    enabled = rootConfig.enabled
                    sourceConfig = null
                    createTime = rootConfig.createTime
                },
            )
        }

        logActivity(
            project = project,
            config = null,
            action = ConfigCenterActivityType.ENVIRONMENT_UPDATED,
            resourceType = "environment",
            resourceKey = saved.slug,
            summary = "更新环境 ${saved.name}",
        )
        return saved.toDto(
            rootConfig = rootConfigOfEnvironment(saved.id),
            missingSecretCount = 0,
        )
    }

    fun listConfigs(
        projectId: Long,
    ): List<ConfigCenterConfigDto> {
        projectOrThrow(projectId)
        val configs = configsOfProject(projectId)
        val environmentsById = environmentsOfProject(projectId).associateBy { environment -> environment.id }
        val configById = configs.associateBy { config -> config.id }
        val secretsByConfigId = secretsOfProject(projectId)
            .groupBy { secret -> secret.config.id }

        return configs
            .sortedWith(
                compareBy<ConfigCenterConfig> { config ->
                    environmentsById[config.environment.id]?.sortOrder ?: Int.MAX_VALUE
                }
                    .thenBy { it.configType.asConfigType().orderWeight() }
                    .thenBy { it.name },
            )
            .map { config ->
                val localSecretCount = secretsByConfigId[config.id]
                    .orEmpty()
                    .count { secret -> secret.enabled && !secret.deleted }
                val inheritedSecretCount = resolveSecretRows(
                    selectedConfig = config,
                    configById = configById,
                    secretsByConfigId = secretsByConfigId,
                    includeInherited = true,
                ).values.count { row -> row.ownerConfig.id != config.id }
                config.toDto(
                    sourceConfigName = config.sourceConfig?.id?.let { sourceId ->
                        configById[sourceId]?.name
                    },
                    secretCount = localSecretCount,
                    inheritedSecretCount = inheritedSecretCount,
                )
            }
    }

    fun createConfig(
        projectId: Long,
        request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto {
        val project = projectOrThrow(projectId)
        val environment = environmentOrThrow(request.environmentId)
        require(environment.project.id == project.id) { "环境不属于当前项目" }
        val configType = request.configType
        require(configType != ConfigCenterConfigType.ROOT) { "根配置只允许系统自动创建" }
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "配置 slug 不能为空" }
        require(request.name.isNotBlank()) { "配置名称不能为空" }
        require(
            configsOfProject(projectId).none { config ->
                config.environment.id == environment.id && config.slug == slug
            },
        ) { "配置 slug 已存在: $slug" }

        val sourceConfigId = request.sourceConfigId ?: rootConfigOfEnvironment(environment.id)?.id
        val saved = sqlClient.save(
            new(ConfigCenterConfig::class).by {
                configKey = UUID.randomUUID().toString()
                this.project = projectRef(project.id)
                this.environment = environmentRef(environment.id)
                this.slug = slug
                name = request.name.trim()
                this.configType = configType.name
                description = request.description?.trim()?.ifBlank { null }
                locked = request.locked
                enabled = request.enabled
                sourceConfig = sourceConfigId?.let(::configRef)
            },
        ).modifiedEntity

        logActivity(
            project = project,
            config = saved,
            action = ConfigCenterActivityType.CONFIG_CREATED,
            resourceType = "config",
            resourceKey = saved.slug,
            summary = "创建配置 ${saved.name}",
        )
        return saved.toDto(
            sourceConfigName = sourceConfigId?.let(::configOrThrow)?.name,
            secretCount = 0,
            inheritedSecretCount = 0,
        )
    }

    fun updateConfig(
        configId: Long,
        request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto {
        val existing = configOrThrow(configId)
        val project = projectOrThrow(existing.project.id)
        val environment = environmentOrThrow(request.environmentId)
        require(environment.project.id == project.id) { "环境不属于当前项目" }
        val slug = normalizeSlug(request.slug)
        require(slug.isNotBlank()) { "配置 slug 不能为空" }
        require(request.name.isNotBlank()) { "配置名称不能为空" }
        require(
            configsOfProject(project.id).none { config ->
                config.id != configId && config.environment.id == environment.id && config.slug == slug
            },
        ) { "配置 slug 已存在: $slug" }
        require(existing.configType.asConfigType() != ConfigCenterConfigType.ROOT) { "根配置不允许手动改类型" }

        val sourceConfigId = request.sourceConfigId ?: rootConfigOfEnvironment(environment.id)?.id
        val saved = sqlClient.save(
            new(ConfigCenterConfig::class).by {
                id = existing.id
                configKey = existing.configKey
                this.project = projectRef(project.id)
                this.environment = environmentRef(environment.id)
                this.slug = slug
                name = request.name.trim()
                configType = existing.configType
                description = request.description?.trim()?.ifBlank { null }
                locked = request.locked
                enabled = request.enabled
                sourceConfig = sourceConfigId?.let(::configRef)
                createTime = existing.createTime
            },
        ).modifiedEntity

        logActivity(
            project = project,
            config = saved,
            action = ConfigCenterActivityType.CONFIG_UPDATED,
            resourceType = "config",
            resourceKey = saved.slug,
            summary = "更新配置 ${saved.name}",
        )
        val secretsByConfigId = secretsOfProject(project.id).groupBy { secret -> secret.config.id }
        return saved.toDto(
            sourceConfigName = sourceConfigId?.let(::configOrThrow)?.name,
            secretCount = secretsByConfigId[saved.id].orEmpty().count { secret -> secret.enabled && !secret.deleted },
            inheritedSecretCount = resolveSecretRows(
                selectedConfig = saved,
                configById = configsOfProject(project.id).associateBy { config -> config.id },
                secretsByConfigId = secretsByConfigId,
                includeInherited = true,
            ).values.count { row -> row.ownerConfig.id != saved.id },
        )
    }

    fun listSecrets(
        configId: Long,
        includeInherited: Boolean,
    ): List<ConfigCenterSecretDto> {
        val selectedConfig = configOrThrow(configId)
        val configs = configsOfProject(selectedConfig.project.id)
        val configById = configs.associateBy { config -> config.id }
        val secretsByConfigId = secretsOfProject(selectedConfig.project.id)
            .groupBy { secret -> secret.config.id }

        return resolveSecretRows(
            selectedConfig = selectedConfig,
            configById = configById,
            secretsByConfigId = secretsByConfigId,
            includeInherited = includeInherited,
        ).values
            .sortedBy { row -> row.secret.name }
            .map { row ->
                row.secret.toDto(
                    ownerConfig = row.ownerConfig,
                    inherited = row.ownerConfig.id != selectedConfig.id,
                    sourceConfigName = configById[row.ownerConfig.id]?.name,
                )
            }
    }

    fun saveSecret(
        request: ConfigCenterSecretMutationRequest,
        secretId: Long? = null,
    ): ConfigCenterSecretDto {
        val config = configOrThrow(request.configId)
        require(request.name.isNotBlank()) { "Secret 名称不能为空" }
        val existing = secretId?.let(::secretOrThrow)
            ?: localSecret(config.id, request.name)
        val action = if (existing == null) {
            ConfigCenterActivityType.SECRET_CREATED
        } else {
            ConfigCenterActivityType.SECRET_UPDATED
        }

        val saved = saveSecretInternal(
            config = config,
            existing = existing,
            name = request.name.trim(),
            value = request.value,
            note = request.note,
            valueType = request.valueType,
            sensitive = request.sensitive,
            enabled = request.enabled,
            action = action,
            actor = "system",
            changeComment = request.changeComment,
        )

        if (request.mirrorToEnvironmentIds.isNotEmpty() && config.configType.asConfigType() == ConfigCenterConfigType.ROOT) {
            val sourceEnvironmentSlug = environmentOrThrow(config.environment.id).slug
            request.mirrorToEnvironmentIds
                .filter { environmentId -> environmentId != config.environment.id }
                .distinct()
                .mapNotNull(::rootConfigOfEnvironment)
                .forEach { mirrorConfig ->
                    saveSecretInternal(
                        config = mirrorConfig,
                        existing = localSecret(mirrorConfig.id, request.name),
                        name = request.name.trim(),
                        value = request.value,
                        note = request.note,
                        valueType = request.valueType,
                        sensitive = request.sensitive,
                        enabled = request.enabled,
                        action = ConfigCenterActivityType.SECRET_UPDATED,
                        actor = "system",
                        changeComment = "mirror:$sourceEnvironmentSlug",
                    )
                }
        }

        return saved.toDto(
            ownerConfig = config,
            inherited = false,
            sourceConfigName = config.name,
        )
    }

    fun deleteSecret(
        secretId: Long,
    ) {
        val existing = secretOrThrow(secretId)
        val saved = sqlClient.save(
            new(ConfigCenterSecret::class).by {
                id = existing.id
                secretKey = existing.secretKey
                project = projectRef(existing.project.id)
                config = configRef(existing.config.id)
                name = existing.name
                valueText = existing.valueText
                maskedValue = existing.maskedValue
                note = existing.note
                valueType = existing.valueType
                sensitive = existing.sensitive
                enabled = false
                deleted = true
                version = existing.version + 1
                createTime = existing.createTime
            },
        ).modifiedEntity

        recordSecretVersion(
            secret = saved,
            action = ConfigCenterActivityType.SECRET_DELETED,
            actor = "system",
            note = existing.note,
        )
        logActivity(
            project = projectOrThrow(existing.project.id),
            config = configOrThrow(existing.config.id),
            action = ConfigCenterActivityType.SECRET_DELETED,
            resourceType = "secret",
            resourceKey = existing.name,
            summary = "删除 Secret ${existing.name}",
        )
    }

    fun listSecretVersions(
        secretId: Long,
    ): List<ConfigCenterSecretVersionDto> {
        secretOrThrow(secretId)
        return allSecretVersions()
            .filter { version -> version.secret.id == secretId }
            .sortedByDescending { version -> version.version }
            .map(ConfigCenterSecretVersion::toDto)
    }

    fun listServiceTokens(
        configId: Long,
    ): List<ConfigCenterServiceTokenDto> {
        val config = configOrThrow(configId)
        return allServiceTokens()
            .filter { token -> token.config.id == configId }
            .sortedByDescending { token -> token.updateTime ?: token.createTime }
            .map { token -> token.toDto(config.name) }
    }

    fun issueServiceToken(
        request: ConfigCenterServiceTokenIssueRequest,
    ): ConfigCenterServiceTokenIssueResult {
        val config = configOrThrow(request.configId)
        require(request.name.isNotBlank()) { "令牌名称不能为空" }
        val plainTextToken = "kc_st_${UUID.randomUUID()}".replace("-", "")
        val saved = sqlClient.save(
            new(ConfigCenterServiceToken::class).by {
                tokenKey = UUID.randomUUID().toString()
                project = projectRef(config.project.id)
                this.config = configRef(config.id)
                name = request.name.trim()
                tokenHash = sha256(plainTextToken)
                tokenPrefix = plainTextToken.take(12)
                writeAccess = request.writeAccess
                description = request.description?.trim()?.ifBlank { null }
                active = true
                lastUsedTime = null
                expireTime = request.expireTimeMillis?.let(Instant::ofEpochMilli)
                revokeTime = null
            },
        ).modifiedEntity

        logActivity(
            project = projectOrThrow(config.project.id),
            config = config,
            action = ConfigCenterActivityType.TOKEN_ISSUED,
            resourceType = "token",
            resourceKey = saved.tokenPrefix,
            summary = "签发服务令牌 ${saved.name}",
        )
        return ConfigCenterServiceTokenIssueResult(
            token = saved.toDto(config.name),
            plainTextToken = plainTextToken,
        )
    }

    fun revokeServiceToken(
        tokenId: Long,
    ): ConfigCenterServiceTokenDto {
        val existing = serviceTokenOrThrow(tokenId)
        val saved = sqlClient.save(
            new(ConfigCenterServiceToken::class).by {
                id = existing.id
                tokenKey = existing.tokenKey
                project = projectRef(existing.project.id)
                config = configRef(existing.config.id)
                name = existing.name
                tokenHash = existing.tokenHash
                tokenPrefix = existing.tokenPrefix
                writeAccess = existing.writeAccess
                description = existing.description
                active = false
                lastUsedTime = existing.lastUsedTime
                expireTime = existing.expireTime
                revokeTime = Instant.now()
                createTime = existing.createTime
            },
        ).modifiedEntity

        logActivity(
            project = projectOrThrow(existing.project.id),
            config = configOrThrow(existing.config.id),
            action = ConfigCenterActivityType.TOKEN_REVOKED,
            resourceType = "token",
            resourceKey = existing.tokenPrefix,
            summary = "吊销服务令牌 ${existing.name}",
        )
        return saved.toDto(configOrThrow(existing.config.id).name)
    }

    fun listActivityLogs(
        projectId: Long,
        limit: Int,
    ): List<ConfigCenterActivityLogDto> {
        projectOrThrow(projectId)
        return allActivityLogs()
            .filter { activity -> activity.project.id == projectId }
            .sortedByDescending { activity -> activity.createTime }
            .take(limit.coerceIn(1, 200))
            .map(ConfigCenterActivityLog::toDto)
    }

    fun readCompatValue(
        namespace: String,
        key: String,
        profile: String = "default",
    ): ConfigCenterCompatValueDto {
        val rootConfig = compatRootConfig(namespace, profile)
        if (rootConfig == null) {
            return ConfigCenterCompatValueDto(
                namespace = namespace,
                profile = profile,
                key = key,
                value = null,
            )
        }
        val resolved = listSecrets(rootConfig.id, includeInherited = true)
            .firstOrNull { secret -> secret.name == key }
        return ConfigCenterCompatValueDto(
            namespace = namespace,
            profile = profile,
            key = key,
            value = resolved?.value,
            projectId = rootConfig.project.id,
            configId = rootConfig.id,
        )
    }

    fun readCompatSnapshot(
        namespace: String,
        profile: String = "default",
    ): Map<String, String> {
        val rootConfig = compatRootConfig(namespace, profile) ?: return emptyMap()
        return listSecrets(rootConfig.id, includeInherited = true)
            .associate { secret -> secret.name to secret.value }
    }

    fun saveCompatValue(
        namespace: String,
        key: String,
        value: String,
        description: String? = null,
        profile: String = "default",
    ) {
        val project = ensureCompatProject(namespace)
        val environment = ensureCompatEnvironment(project, profile)
        val config = ensureRootConfig(project, environment)
        val existing = localSecret(config.id, key)
        val action = if (existing == null) {
            ConfigCenterActivityType.LEGACY_IMPORTED
        } else {
            ConfigCenterActivityType.SECRET_UPDATED
        }
        saveSecretInternal(
            config = config,
            existing = existing,
            name = key.trim(),
            value = value,
            note = description,
            valueType = guessValueType(value),
            sensitive = shouldTreatAsSensitive(key),
            enabled = true,
            action = action,
            actor = "compat",
            changeComment = description,
        )
    }

    private fun seedDefaultEnvironments(
        project: ConfigCenterProject,
    ) {
        val defaults = listOf(
            ConfigCenterEnvironmentMutationRequest(
                slug = "dev",
                name = "Development",
                description = "默认开发环境",
                sortOrder = 10,
                isDefault = true,
                personalConfigEnabled = true,
            ),
            ConfigCenterEnvironmentMutationRequest(
                slug = "stg",
                name = "Staging",
                description = "预发验证环境",
                sortOrder = 20,
            ),
            ConfigCenterEnvironmentMutationRequest(
                slug = "prd",
                name = "Production",
                description = "生产环境",
                sortOrder = 30,
            ),
        )
        defaults.forEach { request ->
            if (environmentsOfProject(project.id).none { environment -> environment.slug == request.slug }) {
                createEnvironment(project.id, request)
            }
        }
    }

    private fun ensureCompatProject(
        namespace: String,
    ): ConfigCenterProject {
        val slug = normalizeSlug(namespace)
        return allProjects().firstOrNull { project -> project.slug == slug }
            ?: projectOrThrow(
                createProject(
                    ConfigCenterProjectMutationRequest(
                        slug = slug,
                        name = namespace.ifBlank { slug },
                        description = "兼容旧 namespace=${namespace.ifBlank { slug }} 的自动生成项目",
                        enabled = true,
                    ),
                ).id,
            )
    }

    private fun ensureCompatEnvironment(
        project: ConfigCenterProject,
        profile: String,
    ): ConfigCenterEnvironment {
        val environmentSlug = compatEnvironmentSlug(profile)
        return environmentsOfProject(project.id)
            .firstOrNull { environment -> environment.slug == environmentSlug }
            ?: environmentOrThrow(
                createEnvironment(
                    project.id,
                    ConfigCenterEnvironmentMutationRequest(
                        slug = environmentSlug,
                        name = environmentSlug.uppercase(),
                        description = "兼容旧 profile=$profile 自动生成",
                        sortOrder = 90,
                        isDefault = environmentSlug == "dev",
                        personalConfigEnabled = environmentSlug == "dev",
                    ),
                ).id,
            )
    }

    private fun compatEnvironmentSlug(
        profile: String,
    ): String {
        val normalized = profile.trim().ifBlank { "default" }
        return if (normalized == "default") {
            "dev"
        } else {
            normalizeSlug(normalized)
        }
    }

    private fun ensureRootConfig(
        project: ConfigCenterProject,
        environment: ConfigCenterEnvironment,
    ): ConfigCenterConfig {
        return rootConfigOfEnvironment(environment.id) ?: createRootConfig(project, environment)
    }

    private fun createRootConfig(
        project: ConfigCenterProject,
        environment: ConfigCenterEnvironment,
    ): ConfigCenterConfig {
        val existing = rootConfigOfEnvironment(environment.id)
        if (existing != null) {
            return existing
        }
        return sqlClient.save(
            new(ConfigCenterConfig::class).by {
                configKey = UUID.randomUUID().toString()
                this.project = projectRef(project.id)
                this.environment = environmentRef(environment.id)
                slug = environment.slug
                name = "${environment.name} Root"
                configType = ConfigCenterConfigType.ROOT.name
                description = "Doppler 风格的根配置，作为该环境的基线"
                locked = true
                enabled = true
                sourceConfig = null
            },
        ).modifiedEntity
    }

    private fun rootConfigOfEnvironment(
        environmentId: Long,
    ): ConfigCenterConfig? {
        return allConfigs()
            .firstOrNull { config ->
                config.environment.id == environmentId &&
                    config.configType.asConfigType() == ConfigCenterConfigType.ROOT
            }
    }

    private fun saveSecretInternal(
        config: ConfigCenterConfig,
        existing: ConfigCenterSecret?,
        name: String,
        value: String,
        note: String?,
        valueType: ConfigCenterValueType,
        sensitive: Boolean,
        enabled: Boolean,
        action: ConfigCenterActivityType,
        actor: String,
        changeComment: String?,
    ): ConfigCenterSecret {
        val project = projectOrThrow(config.project.id)
        val maskedValue = maskValue(value, sensitive)
        val saved = sqlClient.save(
            new(ConfigCenterSecret::class).by {
                existing?.id?.let { id = it }
                secretKey = existing?.secretKey ?: UUID.randomUUID().toString()
                this.project = projectRef(project.id)
                this.config = configRef(config.id)
                this.name = name
                valueText = value
                this.maskedValue = maskedValue
                this.note = note?.trim()?.ifBlank { null }
                this.valueType = valueType.name
                this.sensitive = sensitive
                this.enabled = enabled
                deleted = false
                version = (existing?.version ?: 0) + 1
                existing?.createTime?.let { createTime = it }
            },
        ).modifiedEntity

        recordSecretVersion(
            secret = saved,
            action = action,
            actor = actor,
            note = changeComment ?: note,
        )
        logActivity(
            project = project,
            config = config,
            action = action,
            resourceType = "secret",
            resourceKey = name,
            summary = when (action) {
                ConfigCenterActivityType.LEGACY_IMPORTED -> "导入旧配置 $name"
                ConfigCenterActivityType.SECRET_CREATED -> "创建 Secret $name"
                else -> "更新 Secret $name"
            },
        )
        return saved
    }

    private fun recordSecretVersion(
        secret: ConfigCenterSecret,
        action: ConfigCenterActivityType,
        actor: String,
        note: String?,
    ) {
        sqlClient.save(
            new(ConfigCenterSecretVersion::class).by {
                this.secret = secretRef(secret.id)
                version = secret.version
                this.action = action.name
                valueText = secret.valueText
                maskedValue = secret.maskedValue
                this.note = note?.trim()?.ifBlank { null }
                this.actor = actor
            },
        )
    }

    private fun resolveSecretRows(
        selectedConfig: ConfigCenterConfig,
        configById: Map<Long, ConfigCenterConfig>,
        secretsByConfigId: Map<Long, List<ConfigCenterSecret>>,
        includeInherited: Boolean,
    ): Map<String, ResolvedSecretRow> {
        val resolved = linkedMapOf<String, ResolvedSecretRow>()

        fun visit(config: ConfigCenterConfig?) {
            if (config == null) {
                return
            }
            if (includeInherited) {
                val parent = config.sourceConfig?.id?.let(configById::get)
                if (parent != null && parent.id != config.id) {
                    visit(parent)
                }
            }
            secretsByConfigId[config.id].orEmpty()
                .sortedBy { secret -> secret.name }
                .forEach { secret ->
                    if (!secret.enabled || secret.deleted) {
                        resolved.remove(secret.name)
                    } else {
                        resolved[secret.name] = ResolvedSecretRow(secret = secret, ownerConfig = config)
                    }
                }
        }

        visit(selectedConfig)
        return resolved
    }

    private fun localSecret(
        configId: Long,
        name: String,
    ): ConfigCenterSecret? {
        return allSecrets()
            .firstOrNull { secret ->
                secret.config.id == configId && secret.name == name.trim()
            }
    }

    private fun allProjects(): List<ConfigCenterProject> {
        return sqlClient.createQuery(ConfigCenterProject::class) {
            select(table)
        }.execute()
    }

    private fun allEnvironments(): List<ConfigCenterEnvironment> {
        return sqlClient.createQuery(ConfigCenterEnvironment::class) {
            select(table)
        }.execute()
    }

    private fun allConfigs(): List<ConfigCenterConfig> {
        return sqlClient.createQuery(ConfigCenterConfig::class) {
            select(table)
        }.execute()
    }

    private fun allSecrets(): List<ConfigCenterSecret> {
        return sqlClient.createQuery(ConfigCenterSecret::class) {
            select(table)
        }.execute()
    }

    private fun allSecretVersions(): List<ConfigCenterSecretVersion> {
        return sqlClient.createQuery(ConfigCenterSecretVersion::class) {
            select(table)
        }.execute()
    }

    private fun allServiceTokens(): List<ConfigCenterServiceToken> {
        return sqlClient.createQuery(ConfigCenterServiceToken::class) {
            select(table)
        }.execute()
    }

    private fun allActivityLogs(): List<ConfigCenterActivityLog> {
        return sqlClient.createQuery(ConfigCenterActivityLog::class) {
            select(table)
        }.execute()
    }

    private fun environmentsOfProject(
        projectId: Long,
    ): List<ConfigCenterEnvironment> {
        return allEnvironments().filter { environment -> environment.project.id == projectId }
    }

    private fun configsOfProject(
        projectId: Long,
    ): List<ConfigCenterConfig> {
        return allConfigs().filter { config -> config.project.id == projectId }
    }

    private fun secretsOfProject(
        projectId: Long,
    ): List<ConfigCenterSecret> {
        return allSecrets().filter { secret -> secret.project.id == projectId }
    }

    private fun localActiveSecrets(
        configId: Long,
    ): List<ConfigCenterSecret> {
        return allSecrets()
            .filter { secret ->
                secret.config.id == configId && secret.enabled && !secret.deleted
            }
    }

    private fun compatRootConfig(
        namespace: String,
        profile: String,
    ): ConfigCenterConfig? {
        val project = allProjects().firstOrNull { candidate ->
            candidate.slug == normalizeSlug(namespace)
        } ?: return null
        val environment = environmentsOfProject(project.id).firstOrNull { candidate ->
            candidate.slug == compatEnvironmentSlug(profile)
        } ?: return null
        return rootConfigOfEnvironment(environment.id)
    }

    private fun projectOrThrow(
        projectId: Long,
    ): ConfigCenterProject {
        return sqlClient.findById(ConfigCenterProject::class, projectId)
            ?: throw NoSuchElementException("项目不存在: $projectId")
    }

    private fun environmentOrThrow(
        environmentId: Long,
    ): ConfigCenterEnvironment {
        return sqlClient.findById(ConfigCenterEnvironment::class, environmentId)
            ?: throw NoSuchElementException("环境不存在: $environmentId")
    }

    private fun configOrThrow(
        configId: Long,
    ): ConfigCenterConfig {
        return sqlClient.findById(ConfigCenterConfig::class, configId)
            ?: throw NoSuchElementException("配置不存在: $configId")
    }

    private fun secretOrThrow(
        secretId: Long,
    ): ConfigCenterSecret {
        return sqlClient.findById(ConfigCenterSecret::class, secretId)
            ?: throw NoSuchElementException("Secret 不存在: $secretId")
    }

    private fun serviceTokenOrThrow(
        tokenId: Long,
    ): ConfigCenterServiceToken {
        return sqlClient.findById(ConfigCenterServiceToken::class, tokenId)
            ?: throw NoSuchElementException("服务令牌不存在: $tokenId")
    }

    private fun projectRef(
        projectId: Long,
    ): ConfigCenterProject {
        return new(ConfigCenterProject::class).by {
            id = projectId
        }
    }

    private fun environmentRef(
        environmentId: Long,
    ): ConfigCenterEnvironment {
        return new(ConfigCenterEnvironment::class).by {
            id = environmentId
        }
    }

    private fun configRef(
        configId: Long,
    ): ConfigCenterConfig {
        return new(ConfigCenterConfig::class).by {
            id = configId
        }
    }

    private fun secretRef(
        secretId: Long,
    ): ConfigCenterSecret {
        return new(ConfigCenterSecret::class).by {
            id = secretId
        }
    }

    private fun logActivity(
        project: ConfigCenterProject,
        config: ConfigCenterConfig?,
        action: ConfigCenterActivityType,
        resourceType: String,
        resourceKey: String,
        summary: String,
        detailJson: String? = null,
        actor: String = "system",
    ) {
        sqlClient.save(
            new(ConfigCenterActivityLog::class).by {
                this.project = projectRef(project.id)
                this.config = config?.let { target -> configRef(target.id) }
                this.action = action.name
                this.resourceType = resourceType
                this.resourceKey = resourceKey
                this.summary = summary
                this.detailJson = detailJson
                this.actor = actor
            },
        )
    }
}

private data class ResolvedSecretRow(
    val secret: ConfigCenterSecret,
    val ownerConfig: ConfigCenterConfig,
)

private fun ConfigCenterProject.toDto(
    environments: List<ConfigCenterEnvironment>,
    configs: List<ConfigCenterConfig>,
    secrets: List<ConfigCenterSecret>,
): ConfigCenterProjectDto {
    return ConfigCenterProjectDto(
        id = id,
        projectKey = projectKey,
        slug = slug,
        name = name,
        description = description,
        enabled = enabled,
        environmentCount = environments.size,
        configCount = configs.size,
        secretCount = secrets.count { secret -> secret.enabled && !secret.deleted },
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun ConfigCenterEnvironment.toDto(
    rootConfig: ConfigCenterConfig?,
    missingSecretCount: Int,
): ConfigCenterEnvironmentDto {
    return ConfigCenterEnvironmentDto(
        id = id,
        environmentKey = environmentKey,
        projectId = project.id,
        slug = slug,
        name = name,
        description = description,
        sortOrder = sortOrder,
        isDefault = isDefault,
        personalConfigEnabled = personalConfigEnabled,
        rootConfigId = rootConfig?.id,
        rootConfigName = rootConfig?.name,
        missingSecretCount = missingSecretCount,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun ConfigCenterConfig.toDto(
    sourceConfigName: String?,
    secretCount: Int,
    inheritedSecretCount: Int,
): ConfigCenterConfigDto {
    return ConfigCenterConfigDto(
        id = id,
        configKey = configKey,
        projectId = project.id,
        environmentId = environment.id,
        slug = slug,
        name = name,
        configType = configType.asConfigType(),
        description = description,
        locked = locked,
        enabled = enabled,
        sourceConfigId = sourceConfig?.id,
        sourceConfigName = sourceConfigName,
        secretCount = secretCount,
        inheritedSecretCount = inheritedSecretCount,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun ConfigCenterSecret.toDto(
    ownerConfig: ConfigCenterConfig,
    inherited: Boolean,
    sourceConfigName: String?,
): ConfigCenterSecretDto {
    return ConfigCenterSecretDto(
        id = id,
        secretKey = secretKey,
        projectId = project.id,
        configId = ownerConfig.id,
        configName = ownerConfig.name,
        name = name,
        value = valueText,
        maskedValue = maskedValue,
        note = note,
        valueType = valueType.asValueType(),
        sensitive = sensitive,
        enabled = enabled,
        deleted = deleted,
        version = version,
        inherited = inherited,
        sourceConfigId = ownerConfig.id,
        sourceConfigName = sourceConfigName,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun ConfigCenterSecretVersion.toDto(): ConfigCenterSecretVersionDto {
    return ConfigCenterSecretVersionDto(
        id = id,
        secretId = secret.id,
        version = version,
        action = action.asActivityType(),
        value = valueText,
        maskedValue = maskedValue,
        note = note,
        actor = actor,
        createTimeMillis = createTime.toEpochMilli(),
    )
}

private fun ConfigCenterServiceToken.toDto(
    configName: String,
): ConfigCenterServiceTokenDto {
    return ConfigCenterServiceTokenDto(
        id = id,
        tokenKey = tokenKey,
        projectId = project.id,
        configId = config.id,
        configName = configName,
        name = name,
        tokenPrefix = tokenPrefix,
        writeAccess = writeAccess,
        description = description,
        active = active,
        lastUsedTimeMillis = lastUsedTime?.toEpochMilli(),
        expireTimeMillis = expireTime?.toEpochMilli(),
        revokeTimeMillis = revokeTime?.toEpochMilli(),
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun ConfigCenterActivityLog.toDto(): ConfigCenterActivityLogDto {
    return ConfigCenterActivityLogDto(
        id = id,
        projectId = project.id,
        configId = config?.id,
        action = action.asActivityType(),
        resourceType = resourceType,
        resourceKey = resourceKey,
        summary = summary,
        detailJson = detailJson,
        actor = actor,
        createTimeMillis = createTime.toEpochMilli(),
    )
}

private fun String.asConfigType(): ConfigCenterConfigType {
    return runCatching { ConfigCenterConfigType.valueOf(this) }
        .getOrDefault(ConfigCenterConfigType.ROOT)
}

private fun String.asValueType(): ConfigCenterValueType {
    return runCatching { ConfigCenterValueType.valueOf(this) }
        .getOrDefault(ConfigCenterValueType.STRING)
}

private fun String.asActivityType(): ConfigCenterActivityType {
    return runCatching { ConfigCenterActivityType.valueOf(this) }
        .getOrDefault(ConfigCenterActivityType.SECRET_UPDATED)
}

private fun ConfigCenterConfigType.orderWeight(): Int {
    return when (this) {
        ConfigCenterConfigType.ROOT -> 0
        ConfigCenterConfigType.BRANCH -> 1
        ConfigCenterConfigType.PERSONAL -> 2
    }
}

private fun normalizeSlug(
    rawValue: String,
): String {
    return rawValue
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}

private fun maskValue(
    value: String,
    sensitive: Boolean,
): String {
    if (!sensitive) {
        return value
    }
    if (value.isEmpty()) {
        return ""
    }
    if (value.length <= 4) {
        return "*".repeat(value.length)
    }
    return buildString {
        append(value.take(2))
        append("*".repeat((value.length - 4).coerceAtMost(12)))
        append(value.takeLast(2))
    }
}

private fun shouldTreatAsSensitive(
    key: String,
): Boolean {
    val lowered = key.lowercase()
    return lowered.contains("token") ||
        lowered.contains("secret") ||
        lowered.contains("password") ||
        lowered.contains("accesskey") ||
        lowered.contains("private")
}

private fun guessValueType(
    value: String,
): ConfigCenterValueType {
    val normalized = value.trim()
    return when {
        normalized.equals("true", ignoreCase = true) || normalized.equals("false", ignoreCase = true) ->
            ConfigCenterValueType.BOOLEAN

        normalized.toLongOrNull() != null -> ConfigCenterValueType.INTEGER
        normalized.toDoubleOrNull() != null -> ConfigCenterValueType.NUMBER
        normalized.startsWith("{") || normalized.startsWith("[") -> ConfigCenterValueType.JSON
        normalized.contains("\n") -> ConfigCenterValueType.TEXT
        else -> ConfigCenterValueType.STRING
    }
}

private fun sha256(
    text: String,
): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }
}
