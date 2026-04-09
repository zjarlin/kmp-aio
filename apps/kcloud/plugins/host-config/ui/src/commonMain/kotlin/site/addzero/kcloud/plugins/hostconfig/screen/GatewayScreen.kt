@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoSegmentedControl
import io.github.robinpcrd.cupertino.CupertinoSegmentedControlTab
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigRequest
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
import site.addzero.kcloud.plugins.hostconfig.gateway.GatewayViewModel
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.form.CupertinoFormSection
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.dialog.CupertinoDialog
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@Route(
    title = "网关配置",
    routePath = "host-config/gateway",
    icon = "SettingsInputComponent",
    order = 30.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
    ),
)
@Composable
/**
 * 处理网关界面。
 */
fun GatewayScreen() {
    val viewModel = koinViewModel<GatewayViewModel>()
    val state = viewModel.screenState
    var editorVisible by remember { mutableStateOf(false) }
    var pinEditorVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.projects,
            selectedId = state.selectedProjectId,
            onNodeClick = { project ->
                viewModel.selectProject(project.id)
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.30f),
            searchPlaceholder = "搜索工程",
            getId = { item -> item.id },
            getLabel = { item -> item.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.SettingsApplications },
            header = {
                state.errorMessage?.let { message ->
                    CupertinoStatusStrip(message)
                }
                state.noticeMessage?.let { message ->
                    CupertinoStatusStrip(message)
                }
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(0.70f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CupertinoPanel(
                title = state.selectedProject?.name ?: "未选择工程",
                subtitle = "Modbus TCP / RTU 服务端参数概览",
                actions = {
                    WorkbenchActionButton(
                        text = "编辑网关",
                        onClick = {
                            editorVisible = true
                            viewModel.clearNotice()
                        },
                        enabled = state.selectedProjectId != null,
                    )
                },
            ) {
                CupertinoSegmentedControl(
                    selectedTabIndex = if (state.selectedTransport == TransportType.TCP) 0 else 1,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    CupertinoSegmentedControlTab(
                        onClick = {
                            viewModel.selectTransport(TransportType.TCP)
                        },
                        isSelected = state.selectedTransport == TransportType.TCP,
                    ) {
                        CupertinoText("TCP")
                    }
                    CupertinoSegmentedControlTab(
                        onClick = {
                            viewModel.selectTransport(TransportType.RTU)
                        },
                        isSelected = state.selectedTransport == TransportType.RTU,
                    ) {
                        CupertinoText("RTU")
                    }
                }
                val config = state.activeConfig
                CupertinoKeyValueRow("启用", if (config.enabled) "是" else "否")
                CupertinoKeyValueRow("传输类型", config.transportType.label())
                CupertinoKeyValueRow("TCP 端口", config.tcpPort?.toString() ?: "-")
                CupertinoKeyValueRow("串口", config.portName ?: "-")
                CupertinoKeyValueRow("波特率", config.baudRate?.toString() ?: "-")
                CupertinoKeyValueRow("数据位", config.dataBits?.toString() ?: "-")
                CupertinoKeyValueRow("停止位", config.stopBits?.toString() ?: "-")
                CupertinoKeyValueRow("校验位", config.parity?.label() ?: "-")
                CupertinoKeyValueRow("站号", config.stationNo?.toString() ?: "-")
            }

            CupertinoPanel(
                title = "下位机引脚",
                subtitle = "项目级控制灯与运行指示灯引脚配置。",
                actions = {
                    WorkbenchActionButton(
                        text = "编辑引脚",
                        onClick = {
                            pinEditorVisible = true
                            viewModel.clearNotice()
                        },
                        enabled = state.selectedProjectId != null,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                },
            ) {
                CupertinoKeyValueRow("故障控制灯引脚", state.pinConfig.faultIndicatorPin)
                CupertinoKeyValueRow("运行指示灯引脚", state.pinConfig.runningIndicatorPin)
            }
        }
    }

    if (editorVisible && state.selectedProjectId != null) {
        GatewayConfigDialog(
            transportType = state.selectedTransport,
            initial = state.activeConfig,
            saving = state.busy,
            onDismissRequest = {
                editorVisible = false
            },
            onSave = { request ->
                viewModel.saveConfig(state.selectedTransport, request)
                editorVisible = false
            },
        )
    }

    if (pinEditorVisible && state.selectedProjectId != null) {
        GatewayPinConfigDialog(
            initial = state.pinConfig,
            saving = state.busy,
            onDismissRequest = {
                pinEditorVisible = false
            },
            onSave = { request ->
                viewModel.savePinConfig(request)
                pinEditorVisible = false
            },
        )
    }
}

@Composable
/**
 * 处理网关配置dialog。
 *
 * @param transportType 传输类型。
 * @param initial initial。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
private fun GatewayConfigDialog(
    transportType: TransportType,
    initial: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigResponse,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectModbusServerConfigRequest) -> Unit,
) {
    var enabled by remember(initial.id, initial.enabled) { mutableStateOf(initial.enabled) }
    var tcpPort by remember(initial.id, initial.tcpPort) { mutableStateOf(initial.tcpPort?.toString().orEmpty()) }
    var portName by remember(initial.id, initial.portName) { mutableStateOf(initial.portName.orEmpty()) }
    var baudRate by remember(initial.id, initial.baudRate) { mutableStateOf(initial.baudRate?.toString().orEmpty()) }
    var dataBits by remember(initial.id, initial.dataBits) { mutableStateOf(initial.dataBits?.toString().orEmpty()) }
    var stopBits by remember(initial.id, initial.stopBits) { mutableStateOf(initial.stopBits?.toString().orEmpty()) }
    var parity by remember(initial.id, initial.parity) { mutableStateOf(initial.parity ?: Parity.NONE) }
    var stationNo by remember(initial.id, initial.stationNo) { mutableStateOf(initial.stationNo?.toString().orEmpty()) }

    CupertinoDialog(
        title = "编辑 ${transportType.label()} 网关",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton(
                text = "取消",
                onClick = onDismissRequest,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ProjectModbusServerConfigRequest(
                            enabled = enabled,
                            tcpPort = if (transportType == TransportType.TCP) tcpPort.toIntOrNull() else null,
                            portName = if (transportType == TransportType.RTU) portName.ifBlank { null } else null,
                            baudRate = if (transportType == TransportType.RTU) baudRate.toIntOrNull() else null,
                            dataBits = if (transportType == TransportType.RTU) dataBits.toIntOrNull() else null,
                            stopBits = if (transportType == TransportType.RTU) stopBits.toIntOrNull() else null,
                            parity = if (transportType == TransportType.RTU) parity else null,
                            stationNo = if (transportType == TransportType.RTU) stationNo.toIntOrNull() else null,
                        ),
                    )
                },
                enabled = !saving,
            )
        },
    ) {
        CupertinoFormSection(
            title = "服务开关",
            subtitle = "先确定是否启用，再录入 TCP 或 RTU 参数。",
        ) {
            item {
                CupertinoBooleanField("启用服务端", enabled, { enabled = it })
            }
        }
        if (transportType == TransportType.TCP) {
            CupertinoFormSection(
                title = "TCP 配置",
                subtitle = "TCP 模式下只保留端口号这一项核心参数。",
            ) {
                item {
                    CupertinoTextField("TCP 端口", tcpPort, { tcpPort = it })
                }
            }
        } else {
            CupertinoFormSection(
                title = "RTU 配置",
                subtitle = "串口参数默认双栏并排，减少来回滚动。",
            ) {
                item {
                    CupertinoTextField("串口", portName, { portName = it }, placeholder = "例如 COM3")
                }
                item {
                    CupertinoTextField("波特率", baudRate, { baudRate = it })
                }
                item {
                    CupertinoTextField("数据位", dataBits, { dataBits = it })
                }
                item {
                    CupertinoTextField("停止位", stopBits, { stopBits = it })
                }
                item {
                    CupertinoTextField("站号", stationNo, { stationNo = it })
                }
            }
            CupertinoPanel(
                title = "校验位",
                subtitle = "点击切换当前 RTU 校验位。",
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(Parity.NONE, Parity.ODD, Parity.EVEN).forEach { option ->
                        WorkbenchActionButton(
                            text = option.label(),
                            onClick = {
                                parity = option
                            },
                            variant = if (parity == option) {
                                WorkbenchButtonVariant.Default
                            } else {
                                WorkbenchButtonVariant.Outline
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
/**
 * 处理网关pin配置dialog。
 *
 * @param initial initial。
 * @param saving saving。
 * @param onDismissRequest ondismiss请求。
 * @param onSave on保存。
 */
private fun GatewayPinConfigDialog(
    initial: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigResponse,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectGatewayPinConfigRequest) -> Unit,
) {
    var faultIndicatorPin by remember(initial.id, initial.faultIndicatorPin) {
        mutableStateOf(initial.faultIndicatorPin)
    }
    var runningIndicatorPin by remember(initial.id, initial.runningIndicatorPin) {
        mutableStateOf(initial.runningIndicatorPin)
    }

    CupertinoDialog(
        title = "编辑下位机引脚",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton(
                text = "取消",
                onClick = onDismissRequest,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ProjectGatewayPinConfigRequest(
                            faultIndicatorPin = faultIndicatorPin,
                            runningIndicatorPin = runningIndicatorPin,
                        ),
                    )
                },
                enabled = !saving,
            )
        },
    ) {
        CupertinoPanel(
            title = "引脚建议值",
            subtitle = "默认下位机定义：故障控制灯 PA8，运行指示灯 PA2。",
        ) {
            CupertinoKeyValueRow("故障控制灯", "PA8")
            CupertinoKeyValueRow("运行指示灯", "PA2")
        }
        CupertinoFormSection(
            title = "引脚配置",
            subtitle = "把建议值和编辑字段分开，修改时更不容易看花。",
        ) {
            item {
                CupertinoTextField(
                    label = "故障控制灯引脚",
                    value = faultIndicatorPin,
                    onValueChange = { faultIndicatorPin = it },
                    placeholder = "例如 PA8",
                )
            }
            item {
                CupertinoTextField(
                    label = "运行指示灯引脚",
                    value = runningIndicatorPin,
                    onValueChange = { runningIndicatorPin = it },
                    placeholder = "例如 PA2",
                )
            }
        }
    }
}
