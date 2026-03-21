package com.kcloud.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import site.addzero.workbenchshell.Screen
import com.kcloud.feature.KCloudScreenRoots

internal fun kCloudShellRootScreens(): List<Screen> {
    return listOf(
        KCloudRootScreen(
            id = KCloudScreenRoots.SYNC,
            name = "同步",
            icon = Icons.Default.Sync,
            sort = 0,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.MANAGEMENT,
            name = "管理",
            icon = Icons.Default.Folder,
            sort = 1,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.SYSTEM,
            name = "系统",
            icon = Icons.Default.Settings,
            sort = 2,
        ),
    )
}

private data class KCloudRootScreen(
    override val id: String,
    override val name: String,
    override val icon: androidx.compose.ui.graphics.vector.ImageVector,
    override val sort: Int,
) : Screen
