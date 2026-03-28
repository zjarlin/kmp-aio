package site.addzero.generated

import site.addzero.annotation.Route

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val MCU_CONTROL_SCREEN = "mcu/control"
    const val MCU_FLASH_SCREEN = "mcu/flash"
    const val MCU_DEBUG_SCREEN = "mcu/debug"
    const val RBAC_USER_SCREEN = "system/rbac"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "设备", title = "控制台", routePath = "mcu/control", icon = "PowerSettingsNew", order = 0.0, qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuControlScreen", simpleName = "McuControlScreen"),
        Route(value = "设备", title = "烧录", routePath = "mcu/flash", icon = "Upload", order = 10.0, qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuFlashScreen", simpleName = "McuFlashScreen"),
        Route(value = "设备", title = "调试", routePath = "mcu/debug", icon = "BugReport", order = 20.0, qualifiedName = "site.addzero.kcloud.plugins.mcuconsole.screen.McuDebugScreen", simpleName = "McuDebugScreen"),
        Route(value = "系统", title = "RBAC权限管理", routePath = "system/rbac", icon = "AdminPanelSettings", order = 20.0, qualifiedName = "site.addzero.kcloud.plugins.rbac.screen.RbacUserScreen", simpleName = "RbacUserScreen")
    )
}
