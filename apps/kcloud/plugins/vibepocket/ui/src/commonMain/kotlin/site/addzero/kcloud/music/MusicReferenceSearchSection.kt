package site.addzero.kcloud.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import site.addzero.kcloud.api.music.MusicLyric
import site.addzero.kcloud.api.music.MusicResolvedAsset
import site.addzero.kcloud.api.music.MusicTrack
import site.addzero.kcloud.ui.StudioEmptyState
import site.addzero.kcloud.ui.StudioPill
import site.addzero.kcloud.ui.StudioSectionCard
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.media.playlist.player.PlaylistAudioSource

@Composable
internal fun MusicReferenceSearchSection(
    songName: String,
    onSongNameChange: (String) -> Unit,
    artistName: String,
    onArtistNameChange: (String) -> Unit,
    title: String = "搜索歌曲歌词",
    subtitle: String = "这里会聚合网易云和 QQ 音乐结果，支持导入歌词、试听和下载。",
    emptyHintTitle: String = "还没有搜索结果",
    emptyHintDescription: String = "输入歌名后搜索，找到合适的参考内容再继续。",
    sourceActionLabel: String? = null,
    onLyricsImported: (MusicTrack, MusicLyric) -> Unit,
    onSourceResolved: (suspend (MusicTrack, MusicResolvedAsset) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val downloader = remember { HttpClient() }
    val fileSaverLauncher = rememberFileSaverLauncher { }

    var isSearching by remember { mutableStateOf(false) }
    var isDownloadingId by remember { mutableStateOf<String?>(null) }
    var importingLyricId by remember { mutableStateOf<String?>(null) }
    var resolvingSourceId by remember { mutableStateOf<String?>(null) }
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

    StudioSectionCard(
        title = title,
        subtitle = subtitle,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                text = "聚合搜索 · 网易云 + QQ 音乐",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            FilledTonalButton(
                onClick = {
                    if (isSearching) {
                        return@FilledTonalButton
                    }
                    isSearching = true
                    searchError = null
                    searchResults = emptyList()
                    scope.launch {
                        try {
                            searchResults = MusicSearchService.search(searchKeyword)
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
                                append(providerDisplayName(track.platform))
                            }
                        }
                    },
                    durationMsOf = { track -> track.durationMs.takeIf { it > 0 } },
                    coverUrlOf = { track -> track.coverUrl },
                    hasResolvableAudioOf = { track -> !track.link.isNullOrBlank() },
                    resolveAudioSource = { track ->
                        if (track.link.isNullOrBlank()) {
                            PlaylistAudioSource(
                                url = null,
                                unavailableMessage = "无音源",
                            )
                        } else {
                            val asset = MusicSearchService.resolve(track)
                            PlaylistAudioSource(
                                url = asset.url,
                                unavailableMessage = "当前歌曲无可用音源",
                            )
                        }
                    },
                    resolveErrorMessage = { error ->
                        error.message ?: "试听失败"
                    },
                    resolveLyrics = { track ->
                        MusicSearchService.getLyrics(track).lrc.takeIf { it.isNotBlank() }
                    },
                    itemActions = { track, actionState ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            sourceActionLabel?.let { actionLabel ->
                                Button(
                                    onClick = {
                                        if (resolvingSourceId != null) {
                                            return@Button
                                        }
                                        resolvingSourceId = track.id
                                        scope.launch {
                                            try {
                                                val asset = MusicSearchService.resolve(track)
                                                onSourceResolved?.invoke(track, asset)
                                            } catch (error: Exception) {
                                                searchError = error.message ?: "设置音源失败"
                                            } finally {
                                                resolvingSourceId = null
                                            }
                                        }
                                    },
                                    enabled = resolvingSourceId == null && actionState.canUseAudioUrl,
                                ) {
                                    Text(
                                        when {
                                            resolvingSourceId == track.id -> "带入中..."
                                            actionState.isUnavailable -> "无音源"
                                            else -> actionLabel
                                        }
                                    )
                                }
                            }

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
                                                onLyricsImported(track, lyric)
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

            isSearching -> {
                Text(
                    text = "正在搜索，请稍候...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            !isSearching && searchKeyword.isNotBlank() && searchError == null -> {
                StudioEmptyState(
                    icon = "🎼",
                    title = emptyHintTitle,
                    description = emptyHintDescription,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun providerDisplayName(provider: String): String {
    return when (provider.trim().lowercase()) {
        "netease" -> "网易云"
        "qq" -> "QQ 音乐"
        else -> provider
    }
}
