package site.addzero.kcloud.scenes.secondbrain

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

object SecondBrainSceneMenus {
    const val ASSETS = "second-brain.assets"
    const val ARCHIVE = "second-brain.archive"
}

val secondBrainScenePages = listOf(
    KCloudScenePageDto(
        pageId = SecondBrainSceneMenus.ASSETS,
        title = "资料资产",
        path = "/second-brain/assets",
    ),
    KCloudScenePageDto(
        pageId = SecondBrainSceneMenus.ARCHIVE,
        title = "归档仓库",
        path = "/second-brain/archive",
    ),
)

val secondBrainSceneScreens: List<Screen> = listOf(
    SecondBrainLeafScreen(
        id = SecondBrainSceneMenus.ASSETS,
        name = "资料资产",
        sort = 0,
        summary = "第二大脑场景先承接资产、素材、文档归档等能力的 Web 壳层入口。",
    ),
    SecondBrainLeafScreen(
        id = SecondBrainSceneMenus.ARCHIVE,
        name = "归档仓库",
        sort = 1,
        summary = "后续可在这里继续接入包归档、资源检索和知识归档能力。",
    ),
)

private data class SecondBrainLeafScreen(
    override val id: String,
    override val name: String,
    override val sort: Int,
    private val summary: String,
) : Screen {
    override val pid: String = KCloudScreenRoots.SECOND_BRAIN
    override val keywords: List<String> = listOf("second-brain", name)
    override val content: @Composable () -> Unit = {
        SecondBrainPlaceholder(title = name, summary = summary)
    }
}

@Composable
private fun SecondBrainPlaceholder(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Second Brain",
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
