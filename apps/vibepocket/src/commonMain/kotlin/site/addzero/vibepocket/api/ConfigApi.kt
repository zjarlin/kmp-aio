package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import site.addzero.vibepocket.model.ConfigEntry
import site.addzero.vibepocket.model.ConfigResponse
import site.addzero.vibepocket.model.StorageConfig

interface ConfigApi {

    // ── 通用配置 ──────────────────────────────────────────────

    @GET("api/config/{key}")
    suspend fun getConfig(@Path("key") key: String): ConfigResponse


    @PUT("api/config")
    suspend fun updateConfig(@Body entry: ConfigEntry)

    // ── 存储配置 ──────────────────────────────────────────────

    @GET("api/config/storage")
    suspend fun getStorageConfig(): StorageConfig

    @PUT("api/config/storage")
    suspend fun saveStorageConfig(@Body config: StorageConfig)
}
