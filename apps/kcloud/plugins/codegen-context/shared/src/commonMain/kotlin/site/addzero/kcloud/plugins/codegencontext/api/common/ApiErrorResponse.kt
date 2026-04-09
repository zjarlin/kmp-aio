package site.addzero.kcloud.plugins.codegencontext.api.common

import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示API错误响应结果。
 *
 * @property code 编码。
 * @property msg msg。
 * @property data 数据。
 */
data class ApiErrorResponse(
    val code: Int,
    val msg: String,
    val data: String? = null,
)
