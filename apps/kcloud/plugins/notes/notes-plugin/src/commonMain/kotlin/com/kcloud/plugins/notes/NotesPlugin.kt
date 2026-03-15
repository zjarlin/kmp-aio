package com.kcloud.plugins.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugins.notes.ui.KCloudNotesScreen
import org.koin.core.annotation.Single

@Single
class NotesPlugin : KCloudPlugin {
    override val pluginId = "notes-plugin"
    override val order = 35
    override val menuEntries = listOf(
        KCloudMenuEntry(
            id = NotesPluginMenus.NOTES,
            title = "笔记",
            icon = Icons.Default.EditNote,
            parentId = KCloudMenuGroups.MANAGEMENT,
            sortOrder = 35,
            content = { KCloudNotesScreen() }
        )
    )
}
