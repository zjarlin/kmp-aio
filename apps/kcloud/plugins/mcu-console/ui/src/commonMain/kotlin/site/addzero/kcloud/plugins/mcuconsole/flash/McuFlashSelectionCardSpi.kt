package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.material3.Text

/**
 * McuFlash 页面可选卡片的交互插槽。
 *
 * 页面里的烧录配置和探针项都使用同一类可点击卡片；
 * 这个 slot 专门承接“选中哪一项”的交互语义，面板布局和列表编排仍保留在 `McuFlashScreen` 里。
 */
interface McuFlashSelectionCardSpi {
    @Composable
    fun Render(
        state: McuFlashSelectionCardState,
        actions: McuFlashSelectionCardActions,
    )
}

/**
 * McuFlash 页面可选卡片的默认实现。
 *
 * 当前默认行为是沿用现有选中样式，并把点击动作继续回传给页面视图模型；
 * 如果后续要补确认、权限限制或不同宿主样式，只替换这一实现即可。
 */
@Single
class DefaultMcuFlashSelectionCardSpi : McuFlashSelectionCardSpi {
    @Composable
    override fun Render(
        state: McuFlashSelectionCardState,
        actions: McuFlashSelectionCardActions,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = actions::select,
            enabled = state.enabled,
            shape = MaterialTheme.shapes.medium,
            color = if (state.selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            },
            contentColor = if (state.selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (state.selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                },
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = state.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/**
 * McuFlash 页面可选卡片的展示态。
 *
 * 这里收口选择项标题、摘要、选中态和启用态，
 * 让 slot 可以只关心当前卡片应该如何表现，而不用反向依赖整个页面状态对象。
 */
data class McuFlashSelectionCardState(
    val title: String,
    val caption: String,
    val selected: Boolean,
    val enabled: Boolean,
)

/**
 * McuFlash 页面可选卡片的动作桥接。
 *
 * 配置项和探针项都只是“选择一个目标”的局部交互，
 * 用这个小 actions 对象收口后，页面布局里就不需要直接散落 `onClick` 细节。
 */
class McuFlashSelectionCardActions(
    private val onSelect: () -> Unit,
) {
    fun select() {
        onSelect()
    }
}
