package site.addzero.kcloud.plugins.hostconfig.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLightsRegisters
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfoRegisters
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.FlashConfigRegisters

/**
 * 表示模块板卡模型。
 *
 * @property moduleName 模块名称。
 * @property templateCode 模板编码。
 * @property templateName 模板名称。
 * @property family family。
 * @property channelCount channelcount。
 * @property deviceCount 设备count。
 * @property runtime runtime。
 */
internal data class ModuleBoardModel(
    val moduleName: String,
    val templateCode: String,
    val templateName: String,
    val family: ModuleBoardFamily,
    val channelCount: Int,
    val deviceCount: Int,
    val runtime: ModuleBoardRuntimeSnapshot? = null,
)

/**
 * 表示模块板卡runtime快照。
 *
 * @property deviceInfo 设备info。
 * @property powerLights powerlights。
 * @property flashConfig 烧录配置。
 */
data class ModuleBoardRuntimeSnapshot(
    val deviceInfo: DeviceRuntimeInfoRegisters? = null,
    val powerLights: Device24PowerLightsRegisters? = null,
    val flashConfig: FlashConfigRegisters? = null,
) {
    val hasAnyData: Boolean
        get() = deviceInfo != null || powerLights != null || flashConfig != null
}

/**
 * 表示模块板卡family。
 *
 * @property code 编码。
 * @property title title。
 * @property caption caption。
 * @property defaultChannelCount 默认channelcount。
 */
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

/**
 * 表示模块板卡palette。
 *
 * @property accent accent。
 * @property accentSoft accentsoft。
 * @property surfaceTop surfacetop。
 * @property surfaceBottom surfacebottom。
 * @property border border。
 * @property chip chip。
 * @property textPrimary 文本primary。
 * @property textSecondary 文本secondary。
 * @property lightOn lighton。
 * @property lightOff lightoff。
 */
private data class ModuleBoardPalette(
    val accent: Color,
    val accentSoft: Color,
    val surfaceTop: Color,
    val surfaceBottom: Color,
    val border: Color,
    val chip: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val lightOn: Color,
    val lightOff: Color,
)

/**
 * 表示板卡infoentry。
 *
 * @property label label。
 * @property value 值。
 */
private data class BoardInfoEntry(
    val label: String,
    val value: String,
)

/**
 * 解析模块板卡模型。
 *
 * @param module 模块。
 * @param moduleTemplates 模块模板。
 * @param runtime runtime。
 */
internal fun resolveModuleBoardModel(
    module: ModuleTreeNode,
    moduleTemplates: List<ModuleTemplateOptionResponse>,
    runtime: ModuleBoardRuntimeSnapshot? = null,
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
        deviceCount = 1,
        runtime = runtime?.takeIf { it.hasAnyData },
    )
}

/**
 * 解析模块板卡family。
 *
 * @param templateCode 模板编码。
 */
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
/**
 * 处理主机配置模块板卡。
 *
 * @param model 模型。
 * @param modifier modifier。
 * @param compact compact。
 * @param loading 加载。
 * @param errorMessage 错误消息。
 */
internal fun HostConfigModuleBoard(
    model: ModuleBoardModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    loading: Boolean = false,
    errorMessage: String? = null,
) {
    val palette = model.family.palette()
    val shape = RoundedCornerShape(if (compact) 18.dp else 22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.surfaceTop,
                        palette.surfaceBottom,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = palette.border,
                shape = shape,
            )
            .padding(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
    ) {
        ModuleBoardHero(
            model = model,
            palette = palette,
            compact = compact,
        )

        if (loading) {
            CupertinoStatusStrip(
                text = "正在读取在线板卡数据…",
                tone = palette.accentSoft,
            )
        }

        errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            CupertinoStatusStrip(
                text = message,
                tone = Color(0xFFFFF4E5),
            )
        }

        model.runtime?.deviceInfo?.let { deviceInfo ->
            ModuleBoardInfoSection(
                title = "设备信息",
                entries = deviceInfo.toBoardInfoEntries(),
                palette = palette,
                compact = compact,
            )
        }

        model.runtime?.powerLights?.let { powerLights ->
            ModuleBoardLightsSection(
                title = "24 路电源灯",
                lights = powerLights.toPowerLightStates(),
                palette = palette,
                compact = compact,
            )
        }

        model.runtime?.flashConfig?.let { flashConfig ->
            ModuleBoardInfoSection(
                title = "Flash 配置",
                entries = flashConfig.toBoardInfoEntries(),
                palette = palette,
                compact = compact,
            )
        }
    }
}

@Composable
/**
 * 处理模块板卡hero。
 *
 * @param model 模型。
 * @param palette palette。
 * @param compact compact。
 */
private fun ModuleBoardHero(
    model: ModuleBoardModel,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .width(if (compact) 108.dp else 126.dp)
                .clip(RoundedCornerShape(if (compact) 14.dp else 18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            palette.accent,
                            palette.accent.copy(alpha = 0.76f),
                        ),
                    ),
                )
                .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 10.dp else 12.dp),
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
                color = Color.White.copy(alpha = 0.94f),
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
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(if (compact) 14.dp else 18.dp))
                .background(palette.chip)
                .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CupertinoText(
                    text = model.moduleName,
                    style = if (compact) {
                        CupertinoTheme.typography.headline
                    } else {
                        CupertinoTheme.typography.title2
                    },
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                CupertinoText(
                    text = buildString {
                        append(model.templateName.ifBlank { model.family.title })
                        if (model.templateCode.isNotBlank()) {
                            append(" · ")
                            append(model.templateCode)
                        }
                    },
                    style = CupertinoTheme.typography.footnote,
                    color = palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                ModuleBoardMetricChip(
                    title = "通道",
                    value = model.channelCount.toString(),
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                ModuleBoardMetricChip(
                    title = "归属",
                    value = model.deviceCount.toString(),
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
                model.runtime?.powerLights?.let { lights ->
                    ModuleBoardMetricChip(
                        title = "点亮",
                        value = lights.toPowerLightStates().count { item -> item.second }.toString(),
                        palette = palette,
                        modifier = Modifier.weight(1f),
                    )
                } ?: ModuleBoardMetricChip(
                    title = "系列",
                    value = model.family.code,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
/**
 * 处理模块板卡metric芯片。
 *
 * @param title title。
 * @param value 待解析的值。
 * @param palette palette。
 * @param modifier modifier。
 */
private fun ModuleBoardMetricChip(
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
                color = palette.border.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CupertinoText(
            text = title,
            style = CupertinoTheme.typography.caption2,
            color = palette.textSecondary,
        )
        CupertinoText(
            text = value,
            style = CupertinoTheme.typography.footnote,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
/**
 * 处理模块板卡infosection。
 *
 * @param title title。
 * @param entries entries。
 * @param palette palette。
 * @param compact compact。
 */
private fun ModuleBoardInfoSection(
    title: String,
    entries: List<BoardInfoEntry>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    if (entries.isEmpty()) {
        return
    }

    ModuleBoardSection(
        title = title,
        palette = palette,
        compact = compact,
    ) {
        entries.chunked(2).forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                rowEntries.forEach { entry ->
                    ModuleBoardInfoCell(
                        entry = entry,
                        palette = palette,
                        compact = compact,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowEntries.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
/**
 * 处理模块板卡infocell。
 *
 * @param entry entry。
 * @param palette palette。
 * @param compact compact。
 * @param modifier modifier。
 */
private fun ModuleBoardInfoCell(
    entry: BoardInfoEntry,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 7.dp else 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        CupertinoText(
            text = entry.label,
            style = CupertinoTheme.typography.caption2,
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CupertinoText(
            text = entry.value,
            style = CupertinoTheme.typography.footnote,
            color = palette.textPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
/**
 * 处理模块板卡lightssection。
 *
 * @param title title。
 * @param lights lights。
 * @param palette palette。
 * @param compact compact。
 */
private fun ModuleBoardLightsSection(
    title: String,
    lights: List<Pair<String, Boolean>>,
    palette: ModuleBoardPalette,
    compact: Boolean,
) {
    if (lights.isEmpty()) {
        return
    }

    ModuleBoardSection(
        title = title,
        palette = palette,
        compact = compact,
    ) {
        lights.chunked(6).forEach { rowLights ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                rowLights.forEach { (label, active) ->
                    ModuleBoardLightCell(
                        label = label,
                        active = active,
                        palette = palette,
                        compact = compact,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(6 - rowLights.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
/**
 * 处理模块板卡lightcell。
 *
 * @param label label。
 * @param active active。
 * @param palette palette。
 * @param compact compact。
 * @param modifier modifier。
 */
private fun ModuleBoardLightCell(
    label: String,
    active: Boolean,
    palette: ModuleBoardPalette,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(vertical = if (compact) 8.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 10.dp else 12.dp)
                .clip(CircleShape)
                .background(if (active) palette.lightOn else palette.lightOff)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = CircleShape,
                ),
        )
        CupertinoText(
            text = label,
            style = CupertinoTheme.typography.caption2,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CupertinoText(
            text = if (active) "ON" else "OFF",
            style = CupertinoTheme.typography.caption2,
            color = palette.textSecondary,
        )
    }
}

@Composable
/**
 * 处理模块板卡section。
 *
 * @param title title。
 * @param palette palette。
 * @param compact compact。
 * @param content content。
 */
private fun ModuleBoardSection(
    title: String,
    palette: ModuleBoardPalette,
    compact: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 14.dp else 16.dp))
            .background(palette.chip)
            .border(
                width = 1.dp,
                color = palette.border.copy(alpha = 0.7f),
                shape = RoundedCornerShape(if (compact) 14.dp else 16.dp),
            )
            .padding(if (compact) 8.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        content = {
            CupertinoText(
                text = title,
                style = CupertinoTheme.typography.footnote,
                color = palette.textSecondary,
            )
            content()
        },
    )
}

/**
 * 处理设备runtimeinforegisters。
 */
private fun DeviceRuntimeInfoRegisters.toBoardInfoEntries(): List<BoardInfoEntry> {
    return buildList {
        firmwareVersion.takeIf { it.isNotBlank() }?.let { value ->
            add(BoardInfoEntry("固件版本", value))
        }
        cpuModel.takeIf { it.isNotBlank() }?.let { value ->
            add(BoardInfoEntry("CPU 型号", value))
        }
        add(BoardInfoEntry("晶振频率", "${xtalFrequencyHz} Hz"))
        add(BoardInfoEntry("Flash 容量", flashSizeBytes.toLong().toSizeLabel()))
        macAddress.takeIf { it.isNotBlank() }?.let { value ->
            add(BoardInfoEntry("MAC 地址", value))
        }
    }
}

/**
 * 处理烧录配置registers。
 */
private fun FlashConfigRegisters.toBoardInfoEntries(): List<BoardInfoEntry> {
    return buildList {
        add(BoardInfoEntry("魔术字", magicWord.toHexLabel(width = 8)))
        portConfig.toHexBytesLabel()?.let { value ->
            add(BoardInfoEntry("端口配置", value))
        }
        uartParams.toHexBytesLabel()?.let { value ->
            add(BoardInfoEntry("串口参数", value))
        }
        add(BoardInfoEntry("从机地址", slaveAddress.toString()))
        debounceParams.toHexBytesLabel()?.let { value ->
            add(BoardInfoEntry("防抖参数", value))
        }
        add(BoardInfoEntry("帧间隔", "${modbusInterval} ms"))
        add(BoardInfoEntry("看门狗", if (wdtEnable != 0) "开启" else "关闭"))
        add(BoardInfoEntry("固件升级", if (firmwareUpgrade != 0) "是" else "否"))
        diHardwareFirmware.toHexBytesLabel()?.let { value ->
            add(BoardInfoEntry("DI 固件", value))
        }
        diStatus.toHexBytesLabel()?.let { value ->
            add(BoardInfoEntry("DI 状态", value))
        }
        add(BoardInfoEntry("故障状态", faultStatus.toHexLabel(width = 2)))
        add(BoardInfoEntry("CRC", crc.toHexLabel(width = 4)))
    }
}

/**
 * 处理设备24powerlightsregisters。
 */
private fun Device24PowerLightsRegisters.toPowerLightStates(): List<Pair<String, Boolean>> {
    return listOf(
        "CH1" to light1,
        "CH2" to light2,
        "CH3" to light3,
        "CH4" to light4,
        "CH5" to light5,
        "CH6" to light6,
        "CH7" to light7,
        "CH8" to light8,
        "CH9" to light9,
        "CH10" to light10,
        "CH11" to light11,
        "CH12" to light12,
        "CH13" to light13,
        "CH14" to light14,
        "CH15" to light15,
        "CH16" to light16,
        "CH17" to light17,
        "CH18" to light18,
        "CH19" to light19,
        "CH20" to light20,
        "CH21" to light21,
        "CH22" to light22,
        "CH23" to light23,
        "CH24" to light24,
    )
}

/**
 * 处理模块板卡family。
 */
private fun ModuleBoardFamily.palette(): ModuleBoardPalette {
    return when (this) {
        ModuleBoardFamily.DI -> ModuleBoardPalette(
            accent = Color(0xFF2D9CDB),
            accentSoft = Color(0xFFE9F5FD),
            surfaceTop = Color(0xFFF8FBFF),
            surfaceBottom = Color(0xFFEDF4FB),
            border = Color(0xFFB7D4EA),
            chip = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            textPrimary = Color(0xFF163247),
            textSecondary = Color(0xFF5D7587),
            lightOn = Color(0xFF38D46A),
            lightOff = Color(0xFFD0DCE6),
        )
        ModuleBoardFamily.DO -> ModuleBoardPalette(
            accent = Color(0xFFF2994A),
            accentSoft = Color(0xFFFFF2E4),
            surfaceTop = Color(0xFFFFFCF7),
            surfaceBottom = Color(0xFFF8F0E5),
            border = Color(0xFFE7C9A7),
            chip = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            textPrimary = Color(0xFF4B3116),
            textSecondary = Color(0xFF806448),
            lightOn = Color(0xFFFFB45C),
            lightOff = Color(0xFFE6D7C6),
        )
        ModuleBoardFamily.AI -> ModuleBoardPalette(
            accent = Color(0xFF27AE60),
            accentSoft = Color(0xFFE7F7EE),
            surfaceTop = Color(0xFFF7FDF9),
            surfaceBottom = Color(0xFFEAF6EF),
            border = Color(0xFFBDDCC9),
            chip = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            textPrimary = Color(0xFF163C24),
            textSecondary = Color(0xFF58705F),
            lightOn = Color(0xFF2ECC71),
            lightOff = Color(0xFFD6E6DB),
        )
        ModuleBoardFamily.AO -> ModuleBoardPalette(
            accent = Color(0xFF9B51E0),
            accentSoft = Color(0xFFF3EAFE),
            surfaceTop = Color(0xFFFCFAFF),
            surfaceBottom = Color(0xFFF0EAF8),
            border = Color(0xFFD8C3ED),
            chip = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            textPrimary = Color(0xFF35204B),
            textSecondary = Color(0xFF6E5F7F),
            lightOn = Color(0xFFBB6BD9),
            lightOff = Color(0xFFE2D8EB),
        )
        ModuleBoardFamily.GENERIC -> ModuleBoardPalette(
            accent = Color(0xFF5C6AC4),
            accentSoft = Color(0xFFEEF1FF),
            surfaceTop = Color(0xFFF8F9FD),
            surfaceBottom = Color(0xFFEDEFF8),
            border = Color(0xFFC6CCE6),
            chip = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            textPrimary = Color(0xFF273052),
            textSecondary = Color(0xFF66708A),
            lightOn = Color(0xFF6C7BFF),
            lightOff = Color(0xFFD9DEEF),
        )
    }
}

/**
 * 处理int。
 *
 * @param width width。
 */
private fun Int.toHexLabel(
    width: Int,
): String {
    val positiveValue = toLong() and ((1L shl (width * 4)) - 1)
    return "0x${positiveValue.toString(16).uppercase().padStart(width, '0')}"
}

/**
 * 处理字节array。
 */
private fun ByteArray.toHexBytesLabel(): String? {
    if (isEmpty()) {
        return null
    }
    return joinToString(separator = " ") { item ->
        (item.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
    }
}
