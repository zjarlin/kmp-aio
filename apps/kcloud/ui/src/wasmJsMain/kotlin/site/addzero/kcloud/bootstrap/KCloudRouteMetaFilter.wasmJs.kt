package site.addzero.kcloud.bootstrap

import site.addzero.annotation.Route

internal actual fun filterKCloudRouteMeta(
    routeMeta: List<Route>,
): List<Route> {
    return routeMeta.filterNot { route ->
        route.routePath.startsWith("mcu/")
    }
}
