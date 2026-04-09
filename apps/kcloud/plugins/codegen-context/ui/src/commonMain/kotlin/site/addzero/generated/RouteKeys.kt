package site.addzero.generated

import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

/**
 * 路由键
 * 请勿手动修改此文件
 */
object RouteKeys {
    const val CODEGEN_CONTEXT_SCREEN = "codegen-context/contexts"

    /**
     * 所有路由元数据
     */
    val allMeta = listOf(
        Route(value = "", title = "代码生成上下文", routePath = "codegen-context/contexts", icon = "Code", order = 40.0, placement = RoutePlacement(scene = RouteScene(name = "宿主配置", icon = "SettingsApplications", order = 10), defaultInScene = false), qualifiedName = "site.addzero.kcloud.plugins.codegencontext.screen.CodegenContextScreen", simpleName = "CodegenContextScreen")
    )
}
