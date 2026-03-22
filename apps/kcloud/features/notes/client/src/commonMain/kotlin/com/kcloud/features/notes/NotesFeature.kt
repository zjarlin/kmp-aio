package com.kcloud.features.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.runtime.Composable
import com.kcloud.feature.KCloudScreenRoots
import com.kcloud.features.notes.ui.KCloudNotesScreen
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen

@Single
class NotesFeature : Screen {
    override val id = NotesFeatureMenus.NOTES
    override val pid = KCloudScreenRoots.NOTES
    override val name = "笔记"
    override val icon = Icons.Default.EditNote
    override val sort = 35
    override val content: (@Composable () -> Unit) = {
        KCloudNotesScreen()
    }
}
