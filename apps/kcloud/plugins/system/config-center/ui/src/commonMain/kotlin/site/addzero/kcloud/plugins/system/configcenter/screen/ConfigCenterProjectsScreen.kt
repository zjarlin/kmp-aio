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
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigType

@Route(
    value = "配置中心",
    title = "项目与环境",
    routePath = "system/config-center/projects",
    icon = "Hub",
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
fun ConfigCenterProjectsScreen() {
    val state = rememberConfigCenterWorkbenchState()
    val scope = rememberCoroutineScope()

    ConfigCenterWorkspaceFrame(
        title = "项目与环境",
        state = state,
        onRefresh = { state.refreshProjects() },
        actions = {
            OutlinedButton(onClick = { state.beginCreateProject() }) {
                Text("新建项目")
            }
            OutlinedButton(onClick = { state.beginCreateEnvironment() }) {
                Text("新建环境")
            }
            OutlinedButton(onClick = { state.beginCreateConfig() }) {
                Text("新建 Config")
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPane(
                title = "项目",
                modifier = Modifier.weight(0.8f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.projects, key = { item -> item.id }) { project ->
                        ConfigCenterSelectableItem(
                            selected = state.selectedProjectId == project.id,
                            title = project.name,
                            subtitle = "${project.slug} | ${project.environmentCount} env | ${project.secretCount} secrets",
                            trailing = if (project.enabled) "ACTIVE" else "PAUSED",
                            onClick = {
                                scope.launch { state.selectProject(project.id) }
                            },
                        )
                    }
                }
            }

            ConfigCenterPane(
                title = "环境与 Config",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("环境", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        modifier = Modifier.weight(0.46f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.environments, key = { item -> item.id }) { environment ->
                            ConfigCenterSelectableItem(
                                selected = state.selectedEnvironmentId == environment.id,
                                title = environment.name,
                                subtitle = "${environment.slug} | 缺失 ${environment.missingSecretCount} 项",
                                trailing = if (environment.personalConfigEnabled) "Personal On" else "Root Only",
                                onClick = {
                                    state.selectedEnvironmentId = environment.id
                                    state.environmentSlug = environment.slug
                                    state.environmentName = environment.name
                                    state.environmentDescription = environment.description.orEmpty()
                                    state.environmentSortOrderText = environment.sortOrder.toString()
                                    state.environmentIsDefault = environment.isDefault
                                    state.environmentPersonalEnabled = environment.personalConfigEnabled
                                },
                            )
                        }
                    }
                    Text("Configs", style = MaterialTheme.typography.titleSmall)
                    LazyColumn(
                        modifier = Modifier.weight(0.54f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.configs, key = { item -> item.id }) { config ->
                            ConfigCenterSelectableItem(
                                selected = state.selectedConfigId == config.id,
                                title = config.name,
                                subtitle = "${config.slug} | ${config.secretCount} local + ${config.inheritedSecretCount} inherited",
                                trailing = config.configType.name,
                                onClick = {
                                    scope.launch { state.selectConfig(config.id) }
                                },
                            )
                        }
                    }
                }
            }

            ConfigCenterPane(
                title = "编辑",
                modifier = Modifier.weight(1.15f).fillMaxHeight(),
            ) {
                ConfigCenterFormColumn(modifier = Modifier.fillMaxSize()) {
                    Text("项目", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = state.projectSlug,
                        onValueChange = { state.projectSlug = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Project Slug") },
                    )
                    OutlinedTextField(
                        value = state.projectName,
                        onValueChange = { state.projectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Project Name") },
                    )
                    OutlinedTextField(
                        value = state.projectDescription,
                        onValueChange = { state.projectDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                    )
                    CompactSwitchRow(
                        label = "项目启用",
                        checked = state.projectEnabled,
                        onCheckedChange = { state.projectEnabled = it },
                    )
                    Button(
                        onClick = { scope.launch { state.saveProject() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存项目")
                    }

                    HorizontalDivider()
                    Text("环境", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = state.environmentSlug,
                        onValueChange = { state.environmentSlug = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Environment Slug") },
                    )
                    OutlinedTextField(
                        value = state.environmentName,
                        onValueChange = { state.environmentName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Environment Name") },
                    )
                    OutlinedTextField(
                        value = state.environmentDescription,
                        onValueChange = { state.environmentDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                    )
                    OutlinedTextField(
                        value = state.environmentSortOrderText,
                        onValueChange = { state.environmentSortOrderText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Sort Order") },
                    )
                    CompactSwitchRow(
                        label = "默认环境",
                        checked = state.environmentIsDefault,
                        onCheckedChange = { state.environmentIsDefault = it },
                    )
                    CompactSwitchRow(
                        label = "启用 Personal Config",
                        checked = state.environmentPersonalEnabled,
                        onCheckedChange = { state.environmentPersonalEnabled = it },
                    )
                    Button(
                        onClick = { scope.launch { state.saveEnvironment() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存环境")
                    }

                    HorizontalDivider()
                    Text("Config", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = state.configSlug,
                        onValueChange = { state.configSlug = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Config Slug") },
                    )
                    OutlinedTextField(
                        value = state.configName,
                        onValueChange = { state.configName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Config Name") },
                    )
                    OutlinedTextField(
                        value = state.configDescription,
                        onValueChange = { state.configDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.configType == ConfigCenterConfigType.BRANCH,
                            onClick = { state.configType = ConfigCenterConfigType.BRANCH },
                            label = { Text("Branch") },
                        )
                        FilterChip(
                            selected = state.configType == ConfigCenterConfigType.PERSONAL,
                            onClick = { state.configType = ConfigCenterConfigType.PERSONAL },
                            label = { Text("Personal") },
                        )
                    }
                    CompactSwitchRow(
                        label = "锁定 Config",
                        checked = state.configLocked,
                        onCheckedChange = { state.configLocked = it },
                    )
                    CompactSwitchRow(
                        label = "Config 启用",
                        checked = state.configEnabled,
                        onCheckedChange = { state.configEnabled = it },
                    )
                    Button(
                        onClick = { scope.launch { state.saveConfig() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存 Config")
                    }
                }
            }
        }
    }
}
