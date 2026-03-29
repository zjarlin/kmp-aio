package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val KBOX_STORAGE_TOOL_SCREEN = "tools/storage-tool"
    const val KBOX_PLUGIN_MANAGER_SCREEN = "system/plugin-manager"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "本地收纳", title = "安装包与远端迁移", routePath = "tools/storage-tool", icon = "Inventory2", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "工具箱", icon = "Inventory2", order = 200), defaultInScene = true), qualifiedName = "site.addzero.kbox.plugins.tools.storagetool.screen.KboxStorageToolScreen", simpleName = "KboxStorageToolScreen"),
        Route(value = "插件运行时", title = "插件管理", routePath = "system/plugin-manager", icon = "Extension", order = 100.0, placement = RoutePlacement(scene = RouteScene(name = "系统", icon = "AdminPanelSettings", order = 100), defaultInScene = false), qualifiedName = "site.addzero.kbox.plugins.system.pluginmanager.screen.KboxPluginManagerScreen", simpleName = "KboxPluginManagerScreen")
    )
}
