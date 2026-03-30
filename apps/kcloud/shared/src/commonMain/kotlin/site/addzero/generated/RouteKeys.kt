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

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "设备会话", title = "控制台", routePath = "mcu/control", icon = "PowerSettingsNew", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "设备", icon = "Build", order = 0), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen", simpleName = "McuControlScreen")
    )
}
