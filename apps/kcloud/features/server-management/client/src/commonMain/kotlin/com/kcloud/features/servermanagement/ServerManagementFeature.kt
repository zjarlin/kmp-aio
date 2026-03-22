package com.kcloud.features.servermanagement

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.servermanagement.ui.ServerManagementScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object ServerManagementFeatureMenus {
    const val SERVER_MANAGEMENT = "server-management"
}

@Single
class ServerManagementFeature : Screen {
    override val id = ServerManagementFeatureMenus.SERVER_MANAGEMENT
    override val pid = KCloudScreenRoots.OPS
    override val name = "服务器管理"
    override val icon = Icons.Default.AccountBox
    override val sort = 20
    override val content: (@Composable () -> Unit) = {
        ServerManagementScreen()
    }
}
