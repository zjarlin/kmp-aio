package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
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

@Route(
    value = "配置中心",
    title = "配置项",
    routePath = "system/config-center/value",
    icon = "Key",
    order = 80.0,
    enabled = true,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统管理",
            icon = "AdminPanelSettings",
            order = 100,
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
