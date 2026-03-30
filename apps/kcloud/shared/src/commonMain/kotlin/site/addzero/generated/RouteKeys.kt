package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val MCU_CONTROL_SCREEN = "mcu/control"
    const val MCU_FLASH_SCREEN = "mcu/flash"
    const val MCU_DEBUG_SCREEN = "mcu/debug"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "设备会话", title = "控制台", routePath = "mcu/control", icon = "PowerSettingsNew", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen", simpleName = "McuControlScreen"),
        Route(value = "开发工具", title = "烧录", routePath = "mcu/flash", icon = "Upload", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen", simpleName = "McuFlashScreen"),
        Route(value = "开发工具", title = "调试", routePath = "mcu/debug", icon = "BugReport", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen", simpleName = "McuDebugScreen")
    )
}
