package site.addzero.kcloud.plugins.system.configcenter.api

import kotlinx.serialization.Serializable

@Serializable
enum class ConfigCenterConfigType {
    ROOT,
    BRANCH,
    PERSONAL,
}

@Serializable
enum class ConfigCenterValueType {
    STRING,
    BOOLEAN,
    INTEGER,
    NUMBER,
    JSON,
    TEXT,
}

@Serializable
enum class ConfigCenterActivityType {
    PROJECT_CREATED,
    PROJECT_UPDATED,
    ENVIRONMENT_CREATED,
    ENVIRONMENT_UPDATED,
    CONFIG_CREATED,
    CONFIG_UPDATED,
    SECRET_CREATED,
    SECRET_UPDATED,
    SECRET_DELETED,
    TOKEN_ISSUED,
    TOKEN_REVOKED,
    LEGACY_IMPORTED,
}

@Serializable
data class ConfigCenterProjectDto(
    val id: Long,
    val projectKey: String,
    val slug: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val environmentCount: Int = 0,
    val configCount: Int = 0,
    val secretCount: Int = 0,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterProjectMutationRequest(
    val slug: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
)

@Serializable
data class ConfigCenterEnvironmentDto(
    val id: Long,
    val environmentKey: String,
    val projectId: Long,
    val slug: String,
    val name: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val personalConfigEnabled: Boolean = false,
    val rootConfigId: Long? = null,
    val rootConfigName: String? = null,
    val missingSecretCount: Int = 0,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterEnvironmentMutationRequest(
    val slug: String,
    val name: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val personalConfigEnabled: Boolean = false,
)

@Serializable
data class ConfigCenterConfigDto(
    val id: Long,
    val configKey: String,
    val projectId: Long,
    val environmentId: Long,
    val slug: String,
    val name: String,
    val configType: ConfigCenterConfigType = ConfigCenterConfigType.ROOT,
    val description: String? = null,
    val locked: Boolean = false,
    val enabled: Boolean = true,
    val sourceConfigId: Long? = null,
    val sourceConfigName: String? = null,
    val secretCount: Int = 0,
    val inheritedSecretCount: Int = 0,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterConfigMutationRequest(
    val environmentId: Long,
    val slug: String,
    val name: String,
    val configType: ConfigCenterConfigType = ConfigCenterConfigType.BRANCH,
    val description: String? = null,
    val locked: Boolean = false,
    val enabled: Boolean = true,
    val sourceConfigId: Long? = null,
)

@Serializable
data class ConfigCenterSecretDto(
    val id: Long,
    val secretKey: String,
    val projectId: Long,
    val configId: Long,
    val configName: String,
    val name: String,
    val value: String = "",
    val maskedValue: String = "",
    val note: String? = null,
    val valueType: ConfigCenterValueType = ConfigCenterValueType.STRING,
    val sensitive: Boolean = true,
    val enabled: Boolean = true,
    val deleted: Boolean = false,
    val version: Int = 1,
    val inherited: Boolean = false,
    val sourceConfigId: Long? = null,
    val sourceConfigName: String? = null,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterSecretMutationRequest(
    val configId: Long,
    val name: String,
    val value: String,
    val note: String? = null,
    val valueType: ConfigCenterValueType = ConfigCenterValueType.STRING,
    val sensitive: Boolean = true,
    val enabled: Boolean = true,
    val changeComment: String? = null,
    val mirrorToEnvironmentIds: List<Long> = emptyList(),
)

@Serializable
data class ConfigCenterSecretVersionDto(
    val id: Long,
    val secretId: Long,
    val version: Int,
    val action: ConfigCenterActivityType,
    val value: String = "",
    val maskedValue: String = "",
    val note: String? = null,
    val actor: String = "system",
    val createTimeMillis: Long,
)

@Serializable
data class ConfigCenterServiceTokenDto(
    val id: Long,
    val tokenKey: String,
    val projectId: Long,
    val configId: Long,
    val configName: String,
    val name: String,
    val tokenPrefix: String,
    val writeAccess: Boolean = false,
    val description: String? = null,
    val active: Boolean = true,
    val lastUsedTimeMillis: Long? = null,
    val expireTimeMillis: Long? = null,
    val revokeTimeMillis: Long? = null,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterServiceTokenIssueRequest(
    val configId: Long,
    val name: String,
    val description: String? = null,
    val writeAccess: Boolean = false,
    val expireTimeMillis: Long? = null,
)

@Serializable
data class ConfigCenterServiceTokenIssueResult(
    val token: ConfigCenterServiceTokenDto,
    val plainTextToken: String,
)

@Serializable
data class ConfigCenterActivityLogDto(
    val id: Long,
    val projectId: Long,
    val configId: Long? = null,
    val action: ConfigCenterActivityType,
    val resourceType: String,
    val resourceKey: String,
    val summary: String,
    val detailJson: String? = null,
    val actor: String = "system",
    val createTimeMillis: Long,
)

@Serializable
data class ConfigCenterCompatValueDto(
    val namespace: String,
    val profile: String,
    val key: String,
    val value: String? = null,
    val projectId: Long? = null,
    val configId: Long? = null,
)
