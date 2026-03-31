package site.addzero.generated

import androidx.compose.runtime.Composable

typealias RouteContent = @Composable () -> Unit

/**
 * 路由表
 * 请勿手动修改此文件
 */
object RouteTable {
    /**
     * 所有路由映射
     */
    val allRoutes: Map<String, RouteContent> = mutableMapOf<String, RouteContent>().apply {
        put(RouteKeys.KBOX_STORAGE_TOOL_SCREEN, { site.addzero.kbox.plugins.tools.storagetool.screen.KboxStorageToolScreen() })
        put(RouteKeys.KBOX_PLUGIN_MANAGER_SCREEN, { site.addzero.kbox.plugins.system.pluginmanager.screen.KboxPluginManagerScreen() })
    }

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): RouteContent {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
