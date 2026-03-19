package com.kcloud.features.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import com.kcloud.feature.KCloudMenuEntry
import com.kcloud.feature.KCloudMenuGroups
import com.kcloud.feature.KCloudFeature
import com.kcloud.features.notes.ui.KCloudNotesScreen
import org.koin.core.annotation.Single

@Single(binds = [KCloudFeature::class])
class NotesFeature : KCloudFeature {
    override val featureId = "notes"
    override val order = 35
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = NotesFeatureMenus.NOTES,
            title = "笔记",
            icon = Icons.Default.EditNote,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 35,
            content = { KCloudNotesScreen() }
        )
    )
}
