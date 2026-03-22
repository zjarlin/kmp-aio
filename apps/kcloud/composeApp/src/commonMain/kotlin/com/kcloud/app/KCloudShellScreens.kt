package com.kcloud.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import site.addzero.workbenchshell.Screen
import com.kcloud.feature.KCloudScreenRoots

internal fun kCloudShellRootScreens(): List<Screen> {
    val listOf = listOf(
        KCloudRootScreen(
            id = KCloudScreenRoots.WORKSPACE,
            name = "工作台",
            icon = Icons.Default.Sync,
            sort = 0,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.NOTES,
            name = "笔记",
            icon = Icons.Default.EditNote,
            sort = 1,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.SECOND_BRAIN,
            name = "第二大脑",
            icon = Icons.Default.Inventory2,
            sort = 2,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.OPS,
            name = "运维",
            icon = Icons.Default.Dns,
            sort = 3,
        ),
        KCloudRootScreen(
            id = KCloudScreenRoots.SYSTEM,
            name = "系统",
            icon = Icons.Default.Settings,
            sort = 4,
        ),
    )
    return listOf
}

private data class KCloudRootScreen(
    override val id: String,
    override val name: String,
    override val icon: ImageVector,
    override val sort: Int,
) : Screen
