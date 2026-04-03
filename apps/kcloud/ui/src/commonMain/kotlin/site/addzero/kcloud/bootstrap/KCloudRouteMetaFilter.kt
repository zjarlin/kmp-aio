package site.addzero.kcloud.bootstrap

import site.addzero.annotation.Route

internal expect fun filterKCloudRouteMeta(
    routeMeta: List<Route>,
): List<Route>
