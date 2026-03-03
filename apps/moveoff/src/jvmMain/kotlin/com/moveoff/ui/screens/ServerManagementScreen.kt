package com.moveoff.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.model.AuthType
import com.moveoff.model.ServerConfig
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.EmptyState
import kotlinx.coroutines.launch

@Composable
fun ServerManagementScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<ServerConfig?>(null) }
    var serverToDelete by remember { mutableStateOf<ServerConfig?>(null) }
    var testingServer by remember { mutableStateOf<String?>(null) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "服务器管理",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "管理您的远程服务器连接",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加服务器")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Server List
        if (settings.servers.isEmpty()) {
            EmptyState(
                icon = Icons.Default.AccountBox,
                message = "暂无服务器",
                description = "添加您的第一个远程服务器以开始文件迁移",
                actionText = "添加服务器",
                onAction = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(settings.servers) { server ->
                    ServerCard(
                        server = server,
                        isTesting = testingServer == server.id,
                        testResult = if (testingServer == server.id) testResult else null,
                        onEdit = { editingServer = server },
                        onDelete = { serverToDelete = server },
                        onTest = {
                            testingServer = server.id
                            testResult = null
                            scope.launch {
                                // Simulate connection test
                                kotlinx.coroutines.delay(1000)
                                testResult = true
                                kotlinx.coroutines.delay(2000)
                                testingServer = null
                                testResult = null
                            }
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingServer != null) {
        ServerDialog(
            server = editingServer,
            onConfirm = { server ->
                if (editingServer != null) {
                    // Update existing
                    val updated = settings.servers.map {
                        if (it.id == server.id) server else it
                    }
                    viewModel.updateSettings(settings.copy(servers = updated))
                } else {
                    // Add new
                    viewModel.addServer(server)
                }
                showAddDialog = false
                editingServer = null
            },
            onDismiss = {
                showAddDialog = false
                editingServer = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (serverToDelete != null) {
        AlertDialog(
            onDismissRequest = { serverToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("删除服务器") },
            text = { Text("确定要删除服务器 \"${serverToDelete?.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        serverToDelete?.let { viewModel.removeServer(it.id) }
                        serverToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { serverToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ServerCard(
    server: ServerConfig,
    isTesting: Boolean,
    testResult: Boolean?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Server Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${server.username}@${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "远程路径: ${server.remoteRootPath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Connection Status
                when {
                    isTesting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    testResult == true -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    testResult == false -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Actions
                Row {
                    IconButton(onClick = onTest) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "测试连接"
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServerDialog(
    server: ServerConfig?,
    onConfirm: (ServerConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(server?.name ?: "") }
    var host by remember { mutableStateOf(server?.host ?: "") }
    var port by remember { mutableStateOf(server?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(server?.username ?: "") }
    var authType by remember { mutableStateOf(server?.authType ?: AuthType.PASSWORD) }
    var password by remember { mutableStateOf(server?.password ?: "") }
    var privateKeyPath by remember { mutableStateOf(server?.privateKeyPath ?: "") }
    var passphrase by remember { mutableStateOf(server?.passphrase ?: "") }
    var remoteRootPath by remember { mutableStateOf(server?.remoteRootPath ?: "") }

    val isEditing = server != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑服务器" else "添加服务器") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("服务器名称") },
                    placeholder = { Text("例如: 我的服务器") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Host and Port
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("主机地址") },
                        placeholder = { Text("192.168.1.1") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it.filter { c -> c.isDigit() } },
                        label = { Text("端口") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Auth Type
                Text(
                    text = "认证方式",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row {
                    AuthTypeOption(
                        type = AuthType.PASSWORD,
                        selected = authType == AuthType.PASSWORD,
                        onSelect = { authType = AuthType.PASSWORD }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AuthTypeOption(
                        type = AuthType.PRIVATE_KEY,
                        selected = authType == AuthType.PRIVATE_KEY,
                        onSelect = { authType = AuthType.PRIVATE_KEY }
                    )
                }

                // Auth Details
                if (authType == AuthType.PASSWORD) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = privateKeyPath,
                        onValueChange = { privateKeyPath = it },
                        label = { Text("私钥路径") },
                        placeholder = { Text("~/.ssh/id_rsa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text("私钥密码 (可选)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Remote Root Path
                OutlinedTextField(
                    value = remoteRootPath,
                    onValueChange = { remoteRootPath = it },
                    label = { Text("远程根目录") },
                    placeholder = { Text("/home/username/moveoff") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newServer = ServerConfig(
                        id = server?.id ?: generateId(),
                        name = name,
                        host = host,
                        port = port.toIntOrNull() ?: 22,
                        username = username,
                        authType = authType,
                        password = password.takeIf { it.isNotEmpty() },
                        privateKeyPath = privateKeyPath.takeIf { it.isNotEmpty() },
                        passphrase = passphrase.takeIf { it.isNotEmpty() },
                        remoteRootPath = remoteRootPath.ifEmpty { "/home/$username/moveoff" },
                        createdAt = server?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onConfirm(newServer)
                },
                enabled = name.isNotBlank() && host.isNotBlank() && username.isNotBlank()
            ) {
                Text(if (isEditing) "保存" else "添加")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AuthTypeOption(
    type: AuthType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onSelect)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = when (type) {
                AuthType.PASSWORD -> "密码"
                AuthType.PRIVATE_KEY -> "私钥"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun generateId(): String {
    return "${System.currentTimeMillis()}_${(1000..9999).random()}"
}
