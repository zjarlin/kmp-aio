package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.workbenchshell.ScreenNode

@Serializable
data class SidebarShowcaseSceneConfig(
    val sceneId: String = "",
    val subtitle: String = "",
    val shell: AppSidebarScaffoldShell = AppSidebarScaffoldShell.Workbench,
    val initialLeafId: String = "",
    val headerInfo: SidebarShowcaseInfoConfig = SidebarShowcaseInfoConfig(),
    val footerInfo: SidebarShowcaseInfoConfig = SidebarShowcaseInfoConfig(),
    val pagePrimaryActionLabel: String = "",
    val pageSecondaryActionLabel: String = "",
    val languageLabel: String = "简体中文",
    val userLabel: String = "demo@addzero.site",
    val notificationCount: Int = 0,
    val isDarkTheme: Boolean = true,
    val badges: Map<String, String> = emptyMap(),
)

@Serializable
data class SidebarShowcaseInfoConfig(
    val title: String = "",
    val value: String = "",
)

@Serializable
data class SidebarShowcaseDetailConfig(
    val title: String = "",
    val summary: String = "",
    val facts: List<SidebarShowcaseFactConfig> = emptyList(),
    val tasks: List<String> = emptyList(),
)

@Serializable
data class SidebarShowcaseFactConfig(
    val label: String = "",
    val value: String = "",
)

interface SidebarShowcaseSlot {
    val config: SidebarShowcaseSceneConfig
    val details: Map<String, SidebarShowcaseDetailConfig>

    @Composable
    fun ColumnScope.SidebarHeader() {
        config.headerInfo.takeIf { info ->
            info.title.isNotBlank() || info.value.isNotBlank()
        }?.let { info ->
            ShowcaseSidebarInfo(
                title = info.title,
                value = info.value,
            )
        }
    }

    @Composable
    fun ColumnScope.SidebarFooter() {
        config.footerInfo.takeIf { info ->
            info.title.isNotBlank() || info.value.isNotBlank()
        }?.let { info ->
            ShowcaseSidebarInfo(
                title = info.title,
                value = info.value,
            )
        }
    }

    @Composable
    fun RowScope.PageActions() {
        if (config.pagePrimaryActionLabel.isNotBlank()) {
            Button(onClick = {}) {
                Text(config.pagePrimaryActionLabel)
            }
        }
        if (config.pageSecondaryActionLabel.isNotBlank()) {
            OutlinedButton(onClick = {}) {
                Text(config.pageSecondaryActionLabel)
            }
        }
    }

    @Composable
    fun RowScope.SidebarTrailing(
        node: ScreenNode,
        selected: Boolean,
        descendantSelected: Boolean,
    ) {
        val badge = config.badges[node.id] ?: return
        SidebarShowcaseBadge(
            badge = badge,
            emphasized = selected || descendantSelected,
        )
    }

    @Composable
    fun ColumnScope.Detail(
        node: ScreenNode,
    ) {
        val detail = details[node.id] ?: return
        ShowcaseInspector(
            title = detail.title,
            summary = detail.summary,
            facts = detail.facts.map { fact -> fact.label to fact.value },
            tasks = detail.tasks,
        )
    }
}

class DefaultSidebarShowcaseSlot(
    sceneId: String,
    sceneName: String,
) : SidebarShowcaseSlot {
    override val config = SidebarShowcaseSceneConfig(
        sceneId = sceneId,
        subtitle = "$sceneName 场景",
    )
    override val details: Map<String, SidebarShowcaseDetailConfig> = emptyMap()
}

@Composable
private fun RowScope.SidebarShowcaseBadge(
    badge: String,
    emphasized: Boolean,
) {
    Box(
        modifier = Modifier.background(
            color = if (emphasized) {
                Color(0xFF3A7BFF).copy(alpha = 0.28f)
            } else {
                Color.White.copy(alpha = 0.08f)
            },
            shape = CircleShape,
        ).padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            color = Color.White.copy(alpha = 0.96f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
