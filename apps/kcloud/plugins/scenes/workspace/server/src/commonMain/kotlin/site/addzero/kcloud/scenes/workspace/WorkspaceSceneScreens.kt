package site.addzero.kcloud.scenes.workspace

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

object WorkspaceSceneMenus {
    const val OVERVIEW = "workspace.overview"
    const val SYNC_CENTER = "workspace.sync-center"
}

val workspaceScenePages = listOf(
    KCloudScenePageDto(
        pageId = WorkspaceSceneMenus.OVERVIEW,
        title = "工作总览",
        path = "/workspace/overview",
    ),
    KCloudScenePageDto(
        pageId = WorkspaceSceneMenus.SYNC_CENTER,
        title = "同步中心",
        path = "/workspace/sync-center",
    ),
)

val workspaceSceneScreens: List<Screen> = listOf(
    WorkspaceLeafScreen(
        id = WorkspaceSceneMenus.OVERVIEW,
        name = "工作总览",
        sort = 0,
        summary = "这里先放 KCloud Web/桌面共用的工作台入口，占位页用于验证顶部场景切换和左侧导航结构。",
    ),
    WorkspaceLeafScreen(
        id = WorkspaceSceneMenus.SYNC_CENTER,
        name = "同步中心",
        sort = 1,
        summary = "同步队列、任务状态和后续物联网设备同步入口会继续往这里收口。",
    ),
)

private data class WorkspaceLeafScreen(
    override val id: String,
    override val name: String,
    override val sort: Int,
    private val summary: String,
) : Screen {
    override val pid: String = KCloudScreenRoots.WORKSPACE
    override val keywords: List<String> = listOf("workspace", name)
    override val content: @Composable () -> Unit = {
        ScenePlaceholder(
            eyebrow = "Workspace",
            title = name,
            summary = summary,
        )
    }
}

@Composable
private fun ScenePlaceholder(
    eyebrow: String,
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = eyebrow,
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
