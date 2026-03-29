package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueType

@Route(
    value = "配置中心",
    title = "Secret 管理",
    routePath = "system/config-center/secrets",
    icon = "Key",
    order = 81.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun ConfigCenterSecretsScreen() {
    val state = rememberConfigCenterWorkbenchState()
    val scope = rememberCoroutineScope()

    ConfigCenterWorkspaceFrame(
        title = "Secret 管理",
        state = state,
        onRefresh = { state.refreshSecrets() },
        actions = {
            OutlinedButton(onClick = { state.beginCreateSecret() }) {
                Text("新建 Secret")
            }
            FilterChip(
                selected = state.includeInheritedSecrets,
                onClick = {
                    scope.launch { state.toggleInheritedSecrets(!state.includeInheritedSecrets) }
                },
                label = { Text(if (state.includeInheritedSecrets) "显示继承" else "仅本地") },
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPane(
                title = "Secrets",
                modifier = Modifier.weight(0.95f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.secrets, key = { item -> item.id }) { secret ->
                        ConfigCenterSelectableItem(
                            selected = state.selectedSecretId == secret.id,
                            title = secret.name,
                            subtitle = "${secret.configName} | ${if (secret.sensitive) secret.maskedValue else secret.value}",
                            trailing = if (secret.inherited) "Inherited" else "Local",
                            onClick = {
                                scope.launch {
                                    state.selectConfig(secret.configId)
                                }
                            },
                        )
                    }
                }
            }

            ConfigCenterPane(
                title = "编辑器",
                modifier = Modifier.weight(1.15f).fillMaxHeight(),
            ) {
                ConfigCenterFormColumn(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = state.configs.firstOrNull { it.id == state.selectedConfigId }?.name
                            ?: "先选择一个 Config",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    OutlinedTextField(
                        value = state.secretName,
                        onValueChange = { state.secretName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Secret Name") },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.secretValueType == ConfigCenterValueType.STRING,
                            onClick = { state.secretValueType = ConfigCenterValueType.STRING },
                            label = { Text("String") },
                        )
                        FilterChip(
                            selected = state.secretValueType == ConfigCenterValueType.JSON,
                            onClick = { state.secretValueType = ConfigCenterValueType.JSON },
                            label = { Text("JSON") },
                        )
                        FilterChip(
                            selected = state.secretValueType == ConfigCenterValueType.TEXT,
                            onClick = { state.secretValueType = ConfigCenterValueType.TEXT },
                            label = { Text("Text") },
                        )
                    }
                    CodeArea(
                        value = state.secretValue,
                        onValueChange = { state.secretValue = it },
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        label = "Secret Value",
                    )
                    OutlinedTextField(
                        value = state.secretNote,
                        onValueChange = { state.secretNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note") },
                    )
                    CompactSwitchRow(
                        label = "敏感值",
                        checked = state.secretSensitive,
                        onCheckedChange = { state.secretSensitive = it },
                    )
                    CompactSwitchRow(
                        label = "启用",
                        checked = state.secretEnabled,
                        onCheckedChange = { state.secretEnabled = it },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { scope.launch { state.saveSecret() } },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("保存")
                        }
                        OutlinedButton(
                            onClick = { scope.launch { state.deleteSelectedSecret() } },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("删除")
                        }
                    }
                }
            }

            ConfigCenterPane(
                title = "版本历史",
                modifier = Modifier.weight(0.9f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.versions, key = { item -> item.id }) { version ->
                        ConfigCenterSelectableItem(
                            selected = false,
                            title = "v${version.version} ${version.action.name}",
                            subtitle = version.note.orEmpty().ifBlank { version.maskedValue },
                            trailing = version.actor,
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}
