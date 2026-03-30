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
        put(RouteKeys.MCU_CONTROL_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen() })
        put(RouteKeys.MCU_FLASH_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen() })
        put(RouteKeys.MCU_MODBUS_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.screen.McuModbusScreen() })
        put(RouteKeys.MCU_DEBUG_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen() })
    }

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): RouteContent {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
