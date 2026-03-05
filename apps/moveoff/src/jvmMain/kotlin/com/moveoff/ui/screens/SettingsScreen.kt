package com.moveoff.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.moveoff.model.*
import com.moveoff.storage.S3Config
import com.moveoff.storage.SSHConfig
import com.moveoff.storage.SSHAuthType
import com.moveoff.update.UpdateCheckerManager
import com.moveoff.update.UpdateState
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.SectionTitle
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "配置应用程序的各项参数",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // S3 Storage Section
        SectionTitle(title = "S3 存储配置（主）", icon = Icons.Default.Cloud)
        S3StorageSettingsCard()

        Spacer(modifier = Modifier.height(24.dp))

        // SSH Storage Section
        SectionTitle(title = "SSH/SFTP 备用配置", icon = Icons.Default.Computer)
        SSHStorageSettingsCard()

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance Section
        SectionTitle(title = "外观", icon = Icons.Default.Menu)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Theme Selection
                Text(
                    text = "主题",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    ThemeOption(
                        theme = Theme.LIGHT,
                        currentTheme = settings.theme,
                        onSelect = {
                            viewModel.updateSettings(settings.copy(theme = Theme.LIGHT))
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ThemeOption(
                        theme = Theme.DARK,
                        currentTheme = settings.theme,
                        onSelect = {
                            viewModel.updateSettings(settings.copy(theme = Theme.DARK))
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ThemeOption(
                        theme = Theme.SYSTEM,
                        currentTheme = settings.theme,
                        onSelect = {
                            viewModel.updateSettings(settings.copy(theme = Theme.SYSTEM))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "语言",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "当前: 简体中文",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    OutlinedButton(onClick = { /* TODO */ }) {
                        Text("更改")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Storage Strategy Section
        SectionTitle(title = "存储策略", icon = Icons.Default.AccountBox)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Organize by extension
                SettingSwitch(
                    title = "按文件类型组织",
                    description = "根据文件扩展名自动分类存储",
                    checked = settings.storageStrategy.organizeByExtension,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(
                                storageStrategy = settings.storageStrategy.copy(organizeByExtension = it)
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Organize by date
                SettingSwitch(
                    title = "按日期组织",
                    description = "根据上传日期自动分类存储",
                    checked = settings.storageStrategy.organizeByDate,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(
                                storageStrategy = settings.storageStrategy.copy(organizeByDate = it)
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Conflict Strategy
                Text(
                    text = "冲突处理策略",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ConflictStrategySelector(
                    currentStrategy = settings.storageStrategy.conflictStrategy,
                    onSelect = {
                        viewModel.updateSettings(
                            settings.copy(
                                storageStrategy = settings.storageStrategy.copy(conflictStrategy = it)
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Local Strategy Section
        SectionTitle(title = "本地处理", icon = Icons.Default.Home)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Delete after transfer
                SettingSwitch(
                    title = "传输后删除本地文件",
                    description = "成功上传后自动删除本地源文件",
                    checked = settings.localStrategy.deleteAfterTransfer,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(
                                localStrategy = settings.localStrategy.copy(deleteAfterTransfer = it)
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Create shortcut
                SettingSwitch(
                    title = "创建快捷方式",
                    description = "在原位置保留文件快捷方式",
                    checked = settings.localStrategy.createShortcut,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(
                                localStrategy = settings.localStrategy.copy(createShortcut = it)
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transfer Settings Section
        SectionTitle(title = "传输设置", icon = Icons.Default.Send)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Max concurrent transfers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "最大并发传输数",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "同时传输的文件数量",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (settings.maxConcurrentTransfers > 1) {
                                    viewModel.updateSettings(
                                        settings.copy(maxConcurrentTransfers = settings.maxConcurrentTransfers - 1)
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                        Text(
                            text = settings.maxConcurrentTransfers.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = {
                                if (settings.maxConcurrentTransfers < 10) {
                                    viewModel.updateSettings(
                                        settings.copy(maxConcurrentTransfers = settings.maxConcurrentTransfers + 1)
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Notifications
                SettingSwitch(
                    title = "显示通知",
                    description = "任务完成或失败时显示系统通知",
                    checked = settings.showNotifications,
                    onCheckedChange = {
                        viewModel.updateSettings(settings.copy(showNotifications = it))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Update Section
        SectionTitle(title = "自动更新", icon = Icons.Default.Update)
        UpdateSettingsCard(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // System Section
        SectionTitle(title = "系统", icon = Icons.Default.Settings)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Auto start
                SettingSwitch(
                    title = "开机自动启动",
                    description = "系统启动时自动运行 MoveOff",
                    checked = settings.autoStart,
                    onCheckedChange = {
                        viewModel.updateSettings(settings.copy(autoStart = it))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Version info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "版本",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "MoveOff v1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    TextButton(onClick = { /* TODO */ }) {
                        Text("检查更新")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ThemeOption(
    theme: Theme,
    currentTheme: Theme,
    onSelect: () -> Unit
) {
    val isSelected = theme == currentTheme
    val text = when (theme) {
        Theme.LIGHT -> "浅色"
        Theme.DARK -> "深色"
        Theme.SYSTEM -> "跟随系统"
    }

    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = { Text(text) }
    )
}

@Composable
fun ConflictStrategySelector(
    currentStrategy: ConflictStrategy,
    onSelect: (ConflictStrategy) -> Unit
) {
    Row {
        ConflictStrategyOption(
            strategy = ConflictStrategy.RENAME,
            currentStrategy = currentStrategy,
            onSelect = onSelect
        )
        Spacer(modifier = Modifier.width(12.dp))
        ConflictStrategyOption(
            strategy = ConflictStrategy.OVERWRITE,
            currentStrategy = currentStrategy,
            onSelect = onSelect
        )
        Spacer(modifier = Modifier.width(12.dp))
        ConflictStrategyOption(
            strategy = ConflictStrategy.SKIP,
            currentStrategy = currentStrategy,
            onSelect = onSelect
        )
    }
}

@Composable
fun ConflictStrategyOption(
    strategy: ConflictStrategy,
    currentStrategy: ConflictStrategy,
    onSelect: (ConflictStrategy) -> Unit
) {
    val text = when (strategy) {
        ConflictStrategy.RENAME -> "重命名"
        ConflictStrategy.OVERWRITE -> "覆盖"
        ConflictStrategy.SKIP -> "跳过"
    }

    FilterChip(
        selected = strategy == currentStrategy,
        onClick = { onSelect(strategy) },
        label = { Text(text) }
    )
}

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S3StorageSettingsCard() {
    val scope = rememberCoroutineScope()

    var s3Endpoint by remember { mutableStateOf(System.getenv("S3_ENDPOINT") ?: "http://localhost:9000") }
    var s3Region by remember { mutableStateOf("us-east-1") }
    var s3Bucket by remember { mutableStateOf(System.getenv("S3_BUCKET") ?: "moveoff") }
    var s3AccessKey by remember { mutableStateOf(System.getenv("S3_ACCESS_KEY") ?: "") }
    var s3SecretKey by remember { mutableStateOf(System.getenv("S3_SECRET_KEY") ?: "") }
    var s3Prefix by remember { mutableStateOf("sync/") }
    var s3ForcePathStyle by remember { mutableStateOf(true) }

    var testStatus by remember { mutableStateOf<TestStatus?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "配置S3兼容的对象存储（MinIO、阿里云OSS、腾讯云COS等）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = s3Endpoint,
                onValueChange = { s3Endpoint = it },
                label = { Text("服务端点") },
                placeholder = { Text("https://s3.amazonaws.com 或 http://localhost:9000") },
                leadingIcon = { Icon(Icons.Default.Link, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                OutlinedTextField(
                    value = s3Region,
                    onValueChange = { s3Region = it },
                    label = { Text("Region") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = s3Bucket,
                    onValueChange = { s3Bucket = it },
                    label = { Text("存储桶") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = s3AccessKey,
                onValueChange = { s3AccessKey = it },
                label = { Text("Access Key") },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = s3SecretKey,
                onValueChange = { s3SecretKey = it },
                label = { Text("Secret Key") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = s3Prefix,
                onValueChange = { s3Prefix = it },
                label = { Text("存储前缀（可选）") },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = s3ForcePathStyle,
                    onCheckedChange = { s3ForcePathStyle = it }
                )
                Text("使用路径样式访问（MinIO需要勾选）")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        scope.launch {
                            isTesting = true
                            testStatus = null
                            try {
                                val client = com.moveoff.storage.S3StorageClient(
                                    S3Config(
                                        endpoint = s3Endpoint,
                                        region = s3Region,
                                        bucket = s3Bucket,
                                        accessKey = s3AccessKey,
                                        secretKey = s3SecretKey,
                                        prefix = s3Prefix,
                                        forcePathStyle = s3ForcePathStyle
                                    )
                                )
                                val success = client.testConnection()
                                testStatus = if (success) TestStatus.Success else TestStatus.Failed("连接失败")
                            } catch (e: Exception) {
                                testStatus = TestStatus.Failed(e.message ?: "未知错误")
                            } finally {
                                isTesting = false
                            }
                        }
                    },
                    enabled = !isTesting &&
                            s3Endpoint.isNotBlank() && s3Bucket.isNotBlank() &&
                            s3AccessKey.isNotBlank() && s3SecretKey.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试中...")
                    } else {
                        Icon(Icons.Default.NetworkCheck, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试连接")
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { /* TODO: 保存配置 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存配置")
                }
            }

            testStatus?.let { status ->
                Spacer(modifier = Modifier.height(12.dp))
                when (status) {
                    is TestStatus.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "连接成功！",
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    is TestStatus.Failed -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "连接失败: ${status.message}",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class TestStatus {
    object Success : TestStatus()
    data class Failed(val message: String) : TestStatus()
}

@Composable
fun UpdateSettingsCard(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val updateSettings = settings.updateSettings
    val scope = rememberCoroutineScope()

    var isChecking by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Auto check
            SettingSwitch(
                title = "自动检查更新",
                description = "启动时和每天自动检查新版本",
                checked = updateSettings.checkUpdatesAutomatically,
                onCheckedChange = {
                    viewModel.updateSettings(
                        settings.copy(
                            updateSettings = updateSettings.copy(
                                checkUpdatesAutomatically = it
                            )
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Update channel
            Text(
                text = "更新通道",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                UpdateChannelOption(
                    channel = UpdateChannel.STABLE,
                    currentChannel = updateSettings.updateChannel,
                    onSelect = {
                        viewModel.updateSettings(
                            settings.copy(
                                updateSettings = updateSettings.copy(
                                    updateChannel = it
                                )
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                UpdateChannelOption(
                    channel = UpdateChannel.BETA,
                    currentChannel = updateSettings.updateChannel,
                    onSelect = {
                        viewModel.updateSettings(
                            settings.copy(
                                updateSettings = updateSettings.copy(
                                    updateChannel = it
                                )
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                UpdateChannelOption(
                    channel = UpdateChannel.DEV,
                    currentChannel = updateSettings.updateChannel,
                    onSelect = {
                        viewModel.updateSettings(
                            settings.copy(
                                updateSettings = updateSettings.copy(
                                    updateChannel = it
                                )
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Auto download
            SettingSwitch(
                title = "自动下载更新",
                description = "发现新版本后自动下载（不会自动安装）",
                checked = updateSettings.downloadUpdatesAutomatically,
                onCheckedChange = {
                    viewModel.updateSettings(
                        settings.copy(
                            updateSettings = updateSettings.copy(
                                downloadUpdatesAutomatically = it
                            )
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "当前版本",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "v1.0.0 (${updateSettings.updateChannel.name})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            isChecking = true
                            try {
                                val checker = UpdateCheckerManager.get()
                                checker.checkForUpdate()
                            } catch (e: Exception) {
                                // 处理错误
                            } finally {
                                isChecking = false
                            }
                        }
                    },
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("检查中...")
                    } else {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即检查")
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateChannelOption(
    channel: UpdateChannel,
    currentChannel: UpdateChannel,
    onSelect: (UpdateChannel) -> Unit
) {
    val text = when (channel) {
        UpdateChannel.STABLE -> "稳定版"
        UpdateChannel.BETA -> "测试版"
        UpdateChannel.DEV -> "开发版"
    }

    FilterChip(
        selected = channel == currentChannel,
        onClick = { onSelect(channel) },
        label = { Text(text) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SSHStorageSettingsCard() {
    val scope = rememberCoroutineScope()

    var sshHost by remember { mutableStateOf("") }
    var sshPort by remember { mutableStateOf("22") }
    var sshUsername by remember { mutableStateOf("") }
    var sshPassword by remember { mutableStateOf("") }
    var sshPrivateKeyPath by remember { mutableStateOf("") }
    var sshPassphrase by remember { mutableStateOf("") }
    var sshRemotePath by remember { mutableStateOf("") }
    var usePrivateKey by remember { mutableStateOf(false) }
    var sshEnabled by remember { mutableStateOf(false) }

    var testStatus by remember { mutableStateOf<TestStatus?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "启用 SSH/SFTP 备用存储",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = sshEnabled,
                    onCheckedChange = { sshEnabled = it }
                )
            }

            if (sshEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "当 S3 存储不可用时，自动切换到 SSH/SFTP 服务器",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    OutlinedTextField(
                        value = sshHost,
                        onValueChange = { sshHost = it },
                        label = { Text("主机地址") },
                        placeholder = { Text("example.com 或 192.168.1.100") },
                        leadingIcon = { Icon(Icons.Default.Computer, null) },
                        modifier = Modifier.weight(2f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = sshPort,
                        onValueChange = { sshPort = it.filter { it.isDigit() } },
                        label = { Text("端口") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sshUsername,
                    onValueChange = { sshUsername = it },
                    label = { Text("用户名") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 认证方式选择
                Text(
                    text = "认证方式",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !usePrivateKey,
                        onClick = { usePrivateKey = false }
                    )
                    Text("密码认证")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = usePrivateKey,
                        onClick = { usePrivateKey = true }
                    )
                    Text("私钥认证")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!usePrivateKey) {
                    OutlinedTextField(
                        value = sshPassword,
                        onValueChange = { sshPassword = it },
                        label = { Text("密码") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = sshPrivateKeyPath,
                        onValueChange = { sshPrivateKeyPath = it },
                        label = { Text("私钥文件路径") },
                        placeholder = { Text("~/.ssh/id_rsa") },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sshPassphrase,
                        onValueChange = { sshPassphrase = it },
                        label = { Text("私钥密码（可选）") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sshRemotePath,
                    onValueChange = { sshRemotePath = it },
                    label = { Text("远程根目录（可选）") },
                    placeholder = { Text("/home/username/moveoff") },
                    leadingIcon = { Icon(Icons.Default.Folder, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testStatus = null
                                try {
                                    val port = sshPort.toIntOrNull() ?: 22
                                    val remotePath = sshRemotePath.ifEmpty {
                                        "/home/$sshUsername/moveoff"
                                    }

                                    val client = com.moveoff.storage.SSHStorageClient(
                                        SSHConfig(
                                            host = sshHost,
                                            port = port,
                                            username = sshUsername,
                                            authType = if (usePrivateKey) SSHAuthType.PRIVATE_KEY else SSHAuthType.PASSWORD,
                                            password = if (!usePrivateKey) sshPassword else null,
                                            privateKeyPath = if (usePrivateKey) sshPrivateKeyPath else null,
                                            passphrase = if (usePrivateKey) sshPassphrase else null,
                                            remoteRootPath = remotePath
                                        )
                                    )
                                    val success = client.testConnection()
                                    client.disconnect()
                                    testStatus = if (success) TestStatus.Success else TestStatus.Failed("连接失败，请检查配置")
                                } catch (e: Exception) {
                                    testStatus = TestStatus.Failed(e.message ?: "未知错误")
                                } finally {
                                    isTesting = false
                                }
                            }
                        },
                        enabled = !isTesting &&
                                sshHost.isNotBlank() &&
                                sshUsername.isNotBlank() &&
                                (if (usePrivateKey) sshPrivateKeyPath.isNotBlank() else sshPassword.isNotBlank()),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("测试中...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("测试连接")
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { /* TODO: 保存配置 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存配置")
                    }
                }

                testStatus?.let { status ->
                    Spacer(modifier = Modifier.height(12.dp))
                    when (status) {
                        is TestStatus.Success -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "SSH 连接成功！",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        is TestStatus.Failed -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "连接失败: ${status.message}",
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
