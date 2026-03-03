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
import androidx.compose.ui.unit.dp
import com.moveoff.model.*
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.SectionTitle

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
