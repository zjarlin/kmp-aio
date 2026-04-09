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

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "", title = "工程配置", routePath = "host-config/projects", icon = "SettingsApplications", order = 0.0, placement = RoutePlacement(scene = RouteScene(name = "元数据配置", icon = "SettingsApplications", order = -10), defaultInScene = true), qualifiedName = "site.addzero.kcloud.plugins.hostconfig.screen.ProjectsScreen", simpleName = "ProjectsScreen")
    )
}
