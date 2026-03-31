package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureState
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.kcloud.plugins.mcuconsole.client.displayName

@Route(
    value = "设备会话",
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "设备",
            icon = "Build",
            order = 0,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun McuControlScreen() {
    val viewModel: McuControlViewModel = koinViewModel()
    val state = rememberMcuWorkbenchState(viewModel.state)
    val runAction = rememberMcuActionRunner()

    McuWorkbenchFrame(
        state = state,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuDeviceListPanel(
                    state = state,
                    onRefresh = {
                        runAction {
                            state.refreshPorts()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                McuControlSideRail(
                    state = state,
                    runAction = runAction,
                    modifier = Modifier.fillMaxWidth().weight(0.72f),
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuConnectionConfigPanel(
                    state = state,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 300.dp),
                )
                McuTerminalPanel(
                    state = state,
                    followLatestLogs = viewModel.followLatestLogs,
                    onFollowLatestChange = { viewModel.followLatestLogs = it },
                    runAction = runAction,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Composable
private fun McuControlSideRail(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "会话状态",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuInfoNotice(
                text = if (state.session.isOpen) {
                    "串口终端已连通，底部会持续追日志，Enter 会把输入直接写到 REPL。"
                } else {
                    "先扫描并选中串口，再打开终端进入 REPL。"
                },
            )
            McuStatusLamp(
                label = "会话",
                color = if (state.session.isOpen) {
                    Color(0xFF16A34A)
                } else {
                    MaterialTheme.colorScheme.error
                },
                value = if (state.session.isOpen) "在线" else "离线",
            )
            McuStatusLamp(
                label = "运行时",
                color = when (state.runtimeStatus.state) {
                    McuRuntimeEnsureState.READY -> Color(0xFF16A34A)
                    McuRuntimeEnsureState.ERROR -> MaterialTheme.colorScheme.error
                    else -> Color(0xFFF59E0B)
                },
                value = state.runtimeStatus.state.name,
            )
            McuStatusLamp(
                label = "DTR",
                color = if (state.session.dtrEnabled) {
                    Color(0xFF16A34A)
                } else {
                    MaterialTheme.colorScheme.outline
                },
                value = if (state.session.dtrEnabled) "ON" else "OFF",
            )
            McuStatusLamp(
                label = "RTS",
                color = if (state.session.rtsEnabled) {
                    Color(0xFF16A34A)
                } else {
                    MaterialTheme.colorScheme.outline
                },
                value = if (state.session.rtsEnabled) "ON" else "OFF",
            )
            McuStatusLamp(
                label = "烧录",
                color = if (state.flashStatus.state == McuFlashRunState.IDLE) {
                    MaterialTheme.colorScheme.outline
                } else {
                    Color(0xFFF59E0B)
                },
                value = state.flashStatus.state.name,
            )
            McuSummaryTable(
                rows = listOf(
                    "选中串口" to (state.selectedPortPath ?: "未选择"),
                    "当前会话" to (state.session.portPath ?: "未打开"),
                    "波特率" to state.baudRateText.ifBlank { "115200" },
                ),
            )
            OutlinedButton(
                onClick = {
                    runAction {
                        state.refreshPorts()
                    }
                },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("扫描串口")
            }
            FilledTonalButton(
                onClick = {
                    runAction {
                        if (state.session.isOpen) {
                            state.closeSession()
                        } else {
                            state.openReplSession()
                        }
                    }
                },
                enabled = if (state.session.isOpen) {
                    !state.isSubmitting
                } else {
                    !state.isSubmitting &&
                        state.selectedPortPath != null &&
                        state.baudRateText.toIntOrNull() != null
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.session.isOpen) "关闭终端" else "打开终端")
            }
            OutlinedButton(
                onClick = {
                    runAction {
                        state.resetSession()
                    }
                },
                enabled = state.canControlSerialLines,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("设备复位")
            }
            OutlinedButton(
                onClick = {
                    runAction {
                        state.updateDtr(!state.session.dtrEnabled)
                    }
                },
                enabled = state.canControlSerialLines,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR")
            }
            OutlinedButton(
                onClick = {
                    runAction {
                        state.updateRts(!state.session.rtsEnabled)
                    }
                },
                enabled = state.canControlSerialLines,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS")
            }
            OutlinedButton(
                onClick = {
                    runAction {
                        state.ensureRuntime(forceReflash = false)
                    }
                },
                enabled = state.canEnsureRuntime,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("确保运行时")
            }
        }
    }
}

@Composable
private fun McuStatusLamp(
    label: String,
    color: Color,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(10.dp).background(color, CircleShape),
        )
        Text(
            text = label,
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun McuDeviceListPanel(
    state: McuConsoleWorkbenchState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    McuPanel(
        title = "设备列表",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "这里只保留本机串口自动发现。先选设备，再到右侧确认波特率并打开终端。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
            ) {
                McuPortBrowser(
                    state = state,
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

@Composable
private fun McuConnectionConfigPanel(
    state: McuConsoleWorkbenchState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val selectedPort = state.selectedPort
    McuPanel(
        title = "串口连接",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuInfoNotice(
                text = "底部终端按 Enter 时，会使用这里的波特率和回车行尾配置直接写到串口。",
            )
            McuCompactInput(
                value = state.baudRateText,
                onValueChange = { state.baudRateText = it },
                label = "baudRate",
                supportingText = "常用值例如 115200。",
            )
            Text(
                text = "回车行尾",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            McuChoiceChipRow(
                items = McuSerialLineEnding.entries,
                selectedItem = state.serialCommandLineEnding,
                labelOf = { lineEnding -> lineEnding.displayName() },
                onSelect = { lineEnding ->
                    state.serialCommandLineEnding = lineEnding
                },
            )
            McuSummaryTable(
                rows = listOf(
                    "当前设备" to (state.selectedPortPath ?: "未选择"),
                    "当前会话" to if (state.session.isOpen) "串口终端" else "未打开",
                    "当前串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                    "已发现数量" to state.ports.size.toString(),
                    "回车行尾" to state.serialCommandLineEnding.displayName(),
                ),
            )
            selectedPort?.let { port ->
                McuInfoNotice(
                    text = listOf(
                        port.remark,
                        port.portName,
                        port.descriptiveName,
                        port.description,
                        port.manufacturer,
                    )
                        .filter { value -> value.isNotBlank() }
                        .joinToString(" / ")
                        .ifBlank { "当前设备没有更多可展示描述。" },
                )
            }
        }
    }
}

@Composable
private fun McuTerminalPanel(
    state: McuConsoleWorkbenchState,
    followLatestLogs: Boolean,
    onFollowLatestChange: (Boolean) -> Unit,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkThemeEnabled = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val toolbarBackground = if (darkThemeEnabled) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(toolbarBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "串口终端",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (state.session.isOpen) {
                            "REPL 已连接 ${state.session.portPath.orEmpty()}，Enter 发送，Ctrl+C 中断。"
                        } else {
                            "打开终端后会实时滚动串口日志。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = {
                        onFollowLatestChange(!followLatestLogs)
                    },
                ) {
                    Text(if (followLatestLogs) "停止跟随" else "跟随最新")
                }
                TextButton(
                    onClick = {
                        state.clearVisibleEvents()
                    },
                ) {
                    Text("清空")
                }
                FilledTonalButton(
                    onClick = {
                        runAction {
                            if (state.session.isOpen) {
                                state.sendReplInterrupt()
                            } else {
                                state.openReplSession()
                            }
                        }
                    },
                    enabled = if (state.session.isOpen) {
                        state.canSendDirectSerialText
                    } else {
                        !state.isSubmitting &&
                            state.selectedPortPath != null &&
                            state.baudRateText.toIntOrNull() != null
                    },
                ) {
                    Text(if (state.session.isOpen) "Ctrl+C" else "打开终端")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            McuTerminalFeed(
                events = state.events.takeLast(400),
                modifier = Modifier.fillMaxWidth().weight(1f),
                autoScrollToLatest = followLatestLogs,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "repl>",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                )
                OutlinedTextField(
                    value = state.serialCommandText,
                    onValueChange = { state.serialCommandText = it },
                    modifier = Modifier.weight(1f).onPreviewKeyEvent { keyEvent ->
                        handleTerminalKeyEvent(
                            keyEvent = keyEvent,
                            state = state,
                            runAction = runAction,
                        )
                    },
                    placeholder = {
                        Text(
                            if (state.session.isOpen) {
                                "输入 REPL 命令，按 Enter 发送"
                            } else {
                                "先打开终端"
                            },
                        )
                    },
                    enabled = state.session.isOpen && !state.isSubmitting,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
                FilledTonalButton(
                    onClick = {
                        runAction {
                            if (state.serialCommandText.isEmpty()) {
                                state.sendReplNewLine()
                            } else {
                                state.sendReplText()
                            }
                        }
                    },
                    enabled = state.canSendDirectSerialText,
                ) {
                    Text(if (state.serialCommandText.isEmpty()) "回车" else "发送")
                }
            }
        }
    }
}

/**
 * 终端模式优先保留 Enter / Ctrl+C，避免再退回卡片式命令表单交互。
 */
private fun handleTerminalKeyEvent(
    keyEvent: KeyEvent,
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
): Boolean {
    if (!state.canSendDirectSerialText || keyEvent.type != KeyEventType.KeyDown) {
        return false
    }
    return when {
        keyEvent.isCtrlPressed && keyEvent.key == Key.C -> {
            runAction {
                state.sendReplInterrupt()
            }
            true
        }

        !keyEvent.isShiftPressed && (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) -> {
            runAction {
                if (state.serialCommandText.isEmpty()) {
                    state.sendReplNewLine()
                } else {
                    state.sendReplText()
                }
            }
            true
        }

        else -> false
    }
}
