package site.addzero.kcloud.bootstrap

import site.addzero.annotation.Route

internal expect fun filterRouteMeta(
    routeMeta: List<Route>,
): List<Route>
