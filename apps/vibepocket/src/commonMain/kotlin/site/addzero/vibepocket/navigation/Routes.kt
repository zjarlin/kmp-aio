package site.addzero.vibepocket.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 通用路由键 — 直接用 routeKey 字符串（Composable 函数全限定名）作为 Nav3 的 key。
 *
 * 这样 MenuMetadata.routeKey 包一层就能用，不需要为每个页面定义单独的 data object。
 */
@Serializable
data class RouteKey(val key: String) : NavKey
