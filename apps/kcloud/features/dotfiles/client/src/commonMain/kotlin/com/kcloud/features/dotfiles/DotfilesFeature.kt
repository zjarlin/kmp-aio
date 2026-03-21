package com.kcloud.features.dotfiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.dotfiles.ui.DotfilesScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object DotfilesFeatureMenus {
    const val DOTFILES = "dotfiles"
}

@Single
class DotfilesFeature : Screen {
    override val id = DotfilesFeatureMenus.DOTFILES
    override val pid = KCloudScreenRoots.SYSTEM
    override val name = "Dotfiles"
    override val icon = Icons.Default.Description
    override val sort = 80
    override val content = {
        DotfilesScreen()
    }
}
