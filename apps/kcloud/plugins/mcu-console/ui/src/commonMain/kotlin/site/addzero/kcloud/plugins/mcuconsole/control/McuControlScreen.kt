package site.addzero.kcloud.plugins.mcuconsole.control

import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import site.addzero.cupertino.workbench.material3.HorizontalDivider
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.OutlinedTextField
import site.addzero.cupertino.workbench.material3.SecondaryTabRow
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.material3.Tab
import site.addzero.cupertino.workbench.material3.Text
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
import androidx.compose.ui.unit.Dp
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
import site.addzero.cupertino.workbench.button.WorkbenchButton as ShadcnButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonSize as ShadcnButtonSize
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant as ShadcnButtonVariant
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class McuControlTab(
    val id: String,
    val title: String,
) {
    SERIAL("serial", "串口终端"),
    OVERVIEW("overview", "设备概览"),
    RUNTIME("runtime", "运行监控"),
}

@Route(
    value = "设备会话",
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "物联网上位机",
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
    var selectedTabId by rememberSaveable { mutableStateOf(McuControlTab.SERIAL.id) }
    val workbenchState = rememberMcuWorkbenchState(state)
    val runAction = rememberMcuActionRunner()
    val contentScrollState = rememberScrollState()
    val selectedTab = McuControlTab.entries.firstOrNull { it.id == selectedTabId } ?: McuControlTab.SERIAL

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
            McuProjectNavigatorPanel(
                state = workbenchState,
                runAction = runAction,
                modifier = Modifier.width(340.dp).fillMaxHeight(),
            )
            McuBoundedScrollColumn(
                scrollState = contentScrollState,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                McuWorkbenchHeroPanel(
                    state = workbenchState,
                    runAction = runAction,
                    modifier = Modifier.fillMaxWidth(),
                )
                McuControlTabSection(
                    selectedTab = selectedTab,
                    onSelect = { tab -> selectedTabId = tab.id },
                )
                when (selectedTab) {
                    McuControlTab.SERIAL -> McuSerialWorkspaceTab(
                        state = workbenchState,
                        followLatestLogs = followLatestLogs,
                        onFollowLatestChange = { followLatestLogs = it },
                        runAction = runAction,
                    )
                    McuControlTab.OVERVIEW -> McuOverviewWorkspaceTab(
                        state = workbenchState,
                        runAction = runAction,
                    )
                    McuControlTab.RUNTIME -> McuRuntimeWorkspaceTab(
                        state = workbenchState,
                        runAction = runAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun McuBoundedScrollColumn(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier,
    ) {
        val contentModifier = if (maxHeight != Dp.Infinity) {
            Modifier.fillMaxSize().verticalScroll(scrollState)
        } else {
            Modifier.fillMaxWidth()
        }
        Column(
            modifier = contentModifier,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
private fun McuControlTabSection(
    selectedTab: McuControlTab,
    onSelect: (McuControlTab) -> Unit,
) {
    McuPanel(
        title = "工作台分区",
        modifier = Modifier.fillMaxWidth(),
    ) {
        SecondaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            McuControlTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onSelect(tab) },
                    text = { Text(tab.title) },
                )
            }
        }
    }
}

@Composable
private fun McuSerialWorkspaceTab(
    state: McuConsoleWorkbenchState,
    followLatestLogs: Boolean,
    onFollowLatestChange: (Boolean) -> Unit,
    runAction: (suspend () -> Unit) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            McuDevicePropertyEditorPanel(
                state = state,
                runAction = runAction,
                modifier = Modifier.weight(0.42f).heightIn(min = 540.dp),
            )
            McuCommandCatalogPanel(
                state = state,
                runAction = runAction,
                modifier = Modifier.weight(0.58f).heightIn(min = 540.dp),
            )
        }
        McuTerminalPanel(
            state = state,
            followLatestLogs = followLatestLogs,
            onFollowLatestChange = onFollowLatestChange,
            runAction = runAction,
            modifier = Modifier.fillMaxWidth().heightIn(min = 500.dp),
        )
    }
}

@Composable
private fun McuOverviewWorkspaceTab(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        McuDeviceOverviewPanel(
            state = state,
            modifier = Modifier.fillMaxWidth().heightIn(min = 480.dp),
        )
        McuRuntimeMonitorPanel(
            state = state,
            runAction = runAction,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun McuRuntimeWorkspaceTab(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            McuAssetBundlePanel(
                state = state,
                runAction = runAction,
                modifier = Modifier.weight(0.56f).heightIn(min = 620.dp),
            )
            McuRuntimeMonitorPanel(
                state = state,
                runAction = runAction,
                modifier = Modifier.weight(0.44f).heightIn(min = 620.dp),
            )
        }
    }
}

@Composable
private fun RowScope.McuControlTopActions(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
) {
    ShadcnButton(
        onClick = {
            runAction {
                state.refreshPorts()
            }
        },
        enabled = !state.isSubmitting,
        modifier = Modifier.heightIn(min = 38.dp),
        variant = ShadcnButtonVariant.Default,
        size = ShadcnButtonSize.Default,
        shape = RoundedCornerShape(12.dp),
        content = { Text("扫描设备") },
    )
    ShadcnButton(
        onClick = {
            runAction {
                state.refreshAll()
            }
        },
        enabled = !state.isSubmitting,
        modifier = Modifier.heightIn(min = 38.dp),
        variant = ShadcnButtonVariant.Outline,
        size = ShadcnButtonSize.Default,
        shape = RoundedCornerShape(12.dp),
        content = { Text("刷新资源") },
    )
    ShadcnButton(
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
        modifier = Modifier.heightIn(min = 38.dp),
        variant = ShadcnButtonVariant.Default,
        size = ShadcnButtonSize.Default,
        shape = RoundedCornerShape(12.dp),
        content = { Text(if (state.session.isOpen) "关闭会话" else "打开会话") },
    )
    ShadcnButton(
        onClick = {
            runAction {
                state.resetSession()
            }
        },
        enabled = state.canResetDevice,
        modifier = Modifier.heightIn(min = 38.dp),
        variant = ShadcnButtonVariant.Outline,
        size = ShadcnButtonSize.Default,
        shape = RoundedCornerShape(12.dp),
        content = { Text("设备复位") },
    )
    ShadcnButton(
        onClick = {
            runAction {
                state.ensureRuntime(forceReflash = false)
            }
        },
        enabled = state.canEnsureRuntime,
        modifier = Modifier.heightIn(min = 38.dp),
        variant = ShadcnButtonVariant.Outline,
        size = ShadcnButtonSize.Default,
        shape = RoundedCornerShape(12.dp),
        content = { Text("确保运行时") },
    )
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = state.selectedPortPath ?: "未选择设备",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.Monospace,
    )
}

@Composable
private fun McuProjectNavigatorPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    McuPanel(
        title = "工程资源",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "当前工程",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.selectedPort?.remark
                            ?: state.selectedPort?.portName
                            ?: "未选择设备",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.selectedPortPath ?: "请先从资源树里选中一个串口设备",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShadcnButton(
                            onClick = {
                                runAction {
                                    state.refreshPorts()
                                }
                            },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Default,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text("扫描串口") },
                        )
                        ShadcnButton(
                            onClick = {
                                runAction {
                                    state.refreshAll()
                                }
                            },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Outline,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text("刷新资源") },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
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

            McuSummaryTable(
                rows = listOf(
                    "已发现设备" to state.ports.size.toString(),
                    "筛选结果" to state.filteredPorts.size.toString(),
                    "当前会话" to if (state.session.isOpen) "在线" else "未连接",
                    "运行时" to state.runtimeStatus.state.name,
                ),
            )
        }
    }
}

@Composable
private fun McuWorkbenchHeroPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPort = state.selectedPort
    McuPanel(
        title = "设备概览",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                McuStatusChip(
                    label = "协议",
                    value = state.selectedModbusAtomicAction.displayName(),
                    positive = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = selectedPort?.remark
                            ?: selectedPort?.portName
                            ?: "未选择设备",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = state.selectedPortPath ?: "请先在左侧资源树选中串口设备",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                    McuInfoNotice(
                        text = listOf(
                            selectedPort?.descriptiveName,
                            selectedPort?.description,
                            selectedPort?.manufacturer,
                            selectedPort?.deviceKey,
                        ).filterNotNull().filter { it.isNotBlank() }
                            .joinToString(" / ")
                            .ifBlank { "当前设备还没有更多可展示的工程元数据。" },
                    )
                }
                Column(
                    modifier = Modifier.widthIn(min = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShadcnButton(
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
                                state.canOpenSelectedTransportSession
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Default,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text(if (state.session.isOpen) "关闭会话" else "打开会话") },
                        )
                        ShadcnButton(
                            onClick = {
                                runAction {
                                    state.ensureRuntime(forceReflash = false)
                                }
                            },
                            enabled = state.canEnsureRuntime,
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Outline,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text("确保运行时") },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShadcnButton(
                            onClick = {
                                runAction {
                                    state.refreshRuntimeStatus()
                                    state.refreshFlashStatus()
                                    state.refreshDeviceOverview()
                                }
                            },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Outline,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text("刷新状态") },
                        )
                        ShadcnButton(
                            onClick = {
                                runAction {
                                    state.resetSession()
                                }
                            },
                            enabled = state.canResetDevice,
                            modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                            variant = ShadcnButtonVariant.Outline,
                            size = ShadcnButtonSize.Default,
                            shape = RoundedCornerShape(12.dp),
                            content = { Text("设备复位") },
                        )
                    }
                }
            }

            McuSummaryTable(
                rows = listOf(
                    "设备键" to (selectedPort?.deviceKey ?: "未提供"),
                    "设备备注" to (selectedPort?.remark ?: "未填写"),
                    "运行时包" to (state.selectedRuntimeBundle?.title ?: "未选择"),
                    "烧录模板" to (state.selectedFlashProfile?.title ?: "未选择"),
                ),
            )
        }
    }
}

@Composable
private fun McuDevicePropertyEditorPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "属性编辑",
        modifier = modifier,
    ) {
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPanelSectionTitle("设备元数据")
            McuCompactInput(
                value = state.selectedPortRemarkDraft,
                onValueChange = { state.updateSelectedPortRemarkDraft(it) },
                label = "设备备注",
                supportingText = "备注会跟随稳定设备键持久化保存。",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.saveSelectedPortRemark()
                        }
                    },
                    enabled = !state.isSubmitting && state.selectedPort != null,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("保存备注") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.refreshPorts()
                        }
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("重新扫描") },
                )
            }
            McuSummaryTable(
                rows = listOf(
                    "串口路径" to (state.selectedPortPath ?: "未选择"),
                    "设备名称" to (state.selectedPort?.portName ?: "-"),
                    "制造商" to (state.selectedPort?.manufacturer ?: "-"),
                    "稳定键" to (state.selectedPort?.deviceKey ?: "未提供"),
                ),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            McuPanelSectionTitle("串口连接")
            McuCompactInput(
                value = state.baudRateText,
                onValueChange = { value ->
                    state.updateTransportDraft { copy(baudRate = value.toIntOrNull()) }
                },
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
                    state.updateSerialConsoleDraft { copy(lineEnding = lineEnding) }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
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
                        state.canOpenSelectedTransportSession
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.session.isOpen) "关闭终端" else "打开终端") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.resetSession()
                        }
                    },
                    enabled = state.canResetDevice,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("设备复位") },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            McuPanelSectionTitle("脚本预览")
            McuScriptPreview(
                script = state.scriptText.ifBlank { state.serialCommandText },
            )
        }
    }
}

@Composable
private fun McuAssetBundlePanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "运行时与能力包",
        modifier = modifier,
    ) {
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPanelSectionTitle("运行时包")
            McuRuntimeBundleBrowser(
                bundles = state.runtimeBundles,
                selectedBundleId = state.selectedRuntimeBundleId,
                onSelect = { bundleId -> state.selectRuntimeBundle(bundleId) },
                modifier = Modifier.fillMaxWidth().height(148.dp),
            )
            McuPanelSectionTitle("烧录模板")
            McuFlashProfileBrowser(
                profiles = state.flashProfiles,
                selectedProfileId = state.selectedFlashProfileId,
                onSelect = { profileId -> state.selectFlashProfile(profileId) },
                modifier = Modifier.fillMaxWidth().height(116.dp),
            )
            McuPanelSectionTitle("探针")
            McuFlashProbeBrowser(
                probes = state.flashProbes,
                selectedSerialNumber = state.selectedFlashProbeSerialNumber,
                onSelect = { serialNumber -> state.selectFlashProbe(serialNumber) },
                modifier = Modifier.fillMaxWidth().height(116.dp),
            )
            McuCompactInput(
                value = state.firmwarePathText,
                onValueChange = { value ->
                    state.updateFlashEditorDraft { copy(firmwarePathText = value) }
                },
                label = state.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                supportingText = state.runtimeStatus.artifactPath
                    ?: state.selectedFlashProfile?.artifactHint,
            )
            McuSummaryTable(
                rows = listOf(
                    "运行时状态" to state.runtimeStatus.state.name,
                    "默认烧录" to (state.runtimeStatus.defaultFlashProfileId
                        ?: state.selectedRuntimeBundle?.defaultFlashProfileId.orEmpty()),
                    "烧录探针" to (state.selectedFlashProbe?.serialNumber
                        ?: state.selectedFlashProbe?.productName
                        ?: "未选择"),
                    "烧录进度" to "${state.flashStatus.progressPercent.toInt()}%",
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.refreshRuntimeBundles()
                            state.refreshFlashProfiles()
                            state.refreshFlashProbes()
                            state.refreshRuntimeStatus()
                        }
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("刷新能力") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.startFlash()
                        }
                    },
                    enabled = state.canStartFlash,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("开始烧录") },
                )
            }
        }
    }
}

@Composable
private fun McuCommandCatalogPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "协议与脚本",
        modifier = modifier,
    ) {
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuPanelSectionTitle("原子指令")
            McuScriptCommandSection(state = state)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("示例脚本")
            McuScriptExampleSection(state = state)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("Modbus 摘要")
            McuSummaryTable(rows = state.modbusConnectionSummaryRows())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.executeScript()
                        }
                    },
                    enabled = state.hasActiveSession && state.isRuntimeReady && !state.isScriptRunning,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("执行脚本") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.executeSelectedModbusAction()
                        }
                    },
                    enabled = state.canExecuteSelectedModbusAction,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("执行 Modbus") },
                )
            }
            McuScriptPreview(
                script = listOf(
                    state.scriptText.takeIf { it.isNotBlank() },
                    state.modbusLastResultRows().joinToString("\n") { (label, value) ->
                        "$label: $value"
                    },
                ).filterNotNull().joinToString("\n\n").ifBlank { "-" },
            )
        }
    }
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
    val lightStates = List(24) { index -> state.devicePowerLights.lights.getOrNull(index) == true }
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
                McuStatusChip(
                    label = "24 通路",
                    value = "${state.devicePowerLights.onCount}/24",
                    positive = state.devicePowerLights.onCount > 0,
                    modifier = Modifier.weight(1f),
                )
            }
            McuSummaryTable(
                rows = listOf(
                    "当前设备" to (state.selectedPortPath ?: "未选择"),
                    "设备键" to (selectedPort?.deviceKey ?: "未提供"),
                    "设备备注" to (selectedPort?.remark ?: "未填写"),
                    "固件版本" to (state.deviceInfo.firmwareVersion ?: "-"),
                    "CPU" to (state.deviceInfo.cpuModel ?: "-"),
                    "Flash" to formatBytes(state.deviceInfo.flashSizeBytes),
                    "MAC" to (state.deviceInfo.macAddress ?: "-"),
                ),
            )
            McuPanelSectionTitle("24 通路状态")
            McuPowerLightsGrid(
                states = lightStates,
                modifier = Modifier.fillMaxWidth(),
            )
            selectedPort?.let { port ->
                McuInfoNotice(
                    text = listOf(
                        port.portName,
                        port.descriptiveName,
                        port.description,
                        port.manufacturer,
                        state.deviceInfo.updatedAt?.let { "更新时间 $it" },
                    )
                        .filterNotNull()
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
private fun McuRuntimeMonitorPanel(
    state: McuConsoleWorkbenchState,
    runAction: (suspend () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    McuPanel(
        title = "运行监控",
        modifier = modifier,
    ) {
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuSummaryTable(
                rows = listOf(
                    "会话" to if (state.session.isOpen) "在线" else "离线",
                    "运行时" to state.runtimeStatus.state.name,
                    "脚本" to state.scriptStatus.state.name,
                    "烧录" to state.flashStatus.state.name,
                    "探针" to (state.selectedFlashProbe?.serialNumber ?: state.selectedFlashProbe?.productName ?: "未选择"),
                    "芯片" to formatChipId(state.flashStatus.targetChipId),
                    "电压" to formatVoltage(state.flashStatus.targetVoltageMillivolts),
                    "功率通路" to "${state.devicePowerLights.onCount}/24",
                    "事件缓存" to state.events.size.toString(),
                    "最近消息" to (state.runtimeStatus.lastMessage
                        ?: state.flashStatus.lastMessage
                        ?: state.scriptStatus.lastMessage
                        ?: state.devicePowerLights.lastMessage
                        ?: "-"),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.refreshRuntimeStatus()
                            state.refreshFlashStatus()
                            state.refreshDeviceOverview()
                        }
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("刷新监控") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.ensureRuntime(forceReflash = false)
                        }
                    },
                    enabled = state.canEnsureRuntime,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("确保运行时") },
                )
            }
            McuPanelSectionTitle("设备信息")
            McuSummaryTable(
                rows = listOf(
                    "串口" to (state.deviceInfo.portPath ?: state.selectedPortPath ?: "未选择"),
                    "固件版本" to (state.deviceInfo.firmwareVersion ?: "-"),
                    "CPU" to (state.deviceInfo.cpuModel ?: "-"),
                    "晶振" to state.deviceInfo.xtalFrequencyHz?.let { "$it Hz" }.orEmpty().ifBlank { "-" },
                    "Flash" to formatBytes(state.deviceInfo.flashSizeBytes),
                    "MAC" to (state.deviceInfo.macAddress ?: "-"),
                    "更新时间" to (state.deviceInfo.updatedAt ?: "-"),
                ),
            )
            McuPanelSectionTitle("烧录进度")
            McuSummaryTable(
                rows = listOf(
                    "文件" to (state.flashStatus.firmwarePath ?: state.firmwarePathText.ifBlank { "-" }),
                    "地址" to state.flashStatus.flashStartAddress?.let { "0x${it.toString(16).uppercase()}" }.orEmpty().ifBlank { "-" },
                    "进度" to "${state.flashStatus.progressPercent.toInt()}%",
                    "字节" to "${state.flashStatus.bytesSent} / ${state.flashStatus.totalBytes}",
                    "探针描述" to (state.flashStatus.probeDescription ?: "-"),
                    "更新时间" to (state.flashStatus.updatedAt ?: "-"),
                ),
            )
        }
    }
}

@Composable
private fun McuPowerLightsGrid(
    states: List<Boolean>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        states.chunked(6).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEachIndexed { columnIndex, isOn ->
                    val index = rowIndex * 6 + columnIndex
                    McuPowerLightItem(
                        index = index,
                        isOn = isOn,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun McuPowerLightItem(
    index: Int,
    isOn: Boolean,
    modifier: Modifier = Modifier,
) {
    val lightColor = if (isOn) {
        Color(0xFF16A34A)
    } else {
        MaterialTheme.colorScheme.outline
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(10.dp).background(lightColor, CircleShape),
            )
            Text(
                text = "CH${(index + 1).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
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
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
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
                ShadcnButton(
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
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.session.isOpen) "关闭终端" else "打开终端") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.resetSession()
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("设备复位") },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.updateDtr(!state.session.dtrEnabled)
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.session.dtrEnabled) "关闭 DTR" else "开启 DTR") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.updateRts(!state.session.rtsEnabled)
                        }
                    },
                    enabled = state.canControlSerialLines,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.session.rtsEnabled) "关闭 RTS" else "开启 RTS") },
                )
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
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.refreshRuntimeBundles()
                            state.refreshRuntimeStatus()
                        }
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("刷新运行时") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.ensureRuntime(forceReflash = false)
                        }
                    },
                    enabled = state.canEnsureRuntime,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("确保运行时") },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            McuPanelSectionTitle("烧录能力")
            McuFlashProfileBrowser(
                profiles = state.flashProfiles,
                selectedProfileId = state.selectedFlashProfileId,
                onSelect = { profileId -> state.selectFlashProfile(profileId) },
                modifier = Modifier.fillMaxWidth().height(132.dp),
            )
            McuFlashProbeBrowser(
                probes = state.flashProbes,
                selectedSerialNumber = state.selectedFlashProbeSerialNumber,
                onSelect = { serialNumber -> state.selectFlashProbe(serialNumber) },
                modifier = Modifier.fillMaxWidth().height(132.dp),
            )
            McuCompactInput(
                value = state.firmwarePathText,
                onValueChange = { value ->
                    state.updateFlashEditorDraft { copy(firmwarePathText = value) }
                },
                label = state.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                supportingText = state.runtimeStatus.artifactPath
                    ?: state.selectedFlashProfile?.artifactHint,
            )
            McuSummaryTable(
                rows = listOf(
                    "烧录状态" to state.flashStatus.state.name,
                    "探针" to (state.selectedFlashProbe?.serialNumber ?: state.selectedFlashProbe?.productName ?: "未选择"),
                    "芯片" to (state.flashStatus.targetChipId?.let { chipId ->
                        "0x${chipId.toString(16).uppercase()}"
                    } ?: "-"),
                    "进度" to "${state.flashStatus.bytesSent} / ${state.flashStatus.totalBytes}",
                    "百分比" to "${state.flashStatus.progressPercent.toInt()}%",
                    "消息" to (state.flashStatus.lastMessage ?: state.runtimeStatus.lastMessage.orEmpty()),
                ),
            )
            ShadcnButton(
                onClick = {
                    runAction {
                        state.startFlash()
                    }
                },
                enabled = state.canStartFlash,
                modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp),
                variant = ShadcnButtonVariant.Default,
                size = ShadcnButtonSize.Default,
                shape = RoundedCornerShape(12.dp),
                content = { Text("开始烧录") },
            )
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
                ShadcnButton(
                    onClick = onRefresh,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("扫描") },
                )
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
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuCompactInput(
                value = state.baudRateText,
                onValueChange = { value ->
                    state.updateTransportDraft { copy(baudRate = value.toIntOrNull()) }
                },
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
                    state.updateSerialConsoleDraft { copy(lineEnding = lineEnding) }
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
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
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
                onValueChange = { value ->
                    state.updateScriptEditorDraft { copy(timeoutMsText = value) }
                },
                label = "timeoutMs",
            )
            OutlinedTextField(
                value = state.scriptText,
                onValueChange = { value ->
                    state.updateScriptEditorDraft { copy(scriptText = value) }
                },
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
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.executeScript()
                        }
                    },
                    enabled = state.hasActiveSession && state.isRuntimeReady && !state.isScriptRunning,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("执行脚本") },
                )
                ShadcnButton(
                    onClick = {
                        runAction {
                            state.stopScript()
                        }
                    },
                    enabled = state.hasActiveSession && state.isScriptRunning,
                    modifier = Modifier.weight(1f).heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("停止脚本") },
                )
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
        McuBoundedScrollColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize(),
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
                onSelect = { frameFormat ->
                    state.updateModbusDraft { copy(frameFormat = frameFormat) }
                },
            )
            McuCompactInput(
                value = state.modbusRtuUnitIdText,
                onValueChange = { value ->
                    state.updateTransportDraft { copy(unitId = value.toIntOrNull()) }
                },
                label = "站号 UnitId",
                supportingText = "1..247",
            )
            McuCompactInput(
                value = state.modbusRtuTimeoutMsText,
                onValueChange = { value ->
                    state.updateTransportDraft { copy(timeoutMs = value.toIntOrNull()) }
                },
                label = "超时(ms)",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                McuCompactInput(
                    value = state.modbusRtuDataBitsText,
                    onValueChange = { value ->
                        state.updateTransportDraft { copy(dataBits = value.toIntOrNull()) }
                    },
                    label = "数据位",
                    modifier = Modifier.weight(1f),
                )
                McuCompactInput(
                    value = state.modbusRtuStopBitsText,
                    onValueChange = { value ->
                        state.updateTransportDraft { copy(stopBits = value.toIntOrNull()) }
                    },
                    label = "停止位",
                    modifier = Modifier.weight(1f),
                )
            }
            McuChoiceChipRow(
                items = McuModbusSerialParity.entries,
                selectedItem = state.modbusRtuParity,
                labelOf = { parity -> parity.displayName() },
                onSelect = { parity ->
                    state.updateTransportDraft { copy(parity = parity) }
                },
            )
            when (state.selectedModbusAtomicAction) {
                McuModbusAtomicAction.GPIO_WRITE -> {
                    McuCompactInput(
                        value = state.modbusGpioWritePinText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(gpioWritePinText = value) }
                        },
                        label = "GPIO 引脚",
                    )
                    McuChoiceChipRow(
                        items = listOf(true, false),
                        selectedItem = state.modbusGpioWriteHigh,
                        labelOf = { high -> if (high) "HIGH" else "LOW" },
                        onSelect = { high ->
                            state.updateModbusDraft { copy(gpioWriteHigh = high) }
                        },
                    )
                }

                McuModbusAtomicAction.GPIO_MODE -> {
                    McuCompactInput(
                        value = state.modbusGpioModePinText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(gpioModePinText = value) }
                        },
                        label = "GPIO 引脚",
                    )
                    McuChoiceChipRow(
                        items = McuModbusGpioMode.entries,
                        selectedItem = state.modbusGpioMode,
                        labelOf = { mode -> mode.displayName() },
                        onSelect = { mode ->
                            state.updateModbusDraft { copy(gpioMode = mode) }
                        },
                    )
                }

                McuModbusAtomicAction.PWM_DUTY -> {
                    McuCompactInput(
                        value = state.modbusPwmPinText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(pwmPinText = value) }
                        },
                        label = "PWM 引脚",
                    )
                    McuCompactInput(
                        value = state.modbusPwmDutyText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(pwmDutyText = value) }
                        },
                        label = "dutyU16",
                        supportingText = "合法范围 0..65535",
                    )
                }

                McuModbusAtomicAction.SERVO_ANGLE -> {
                    McuCompactInput(
                        value = state.modbusServoPinText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(servoPinText = value) }
                        },
                        label = "舵机引脚",
                    )
                    McuCompactInput(
                        value = state.modbusServoAngleText,
                        onValueChange = { value ->
                            state.updateModbusDraft { copy(servoAngleText = value) }
                        },
                        label = "目标角度",
                        supportingText = "合法范围 0..180",
                    )
                }
            }
            ShadcnButton(
                onClick = {
                    runAction {
                        state.executeSelectedModbusAction()
                    }
                },
                enabled = state.canExecuteSelectedModbusAction,
                modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp),
                variant = ShadcnButtonVariant.Default,
                size = ShadcnButtonSize.Default,
                shape = RoundedCornerShape(12.dp),
                content = { Text("执行 ${state.selectedModbusAtomicAction.displayName()}") },
            )
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
                ShadcnButton(
                    onClick = {
                        onFollowLatestChange(!followLatestLogs)
                    },
                    modifier = Modifier.heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (followLatestLogs) "停止跟随" else "跟随最新") },
                )
                ShadcnButton(
                    onClick = {
                        state.clearVisibleEvents()
                    },
                    modifier = Modifier.heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Outline,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text("清空") },
                )
                ShadcnButton(
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
                    modifier = Modifier.heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.session.isOpen) "Ctrl+C" else "打开终端") },
                )
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
                    onValueChange = { value ->
                        state.updateSerialConsoleDraft { copy(commandText = value) }
                    },
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
                ShadcnButton(
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
                    modifier = Modifier.heightIn(min = 38.dp),
                    variant = ShadcnButtonVariant.Default,
                    size = ShadcnButtonSize.Default,
                    shape = RoundedCornerShape(12.dp),
                    content = { Text(if (state.serialCommandText.isEmpty()) "回车" else "发送") },
                )
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

private fun formatBytes(bytes: Int?): String {
    val value = bytes ?: return "-"
    return when {
        value >= 1024 * 1024 -> "${formatDecimal(value / 1024f / 1024f, 1)} MB"
        value >= 1024 -> "${formatDecimal(value / 1024f, 1)} KB"
        else -> "$value B"
    }
}

private fun formatChipId(chipId: Int?): String {
    return chipId?.let { "0x${it.toString(16).uppercase()}" } ?: "-"
}

private fun formatVoltage(millivolts: Int?): String {
    return millivolts?.let { "${formatDecimal(it / 1000f, 2)} V" } ?: "-"
}

/**
 * 兼容 commonMain 的定点格式化，避免依赖 JVM 专属的 String.format。
 */
private fun formatDecimal(value: Float, digits: Int): String {
    if (digits <= 0) {
        return value.roundToInt().toString()
    }

    val factor = when (digits) {
        1 -> 10
        2 -> 100
        3 -> 1_000
        else -> {
            var computedFactor = 1
            repeat(digits) {
                computedFactor *= 10
            }
            computedFactor
        }
    }
    val rounded = (value * factor).roundToInt()
    val integerPart = rounded / factor
    val fractionPart = abs(rounded % factor)
    return buildString {
        append(integerPart)
        append('.')
        append(fractionPart.toString().padStart(digits, '0'))
    }
}
