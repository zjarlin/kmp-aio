package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

@Route(
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            id = "device",
            name = "设备",
            icon = "Build",
            order = 0,
        ),
        menuPath = ["设备会话"],
    ),
)
@Composable
fun McuControlScreen() {
    val state = rememberMcuWorkbenchState()
    val scope = rememberCoroutineScope()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("扫描串口", Icons.Default.Search) {
                scope.launch {
                    state.refreshPorts()
                }
            },
            McuToolbarAction(
                label = "打开会话",
                icon = Icons.Default.PowerSettingsNew,
                enabled = !state.session.isOpen && state.selectedPortPath != null,
            ) {
                scope.launch {
                    state.openSession()
                }
            },
            McuToolbarAction(
                label = "关闭会话",
                icon = Icons.Default.Stop,
                enabled = state.session.isOpen,
            ) {
                scope.launch {
                    state.closeSession()
                }
            },
            McuToolbarAction(
                label = "确保运行时",
                icon = Icons.Default.Build,
                enabled = state.session.isOpen && state.selectedRuntimeBundle != null,
            ) {
                scope.launch {
                    state.ensureRuntime(forceReflash = false)
                }
            },
            McuToolbarAction(
                label = "复位",
                icon = Icons.Default.Refresh,
                enabled = state.session.isOpen,
            ) {
                scope.launch {
                    state.resetSession()
                }
            },
            McuToolbarAction(
                label = if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR",
                icon = Icons.Default.Settings,
                enabled = state.session.isOpen,
            ) {
                scope.launch {
                    state.updateDtr(!state.session.dtrEnabled)
                }
            },
            McuToolbarAction(
                label = if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS",
                icon = Icons.Default.Tune,
                enabled = state.session.isOpen,
            ) {
                scope.launch {
                    state.updateRts(!state.session.rtsEnabled)
                }
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            McuPanel(
                title = "串口列表",
                modifier = Modifier.width(320.dp).fillMaxHeight(),
            ) {
                McuPortBrowser(
                    state = state,
                    onRefresh = {
                        scope.launch {
                            state.refreshPorts()
                        }
                    },
                )
            }

            McuPanel(
                title = "会话状态",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            ) {
                McuSummaryTable(
                    rows = listOf(
                        "当前串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                        "波特率" to state.baudRateText,
                        "会话" to if (state.session.isOpen) "OPEN" else "CLOSED",
                        "运行时" to state.runtimeStatus.state.name,
                        "Bundle" to (state.runtimeStatus.bundleTitle ?: state.selectedRuntimeBundle?.title.orEmpty()),
                        "Profile" to (state.runtimeStatus.defaultFlashProfileId ?: state.selectedRuntimeBundle?.defaultFlashProfileId.orEmpty()),
                        "脚本" to state.scriptStatus.state.name,
                        "烧录" to state.flashStatus.state.name,
                        "DTR" to state.session.dtrEnabled.toString(),
                        "RTS" to state.session.rtsEnabled.toString(),
                        "消息" to (state.runtimeStatus.lastMessage ?: state.scriptStatus.lastMessage.orEmpty()),
                        "最后错误" to state.session.lastError.orEmpty(),
                    ),
                )
            }

            McuPanel(
                title = "最近事件",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                McuEventFeed(events = state.events.takeLast(80))
            }
        }
    }
}
