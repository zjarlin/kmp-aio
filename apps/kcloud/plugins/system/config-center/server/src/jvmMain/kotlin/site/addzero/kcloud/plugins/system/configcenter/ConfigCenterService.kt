package site.addzero.kcloud.plugins.system.configcenter

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto
import site.addzero.kcloud.plugins.system.configcenter.model.ConfigCenterConfig
import site.addzero.kcloud.plugins.system.configcenter.model.ConfigCenterEnvironment
import site.addzero.kcloud.plugins.system.configcenter.model.ConfigCenterProject
import site.addzero.kcloud.plugins.system.configcenter.model.ConfigCenterSecret
import site.addzero.kcloud.plugins.system.configcenter.model.ConfigCenterValue
import site.addzero.kcloud.plugins.system.configcenter.model.by
import java.util.UUID

@Single
class ConfigCenterService(
    private val sqlClient: KSqlClient,
) {
    fun readValue(
        namespace: String,
        key: String,
        active: String = "dev",
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeNamespace(namespace)
        val normalizedKey = normalizeConfigKey(key)
        val normalizedActive = normalizeActiveProfile(active)
        require(normalizedNamespace.isNotBlank()) { "namespace 不能为空" }
        require(normalizedKey.isNotBlank()) { "key 不能为空" }

        val storedEntries = storedEntriesOrNull(
            namespace = normalizedNamespace,
            active = normalizedActive,
        )
        storedEntries
            ?.firstOrNull { entry -> entry.configKey == normalizedKey }
            ?.let(ConfigCenterValue::toDto)
            ?.let { return it }

        val legacyValue = readLegacyValue(
            namespace = normalizedNamespace,
            active = normalizedActive,
            key = normalizedKey,
        )
        if (legacyValue == null) {
            return ConfigCenterValueDto(
                namespace = normalizedNamespace,
                active = normalizedActive,
                key = normalizedKey,
                value = null,
            )
        }
        if (storedEntries == null) {
            return ConfigCenterValueDto(
                namespace = normalizedNamespace,
                active = normalizedActive,
                key = normalizedKey,
                value = legacyValue,
            )
        }
        return saveStoredValue(
            existing = null,
            namespace = normalizedNamespace,
            active = normalizedActive,
            key = normalizedKey,
            value = legacyValue,
        ).toDto()
    }

    fun writeValue(
        namespace: String,
        key: String,
        value: String,
        active: String = "dev",
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeNamespace(namespace)
        val normalizedKey = normalizeConfigKey(key)
        val normalizedActive = normalizeActiveProfile(active)
        require(normalizedNamespace.isNotBlank()) { "namespace 不能为空" }
        require(normalizedKey.isNotBlank()) { "key 不能为空" }

        val storedEntries = storedEntriesOrNull(
            namespace = normalizedNamespace,
            active = normalizedActive,
        )
        if (storedEntries != null) {
            return saveStoredValue(
                existing = storedEntries.firstOrNull { entry -> entry.configKey == normalizedKey },
                namespace = normalizedNamespace,
                active = normalizedActive,
                key = normalizedKey,
                value = value,
            ).toDto()
        }

        saveLegacyValue(
            namespace = normalizedNamespace,
            active = normalizedActive,
            key = normalizedKey,
            value = value,
        )
        return ConfigCenterValueDto(
            namespace = normalizedNamespace,
            active = normalizedActive,
            key = normalizedKey,
            value = value,
        )
    }

    private fun storedEntriesOrNull(
        namespace: String,
        active: String,
    ): List<ConfigCenterValue>? {
        return try {
            allStoredValues().filter { entry ->
                entry.namespace == namespace && entry.active == active
            }
        } catch (error: Throwable) {
            if (error.isMissingConfigCenterValueTable()) {
                null
            } else {
                throw error
            }
        }
    }

    private fun allStoredValues(): List<ConfigCenterValue> {
        return sqlClient.createQuery(ConfigCenterValue::class) {
            select(table)
        }.execute()
    }

    private fun saveStoredValue(
        existing: ConfigCenterValue?,
        namespace: String,
        active: String,
        key: String,
        value: String,
    ): ConfigCenterValue {
        return sqlClient.save(
            new(ConfigCenterValue::class).by {
                existing?.id?.let { id = it }
                this.namespace = namespace
                this.active = active
                configKey = key
                configValue = value
                existing?.createTime?.let { createTime = it }
            },
        ).modifiedEntity
    }

    private fun readLegacyValue(
        namespace: String,
        active: String,
        key: String,
    ): String? {
        val rootConfig = findLegacyRootConfig(
            namespace = namespace,
            active = active,
        ) ?: return null
        return legacySecrets(rootConfig.id)
            .firstOrNull { secret -> secret.name == key }
            ?.valueText
    }

    private fun saveLegacyValue(
        namespace: String,
        active: String,
        key: String,
        value: String,
    ) {
        val project = ensureLegacyProject(namespace)
        val environment = ensureLegacyEnvironment(
            project = project,
            active = active,
        )
        val config = ensureLegacyRootConfig(
            project = project,
            environment = environment,
        )
        val existing = legacySecrets(config.id)
            .firstOrNull { secret -> secret.name == key }
        sqlClient.save(
            new(ConfigCenterSecret::class).by {
                existing?.id?.let { id = it }
                secretKey = existing?.secretKey ?: UUID.randomUUID().toString()
                this.project = projectRef(project.id)
                this.config = configRef(config.id)
                name = key
                valueText = value
                maskedValue = maskValue(
                    value = value,
                    sensitive = shouldTreatAsSensitive(key),
                )
                note = null
                valueType = guessLegacyValueType(value)
                sensitive = shouldTreatAsSensitive(key)
                enabled = true
                deleted = false
                version = (existing?.version ?: 0) + 1
                existing?.createTime?.let { createTime = it }
            },
        )
    }

    private fun findLegacyRootConfig(
        namespace: String,
        active: String,
    ): ConfigCenterConfig? {
        val project = allLegacyProjects()
            .firstOrNull { project -> project.slug == namespace }
            ?: return null
        val environment = allLegacyEnvironments()
            .firstOrNull { candidate ->
                candidate.project.id == project.id && candidate.slug == active
            }
            ?: return null
        return allLegacyConfigs()
            .firstOrNull { config ->
                config.project.id == project.id &&
                    config.environment.id == environment.id &&
                    config.configType == LEGACY_ROOT_CONFIG_TYPE
            }
    }

    private fun ensureLegacyProject(
        namespace: String,
    ): ConfigCenterProject {
        return allLegacyProjects()
            .firstOrNull { project -> project.slug == namespace }
            ?: sqlClient.save(
                new(ConfigCenterProject::class).by {
                    projectKey = UUID.randomUUID().toString()
                    slug = namespace
                    name = namespace
                    description = "兼容旧配置结构自动生成"
                    enabled = true
                },
            ).modifiedEntity
    }

    private fun ensureLegacyEnvironment(
        project: ConfigCenterProject,
        active: String,
    ): ConfigCenterEnvironment {
        return allLegacyEnvironments()
            .firstOrNull { environment ->
                environment.project.id == project.id && environment.slug == active
            }
            ?: sqlClient.save(
                new(ConfigCenterEnvironment::class).by {
                    environmentKey = UUID.randomUUID().toString()
                    this.project = projectRef(project.id)
                    slug = active
                    name = active.uppercase()
                    description = "兼容旧配置结构自动生成"
                    sortOrder = legacyEnvironmentSortOrder(active)
                    isDefault = active == "dev"
                    personalConfigEnabled = active == "dev"
                },
            ).modifiedEntity
    }

    private fun ensureLegacyRootConfig(
        project: ConfigCenterProject,
        environment: ConfigCenterEnvironment,
    ): ConfigCenterConfig {
        return allLegacyConfigs()
            .firstOrNull { config ->
                config.project.id == project.id &&
                    config.environment.id == environment.id &&
                    config.configType == LEGACY_ROOT_CONFIG_TYPE
            }
            ?: sqlClient.save(
                new(ConfigCenterConfig::class).by {
                    configKey = UUID.randomUUID().toString()
                    this.project = projectRef(project.id)
                    this.environment = environmentRef(environment.id)
                    slug = environment.slug
                    name = "${environment.name} Root"
                    configType = LEGACY_ROOT_CONFIG_TYPE
                    description = "兼容旧配置结构自动生成"
                    locked = true
                    enabled = true
                    sourceConfig = null
                },
            ).modifiedEntity
    }

    private fun allLegacyProjects(): List<ConfigCenterProject> {
        return sqlClient.createQuery(ConfigCenterProject::class) {
            select(table)
        }.execute()
    }

    private fun allLegacyEnvironments(): List<ConfigCenterEnvironment> {
        return sqlClient.createQuery(ConfigCenterEnvironment::class) {
            select(table)
        }.execute()
    }

    private fun allLegacyConfigs(): List<ConfigCenterConfig> {
        return sqlClient.createQuery(ConfigCenterConfig::class) {
            select(table)
        }.execute()
    }

    private fun legacySecrets(
        configId: Long,
    ): List<ConfigCenterSecret> {
        return sqlClient.createQuery(ConfigCenterSecret::class) {
            select(table)
        }.execute().filter { secret ->
            secret.config.id == configId && secret.enabled && !secret.deleted
        }
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
}

private const val LEGACY_ROOT_CONFIG_TYPE = "ROOT"

private fun ConfigCenterValue.toDto(): ConfigCenterValueDto {
    return ConfigCenterValueDto(
        namespace = namespace,
        active = active,
        key = configKey,
        value = configValue,
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun normalizeNamespace(
    rawValue: String,
): String {
    return normalizeSlug(rawValue)
}

private fun normalizeConfigKey(
    rawValue: String,
): String {
    return rawValue.trim()
}

private fun normalizeActiveProfile(
    rawValue: String,
): String {
    val normalized = rawValue.trim().lowercase().ifBlank { "dev" }
    return when (normalized) {
        "default",
        "dev",
        "development",
        -> "dev"

        "prod",
        "prd",
        "production",
        -> "prod"

        else -> normalizeSlug(normalized).ifBlank { "dev" }
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

private fun legacyEnvironmentSortOrder(
    active: String,
): Int {
    return when (active) {
        "dev" -> 10
        "prod" -> 20
        else -> 30
    }
}

private fun guessLegacyValueType(
    value: String,
): String {
    val normalized = value.trim()
    return when {
        normalized.equals("true", ignoreCase = true) || normalized.equals("false", ignoreCase = true) ->
            "BOOLEAN"

        normalized.toLongOrNull() != null -> "INTEGER"
        normalized.toDoubleOrNull() != null -> "NUMBER"
        normalized.startsWith("{") || normalized.startsWith("[") -> "JSON"
        normalized.contains("\n") -> "TEXT"
        else -> "STRING"
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

private fun Throwable.isMissingConfigCenterValueTable(): Boolean {
    val messageText = generateSequence(this) { error -> error.cause }
        .mapNotNull { error -> error.message }
        .joinToString(" ")
        .lowercase()
    return messageText.contains("config_center_value") &&
        (
            messageText.contains("no such table") ||
                messageText.contains("does not exist") ||
                messageText.contains("not exist") ||
                messageText.contains("unknown table")
            )
}
