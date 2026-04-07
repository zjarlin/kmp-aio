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
        put(RouteKeys.PROJECTS_SCREEN, { site.addzero.kcloud.plugins.hostconfig.screen.ProjectsScreen() })
        put(RouteKeys.MCU_CONTROL_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.control.McuControlScreen() })
        put(RouteKeys.PROTOCOLS_SCREEN, { site.addzero.kcloud.plugins.hostconfig.screen.ProtocolsScreen() })
        put(RouteKeys.MCU_FLASH_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashScreen() })
        put(RouteKeys.CLOUD_SCREEN, { site.addzero.kcloud.plugins.hostconfig.screen.CloudScreen() })
        put(RouteKeys.MCU_DEBUG_SCREEN, { site.addzero.kcloud.plugins.mcuconsole.debug.McuDebugScreen() })
        put(RouteKeys.GATEWAY_SCREEN, { site.addzero.kcloud.plugins.hostconfig.screen.GatewayScreen() })
    }

    /**
     * 根据路由键获取对应的Composable函数
     */
    operator fun get(routeKey: String): RouteContent {
        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found: $routeKey")
    }
}
