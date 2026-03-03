package site.addzero.vibepocket.music

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.glass.GlassStatCard
import site.addzero.component.glass.GlassTheme

/**
 * CreditsBar — 积分显示条
 *
 * 嵌入音乐模块页面顶部，以 GlassStatCard 样式紧凑展示 Suno API 剩余积分。
 *
 * @param credits 积分数值，null 表示加载失败或尚未加载
 * @param isLoading 是否正在加载积分数据
 */
@Composable
fun CreditsBar(
    credits: Int?,
    isLoading: Boolean,
) {
    val value = when {
        isLoading -> "..."
        credits != null -> "$credits"
        else -> "?"
    }
    val label = when {
        isLoading -> "加载中"
        credits != null -> "🎵 积分"
        else -> "积分未知"
    }
    val glowColor = when {
        isLoading -> GlassTheme.NeonPurple
        credits != null -> GlassTheme.NeonCyan
        else -> GlassTheme.NeonMagenta
    }

    GlassStatCard(
        value = value,
        label = label,
        modifier = Modifier.width(100.dp).height(64.dp),
        glowColor = glowColor,
    )
}
