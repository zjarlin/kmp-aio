package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
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
    val viewModel: McuDebugViewModel = koinViewModel()
    val state = rememberMcuWorkbenchState(viewModel.state)
    val runAction = rememberMcuActionRunner()

    McuCupertinoScene {
        McuWorkbenchFrame(
            state = state,
            actions = {
                McuCupertinoSecondaryButton(
                    text = "刷新",
                    onClick = {
                        runAction {
                            state.loadRecentEvents()
                            state.refreshRuntimeStatus()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "清空日志",
                    onClick = {
                        state.clearVisibleEvents()
                    },
                )
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuPanel(
                    title = "调试状态",
                    modifier = Modifier.width(360.dp).fillMaxHeight(),
                ) {
                    McuCupertinoSummarySection(
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
}
