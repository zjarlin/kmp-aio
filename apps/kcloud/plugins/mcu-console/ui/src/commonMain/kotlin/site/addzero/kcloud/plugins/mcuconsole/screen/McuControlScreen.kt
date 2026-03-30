package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.component.button.AddIconButton
import site.addzero.kcloud.plugins.mcuconsole.McuBluetoothMode
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.kcloud.plugins.mcuconsole.client.displayName

/**
 */
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
    val state = rememberMcuWorkbenchState()
    val runAction = rememberMcuActionRunner()
    var followLatestLogs by rememberSaveable {
        mutableStateOf(true)
    }
    McuWorkbenchFrame(
        state = state,
        actions = {
            AddIconButton(
                text = "扫描串口",
                imageVector = Icons.Default.Search,
            ) {
                runAction {
                    state.refreshPorts()
                }
            }
            AddIconButton(
                text = state.openSessionActionLabel,
                imageVector = Icons.Default.PowerSettingsNew,
                enabled = state.canOpenSelectedTransportSession,
            ) {
                runAction {
                    state.openSession()
                }
            }
            AddIconButton(
                text = "保存连接",
                imageVector = Icons.Default.Save,
                enabled = !state.isSubmitting,
            ) {
                runAction {
                    state.saveCurrentTransportProfile()
                }
            }
            AddIconButton(
                text = "关闭会话",
                imageVector = Icons.Default.Stop,
                enabled = state.session.isOpen,
            ) {
                runAction {
                    state.closeSession()
                }
            }
            AddIconButton(
                text = "确保运行时",
                imageVector = Icons.Default.Build,
                enabled = state.canEnsureRuntime,
            ) {
                runAction {
                    state.ensureRuntime(forceReflash = false)
                }
            }
            AddIconButton(
                text = "复位",
                imageVector = Icons.Default.Refresh,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
                    state.resetSession()
                }
            }
            AddIconButton(
                text = if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR",
                imageVector = Icons.Default.Settings,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
                    state.updateDtr(!state.session.dtrEnabled)
                }
            }
            AddIconButton(
                text = if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS",
                imageVector = Icons.Default.Tune,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
                    state.updateRts(!state.session.rtsEnabled)
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(520.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.width(400.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    McuDeviceListPanel(
                        state = state,
                        onRefresh = {
                            runAction {
                                state.refreshPorts()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().weight(1.35f),
                    )
                    McuSessionStatusPanel(
                        state = state,
                        modifier = Modifier.fillMaxWidth().weight(0.9f),
                    )
                }

                McuConnectionConfigPanel(
                    state = state,
                    runAction = runAction,
                    modifier = Modifier.width(420.dp).fillMaxHeight(),
                )

                McuPanel(
                    title = "快捷命令",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            McuInfoNotice(
                                if (state.canSendDirectSerialText) {
                                    "当前串口已打开，可直接把 UTF-8 文本写进 MicroPython REPL。快捷按钮默认调用板子上的 panel_control.py。"
                                } else {
                                    "先以“串口”模式打开会话，再发送 MicroPython 命令。"
                                },
                            )
                        }
                        item {
                            McuCompactInput(
                                value = state.panelControlModuleText,
                                onValueChange = { state.panelControlModuleText = it },
                                label = "panel_control 模块",
                                supportingText = "默认使用板子上的 panel_control.py",
                            )
                        }
                        item {
                            McuCompactInput(
                                value = state.panelDisplayValueText,
                                onValueChange = { state.panelDisplayValueText = it },
                                label = "显示内容",
                            )
                        }
                        item {
                            McuCompactInput(
                                value = state.panelBeepTimesText,
                                onValueChange = { state.panelBeepTimesText = it },
                                label = "蜂鸣器次数",
                            )
                        }
                        item {
                            McuCompactInput(
                                value = state.panelLedIndexText,
                                onValueChange = { state.panelLedIndexText = it },
                                label = "LED 序号",
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelDisplayCommand()
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("显示数码管")
                                }
                                FilledTonalButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelBeepCommand()
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("蜂鸣器")
                                }
                                OutlinedButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelClearDisplayCommand()
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("清屏")
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelLedCommand(enabled = true)
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("点亮 LED")
                                }
                                OutlinedButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelLedCommand(enabled = false)
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("熄灭 LED")
                                }
                                FilledTonalButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelAllLedCommand(enabled = true)
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("全亮")
                                }
                                OutlinedButton(
                                    onClick = {
                                        runAction {
                                            state.sendPanelAllLedCommand(enabled = false)
                                        }
                                    },
                                    enabled = state.canSendDirectSerialText,
                                ) {
                                    Text("全灭")
                                }
                            }
                        }
                        item {
                            McuInfoNotice(
                                "板卡探测优先读取固件脚本里的已知引脚定义；GPIO 快照会临时把填写的引脚切成输入模式，适合做人工排查，不适合长时间挂着。",
                            )
                        }
                        item {
                            McuCompactInput(
                                value = state.probePinMapFilesText,
                                onValueChange = { state.probePinMapFilesText = it },
                                label = "探测文件",
                                supportingText = "逗号分隔，默认读取 boot.py / main.py / panel_control.py",
                            )
                        }
                        item {
                            FilledTonalButton(
                                onClick = {
                                    runAction {
                                        state.probeKnownPinMap()
                                    }
                                },
                                enabled = state.canSendDirectSerialText,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("探测已知引脚映射")
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                McuCompactInput(
                                    value = state.probeI2cSdaText,
                                    onValueChange = { state.probeI2cSdaText = it },
                                    label = "I2C SDA",
                                    modifier = Modifier.weight(1f),
                                )
                                McuCompactInput(
                                    value = state.probeI2cSclText,
                                    onValueChange = { state.probeI2cSclText = it },
                                    label = "I2C SCL",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            FilledTonalButton(
                                onClick = {
                                    runAction {
                                        state.probeI2cDevices()
                                    }
                                },
                                enabled = state.canSendDirectSerialText,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("扫描 I2C 外设")
                            }
                        }
                        item {
                            McuCompactInput(
                                value = state.probeGpioSnapshotPinsText,
                                onValueChange = { state.probeGpioSnapshotPinsText = it },
                                label = "GPIO 快照引脚",
                                singleLine = false,
                                supportingText = "逗号分隔。点击后会把这些脚临时切到输入模式并打印电平。",
                            )
                        }
                        item {
                            FilledTonalButton(
                                onClick = {
                                    runAction {
                                        state.probeGpioSnapshot()
                                    }
                                },
                                enabled = state.canSendDirectSerialText,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("读取 GPIO 电平快照")
                            }
                        }
                        item {
                            McuInfoNotice(
                                "下面 4 个按钮槽位都可以自定义按钮名和 Python 脚本。点击按钮后，会把脚本直接写入当前串口会话。",
                            )
                        }
                        state.customSerialActions.forEachIndexed { index, action ->
                            item(key = action.id) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text("自定义按钮 ${index + 1}")
                                    McuCompactInput(
                                        value = action.labelText,
                                        onValueChange = { state.updateCustomSerialActionLabel(index, it) },
                                        label = "按钮名称",
                                    )
                                    McuCompactInput(
                                        value = action.scriptText,
                                        onValueChange = { state.updateCustomSerialActionScript(index, it) },
                                        label = "按钮脚本",
                                        singleLine = false,
                                        supportingText = "支持多行 Python；点击下方按钮后会按当前换行设置直接发到串口。",
                                    )
                                    FilledTonalButton(
                                        onClick = {
                                            runAction {
                                                state.runCustomSerialAction(index)
                                            }
                                        },
                                        enabled = state.canSendDirectSerialText,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(action.labelText.ifBlank { "发送自定义脚本" })
                                    }
                                }
                            }
                        }
                        item {
                            McuCompactInput(
                                value = state.serialCommandText,
                                onValueChange = { state.serialCommandText = it },
                                label = "原始串口命令",
                                singleLine = false,
                                supportingText = "支持多行；默认按所选换行符把文本直接写入当前串口。",
                            )
                        }
                        item {
                            McuChoiceChipRow(
                                items = McuSerialLineEnding.entries,
                                selectedItem = state.serialCommandLineEnding,
                                labelOf = { ending -> ending.displayName() },
                                onSelect = { ending -> state.serialCommandLineEnding = ending },
                            )
                        }
                        item {
                            McuChoiceChipRow(
                                items = listOf(true, false),
                                selectedItem = state.serialCommandAppendLineEnding,
                                labelOf = { enabled -> if (enabled) "追加结尾换行" else "原样发送" },
                                onSelect = { enabled -> state.serialCommandAppendLineEnding = enabled },
                            )
                        }
                        item {
                            FilledTonalButton(
                                onClick = {
                                    runAction {
                                        state.sendSerialCommand()
                                    }
                                },
                                enabled = state.canSendDirectSerialText,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("发送原始串口命令")
                            }
                        }
                    }
                }
            }

            McuPanel(
                title = "实时日志",
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (state.session.isOpen) {
                                "当前已连接 ${state.session.portPath.orEmpty()}，新日志会持续追加。"
                            } else {
                                "当前未连接设备，打开会话后会开始实时拉取日志。"
                            },
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = {
                                followLatestLogs = !followLatestLogs
                            },
                        ) {
                            Text(if (followLatestLogs) "停止跟随" else "跟随最新")
                        }
                        FilledTonalButton(
                            onClick = {
                                state.clearVisibleEvents()
                            },
                        ) {
                            Text("清空日志")
                        }
                    }
                    Text(
                        text = "已缓存 ${state.events.size} 条事件，点击单条日志可展开或收起原文。",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    McuEventFeed(
                        events = state.events.takeLast(200),
                        modifier = Modifier.weight(1f),
                        autoScrollToLatest = followLatestLogs,
                    )
                }
            }
        }
    }
}

@Composable
private fun McuDeviceListPanel(
    state: McuConsoleWorkbenchState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    McuPanel(
        title = if (state.supportsSelectedTransportConnection) "设备列表" else "设备入口",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuTransportSelector(
                selectedKind = state.selectedTransportKind,
                onSelect = { kind -> state.selectTransport(kind) },
            )
            McuInfoNotice(
                text = if (state.supportsSelectedTransportConnection) {
                    "先在左侧选设备，再到中间面板配置连接参数并打开会话。"
                } else {
                    "当前模式不依赖本机串口，中间面板填写连接参数即可。"
                },
            )
            if (state.supportsSelectedTransportConnection) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                ) {
                    McuPortBrowser(
                        state = state,
                        onRefresh = onRefresh,
                    )
                }
            } else {
                McuInfoNotice(text = state.selectedTransportNotice)
                McuSummaryTable(
                    rows = listOf(
                        "配置模式" to state.selectedTransportKind.displayName(),
                        "当前会话" to if (state.session.isOpen) {
                            state.activeSessionTransportKind.displayName()
                        } else {
                            "未打开"
                        },
                    ),
                )
                Spacer(modifier = Modifier.weight(1f, fill = true))
            }
        }
    }
}

@Composable
private fun McuConnectionConfigPanel(
    state: McuConsoleWorkbenchState,
    runAction: ((suspend () -> Unit) -> Unit),
    modifier: Modifier = Modifier,
) {
    McuPanel(
        title = "连接配置",
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                McuTransportProfileList(
                    state = state,
                    onSave = {
                        runAction {
                            state.saveCurrentTransportProfile()
                        }
                    },
                    onApply = { profileKey ->
                        state.applyTransportProfile(profileKey)
                    },
                    onDelete = { profileKey ->
                        runAction {
                            state.deleteTransportProfile(profileKey)
                        }
                    },
                )
            }
            when (state.selectedTransportKind) {
                McuTransportKind.SERIAL -> {
                    item {
                        McuCompactInput(
                            value = state.baudRateText,
                            onValueChange = { state.baudRateText = it },
                            label = "baudRate",
                            supportingText = "左侧设备列表用于选串口，这里保留串口链路参数。",
                        )
                    }
                }

                McuTransportKind.MODBUS_RTU -> {
                    item {
                        McuCompactInput(
                            value = state.baudRateText,
                            onValueChange = { state.baudRateText = it },
                            label = "baudRate",
                            supportingText = "RTU 仍复用左侧所选本地串口。",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusRtuUnitIdText,
                            onValueChange = { state.modbusRtuUnitIdText = it },
                            label = "unitId",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusRtuTimeoutMsText,
                            onValueChange = { state.modbusRtuTimeoutMsText = it },
                            label = "timeoutMs",
                        )
                    }
                }

                McuTransportKind.MODBUS_TCP -> {
                    item {
                        McuCompactInput(
                            value = state.modbusConnectionNameText,
                            onValueChange = { state.modbusConnectionNameText = it },
                            label = "connectionName",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusTcpHostText,
                            onValueChange = { state.modbusTcpHostText = it },
                            label = "host",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusTcpPortText,
                            onValueChange = { state.modbusTcpPortText = it },
                            label = "port",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusTcpUnitIdText,
                            onValueChange = { state.modbusTcpUnitIdText = it },
                            label = "unitId",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.modbusTcpTimeoutMsText,
                            onValueChange = { state.modbusTcpTimeoutMsText = it },
                            label = "timeoutMs",
                        )
                    }
                }

                McuTransportKind.BLUETOOTH -> {
                    item {
                        McuChoiceChipRow(
                            items = McuBluetoothMode.entries,
                            selectedItem = state.bluetoothMode,
                            labelOf = { mode -> mode.displayName() },
                            onSelect = { mode -> state.bluetoothMode = mode },
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.bluetoothDeviceNameText,
                            onValueChange = { state.bluetoothDeviceNameText = it },
                            label = "deviceName",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.bluetoothDeviceAddressText,
                            onValueChange = { state.bluetoothDeviceAddressText = it },
                            label = "deviceAddress",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.bluetoothServiceUuidText,
                            onValueChange = { state.bluetoothServiceUuidText = it },
                            label = "serviceUuid",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.bluetoothWriteCharacteristicUuidText,
                            onValueChange = { state.bluetoothWriteCharacteristicUuidText = it },
                            label = "writeCharacteristicUuid",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.bluetoothNotifyCharacteristicUuidText,
                            onValueChange = { state.bluetoothNotifyCharacteristicUuidText = it },
                            label = "notifyCharacteristicUuid",
                        )
                    }
                }

                McuTransportKind.MQTT -> {
                    item {
                        McuCompactInput(
                            value = state.modbusConnectionNameText,
                            onValueChange = { state.modbusConnectionNameText = it },
                            label = "connectionName",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttBrokerUrlText,
                            onValueChange = { state.mqttBrokerUrlText = it },
                            label = "brokerUrl",
                            supportingText = "例如 tcp://127.0.0.1:1883 或 ssl://broker:8883",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttClientIdText,
                            onValueChange = { state.mqttClientIdText = it },
                            label = "clientId",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttUsernameText,
                            onValueChange = { state.mqttUsernameText = it },
                            label = "username",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttPasswordText,
                            onValueChange = { state.mqttPasswordText = it },
                            label = "password",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttPublishTopicText,
                            onValueChange = { state.mqttPublishTopicText = it },
                            label = "publishTopic",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttSubscribeTopicText,
                            onValueChange = { state.mqttSubscribeTopicText = it },
                            label = "subscribeTopic",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttQosText,
                            onValueChange = { state.mqttQosText = it },
                            label = "qos",
                        )
                    }
                    item {
                        McuCompactInput(
                            value = state.mqttKeepAliveSecondsText,
                            onValueChange = { state.mqttKeepAliveSecondsText = it },
                            label = "keepAliveSeconds",
                        )
                    }
                }
            }
            item {
                McuSummaryTable(rows = state.transportSummaryRows())
            }
        }
    }
}

@Composable
private fun McuSessionStatusPanel(
    state: McuConsoleWorkbenchState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "会话状态",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            McuSummaryTable(
                rows = listOf(
                    "配置模式" to state.selectedTransportKind.displayName(),
                    "当前会话" to if (state.session.isOpen) {
                        state.activeSessionTransportKind.displayName()
                    } else {
                        "未打开"
                    },
                    "当前串口" to (state.session.portPath ?: state.selectedPortPath.orEmpty()),
                    "波特率" to state.baudRateText,
                    "会话" to if (state.session.isOpen) "OPEN" else "CLOSED",
                    "运行时" to state.runtimeStatus.state.name,
                    "烧录" to state.flashStatus.state.name,
                    "消息" to state.runtimeStatus.lastMessage.orEmpty(),
                    "最后错误" to state.session.lastError.orEmpty(),
                ),
            )
        }
    }
}
