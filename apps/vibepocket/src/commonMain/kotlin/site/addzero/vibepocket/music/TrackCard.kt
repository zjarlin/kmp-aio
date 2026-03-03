package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import site.addzero.component.glass.GlassCard
import site.addzero.component.glass.GlassColors
import site.addzero.component.glass.GlassTheme
import site.addzero.vibepocket.api.suno.SunoTrack
import site.addzero.vibepocket.model.TrackAction
import site.addzero.vibepocket.model.TrackPlayerState

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
    // 操作菜单展开状态
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── 顶部：封面图 + 标题/标签 + 操作按钮 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 封面图
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
                        Text("🎵", fontSize = 24.sp)
                    }
                }

                // 标题 + 标签
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = track.title ?: "未命名音轨",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    track.tags?.let { tags ->
                        Text(
                            text = "🏷️ $tags",
                            color = GlassTheme.TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // 收藏星星按钮（仅当 track 有 id 时显示）
                if (track.id != null) {
                    IconButton(
                        onClick = { onFavoriteToggle(!isFavorite) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) GlassColors.NeonCyan else GlassTheme.TextTertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // 操作菜单按钮（仅当 track 有 id 时显示）
                if (track.id != null) {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "操作菜单",
                                tint = GlassTheme.TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // 下拉操作菜单
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

            // ── 内联播放器（仅当 track 有 audioUrl 时显示） ──
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
