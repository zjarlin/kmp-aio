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

@Route(
    value = "配置中心",
    title = "令牌与审计",
    routePath = "system/config-center/access",
    icon = "Shield",
    order = 82.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun ConfigCenterAccessScreen() {
    val state = rememberConfigCenterWorkbenchState()
    val scope = rememberCoroutineScope()

    ConfigCenterWorkspaceFrame(
        title = "令牌与审计",
        state = state,
        onRefresh = { state.refreshActivities() },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfigCenterPane(
                title = "服务令牌",
                modifier = Modifier.weight(0.95f).fillMaxHeight(),
            ) {
                ConfigCenterFormColumn(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = state.configs.firstOrNull { it.id == state.selectedConfigId }?.name
                            ?: "先在“项目与环境”页选中一个 Config",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    OutlinedTextField(
                        value = state.tokenName,
                        onValueChange = { state.tokenName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Token Name") },
                    )
                    OutlinedTextField(
                        value = state.tokenDescription,
                        onValueChange = { state.tokenDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                    )
                    OutlinedTextField(
                        value = state.tokenExpireTimeText,
                        onValueChange = { state.tokenExpireTimeText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Expire Time Millis") },
                    )
                    CompactSwitchRow(
                        label = "允许写入",
                        checked = state.tokenWriteAccess,
                        onCheckedChange = { state.tokenWriteAccess = it },
                    )
                    Button(
                        onClick = { scope.launch { state.issueToken() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("签发令牌")
                    }
                    if (state.issuedTokenText.isNotBlank()) {
                        CodeArea(
                            value = state.issuedTokenText,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            label = "一次性明文令牌",
                            readOnly = true,
                        )
                    }
                    HorizontalDivider()
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.tokens, key = { item -> item.id }) { token ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                ConfigCenterSelectableItem(
                                    selected = false,
                                    title = token.name,
                                    subtitle = "${token.tokenPrefix} | ${token.configName}",
                                    trailing = if (token.active) "ACTIVE" else "REVOKED",
                                    onClick = {},
                                )
                                OutlinedButton(
                                    onClick = { scope.launch { state.revokeToken(token.id) } },
                                    enabled = token.active,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("吊销令牌")
                                }
                            }
                        }
                    }
                }
            }

            ConfigCenterPane(
                title = "审计流",
                modifier = Modifier.weight(1.05f).fillMaxHeight(),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.activities, key = { item -> item.id }) { activity ->
                        ConfigCenterSelectableItem(
                            selected = false,
                            title = activity.summary,
                            subtitle = "${activity.resourceType}:${activity.resourceKey}",
                            trailing = activity.action.name,
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}
