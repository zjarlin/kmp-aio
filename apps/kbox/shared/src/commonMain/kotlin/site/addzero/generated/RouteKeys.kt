package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val KBOX_PLUGIN_MANAGER_SCREEN = ""
    const val KBOX_STORAGE_TOOL_SCREEN = ""

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "插件运行时", title = "插件管理", routePath = "", icon = "Extension", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 2147483647), defaultInScene = false), qualifiedName = "site.addzero.kbox.plugins.system.pluginmanager.screen.KboxPluginManagerScreen", simpleName = "KboxPluginManagerScreen"),
        Route(value = "环境资产", title = "环境资产管理", routePath = "", icon = "Inventory2", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "工具箱", icon = "Inventory2", order = 2147483647), defaultInScene = false), qualifiedName = "site.addzero.kbox.plugins.tools.storagetool.screen.KboxStorageToolScreen", simpleName = "KboxStorageToolScreen")
    )
}
