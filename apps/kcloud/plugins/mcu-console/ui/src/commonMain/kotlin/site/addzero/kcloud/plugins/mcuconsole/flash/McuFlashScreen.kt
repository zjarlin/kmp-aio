package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.cupertino.workbench.material3.CircularProgressIndicator
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.material3.Text

/**
 * 处理mcu烧录界面。
 */
@Route(
    value = "开发工具",
    title = "ST-LINK烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "上位机",
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
            viewModel = viewModel,
        )
        state.errorMessage?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.errorContainer,
            )
        }
        state.noticeMessage?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.secondaryContainer,
            )
        }
        state.probeMessage?.takeIf { it != state.noticeMessage }?.let { message ->
            McuFlashBanner(
                text = message,
                tone = MaterialTheme.colorScheme.surfaceVariant,
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
                            viewModel = viewModel,
                        )
                        McuFlashProbePanel(
                            state = state,
                            viewModel = viewModel,
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
                        viewModel = viewModel,
                    )
                    McuFlashProbePanel(
                        state = state,
                        viewModel = viewModel,
                    )
                    McuFlashStatusPanel(state)
                    McuFlashRuntimeSummaryPanel(state)
                }
            }
        }
    }
}

/**
 * 处理mcu烧录header。
 *
 * @param state 状态。
 * @param viewModel 页面视图模型。
 */
@Composable
private fun McuFlashHeader(
    state: McuFlashScreenState,
    viewModel: McuFlashViewModel,
) {
    val headerActionsSpi = koinInject<McuFlashHeaderActionsSpi>()
    CupertinoPanel(
        title = "MCU 烧录",
        subtitle = "通过 ST-Link SWD 选择探针、填写固件路径并发起烧录。",
        actions = {
            headerActionsSpi.Render(
                state = state,
                viewModel = viewModel,
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

/**
 * 处理mcu烧录配置panel。
 *
 * @param state 状态。
 * @param viewModel 页面视图模型。
 */
@Composable
private fun McuFlashConfigPanel(
    state: McuFlashScreenState,
    viewModel: McuFlashViewModel,
) {
    val configActionsSpi = koinInject<McuFlashConfigActionsSpi>()
    val selectionCardSpi = koinInject<McuFlashSelectionCardSpi>()
    CupertinoPanel(
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
                    selectionCardSpi.Render(
                        state = McuFlashSelectionCardState(
                            title = profile.title,
                            caption = buildProfileCaption(profile),
                            selected = state.selectedProfileId == profile.id,
                            enabled = !state.running && !state.busy,
                        ),
                        actions = McuFlashSelectionCardActions(
                            onSelect = { viewModel.selectProfile(profile.id) },
                        ),
                    )
                }
            }
        }

        CupertinoTextField(
            label = "固件路径",
            value = state.firmwarePath,
            onValueChange = viewModel::updateFirmwarePath,
            modifier = Modifier.fillMaxWidth(),
            placeholder = state.selectedProfile?.artifactHint ?: "例如 /tmp/firmware.bin",
            description = "当前直接填写本机固件文件路径，后端会按 ST-Link 流程读取并烧录。",
            singleLine = true,
        )

        CupertinoTextField(
            label = "起始地址",
            value = state.startAddressInput,
            onValueChange = viewModel::updateStartAddressInput,
            modifier = Modifier.fillMaxWidth(),
            placeholder = state.selectedProfile?.defaultStartAddress?.toHexAddress() ?: "0x08000000",
            description = "留空时使用当前配置默认地址，支持 `0x08000000` 或十进制。",
            singleLine = true,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            with(configActionsSpi) {
                Render(
                    state = state,
                    viewModel = viewModel,
                )
            }
        }
    }
}

/**
 * 处理mcu烧录探针panel。
 *
 * @param state 状态。
 * @param viewModel 页面视图模型。
 */
@Composable
private fun McuFlashProbePanel(
    state: McuFlashScreenState,
    viewModel: McuFlashViewModel,
) {
    val selectionCardSpi = koinInject<McuFlashSelectionCardSpi>()
    CupertinoPanel(
        title = "ST-Link 探针",
        subtitle = "手动指定序列号时，后端会把该序列号传给 `StLinkConfig`。",
    ) {
        selectionCardSpi.Render(
            state = McuFlashSelectionCardState(
                title = "自动选择首个可用探针",
                caption = "适合只连接一个 ST-Link 的场景。",
                selected = state.useAutoProbeSelection,
                enabled = !state.running && !state.busy,
            ),
            actions = McuFlashSelectionCardActions(
                onSelect = viewModel::selectAutoProbe,
            ),
        )
        if (state.probes.isEmpty()) {
            McuFlashInlineNotice("未发现可选探针时仍可先填写固件路径，待设备接入后再刷新。")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.probes.forEach { probe ->
                    val serialNumber = probe.serialNumber
                    selectionCardSpi.Render(
                        state = McuFlashSelectionCardState(
                            title = probe.displayName(),
                            caption = buildProbeCaption(probe),
                            selected = !state.useAutoProbeSelection && serialNumber == state.selectedProbeSerialNumber,
                            enabled = serialNumber != null && !state.running && !state.busy,
                        ),
                        actions = McuFlashSelectionCardActions(
                            onSelect = {
                                if (serialNumber != null) {
                                    viewModel.selectProbe(serialNumber)
                                }
                            },
                        ),
                    )
                }
            }
        }
    }
}

/**
 * 处理mcu烧录状态panel。
 *
 * @param state 状态。
 */
@Composable
private fun McuFlashStatusPanel(
    state: McuFlashScreenState,
) {
    val progress = (state.status.progressPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
    CupertinoPanel(
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
        val statusRows = listOf(
            "当前阶段" to state.status.currentStage.orDash(),
            "发送字节" to "${state.status.bytesSent.toSizeLabel()} / ${state.status.totalBytes.toSizeLabel()}",
            "固件路径" to state.status.firmwarePath.orDash(),
            "烧录地址" to state.status.flashStartAddress?.toHexAddress().orDash(),
            "校验结果" to state.status.verified.toYesNo(),
            "是否启动应用" to state.status.startedApplication.toYesNo(),
            "开始时间" to state.status.startedAt.orDash(),
            "结束时间" to state.status.finishedAt.orDash(),
            "最近更新" to state.status.updatedAt.orDash(),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            statusRows.forEach { (label, value) ->
                CupertinoKeyValueRow(label, value)
            }
        }
    }
}

/**
 * 处理mcu烧录runtime摘要panel。
 *
 * @param state 状态。
 */
@Composable
private fun McuFlashRuntimeSummaryPanel(
    state: McuFlashScreenState,
) {
    val profile = state.selectedProfile
    val summaryRows = listOf(
        "当前配置" to profile?.title.orDash(),
        "传输方式" to profile?.transport.orDash(),
        "默认起始地址" to profile?.defaultStartAddress?.toHexAddress().orDash(),
        "支持 chipId" to profile?.supportedChipIds.toChipIdLabel(),
        "固件提示" to profile?.artifactHint.orDash(),
        "目标 chipId" to state.status.targetChipId?.toChipIdLabel().orDash(),
        "目标电压" to state.status.targetVoltageMillivolts?.toVoltageLabel().orDash(),
        "运行中探针" to state.status.probeSerialNumber?.let { serial ->
            "${state.status.probeDescription.orDash()} / $serial"
        }.orDash(),
        "预选探针" to if (state.useAutoProbeSelection) {
            "自动选择"
        } else {
            state.selectedProbe?.let { probe ->
                "${probe.displayName()} / ${probe.serialNumber.orDash()}"
            }.orDash()
        },
    )
    CupertinoPanel(
        title = "运行摘要",
        subtitle = "便于确认当前目标、电压和探针落点。",
    ) {
        summaryRows.forEach { (label, value) ->
            CupertinoKeyValueRow(label, value)
        }
    }
}

/**
 * 处理mcu烧录banner。
 *
 * @param text 文本。
 * @param tone tone。
 */
@Composable
private fun McuFlashBanner(
    text: String,
    tone: Color,
) {
    CupertinoStatusStrip(
        text = text,
        tone = tone,
    )
}

/**
 * 处理mcu烧录inline提示。
 *
 * @param text 文本。
 */
@Composable
private fun McuFlashInlineNotice(
    text: String,
) {
    CupertinoStatusStrip(
        text = text,
        tone = MaterialTheme.colorScheme.surfaceVariant,
    )
}

/**
 * 处理mcu烧录状态badge。
 *
 * @param state 状态。
 */
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

/**
 * 处理mcu烧录进度bar。
 *
 * @param progress 当前进度。
 */
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

/**
 * 处理mcu烧录运行状态。
 */
private fun McuFlashRunState.label(): String {
    return when (this) {
        McuFlashRunState.IDLE -> "空闲"
        McuFlashRunState.RUNNING -> "运行中"
        McuFlashRunState.COMPLETED -> "已完成"
        McuFlashRunState.ERROR -> "失败"
    }
}

/**
 * 处理mcu烧录运行状态。
 */
@Composable
private fun McuFlashRunState.tint(): Color {
    return when (this) {
        McuFlashRunState.IDLE -> MaterialTheme.colorScheme.primary
        McuFlashRunState.RUNNING -> MaterialTheme.colorScheme.tertiary
        McuFlashRunState.COMPLETED -> Color(0xFF1B8A5A)
        McuFlashRunState.ERROR -> MaterialTheme.colorScheme.error
    }
}

/**
 * 构建配置档caption。
 *
 * @param profile 配置档。
 */
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

/**
 * 构建探针caption。
 *
 * @param probe 探针。
 */
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

/**
 * 处理mcu烧录探针摘要。
 */
private fun McuFlashProbeSummary.displayName(): String {
    return productName
        ?: manufacturerName
        ?: "ST-Link 探针"
}

/**
 * 处理列表。
 */
private fun List<Int>?.toChipIdLabel(): String {
    if (this == null || isEmpty()) {
        return "未限制"
    }
    return joinToString(", ") { chipId ->
        chipId.toChipIdLabel()
    }
}

/**
 * 处理int。
 */
private fun Int.toChipIdLabel(): String {
    return "0x${toString(16).uppercase()}"
}

/**
 * 处理int。
 */
private fun Int.toHexShort(): String {
    return toString(16).uppercase().padStart(4, '0')
}

/**
 * 处理long。
 */
private fun Long.toHexAddress(): String {
    return "0x${toString(16).uppercase().padStart(8, '0')}"
}

/**
 * 处理long。
 */
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

/**
 * 处理double。
 */
private fun Double.toCompactNumber(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/**
 * 处理int。
 */
private fun Int.toVoltageLabel(): String {
    val volts = this / 1000
    val millivolts = (this % 1000).toString().padStart(3, '0')
    return "$volts.$millivolts V"
}

/**
 * 处理boolean。
 */
private fun Boolean.toYesNo(): String {
    return if (this) {
        "是"
    } else {
        "否"
    }
}

/**
 * 处理string。
 */
private fun String?.orDash(): String {
    return this?.takeIf { it.isNotBlank() } ?: "-"
}
