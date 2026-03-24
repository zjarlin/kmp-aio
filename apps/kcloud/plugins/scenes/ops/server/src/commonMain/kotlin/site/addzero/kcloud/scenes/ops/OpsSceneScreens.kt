package site.addzero.kcloud.scenes.ops

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

object OpsSceneMenus {
    const val SERVERS = "ops.servers"
    const val SETTINGS = "ops.settings"
}

val opsScenePages = listOf(
    KCloudScenePageDto(
        pageId = OpsSceneMenus.SERVERS,
        title = "服务器台账",
        path = "/ops/servers",
    ),
    KCloudScenePageDto(
        pageId = OpsSceneMenus.SETTINGS,
        title = "运维设置",
        path = "/ops/settings",
    ),
)

val opsSceneScreens: List<Screen> = listOf(
    OpsLeafScreen(
        id = OpsSceneMenus.SERVERS,
        name = "服务器台账",
        sort = 0,
        summary = "运维场景后续会继续吸收服务器、容器、环境管理等能力，这里先保留占位壳层。",
    ),
    OpsLeafScreen(
        id = OpsSceneMenus.SETTINGS,
        name = "运维设置",
        sort = 1,
        summary = "作为现阶段的设置与运维入口，用于承接系统配置和后续 Agent 运维接入。",
    ),
)

private data class OpsLeafScreen(
    override val id: String,
    override val name: String,
    override val sort: Int,
    private val summary: String,
) : Screen {
    override val pid: String = KCloudScreenRoots.OPS
    override val keywords: List<String> = listOf("ops", name)
    override val content: @Composable () -> Unit = {
        OpsPlaceholder(title = name, summary = summary)
    }
}

@Composable
private fun OpsPlaceholder(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ops",
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
