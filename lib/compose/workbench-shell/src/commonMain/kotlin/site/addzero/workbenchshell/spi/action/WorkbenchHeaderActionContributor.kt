package site.addzero.workbenchshell.spi.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class WorkbenchHeaderActionPlacement {
    PAGE,
    UTILITY,
}

interface WorkbenchHeaderActionContributor {
    val id: String
    val label: String
    val icon: ImageVector?
        get() = null
    val placement: WorkbenchHeaderActionPlacement
        get() = WorkbenchHeaderActionPlacement.UTILITY
    val order: Int
        get() = Int.MAX_VALUE
    val visible: Boolean
        get() = true

    @Composable
    fun RowScope.Render()
}

@Composable
fun RowScope.RenderWorkbenchHeaderActions(
    contributors: List<WorkbenchHeaderActionContributor>,
    placement: WorkbenchHeaderActionPlacement,
    modifier: Modifier = Modifier,
) {
    val visibleContributors = contributors
        .asSequence()
        .filter { contributor -> contributor.visible && contributor.placement == placement }
        .sortedWith(
            compareBy<WorkbenchHeaderActionContributor> { contributor -> contributor.order }
                .thenBy { contributor -> contributor.label },
        )
        .toList()

    if (visibleContributors.isEmpty()) {
        return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleContributors.forEach { contributor ->
            with(contributor) {
                Render()
            }
        }
    }
}
