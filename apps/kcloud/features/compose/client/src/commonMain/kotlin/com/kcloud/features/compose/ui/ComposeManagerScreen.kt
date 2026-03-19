package com.kcloud.features.compose.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.features.compose.ComposeCommandResult
import com.kcloud.features.compose.ComposeLogsResult
import com.kcloud.features.compose.ComposeManagerService
import com.kcloud.features.compose.ComposeStackSummary
import com.kcloud.features.compose.ComposeStackStatus
import com.kcloud.features.compose.ComposeTargetMode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ComposeManagerScreen(
    service: ComposeManagerService = koinInject()
) {
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(service.loadSettings()) }
    var targets by remember { mutableStateOf(service.listServerTargets()) }
    var stacks by remember { mutableStateOf<List<ComposeStackSummary>>(emptyList()) }
    var selectedStackName by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf(service.createDraft()) }
    var runtimeMessage by remember { mutableStateOf("正在检查 Docker Compose 环境…") }
    var status by remember { mutableStateOf("这里按 Dockge 的“目录即栈”方式管理 Compose 项目。") }
    var logs by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    suspend fun reloadStacks(restoreSelection: Boolean = true) {
        loading = true
        targets = service.listServerTargets()
        stacks = service.listStacks()
        runtimeMessage = service.inspectRuntime().message
        val targetName = if (restoreSelection) {
            selectedStackName?.takeIf { current -> stacks.any { item -> item.name == current } }
                ?: stacks.firstOrNull()?.name
        } else {
            stacks.firstOrNull()?.name
        }
        selectedStackName = targetName
        draft = if (targetName != null) {
            service.readStack(targetName) ?: service.createDraft(targetName)
        } else {
            service.createDraft()
        }
        loading = false
    }

    fun launchAction(
        action: suspend () -> ComposeCommandResult,
        refresh: Boolean = true
    ) {
        scope.launch {
            loading = true
            val result = action()
            status = result.message
            if (result.output.isNotBlank()) {
                logs = result.output
            }
            if (refresh) {
                reloadStacks()
            } else {
                loading = false
            }
        }
    }

    fun launchLogs() {
        val name = draft.name
        if (name.isBlank()) {
            status = "请先选择一个栈"
            return
        }
        scope.launch {
            loading = true
            val result: ComposeLogsResult = service.readLogs(name)
            status = result.message
            logs = result.output
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadStacks(restoreSelection = false)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Compose 管理", style = MaterialTheme.typography.headlineSmall)
            Text(runtimeMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("目标与目录", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                settings = settings.copy(targetMode = ComposeTargetMode.LOCAL)
                            }
                        ) {
                            Text(if (settings.targetMode == ComposeTargetMode.LOCAL) "本机 ✓" else "本机")
                        }
                        Button(
                            onClick = {
                                settings = settings.copy(targetMode = ComposeTargetMode.SERVER)
                            }
                        ) {
                            Text(if (settings.targetMode == ComposeTargetMode.SERVER) "远程服务器 ✓" else "远程服务器")
                        }
                    }

                    if (settings.targetMode == ComposeTargetMode.SERVER) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (targets.isEmpty()) {
                                Text("还没有已保存服务器，请先到“服务器管理”里新增。")
                            } else {
                                targets.forEach { target ->
                                    Button(
                                        onClick = {
                                            settings = settings.copy(selectedServerId = target.id)
                                        }
                                    ) {
                                        Text(
                                            if (settings.selectedServerId == target.id) {
                                                "${target.name} ✓"
                                            } else {
                                                target.name
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = if (settings.targetMode == ComposeTargetMode.LOCAL) {
                            settings.localStacksPath
                        } else {
                            settings.remoteStacksPath
                        },
                        onValueChange = { value ->
                            settings = if (settings.targetMode == ComposeTargetMode.LOCAL) {
                                settings.copy(localStacksPath = value)
                            } else {
                                settings.copy(remoteStacksPath = value)
                            }
                        },
                        label = {
                            Text(if (settings.targetMode == ComposeTargetMode.LOCAL) "本机栈目录" else "远程栈目录")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = settings.composeCommand,
                        onValueChange = { settings = settings.copy(composeCommand = it) },
                        label = { Text("Compose 命令") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Docker 命令加 sudo")
                        Switch(
                            checked = settings.useSudo,
                            onCheckedChange = { checked -> settings = settings.copy(useSudo = checked) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                settings = service.saveSettings(settings)
                                scope.launch { reloadStacks() }
                            }
                        ) {
                            Text("保存设置")
                        }
                        Button(
                            onClick = {
                                scope.launch { reloadStacks() }
                            }
                        ) {
                            Text("刷新")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        selectedStackName = null
                        draft = service.createDraft()
                        logs = ""
                        status = "已切换到新建栈模式"
                    }
                ) {
                    Text("新建栈")
                }
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            Text("栈列表 (${stacks.size})", fontWeight = FontWeight.SemiBold)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stacks, key = { item -> item.name }) { item ->
                    val selected = item.name == selectedStackName
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(item.name, fontWeight = FontWeight.SemiBold)
                            Text(item.path, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${item.status.displayName()} · 运行 ${item.runningCount}/${item.containerCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.message.isNotBlank()) {
                                Text(
                                    item.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        loading = true
                                        selectedStackName = item.name
                                        draft = service.readStack(item.name) ?: service.createDraft(item.name)
                                        loading = false
                                    }
                                }
                            ) {
                                Text("打开")
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("栈编辑器", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = draft.name,
                        onValueChange = { value -> draft = draft.copy(name = value) },
                        label = { Text("栈名称") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !draft.exists
                    )
                    OutlinedTextField(
                        value = draft.composeFileName,
                        onValueChange = { value -> draft = draft.copy(composeFileName = value) },
                        label = { Text("Compose 文件名") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !draft.exists
                    )
                    Text(
                        "路径：${draft.path.ifBlank { "保存后会自动创建目录" }}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "状态：${draft.status.displayName()}${draft.message.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                launchAction(action = { service.validateDraft(draft) }, refresh = false)
                            },
                            enabled = draft.name.isNotBlank()
                        ) {
                            Text("校验")
                        }
                        Button(
                            onClick = {
                                launchAction(action = { service.saveStack(draft) })
                            },
                            enabled = draft.name.isNotBlank()
                        ) {
                            Text("保存")
                        }
                        Button(
                            onClick = {
                                launchAction(action = { service.upStack(draft.name) })
                            },
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("启动")
                        }
                        Button(
                            onClick = {
                                launchAction(action = { service.downStack(draft.name) })
                            },
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("停止")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                launchAction(action = { service.restartStack(draft.name) })
                            },
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("重启")
                        }
                        Button(
                            onClick = {
                                launchAction(action = { service.pullStack(draft.name) }, refresh = false)
                            },
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("拉取镜像")
                        }
                        Button(
                            onClick = ::launchLogs,
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("查看日志")
                        }
                        Button(
                            onClick = {
                                launchAction(action = { service.deleteStack(draft.name) })
                            },
                            enabled = draft.exists && draft.name.isNotBlank()
                        ) {
                            Text("删除栈")
                        }
                    }

                    OutlinedTextField(
                        value = draft.composeYaml,
                        onValueChange = { value -> draft = draft.copy(composeYaml = value) },
                        label = { Text("compose.yaml") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("服务状态", fontWeight = FontWeight.SemiBold)
                    if (draft.services.isEmpty()) {
                        Text("当前还没有容器状态；首次启动后这里会显示服务运行情况。")
                    } else {
                        draft.services.forEach { item ->
                            val publishers = item.publishers
                                .takeIf { values -> values.isNotEmpty() }
                                ?.joinToString(
                                    prefix = " · ",
                                    separator = ", "
                                )
                                .orEmpty()
                            Text(
                                "${item.service} · ${item.state}" +
                                    (item.health.takeIf { health -> health.isNotBlank() }?.let { " · $it" } ?: "") +
                                    publishers
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("日志 / 命令输出", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = logs,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        readOnly = true,
                        label = { Text("输出") }
                    )
                }
            }
        }
    }
}

private fun ComposeStackStatus.displayName(): String {
    return when (this) {
        ComposeStackStatus.RUNNING -> "运行中"
        ComposeStackStatus.PARTIAL -> "部分运行"
        ComposeStackStatus.STOPPED -> "已停止"
        ComposeStackStatus.EMPTY -> "未启动"
        ComposeStackStatus.INVALID -> "Compose 配置异常"
        ComposeStackStatus.MISSING_DOCKER -> "Docker 不可用"
        ComposeStackStatus.UNKNOWN -> "未知"
    }
}
