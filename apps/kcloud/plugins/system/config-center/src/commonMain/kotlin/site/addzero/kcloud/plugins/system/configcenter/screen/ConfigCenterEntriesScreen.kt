package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.*
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
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.configcenter.spec.ConfigDomain
import site.addzero.configcenter.spec.ConfigStorageMode
import site.addzero.configcenter.spec.ConfigValueType

@Route(
    value = "配置中心",
    title = "配置项",
    routePath = "system/config-center/entries",
    icon = "Settings",
    order = 80.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun ConfigCenterEntriesScreen() {
    val state = rememberConfigCenterWorkbenchState()

    ConfigCenterPageFrame(
        title = "配置项",
        state = state,
        onRefresh = { state.refreshEntries() },
        onCreate = { state.beginCreateEntry() },
        onSave = { state.saveEntry() },
        onDelete = { state.deleteSelectedEntry() },
        searchBar = {
            ConfigCenterEntryFilters(
                state = state,
                onRefresh = { state.refreshEntries() },
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPanel(
                title = "记录",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.entries, key = { entry -> entry.id }) { entry ->
                        ConfigCenterListItem(
                            selected = state.selectedEntryId == entry.id,
                            title = entry.key,
                            subtitle = "${entry.namespace} | ${entry.storageMode}",
                            trailing = entry.profile,
                            onClick = { state.selectEntry(entry) },
                        )
                    }
                }
            }

            ConfigCenterPanel(
                title = "编辑",
                modifier = Modifier.weight(1.2f).fillMaxHeight(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.entryKey,
                        onValueChange = { state.entryKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Key") },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = state.entryNamespace,
                            onValueChange = { state.entryNamespace = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Namespace") },
                        )
                        OutlinedTextField(
                            value = state.entryProfile,
                            onValueChange = { state.entryProfile = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Profile") },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        EnumSelectRow(
                            label = "分类",
                            values = ConfigDomain.entries.toList(),
                            currentValue = state.entryDomain,
                            onValueChange = { state.entryDomain = it as ConfigDomain },
                        )
                        EnumSelectRow(
                            label = "值类型",
                            values = ConfigValueType.entries.toList(),
                            currentValue = state.entryValueType,
                            onValueChange = { state.entryValueType = it as ConfigValueType },
                        )
                        EnumSelectRow(
                            label = "存储方式",
                            values = ConfigStorageMode.entries.toList(),
                            currentValue = state.entryStorageMode,
                            onValueChange = { state.entryStorageMode = it as ConfigStorageMode },
                        )
                    }
                    OutlinedTextField(
                        value = state.entryValue,
                        onValueChange = { state.entryValue = it },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        label = { Text("Value") },
                    )
                    OutlinedTextField(
                        value = state.entryDescription,
                        onValueChange = { state.entryDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                    )
                    OutlinedTextField(
                        value = state.entryTagsText,
                        onValueChange = { state.entryTagsText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Tags") },
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
                            checked = state.entryEnabled,
                            onCheckedChange = { state.entryEnabled = it },
                        )
                    }
                }
            }
        }
    }
}
