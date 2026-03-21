package site.addzero.liquiddemo

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import site.addzero.appsidebar.AppSidebarItem

internal enum class SidebarDemoShell {
    Workbench,
    AdminWorkbench,
}

internal data class SidebarDemoScene(
    val id: String,
    val title: String,
    val subtitle: String,
    val items: List<AppSidebarItem>,
    val initialSelectedId: String,
    val shell: SidebarDemoShell = SidebarDemoShell.Workbench,
    val headerSlot: @Composable ColumnScope.() -> Unit = {},
    val footerSlot: @Composable ColumnScope.() -> Unit = {},
    val detail: @Composable ColumnScope.(AppSidebarItem?) -> Unit,
)
