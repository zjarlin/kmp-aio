package site.addzero.kbox.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.koinConfiguration
import site.addzero.kbox.KboxKoinApplication
import site.addzero.kbox.core.model.KboxInstallerCandidate
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxOffloadRecord
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxSshAuthMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val KboxColorScheme = darkColorScheme(
    primary = Color(0xFF8EC8FF),
    onPrimary = Color(0xFF08233D),
    secondary = Color(0xFF74E0D4),
    background = Color(0xFF061019),
    surface = Color(0xFF0B1624),
    surfaceVariant = Color(0xFF102033),
    onBackground = Color(0xFFE7F0FB),
    onSurface = Color(0xFFE7F0FB),
)

@Composable
fun App(
    defaultWindowPadding: Dp,
) {
    KoinApplication(
        configuration = koinConfiguration<KboxKoinApplication>(),
    ) {
        MaterialTheme(
            colorScheme = KboxColorScheme,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                KboxWorkbench(modifier = Modifier.padding(defaultWindowPadding))
            }
        }
    }
}

@Composable
private fun KboxWorkbench(
    modifier: Modifier = Modifier,
) {
    val state = remember {
        KoinPlatform.getKoin().get<KboxWorkbenchState>()
    }
    val scope = rememberCoroutineScope()
    var showOffloadHistory by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        state.load()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderPanel(state = state)
        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsPanel(
                state = state,
                modifier = Modifier.width(360.dp).fillMaxHeight(),
                onSave = { scope.launch { state.saveSettings() } },
                onTestSsh = { scope.launch { state.testSsh() } },
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InstallerPanel(
                        state = state,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onScan = { scope.launch { state.scanInstallers() } },
                        onCollectSelected = { scope.launch { state.collectSelectedInstallers(includeAll = false) } },
                        onCollectAll = { scope.launch { state.collectSelectedInstallers(includeAll = true) } },
                    )
                    LargeFilePanel(
                        state = state,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onScan = { scope.launch { state.scanLargeFiles() } },
                        onOffloadSelected = { scope.launch { state.offloadSelectedLargeFiles(includeAll = false) } },
                        onOffloadAll = { scope.launch { state.offloadSelectedLargeFiles(includeAll = true) } },
                    )
                }
                HistoryPanel(
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    showOffloadHistory = showOffloadHistory,
                    onToggle = { showOffloadHistory = it },
                )
            }
        }
    }
}

@Composable
private fun HeaderPanel(
    state: KboxWorkbenchState,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "KBox",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "把安装包集中收纳到本机 appDataDir，并把大文件按同一相对路径迁移到远端 appDataDir。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            Text(
                text = "本地目录：${state.localAppDataDir}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "远端目录预览：${state.remoteAppDataPreview}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.statusIsError) {
                    Color(0xFFFF9B9B)
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    state: KboxWorkbenchState,
    modifier: Modifier,
    onSave: () -> Unit,
    onTestSsh: () -> Unit,
) {
    val draft = state.draft
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = draft.installerScanRootsText,
                onValueChange = { value ->
                    state.updateDraft { it.copy(installerScanRootsText = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("安装包扫描目录") },
                minLines = 4,
            )
            OutlinedTextField(
                value = draft.largeFileScanRootsText,
                onValueChange = { value ->
                    state.updateDraft { it.copy(largeFileScanRootsText = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("大文件扫描目录") },
                minLines = 4,
            )
            OutlinedTextField(
                value = draft.largeFileThresholdGbText,
                onValueChange = { value ->
                    state.updateDraft { it.copy(largeFileThresholdGbText = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("大文件阈值（GB）") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("启用 SSH 远端迁移")
                Switch(
                    checked = draft.sshEnabled,
                    onCheckedChange = { checked ->
                        state.updateDraft { it.copy(sshEnabled = checked) }
                    },
                )
            }
            OutlinedTextField(
                value = draft.sshHost,
                onValueChange = { value ->
                    state.updateDraft { it.copy(sshHost = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("SSH 主机") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.sshPortText,
                    onValueChange = { value ->
                        state.updateDraft { it.copy(sshPortText = value) }
                    },
                    modifier = Modifier.weight(0.45f),
                    label = { Text("端口") },
                )
                OutlinedTextField(
                    value = draft.sshUsername,
                    onValueChange = { value ->
                        state.updateDraft { it.copy(sshUsername = value) }
                    },
                    modifier = Modifier.weight(0.55f),
                    label = { Text("用户名") },
                )
            }
            ChoiceChipRow(
                title = "认证方式",
                options = listOf(
                    "密码" to (draft.sshAuthMode == KboxSshAuthMode.PASSWORD),
                    "私钥" to (draft.sshAuthMode == KboxSshAuthMode.PRIVATE_KEY),
                ),
                onSelect = { selected ->
                    state.updateDraft {
                        it.copy(
                            sshAuthMode = if (selected == "密码") {
                                KboxSshAuthMode.PASSWORD
                            } else {
                                KboxSshAuthMode.PRIVATE_KEY
                            },
                        )
                    }
                },
            )
            if (draft.sshAuthMode == KboxSshAuthMode.PASSWORD) {
                OutlinedTextField(
                    value = draft.sshPassword,
                    onValueChange = { value ->
                        state.updateDraft { it.copy(sshPassword = value) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("密码") },
                )
            } else {
                OutlinedTextField(
                    value = draft.sshPrivateKeyPath,
                    onValueChange = { value ->
                        state.updateDraft { it.copy(sshPrivateKeyPath = value) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("私钥路径") },
                )
                OutlinedTextField(
                    value = draft.sshPrivateKeyPassphrase,
                    onValueChange = { value ->
                        state.updateDraft { it.copy(sshPrivateKeyPassphrase = value) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("私钥口令") },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("严格主机校验")
                Switch(
                    checked = draft.sshStrictHostKeyChecking,
                    onCheckedChange = { checked ->
                        state.updateDraft { it.copy(sshStrictHostKeyChecking = checked) }
                    },
                )
            }
            ChoiceChipRow(
                title = "远端系统",
                options = listOf(
                    "macOS" to (draft.remoteOs == KboxRemoteOs.MACOS),
                    "Windows" to (draft.remoteOs == KboxRemoteOs.WINDOWS),
                    "Linux" to (draft.remoteOs == KboxRemoteOs.LINUX),
                ),
                onSelect = { selected ->
                    val remoteOs = when (selected) {
                        "Windows" -> KboxRemoteOs.WINDOWS
                        "Linux" -> KboxRemoteOs.LINUX
                        else -> KboxRemoteOs.MACOS
                    }
                    state.updateDraft { it.copy(remoteOs = remoteOs) }
                },
            )
            OutlinedTextField(
                value = draft.remoteUserHome,
                onValueChange = { value ->
                    state.updateDraft { it.copy(remoteUserHome = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端用户目录（可选）") },
            )
            OutlinedTextField(
                value = draft.remoteLocalAppData,
                onValueChange = { value ->
                    state.updateDraft { it.copy(remoteLocalAppData = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 LOCALAPPDATA（Windows 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteAppData,
                onValueChange = { value ->
                    state.updateDraft { it.copy(remoteAppData = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 APPDATA（Windows 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteXdgDataHome,
                onValueChange = { value ->
                    state.updateDraft { it.copy(remoteXdgDataHome = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 XDG_DATA_HOME（Linux 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteAppName,
                onValueChange = { value ->
                    state.updateDraft { it.copy(remoteAppName = value) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 appData 名称") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !state.isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("保存配置")
                }
                Button(
                    onClick = onTestSsh,
                    enabled = !state.isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("测试 SSH")
                }
            }
        }
    }
}

@Composable
private fun InstallerPanel(
    state: KboxWorkbenchState,
    modifier: Modifier,
    onScan: () -> Unit,
    onCollectSelected: () -> Unit,
    onCollectAll: () -> Unit,
) {
    DataPanel(
        title = "安装包归档",
        modifier = modifier,
        actions = {
            Button(onClick = onScan, enabled = !state.isBusy) {
                Text("扫描")
            }
            Button(onClick = onCollectSelected, enabled = !state.isBusy) {
                Text("归档选中")
            }
            Button(onClick = onCollectAll, enabled = !state.isBusy) {
                Text("全部归档")
            }
        },
    ) {
        FileTableHeader(
            leading = "选中",
            title = "来源",
            trailing = "目标",
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.installerCandidates,
                key = { it.sourcePath },
            ) { item ->
                InstallerRow(
                    item = item,
                    selected = state.selectedInstallerPaths.contains(item.sourcePath),
                    onSelectedChange = { checked ->
                        state.toggleInstallerSelection(item.sourcePath, checked)
                    },
                )
            }
        }
    }
}

@Composable
private fun LargeFilePanel(
    state: KboxWorkbenchState,
    modifier: Modifier,
    onScan: () -> Unit,
    onOffloadSelected: () -> Unit,
    onOffloadAll: () -> Unit,
) {
    DataPanel(
        title = "大文件远端迁移",
        modifier = modifier,
        actions = {
            Button(onClick = onScan, enabled = !state.isBusy) {
                Text("扫描")
            }
            Button(onClick = onOffloadSelected, enabled = !state.isBusy) {
                Text("发送选中")
            }
            Button(onClick = onOffloadAll, enabled = !state.isBusy) {
                Text("全部发送")
            }
        },
    ) {
        FileTableHeader(
            leading = "选中",
            title = "本地文件",
            trailing = "远端路径",
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.largeFileCandidates,
                key = { it.sourcePath },
            ) { item ->
                LargeFileRow(
                    item = item,
                    remotePath = state.remoteAbsolutePathOf(item),
                    selected = state.selectedLargeFilePaths.contains(item.sourcePath),
                    onSelectedChange = { checked ->
                        state.toggleLargeFileSelection(item.sourcePath, checked)
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryPanel(
    state: KboxWorkbenchState,
    modifier: Modifier,
    showOffloadHistory: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    DataPanel(
        title = if (showOffloadHistory) "迁移历史" else "归档历史",
        modifier = modifier,
        actions = {
            FilterChip(
                selected = !showOffloadHistory,
                onClick = { onToggle(false) },
                label = { Text("归档") },
            )
            FilterChip(
                selected = showOffloadHistory,
                onClick = { onToggle(true) },
                label = { Text("迁移") },
            )
        },
    ) {
        if (!showOffloadHistory) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.installerHistory, key = { "${it.sourcePath}-${it.archivedAtMillis}" }) { record ->
                    HistoryRow(
                        primary = record.destinationRelativePath,
                        secondary = record.sourcePath,
                        extra = "${formatSize(record.sizeBytes)}  ·  ${formatTime(record.archivedAtMillis)}",
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.offloadHistory, key = { "${it.sourcePath}-${it.offloadedAtMillis}" }) { record ->
                    OffloadHistoryRow(record = record)
                }
            }
        }
    }
}

@Composable
private fun DataPanel(
    title: String,
    modifier: Modifier,
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions()
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun FileTableHeader(
    leading: String,
    title: String,
    trailing: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(leading, modifier = Modifier.width(56.dp), style = MaterialTheme.typography.labelMedium)
        Text(title, modifier = Modifier.weight(0.95f), style = MaterialTheme.typography.labelMedium)
        Text(trailing, modifier = Modifier.weight(1.05f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun InstallerRow(
    item: KboxInstallerCandidate,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { checked ->
                onSelectedChange(checked)
            },
            modifier = Modifier.width(56.dp),
        )
        Column(modifier = Modifier.weight(0.95f)) {
            Text(item.fileName, maxLines = 1, fontWeight = FontWeight.Medium)
            Text(
                "${item.sourcePath}\n${item.platform.name.lowercase()} · ${item.extension} · ${formatSize(item.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
            )
        }
        Text(
            text = item.destinationRelativePath,
            modifier = Modifier.weight(1.05f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun LargeFileRow(
    item: KboxLargeFileCandidate,
    remotePath: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { checked ->
                onSelectedChange(checked)
            },
            modifier = Modifier.width(56.dp),
        )
        Column(modifier = Modifier.weight(0.95f)) {
            Text(item.fileName, maxLines = 1, fontWeight = FontWeight.Medium)
            Text(
                "${item.sourcePath}\n${formatSize(item.sizeBytes)} · ${formatTime(item.lastModifiedMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
            )
        }
        Text(
            text = remotePath,
            modifier = Modifier.weight(1.05f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun HistoryRow(
    primary: String,
    secondary: String,
    extra: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        Text(
            secondary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            fontFamily = FontFamily.Monospace,
        )
        Text(
            extra,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun OffloadHistoryRow(
    record: KboxOffloadRecord,
) {
    HistoryRow(
        primary = record.remoteRelativePath,
        secondary = "${record.sourcePath}\n${record.remotePath}",
        extra = "${formatSize(record.sizeBytes)}  ·  ${formatTime(record.offloadedAtMillis)}  ·  删除本地=${record.deletedLocalSource}",
    )
}

@Composable
private fun ChoiceChipRow(
    title: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, selected) ->
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(label) },
                    label = { Text(label) },
                )
            }
        }
    }
}

private fun formatSize(
    bytes: Long,
): String {
    if (bytes < 1024) {
        return "${bytes} B"
    }
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index += 1
    }
    return String.format(Locale.US, "%.2f %s", value, units[index])
}

private fun formatTime(
    millis: Long,
): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(millis))
}
