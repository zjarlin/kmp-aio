package site.addzero.kcloud

import io.ktor.server.routing.Route

/**
 * 由插件市场维护的服务端插件路由聚合入口。
 */
fun Route.registerKCloudPluginRoutes() {
    // <managed:plugin-market-server-routes:start>
    invokeRouteRegistrar("site.addzero.configcenter.ktor.ConfigCenterRoutesKt", "configCenterRoutes")
    invokeRouteRegistrar("site.addzero.kcloud.plugins.rbac.RbacRoutesKt", "rbacRoutes")
    invokeRouteRegistrar("site.addzero.kcloud.plugins.system.aichat.AiChatRoutesKt", "aiChatRoutes")
    invokeRouteRegistrar("site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseRoutesKt", "knowledgeBaseRoutes")
    invokeRouteRegistrar("site.addzero.kcloud.plugins.mcuconsole.McuConsoleRoutesKt", "mcuConsoleRoutes")
    invokeRouteRegistrar("site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketRoutesKt", "pluginMarketRoutes")
    invokeRouteRegistrar("site.addzero.vibepocket.routes.VibePocketRoutesKt", "vibePocketRoutes")
    // <managed:plugin-market-server-routes:end>
}

private fun Route.invokeRouteRegistrar(
    className: String,
    methodName: String,
) {
    val registrarClass = Class.forName(className)
    val registrarMethod = registrarClass.getDeclaredMethod(methodName, Route::class.java)
    registrarMethod.invoke(null, this)
}
