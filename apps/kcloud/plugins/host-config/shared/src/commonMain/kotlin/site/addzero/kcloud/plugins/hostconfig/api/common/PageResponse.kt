package site.addzero.kcloud.plugins.hostconfig.api.common

import kotlinx.serialization.Serializable

@Serializable
/**
 * 表示分页响应结果。
 *
 * @property d d。
 * @property t t。
 * @property p p。
 */
data class PageResponse<T>(
    val d: List<T>,
    val t: Long,
    val p: Int,
)
