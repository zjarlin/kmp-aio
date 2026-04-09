package site.addzero.kcloud.plugins.hostconfig.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val steelTop: Color,
    val steelBottom: Color,
)

private data class ModuleBoardFrontSpec(
    val busLabel: String,
    val terminalLegend: String,
    val interfaceMarks: List<String>,
    val statusLights: List<StatusLightSpec>,
    val fuseWindows: List<FuseWindowSpec>,
    val calibrationMarks: List<String>,
    val rangeSwitches: List<RangeSwitchSpec>,
    val terminalRows: List<List<TerminalGroup>>,
    val chipLabels: List<String>,
    val screwMarks: List<String>,
    val railClipLabel: String,
    val nameplateTitle: String,
    val nameplateSerial: String,
    val ventSlotCount: Int,
)

private data class TerminalGroup(
    val label: String,
    val detail: String,
    val indicatorCount: Int,
    val terminals: List<String>,
    val channelNumbers: List<String>,
)

private data class RangeSwitchSpec(
    val code: String,
    val mode: String,
    val active: Boolean,
)

private data class StatusLightSpec(
    val label: String,
    val active: Boolean,
    val tone: StatusLightTone,
)

private data class FuseWindowSpec(
    val label: String,
    val healthy: Boolean,
)

private enum class StatusLightTone {
    Accent,
    Warning,
    Danger,
    Neutral,
}

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

@Composable
internal fun HostConfigModuleBoard(
    model: ModuleBoardModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val palette = model.family.palette()
    val spec = model.toFrontSpec(compact)
    val outerShape = RoundedCornerShape(if (compact) 18.dp else 22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.faceTop, palette.faceBottom),
                ),
            )
            .border(
                width = 1.dp,
                color = palette.stroke.copy(alpha = 0.34f),
                shape = outerShape,
            )
            .padding(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
    ) {
        ModuleBoardSummaryRow(
            model = model,
            spec = spec,
            palette = palette,
            compact = compact,
        )
        ModuleBoardHardwareStrip(
            spec = spec,
            palette = palette,
            compact = compact,
        )
        if (
            spec.statusLights.isNotEmpty() ||
            spec.fuseWindows.isNotEmpty() ||
            spec.calibrationMarks.isNotEmpty()
        ) {
            ModuleBoardServiceStrip(
                spec = spec,
                palette = palette,
                compact = compact,
            )
        }
        if (spec.rangeSwitches.isNotEmpty()) {
            ModuleBoardRangeSwitchStrip(
                switches = spec.rangeSwitches,
                palette = palette,
                compact = compact,
            )
        }
        spec.terminalRows.forEach { row ->
            ModuleBoardTerminalRow(
                groups = row,
                palette = palette,
                compact = compact,
            )
        }
        ModuleBoardChipRow(
            model = model,
            labels = spec.chipLabels,
            palette = palette,
            compact = compact,
        )
        if (spec.ventSlotCount > 0) {
            ModuleBoardVentSlotRow(
                slotCount = spec.ventSlotCount,
                palette = palette,
                compact = compact,
            )
        }
        ModuleBoardScrewTerminalRow(
            marks = spec.screwMarks,
            palette = palette,
            compact = compact,
        )
    }
}

@Composable
private fun ModuleBoardSummaryRow(
    model: ModuleBoardModel,
    spec: ModuleBoardFrontSpec,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ModuleBoardAccentRail(
            model = model,
            spec = spec,
            palette = palette,
            compact = compact,
            modifier = Modifier.width(if (compact) 112.dp else 138.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(if (compact) 16.dp else 18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            palette.shellTop.copy(alpha = 0.48f),
                            palette.shellBottom.copy(alpha = 0.32f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
                )
                .padding(if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        ) {
            ModuleBoardHeader(
                model = model,
                palette = palette,
                compact = compact,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                ModuleBoardMetricBadge(
                    title = "系列",
                    value = model.family.code,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                ModuleBoardMetricBadge(
                    title = "通道",
                    value = model.channelCount.toString(),
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                ModuleBoardMetricBadge(
                    title = "设备",
                    value = model.deviceCount.toString(),
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModuleBoardInfoTag(
                    text = spec.busLabel,
                    palette = palette,
                )
                ModuleBoardInfoTag(
                    text = spec.terminalLegend,
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun ModuleBoardAccentRail(
    model: ModuleBoardModel,
    spec: ModuleBoardFrontSpec,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 16.dp else 18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.accentTop.copy(alpha = 0.94f),
                        palette.accentBottom.copy(alpha = 0.92f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
            )
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
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
                text = spec.busLabel,
                style = CupertinoTheme.typography.caption2,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
        ModuleBoardRailNameplate(
            title = spec.nameplateTitle,
            serial = spec.nameplateSerial,
            palette = palette,
            compact = compact,
        )
    }
}

@Composable
private fun ModuleBoardMetricBadge(
    title: String,
    value: String,
    palette: ModuleBoardPalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = palette.stroke.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.52f),
        )
        CupertinoText(
            text = value,
            style = CupertinoTheme.typography.footnote,
            color = Color.White.copy(alpha = 0.92f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
private fun ModuleBoardHardwareStrip(
    spec: ModuleBoardFrontSpec,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModuleBoardInfoTag(
            text = spec.terminalLegend,
            palette = palette,
        )
        spec.interfaceMarks.forEach { mark ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                palette.steelTop.copy(alpha = 0.94f),
                                palette.steelBottom.copy(alpha = 0.90f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                CupertinoText(
                    text = mark,
                    style = CupertinoTheme.typography.caption2,
                    color = Color.White.copy(alpha = 0.84f),
                )
            }
        }
    }
}

@Composable
private fun ModuleBoardServiceStrip(
    spec: ModuleBoardFrontSpec,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    val segmentCount = listOf(
        spec.statusLights.isNotEmpty(),
        spec.fuseWindows.isNotEmpty(),
        spec.calibrationMarks.isNotEmpty(),
    ).count { it }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (spec.statusLights.isNotEmpty()) {
            ModuleBoardLampBus(
                lights = spec.statusLights,
                palette = palette,
                compact = compact,
                modifier = if (segmentCount > 1) {
                    Modifier.weight(1f)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
        }
        if (spec.fuseWindows.isNotEmpty()) {
            ModuleBoardFuseStrip(
                windows = spec.fuseWindows,
                palette = palette,
                compact = compact,
                modifier = if (segmentCount > 1) {
                    Modifier.weight(1f)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
        }
        if (spec.calibrationMarks.isNotEmpty()) {
            ModuleBoardCalibrationStrip(
                marks = spec.calibrationMarks,
                palette = palette,
                compact = compact,
                modifier = if (segmentCount > 1) {
                    Modifier.weight(1f)
                } else {
                    Modifier.fillMaxWidth()
                },
            )
        }
    }
}

@Composable
private fun ModuleBoardLampBus(
    lights: List<StatusLightSpec>,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    ModuleBoardServicePanel(
        title = "状态总线",
        palette = palette,
        compact = compact,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            lights.forEach { light ->
                val glow = light.tone.resolveActiveColor(palette)
                val off = light.tone.resolveInactiveColor(palette)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 11.dp else 13.dp)
                            .clip(CircleShape)
                            .background(if (light.active) glow else off)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.14f),
                                shape = CircleShape,
                            ),
                    )
                    CupertinoText(
                        text = light.label,
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.74f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBoardFuseStrip(
    windows: List<FuseWindowSpec>,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    ModuleBoardServicePanel(
        title = "保险窗口",
        palette = palette,
        compact = compact,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            windows.forEach { window ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 16.dp else 18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (window.healthy) {
                                        listOf(
                                            Color(0xFFFFD781).copy(alpha = 0.94f),
                                            Color(0xFFC67A18).copy(alpha = 0.92f),
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFFF9A8E).copy(alpha = 0.88f),
                                            Color(0xFF7A2F2A).copy(alpha = 0.92f),
                                        )
                                    },
                                ),
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )
                    CupertinoText(
                        text = window.label,
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBoardCalibrationStrip(
    marks: List<String>,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    ModuleBoardServicePanel(
        title = "校准区",
        palette = palette,
        compact = compact,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            marks.forEach { mark ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 18.dp else 20.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.22f))
                            .border(
                                width = 1.dp,
                                color = palette.steelTop.copy(alpha = 0.72f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (compact) 6.dp else 7.dp)
                                .clip(CircleShape)
                                .background(palette.steelBottom.copy(alpha = 0.96f)),
                        )
                    }
                    CupertinoText(
                        text = mark,
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.74f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleBoardServicePanel(
    title: String,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        palette.chip.copy(alpha = 0.94f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = if (compact) 7.dp else 8.dp, vertical = if (compact) 6.dp else 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.50f),
        )
        content()
    }
}

@Composable
private fun ModuleBoardRangeSwitchStrip(
    switches: List<RangeSwitchSpec>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModuleBoardInfoTag(
            text = "量程拨码",
            palette = palette,
        )
        switches.forEach { switch ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                palette.chip.copy(alpha = 0.96f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = if (switch.active) {
                            palette.accentTop.copy(alpha = 0.36f)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CupertinoText(
                        text = switch.code,
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.48f),
                    )
                    Box(
                        modifier = Modifier
                            .width(if (compact) 20.dp else 24.dp)
                            .height(if (compact) 10.dp else 12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.30f))
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(if (switch.active) Alignment.CenterEnd else Alignment.CenterStart)
                                .size(if (compact) 6.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (switch.active) {
                                        palette.accentTop
                                    } else {
                                        Color.White.copy(alpha = 0.28f)
                                    },
                                ),
                        )
                    }
                }
                CupertinoText(
                    text = switch.mode,
                    style = CupertinoTheme.typography.caption1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.86f),
                )
            }
        }
    }
}

@Composable
private fun ModuleBoardTerminalRow(
    groups: List<TerminalGroup>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
    ) {
        groups.forEach { group ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(if (compact) 14.dp else 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.06f),
                                palette.chip.copy(alpha = 0.92f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(if (compact) 14.dp else 16.dp),
                    )
                    .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = if (compact) 8.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
            ) {
                CupertinoText(
                    text = group.label,
                    style = CupertinoTheme.typography.caption1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.92f),
                )
                CupertinoText(
                    text = group.detail,
                    style = CupertinoTheme.typography.caption2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.56f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(group.indicatorCount) { index ->
                        val alpha = 0.46f + (index * 0.12f).coerceAtMost(0.40f)
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
                        text = "LED",
                        style = CupertinoTheme.typography.caption2,
                        color = Color.White.copy(alpha = 0.40f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    group.terminals.forEach { terminal ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = if (compact) 20.dp else 24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            palette.steelTop,
                                            palette.steelBottom,
                                        ),
                                    ),
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CupertinoText(
                                text = terminal,
                                style = CupertinoTheme.typography.caption2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = 0.84f),
                            )
                        }
                    }
                }
                ModuleBoardWireNumberRow(
                    numbers = group.channelNumbers,
                    palette = palette,
                    compact = compact,
                )
            }
        }
    }
}

@Composable
private fun ModuleBoardWireNumberRow(
    numbers: List<String>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        numbers.forEach { number ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        color = palette.accentTop.copy(alpha = 0.12f),
                    )
                    .border(
                        width = 1.dp,
                        color = palette.accentTop.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 4.dp, vertical = if (compact) 3.dp else 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                CupertinoText(
                    text = number,
                    style = CupertinoTheme.typography.caption2,
                    color = Color.White.copy(alpha = 0.72f),
                )
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
                    .heightIn(min = if (compact) 28.dp else 32.dp)
                    .clip(RoundedCornerShape(if (compact) 10.dp else 12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
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
            modifier = Modifier.widthIn(min = if (compact) 64.dp else 80.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ModuleBoardInfoTag(
                text = model.family.caption,
                palette = palette,
            )
            ModuleBoardInfoTag(
                text = "${model.deviceCount} 台设备",
                palette = palette,
            )
        }
    }
}

@Composable
private fun ModuleBoardVentSlotRow(
    slotCount: Int,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CupertinoText(
            text = "VENT",
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.44f),
        )
        repeat(slotCount) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 4.dp else 5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.22f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun ModuleBoardScrewTerminalRow(
    marks: List<String>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marks.forEach { mark ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                palette.steelTop.copy(alpha = 0.88f),
                                palette.steelBottom.copy(alpha = 0.88f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 8.dp else 10.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.28f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                )
                CupertinoText(
                    text = mark,
                    style = CupertinoTheme.typography.caption2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }
    }
}

@Composable
private fun ModuleBoardRailNameplate(
    title: String,
    serial: String,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = if (compact) 6.dp else 7.dp, vertical = if (compact) 5.dp else 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.64f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CupertinoText(
            text = serial,
            style = CupertinoTheme.typography.caption2,
            color = Color.White.copy(alpha = 0.84f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
            color = Color.White.copy(alpha = 0.80f),
        )
    }
}

private fun ModuleBoardModel.toFrontSpec(
    compact: Boolean,
): ModuleBoardFrontSpec {
    return when (family) {
        ModuleBoardFamily.DI -> ModuleBoardFrontSpec(
            busLabel = "24V 采集母线",
            terminalLegend = "SINK INPUT",
            interfaceMarks = listOf("光耦隔离", "滤波扫描", "阈值整形"),
            statusLights = listOf(
                StatusLightSpec("PWR", true, StatusLightTone.Accent),
                StatusLightSpec("RUN", deviceCount > 0, StatusLightTone.Accent),
                StatusLightSpec("FLT", false, StatusLightTone.Warning),
                StatusLightSpec("COM", true, StatusLightTone.Neutral),
            ),
            fuseWindows = emptyList(),
            calibrationMarks = emptyList(),
            rangeSwitches = emptyList(),
            terminalRows = splitGroupsIntoRows(
                groups = buildTerminalGroups(
                    channelCount = channelCount,
                    desiredGroupCount = if (compact) 4 else 8,
                    prefix = "X",
                    detail = "DI 输入",
                    terminals = listOf("SIG", "COM", "24V"),
                ),
                rowCount = 2,
            ),
            chipLabels = listOf("DI 扫描", "门限比较", "抗抖锁存"),
            screwMarks = listOf("24V", "0V", "A+", "B-"),
            railClipLabel = "DIN TS35",
            nameplateTitle = "IEC 61131-2",
            nameplateSerial = buildNameplateSerial(),
            ventSlotCount = if (compact) 0 else 6,
        )

        ModuleBoardFamily.DO -> ModuleBoardFrontSpec(
            busLabel = "24V 驱动母线",
            terminalLegend = "SOURCE OUTPUT",
            interfaceMarks = listOf("驱动阵列", "短路保护", "状态回读"),
            statusLights = listOf(
                StatusLightSpec("PWR", true, StatusLightTone.Accent),
                StatusLightSpec("RUN", deviceCount > 0, StatusLightTone.Accent),
                StatusLightSpec("OVR", false, StatusLightTone.Warning),
                StatusLightSpec("ALM", false, StatusLightTone.Danger),
            ),
            fuseWindows = listOf(
                FuseWindowSpec("F1", true),
                FuseWindowSpec("F2", true),
                FuseWindowSpec("F3", true),
                FuseWindowSpec("F4", true),
            ),
            calibrationMarks = emptyList(),
            rangeSwitches = emptyList(),
            terminalRows = splitGroupsIntoRows(
                groups = buildTerminalGroups(
                    channelCount = channelCount,
                    desiredGroupCount = if (compact) 4 else 8,
                    prefix = "Y",
                    detail = "DO 输出",
                    terminals = listOf("OUT", "COM", "LOAD"),
                ),
                rowCount = 2,
            ),
            chipLabels = listOf("功率驱动", "保险监测", "互锁保护"),
            screwMarks = listOf("24V", "0V", "SAFE", "FB"),
            railClipLabel = "DIN TS35",
            nameplateTitle = "IEC 61131-2",
            nameplateSerial = buildNameplateSerial(),
            ventSlotCount = if (compact) 0 else 7,
        )

        ModuleBoardFamily.AI -> ModuleBoardFrontSpec(
            busLabel = "多量程采样总线",
            terminalLegend = "ANALOG INPUT",
            interfaceMarks = listOf("ADC", "量程切换", "数字滤波"),
            statusLights = listOf(
                StatusLightSpec("PWR", true, StatusLightTone.Accent),
                StatusLightSpec("RUN", deviceCount > 0, StatusLightTone.Accent),
                StatusLightSpec("CAL", true, StatusLightTone.Warning),
                StatusLightSpec("ALM", false, StatusLightTone.Danger),
            ),
            fuseWindows = emptyList(),
            calibrationMarks = listOf("ZERO", "SPAN", "CJC"),
            rangeSwitches = listOf(
                RangeSwitchSpec("SW1", "0-10V", true),
                RangeSwitchSpec("SW2", "4-20mA", false),
                RangeSwitchSpec("SW3", "PT100", false),
            ),
            terminalRows = splitGroupsIntoRows(
                groups = buildTerminalGroups(
                    channelCount = channelCount,
                    desiredGroupCount = if (compact) 4 else 6,
                    prefix = "AI",
                    detail = "4-20mA / 0-10V",
                    terminals = listOf("I+", "V+", "COM"),
                ),
                rowCount = 2,
            ),
            chipLabels = listOf("ADC 采样", "校准基准", "隔离放大"),
            screwMarks = listOf("REF", "AG", "A+", "B-"),
            railClipLabel = "DIN TS35",
            nameplateTitle = "IEC 61326-1",
            nameplateSerial = buildNameplateSerial(),
            ventSlotCount = if (compact) 0 else 5,
        )

        ModuleBoardFamily.AO -> ModuleBoardFrontSpec(
            busLabel = "闭环输出总线",
            terminalLegend = "ANALOG OUTPUT",
            interfaceMarks = listOf("DAC", "反馈校准", "安全回路"),
            statusLights = listOf(
                StatusLightSpec("PWR", true, StatusLightTone.Accent),
                StatusLightSpec("RUN", deviceCount > 0, StatusLightTone.Accent),
                StatusLightSpec("TRIM", true, StatusLightTone.Warning),
                StatusLightSpec("ALM", false, StatusLightTone.Danger),
            ),
            fuseWindows = emptyList(),
            calibrationMarks = listOf("ZERO", "SPAN", "LOOP"),
            rangeSwitches = listOf(
                RangeSwitchSpec("SW1", "0-10V", true),
                RangeSwitchSpec("SW2", "4-20mA", false),
                RangeSwitchSpec("TRM", "ZERO", true),
            ),
            terminalRows = splitGroupsIntoRows(
                groups = buildTerminalGroups(
                    channelCount = channelCount,
                    desiredGroupCount = if (compact) 4 else 4,
                    prefix = "AO",
                    detail = "0-10V / 4-20mA",
                    terminals = listOf("OUT", "RET", "COM"),
                ),
                rowCount = 2,
            ),
            chipLabels = listOf("DAC 输出", "闭环补偿", "量程校准"),
            screwMarks = listOf("REF", "COM", "EN", "FB"),
            railClipLabel = "DIN TS35",
            nameplateTitle = "IEC 61326-1",
            nameplateSerial = buildNameplateSerial(),
            ventSlotCount = if (compact) 0 else 5,
        )

        ModuleBoardFamily.GENERIC -> ModuleBoardFrontSpec(
            busLabel = "现场扩展背板",
            terminalLegend = "GENERIC I/O",
            interfaceMarks = listOf("总线接口", "状态监测", "扩展供电"),
            statusLights = listOf(
                StatusLightSpec("PWR", true, StatusLightTone.Accent),
                StatusLightSpec("RUN", deviceCount > 0, StatusLightTone.Accent),
                StatusLightSpec("BUS", true, StatusLightTone.Neutral),
                StatusLightSpec("ALM", false, StatusLightTone.Danger),
            ),
            fuseWindows = emptyList(),
            calibrationMarks = emptyList(),
            rangeSwitches = emptyList(),
            terminalRows = splitGroupsIntoRows(
                groups = buildTerminalGroups(
                    channelCount = channelCount,
                    desiredGroupCount = if (compact) 4 else 6,
                    prefix = "IO",
                    detail = "可编排通道",
                    terminals = listOf("SIG", "COM", "AUX"),
                ),
                rowCount = 2,
            ),
            chipLabels = listOf("通信控制", "状态采样", "扩展背板"),
            screwMarks = listOf("24V", "0V", "A", "B"),
            railClipLabel = "DIN TS35",
            nameplateTitle = "REMOTE I/O",
            nameplateSerial = buildNameplateSerial(),
            ventSlotCount = if (compact) 0 else 6,
        )
    }
}

private fun buildTerminalGroups(
    channelCount: Int,
    desiredGroupCount: Int,
    prefix: String,
    detail: String,
    terminals: List<String>,
): List<TerminalGroup> {
    val blockSize = ceilingDiv(
        dividend = channelCount,
        divisor = desiredGroupCount.coerceAtLeast(1),
    ).coerceAtLeast(1)
    return (1..channelCount).chunked(blockSize).map { group ->
        val label = if (group.size == 1) {
            "$prefix${group.first().toTwoDigit()}"
        } else {
            "$prefix${group.first().toTwoDigit()}-${group.last().toTwoDigit()}"
        }
        TerminalGroup(
            label = label,
            detail = detail,
            indicatorCount = group.size.coerceAtMost(4),
            terminals = terminals,
            channelNumbers = group.map { number -> number.toTwoDigit() },
        )
    }
}

private fun splitGroupsIntoRows(
    groups: List<TerminalGroup>,
    rowCount: Int,
): List<List<TerminalGroup>> {
    val safeRowCount = rowCount.coerceAtLeast(1)
    val groupsPerRow = ceilingDiv(
        dividend = groups.size,
        divisor = safeRowCount,
    ).coerceAtLeast(1)
    return groups.chunked(groupsPerRow)
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
            steelTop = Color(0xFF60716D),
            steelBottom = Color(0xFF354340),
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
            steelTop = Color(0xFF7D6852),
            steelBottom = Color(0xFF4B3A2B),
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
            steelTop = Color(0xFF60798D),
            steelBottom = Color(0xFF34485A),
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
            steelTop = Color(0xFF607B78),
            steelBottom = Color(0xFF344A47),
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
            steelTop = Color(0xFF74818B),
            steelBottom = Color(0xFF46515A),
        )
    }
}

private fun StatusLightTone.resolveActiveColor(
    palette: ModuleBoardPalette,
): Color {
    return when (this) {
        StatusLightTone.Accent -> palette.lightOn
        StatusLightTone.Warning -> Color(0xFFFFC965)
        StatusLightTone.Danger -> Color(0xFFFF7E74)
        StatusLightTone.Neutral -> Color(0xFFDDE7EF)
    }
}

private fun StatusLightTone.resolveInactiveColor(
    palette: ModuleBoardPalette,
): Color {
    return when (this) {
        StatusLightTone.Accent -> palette.lightOff
        StatusLightTone.Warning -> Color(0xFF6F5632)
        StatusLightTone.Danger -> Color(0xFF5C2A28)
        StatusLightTone.Neutral -> Color.White.copy(alpha = 0.14f)
    }
}

private fun ModuleBoardModel.buildNameplateSerial(): String {
    val normalizedCode = templateCode
        .uppercase()
        .substringAfter("RIO_", templateCode.uppercase())
        .replace('_', '-')
        .take(12)
        .ifBlank { family.code }
    return "SN $normalizedCode-${channelCount.toTwoDigit()}${deviceCount.toTwoDigit()}"
}

private fun ceilingDiv(
    dividend: Int,
    divisor: Int,
): Int {
    if (dividend <= 0) {
        return 0
    }
    return (dividend + divisor - 1) / divisor
}

private fun Int.toTwoDigit(): String {
    return toString().padStart(2, '0')
}
