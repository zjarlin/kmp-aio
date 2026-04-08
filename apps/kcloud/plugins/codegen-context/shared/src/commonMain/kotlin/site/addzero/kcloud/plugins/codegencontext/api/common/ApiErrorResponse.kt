package site.addzero.kcloud.plugins.codegencontext.api.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val code: Int,
    val msg: String,
    val data: String? = null,
)
