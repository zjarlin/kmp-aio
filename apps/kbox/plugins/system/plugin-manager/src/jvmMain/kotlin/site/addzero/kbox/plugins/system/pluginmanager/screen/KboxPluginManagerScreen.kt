package site.addzero.kbox.plugins.system.pluginmanager.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kbox.plugin.api.KboxInstalledPluginSnapshot
import site.addzero.kbox.plugins.system.pluginmanager.KboxPluginManagerState

@Route(
    value = "插件运行时",
    title = "插件管理",
    routePath = "system/plugin-manager",
    icon = "Extension",
    order = 100.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
        defaultInScene = false,
    ),
)
@Composable
fun KboxPluginManagerScreen(
    modifier: Modifier = Modifier,
) {
    val state = remember {
        KoinPlatform.getKoin().get<KboxPluginManagerState>()
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state.refresh()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "运行时插件管理",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "安装目录包后可直接热加载到当前工作台。目录格式固定为 plugin.json + lib/*.jar。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                OutlinedTextField(
                    value = state.installSourceDir,
                    onValueChange = { value -> state.installSourceDir = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("待安装插件目录") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { state.installFromDirectory() } },
                        enabled = !state.isBusy,
                    ) {
                        Text("安装并加载")
                    }
                    Button(
                        onClick = { scope.launch { state.refresh() } },
                        enabled = !state.isBusy,
                    ) {
                        Text("刷新列表")
                    }
                }
                Text(
                    text = state.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.statusIsError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                )
            }
        }
        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PluginListPanel(
                plugins = state.plugins,
                selectedPluginId = state.selectedPluginId,
                onSelect = state::selectPlugin,
                modifier = Modifier.width(340.dp).fillMaxHeight(),
            )
            PluginDetailPanel(
                plugin = state.selectedPlugin,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onEnable = { scope.launch { state.enableSelected() } },
                onDisable = { scope.launch { state.disableSelected() } },
                onUninstall = { scope.launch { state.uninstallSelected() } },
                busy = state.isBusy,
            )
        }
    }
}

@Composable
private fun PluginListPanel(
    plugins: List<KboxInstalledPluginSnapshot>,
    selectedPluginId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "已安装插件",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(plugins, key = { plugin -> plugin.pluginId }) { plugin ->
                    val selected = plugin.pluginId == selectedPluginId
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                },
                            )
                            .clickable { onSelect(plugin.pluginId) }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(plugin.name, fontWeight = FontWeight.Medium)
                        Text(
                            "${plugin.pluginId} · ${plugin.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            plugin.state.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginDetailPanel(
    plugin: KboxInstalledPluginSnapshot?,
    modifier: Modifier,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onUninstall: () -> Unit,
    busy: Boolean,
) {
    Card(modifier = modifier) {
        if (plugin == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("请选择一个插件")
            }
            return@Card
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(plugin.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            DetailLine("插件 ID", plugin.pluginId)
            DetailLine("版本", plugin.version)
            DetailLine("状态", plugin.state.name)
            DetailLine("目录", plugin.pluginDir)
            DetailLine("页面能力", if (plugin.hasScreen) "有" else "无")
            if (plugin.lastError.isNotBlank()) {
                DetailLine("最近错误", plugin.lastError)
            }
            Divider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEnable, enabled = !busy) {
                    Text("启用")
                }
                Button(onClick = onDisable, enabled = !busy) {
                    Text("停用")
                }
                Button(onClick = onUninstall, enabled = !busy) {
                    Text("卸载")
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(
            value.ifBlank { "-" },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}
