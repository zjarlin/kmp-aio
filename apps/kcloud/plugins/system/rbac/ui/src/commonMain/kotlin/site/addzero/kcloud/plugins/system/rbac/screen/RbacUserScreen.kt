package site.addzero.kcloud.plugins.system.rbac.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.design.button.KCloudButton as Button
import site.addzero.kcloud.plugins.system.rbac.RbacWorkbenchState

@Route(
    value = "权限中心",
    title = "RBAC权限管理",
    routePath = "system/rbac",
    icon = "AdminPanelSettings",
    order = 20.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统管理",
            icon = "AdminPanelSettings",
            order = 100,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun RbacUserScreen() {
    val viewModel: RbacUserViewModel = koinViewModel()
    RbacUserContent(state = viewModel.state)
}

@Composable
private fun RbacUserContent(
    state: RbacWorkbenchState,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        state.ensureLoaded()
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.weight(0.9f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "角色清单",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "先落角色元数据管理，成员绑定、权限矩阵和审计流保留到下一阶段。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.statusMessage.isNotBlank()) {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { state.beginCreateRole() },
                    ) {
                        Text("新建角色")
                    }
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.refreshRoles() } },
                    ) {
                        Text("刷新")
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.roles, key = { role -> role.id }) { role ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.selectedRoleId == role.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                },
                            ),
                            onClick = {
                                state.selectRole(role.id)
                            },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = role.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = if (role.enabled) "启用" else "停用",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (role.enabled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    )
                                }
                                Text(
                                    text = role.roleCode,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (role.builtIn) "内建角色" else "自定义角色",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = role.description.orEmpty().ifBlank { "未填写角色说明" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1.2f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "角色编辑",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = state.roleCode,
                    onValueChange = { state.roleCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isBusy && state.selectedRole?.builtIn != true,
                    singleLine = true,
                    label = { Text("角色编码") },
                )
                OutlinedTextField(
                    value = state.roleName,
                    onValueChange = { state.roleName = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isBusy,
                    singleLine = true,
                    label = { Text("角色名称") },
                )
                OutlinedTextField(
                    value = state.roleDescription,
                    onValueChange = { state.roleDescription = it },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    enabled = !state.isBusy,
                    minLines = 8,
                    label = { Text("角色说明") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = state.roleEnabled,
                        onCheckedChange = { checked -> state.roleEnabled = checked },
                        enabled = !state.isBusy,
                    )
                    Text(
                        text = "启用角色",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.saveRole() } },
                    ) {
                        Text("保存角色")
                    }
                    Button(
                        enabled = !state.isBusy && state.selectedRole?.builtIn == false,
                        onClick = { scope.launch { state.deleteSelectedRole() } },
                    ) {
                        Text("删除角色")
                    }
                }
                state.selectedRole?.let { role ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = if (role.builtIn) {
                                    "当前是内建角色，编码固定且不允许删除。"
                                } else {
                                    "当前是自定义角色，可继续调整编码、名称、说明与启用态。"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "角色键：${role.roleKey}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "创建时间：${role.createTimeMillis}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "更新时间：${role.updateTimeMillis ?: role.createTimeMillis}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } ?: run {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "新建时会自动创建自定义角色，默认启用。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
