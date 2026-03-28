package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route

@Route(
    value = "设备会话",
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
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
                modifier = Modifier.width(340.dp).fillMaxHeight(),
            ) {
                McuSummaryTable(
                    rows = listOf(
                        "当前串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                        "波特率" to state.baudRateText,
                        "会话" to if (state.session.isOpen) "OPEN" else "CLOSED",
                        "DTR" to state.session.dtrEnabled.toString(),
                        "RTS" to state.session.rtsEnabled.toString(),
                        "脚本" to state.scriptStatus.state.name,
                        "烧录" to state.flashStatus.state.name,
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
