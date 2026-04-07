package site.addzero.kcloud.plugins.hostconfig.api.common

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val d: List<T>,
    val t: Long,
    val p: Int,
)
