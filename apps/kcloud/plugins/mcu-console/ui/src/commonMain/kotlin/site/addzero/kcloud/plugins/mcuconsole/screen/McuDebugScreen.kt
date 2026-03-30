package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

@Route(
    value = "开发工具",
    title = "调试",
    routePath = "mcu/debug",
    icon = "BugReport",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "设备",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuDebugScreen() {
    val state = rememberMcuWorkbenchState()
    val runAction = rememberMcuActionRunner()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("刷新", Icons.Default.Refresh) {
                runAction {
                    state.loadRecentEvents()
                    state.refreshRuntimeStatus()
                }
            },
            McuToolbarAction("清空日志", Icons.Default.Stop) {
                state.clearVisibleEvents()
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            McuPanel(
                title = "调试状态",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            ) {
                McuSummaryTable(
                    rows = listOf(
                        "串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                        "Bundle" to (state.runtimeStatus.bundleTitle ?: state.selectedRuntimeBundle?.title.orEmpty()),
                        "运行时" to state.runtimeStatus.state.name,
                        "烧录" to state.flashStatus.state.name,
                        "消息" to state.runtimeStatus.lastMessage.orEmpty(),
                    ),
                )
            }

            McuPanel(
                title = "协议与串口日志",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                McuEventFeed(events = state.events.takeLast(200))
            }
        }
    }
}
