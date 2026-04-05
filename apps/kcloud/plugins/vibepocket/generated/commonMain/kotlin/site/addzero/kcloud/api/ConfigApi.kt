package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.Config.kt
 * 基础路径: 
 */
interface ConfigApi {

    /**
     * getRuntimeInfo
     * HTTP方法: GET
     * 路径: /api/config/runtime
     * 返回类型: site.addzero.kcloud.vibepocket.routes.ConfigRuntimeInfo
     */
    @GET("/api/config/runtime")
    suspend fun getRuntimeInfo(): site.addzero.kcloud.vibepocket.routes.ConfigRuntimeInfo

    /**
     * getConfig
     * HTTP方法: GET
     * 路径: /api/config/{key}
     * 参数:
     *   - key: kotlin.String (PathVariable)
     * 返回类型: site.addzero.kcloud.vibepocket.routes.ConfigResponse
     */
    @GET("/api/config/{key}")
    suspend fun getConfig(
        @Path("key") key: kotlin.String
    ): site.addzero.kcloud.vibepocket.routes.ConfigResponse

    /**
     * getStorageConfig
     * HTTP方法: GET
     * 路径: /api/config/storage
     * 返回类型: site.addzero.kcloud.vibepocket.routes.StorageConfig
     */
    @GET("/api/config/storage")
    suspend fun getStorageConfig(): site.addzero.kcloud.vibepocket.routes.StorageConfig

    /**
     * updateConfig
     * HTTP方法: PUT
     * 路径: /api/config
     * 参数:
     *   - entry: site.addzero.kcloud.vibepocket.routes.ConfigEntry (RequestBody)
     * 返回类型: site.addzero.kcloud.vibepocket.dto.OkResponse
     */
    @PUT("/api/config")
    @Headers("Content-Type: application/json")
    suspend fun updateConfig(
        @Body entry: site.addzero.kcloud.vibepocket.routes.ConfigEntry
    ): site.addzero.kcloud.vibepocket.dto.OkResponse

    /**
     * saveStorageConfig
     * HTTP方法: PUT
     * 路径: /api/config/storage
     * 参数:
     *   - config: site.addzero.kcloud.vibepocket.routes.StorageConfig (RequestBody)
     * 返回类型: site.addzero.kcloud.vibepocket.dto.OkResponse
     */
    @PUT("/api/config/storage")
    @Headers("Content-Type: application/json")
    suspend fun saveStorageConfig(
        @Body config: site.addzero.kcloud.vibepocket.routes.StorageConfig
    ): site.addzero.kcloud.vibepocket.dto.OkResponse

}