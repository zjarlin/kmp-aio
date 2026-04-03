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
        put(RouteKeys.MCU_CONTROL_SCREEN, { site.addzero.kcloud.shell.routebridge.mcu.McuControlRouteScreen() })
        put(RouteKeys.MCU_FLASH_SCREEN, { site.addzero.kcloud.shell.routebridge.mcu.McuFlashRouteScreen() })
        put(RouteKeys.MCU_MODBUS_SCREEN, { site.addzero.kcloud.shell.routebridge.mcu.McuModbusRouteScreen() })
        put(RouteKeys.MCU_ONLINE_DEV_SCREEN, { site.addzero.kcloud.shell.routebridge.mcu.McuOnlineDevRouteScreen() })
        put(RouteKeys.MCU_DEBUG_SCREEN, { site.addzero.kcloud.shell.routebridge.mcu.McuDebugRouteScreen() })
        put(RouteKeys.CONFIG_CENTER_PROJECTS_SCREEN, { site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterProjectsScreen() })
    }

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): RouteContent {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
