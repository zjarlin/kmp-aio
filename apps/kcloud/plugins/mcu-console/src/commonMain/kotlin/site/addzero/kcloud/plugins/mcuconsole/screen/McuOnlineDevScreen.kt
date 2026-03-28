package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind

@Route(
    value = "开发工具",
    title = "在线开发",
    routePath = "mcu/online-dev",
    icon = "Build",
    order = 15.0,
)
@Composable
fun McuOnlineDevScreen() {
    val state = rememberMcuWorkbenchState()
    val scope = rememberCoroutineScope()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("刷新", Icons.Default.Refresh) {
                scope.launch {
                    state.refreshAll()
                }
            },
            McuToolbarAction(
                label = "执行脚本",
                icon = Icons.Default.PlayArrow,
                enabled = state.hasActiveSession && !state.isScriptRunning,
            ) {
                scope.launch {
                    state.executeScript()
                }
            },
            McuToolbarAction(
                label = "停止脚本",
                icon = Icons.Default.Stop,
                enabled = state.hasActiveSession && state.isScriptRunning,
            ) {
                scope.launch {
                    state.stopScript()
                }
            },
            McuToolbarAction("清空日志", Icons.Default.Build) {
                state.clearVisibleEvents()
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            McuPanel(
                title = "脚本",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                OutlinedTextField(
                    value = state.scriptText,
                    onValueChange = { state.scriptText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    label = {
                        Text("rhai")
                    },
                )
            }

            McuPanel(
                title = "运行态",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            ) {
                McuCompactInput(
                    value = state.timeoutMsText,
                    onValueChange = { state.timeoutMsText = it },
                    label = "timeoutMs",
                )
                McuSummaryTable(
                    rows = listOf(
                        "串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                        "会话" to if (state.session.isOpen) "OPEN" else "CLOSED",
                        "脚本状态" to state.scriptStatus.state.name,
                        "活动请求" to state.scriptStatus.activeRequestId.orEmpty(),
                        "最近请求" to state.scriptStatus.lastRequestId.orEmpty(),
                        "消息" to state.scriptStatus.lastMessage.orEmpty(),
                    ),
                )
                McuEventFeed(
                    events = state.events.filter { event ->
                        event.kind == McuEventKind.TX_FRAME || event.kind == McuEventKind.RX_FRAME
                    }.takeLast(40),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
