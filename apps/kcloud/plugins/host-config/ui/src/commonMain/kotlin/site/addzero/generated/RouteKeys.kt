package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val PROJECTS_SCREEN = "host-config/projects"
    const val CATALOG_SCREEN = "host-config/catalog"
    const val PROTOCOLS_SCREEN = "host-config/protocols"
    const val CLOUD_SCREEN = "host-config/cloud"
    const val GATEWAY_SCREEN = "host-config/gateway"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "", title = "工程配置", routePath = "host-config/projects", icon = "SettingsApplications", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.ProjectsScreen", simpleName = "ProjectsScreen"),
        Route(value = "", title = "产品目录", routePath = "host-config/catalog", icon = "Category", order = 5.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.CatalogScreen", simpleName = "CatalogScreen"),
        Route(value = "", title = "协议模板", routePath = "host-config/protocols", icon = "Key", order = 10.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.ProtocolsScreen", simpleName = "ProtocolsScreen"),
        Route(value = "", title = "云接入", routePath = "host-config/cloud", icon = "Apps", order = 20.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.CloudScreen", simpleName = "CloudScreen"),
        Route(value = "", title = "网关配置", routePath = "host-config/gateway", icon = "SettingsInputComponent", order = 30.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.GatewayScreen", simpleName = "GatewayScreen")
    )
}
