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
    const val MCU_MODBUS_SCREEN = "mcu/modbus"
    const val MCU_ONLINE_DEV_SCREEN = "mcu/online-dev"
    const val MCU_DEBUG_SCREEN = "mcu/debug"
    const val CONFIG_CENTER_PROJECTS_SCREEN = "system/config-center/value"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "设备会话", title = "控制台", routePath = "mcu/control", icon = "PowerSettingsNew", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "物联网上位机", icon = "Build", order = 0), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.control.McuControlScreen", simpleName = "McuControlScreen"),
        Route(value = "开发工具", title = "烧录", routePath = "mcu/flash", icon = "Upload", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "物联网上位机", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashScreen", simpleName = "McuFlashScreen"),
        Route(value = "开发工具", title = "Modbus", routePath = "mcu/modbus", icon = "SettingsInputComponent", order = 15.0, placement = RoutePlacement(scene = RouteScene(name = "物联网上位机", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusScreen", simpleName = "McuModbusScreen"),
        Route(value = "开发工具", title = "在线开发", routePath = "mcu/online-dev", icon = "Code", order = 15.0, placement = RoutePlacement(scene = RouteScene(name = "物联网上位机", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.onlinedev.McuOnlineDevScreen", simpleName = "McuOnlineDevScreen"),
        Route(value = "开发工具", title = "调试", routePath = "mcu/debug", icon = "BugReport", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "物联网上位机", icon = "Build", order = 0), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.debug.McuDebugScreen", simpleName = "McuDebugScreen"),
        Route(value = "配置中心", title = "配置项", routePath = "system/config-center/value", icon = "Key", order = 80.0, placement = RoutePlacement(scene = RouteScene(name = "配置中心", icon = "Key", order = 90), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.system.configcenter.screen.ConfigCenterProjectsScreen", simpleName = "ConfigCenterProjectsScreen")
    )
}
