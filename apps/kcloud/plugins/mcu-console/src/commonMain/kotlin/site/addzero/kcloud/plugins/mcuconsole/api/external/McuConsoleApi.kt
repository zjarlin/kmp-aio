package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.*

interface McuConsoleApi {
    @GET("api/mcu/ports")
    suspend fun listPorts(): McuPortsResponse

    @GET("api/mcu/session")
    suspend fun getSession(): McuSessionSnapshot

    @Headers("Content-Type: application/json")
    @POST("api/mcu/session/open")
    suspend fun openSession(@Body request: McuSessionOpenRequest): McuSessionSnapshot

    @POST("api/mcu/session/close")
    suspend fun closeSession(): McuSessionSnapshot

    @Headers("Content-Type: application/json")
    @POST("api/mcu/session/reset")
    suspend fun resetSession(@Body request: McuResetRequest): McuSessionSnapshot

    @Headers("Content-Type: application/json")
    @POST("api/mcu/session/signals")
    suspend fun updateSignals(@Body request: McuSignalRequest): McuSessionSnapshot

    @Headers("Content-Type: application/json")
    @POST("api/mcu/session/lines")
    suspend fun readRecentLines(@Body request: McuSessionLinesRequest): McuEventBatchResponse

    @GET("api/mcu/events")
    suspend fun readEvents(@Query("afterSeq") afterSeq: Long): McuEventBatchResponse

    @Headers("Content-Type: application/json")
    @POST("api/mcu/script/execute")
    suspend fun executeScript(@Body request: McuScriptExecuteRequest): McuScriptStatusResponse

    @Headers("Content-Type: application/json")
    @POST("api/mcu/script/stop")
    suspend fun stopScript(@Body request: McuScriptStopRequest): McuScriptStatusResponse

    @GET("api/mcu/script/status")
    suspend fun getScriptStatus(): McuScriptStatusResponse

    @GET("api/mcu/flash/profiles")
    suspend fun listFlashProfiles(): McuFlashProfilesResponse

    @Headers("Content-Type: application/json")
    @POST("api/mcu/flash/start")
    suspend fun startFlash(@Body request: McuFlashRequest): McuFlashStatusResponse

    @GET("api/mcu/flash/status")
    suspend fun getFlashStatus(): McuFlashStatusResponse

    @GET("api/mcu/runtime/bundles")
    suspend fun listRuntimeBundles(): McuRuntimeBundlesResponse

    @Headers("Content-Type: application/json")
    @POST("api/mcu/runtime/ensure")
    suspend fun ensureRuntime(@Body request: McuRuntimeEnsureRequest): McuRuntimeStatusResponse

    @GET("api/mcu/runtime/status")
    suspend fun getRuntimeStatus(): McuRuntimeStatusResponse
}
