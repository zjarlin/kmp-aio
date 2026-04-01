package site.addzero.kcloud.ui.app.render

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.ui.app.KCloudRouteCatalog
import site.addzero.kcloud.ui.app.KCloudRouteEntry
import site.addzero.kcloud.ui.app.KCloudRouteScene
import site.addzero.kcloud.ui.app.KCloudShellState

@Composable
internal fun rememberSelectedRoute(
    routeCatalog: KCloudRouteCatalog,
    shellState: KCloudShellState,
): KCloudRouteEntry? {
    val selectedRoutePath = shellState.selectedRoutePath
    return remember(routeCatalog, selectedRoutePath) {
        routeCatalog.findRoute(selectedRoutePath)
    }
}

/** 底部状态条：保留轻分隔，不再额外套一层发灰卡片。 */
internal fun Modifier.statusBarFrame(): Modifier {
    return fillMaxWidth()
        .padding(horizontal = 18.dp)
        .padding(horizontal = 14.dp, vertical = 10.dp)
}

internal fun KCloudRouteScene?.routeCount(): Int {
    return this?.routeCount ?: 0
}
