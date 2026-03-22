package com.kcloud.features.rbac

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.rbac.ui.RbacScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

@Single
class RbacFeature : Screen {
    override val id = RbacFeatureMenus.RBAC
    override val pid = KCloudScreenRoots.SYSTEM
    override val name = "RBAC"
    override val icon = Icons.Default.AdminPanelSettings
    override val sort = 20
    override val content: (@Composable () -> Unit) = {
        RbacScreen()
    }
}
