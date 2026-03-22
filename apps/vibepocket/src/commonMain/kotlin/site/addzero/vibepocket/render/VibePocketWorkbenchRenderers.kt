package site.addzero.vibepocket.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.liquidglass.LiquidGlassWorkbenchDefaults
import site.addzero.liquidglass.liquidGlassSurface
import site.addzero.vibepocket.feature.VibePocketFeatureSidebar
import site.addzero.vibepocket.screens.PlaceholderScreen
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.spi.content.WorkbenchContentRenderer
import site.addzero.workbenchshell.spi.header.WorkbenchHeaderRenderer
import site.addzero.workbenchshell.spi.sidebar.WorkbenchSidebarRenderer

internal class VibePocketSidebarRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: VibePocketShellState,
) : WorkbenchSidebarRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedScreenId by shellState.selectedScreenId.collectAsState()

        VibePocketFeatureSidebar(
            screenCatalog = screenCatalog,
            selectedId = screenCatalog.findLeaf(selectedScreenId)?.id.orEmpty(),
            onLeafClick = shellState::selectScreen,
            modifier = modifier,
        )
    }
}

internal class VibePocketHeaderRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: VibePocketShellState,
) : WorkbenchHeaderRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedScreenId by shellState.selectedScreenId.collectAsState()
        val selectedNode = screenCatalog.findLeaf(selectedScreenId)
        val breadcrumb = screenCatalog.breadcrumbNamesFor(selectedNode?.id.orEmpty())

        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (breadcrumb.isNotEmpty()) {
                androidx.compose.material3.Text(
                    text = breadcrumb.joinToString(" / "),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            }
            androidx.compose.material3.Text(
                text = selectedNode?.name ?: "未选择页面",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

internal class VibePocketContentRenderer(
    private val screenCatalog: ScreenCatalog,
    private val shellState: VibePocketShellState,
) : WorkbenchContentRenderer {
    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val selectedScreenId by shellState.selectedScreenId.collectAsState()
        val selectedNode = screenCatalog.findLeaf(selectedScreenId)

        Box(
            modifier = modifier.fillMaxSize().liquidGlassSurface(LiquidGlassWorkbenchDefaults.workspace),
            contentAlignment = Alignment.Center,
        ) {
            val content = selectedNode?.content
            if (content == null) {
                PlaceholderScreen("🧩", "这个功能页面暂时还没有挂载内容。")
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().contentPaddingFrame(),
                ) {
                    content()
                }
            }
        }
    }
}

/** 内容内边距：给业务页面留安全区，避免控件直接贴到玻璃边缘。 */
private fun Modifier.contentPaddingFrame(): Modifier {
    return fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)
}
