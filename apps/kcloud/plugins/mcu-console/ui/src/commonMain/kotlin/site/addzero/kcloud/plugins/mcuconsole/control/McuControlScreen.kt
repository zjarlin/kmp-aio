package site.addzero.kcloud.plugins.mcuconsole.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRunState
import site.addzero.kcloud.plugins.mcuconsole.McuRuntimeEnsureState
import site.addzero.kcloud.plugins.mcuconsole.McuSerialLineEnding
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusFrameFormat
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusSerialParity
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusAtomicAction
import site.addzero.kcloud.plugins.mcuconsole.modbus.atomic.McuModbusGpioMode
import site.addzero.kcloud.plugins.mcuconsole.workbench.*

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
    val state: McuConsoleWorkbenchState = koinInject()
    var followLatestLogs by rememberSaveable { mutableStateOf(true) }
    val workbenchState = rememberMcuWorkbenchState(state)
    val runAction = rememberMcuActionRunner()

    McuWorkbenchFrame(
        state = workbenchState,
        actions = {
            McuControlTopActions(
                state = workbenchState,
                runAction = runAction,
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            McuDeviceTreeWorkspace(
                state = workbenchState,
                onRefresh = {
                    runAction {
                        workbenchState.refreshPorts()
                    }
                },
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                McuDeviceOverviewPanel(
                    state = workbenchState,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 188.dp, max = 228.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().weight(0.94f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    McuConnectionConfigPanel(
                        state = workbenchState,
                        runAction = runAction,
                        modifier = Modifier.weight(0.54f).fillMaxHeight(),
                    )
                    McuRuntimeOpsPanel(
                        state = workbenchState,
                        runAction = runAction,
                        modifier = Modifier.weight(0.46f).fillMaxHeight(),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1.02f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    McuScriptWorkbenchPanel(
                        state = workbenchState,
                        runAction = runAction,
                        modifier = Modifier.weight(0.58f).fillMaxHeight(),
                    )
                    McuModbusQuickPanel(
                        state = workbenchState,
                        runAction = runAction,
                        modifier = Modifier.weight(0.42f).fillMaxHeight(),
                    )
                }
                McuTerminalPanel(
                    state = workbenchState,
                    followLatestLogs = followLatestLogs,
                    onFollowLatestChange = { followLatestLogs = it },
                    runAction = runAction,
                    modifier = Modifier.fillMaxWidth().weight(1.18f),
                )
            }
        }
    }
}

@Composable
private fun RowScope.McuControlTopActions(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
) {
    McuPrimaryButton(
        onClick = {
            runAction {
                state.refreshPorts()
            }
        },
        enabled = !state.isSubmitting,
    ) {
        Text("扫描设备")
    }
    McuSecondaryButton(
        onClick = {
            runAction {
                state.refreshAll()
            }
        },
        enabled = !state.isSubmitting,
    ) {
        Text("刷新资源")
    }
    McuPrimaryButton(
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
    ) {
        Text(if (state.session.isOpen) "关闭会话" else "打开会话")
    }
    McuSecondaryButton(
        onClick = {
            runAction {
                state.resetSession()
            }
        },
        enabled = state.canControlSerialLines,
    ) {
        Text("设备复位")
    }
    McuSecondaryButton(
        onClick = {
            runAction {
                state.ensureRuntime(forceReflash = false)
            }
        },
        enabled = state.canEnsureRuntime,
    ) {
        Text("确保运行时")
    }
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = state.selectedPortPath ?: "未选择设备",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.Monospace,
    )
}

@Composable
private fun McuDeviceTreeWorkspace(
    state: McuConsoleWorkbenchState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    McuPanel(
        title = "设备树",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPortBrowser(
                state = state,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun McuDeviceOverviewPanel(
    state: McuConsoleWorkbenchState,
    modifier: Modifier = Modifier,
) {
    val selectedPort = state.selectedPort
    McuPanel(
        title = "设备概览",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                McuStatusChip(
                    label = "会话",
                    value = if (state.session.isOpen) "在线" else "离线",
                    positive = state.session.isOpen,
                    modifier = Modifier.weight(1f),
                )
                McuStatusChip(
                    label = "运行时",
                    value = state.runtimeStatus.state.name,
                    positive = state.runtimeStatus.state == McuRuntimeEnsureState.READY,
                    modifier = Modifier.weight(1f),
                )
                McuStatusChip(
                    label = "烧录",
                    value = state.flashStatus.state.name,
                    positive = state.flashStatus.state == McuFlashRunState.IDLE,
                    modifier = Modifier.weight(1f),
                )
            }
            McuSummaryTable(
                rows = listOf(
                    "当前设备" to (state.selectedPortPath ?: "未选择"),
                    "设备键" to (selectedPort?.deviceKey ?: "未提供"),
                    "设备备注" to (selectedPort?.remark ?: "未填写"),
                    "已保存连接" to state.transportProfiles.size.toString(),
                    "运行时包" to state.runtimeBundles.size.toString(),
                ),
            )
            selectedPort?.let { port ->
                McuInfoNotice(
                    text = listOf(
                        port.portName,
                        port.descriptiveName,
                        port.description,
                        port.manufacturer,
                    )
                        .filter { it.isNotBlank() }
                        .joinToString(" / ")
                        .ifBlank { "当前设备没有更多可展示的硬件描述。" },
                )
            } ?: Text(
                text = "先在左侧选择一个串口设备，再继续查看连接和终端详情。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun McuStatusChip(
    label: String,
    value: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = if (positive) {
        Color(0xFF1F8F52)
    } else {
        MaterialTheme.colorScheme.secondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.14f),
        contentColor = accent,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun McuRuntimeOpsPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "运行与烧录",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPanelSectionTitle("会话状态")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuPrimaryButton(
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
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.session.isOpen) "关闭终端" else "打开终端")
                }
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.resetSession()
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("设备复位")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.updateDtr(!state.session.dtrEnabled)
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR")
                }
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.updateRts(!state.session.rtsEnabled)
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("运行时")
            McuRuntimeBundleBrowser(
                bundles = state.runtimeBundles,
                selectedBundleId = state.selectedRuntimeBundleId,
                onSelect = { bundleId -> state.selectRuntimeBundle(bundleId) },
                modifier = Modifier.fillMaxWidth().height(170.dp),
            )
            McuSummaryTable(
                rows = listOf(
                    "Bundle" to (state.runtimeStatus.bundleTitle ?: state.selectedRuntimeBundle?.title.orEmpty()),
                    "运行时" to state.runtimeStatus.state.name,
                    "语言" to state.scriptLanguage,
                    "默认烧录" to (state.runtimeStatus.defaultFlashProfileId
                        ?: state.selectedRuntimeBundle?.defaultFlashProfileId.orEmpty()),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.refreshRuntimeBundles()
                            state.refreshRuntimeStatus()
                        }
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("刷新运行时")
                }
                McuPrimaryButton(
                    onClick = {
                        runAction {
                            state.ensureRuntime(forceReflash = false)
                        }
                    },
                    enabled = state.canEnsureRuntime,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("确保运行时")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("烧录能力")
            McuFlashProfileBrowser(
                profiles = state.flashProfiles,
                selectedProfileId = state.selectedFlashProfileId,
                onSelect = { profileId -> state.selectFlashProfile(profileId) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            McuCompactInput(
                value = state.firmwarePathText,
                onValueChange = { state.firmwarePathText = it },
                label = state.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                supportingText = state.runtimeStatus.artifactPath
                    ?: state.selectedFlashProfile?.artifactHint,
            )
            if (state.selectedFlashProfile?.supportsOnlineDownload == true || state.firmwareDownloadUrlText.isNotBlank()) {
                McuCompactInput(
                    value = state.firmwareDownloadUrlText,
                    onValueChange = { state.firmwareDownloadUrlText = it },
                    label = "downloadUrl",
                    supportingText = state.selectedFlashProfile?.downloadUrlHint
                        ?: state.selectedFlashProfile?.defaultDownloadUrl,
                    singleLine = false,
                )
            }
            if (state.selectedFlashProfile?.supportsCommandOverride == true) {
                McuCompactInput(
                    value = state.flashCommandTemplateText,
                    onValueChange = { state.flashCommandTemplateText = it },
                    label = "commandTemplate",
                    supportingText = "{firmwarePath} {portPath} {baudRate} {firmwareName} {firmwareDir} {profileId} {runtimeKind} {mcuFamily}",
                    singleLine = false,
                )
            }
            McuSummaryTable(
                rows = listOf(
                    "烧录状态" to state.flashStatus.state.name,
                    "目标串口" to (state.selectedPortPath ?: state.session.portPath.orEmpty()),
                    "进度" to "${state.flashStatus.bytesSent} / ${state.flashStatus.totalBytes}",
                    "消息" to (state.flashStatus.lastMessage ?: state.runtimeStatus.lastMessage.orEmpty()),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.downloadFirmwareOnline(flashAfterDownload = false)
                        }
                    },
                    enabled = state.canDownloadFirmwareOnline,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("在线下载")
                }
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.downloadFirmwareOnline(flashAfterDownload = true)
                        }
                    },
                    enabled = state.canDownloadFirmwareOnline,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("下载并烧录")
                }
            }
            McuPrimaryButton(
                onClick = {
                    runAction {
                        state.startFlash()
                    }
                },
                enabled = state.canStartFlash,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("开始烧录")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "共 ${state.filteredPorts.size} / ${state.ports.size}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                McuPrimaryButton(
                    onClick = onRefresh,
                    enabled = !state.isSubmitting,
                ) {
                    Text("扫描")
                }
            }
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
    runAction: (suspend () -> Unit) -> Unit,
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("连接模板")
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
    }
}

@Composable
private fun McuScriptWorkbenchPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "脚本与控件",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPanelSectionTitle("原子指令")
            McuScriptCommandSection(
                state = state,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("示例脚本")
            McuScriptExampleSection(
                state = state,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("脚本编辑")
            McuCompactInput(
                value = state.timeoutMsText,
                onValueChange = { state.timeoutMsText = it },
                label = "timeoutMs",
            )
            OutlinedTextField(
                value = state.scriptText,
                onValueChange = { state.scriptText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                label = {
                    Text(state.scriptLanguage)
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuPrimaryButton(
                    onClick = {
                        runAction {
                            state.executeScript()
                        }
                    },
                    enabled = state.hasActiveSession && state.isRuntimeReady && !state.isScriptRunning,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("执行脚本")
                }
                McuSecondaryButton(
                    onClick = {
                        runAction {
                            state.stopScript()
                        }
                    },
                    enabled = state.hasActiveSession && state.isScriptRunning,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("停止脚本")
                }
            }
            McuSummaryTable(
                rows = listOf(
                    "运行时" to state.runtimeStatus.state.name,
                    "脚本状态" to state.scriptStatus.state.name,
                    "Frame" to state.scriptStatus.lastFrameType.orEmpty(),
                    "控件实例" to state.widgetInstances.size.toString(),
                ),
            )
            McuScriptPreview(
                script = listOfNotNull(
                    state.scriptStatus.lastMessage,
                    state.scriptStatus.lastPayload?.toString(),
                ).joinToString("\n").ifBlank { "-" },
            )
        }
    }
}

@Composable
private fun McuModbusQuickPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "Modbus 快捷调试",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuSummaryTable(rows = state.modbusConnectionSummaryRows())
            McuChoiceChipRow(
                items = McuModbusAtomicAction.entries,
                selectedItem = state.selectedModbusAtomicAction,
                labelOf = { action -> action.displayName() },
                onSelect = { action -> state.selectModbusAtomicAction(action) },
            )
            McuChoiceChipRow(
                items = McuModbusFrameFormat.entries,
                selectedItem = state.modbusFrameFormat,
                labelOf = { frameFormat -> frameFormat.displayName() },
                onSelect = { frameFormat -> state.modbusFrameFormat = frameFormat },
            )
            McuCompactInput(
                value = state.modbusRtuUnitIdText,
                onValueChange = { state.modbusRtuUnitIdText = it },
                label = "站号 UnitId",
                supportingText = "1..247",
            )
            McuCompactInput(
                value = state.modbusRtuTimeoutMsText,
                onValueChange = { state.modbusRtuTimeoutMsText = it },
                label = "超时(ms)",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuCompactInput(
                    value = state.modbusRtuDataBitsText,
                    onValueChange = { state.modbusRtuDataBitsText = it },
                    label = "数据位",
                    modifier = Modifier.weight(1f),
                )
                McuCompactInput(
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
            when (state.selectedModbusAtomicAction) {
                McuModbusAtomicAction.GPIO_WRITE -> {
                    McuCompactInput(
                        value = state.modbusGpioWritePinText,
                        onValueChange = { state.modbusGpioWritePinText = it },
                        label = "GPIO 引脚",
                    )
                    McuChoiceChipRow(
                        items = listOf(true, false),
                        selectedItem = state.modbusGpioWriteHigh,
                        labelOf = { high -> if (high) "HIGH" else "LOW" },
                        onSelect = { high -> state.modbusGpioWriteHigh = high },
                    )
                }

                McuModbusAtomicAction.GPIO_MODE -> {
                    McuCompactInput(
                        value = state.modbusGpioModePinText,
                        onValueChange = { state.modbusGpioModePinText = it },
                        label = "GPIO 引脚",
                    )
                    McuChoiceChipRow(
                        items = McuModbusGpioMode.entries,
                        selectedItem = state.modbusGpioMode,
                        labelOf = { mode -> mode.displayName() },
                        onSelect = { mode -> state.modbusGpioMode = mode },
                    )
                }

                McuModbusAtomicAction.PWM_DUTY -> {
                    McuCompactInput(
                        value = state.modbusPwmPinText,
                        onValueChange = { state.modbusPwmPinText = it },
                        label = "PWM 引脚",
                    )
                    McuCompactInput(
                        value = state.modbusPwmDutyText,
                        onValueChange = { state.modbusPwmDutyText = it },
                        label = "dutyU16",
                        supportingText = "合法范围 0..65535",
                    )
                }

                McuModbusAtomicAction.SERVO_ANGLE -> {
                    McuCompactInput(
                        value = state.modbusServoPinText,
                        onValueChange = { state.modbusServoPinText = it },
                        label = "舵机引脚",
                    )
                    McuCompactInput(
                        value = state.modbusServoAngleText,
                        onValueChange = { state.modbusServoAngleText = it },
                        label = "目标角度",
                        supportingText = "合法范围 0..180",
                    )
                }
            }
            McuPrimaryButton(
                onClick = {
                    runAction {
                        state.executeSelectedModbusAction()
                    }
                },
                enabled = state.canExecuteSelectedModbusAction,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("执行 ${state.selectedModbusAtomicAction.displayName()}")
            }
            McuSummaryTable(rows = state.modbusLastResultRows())
        }
    }
}

@Composable
private fun McuScriptCommandSection(
    state: McuConsoleWorkbenchState,
) {
    val commands = state.selectedRuntimeBundle?.atomicCommands.orEmpty()
    if (commands.isEmpty()) {
        McuInfoNotice("当前运行时没有原子指令定义。")
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        commands.forEach { command ->
            McuSelectableItem(
                title = command.title,
                subtitle = command.signature,
                detail = command.description,
                selected = command.id == state.selectedAtomicCommandId,
                onClick = { state.selectAtomicCommand(command.id) },
            )
        }
    }
}

@Composable
private fun McuScriptExampleSection(
    state: McuConsoleWorkbenchState,
) {
    val examples = state.selectedRuntimeBundle?.scriptExamples.orEmpty()
    if (examples.isEmpty()) {
        McuInfoNotice("当前运行时没有示例脚本。")
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        examples.forEach { example ->
            McuSelectableItem(
                title = example.title,
                subtitle = example.language,
                detail = example.description,
                selected = example.id == state.selectedScriptExampleId,
                onClick = { state.selectScriptExample(example.id) },
            )
        }
    }
}

@Composable
private fun McuSelectableItem(
    title: String,
    subtitle: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = contentColor.copy(alpha = 0.76f),
            )
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun McuPanelSectionTitle(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
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
                McuSecondaryButton(
                    onClick = {
                        onFollowLatestChange(!followLatestLogs)
                    },
                ) {
                    Text(if (followLatestLogs) "停止跟随" else "跟随最新")
                }
                McuSecondaryButton(
                    onClick = {
                        state.clearVisibleEvents()
                    },
                ) {
                    Text("清空")
                }
                McuPrimaryButton(
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
                McuPrimaryButton(
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
