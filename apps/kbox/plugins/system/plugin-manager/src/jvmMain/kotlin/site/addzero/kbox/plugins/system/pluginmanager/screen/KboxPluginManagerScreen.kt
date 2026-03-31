package site.addzero.kbox.plugins.system.pluginmanager.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
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
    icon = "Extension",
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
        ),
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Plugin runtime",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Install runtime plugins from a folder, inspect their current state, and enable or disable them without leaving the workbench.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    PluginStatePill(
                        text = when {
                            state.isBusy -> "Refreshing"
                            state.statusIsError -> "Issue detected"
                            else -> "Stable"
                        },
                        accent = when {
                            state.statusIsError -> MaterialTheme.colorScheme.errorContainer
                            state.isBusy -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        contentColor = when {
                            state.statusIsError -> MaterialTheme.colorScheme.onErrorContainer
                            state.isBusy -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PluginMetricCard(
                        title = "Installed",
                        value = state.plugins.size.toString(),
                        detail = "Discovered plugin folders",
                        modifier = Modifier.weight(1f),
                    )
                    PluginMetricCard(
                        title = "Selected",
                        value = state.selectedPlugin?.name ?: "-",
                        detail = state.selectedPlugin?.state?.name ?: "No plugin selected",
                        modifier = Modifier.weight(1.3f),
                        accent = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    )
                    PluginMetricCard(
                        title = "Install source",
                        value = if (state.installSourceDir.isBlank()) "-" else "Ready",
                        detail = if (state.installSourceDir.isBlank()) {
                            "Choose a folder to install from"
                        } else {
                            state.installSourceDir
                        },
                        modifier = Modifier.weight(1.6f),
                    )
                }

                OutlinedTextField(
                    value = state.installSourceDir,
                    onValueChange = { value -> state.installSourceDir = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Plugin install directory") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { state.installFromDirectory() } },
                        enabled = !state.isBusy,
                    ) {
                        Text("Install and load")
                    }
                    Button(
                        onClick = { scope.launch { state.refresh() } },
                        enabled = !state.isBusy,
                    ) {
                        Text("Refresh list")
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
                modifier = Modifier.width(360.dp).fillMaxHeight(),
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Installed plugins",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Select a plugin to inspect its runtime state, capabilities, and installation directory.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(plugins, key = { plugin -> plugin.pluginId }) { plugin ->
                    val selected = plugin.pluginId == selectedPluginId
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
                                },
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(plugin.pluginId) }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(plugin.name, fontWeight = FontWeight.Medium)
                            PluginStatePill(
                                text = plugin.state.name,
                                accent = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "${plugin.pluginId} / ${plugin.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            fontFamily = FontFamily.Monospace,
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (plugin == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Select a plugin to inspect its runtime details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Card
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = plugin.pluginId,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PluginStatePill(
                    text = plugin.state.name,
                    accent = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            PluginInspectorCard(
                title = "Runtime details",
            ) {
                DetailLine("Version", plugin.version)
                DetailLine("Plugin directory", plugin.pluginDir)
                DetailLine("Screen capability", if (plugin.hasScreen) "Available" else "Not provided")
                if (plugin.lastError.isNotBlank()) {
                    DetailLine("Last error", plugin.lastError)
                }
            }

            PluginInspectorCard(
                title = "Actions",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEnable, enabled = !busy) {
                        Text("Enable")
                    }
                    Button(onClick = onDisable, enabled = !busy) {
                        Text("Disable")
                    }
                    Button(onClick = onUninstall, enabled = !busy) {
                        Text("Uninstall")
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginInspectorCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
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

@Composable
private fun PluginMetricCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun PluginStatePill(
    text: String,
    accent: Color,
    contentColor: Color,
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}
