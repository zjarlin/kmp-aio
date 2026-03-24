package site.addzero.kcloud.scenes.system

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

object SystemSceneMenus {
    const val RBAC = "system.rbac"
    const val ENVIRONMENT = "system.environment"
}

val systemScenePages = listOf(
    KCloudScenePageDto(
        pageId = SystemSceneMenus.RBAC,
        title = "权限与角色",
        path = "/system/rbac",
    ),
    KCloudScenePageDto(
        pageId = SystemSceneMenus.ENVIRONMENT,
        title = "运行环境",
        path = "/system/environment",
    ),
)

val systemSceneScreens: List<Screen> = listOf(
    SystemLeafScreen(
        id = SystemSceneMenus.RBAC,
        name = "权限与角色",
        sort = 0,
        summary = "系统场景保留基础权限与资源位，后续可继续接 RBAC 与系统级控制台。",
    ),
    SystemLeafScreen(
        id = SystemSceneMenus.ENVIRONMENT,
        name = "运行环境",
        sort = 1,
        summary = "这里先作为环境诊断和系统健康入口，后续再补完整运维与环境能力。",
    ),
)

private data class SystemLeafScreen(
    override val id: String,
    override val name: String,
    override val sort: Int,
    private val summary: String,
) : Screen {
    override val pid: String = KCloudScreenRoots.SYSTEM
    override val keywords: List<String> = listOf("system", name)
    override val content: @Composable () -> Unit = {
        SystemPlaceholder(title = name, summary = summary)
    }
}

@Composable
private fun SystemPlaceholder(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "System",
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
