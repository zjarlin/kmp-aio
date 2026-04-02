package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.kcloud.plugins.mcuconsole.client.displayName
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode

@Route(
    value = "开发工具",
    title = "Modbus",
    routePath = "mcu/modbus",
    icon = "SettingsInputComponent",
    order = 15.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "设备",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuModbusScreen() {
    val viewModel: McuModbusViewModel = koinViewModel()
    val state = rememberMcuWorkbenchState(viewModel.state)
    val runAction = rememberMcuActionRunner()

    McuCupertinoScene {
        McuWorkbenchFrame(
            state = state,
            actions = {
                McuCupertinoSecondaryButton(
                    text = "扫描串口",
                    onClick = {
                        runAction {
                            state.refreshPorts()
                        }
                    },
                )
                McuCupertinoPrimaryButton(
                    text = "执行 ${state.selectedModbusAtomicAction.displayName()}",
                    enabled = state.canExecuteSelectedModbusAction,
                    onClick = {
                        runAction {
                            state.executeSelectedModbusAction()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "保存连接",
                    enabled = !state.isSubmitting,
                    onClick = {
                        runAction {
                            state.saveCurrentTransportProfile()
                        }
                    },
                )
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuPanel(
                    title = "连接资源",
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                ) {
                    McuInfoNotice("这里复用左侧串口自动发现，并把当前 RTU 参数保存成可回填的串口配置草稿。")
                    Box(
                        modifier = Modifier.fillMaxWidth().height(320.dp),
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
                    McuCupertinoSummarySection(rows = state.modbusConnectionSummaryRows())
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    McuModbusConnectionEditor(
                        onExecute = {
                            runAction {
                                state.executeSelectedModbusAction()
                            }
                        },
                        state = state,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        McuModbusActionPanel(
                            state = state,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onExecute = {
                                runAction {
                                    state.executeSelectedModbusAction()
                                }
                            },
                        )

                        McuPanel(
                            title = "执行结果",
                            modifier = Modifier.width(330.dp).fillMaxHeight(),
                        ) {
                            McuInfoNotice("每次执行都会把当前 RTU 参数一起带上。右侧始终展示最近一次请求回包。")
                            McuCupertinoSummarySection(rows = state.modbusLastResultRows())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun McuModbusConnectionEditor(
    state: McuConsoleWorkbenchState,
    onExecute: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "新建连接",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuCupertinoField(
                    value = state.modbusConnectionNameText,
                    onValueChange = { state.modbusConnectionNameText = it },
                    label = "连接别名",
                    modifier = Modifier.weight(1f),
                )

                McuPanel(
                    title = "连接模式",
                    modifier = Modifier.width(240.dp),
                ) {
                    McuSummaryTable(
                        rows = listOf(
                            "模式" to "Serial Port",
                            "角色" to "Master",
                            "通道" to "串口直连",
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                McuPanel(
                    title = "串口配置",
                    modifier = Modifier.weight(1f),
                ) {
                    McuSummaryTable(
                        rows = listOf(
                            "串口号" to state.selectedPortPath.orEmpty().ifBlank { "请先从左侧选择串口" },
                        ),
                    )
                    McuCupertinoField(
                        value = state.baudRateText,
                        onValueChange = { state.baudRateText = it },
                        label = "波特率",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        McuCupertinoField(
                            value = state.modbusRtuDataBitsText,
                            onValueChange = { state.modbusRtuDataBitsText = it },
                            label = "数据位",
                            modifier = Modifier.weight(1f),
                        )
                        McuCupertinoField(
                            value = state.modbusRtuStopBitsText,
                            onValueChange = { state.modbusRtuStopBitsText = it },
                            label = "停止位",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    McuChoiceChipRow(
                        items = McuModbusSerialParity.entries,
                        selectedItem = state.modbusRtuParity,
                        labelOf = { parity -> parity.displayName() },
                        onSelect = { parity -> state.modbusRtuParity = parity },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        McuCupertinoField(
                            value = state.modbusRtuUnitIdText,
                            onValueChange = { state.modbusRtuUnitIdText = it },
                            label = "站号 UnitId",
                            modifier = Modifier.weight(1f),
                            supportingText = "1..247",
                        )
                        McuCupertinoField(
                            value = state.modbusRtuRetriesText,
                            onValueChange = { state.modbusRtuRetriesText = it },
                            label = "重试次数",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    McuCupertinoField(
                        value = state.modbusRtuTimeoutMsText,
                        onValueChange = { state.modbusRtuTimeoutMsText = it },
                        label = "超时(ms)",
                    )
                }

                McuPanel(
                    title = "消息帧格式",
                    modifier = Modifier.width(280.dp),
                ) {
                    McuChoiceChipRow(
                        items = McuModbusFrameFormat.entries,
                        selectedItem = state.modbusFrameFormat,
                        labelOf = { frameFormat -> frameFormat.displayName() },
                        onSelect = { frameFormat -> state.modbusFrameFormat = frameFormat },
                    )
                    McuInfoNotice(
                        if (state.modbusFrameFormat == McuModbusFrameFormat.RTU) {
                            "RTU 已接到后端原子动作接口，当前推荐直接使用。"
                        } else {
                            "ASCII 目前只保留界面参考位，还没有接到后端执行链路。"
                        },
                    )
                    McuSummaryTable(
                        rows = listOf(
                            "帧格式" to state.modbusFrameFormat.displayName(),
                            "流控" to "未接入",
                            "当前动作" to state.selectedModbusAtomicAction.displayName(),
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                McuCupertinoPrimaryButton(
                    text = "按当前配置执行",
                    onClick = onExecute,
                    enabled = state.canExecuteSelectedModbusAction,
                )
            }
        }
    }
}

@Composable
private fun McuModbusActionPanel(
    state: McuConsoleWorkbenchState,
    modifier: Modifier = Modifier,
    onExecute: () -> Unit,
) {
    McuPanel(
        title = "原子动作",
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                McuChoiceChipRow(
                    items = McuModbusAtomicAction.entries,
                    selectedItem = state.selectedModbusAtomicAction,
                    labelOf = { action -> action.displayName() },
                    onSelect = { action -> state.selectModbusAtomicAction(action) },
                )
            }
            item {
                McuSummaryTable(rows = state.selectedModbusActionReferenceRows())
            }
            when (state.selectedModbusAtomicAction) {
                McuModbusAtomicAction.GPIO_WRITE -> {
                    item {
                        McuCupertinoField(
                            value = state.modbusGpioWritePinText,
                            onValueChange = { state.modbusGpioWritePinText = it },
                            label = "GPIO 引脚",
                        )
                    }
                    item {
                        McuChoiceChipRow(
                            items = listOf(true, false),
                            selectedItem = state.modbusGpioWriteHigh,
                            labelOf = { high -> if (high) "HIGH" else "LOW" },
                            onSelect = { high -> state.modbusGpioWriteHigh = high },
                        )
                    }
                }

                McuModbusAtomicAction.GPIO_MODE -> {
                    item {
                        McuCupertinoField(
                            value = state.modbusGpioModePinText,
                            onValueChange = { state.modbusGpioModePinText = it },
                            label = "GPIO 引脚",
                        )
                    }
                    item {
                        McuChoiceChipRow(
                            items = McuModbusGpioMode.entries,
                            selectedItem = state.modbusGpioMode,
                            labelOf = { mode -> mode.displayName() },
                            onSelect = { mode -> state.modbusGpioMode = mode },
                        )
                    }
                }

                McuModbusAtomicAction.PWM_DUTY -> {
                    item {
                        McuCupertinoField(
                            value = state.modbusPwmPinText,
                            onValueChange = { state.modbusPwmPinText = it },
                            label = "PWM 引脚",
                        )
                    }
                    item {
                        McuCupertinoField(
                            value = state.modbusPwmDutyText,
                            onValueChange = { state.modbusPwmDutyText = it },
                            label = "dutyU16",
                            supportingText = "合法范围 0..65535",
                        )
                    }
                }

                McuModbusAtomicAction.SERVO_ANGLE -> {
                    item {
                        McuCupertinoField(
                            value = state.modbusServoPinText,
                            onValueChange = { state.modbusServoPinText = it },
                            label = "舵机引脚",
                        )
                    }
                    item {
                        McuCupertinoField(
                            value = state.modbusServoAngleText,
                            onValueChange = { state.modbusServoAngleText = it },
                            label = "目标角度",
                            supportingText = "合法范围 0..180",
                        )
                    }
                }
            }
            item {
                McuCupertinoPrimaryButton(
                    text = "执行 ${state.selectedModbusAtomicAction.displayName()}",
                    onClick = onExecute,
                    enabled = state.canExecuteSelectedModbusAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
