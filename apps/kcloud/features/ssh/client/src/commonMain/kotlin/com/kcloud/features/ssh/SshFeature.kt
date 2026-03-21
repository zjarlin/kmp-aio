package com.kcloud.features.ssh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.ssh.ui.SshWorkspaceScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object SshFeatureMenus {
    const val SSH = "ssh"
}

@Single
class SshFeature : Screen {
    override val id = SshFeatureMenus.SSH
    override val pid = KCloudScreenRoots.MANAGEMENT
    override val name = "SSH 连接"
    override val icon = Icons.Default.Terminal
    override val sort = 60
    override val content = {
        SshWorkspaceScreen()
    }
}
