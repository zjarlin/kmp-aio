package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.button.AddButton
import site.addzero.component.button.AddIconButton

@Route(
    value = "配置中心",
    title = "预览发布",
    routePath = "system/config-center/preview",
    icon = "Download",
    order = 82.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun ConfigCenterPreviewScreen() {
    val state = rememberConfigCenterWorkbenchState()
    val scope = rememberCoroutineScope()

    ConfigCenterPageFrame(
        title = "预览发布",
        state = state,
        onRefresh = {
            state.refreshTargets()
            state.loadBootstrapSummary()
        },
        searchBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AddButton(
                    displayName = "刷新预览",
                    icon = Icons.Default.Sync,
                    onClick = {
                        scope.launch { state.previewSelectedTarget() }
                    },
                )
                AddButton(
                    displayName = "导出",
                    icon = Icons.Default.Download,
                    onClick = {
                        scope.launch { state.exportSelectedTarget() }
                    },
                )
                AddIconButton(
                    text = "刷新 bootstrap",
                    imageVector = Icons.Default.Sync,
                ) {
                    scope.launch { state.loadBootstrapSummary() }
                }
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPanel(
                title = "目标",
                modifier = Modifier.weight(0.8f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.targets, key = { target -> target.id }) { target ->
                        ConfigCenterListItem(
                            selected = state.selectedTargetId == target.id,
                            title = target.name,
                            subtitle = target.outputPath,
                            trailing = target.targetKind.name,
                            onClick = {
                                state.selectTarget(target)
                                scope.launch { state.previewSelectedTarget() }
                            },
                        )
                    }
                }
            }
            ConfigCenterPanel(
                title = "内容",
                modifier = Modifier.weight(1.4f).fillMaxHeight(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PreviewTextArea(
                        value = state.previewText,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                }
            }
        }
    }
}
