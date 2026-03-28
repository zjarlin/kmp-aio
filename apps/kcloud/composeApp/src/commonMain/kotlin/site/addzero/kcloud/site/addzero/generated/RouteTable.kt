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
        RouteKeys.MCU_CONTROL_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen() },
        RouteKeys.MCU_FLASH_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen() },
        RouteKeys.MCU_DEBUG_SCREEN to @Composable { site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen() },
        RouteKeys.RBAC_USER_SCREEN to @Composable { site.addzero.kcloud.plugins.rbac.screen.RbacUserScreen() }
    )

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): @Composable () -> Unit {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
