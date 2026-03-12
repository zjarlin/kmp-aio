package com.kcloud.plugins.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import com.kcloud.plugin.KCloudMenuEntry
import com.kcloud.plugin.KCloudMenuGroups
import com.kcloud.plugin.KCloudPlugin
import com.kcloud.plugin.KCloudPluginBundle
import com.kcloud.ui.screens.KCloudNotesScreen
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

object NotesPluginMenus {
    const val NOTES = "notes"
}

private val notesPluginModule = module {
    singleOf(::NotesPlugin) withOptions {
        bind<KCloudPlugin>()
    }
}

object NotesPluginBundle : KCloudPluginBundle {
    override val koinModules = listOf(notesPluginModule)
}

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
