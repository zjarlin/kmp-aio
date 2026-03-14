package com.kcloud.plugins.ssh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.ssh.ui.SshWorkspaceScreen
import org.koin.core.annotation.Single

object SshPluginMenus {
    const val SSH = "ssh"
}

@Single
class SshPlugin : KCloudPlugin {
    override val pluginId = "ssh-plugin"
    override val order = 60
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = SshPluginMenus.SSH,
            title = "SSH 连接",
            icon = Icons.Default.Terminal,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 60,
            content = { SshWorkspaceScreen() }
        )
    )
}
