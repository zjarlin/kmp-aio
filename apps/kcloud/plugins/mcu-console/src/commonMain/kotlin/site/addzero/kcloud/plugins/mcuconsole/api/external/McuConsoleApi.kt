package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuEventBatchResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRequest
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuPortsResponse
import site.addzero.kcloud.plugins.mcuconsole.McuResetRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionLinesRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionOpenRequest
import site.addzero.kcloud.plugins.mcuconsole.McuSessionSnapshot
import site.addzero.kcloud.plugins.mcuconsole.McuSignalRequest

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
}
