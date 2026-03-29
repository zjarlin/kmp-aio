package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterCompatValueDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueResult
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenDto

/**
 * 原始文件: site.addzero.kcloud.plugins.system.configcenter.routes.ConfigCenter.kt
 * 基础路径: 
 */
interface ConfigCenterApi {

/**
 * listConfigCenterProjects
 * HTTP方法: GET
 * 路径: /api/system/config-center/projects
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto>
 */
    @GET("/api/system/config-center/projects")    suspend fun listConfigCenterProjects(): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto>

/**
 * listConfigCenterEnvironments
 * HTTP方法: GET
 * 路径: /api/system/config-center/projects/{projectId}/environments
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto>
 */
    @GET("/api/system/config-center/projects/{projectId}/environments")    suspend fun listConfigCenterEnvironments(
        @Path("projectId") projectId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto>

/**
 * listConfigCenterConfigs
 * HTTP方法: GET
 * 路径: /api/system/config-center/projects/{projectId}/configs
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto>
 */
    @GET("/api/system/config-center/projects/{projectId}/configs")    suspend fun listConfigCenterConfigs(
        @Path("projectId") projectId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto>

/**
 * listConfigCenterSecrets
 * HTTP方法: GET
 * 路径: /api/system/config-center/configs/{configId}/secrets
 * 参数:
 *   - configId: kotlin.Long (PathVariable)
 *   - includeInherited: kotlin.Boolean? (RequestParam)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto>
 */
    @GET("/api/system/config-center/configs/{configId}/secrets")    suspend fun listConfigCenterSecrets(
        @Path("configId") configId: kotlin.Long,
        @Query("includeInherited") includeInherited: kotlin.Boolean?
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto>

/**
 * listConfigCenterSecretVersions
 * HTTP方法: GET
 * 路径: /api/system/config-center/secrets/{secretId}/versions
 * 参数:
 *   - secretId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretVersionDto>
 */
    @GET("/api/system/config-center/secrets/{secretId}/versions")    suspend fun listConfigCenterSecretVersions(
        @Path("secretId") secretId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretVersionDto>

/**
 * listConfigCenterServiceTokens
 * HTTP方法: GET
 * 路径: /api/system/config-center/configs/{configId}/tokens
 * 参数:
 *   - configId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenDto>
 */
    @GET("/api/system/config-center/configs/{configId}/tokens")    suspend fun listConfigCenterServiceTokens(
        @Path("configId") configId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenDto>

/**
 * listConfigCenterActivityLogs
 * HTTP方法: GET
 * 路径: /api/system/config-center/projects/{projectId}/activities
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - limit: kotlin.Int? (RequestParam)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterActivityLogDto>
 */
    @GET("/api/system/config-center/projects/{projectId}/activities")    suspend fun listConfigCenterActivityLogs(
        @Path("projectId") projectId: kotlin.Long,
        @Query("limit") limit: kotlin.Int?
    ): kotlin.collections.List<site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterActivityLogDto>

/**
 * getConfigCenterCompatValue
 * HTTP方法: GET
 * 路径: /api/system/config-center/compat/value
 * 参数:
 *   - namespace: kotlin.String (RequestParam)
 *   - key: kotlin.String (RequestParam)
 *   - profile: kotlin.String? (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterCompatValueDto
 */
    @GET("/api/system/config-center/compat/value")    suspend fun getConfigCenterCompatValue(
        @Query("namespace") namespace: kotlin.String,
        @Query("key") key: kotlin.String,
        @Query("profile") profile: kotlin.String?
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterCompatValueDto

/**
 * createConfigCenterProject
 * HTTP方法: POST
 * 路径: /api/system/config-center/projects
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto
 */
    @POST("/api/system/config-center/projects")    suspend fun createConfigCenterProject(
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto

/**
 * createConfigCenterEnvironment
 * HTTP方法: POST
 * 路径: /api/system/config-center/projects/{projectId}/environments
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto
 */
    @POST("/api/system/config-center/projects/{projectId}/environments")    suspend fun createConfigCenterEnvironment(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto

/**
 * createConfigCenterConfig
 * HTTP方法: POST
 * 路径: /api/system/config-center/projects/{projectId}/configs
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto
 */
    @POST("/api/system/config-center/projects/{projectId}/configs")    suspend fun createConfigCenterConfig(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto

/**
 * createConfigCenterSecret
 * HTTP方法: POST
 * 路径: /api/system/config-center/secrets
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto
 */
    @POST("/api/system/config-center/secrets")    suspend fun createConfigCenterSecret(
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto

/**
 * issueConfigCenterServiceToken
 * HTTP方法: POST
 * 路径: /api/system/config-center/tokens
 * 参数:
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueResult
 */
    @POST("/api/system/config-center/tokens")    suspend fun issueConfigCenterServiceToken(
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueResult

/**
 * revokeConfigCenterServiceToken
 * HTTP方法: POST
 * 路径: /api/system/config-center/tokens/{tokenId}/revoke
 * 参数:
 *   - tokenId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenDto
 */
    @POST("/api/system/config-center/tokens/{tokenId}/revoke")    suspend fun revokeConfigCenterServiceToken(
        @Path("tokenId") tokenId: kotlin.Long
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenDto

/**
 * updateConfigCenterProject
 * HTTP方法: PUT
 * 路径: /api/system/config-center/projects/{projectId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto
 */
    @PUT("/api/system/config-center/projects/{projectId}")    suspend fun updateConfigCenterProject(
        @Path("projectId") projectId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectDto

/**
 * updateConfigCenterEnvironment
 * HTTP方法: PUT
 * 路径: /api/system/config-center/projects/{projectId}/environments/{environmentId}
 * 参数:
 *   - projectId: kotlin.Long (PathVariable)
 *   - environmentId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto
 */
    @PUT("/api/system/config-center/projects/{projectId}/environments/{environmentId}")    suspend fun updateConfigCenterEnvironment(
        @Path("projectId") projectId: kotlin.Long,
        @Path("environmentId") environmentId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentDto

/**
 * updateConfigCenterConfig
 * HTTP方法: PUT
 * 路径: /api/system/config-center/configs/{configId}
 * 参数:
 *   - configId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto
 */
    @PUT("/api/system/config-center/configs/{configId}")    suspend fun updateConfigCenterConfig(
        @Path("configId") configId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigDto

/**
 * updateConfigCenterSecret
 * HTTP方法: PUT
 * 路径: /api/system/config-center/secrets/{secretId}
 * 参数:
 *   - secretId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto
 */
    @PUT("/api/system/config-center/secrets/{secretId}")    suspend fun updateConfigCenterSecret(
        @Path("secretId") secretId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest
    ): site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretDto

/**
 * deleteConfigCenterSecret
 * HTTP方法: DELETE
 * 路径: /api/system/config-center/secrets/{secretId}
 * 参数:
 *   - secretId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/system/config-center/secrets/{secretId}")    suspend fun deleteConfigCenterSecret(
        @Path("secretId") secretId: kotlin.Long
    ): kotlin.Unit

}