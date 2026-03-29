package site.addzero.kcloud.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.kcloud.api.suno.SunoLyricItem
import site.addzero.kcloud.ui.StudioPill
import site.addzero.kcloud.ui.StudioSectionCard

@Composable
fun LyricsStep(
    lyrics: String,
    onLyricsChange: (String) -> Unit,
    songName: String,
    onSongNameChange: (String) -> Unit,
    artistName: String,
    onArtistNameChange: (String) -> Unit,
) {
    var isAiMode by remember { mutableStateOf(false) }

    @Composable
    fun LyricsModePanel() {
        StudioSectionCard(
            title = "歌词工作台",
            subtitle = "在这里整理歌词、导入现成歌曲歌词，或者直接调用 Suno 生成候选文案。",
            action = {
                StudioPill(
                    text = if (isAiMode) "AI mode" else "Manual mode",
                    containerColor = if (isAiMode) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (isAiMode) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                )
            },
        ) {
            FilterChip(
                selected = isAiMode,
                onClick = { isAiMode = !isAiMode },
                label = {
                    Text(if (isAiMode) "切回手动编辑" else "切到 AI 歌词")
                },
            )

            if (isAiMode) {
                AiLyricsGenerator(
                    onLyricsGenerated = { generated ->
                        onLyricsChange(generated)
                        isAiMode = false
                    },
                )
            }
        }
    }

    @Composable
    fun LyricsSearchPanel() {
        MusicReferenceSearchSection(
            songName = songName,
            onSongNameChange = onSongNameChange,
            artistName = artistName,
            onArtistNameChange = onArtistNameChange,
            subtitle = "这里会聚合网易云和 QQ 音乐结果，支持导入歌词、试听和下载，且完全独立于 Suno 配置。",
            emptyHintDescription = "输入歌名后搜索，找到合适的参考歌词再继续下一步。",
            onLyricsImported = { track, lyric ->
                onLyricsChange(lyric.lrc)
                onSongNameChange(track.name)
                if (track.artist.isNotBlank()) {
                    onArtistNameChange(track.artist)
                }
            },
        )
    }

    @Composable
    fun LyricsEditorPanel() {
        StudioSectionCard(
            title = "歌词编辑器",
            subtitle = "支持纯文本和带时间轴的 LRC。导入歌词后可以继续细修。",
        ) {
            OutlinedTextField(
                value = lyrics,
                onValueChange = onLyricsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp),
                placeholder = {
                    Text(
                        "支持两种格式：\n纯文本：每行一句歌词\nLRC：如 [00:12.00]第一句歌词\n\n把你的歌词直接贴进来即可。",
                    )
                },
                singleLine = false,
                minLines = 9,
            )
            Text(
                text = "当前共 ${lyrics.lines().count { it.isNotBlank() }} 行",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    BoxWithConstraints {
        val useWideLayout = maxWidth >= 1040.dp
        if (useWideLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(0.92f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LyricsModePanel()
                    LyricsSearchPanel()
                }
                Column(
                    modifier = Modifier.weight(1.08f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LyricsEditorPanel()
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LyricsModePanel()
                LyricsSearchPanel()
                LyricsEditorPanel()
            }
        }
    }
}

@Composable
private fun AiLyricsGenerator(
    onLyricsGenerated: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var candidates by remember { mutableStateOf<List<SunoLyricItem>>(emptyList()) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "AI 歌词候选",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "这里会调用 Suno 的歌词生成接口。没配 Token 时不会闪退，只会给出清晰提示。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                label = { Text("提示词") },
                placeholder = { Text("例如：一首关于夏天海边、轻快又带点失恋味道的流行歌词") },
                singleLine = false,
                minLines = 3,
            )
            FilledTonalButton(
                onClick = {
                    if (isGenerating) return@FilledTonalButton
                    isGenerating = true
                    errorMessage = null
                    candidates = emptyList()
                    statusText = "正在提交..."

                    scope.launch {
                        try {
                            val result = SunoWorkflowService.generateLyricsCandidates(
                                prompt = prompt,
                                onStatusUpdate = { statusText = it },
                            )
                            candidates = result
                            statusText = null
                            if (result.size == 1) {
                                result.first().text?.let(onLyricsGenerated)
                            }
                        } catch (error: Exception) {
                            errorMessage = SunoWorkflowService.errorMessage(error)
                            statusText = null
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = prompt.isNotBlank() && !isGenerating,
            ) {
                Text(if (isGenerating) "生成中..." else "生成歌词候选")
            }

            statusText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (candidates.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEachIndexed { index, candidate ->
                        LyricCandidateItem(
                            index = index + 1,
                            item = candidate,
                            onClick = {
                                candidate.text?.let(onLyricsGenerated)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricCandidateItem(
    index: Int,
    item: SunoLyricItem,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "候选 $index",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                item.title?.takeIf { it.isNotBlank() }?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = item.text.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
