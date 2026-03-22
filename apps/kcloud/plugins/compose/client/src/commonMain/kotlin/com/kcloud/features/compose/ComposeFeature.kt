package com.kcloud.features.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.compose.ui.ComposeManagerScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object ComposeFeatureMenus {
    const val COMPOSE_MANAGER = "compose-manager"
}

@Single
class ComposeFeature : Screen {
    override val id = ComposeFeatureMenus.COMPOSE_MANAGER
    override val pid = KCloudScreenRoots.OPS
    override val name = "Docker Compose"
    override val icon = Icons.Default.Dns
    override val sort = 25
    override val content: (@Composable () -> Unit) = {
        ComposeManagerScreen()
    }
}
