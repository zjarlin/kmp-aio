package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.material3.CircularProgressIndicator
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.OutlinedTextField
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.material3.Text

@Route(
    value = "开发工具",
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "物联网上位机",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuFlashScreen() {
    val viewModel = koinViewModel<McuFlashViewModel>()
    val state = viewModel.screenState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        McuFlashHeader(
            state = state,
            onRefreshAll = viewModel::refresh,
            onRefreshProbes = viewModel::refreshProbes,
            onRefreshStatus = viewModel::refreshStatus,
        )
        state.errorMessage?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        state.noticeMessage?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        state.probeMessage?.takeIf { it != state.noticeMessage }?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (maxWidth >= 1080.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(0.56f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        McuFlashConfigPanel(
                            state = state,
                            onProfileSelected = viewModel::selectProfile,
                            onFirmwarePathChanged = viewModel::updateFirmwarePath,
                            onStartAddressChanged = viewModel::updateStartAddressInput,
                            onStartFlash = viewModel::startFlash,
                            onReset = viewModel::resetTarget,
                        )
                        McuFlashProbePanel(
                            state = state,
                            onSelectAuto = viewModel::selectAutoProbe,
                            onProbeSelected = viewModel::selectProbe,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.44f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        McuFlashStatusPanel(state)
                        McuFlashRuntimeSummaryPanel(state)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    McuFlashConfigPanel(
                        state = state,
                        onProfileSelected = viewModel::selectProfile,
                        onFirmwarePathChanged = viewModel::updateFirmwarePath,
                        onStartAddressChanged = viewModel::updateStartAddressInput,
                        onStartFlash = viewModel::startFlash,
                        onReset = viewModel::resetTarget,
                    )
                    McuFlashProbePanel(
                        state = state,
                        onSelectAuto = viewModel::selectAutoProbe,
                        onProbeSelected = viewModel::selectProbe,
                    )
                    McuFlashStatusPanel(state)
                    McuFlashRuntimeSummaryPanel(state)
                }
            }
        }
    }
}

@Composable
private fun McuFlashHeader(
    state: McuFlashScreenState,
    onRefreshAll: () -> Unit,
    onRefreshProbes: () -> Unit,
    onRefreshStatus: () -> Unit,
) {
    McuFlashPanel(
        title = "MCU 烧录",
        subtitle = "通过 ST-Link SWD 选择探针、填写固件路径并发起烧录。",
        actions = {
            WorkbenchActionButton(
                text = if (state.loading) "加载中" else "刷新页面",
                onClick = onRefreshAll,
                variant = WorkbenchButtonVariant.Outline,
                enabled = !state.busy,
            )
            WorkbenchActionButton(
                text = "刷新探针",
                onClick = onRefreshProbes,
                variant = WorkbenchButtonVariant.Secondary,
                enabled = !state.busy,
            )
            WorkbenchActionButton(
                text = "刷新状态",
                onClick = onRefreshStatus,
                variant = WorkbenchButtonVariant.Secondary,
                enabled = !state.loading && !state.busy,
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = state.selectedProfile?.title ?: "等待烧录配置",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = state.status.state.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = state.status.state.tint(),
                )
            }
            if (state.loading || state.busy || state.running) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun McuFlashConfigPanel(
    state: McuFlashScreenState,
    onProfileSelected: (String) -> Unit,
    onFirmwarePathChanged: (String) -> Unit,
    onStartAddressChanged: (String) -> Unit,
    onStartFlash: () -> Unit,
    onReset: () -> Unit,
) {
    McuFlashPanel(
        title = "烧录配置",
        subtitle = "配置列表来自后台 `FlashController`，页面只负责选择和触发。",
    ) {
        if (state.profiles.isEmpty()) {
            McuFlashInlineNotice("当前没有可用烧录配置。")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.profiles.forEach { profile ->
                    McuFlashSelectableCard(
                        title = profile.title,
                        caption = buildProfileCaption(profile),
                        selected = state.selectedProfileId == profile.id,
                        enabled = !state.running && !state.busy,
                        onClick = {
                            onProfileSelected(profile.id)
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.firmwarePath,
            onValueChange = onFirmwarePathChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("固件路径")
            },
            placeholder = {
                Text(state.selectedProfile?.artifactHint ?: "例如 /tmp/firmware.bin")
            },
            supportingText = {
                Text("当前直接填写本机固件文件路径，后端会按 ST-Link 流程读取并烧录。")
            },
            singleLine = true,
            enabled = !state.running && !state.busy,
        )

        OutlinedTextField(
            value = state.startAddressInput,
            onValueChange = onStartAddressChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("起始地址")
            },
            placeholder = {
                Text(state.selectedProfile?.defaultStartAddress?.toHexAddress() ?: "0x08000000")
            },
            supportingText = {
                Text("留空时使用当前配置默认地址，支持 `0x08000000` 或十进制。")
            },
            singleLine = true,
            enabled = !state.running && !state.busy,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkbenchActionButton(
                text = if (state.busy) "提交中" else "开始烧录",
                onClick = onStartFlash,
                enabled = !state.loading && !state.busy && !state.running && state.selectedProfile != null,
                modifier = Modifier.weight(1f),
            )
            WorkbenchActionButton(
                text = "发送复位",
                onClick = onReset,
                variant = WorkbenchButtonVariant.Outline,
                enabled = !state.loading && !state.busy && !state.running,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun McuFlashProbePanel(
    state: McuFlashScreenState,
    onSelectAuto: () -> Unit,
    onProbeSelected: (String) -> Unit,
) {
    McuFlashPanel(
        title = "ST-Link 探针",
        subtitle = "手动指定序列号时，后端会把该序列号传给 `StLinkConfig`。",
    ) {
        McuFlashSelectableCard(
            title = "自动选择首个可用探针",
            caption = "适合只连接一个 ST-Link 的场景。",
            selected = state.useAutoProbeSelection,
            enabled = !state.running && !state.busy,
            onClick = onSelectAuto,
        )
        if (state.probes.isEmpty()) {
            McuFlashInlineNotice("未发现可选探针时仍可先填写固件路径，待设备接入后再刷新。")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.probes.forEach { probe ->
                    val serialNumber = probe.serialNumber
                    McuFlashSelectableCard(
                        title = probe.displayName(),
                        caption = buildProbeCaption(probe),
                        selected = !state.useAutoProbeSelection && serialNumber == state.selectedProbeSerialNumber,
                        enabled = serialNumber != null && !state.running && !state.busy,
                        onClick = {
                            if (serialNumber != null) {
                                onProbeSelected(serialNumber)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun McuFlashStatusPanel(
    state: McuFlashScreenState,
) {
    val progress = (state.status.progressPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
    McuFlashPanel(
        title = "任务状态",
        subtitle = "这里展示后端当前保存的烧录状态快照。",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            McuFlashStateBadge(state.status.state)
            Text(
                text = "${state.status.progressPercent.toCompactNumber()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        McuFlashProgressBar(progress)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            McuFlashKeyValueRow("当前阶段", state.status.currentStage.orDash())
            McuFlashKeyValueRow(
                "发送字节",
                "${state.status.bytesSent.toSizeLabel()} / ${state.status.totalBytes.toSizeLabel()}",
            )
            McuFlashKeyValueRow("固件路径", state.status.firmwarePath.orDash())
            McuFlashKeyValueRow(
                "烧录地址",
                state.status.flashStartAddress?.toHexAddress().orDash(),
            )
            McuFlashKeyValueRow("校验结果", state.status.verified.toYesNo())
            McuFlashKeyValueRow("是否启动应用", state.status.startedApplication.toYesNo())
            McuFlashKeyValueRow("开始时间", state.status.startedAt.orDash())
            McuFlashKeyValueRow("结束时间", state.status.finishedAt.orDash())
            McuFlashKeyValueRow("最近更新", state.status.updatedAt.orDash())
        }
    }
}

@Composable
private fun McuFlashRuntimeSummaryPanel(
    state: McuFlashScreenState,
) {
    val profile = state.selectedProfile
    McuFlashPanel(
        title = "运行摘要",
        subtitle = "便于确认当前目标、电压和探针落点。",
    ) {
        McuFlashKeyValueRow("当前配置", profile?.title.orDash())
        McuFlashKeyValueRow("传输方式", profile?.transport.orDash())
        McuFlashKeyValueRow("默认起始地址", profile?.defaultStartAddress?.toHexAddress().orDash())
        McuFlashKeyValueRow("支持 chipId", profile?.supportedChipIds.toChipIdLabel())
        McuFlashKeyValueRow("固件提示", profile?.artifactHint.orDash())
        McuFlashKeyValueRow(
            "目标 chipId",
            state.status.targetChipId?.toChipIdLabel().orDash(),
        )
        McuFlashKeyValueRow(
            "目标电压",
            state.status.targetVoltageMillivolts?.toVoltageLabel().orDash(),
        )
        McuFlashKeyValueRow(
            "运行中探针",
            state.status.probeSerialNumber?.let { serial ->
                "${state.status.probeDescription.orDash()} / $serial"
            }.orDash(),
        )
        McuFlashKeyValueRow(
            "预选探针",
            if (state.useAutoProbeSelection) {
                "自动选择"
            } else {
                state.selectedProbe?.let { probe ->
                    "${probe.displayName()} / ${probe.serialNumber.orDash()}"
                }.orDash()
            },
        )
    }
}

@Composable
private fun McuFlashPanel(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = actions,
                )
            }
            content()
        }
    }
}

@Composable
private fun McuFlashBanner(
    text: String,
    tone: Color,
    contentColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = tone,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun McuFlashInlineNotice(
    text: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun McuFlashSelectableCard(
    title: String,
    caption: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun McuFlashKeyValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun McuFlashStateBadge(
    state: McuFlashRunState,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = state.tint().copy(alpha = 0.16f),
    ) {
        Text(
            text = state.label(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = state.tint(),
        )
    }
}

@Composable
private fun McuFlashProgressBar(
    progress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {}
        if (progress > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary,
            ) {}
        }
    }
}

private fun McuFlashRunState.label(): String {
    return when (this) {
        McuFlashRunState.IDLE -> "空闲"
        McuFlashRunState.RUNNING -> "运行中"
        McuFlashRunState.COMPLETED -> "已完成"
        McuFlashRunState.ERROR -> "失败"
    }
}

@Composable
private fun McuFlashRunState.tint(): Color {
    return when (this) {
        McuFlashRunState.IDLE -> MaterialTheme.colorScheme.primary
        McuFlashRunState.RUNNING -> MaterialTheme.colorScheme.tertiary
        McuFlashRunState.COMPLETED -> Color(0xFF1B8A5A)
        McuFlashRunState.ERROR -> MaterialTheme.colorScheme.error
    }
}

private fun buildProfileCaption(
    profile: McuFlashProfileSummary,
): String {
    return buildList {
        add("传输: ${profile.transport}")
        add("默认地址: ${profile.defaultStartAddress.toHexAddress()}")
        add("支持: ${profile.supportedChipIds.toChipIdLabel()}")
        profile.artifactHint?.takeIf { it.isNotBlank() }?.let { hint ->
            add("提示: $hint")
        }
    }.joinToString("  ·  ")
}

private fun buildProbeCaption(
    probe: McuFlashProbeSummary,
): String {
    return buildList {
        add("VID:PID ${probe.vendorId.toHexShort()}:${probe.productId.toHexShort()}")
        add("序列号: ${probe.serialNumber.orDash()}")
        probe.manufacturerName?.takeIf { it.isNotBlank() }?.let { manufacturer ->
            add("厂商: $manufacturer")
        }
        if (probe.serialNumber == null) {
            add("该探针未暴露序列号，仅支持自动选择")
        }
    }.joinToString("  ·  ")
}

private fun McuFlashProbeSummary.displayName(): String {
    return productName
        ?: manufacturerName
        ?: "ST-Link 探针"
}

private fun List<Int>?.toChipIdLabel(): String {
    if (this == null || isEmpty()) {
        return "未限制"
    }
    return joinToString(", ") { chipId ->
        chipId.toChipIdLabel()
    }
}

private fun Int.toChipIdLabel(): String {
    return "0x${toString(16).uppercase()}"
}

private fun Int.toHexShort(): String {
    return toString(16).uppercase().padStart(4, '0')
}

private fun Long.toHexAddress(): String {
    return "0x${toString(16).uppercase().padStart(8, '0')}"
}

private fun Long.toSizeLabel(): String {
    if (this <= 0L) {
        return "0 B"
    }
    if (this < 1024L) {
        return "${this} B"
    }
    val kiloBytes = this / 1024.0
    if (kiloBytes < 1024.0) {
        return "${kiloBytes.toCompactNumber()} KB"
    }
    val megaBytes = kiloBytes / 1024.0
    if (megaBytes < 1024.0) {
        return "${megaBytes.toCompactNumber()} MB"
    }
    val gigaBytes = megaBytes / 1024.0
    return "${gigaBytes.toCompactNumber()} GB"
}

private fun Double.toCompactNumber(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

private fun Int.toVoltageLabel(): String {
    val volts = this / 1000
    val millivolts = (this % 1000).toString().padStart(3, '0')
    return "$volts.$millivolts V"
}

private fun Boolean.toYesNo(): String {
    return if (this) {
        "是"
    } else {
        "否"
    }
}

private fun String?.orDash(): String {
    return this?.takeIf { it.isNotBlank() } ?: "-"
}
