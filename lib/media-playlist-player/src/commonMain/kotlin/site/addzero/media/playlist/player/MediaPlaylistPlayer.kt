package site.addzero.media.playlist.player

import MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun <T> MediaPlaylistPlayer(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemKey: (T) -> String,
    titleOf: (T) -> String,
    artistOf: (T) -> String,
    durationMsOf: (T) -> Long?,
    coverUrlOf: (T) -> String?,
    resolveUrl: suspend (T) -> String,
    headersOf: (T) -> Map<String, String> = { emptyMap() },
    emptyHint: String = "从下方列表选择一首歌开始试听",
    resolveErrorMessage: (Throwable) -> String = { it.message ?: "加载音频失败" },
    itemActions: @Composable (T) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val unavailableMessage = "当前歌曲无可用音源"

    var selectedKey by remember { mutableStateOf<String?>(null) }
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    var selectedHeaders by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var resolvingKey by remember { mutableStateOf<String?>(null) }
    var resolveError by remember { mutableStateOf<String?>(null) }
    var loadToken by remember { mutableIntStateOf(0) }
    var unavailableKeys by remember { mutableStateOf(setOf<String>()) }

    val selectedItem = remember(items, selectedKey) {
        items.firstOrNull { itemKey(it) == selectedKey }
    }

    LaunchedEffect(items) {
        val activeKeys = items.map(itemKey).toSet()
        unavailableKeys = unavailableKeys.filterTo(mutableSetOf()) { it in activeKeys }
        if (selectedKey != null && items.none { itemKey(it) == selectedKey }) {
            selectedKey = null
            selectedUrl = null
            selectedHeaders = emptyMap()
            resolvingKey = null
            resolveError = null
        }
    }

    fun requestPlay(item: T) {
        val key = itemKey(item)
        if (key in unavailableKeys) {
            resolveError = unavailableMessage
            return
        }
        if (selectedKey == key && selectedUrl != null && resolvingKey == null) {
            return
        }

        selectedKey = key
        selectedUrl = null
        selectedHeaders = headersOf(item)
        resolveError = null
        resolvingKey = key
        val requestToken = ++loadToken

        scope.launch {
            try {
                val resolvedUrl = resolveUrl(item).trim()
                if (requestToken == loadToken && selectedKey == key) {
                    if (resolvedUrl.isBlank()) {
                        unavailableKeys = unavailableKeys + key
                        selectedUrl = null
                        resolveError = unavailableMessage
                    } else {
                        selectedUrl = resolvedUrl
                    }
                    resolvingKey = null
                }
            } catch (error: Throwable) {
                if (requestToken == loadToken && selectedKey == key) {
                    resolveError = if (error.isNoAudioSource()) {
                        unavailableKeys = unavailableKeys + key
                        unavailableMessage
                    } else {
                        resolveErrorMessage(error)
                    }
                    resolvingKey = null
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (selectedItem != null) {
                    PlaylistPlayerHeader(
                        title = titleOf(selectedItem),
                        artist = artistOf(selectedItem),
                        coverUrl = coverUrlOf(selectedItem),
                        durationMs = durationMsOf(selectedItem),
                    )
                } else {
                    Text(
                        text = "列表播放器",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                when {
                    selectedKey != null && resolvingKey == selectedKey -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text(
                                text = "正在加载音频...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    selectedItem != null && selectedUrl != null -> {
                        MediaPlayer(
                            modifier = Modifier.fillMaxWidth(),
                            url = selectedUrl.orEmpty(),
                            headers = selectedHeaders,
                            startTime = MaterialTheme.colorScheme.onSurface,
                            endTime = MaterialTheme.colorScheme.onSurface,
                            autoPlay = true,
                            volumeIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            playIconColor = MaterialTheme.colorScheme.primary,
                            sliderTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            sliderIndicatorColor = MaterialTheme.colorScheme.primary,
                            showControls = true,
                        )
                    }

                    else -> {
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                resolveError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = items,
                key = { item -> itemKey(item) },
            ) { item ->
                PlaylistListItem(
                    title = titleOf(item),
                    artist = artistOf(item),
                    durationMs = durationMsOf(item),
                    coverUrl = coverUrlOf(item),
                    selected = itemKey(item) == selectedKey,
                    resolving = itemKey(item) == resolvingKey,
                    unavailable = itemKey(item) in unavailableKeys,
                    onPlayClick = { requestPlay(item) },
                    itemActions = { itemActions(item) },
                )
            }
        }
    }
}

@Composable
private fun PlaylistPlayerHeader(
    title: String,
    artist: String,
    coverUrl: String?,
    durationMs: Long?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistCover(
            coverUrl = coverUrl,
            size = 72.dp,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist.ifBlank { "未知歌手" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        durationMs?.takeIf { it > 0 }?.let {
            PlaylistMetadataPill(text = formatPlaylistDuration(it))
        }
    }
}

@Composable
private fun PlaylistListItem(
    title: String,
    artist: String,
    durationMs: Long?,
    coverUrl: String?,
    selected: Boolean,
    resolving: Boolean,
    unavailable: Boolean,
    onPlayClick: () -> Unit,
    itemActions: @Composable () -> Unit,
) {
    val playButtonText = when {
        unavailable -> "无音源"
        resolving -> "加载中..."
        selected -> "当前播放"
        else -> "试听"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !unavailable, onClick = onPlayClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useWideLayout = maxWidth >= 720.dp

            if (useWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlaylistCover(
                        coverUrl = coverUrl,
                        size = 56.dp,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = artist.ifBlank { "未知歌手" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    durationMs?.takeIf { it > 0 }?.let {
                        Text(
                            text = formatPlaylistDuration(it),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onPlayClick,
                            enabled = !unavailable,
                        ) {
                            Text(playButtonText)
                        }
                        itemActions()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlaylistCover(
                            coverUrl = coverUrl,
                            size = 56.dp,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = artist.ifBlank { "未知歌手" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (durationMs != null && durationMs > 0) {
                            Text(
                                text = formatPlaylistDuration(durationMs),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Box(modifier = Modifier)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalButton(
                                onClick = onPlayClick,
                                enabled = !unavailable,
                            ) {
                                Text(playButtonText)
                            }
                            itemActions()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistMetadataPill(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PlaylistCover(
    coverUrl: String?,
    size: Dp,
) {
    if (!coverUrl.isNullOrBlank()) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier = Modifier.size(size),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "♪",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

private fun formatPlaylistDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

private fun Throwable.isNoAudioSource(): Boolean {
    val message = message?.trim()?.lowercase().orEmpty()
    if (message.isBlank()) return false

    return message.contains("no valid download url") ||
        message.contains("download url not found") ||
        message.contains("download url is empty") ||
        message.contains("vip required") ||
        message.contains("无音源") ||
        message.contains("无可用音源") ||
        message.contains("暂无音源") ||
        message.contains("音源为空") ||
        message.contains("播放url为空") ||
        message.contains("播放 url 为空") ||
        message.contains("音频地址为空")
}
