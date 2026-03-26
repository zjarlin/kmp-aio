package site.addzero.appsidebar

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class AppSidebarStyle {
    Default,
    FlushWorkbench,
}

@Immutable
@Serializable
data class AppSidebarConfig(
    val style: AppSidebarStyle = AppSidebarStyle.Default,
    val supportText: String? = null,
    val searchEnabled: Boolean = true,
    val searchPlaceholder: String = "搜索菜单",
)

@Immutable
data class AppSidebarEvents<T>(
    val onKeywordChange: (String) -> Unit = {},
    val onItemClick: (T) -> Unit = {},
)

data class AppSidebarSlots<T>(
    val header: @Composable ColumnScope.() -> Unit = {},
    val footer: @Composable ColumnScope.() -> Unit = {},
    val empty: @Composable ColumnScope.(String) -> Unit = { keyword ->
        DefaultSidebarEmpty(
            keyword = keyword,
        )
    },
    val leading: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
    val label: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
    val trailing: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
)
