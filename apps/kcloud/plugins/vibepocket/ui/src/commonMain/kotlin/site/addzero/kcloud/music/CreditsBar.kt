package site.addzero.kcloud.music

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.ui.StudioMetricCard

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
        credits != null -> "Suno 积分"
        else -> "积分未知"
    }
    val containerColor = when {
        isLoading -> MaterialTheme.colorScheme.tertiaryContainer
        credits != null -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    StudioMetricCard(
        label = label,
        value = value,
        modifier = Modifier.width(116.dp).height(72.dp),
        containerColor = containerColor,
    )
}
