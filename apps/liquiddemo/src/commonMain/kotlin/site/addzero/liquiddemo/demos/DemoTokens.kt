package site.addzero.liquiddemo.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object DemoTokens {
    val textPrimary = Color.White.copy(alpha = 0.96f)
    val textSecondary = Color.White.copy(alpha = 0.82f)
    val textMuted = Color.White.copy(alpha = 0.64f)
}

/** 轻量标签底板：给 demo 顶部状态和说明一个不喧宾夺主的落点。 */
internal fun Modifier.demoLabelFrame(): Modifier {
    return background(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(999.dp),
    ).padding(horizontal = 12.dp, vertical = 7.dp)
}
