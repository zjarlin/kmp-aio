package site.addzero.kcloud.system.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PUT

interface UserCenterApi {
    @GET("api/system/user/profile")
    suspend fun getCurrentProfile(): UserProfileDto

    @Headers("Content-Type: application/json")
    @PUT("api/system/user/profile")
    suspend fun saveCurrentProfile(@Body request: UserProfileUpdateRequest): UserProfileDto
}
