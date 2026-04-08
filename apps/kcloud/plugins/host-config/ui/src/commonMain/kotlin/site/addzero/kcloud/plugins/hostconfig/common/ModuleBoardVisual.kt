package site.addzero.kcloud.plugins.hostconfig.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse

internal data class ModuleBoardModel(
    val moduleName: String,
    val templateCode: String,
    val templateName: String,
    val family: ModuleBoardFamily,
    val channelCount: Int,
    val deviceCount: Int,
    val portName: String?,
    val baudRate: Int?,
    val responseTimeoutMs: Int?,
)

internal enum class ModuleBoardFamily(
    val code: String,
    val title: String,
    val caption: String,
    val defaultChannelCount: Int,
) {
    DI(
        code = "DI",
        title = "数字输入",
        caption = "干接点采集",
        defaultChannelCount = 24,
    ),
    DO(
        code = "DO",
        title = "数字输出",
        caption = "现场驱动输出",
        defaultChannelCount = 24,
    ),
    AI(
        code = "AI",
        title = "模拟输入",
        caption = "电压电流采样",
        defaultChannelCount = 12,
    ),
    AO(
        code = "AO",
        title = "模拟输出",
        caption = "闭环调节输出",
        defaultChannelCount = 8,
    ),
    GENERIC(
        code = "I/O",
        title = "通用模块",
        caption = "现场扩展单元",
        defaultChannelCount = 8,
    ),
}

private data class ModuleBoardPalette(
    val shellTop: Color,
    val shellBottom: Color,
    val faceTop: Color,
    val faceBottom: Color,
    val accentTop: Color,
    val accentBottom: Color,
    val lightOn: Color,
    val lightOff: Color,
    val stroke: Color,
    val chip: Color,
)

private data class ChannelGroup(
    val label: String,
    val indicatorCount: Int,
)

internal fun resolveModuleBoardModel(
    module: ModuleTreeNode,
    moduleTemplates: List<ModuleTemplateOptionResponse>,
): ModuleBoardModel {
    val template = moduleTemplates.firstOrNull { item ->
        item.id == module.moduleTemplateId || item.code == module.moduleTemplateCode
    }
    val family = resolveModuleBoardFamily(module.moduleTemplateCode)
    return ModuleBoardModel(
        moduleName = module.name,
        templateCode = module.moduleTemplateCode,
        templateName = module.moduleTemplateName,
        family = family,
        channelCount = (template?.channelCount ?: family.defaultChannelCount).coerceAtLeast(1),
        deviceCount = module.devices.size,
        portName = module.portName,
        baudRate = module.baudRate,
        responseTimeoutMs = module.responseTimeoutMs,
    )
}

internal fun resolveModuleBoardFamily(
    templateCode: String?,
): ModuleBoardFamily {
    val normalized = templateCode?.uppercase().orEmpty()
    return when {
        "_DI_" in normalized || normalized.endsWith("_DI") || normalized.startsWith("DI_") -> ModuleBoardFamily.DI
        "_DO_" in normalized || normalized.endsWith("_DO") || normalized.startsWith("DO_") -> ModuleBoardFamily.DO
        "_AI_" in normalized || normalized.endsWith("_AI") || normalized.startsWith("AI_") -> ModuleBoardFamily.AI
        "_AO_" in normalized || normalized.endsWith("_AO") || normalized.startsWith("AO_") -> ModuleBoardFamily.AO
        else -> ModuleBoardFamily.GENERIC
    }
}

@Suppress("LongMethod")
@Composable
internal fun HostConfigModuleBoard(
    model: ModuleBoardModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val palette = model.family.palette()
    val outerShape = RoundedCornerShape(if (compact) 20.dp else 26.dp)
    val faceShape = RoundedCornerShape(if (compact) 18.dp else 22.dp)
    val groupSize = when {
        compact && model.channelCount >= 24 -> 4
        compact && model.channelCount > 8 -> 2
        else -> 1
    }
    val channels = buildChannelGroups(
        channelCount = model.channelCount,
        groupSize = groupSize,
    )
    val splitIndex = (channels.size + 1) / 2
    val topRow = channels.take(splitIndex)
    val bottomRow = channels.drop(splitIndex)
    val chipLabels = model.family.chipLabels()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 188.dp else 260.dp)
            .shadow(
                elevation = if (compact) 10.dp else 16.dp,
                shape = outerShape,
                clip = false,
            )
            .graphicsLayer {
                rotationX = if (compact) 3f else 7f
                rotationY = if (compact) -2f else -5f
            }
            .clip(outerShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.shellTop, palette.shellBottom),
                ),
            )
            .border(
                width = 1.dp,
                color = palette.stroke.copy(alpha = 0.65f),
                shape = outerShape,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 10.dp else 12.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.Transparent,
                                palette.accentTop.copy(alpha = 0.28f),
                            ),
                        ),
                    ),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(if (compact) 10.dp else 14.dp),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
            ) {
                ModuleBoardAccentRail(
                    model = model,
                    palette = palette,
                    compact = compact,
                    modifier = Modifier
                        .width(if (compact) 58.dp else 76.dp)
                        .fillMaxHeight(),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(faceShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(palette.faceTop, palette.faceBottom),
                            ),
                        )
                        .border(
                            width = 1.dp,
                            color = palette.stroke.copy(alpha = 0.72f),
                            shape = faceShape,
                        )
                        .padding(if (compact) 10.dp else 14.dp),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
                ) {
                    ModuleBoardHeader(
                        model = model,
                        palette = palette,
                        compact = compact,
                    )
                    ModuleBoardTerminalRail(
                        palette = palette,
                        segmentCount = topRow.size.coerceAtLeast(4),
                        compact = compact,
                    )
                    ModuleBoardChannelRow(
                        groups = topRow,
                        palette = palette,
                        compact = compact,
                    )
                    if (bottomRow.isNotEmpty()) {
                        ModuleBoardChannelRow(
                            groups = bottomRow,
                            palette = palette,
                            compact = compact,
                        )
                    }
                    ModuleBoardChipRow(
                        model = model,
                        labels = chipLabels,
                        palette = palette,
                        compact = compact,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 10.dp else 14.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                palette.shellBottom.copy(alpha = 0.9f),
                                Color.Black.copy(alpha = 0.22f),
                                palette.accentBottom.copy(alpha = 0.34f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun ModuleBoardAccentRail(
    model: ModuleBoardModel,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 18.dp else 22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.accentTop, palette.accentBottom),
                ),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
            )
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CupertinoText(
                text = model.family.code,
                style = if (compact) {
                    CupertinoTheme.typography.title3
                } else {
                    CupertinoTheme.typography.title2
                },
                color = Color.White,
            )
            CupertinoText(
                text = model.family.title,
                style = CupertinoTheme.typography.footnote,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            CupertinoText(
                text = model.family.caption,
                style = CupertinoTheme.typography.caption2,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ModuleBoardSmallMeta(
                title = "CH",
                value = model.channelCount.toString(),
            )
            ModuleBoardSmallMeta(
                title = "DEV",
                value = model.deviceCount.toString(),
            )
        }
    }
}

@Composable
private fun ModuleBoardHeader(
    model: ModuleBoardModel,
    palette: ModuleBoardPalette,
    compact: Boolean,
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
            CupertinoText(
                text = model.moduleName,
                style = if (compact) {
                    CupertinoTheme.typography.headline
                } else {
                    CupertinoTheme.typography.title2
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.96f),
            )
            CupertinoText(
                text = "${model.templateName} · ${model.templateCode}",
                style = CupertinoTheme.typography.footnote,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.72f),
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(
                    color = palette.accentTop.copy(alpha = 0.18f),
                )
                .border(
                    width = 1.dp,
                    color = palette.accentTop.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(999.dp),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            CupertinoText(
                text = "${model.channelCount} 通道",
                style = CupertinoTheme.typography.footnote,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun ModuleBoardTerminalRail(
    palette: ModuleBoardPalette,
    segmentCount: Int,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        repeat(segmentCount) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 12.dp else 16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.24f),
                                palette.shellBottom.copy(alpha = 0.92f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
        }
    }
}

@Composable
private fun ModuleBoardChannelRow(
    groups: List<ChannelGroup>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
    ) {
        groups.forEach { group ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(if (compact) 12.dp else 14.dp))
                    .background(
                        color = palette.chip.copy(alpha = 0.78f),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(if (compact) 12.dp else 14.dp),
                    )
                    .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 7.dp else 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CupertinoText(
                    text = group.label,
                    style = CupertinoTheme.typography.caption1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.88f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(group.indicatorCount) { index ->
                        val alpha = 0.48f + (index * 0.12f).coerceAtMost(0.38f)
                        Box(
                            modifier = Modifier
                                .size(if (compact) 6.dp else 8.dp)
                                .clip(CircleShape)
                                .background(palette.lightOn.copy(alpha = alpha))
                                .border(
                                    width = 1.dp,
                                    color = palette.lightOff,
                                    shape = CircleShape,
                                ),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    CupertinoText(
                        text = "CH",
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.45f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBoardChipRow(
    model: ModuleBoardModel,
    labels: List<String>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = if (compact) 26.dp else 30.dp)
                    .clip(RoundedCornerShape(if (compact) 10.dp else 12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                palette.chip,
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = palette.stroke.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                CupertinoText(
                    text = label,
                    style = CupertinoTheme.typography.caption1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.74f),
                )
            }
        }

        Column(
            modifier = Modifier.widthIn(min = if (compact) 62.dp else 74.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            model.portName?.takeIf { it.isNotBlank() }?.let { portName ->
                ModuleBoardInfoTag(
                    text = portName,
                    palette = palette,
                )
            }
            model.baudRate?.let { baudRate ->
                ModuleBoardInfoTag(
                    text = "${baudRate}bps",
                    palette = palette,
                )
            }
            model.responseTimeoutMs?.let { timeout ->
                ModuleBoardInfoTag(
                    text = "${timeout}ms",
                    palette = palette,
                )
            }
            ModuleBoardInfoTag(
                text = "${model.deviceCount} 台设备",
                palette = palette,
            )
        }
    }
}

@Composable
private fun ModuleBoardSmallMeta(
    title: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.58f),
        )
        CupertinoText(
            text = value,
            style = CupertinoTheme.typography.headline,
            color = Color.White.copy(alpha = 0.96f),
        )
    }
}

@Composable
private fun ModuleBoardInfoTag(
    text: String,
    palette: ModuleBoardPalette,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color = palette.accentTop.copy(alpha = 0.16f))
            .border(
                width = 1.dp,
                color = palette.accentTop.copy(alpha = 0.28f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        CupertinoText(
            text = text,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}

private fun buildChannelGroups(
    channelCount: Int,
    groupSize: Int,
): List<ChannelGroup> {
    val safeGroupSize = groupSize.coerceAtLeast(1)
    return (1..channelCount).chunked(safeGroupSize).map { group ->
        val label = if (group.size == 1) {
            group.first().toTwoDigit()
        } else {
            "${group.first().toTwoDigit()}-${group.last().toTwoDigit()}"
        }
        ChannelGroup(
            label = label,
            indicatorCount = group.size.coerceAtMost(4),
        )
    }
}

private fun ModuleBoardFamily.palette(): ModuleBoardPalette {
    return when (this) {
        ModuleBoardFamily.DI -> ModuleBoardPalette(
            shellTop = Color(0xFF263D36),
            shellBottom = Color(0xFF182723),
            faceTop = Color(0xFF314B44),
            faceBottom = Color(0xFF1D2F2A),
            accentTop = Color(0xFF43C59E),
            accentBottom = Color(0xFF1B8D70),
            lightOn = Color(0xFF7DFFCC),
            lightOff = Color(0xFF30544A),
            stroke = Color(0xFF7AD5B3),
            chip = Color(0xFF13201D),
        )

        ModuleBoardFamily.DO -> ModuleBoardPalette(
            shellTop = Color(0xFF473524),
            shellBottom = Color(0xFF2A1D14),
            faceTop = Color(0xFF5D4330),
            faceBottom = Color(0xFF36261A),
            accentTop = Color(0xFFFFB84D),
            accentBottom = Color(0xFFD27D1A),
            lightOn = Color(0xFFFFD27D),
            lightOff = Color(0xFF684B2B),
            stroke = Color(0xFFF0BB75),
            chip = Color(0xFF20170F),
        )

        ModuleBoardFamily.AI -> ModuleBoardPalette(
            shellTop = Color(0xFF23394C),
            shellBottom = Color(0xFF142431),
            faceTop = Color(0xFF31526C),
            faceBottom = Color(0xFF1B3345),
            accentTop = Color(0xFF67C6FF),
            accentBottom = Color(0xFF2A8FCD),
            lightOn = Color(0xFFB8E7FF),
            lightOff = Color(0xFF35536A),
            stroke = Color(0xFF7FCCF7),
            chip = Color(0xFF111B24),
        )

        ModuleBoardFamily.AO -> ModuleBoardPalette(
            shellTop = Color(0xFF24413E),
            shellBottom = Color(0xFF152725),
            faceTop = Color(0xFF33605B),
            faceBottom = Color(0xFF1C3835),
            accentTop = Color(0xFF59D5C2),
            accentBottom = Color(0xFF2E9E8E),
            lightOn = Color(0xFFA8F4E8),
            lightOff = Color(0xFF385E59),
            stroke = Color(0xFF78D9CA),
            chip = Color(0xFF101A19),
        )

        ModuleBoardFamily.GENERIC -> ModuleBoardPalette(
            shellTop = Color(0xFF37424B),
            shellBottom = Color(0xFF202830),
            faceTop = Color(0xFF4A5A66),
            faceBottom = Color(0xFF27333C),
            accentTop = Color(0xFFB5C7D6),
            accentBottom = Color(0xFF718799),
            lightOn = Color(0xFFE3EEF7),
            lightOff = Color(0xFF52616D),
            stroke = Color(0xFFB0C0CD),
            chip = Color(0xFF14191E),
        )
    }
}

private fun ModuleBoardFamily.chipLabels(): List<String> {
    return when (this) {
        ModuleBoardFamily.DI -> listOf("光耦隔离", "通道扫描", "抗抖滤波")
        ModuleBoardFamily.DO -> listOf("输出驱动", "短路保护", "状态反馈")
        ModuleBoardFamily.AI -> listOf("ADC 采样", "量程校准", "数字滤波")
        ModuleBoardFamily.AO -> listOf("DAC 输出", "闭环调节", "量程校准")
        ModuleBoardFamily.GENERIC -> listOf("总线接口", "状态监测", "扩展背板")
    }
}

private fun Int.toTwoDigit(): String {
    return toString().padStart(2, '0')
}
