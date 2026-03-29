package site.addzero.kcloud.plugins.system.configcenter.api

import de.jensklingenberg.ktorfit.http.*

interface ConfigCenterApi {
    @GET("api/system/config-center/projects")
    suspend fun listConfigCenterProjects(): List<ConfigCenterProjectDto>

    @POST("api/system/config-center/projects")
    suspend fun createConfigCenterProject(
        @Body request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto

    @PUT("api/system/config-center/projects/{projectId}")
    suspend fun updateConfigCenterProject(
        @Path("projectId") projectId: Long,
        @Body request: ConfigCenterProjectMutationRequest,
    ): ConfigCenterProjectDto

    @GET("api/system/config-center/projects/{projectId}/environments")
    suspend fun listConfigCenterEnvironments(
        @Path("projectId") projectId: Long,
    ): List<ConfigCenterEnvironmentDto>

    @POST("api/system/config-center/projects/{projectId}/environments")
    suspend fun createConfigCenterEnvironment(
        @Path("projectId") projectId: Long,
        @Body request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto

    @PUT("api/system/config-center/projects/{projectId}/environments/{environmentId}")
    suspend fun updateConfigCenterEnvironment(
        @Path("projectId") projectId: Long,
        @Path("environmentId") environmentId: Long,
        @Body request: ConfigCenterEnvironmentMutationRequest,
    ): ConfigCenterEnvironmentDto

    @GET("api/system/config-center/projects/{projectId}/configs")
    suspend fun listConfigCenterConfigs(
        @Path("projectId") projectId: Long,
    ): List<ConfigCenterConfigDto>

    @POST("api/system/config-center/projects/{projectId}/configs")
    suspend fun createConfigCenterConfig(
        @Path("projectId") projectId: Long,
        @Body request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto

    @PUT("api/system/config-center/configs/{configId}")
    suspend fun updateConfigCenterConfig(
        @Path("configId") configId: Long,
        @Body request: ConfigCenterConfigMutationRequest,
    ): ConfigCenterConfigDto

    @GET("api/system/config-center/configs/{configId}/secrets")
    suspend fun listConfigCenterSecrets(
        @Path("configId") configId: Long,
        @Query("includeInherited") includeInherited: Boolean = true,
    ): List<ConfigCenterSecretDto>

    @POST("api/system/config-center/secrets")
    suspend fun createConfigCenterSecret(
        @Body request: ConfigCenterSecretMutationRequest,
    ): ConfigCenterSecretDto

    @PUT("api/system/config-center/secrets/{secretId}")
    suspend fun updateConfigCenterSecret(
        @Path("secretId") secretId: Long,
        @Body request: ConfigCenterSecretMutationRequest,
    ): ConfigCenterSecretDto

    @DELETE("api/system/config-center/secrets/{secretId}")
    suspend fun deleteConfigCenterSecret(
        @Path("secretId") secretId: Long,
    )

    @GET("api/system/config-center/secrets/{secretId}/versions")
    suspend fun listConfigCenterSecretVersions(
        @Path("secretId") secretId: Long,
    ): List<ConfigCenterSecretVersionDto>

    @GET("api/system/config-center/configs/{configId}/tokens")
    suspend fun listConfigCenterServiceTokens(
        @Path("configId") configId: Long,
    ): List<ConfigCenterServiceTokenDto>

    @POST("api/system/config-center/tokens")
    suspend fun issueConfigCenterServiceToken(
        @Body request: ConfigCenterServiceTokenIssueRequest,
    ): ConfigCenterServiceTokenIssueResult

    @POST("api/system/config-center/tokens/{tokenId}/revoke")
    suspend fun revokeConfigCenterServiceToken(
        @Path("tokenId") tokenId: Long,
    ): ConfigCenterServiceTokenDto

    @GET("api/system/config-center/projects/{projectId}/activities")
    suspend fun listConfigCenterActivityLogs(
        @Path("projectId") projectId: Long,
        @Query("limit") limit: Int = 50,
    ): List<ConfigCenterActivityLogDto>
}
