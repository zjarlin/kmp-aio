package com.moveoff.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.sync.PatternType
import com.moveoff.sync.SyncPattern
import com.moveoff.sync.SyncRules

/**
 * 选择性同步设置屏幕
 */
@Composable
fun SelectiveSyncScreen(
    rules: SyncRules,
    onRulesChanged: (SyncRules) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPattern by remember { mutableStateOf<SyncPattern?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = "选择性同步",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加规则")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 全局设置
        GlobalSettings(
            rules = rules,
            onRulesChanged = onRulesChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 规则列表
        Text(
            text = "同步规则 (${rules.localPatterns.size}${if (rules.enableDefaultRules) "+默认" else ""})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 默认规则
            if (rules.enableDefaultRules) {
                item {
                    DefaultRulesSection()
                }
            }

            // 用户自定义规则
            items(rules.localPatterns) { pattern ->
                PatternItem(
                    pattern = pattern,
                    onEdit = { editingPattern = pattern },
                    onDelete = {
                        onRulesChanged(rules.removePattern(pattern))
                    },
                    onToggle = {
                        val updated = pattern.copy(enabled = !pattern.enabled)
                        onRulesChanged(
                            rules.copy(
                                localPatterns = rules.localPatterns.map {
                                    if (it == pattern) updated else it
                                }
                            )
                        )
                    }
                )
            }
        }

        // 添加/编辑规则对话框
        if (showAddDialog || editingPattern != null) {
            PatternEditDialog(
                pattern = editingPattern,
                onConfirm = { pattern ->
                    if (editingPattern != null) {
                        // 编辑现有规则
                        onRulesChanged(
                            rules.copy(
                                localPatterns = rules.localPatterns.map {
                                    if (it == editingPattern) pattern else it
                                }
                            )
                        )
                    } else {
                        // 添加新规则
                        onRulesChanged(rules.addPattern(pattern))
                    }
                    showAddDialog = false
                    editingPattern = null
                },
                onDismiss = {
                    showAddDialog = false
                    editingPattern = null
                }
            )
        }
    }
}

/**
 * 全局设置
 */
@Composable
private fun GlobalSettings(
    rules: SyncRules,
    onRulesChanged: (SyncRules) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "全局设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rules.enableDefaultRules,
                    onCheckedChange = {
                        onRulesChanged(rules.copy(enableDefaultRules = it))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启用默认规则",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "忽略系统文件、IDE配置、依赖目录等",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rules.respectGitignore,
                    onCheckedChange = {
                        onRulesChanged(rules.copy(respectGitignore = it))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "尊重 .gitignore",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "自动读取项目中的.gitignore文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 默认规则区域
 */
@Composable
private fun DefaultRulesSection() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "默认规则",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "系统文件、IDE配置、构建输出等",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 规则项
 */
@Composable
private fun PatternItem(
    pattern: SyncPattern,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = pattern.enabled,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pattern.pattern,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (pattern.description.isNotEmpty()) {
                    Text(
                        text = pattern.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "类型: ${pattern.type.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 规则编辑对话框
 */
@Composable
private fun PatternEditDialog(
    pattern: SyncPattern?,
    onConfirm: (SyncPattern) -> Unit,
    onDismiss: () -> Unit
) {
    var patternStr by remember { mutableStateOf(pattern?.pattern ?: "") }
    var description by remember { mutableStateOf(pattern?.description ?: "") }
    var selectedType by remember { mutableStateOf(pattern?.type ?: PatternType.GLOB) }
    var isNegation by remember { mutableStateOf(pattern?.negation ?: false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pattern != null) "编辑规则" else "添加规则") },
        text = {
            Column {
                OutlinedTextField(
                    value = patternStr,
                    onValueChange = { patternStr = it },
                    label = { Text("模式") },
                    placeholder = { Text("例如: *.log 或 node_modules/") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 类型选择
                Box {
                    OutlinedButton(
                        onClick = { showTypeMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("类型: ${selectedType.name}")
                    }

                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        PatternType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isNegation = !isNegation }
                ) {
                    Checkbox(
                        checked = isNegation,
                        onCheckedChange = { isNegation = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("否定规则（!）")
                        Text(
                            "启用后，匹配的文件不会被忽略",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        SyncPattern(
                            pattern = patternStr,
                            type = selectedType,
                            description = description,
                            negation = isNegation
                        )
                    )
                },
                enabled = patternStr.isNotEmpty()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
