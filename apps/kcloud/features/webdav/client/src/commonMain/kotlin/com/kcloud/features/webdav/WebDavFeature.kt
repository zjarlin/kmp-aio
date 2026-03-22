package com.kcloud.features.webdav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.webdav.ui.WebDavWorkspaceScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

object WebDavFeatureMenus {
    const val WEBDAV = "webdav"
}

@Single
class WebDavFeature : Screen {
    override val id = WebDavFeatureMenus.WEBDAV
    override val pid = KCloudScreenRoots.WORKSPACE
    override val name = "WebDAV"
    override val icon = Icons.Default.Cloud
    override val sort = 70
    override val content: (@Composable () -> Unit) = {
        WebDavWorkspaceScreen()
    }
}
