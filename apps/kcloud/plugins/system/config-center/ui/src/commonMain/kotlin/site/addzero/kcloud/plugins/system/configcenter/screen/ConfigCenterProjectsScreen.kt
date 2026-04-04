package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.workbench.design.button.WorkbenchButton as Button

@Route(
    value = "配置中心",
    title = "配置项",
    routePath = "system/config-center/value",
    icon = "Key",
    order = 80.0,
    enabled = true,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "配置中心",
            icon = "Key",
            order = 90,
        ),
    ),
)
@Composable
fun ConfigCenterProjectsScreen() {
    val viewModel: ConfigCenterProjectsViewModel = koinViewModel()
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    ConfigCenterWorkspaceFrame(
        title = "配置项",
        state = state,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPane(
                title = "键",
                modifier = Modifier.weight(0.9f).fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "AI 快捷配置",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.namespace == "kcloud.ai",
                            onClick = { state.useAiNamespace() },
                            label = { Text("kcloud.ai") },
                        )
                        FilterChip(
                            selected = state.key == "provider" && state.value == "openai",
                            onClick = { state.applyAiProviderPreset("openai") },
                            label = { Text("OpenAI") },
                        )
                        FilterChip(
                            selected = state.key == "provider" && state.value == "anthropic",
                            onClick = { state.applyAiProviderPreset("anthropic") },
                            label = { Text("Anthropic") },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.key == "apiUrl" && state.value == "https://api.openai.com",
                            onClick = { state.applyAiUrlPreset("openai") },
                            label = { Text("OpenAI URL") },
                        )
                        FilterChip(
                            selected = state.key == "apiUrl" && state.value == "https://api.anthropic.com",
                            onClick = { state.applyAiUrlPreset("anthropic") },
                            label = { Text("Anthropic URL") },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("apiKey", "apiUrl", "provider", "model", "systemPrompt", "transport").forEach { preset ->
                            FilterChip(
                                selected = state.key == preset,
                                onClick = { state.applyAiKeyPreset(preset) },
                                label = { Text(preset) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.namespace,
                        onValueChange = { state.namespace = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Namespace") },
                    )
                    OutlinedTextField(
                        value = state.key,
                        onValueChange = { state.key = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Key") },
                    )
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.active == "dev",
                            onClick = { state.active = "dev" },
                            label = { Text("dev") },
                        )
                        FilterChip(
                            selected = state.active == "prod",
                            onClick = { state.active = "prod" },
                            label = { Text("prod") },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    state.readValue()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("读取")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    state.writeValue()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("写入")
                        }
                    }
                }
            }

            ConfigCenterPane(
                title = "值",
                modifier = Modifier.weight(1.25f).fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CodeArea(
                        value = state.value,
                        onValueChange = { state.value = it },
                        modifier = Modifier.fillMaxWidth().height(320.dp),
                        label = "Value",
                    )
                    Text(
                        text = state.updateTimeMillis
                            ?.let { millis -> "最近更新时间: $millis" }
                            ?: "最近更新时间: -",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
