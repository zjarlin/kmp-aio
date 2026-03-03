package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.GlassColors
import site.addzero.component.glass.GlassTheme
import site.addzero.component.glass.glassEffect

/**
 * InlinePlayer — 内联播放控件
 *
 * 紧凑型播放器，设计用于嵌入 TrackCard 内部。
 * 包含播放/暂停按钮、进度条和时间标签，使用 GlassUI 样式。
 *
 * @param audioUrl 音频地址（预留，当前仅用于标识）
 * @param isPlaying 当前是否正在播放
 * @param onPlayPause 播放/暂停切换回调
 * @param progress 播放进度 0f..1f
 * @param currentTime 当前播放时间，如 "1:23"
 * @param totalTime 总时长，如 "3:45"
 */
@Composable
fun InlinePlayer(
    audioUrl: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    progress: Float,
    currentTime: String,
    totalTime: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 播放/暂停按钮
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = GlassColors.NeonCyan,
                modifier = Modifier.size(24.dp),
            )
        }

        // 当前时间
        Text(
            text = currentTime,
            color = GlassTheme.TextSecondary,
            fontSize = 11.sp,
        )

        // 进度条
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = GlassColors.NeonCyan,
            trackColor = GlassTheme.GlassBorder,
        )

        // 总时长
        Text(
            text = totalTime,
            color = GlassTheme.TextTertiary,
            fontSize = 11.sp,
        )
    }
}
