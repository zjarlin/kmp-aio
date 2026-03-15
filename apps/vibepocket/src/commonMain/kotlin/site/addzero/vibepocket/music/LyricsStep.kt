package site.addzero.vibepocket.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.launch
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.media.playlist.player.PlaylistAudioSource
import site.addzero.vibepocket.api.music.MusicTrack
import site.addzero.vibepocket.api.suno.SunoLyricItem
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard

private const val NETEASE_PROVIDER = "netease"
private const val QQ_PROVIDER = "qq"

@Composable
fun LyricsStep(
    lyrics: String,
    onLyricsChange: (String) -> Unit,
    songName: String,
    onSongNameChange: (String) -> Unit,
    artistName: String,
    onArtistNameChange: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val downloader = remember { HttpClient() }
    val fileSaverLauncher = rememberFileSaverLauncher { }

    var selectedProvider by remember { mutableStateOf(NETEASE_PROVIDER) }
    var isAiMode by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isDownloadingId by remember { mutableStateOf<String?>(null) }
    var importingLyricId by remember { mutableStateOf<String?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }

    DisposableEffect(downloader) {
        onDispose { downloader.close() }
    }

    val searchKeyword = remember(songName, artistName) {
        buildString {
            append(songName.trim())
            if (artistName.isNotBlank()) {
                append(' ')
                append(artistName.trim())
            }
        }.trim()
    }

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
        StudioSectionCard(
            title = "搜索歌曲歌词",
            subtitle = "搜索结果支持导入歌词、试听和下载，这里完全独立于 Suno 配置。",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilterChip(
                    selected = selectedProvider == NETEASE_PROVIDER,
                    onClick = { selectedProvider = NETEASE_PROVIDER },
                    label = { Text("网易云") },
                )
                FilterChip(
                    selected = selectedProvider == QQ_PROVIDER,
                    onClick = { selectedProvider = QQ_PROVIDER },
                    label = { Text("QQ 音乐") },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = songName,
                    onValueChange = onSongNameChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("歌名") },
                    placeholder = { Text("例如：晴天") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = artistName,
                    onValueChange = onArtistNameChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("歌手") },
                    placeholder = { Text("可选，例如：周杰伦") },
                    singleLine = true,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StudioPill(
                    text = providerLabel(selectedProvider),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                FilledTonalButton(
                    onClick = {
                        if (isSearching) return@FilledTonalButton
                        isSearching = true
                        searchError = null
                        searchResults = emptyList()
                        scope.launch {
                            try {
                                searchResults = MusicSearchService.search(selectedProvider, searchKeyword)
                            } catch (error: Exception) {
                                searchError = error.message ?: "搜索失败"
                            } finally {
                                isSearching = false
                            }
                        }
                    },
                    enabled = searchKeyword.isNotBlank() && !isSearching,
                ) {
                    Text(if (isSearching) "搜索中..." else "搜索歌曲")
                }
            }

            searchError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            when {
                searchResults.isNotEmpty() -> {
                    DefaultPlaylistPlayer(
                        items = searchResults,
                        itemKey = { track -> MusicSearchService.playbackId(track) },
                        titleOf = { track -> track.name },
                        subtitleOf = { track ->
                            buildString {
                                append(track.artist.ifBlank { "未知歌手" })
                                if (track.album.isNotBlank()) {
                                    append(" · ")
                                    append(track.album)
                                }
                                if (track.platform.isNotBlank()) {
                                    append(" · ")
                                    append(providerLabel(track.platform))
                                }
                            }
                        },
                        durationMsOf = { track -> track.durationMs.takeIf { it > 0 } },
                        coverUrlOf = { track -> track.coverUrl },
                        resolveAudioSource = { track ->
                            val asset = MusicSearchService.resolve(track)
                            PlaylistAudioSource(
                                url = asset.url,
                                unavailableMessage = "当前歌曲无可用音源",
                            )
                        },
                        resolveErrorMessage = { error ->
                            error.message ?: "试听失败"
                        },
                        resolveLyrics = { track ->
                            MusicSearchService.getLyrics(track).lrc.takeIf { it.isNotBlank() }
                        },
                        itemActions = { track, actionState ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    onClick = {
                                        if (importingLyricId != null) {
                                            return@Button
                                        }
                                        importingLyricId = track.id
                                        scope.launch {
                                            try {
                                                val lyric = MusicSearchService.getLyrics(track)
                                                if (lyric.lrc.isBlank()) {
                                                    searchError = "这首歌暂时没有可导入的歌词"
                                                } else {
                                                    onLyricsChange(lyric.lrc)
                                                    onSongNameChange(track.name)
                                                    if (track.artist.isNotBlank()) {
                                                        onArtistNameChange(track.artist)
                                                    }
                                                }
                                            } catch (error: Exception) {
                                                searchError = error.message ?: "获取歌词失败"
                                            } finally {
                                                importingLyricId = null
                                            }
                                        }
                                    },
                                    enabled = importingLyricId == null,
                                ) {
                                    Text(if (importingLyricId == track.id) "导入中..." else "导入歌词")
                                }
                                FilledTonalButton(
                                    onClick = {
                                        if (isDownloadingId != null) {
                                            return@FilledTonalButton
                                        }
                                        isDownloadingId = track.id
                                        scope.launch {
                                            try {
                                                val asset = MusicSearchService.resolve(track)
                                                val bytes = downloader.get(asset.url).readRawBytes()
                                                val extension = asset.fileName.substringAfterLast('.', "mp3")
                                                val baseName = asset.fileName.substringBeforeLast('.', asset.fileName)
                                                fileSaverLauncher.launch(
                                                    bytes = bytes,
                                                    baseName = baseName,
                                                    extension = extension,
                                                )
                                            } catch (error: Exception) {
                                                searchError = error.message ?: "下载失败"
                                            } finally {
                                                isDownloadingId = null
                                            }
                                        }
                                    },
                                    enabled = isDownloadingId == null && actionState.canUseAudioUrl,
                                ) {
                                    Text(
                                        when {
                                            isDownloadingId == track.id -> "下载中..."
                                            actionState.isUnavailable -> "无音源"
                                            else -> "下载"
                                        }
                                    )
                                }
                            }
                        },
                    )
                }

                !isSearching && searchKeyword.isNotBlank() && searchError == null -> {
                    StudioEmptyState(
                        icon = "🎼",
                        title = "还没有搜索结果",
                        description = "输入歌名后搜索，找到合适的参考歌词再继续下一步。",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
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
                    .heightIn(min = 280.dp),
                placeholder = {
                    Text(
                        "在这里输入或粘贴歌词，例如：\n[00:33.71]阿刁\n[00:36.31]住在西藏的某个地方",
                    )
                },
                singleLine = false,
                minLines = 12,
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
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(0.92f),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    LyricsModePanel()
                    LyricsSearchPanel()
                }
                Column(
                    modifier = Modifier.weight(1.08f),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    LyricsEditorPanel()
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    .heightIn(min = 120.dp),
                label = { Text("提示词") },
                placeholder = { Text("例如：一首关于夏天海边、轻快又带点失恋味道的流行歌词") },
                singleLine = false,
                minLines = 4,
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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

private fun providerLabel(provider: String): String {
    return when (provider.trim().lowercase()) {
        NETEASE_PROVIDER -> "网易云"
        QQ_PROVIDER -> "QQ 音乐"
        else -> provider
    }
}
