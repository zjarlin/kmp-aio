package site.addzero.kcloud.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.workbench.design.button.WorkbenchFilledTonalButton as FilledTonalButton
import site.addzero.workbench.design.button.WorkbenchOutlinedButton as OutlinedButton

internal data class MusicPromptPreset(
    val id: String,
    val title: String,
    val summary: String,
    val prompt: String,
    val suggestedStyle: String = "",
)

internal val musicPromptPresets: List<MusicPromptPreset> = listOf(
    MusicPromptPreset(
        id = "black-gospel",
        title = "黑人福音男声",
        summary = "灵魂感极强的黑福音男主唱，电影化铺陈，逐步推向追逐感高潮。",
        prompt = """
            Powerful Black male gospel lead vocal,
            deep, soulful, and resonant,
            emotional delivery with spiritual intensity,
            rich gospel-style backing vocals and call-and-response harmonies.

            Intro:
            ethereal and cinematic opening,
            air pads, distant choir textures,
            minimalist, spacious, sacred atmosphere,
            slow and reverent, almost ritualistic.

            Verse:
            gradual entrance of rhythm,
            soft percussive pulses and subtle drum effects,
            heartbeat-like groove,
            sense of forward motion begins,
            vocals remain controlled and expressive.

            Pre-Chorus:
            rhythm intensifies,
            layered percussion, hand drums, cinematic hits,
            marching feel develops,
            energy steadily rising,
            gospel harmonies becoming fuller.

            Chorus:
            Circle of Life–inspired gospel arrangement,
            powerful choir layers,
            bold, uplifting, triumphant,
            strong sense of movement and purpose,
            emotional release with spiritual weight.

            Final Chorus:
            driving rhythm and cinematic percussion,
            running and chasing sensation,
            propulsive drums, wide brass or synth swells,
            euphoric spiritual climax.
        """.trimIndent(),
        suggestedStyle = "black gospel, cinematic choir, soulful male lead, call-and-response, triumphant spiritual",
    ),
    MusicPromptPreset(
        id = "cinematic-female-ballad",
        title = "电影抒情女声",
        summary = "空灵女声搭配钢琴和弦乐，情绪从克制到爆发，适合剧情向主题曲。",
        prompt = """
            Emotional female lead vocal,
            intimate, airy, and cinematic,
            soft vulnerability at the beginning,
            gradually expanding into a powerful emotional release.

            Intro:
            sparse piano,
            soft tape ambience,
            wide reverb and distant strings,
            reflective and fragile atmosphere.

            Verse:
            close-mic storytelling vocal,
            restrained rhythm,
            subtle pulse and low-end warmth,
            elegant and heartfelt melodic lines.

            Chorus:
            soaring string layers,
            fuller drums,
            cinematic swells and emotional lift,
            bittersweet but hopeful climax.
        """.trimIndent(),
        suggestedStyle = "cinematic pop ballad, emotional female vocal, piano strings, bittersweet uplifting",
    ),
    MusicPromptPreset(
        id = "urban-rnb-night",
        title = "都市 R&B 夜色",
        summary = "近距离人声、丝滑和声与低频律动，适合午夜街灯感的现代 R&B。",
        prompt = """
            Smooth contemporary R&B vocal,
            intimate and close,
            silky harmonies,
            confident but melancholic night-drive mood.

            Intro:
            soft synth haze,
            filtered keys,
            subtle vinyl noise,
            neon-lit midnight atmosphere.

            Verse:
            laid-back groove,
            warm bass,
            crisp finger snaps and minimal drums,
            emotionally controlled vocal phrasing.

            Chorus:
            richer harmonies,
            wider synth pads,
            deeper groove,
            sensual but emotionally distant release.
        """.trimIndent(),
        suggestedStyle = "modern rnb, night drive, intimate vocal, silky harmony, warm bass groove",
    ),
)

@Composable
internal fun MusicPromptPresetSection(
    onApplyPrompt: (String) -> Unit,
    onApplyStyle: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedPresetId by remember { mutableStateOf(musicPromptPresets.firstOrNull()?.id) }
    val selectedPreset = musicPromptPresets.firstOrNull { it.id == selectedPresetId }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "内置提示词候选",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            musicPromptPresets.forEach { preset ->
                FilterChip(
                    selected = preset.id == selectedPresetId,
                    onClick = { selectedPresetId = preset.id },
                    label = { Text(preset.title) },
                )
            }
        }
        selectedPreset?.let { preset ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = preset.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = preset.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (preset.suggestedStyle.isNotBlank()) {
                        Text(
                            text = "建议风格标签: ${preset.suggestedStyle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    ) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp),
                            ) {
                                Text(
                                    text = preset.prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { onApplyPrompt(preset.prompt) },
                        ) {
                            Text("回填提示词")
                        }
                        if (onApplyStyle != null && preset.suggestedStyle.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onApplyStyle(preset.suggestedStyle) },
                            ) {
                                Text("回填风格")
                            }
                            OutlinedButton(
                                onClick = {
                                    onApplyPrompt(preset.prompt)
                                    onApplyStyle(preset.suggestedStyle)
                                },
                            ) {
                                Text("全部回填")
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun buildSunoStyleText(
    tags: String,
    descriptionPrompt: String,
): String {
    val normalizedTags = tags.trim()
    val normalizedDescription = descriptionPrompt.trim()
    return buildList {
        if (normalizedTags.isNotBlank()) {
            add(normalizedTags)
        }
        if (normalizedDescription.isNotBlank()) {
            add(normalizedDescription)
        }
    }.joinToString(separator = "\n\n")
}
