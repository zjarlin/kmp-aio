package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.configcenter.spec.ConfigTargetKind

@Route(
    value = "配置中心",
    title = "渲染目标",
    routePath = "system/config-center/targets",
    icon = "Tune",
    order = 81.0,
)
@Composable
fun ConfigCenterTargetsScreen() {
    val state = rememberConfigCenterWorkbenchState()

    ConfigCenterPageFrame(
        title = "渲染目标",
        state = state,
        onRefresh = { state.refreshTargets() },
        onCreate = { state.beginCreateTarget() },
        onSave = { state.saveTarget() },
        onDelete = { state.deleteSelectedTarget() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPanel(
                title = "目标列表",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.targets, key = { target -> target.id }) { target ->
                        ConfigCenterListItem(
                            selected = state.selectedTargetId == target.id,
                            title = target.name,
                            subtitle = "${target.targetKind} | ${target.namespaceFilter.orEmpty()}",
                            trailing = target.profile,
                            onClick = { state.selectTarget(target) },
                        )
                    }
                }
            }

            ConfigCenterPanel(
                title = "目标编辑",
                modifier = Modifier.weight(1.2f).fillMaxHeight(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.targetName,
                        onValueChange = { state.targetName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("名称") },
                    )
                    EnumSelectRow(
                        label = "目标类型",
                        values = ConfigTargetKind.entries.toList(),
                        currentValue = state.targetKind,
                        onValueChange = { state.targetKind = it as ConfigTargetKind },
                    )
                    OutlinedTextField(
                        value = state.targetOutputPath,
                        onValueChange = { state.targetOutputPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("输出路径") },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = state.targetNamespaceFilter,
                            onValueChange = { state.targetNamespaceFilter = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("命名空间过滤") },
                        )
                        OutlinedTextField(
                            value = state.targetProfile,
                            onValueChange = { state.targetProfile = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Profile") },
                        )
                    }
                    OutlinedTextField(
                        value = state.targetSortOrderText,
                        onValueChange = { state.targetSortOrderText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("排序") },
                    )
                    OutlinedTextField(
                        value = state.targetTemplateText,
                        onValueChange = { state.targetTemplateText = it },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        label = { Text("模板") },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "启用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked = state.targetEnabled,
                            onCheckedChange = { state.targetEnabled = it },
                        )
                    }
                }
            }
        }
    }
}
