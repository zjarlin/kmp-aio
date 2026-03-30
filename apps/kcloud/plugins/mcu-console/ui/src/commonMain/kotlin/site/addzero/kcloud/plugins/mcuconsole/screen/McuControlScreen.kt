package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuBluetoothMode
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
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
    ),
)
@Composable
fun McuControlScreen() {
    val state = rememberMcuWorkbenchState()
    val runAction = rememberMcuActionRunner()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("扫描串口", Icons.Default.Search) {
                runAction {
                    state.refreshPorts()
                }
            },
            McuToolbarAction(
                label = state.openSessionActionLabel,
                icon = Icons.Default.PowerSettingsNew,
                enabled = state.canOpenSelectedTransportSession,
            ) {
                runAction {
                    state.openSession()
                }
            },
            McuToolbarAction(
                label = "关闭会话",
                icon = Icons.Default.Stop,
                enabled = state.session.isOpen,
            ) {
                runAction {
                    state.closeSession()
                }
            },
            McuToolbarAction(
                label = "确保运行时",
                icon = Icons.Default.Build,
                enabled = state.canEnsureRuntime,
            ) {
                runAction {
                    state.ensureRuntime(forceReflash = false)
                }
            },
            McuToolbarAction(
                label = "复位",
                icon = Icons.Default.Refresh,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
                    state.resetSession()
                }
            },
            McuToolbarAction(
                label = if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR",
                icon = Icons.Default.Settings,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
                    state.updateDtr(!state.session.dtrEnabled)
                }
            },
            McuToolbarAction(
                label = if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS",
                icon = Icons.Default.Tune,
                enabled = state.canControlSerialLines,
            ) {
                runAction {
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
                title = "连接配置",
                modifier = Modifier.width(420.dp).fillMaxHeight(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        McuTransportSelector(
                            selectedKind = state.selectedTransportKind,
                            onSelect = { kind -> state.selectTransport(kind) },
                        )
                    }
                    item {
                        McuInfoNotice(text = state.selectedTransportNotice)
                    }
                    when (state.selectedTransportKind) {
                        McuTransportKind.SERIAL -> {
                            item {
                                McuCompactInput(
                                    value = state.baudRateText,
                                    onValueChange = { state.baudRateText = it },
                                    label = "baudRate",
                                    supportingText = "当前串口链路将直接作为 MCU 会话使用",
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(240.dp),
                                ) {
                                    McuPortBrowser(
                                        state = state,
                                        onRefresh = {
                                            runAction {
                                                state.refreshPorts()
                                            }
                                        },
                                    )
                                }
                            }
                        }

                        McuTransportKind.MODBUS_RTU -> {
                            item {
                                McuCompactInput(
                                    value = state.baudRateText,
                                    onValueChange = { state.baudRateText = it },
                                    label = "baudRate",
                                    supportingText = "RTU 仍复用本地串口打开链路",
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
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(240.dp),
                                ) {
                                    McuPortBrowser(
                                        state = state,
                                        onRefresh = {
                                            runAction {
                                                state.refreshPorts()
                                            }
                                        },
                                    )
                                }
                            }
                        }

                        McuTransportKind.MODBUS_TCP -> {
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

            McuPanel(
                title = "会话状态",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
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
                        "Bundle" to (state.runtimeStatus.bundleTitle ?: state.selectedRuntimeBundle?.title.orEmpty()),
                        "Profile" to (state.runtimeStatus.defaultFlashProfileId ?: state.selectedRuntimeBundle?.defaultFlashProfileId.orEmpty()),
                        "烧录" to state.flashStatus.state.name,
                        "DTR" to state.session.dtrEnabled.toString(),
                        "RTS" to state.session.rtsEnabled.toString(),
                        "消息" to state.runtimeStatus.lastMessage.orEmpty(),
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
