package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.vibepocket.model.*

/**
 * Persona 相关接口
 */
interface PersonaApi {

    @POST("api/personas")
    suspend fun savePersona(@Body request: PersonaSaveRequest): PersonaItem

    @GET("api/personas")
    suspend fun getPersonas(): List<PersonaItem>
}
