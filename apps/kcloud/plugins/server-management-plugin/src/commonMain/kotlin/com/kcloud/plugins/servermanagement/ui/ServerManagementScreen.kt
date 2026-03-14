package com.kcloud.plugins.servermanagement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kcloud.model.AuthType
import com.kcloud.model.ServerConfig
import com.kcloud.plugins.servermanagement.ServerManagementService
import org.koin.compose.koinInject

@Composable
fun ServerManagementScreen(
    service: ServerManagementService = koinInject()
) {
    var servers by remember { mutableStateOf(service.listServers()) }
    var draft by remember { mutableStateOf(emptyServerConfig()) }
    var status by remember { mutableStateOf("这里维护的是壳层统一保存的服务器列表，后续会给 SSH / 同步策略复用。") }
    var selectedServerId by remember { mutableStateOf<String?>(null) }

    fun saveCurrent() {
        val result = service.saveServer(draft)
        servers = result.servers
        status = result.message

        if (!result.success) {
            return
        }

        selectedServerId = draft.id
        draft = service.findServer(draft.id) ?: draft
    }

    fun deleteCurrent() {
        val targetId = selectedServerId ?: draft.id
        val result = service.deleteServer(targetId)
        servers = result.servers
        status = result.message

        if (!result.success) {
            return
        }

        draft = emptyServerConfig()
        selectedServerId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("服务器管理", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.host,
                    onValueChange = { draft = draft.copy(host = it) },
                    label = { Text("Host") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.port.toString(),
                    onValueChange = { input ->
                        draft = draft.copy(port = input.toIntOrNull() ?: draft.port)
                    },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.username,
                    onValueChange = { draft = draft.copy(username = it) },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            draft = draft.copy(authType = AuthType.PASSWORD)
                        }
                    ) {
                        Text(if (draft.authType == AuthType.PASSWORD) "密码模式 ✓" else "密码模式")
                    }
                    Button(
                        onClick = {
                            draft = draft.copy(authType = AuthType.PRIVATE_KEY)
                        }
                    ) {
                        Text(if (draft.authType == AuthType.PRIVATE_KEY) "密钥模式 ✓" else "密钥模式")
                    }
                }

                if (draft.authType == AuthType.PASSWORD) {
                    OutlinedTextField(
                        value = draft.password.orEmpty(),
                        onValueChange = { draft = draft.copy(password = it) },
                        label = { Text("密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = draft.privateKeyPath.orEmpty(),
                        onValueChange = { draft = draft.copy(privateKeyPath = it) },
                        label = { Text("私钥路径") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draft.passphrase.orEmpty(),
                        onValueChange = { draft = draft.copy(passphrase = it) },
                        label = { Text("私钥口令（可选）") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = draft.remoteRootPath,
                    onValueChange = { draft = draft.copy(remoteRootPath = it) },
                    label = { Text("远程根目录") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = ::saveCurrent) {
                        Text("保存服务器")
                    }
                    Button(
                        onClick = {
                            draft = emptyServerConfig()
                            selectedServerId = null
                            status = "已切换到新建模式"
                        }
                    ) {
                        Text("新建")
                    }
                    Button(
                        onClick = ::deleteCurrent,
                        enabled = selectedServerId != null
                    ) {
                        Text("删除")
                    }
                }
            }
        }

        Text("已保存服务器 (${servers.size})", fontWeight = FontWeight.SemiBold)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers, key = { server -> server.id }) { server ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (server.id == selectedServerId) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(server.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${server.username}@${server.host}:${server.port}")
                        Text(
                            "认证：${server.authType.displayName()} · 根目录：${server.remoteRootPath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    draft = server
                                    selectedServerId = server.id
                                    status = "已载入服务器：${server.name}"
                                }
                            ) {
                                Text("编辑")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun AuthType.displayName(): String {
    return when (this) {
        AuthType.PASSWORD -> "密码"
        AuthType.PRIVATE_KEY -> "私钥"
    }
}

private fun emptyServerConfig(): ServerConfig {
    return ServerConfig(name = "", host = "", username = "")
}
