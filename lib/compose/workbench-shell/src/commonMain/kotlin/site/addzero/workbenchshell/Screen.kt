package site.addzero.workbenchshell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface Screen {
    val id: String
    val pid: String?
        get() = null
    val name: String
    val icon: ImageVector?
        get() = Icons.Default.Apps
    val sort: Int
        get() = Int.MAX_VALUE
    val visible: Boolean
        get() = true
    val keywords: List<String>
        get() = emptyList()
    val badge: String?
        get() = null
    val content: (@Composable () -> Unit)?
        get() = null
}
