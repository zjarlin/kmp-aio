package com.kcloud.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.plugins.ssh.SshAuthMode
import com.kcloud.plugins.ssh.SshConnectionConfig
import com.kcloud.plugins.ssh.SshDirectoryEntry
import com.kcloud.plugins.ssh.SshWorkspaceService
import org.koin.compose.koinInject

@Composable
fun SshWorkspaceScreen(
    service: SshWorkspaceService = koinInject()
) {
    var config by remember { mutableStateOf(SshConnectionConfig()) }
    var currentPath by remember { mutableStateOf(".") }
    var newDirectoryPath by remember { mutableStateOf("") }
    var deletePath by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<SshDirectoryEntry>>(emptyList()) }
    var status by remember { mutableStateOf("配置 SSH 连接后可以浏览远程目录") }

    fun refresh() {
        entries = service.listDirectory(currentPath)
        status = "当前目录 ${currentPath}，共 ${entries.size} 项"
    }

    LaunchedEffect(Unit) {
        config = service.loadSettings()
        currentPath = config.remoteRootPath.ifBlank { "." }
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("SSH 连接", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = config.host,
            onValueChange = { config = config.copy(host = it) },
            label = { Text("Host") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.port.toString(),
            onValueChange = { config = config.copy(port = it.toIntOrNull() ?: 22) },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.username,
            onValueChange = { config = config.copy(username = it) },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.password,
            onValueChange = { config = config.copy(password = it) },
            label = { Text("密码（密码模式）") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.privateKeyPath,
            onValueChange = { config = config.copy(privateKeyPath = it) },
            label = { Text("私钥路径（私钥模式）") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.remoteRootPath,
            onValueChange = { config = config.copy(remoteRootPath = it) },
            label = { Text("远程根目录") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    config = service.saveSettings(config)
                    currentPath = config.remoteRootPath.ifBlank { "." }
                    status = "SSH 配置已保存，认证模式：${config.authMode.name}"
                }
            ) {
                Text("保存")
            }
            Button(
                onClick = {
                    val result = service.testConnection(config)
                    config = service.saveSettings(config)
                    status = result.message
                }
            ) {
                Text("测试连接")
            }
            Button(
                onClick = {
                    config = service.saveSettings(config)
                    refresh()
                }
            ) {
                Text("浏览目录")
            }
            Button(
                onClick = {
                    config = config.copy(
                        authMode = if (config.authMode == SshAuthMode.PASSWORD) {
                            SshAuthMode.PRIVATE_KEY
                        } else {
                            SshAuthMode.PASSWORD
                        }
                    )
                }
            ) {
                Text("切换认证")
            }
        }

        OutlinedTextField(
            value = currentPath,
            onValueChange = { currentPath = it },
            label = { Text("当前目录") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newDirectoryPath,
            onValueChange = { newDirectoryPath = it },
            label = { Text("新建目录路径") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = deletePath,
            onValueChange = { deletePath = it },
            label = { Text("删除路径") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val result = service.createDirectory(newDirectoryPath)
                    status = result.message
                    refresh()
                }
            ) {
                Text("新建目录")
            }
            Button(
                onClick = {
                    val result = service.deletePath(deletePath)
                    status = result.message
                    refresh()
                }
            ) {
                Text("删除路径")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { entry -> entry.path }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(entry.name, fontWeight = FontWeight.SemiBold)
                        Text(entry.path, style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (entry.directory) "目录" else "文件 ${entry.size} B",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
