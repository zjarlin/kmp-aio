package site.addzero.kcloud.plugins.hostconfig.api.common

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示API错误响应结果。
 *
 * @property code 编码。
 * @property msg msg。
 * @property @Contextual contextual。
 */
data class ApiErrorResponse(
    val code: Int,
    val msg: String? = null,
    @Contextual val data: Any? = null,
)
