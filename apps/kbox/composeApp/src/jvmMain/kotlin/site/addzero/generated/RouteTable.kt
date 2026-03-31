package site.addzero.generated

import androidx.compose.runtime.Composable

/**
 * 路由表
 * 请勿手动修改此文件
 */
object RouteTable {
    /**
     * 所有路由映射
     */
    val allRoutes: Map<String, @Composable () -> Unit> = mapOf(
        RouteKeys.KBOX_PLUGIN_MANAGER_SCREEN to @Composable { site.addzero.kbox.plugins.system.pluginmanager.screen.KboxPluginManagerScreen() },
        RouteKeys.KBOX_STORAGE_TOOL_SCREEN to @Composable { site.addzero.kbox.plugins.tools.storagetool.screen.KboxStorageToolScreen() }
    )

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): @Composable () -> Unit {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
