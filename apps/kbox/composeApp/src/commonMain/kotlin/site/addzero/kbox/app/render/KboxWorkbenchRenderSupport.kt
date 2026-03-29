package site.addzero.kbox.app.render

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kbox.app.KboxRouteCatalog
import site.addzero.kbox.app.KboxRouteEntry
import site.addzero.kbox.app.KboxRouteScene
import site.addzero.kbox.app.KboxShellState

@Composable
internal fun rememberSelectedRoute(
    routeCatalog: KboxRouteCatalog,
    shellState: KboxShellState,
    routeVersion: Int,
): KboxRouteEntry? {
    val selectedRoutePath = shellState.selectedRoutePath
    return remember(routeCatalog, selectedRoutePath, routeVersion) {
        routeCatalog.findRoute(selectedRoutePath)
    }
}

internal fun Modifier.statusBarFrame(): Modifier {
    return fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(horizontal = 14.dp, vertical = 10.dp)
}

internal fun KboxRouteScene?.routeCount(): Int {
    return this?.routeCount ?: 0
}
