package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import site.addzero.annotation.Route

@Route(
    value = "开发工具",
    title = "调试",
    routePath = "mcu/debug",
    icon = "BugReport",
    order = 20.0,
)
@Composable
fun McuDebugScreen() {
    val state = rememberMcuWorkbenchState()
    val scope = rememberCoroutineScope()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("刷新", Icons.Default.Refresh) {
                scope.launch {
                    state.loadRecentEvents()
                    state.refreshScriptStatus()
                }
            },
            McuToolbarAction("清空日志", Icons.Default.Stop) {
                state.clearVisibleEvents()
            },
        ),
    ) {
        McuPanel(
            title = "协议与串口日志",
            modifier = Modifier.weight(1f),
        ) {
            McuEventFeed(events = state.events.takeLast(200))
        }
    }
}
