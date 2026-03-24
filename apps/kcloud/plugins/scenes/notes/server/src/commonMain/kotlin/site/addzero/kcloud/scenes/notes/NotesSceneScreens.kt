package site.addzero.kcloud.scenes.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.feature.KCloudScenePageDto
import site.addzero.kcloud.feature.KCloudScreenRoots
import site.addzero.workbenchshell.Screen

object NotesSceneMenus {
    const val OVERVIEW = "notes.overview"
    const val COLLECTIONS = "notes.collections"
}

val notesScenePages = listOf(
    KCloudScenePageDto(
        pageId = NotesSceneMenus.OVERVIEW,
        title = "笔记总览",
        path = "/notes/overview",
    ),
    KCloudScenePageDto(
        pageId = NotesSceneMenus.COLLECTIONS,
        title = "笔记库",
        path = "/notes/collections",
    ),
)

val notesSceneScreens: List<Screen> = listOf(
    NotesLeafScreen(
        id = NotesSceneMenus.OVERVIEW,
        name = "笔记总览",
        sort = 0,
        summary = "这里保留笔记场景入口，后续可以把本地笔记、知识卡和同步适配器继续收进来。",
    ),
    NotesLeafScreen(
        id = NotesSceneMenus.COLLECTIONS,
        name = "笔记库",
        sort = 1,
        summary = "先用占位页面验证多级菜单与场景切换，后续再接真实笔记集合接口。",
    ),
)

private data class NotesLeafScreen(
    override val id: String,
    override val name: String,
    override val sort: Int,
    private val summary: String,
) : Screen {
    override val pid: String = KCloudScreenRoots.NOTES
    override val keywords: List<String> = listOf("notes", name)
    override val content: @Composable () -> Unit = {
        NotesPlaceholder(title = name, summary = summary)
    }
}

@Composable
private fun NotesPlaceholder(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            ),
        ) {
            Text(
                text = summary,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
