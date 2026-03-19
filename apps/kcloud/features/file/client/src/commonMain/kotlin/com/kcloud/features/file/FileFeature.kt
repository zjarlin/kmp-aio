package com.kcloud.features.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.file.ui.FileManagerScreen
import org.koin.core.annotation.Single

@Single(binds = [KCloudFeature::class])
class FileFeature : KCloudFeature {
    override val featureId = "file"
    override val order = 30
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = FileFeatureMenus.FILE_MANAGER,
            title = "文件管理",
            icon = Icons.Default.Folder,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 30,
            content = { FileManagerScreen() }
        )
    )
}
