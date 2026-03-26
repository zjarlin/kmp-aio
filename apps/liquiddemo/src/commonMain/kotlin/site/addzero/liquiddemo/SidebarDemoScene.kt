package site.addzero.liquiddemo

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.workbenchshell.Screen

internal interface SidebarDemoSceneScreen : Screen {
    val shell: AppSidebarScaffoldShell
    val subtitle: String
    val initialLeafId: String
    val headerSlot: @Composable ColumnScope.() -> Unit
        get() = {}
    val footerSlot: @Composable ColumnScope.() -> Unit
        get() = {}
}

internal interface SidebarDemoLeafScreen : Screen {
    val detail: @Composable ColumnScope.() -> Unit
}

internal abstract class SidebarDemoSceneScreenBase(
    override val id: String,
    override val name: String,
    override val sort: Int,
    override val shell: AppSidebarScaffoldShell,
    override val subtitle: String,
    override val initialLeafId: String,
) : SidebarDemoSceneScreen {
    final override val pid: String? = null
}

internal abstract class SidebarDemoBranchScreenBase(
    final override val id: String,
    final override val pid: String,
    final override val name: String,
    final override val icon: ImageVector?,
    final override val sort: Int,
    final override val keywords: List<String> = emptyList(),
    final override val badge: String? = null,
) : Screen {
    final override val content: (@Composable () -> Unit)? = null
}

internal abstract class SidebarDemoLeafScreenBase(
    final override val id: String,
    final override val pid: String,
    final override val name: String,
    final override val icon: ImageVector?,
    final override val sort: Int,
    final override val keywords: List<String> = emptyList(),
    final override val badge: String? = null,
) : SidebarDemoLeafScreen {
    final override val content: (@Composable () -> Unit)? = {}
}

internal fun buildSidebarDemoScreenId(
    sceneId: String,
    vararg path: String,
): String {
    return (listOf(sceneId) + path.toList()).joinToString("/")
}
