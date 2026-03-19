package com.kcloud.features.ssh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.ssh.ui.SshWorkspaceScreen
import org.koin.core.annotation.Single

object SshFeatureMenus {
    const val SSH = "ssh"
}

@Single(binds = [KCloudFeature::class])
class SshFeature : KCloudFeature {
    override val featureId = "ssh"
    override val order = 60
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = SshFeatureMenus.SSH,
            title = "SSH 连接",
            icon = Icons.Default.Terminal,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 60,
            content = { SshWorkspaceScreen() }
        )
    )
}
