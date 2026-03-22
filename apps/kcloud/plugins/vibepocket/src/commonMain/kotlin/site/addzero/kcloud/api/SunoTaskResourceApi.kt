package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import site.addzero.vibepocket.model.SunoTaskResourceItem
import site.addzero.vibepocket.model.SunoTaskResourceSaveRequest

interface SunoTaskResourceApi {

    @Headers("Content-Type: application/json")
    @POST("api/suno/resources")
    suspend fun save(@Body request: SunoTaskResourceSaveRequest): SunoTaskResourceItem

    @GET("api/suno/resources")
    suspend fun list(): List<SunoTaskResourceItem>

    @GET("api/suno/resources/{taskId}")
    suspend fun get(@Path("taskId") taskId: String): SunoTaskResourceItem
}
