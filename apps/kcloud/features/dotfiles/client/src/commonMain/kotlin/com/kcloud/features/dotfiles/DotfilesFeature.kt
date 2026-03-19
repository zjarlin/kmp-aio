package com.kcloud.features.dotfiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.dotfiles.ui.DotfilesScreen
import org.koin.core.annotation.Single

object DotfilesFeatureMenus {
    const val DOTFILES = "dotfiles"
}

@Single(binds = [KCloudFeature::class])
class DotfilesFeature : KCloudFeature {
    override val featureId = "dotfiles"
    override val order = 80
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = DotfilesFeatureMenus.DOTFILES,
            title = "Dotfiles",
            icon = Icons.Default.Description,
            parentId = KCloudMenuGroups.SYSTEM,
            sortOrder = 80,
            content = { DotfilesScreen() }
        )
    )
}
