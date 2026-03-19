package com.kcloud.features.webdav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.webdav.ui.WebDavWorkspaceScreen
import org.koin.core.annotation.Single

object WebDavFeatureMenus {
    const val WEBDAV = "webdav"
}

@Single(binds = [KCloudFeature::class])
class WebDavFeature : KCloudFeature {
    override val featureId = "webdav"
    override val order = 70
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = WebDavFeatureMenus.WEBDAV,
            title = "WebDAV",
            icon = Icons.Default.Cloud,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 70,
            content = { WebDavWorkspaceScreen() }
        )
    )
}
