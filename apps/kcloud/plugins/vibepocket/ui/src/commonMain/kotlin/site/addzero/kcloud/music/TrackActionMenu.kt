package site.addzero.kcloud.music

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.model.TrackAction

/**
 * TrackAction 的中文标签映射
 */
private fun TrackAction.label(): String = when (this) {
    TrackAction.EXTEND -> "扩展"
    TrackAction.VOCAL_REMOVAL -> "人声分离"
    TrackAction.GENERATE_COVER -> "生成封面"
    TrackAction.CREATE_PERSONA -> "创建 Persona"
    TrackAction.REPLACE_SECTION -> "替换片段"
    TrackAction.EXPORT_WAV -> "导出 WAV"
    TrackAction.BOOST_STYLE -> "风格提升"
}

/**
 * TrackAction 的图标 emoji
 */
private fun TrackAction.icon(): String = when (this) {
    TrackAction.EXTEND -> "🔄"
    TrackAction.VOCAL_REMOVAL -> "🎤"
    TrackAction.GENERATE_COVER -> "🖼️"
    TrackAction.CREATE_PERSONA -> "👤"
    TrackAction.REPLACE_SECTION -> "✂️"
    TrackAction.EXPORT_WAV -> "📥"
    TrackAction.BOOST_STYLE -> "✨"
}

/**
 * TrackActionMenu — Track 操作下拉菜单
 *
 * 列出所有 TrackAction 操作项，使用 GlassUI 深色背景样式。
 * 每个菜单项显示图标 emoji + 中文标签。
 *
 * @param expanded 菜单是否展开
 * @param onDismiss 关闭菜单回调
 * @param onAction 选择操作回调
 */
@Composable
fun TrackActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAction: (TrackAction) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
        ),
    ) {
        TrackAction.entries.forEach { action ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = "${action.icon()} ${action.label()}",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                onClick = { onAction(action) },
            )
        }
    }
}
