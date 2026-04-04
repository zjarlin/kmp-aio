package site.addzero.kcloud.plugins.system.rbac.screen

import androidx.compose.foundation.layout.*
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
import site.addzero.workbench.design.button.WorkbenchButton as Button
import site.addzero.kcloud.plugins.system.rbac.UserCenterWorkbenchState

@Route(
    value = "用户中心",
    title = "个人资料",
    routePath = "system/user-center/profile",
    icon = "Person",
    order = 10.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统管理",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun UserCenterProfileScreen() {
    val viewModel: UserCenterProfileViewModel = koinViewModel()
    UserCenterProfileContent(state = viewModel.state)
}

@Composable
private fun UserCenterProfileContent(
    state: UserCenterWorkbenchState,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        state.ensureLoaded()
    }

    Card(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "个人资料",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "当前是单机单账号模型，先把个人资料和工作台标识稳定下来。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    enabled = !state.isBusy,
                    onClick = { scope.launch { state.save() } },
                ) {
                    Text("保存")
                }
            }

            if (state.message.isNotBlank()) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (state.isBusy && state.profile == null) {
                CircularProgressIndicator()
            }

            OutlinedTextField(
                value = state.displayName,
                onValueChange = { state.displayName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("显示名称") },
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = { state.email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("邮箱") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.avatarLabel,
                    onValueChange = { state.avatarLabel = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("头像标签") },
                )
                OutlinedTextField(
                    value = state.locale,
                    onValueChange = { state.locale = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("语言区域") },
                )
            }
            OutlinedTextField(
                value = state.timeZone,
                onValueChange = { state.timeZone = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("时区") },
            )
            state.profile?.let { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "账号键：${profile.accountKey}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "创建时间：${profile.createTimeMillis}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "更新时间：${profile.updateTimeMillis ?: profile.createTimeMillis}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
