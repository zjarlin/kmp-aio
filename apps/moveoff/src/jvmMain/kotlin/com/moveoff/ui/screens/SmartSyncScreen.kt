package com.moveoff.ui.screens

import androidx.compose.foundation.clickable
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
import com.moveoff.sync.*
import com.moveoff.team.*

/**
 * 智能同步设置屏幕
 */
@Composable
fun SmartSyncScreen(
    smartSyncManager: SmartSyncManager,
    onBack: () -> Unit
) {
    val currentPolicy by smartSyncManager.currentPolicy.collectAsState()
    val currentNetwork by smartSyncManager.currentNetworkType.collectAsState()
    val shouldSync by smartSyncManager.shouldSync.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "智能同步策略",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 当前网络状态
        CurrentNetworkCard(
            networkType = currentNetwork,
            policy = currentPolicy,
            shouldSync = shouldSync
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 网络策略设置
        Text(
            text = "网络同步策略",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        NetworkType.values().forEach { networkType ->
            if (networkType != NetworkType.UNKNOWN) {
                NetworkPolicyItem(
                    networkType = networkType,
                    policy = currentPolicy?.takeIf { it.networkType == networkType }
                        ?: NetworkSyncPolicy(networkType, true, SyncDirection.BOTH)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 计划同步
        ScheduleSettings()

        Spacer(modifier = Modifier.height(16.dp))

        // 电量设置
        BatterySettings()
    }
}

@Composable
private fun CurrentNetworkCard(
    networkType: NetworkType,
    policy: NetworkSyncPolicy?,
    shouldSync: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (shouldSync) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (networkType) {
                    NetworkType.WIFI -> Icons.Default.Wifi
                    NetworkType.ETHERNET -> Icons.Default.SettingsEthernet
                    NetworkType.MOBILE -> Icons.Default.Smartphone
                    NetworkType.METERED -> Icons.Default.MoneyOff
                    NetworkType.UNKNOWN -> Icons.Default.Help
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (shouldSync) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "当前网络: ${networkType.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (shouldSync) "✅ 可以同步" else "⏸️ 同步已暂停",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (shouldSync) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                policy?.let { p ->
                    Text(
                        text = "方向: ${p.syncDirection.name} | 带宽限制: ${p.bandwidthLimit?.let { "$it B/s" } ?: "无"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkPolicyItem(
    networkType: NetworkType,
    policy: NetworkSyncPolicy
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (networkType) {
                        NetworkType.WIFI -> Icons.Default.Wifi
                        NetworkType.ETHERNET -> Icons.Default.SettingsEthernet
                        NetworkType.MOBILE -> Icons.Default.Smartphone
                        NetworkType.METERED -> Icons.Default.MoneyOff
                        NetworkType.UNKNOWN -> Icons.Default.Help
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = networkType.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (policy.autoSyncEnabled) "自动同步已启用" else "自动同步已禁用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = policy.autoSyncEnabled,
                    onCheckedChange = { /* TODO */ }
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                // 同步方向选择
                Text("同步方向", style = MaterialTheme.typography.labelMedium)
                Row {
                    SyncDirection.values().forEach { direction ->
                        FilterChip(
                            selected = policy.syncDirection == direction,
                            onClick = { /* TODO */ },
                            label = { Text(direction.name) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // 大文件行为
                Text("大文件处理", style = MaterialTheme.typography.labelMedium)
                Row {
                    LargeFileBehavior.values().forEach { behavior ->
                        FilterChip(
                            selected = policy.largeFileBehavior == behavior,
                            onClick = { /* TODO */ },
                            label = { Text(behavior.name) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSettings() {
    var enabled by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "计划同步",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "仅在指定时间段内自动同步",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it }
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimePickerField(label = "开始时间", hour = 2)
                    TimePickerField(label = "结束时间", hour = 6)
                }
            }
        }
    }
}

@Composable
private fun TimePickerField(label: String, hour: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = String.format("%02d:00", hour),
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun BatterySettings() {
    var enabled by remember { mutableStateOf(true) }
    var threshold by remember { mutableStateOf(20) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "电量保护",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "低电量时暂停同步",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it }
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("电量阈值: $threshold%", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = threshold.toFloat(),
                    onValueChange = { threshold = it.toInt() },
                    valueRange = 5f..50f,
                    steps = 8
                )
            }
        }
    }
}
