package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import site.addzero.vibepocket.api.suno.SunoTrack
import site.addzero.vibepocket.model.TrackAction
import site.addzero.vibepocket.model.TrackPlayerState
import site.addzero.vibepocket.ui.StudioPill

/**
 * TrackCard — 统一音轨卡片组件
 *
 * 展示单首 Track 的标题、标签、封面图，集成内联播放器、收藏星星和操作菜单。
 * 使用 GlassCard 样式，设计用于 TaskProgressPanel、MusicHistoryPage 等场景。
 */
@Composable
fun TrackCard(
    track: SunoTrack,
    taskId: String,
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onAction: (TrackAction) -> Unit,
    playerState: TrackPlayerState,
    onPlayToggle: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val imageUrl = track.imageUrl
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = track.title,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🎵",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = track.title ?: "未命名音轨",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        track.tags?.takeIf { it.isNotBlank() }?.let { tags ->
                            StudioPill(
                                text = tags,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        taskId.takeIf { it.isNotBlank() }?.let { id ->
                            StudioPill(
                                text = id.take(8),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }

                if (track.id != null) {
                    IconButton(
                        onClick = { onFavoriteToggle(!isFavorite) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (track.id != null) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "操作菜单",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        TrackActionMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onAction = { action ->
                                menuExpanded = false
                                onAction(action)
                            },
                        )
                    }
                }
            }

            track.audioUrl?.let { audioUrl ->
                InlinePlayer(
                    audioUrl = audioUrl,
                    isPlaying = playerState.isPlaying,
                    onPlayPause = onPlayToggle,
                    progress = playerState.progress,
                    currentTime = playerState.currentTime,
                    totalTime = playerState.totalTime,
                )
            }
        }
    }
}
