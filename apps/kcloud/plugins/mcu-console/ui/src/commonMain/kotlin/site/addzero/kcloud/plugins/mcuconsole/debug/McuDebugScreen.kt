package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.workbench.*
import site.addzero.kcloud.plugins.mcuconsole.workbench.cupertino.*

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
    val state: McuConsoleWorkbenchState = koinInject()
    val workbenchState = rememberMcuWorkbenchState(state)
    val runAction = rememberMcuActionRunner()

    McuCupertinoScene {
        McuWorkbenchFrame(
            state = workbenchState,
            actions = {
                McuCupertinoSecondaryButton(
                    text = "刷新",
                    onClick = {
                        runAction {
                            workbenchState.loadRecentEvents()
                            workbenchState.refreshRuntimeStatus()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "清空日志",
                    onClick = {
                        workbenchState.clearVisibleEvents()
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
                            "串口" to (workbenchState.session.portPath ?: workbenchState.selectedPortPath.orEmpty()),
                            "Bundle" to (workbenchState.runtimeStatus.bundleTitle ?: workbenchState.selectedRuntimeBundle?.title.orEmpty()),
                            "运行时" to workbenchState.runtimeStatus.state.name,
                            "烧录" to workbenchState.flashStatus.state.name,
                            "消息" to workbenchState.runtimeStatus.lastMessage.orEmpty(),
                        ),
                    )
                }

                McuPanel(
                    title = "协议与串口日志",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    McuEventFeed(events = workbenchState.events.takeLast(200))
                }
            }
        }
    }
}
