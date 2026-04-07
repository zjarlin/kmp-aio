package site.addzero.kcloud.plugins.hostconfig.api.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val code: Int,
    val msg: String? = null,
    val data: Any? = null,
)
