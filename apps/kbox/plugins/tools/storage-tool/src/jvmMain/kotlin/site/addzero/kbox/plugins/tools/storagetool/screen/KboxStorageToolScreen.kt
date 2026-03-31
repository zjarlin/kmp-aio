package site.addzero.kbox.plugins.tools.storagetool.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kbox.core.model.KboxComposeProjectSnapshot
import site.addzero.kbox.core.model.KboxDotfileCandidate
import site.addzero.kbox.core.model.KboxInstallerArchiveRecord
import site.addzero.kbox.core.model.KboxInstallerCandidate
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxOffloadRecord
import site.addzero.kbox.core.model.KboxPackageDiff
import site.addzero.kbox.core.model.KboxPackageProfileSummary
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.plugins.tools.storagetool.KboxComposeAction
import site.addzero.kbox.plugins.tools.storagetool.KboxStorageHubTab
import site.addzero.kbox.plugins.tools.storagetool.KboxStorageToolState
import site.addzero.kbox.plugins.tools.storagetool.KboxSyncToolState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Route(
    value = "环境资产",
    title = "环境资产管理",
    routePath = "tools/storage-tool",
    icon = "Inventory2",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "工具箱",
            icon = "Inventory2",
            order = 200,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun KboxStorageToolScreen(
    modifier: Modifier = Modifier,
) {
    val state = remember {
        KoinPlatform.getKoin().get<KboxStorageToolState>()
    }
    val syncState = remember {
        KoinPlatform.getKoin().get<KboxSyncToolState>()
    }
    val scope = rememberCoroutineScope()
    var showOffloadHistory by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        state.load()
        syncState.load()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderPanel(state = state)
        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        StorageHubTabs(
            currentTab = state.currentTab,
            onSelect = { tab -> state.currentTab = tab },
        )
        Box(modifier = Modifier.weight(1f)) {
            when (state.currentTab) {
                KboxStorageHubTab.FILES -> FilesTab(
                    state = state,
                    showOffloadHistory = showOffloadHistory,
                    onToggleHistory = { showOffloadHistory = it },
                    modifier = Modifier.fillMaxSize(),
                    onScanInstallers = { scope.launch { state.scanInstallers() } },
                    onCollectSelected = { scope.launch { state.collectSelectedInstallers(includeAll = false) } },
                    onCollectAll = { scope.launch { state.collectSelectedInstallers(includeAll = true) } },
                    onDeleteSelected = { scope.launch { state.deleteSelectedInstallers(includeAll = false) } },
                    onDeleteAll = { scope.launch { state.deleteSelectedInstallers(includeAll = true) } },
                    onScanLargeFiles = { scope.launch { state.scanLargeFiles() } },
                    onOffloadSelected = { scope.launch { state.offloadSelectedLargeFiles(includeAll = false) } },
                    onOffloadAll = { scope.launch { state.offloadSelectedLargeFiles(includeAll = true) } },
                    onOpenFolder = { path -> scope.launch { state.openContainingFolder(path) } },
                )

                KboxStorageHubTab.SYNC -> SyncTab(
                    state = syncState,
                    modifier = Modifier.fillMaxSize(),
                    onStart = { scope.launch { syncState.start() } },
                    onPause = { scope.launch { syncState.pause() } },
                    onRefresh = { scope.launch { syncState.refresh() } },
                    onCompare = { entryId -> scope.launch { syncState.compare(entryId) } },
                    onApplyAction = { entryId, action ->
                        scope.launch {
                            syncState.applyAction(entryId, action)
                        }
                    },
                    onDismissCompare = { syncState.clearComparePreview() },
                )

                KboxStorageHubTab.PACKAGE_PROFILES -> PackageProfilesTab(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { scope.launch { state.refreshPackageProfiles() } },
                    onExport = { scope.launch { state.exportPackageProfile() } },
                    onImport = { scope.launch { state.importSelectedPackageProfile() } },
                )

                KboxStorageHubTab.DOTFILES -> DotfilesTab(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { scope.launch { state.refreshDotfiles() } },
                    onImportSelected = { scope.launch { state.importSelectedDotfile() } },
                    onImportManual = { scope.launch { state.importManualDotfile() } },
                    onRelink = { scope.launch { state.relinkSelectedDotfile() } },
                    onRemove = { scope.launch { state.removeSelectedDotfile() } },
                    onOpenFolder = { path -> scope.launch { state.openContainingFolder(path) } },
                )

                KboxStorageHubTab.COMPOSE -> ComposeTab(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { scope.launch { state.refreshComposeProjects() } },
                    onRegister = { scope.launch { state.registerComposeProject() } },
                    onRemove = { scope.launch { state.removeSelectedComposeProject() } },
                    onSaveCompose = { scope.launch { state.saveSelectedComposeFile() } },
                    onSaveEnv = { scope.launch { state.saveSelectedEnvFile() } },
                    onRunAction = { action -> scope.launch { state.runComposeAction(action) } },
                    onOpenFolder = { path -> scope.launch { state.openContainingFolder(path) } },
                )

                KboxStorageHubTab.SETTINGS -> StorageSettingsTab(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onSave = {
                        scope.launch {
                            state.saveSettings()
                            syncState.reloadSettings()
                        }
                    },
                    onTestSsh = { scope.launch { state.testSsh() } },
                )
            }
        }
    }
}

@Composable
private fun HeaderPanel(
    state: KboxStorageToolState,
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
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "集中管理安装包、系统包 profile、dotfile、Docker Compose 与 SSH/数据目录。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            MonoLine("当前数据目录", state.activeAppDataDir)
            MonoLine("目标数据目录预览", state.configuredAppDataDirPreview)
            MonoLine("远端 appData 预览", state.remoteAppDataPreview)
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.statusIsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
    }
}

@Composable
private fun HubTabs(
    currentTab: KboxStorageHubTab,
    onSelect: (KboxStorageHubTab) -> Unit,
) {
    val tabs = listOf(
        KboxStorageHubTab.FILES to "文件",
        KboxStorageHubTab.PACKAGE_PROFILES to "包配置",
        KboxStorageHubTab.DOTFILES to "Dotfile",
        KboxStorageHubTab.COMPOSE to "Compose",
        KboxStorageHubTab.SETTINGS to "设置",
    )
    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0),
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = currentTab == tab,
                onClick = { onSelect(tab) },
                text = { Text(label) },
            )
        }
    }
}

@Composable
private fun StorageHubTabs(
    currentTab: KboxStorageHubTab,
    onSelect: (KboxStorageHubTab) -> Unit,
) {
    val tabs = listOf(
        KboxStorageHubTab.FILES to "Files",
        KboxStorageHubTab.SYNC to "Sync",
        KboxStorageHubTab.PACKAGE_PROFILES to "Profiles",
        KboxStorageHubTab.DOTFILES to "Dotfiles",
        KboxStorageHubTab.COMPOSE to "Compose",
        KboxStorageHubTab.SETTINGS to "Settings",
    )
    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0),
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = currentTab == tab,
                onClick = { onSelect(tab) },
                text = { Text(label) },
            )
        }
    }
}

@Composable
private fun FilesTab(
    state: KboxStorageToolState,
    showOffloadHistory: Boolean,
    onToggleHistory: (Boolean) -> Unit,
    modifier: Modifier,
    onScanInstallers: () -> Unit,
    onCollectSelected: () -> Unit,
    onCollectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDeleteAll: () -> Unit,
    onScanLargeFiles: () -> Unit,
    onOffloadSelected: () -> Unit,
    onOffloadAll: () -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DataPanel(
                title = "安装包归档",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                actions = {
                    Button(onClick = onScanInstallers, enabled = !state.isBusy) { Text("扫描") }
                    Button(onClick = onCollectSelected, enabled = !state.isBusy) { Text("归档选中") }
                    Button(onClick = onCollectAll, enabled = !state.isBusy) { Text("全部归档") }
                    Button(onClick = onDeleteSelected, enabled = !state.isBusy) { Text("删选中") }
                    Button(onClick = onDeleteAll, enabled = !state.isBusy) { Text("删全部") }
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.installerCandidates, key = { item -> item.sourcePath }) { item ->
                        FileSelectionRow(
                            title = item.fileName,
                            checked = state.selectedInstallerPaths.contains(item.sourcePath),
                            onCheckedChange = { checked -> state.toggleInstallerSelection(item.sourcePath, checked) },
                            primary = item.sourcePath,
                            secondary = "${item.destinationRelativePath}\n${item.platform.name.lowercase()} · ${item.extension} · ${formatSize(item.sizeBytes)}",
                            onOpenFolder = { onOpenFolder(item.sourcePath) },
                        )
                    }
                }
            }
            DataPanel(
                title = "大文件远端迁移",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                actions = {
                    Button(onClick = onScanLargeFiles, enabled = !state.isBusy) { Text("扫描") }
                    Button(onClick = onOffloadSelected, enabled = !state.isBusy) { Text("发送选中") }
                    Button(onClick = onOffloadAll, enabled = !state.isBusy) { Text("全部发送") }
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.largeFileCandidates, key = { item -> item.sourcePath }) { item ->
                        FileSelectionRow(
                            title = item.fileName,
                            checked = state.selectedLargeFilePaths.contains(item.sourcePath),
                            onCheckedChange = { checked -> state.toggleLargeFileSelection(item.sourcePath, checked) },
                            primary = item.sourcePath,
                            secondary = "${state.remoteAbsolutePathOf(item)}\n${formatSize(item.sizeBytes)} · ${formatTime(item.lastModifiedMillis)}",
                            onOpenFolder = { onOpenFolder(item.sourcePath) },
                        )
                    }
                }
            }
        }
        DataPanel(
            title = if (showOffloadHistory) "迁移历史" else "归档历史",
            modifier = Modifier.fillMaxWidth().height(240.dp),
            actions = {
                FilterChip(
                    selected = !showOffloadHistory,
                    onClick = { onToggleHistory(false) },
                    label = { Text("归档") },
                )
                FilterChip(
                    selected = showOffloadHistory,
                    onClick = { onToggleHistory(true) },
                    label = { Text("迁移") },
                )
            },
        ) {
            if (!showOffloadHistory) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.installerHistory, key = { record -> "${record.sourcePath}-${record.archivedAtMillis}" }) { record ->
                        HistoryRow(
                            primary = record.destinationRelativePath,
                            secondary = record.sourcePath,
                            extra = "${formatSize(record.sizeBytes)} · ${formatTime(record.archivedAtMillis)}",
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.offloadHistory, key = { record -> "${record.sourcePath}-${record.offloadedAtMillis}" }) { record ->
                        HistoryRow(
                            primary = record.remoteRelativePath,
                            secondary = "${record.sourcePath}\n${record.remotePath}",
                            extra = "${formatSize(record.sizeBytes)} · ${formatTime(record.offloadedAtMillis)} · 删除本地=${record.deletedLocalSource}",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageProfilesTab(
    state: KboxStorageToolState,
    modifier: Modifier,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DataPanel(
            title = "包管理器与 Profile",
            modifier = Modifier.width(380.dp).fillMaxHeight(),
            actions = {
                Button(onClick = onRefresh, enabled = !state.isBusy) { Text("刷新") }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.exportProfileName,
                    onValueChange = { value -> state.exportProfileName = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("导出文件名") },
                )
                Button(onClick = onExport, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("导出当前机器清单")
                }
                Text("已检测包管理器", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.detectedPackageManagers, key = { manager -> manager.managerId }) { manager ->
                        SimpleCardRow(
                            title = manager.displayName,
                            subtitle = "${manager.managerId} · ${manager.detail}",
                            highlighted = manager.available,
                        )
                    }
                }
                Text("Profile 文件", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.packageProfiles, key = { profile -> profile.fileName }) { profile ->
                        SelectableCardRow(
                            title = profile.profileName,
                            subtitle = "${profile.fileName} · 管理器 ${profile.packageManagerCount} 个 · 包 ${profile.packageCount} 个",
                            selected = state.selectedPackageProfileFileName == profile.fileName,
                            onClick = { state.selectPackageProfile(profile.fileName) },
                        )
                    }
                }
            }
        }
        DataPanel(
            title = "Profile 对比与恢复",
            modifier = Modifier.weight(1f).fillMaxHeight(),
            actions = {
                Button(
                    onClick = onImport,
                    enabled = !state.isBusy && state.selectedPackageProfileFileName.isNotBlank(),
                ) {
                    Text("补装缺失包")
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val profile = state.selectedPackageProfile
                if (profile == null) {
                    EmptyState("请选择一个 profile")
                } else {
                    Text(
                        "Profile：${profile.profileName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "创建时间：${formatTime(profile.createdAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.packageDiffs, key = { diff -> diff.managerId }) { diff ->
                            PackageDiffRow(diff = diff)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DotfilesTab(
    state: KboxStorageToolState,
    modifier: Modifier,
    onRefresh: () -> Unit,
    onImportSelected: () -> Unit,
    onImportManual: () -> Unit,
    onRelink: () -> Unit,
    onRemove: () -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DataPanel(
            title = "候选 Dotfile",
            modifier = Modifier.width(420.dp).fillMaxHeight(),
            actions = {
                Button(onClick = onRefresh, enabled = !state.isBusy) { Text("刷新") }
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.dotfileCandidates, key = { candidate -> candidate.targetPath }) { candidate ->
                    SelectableCardRow(
                        title = candidate.logicalName,
                        subtitle = "${candidate.targetPath}\n${candidate.status.name} · ${candidate.message}",
                        selected = state.selectedDotfileTargetPath == candidate.targetPath,
                        onClick = { state.selectDotfile(candidate.targetPath) },
                    )
                }
            }
        }
        DataPanel(
            title = "托管详情",
            modifier = Modifier.weight(1f).fillMaxHeight(),
            actions = {
                Button(
                    onClick = onImportSelected,
                    enabled = !state.isBusy && state.selectedDotfile != null,
                ) {
                    Text("导入/托管")
                }
                Button(
                    onClick = onRelink,
                    enabled = !state.isBusy && state.selectedDotfile != null,
                ) {
                    Text("重建链接")
                }
                Button(
                    onClick = onRemove,
                    enabled = !state.isBusy && state.selectedDotfile != null,
                ) {
                    Text("取消托管")
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val candidate = state.selectedDotfile
                if (candidate == null) {
                    EmptyState("请选择一个 dotfile")
                } else {
                    MonoLine("逻辑名", candidate.logicalName)
                    MonoLine("目标路径", candidate.targetPath)
                    MonoLine("Canonical", candidate.canonicalPath.ifBlank { "-" })
                    MonoLine("状态", "${candidate.status.name} · ${candidate.message}")
                    Button(onClick = { onOpenFolder(candidate.targetPath) }, enabled = !state.isBusy) {
                        Text("打开所在目录")
                    }
                }
                OutlinedTextField(
                    value = state.dotfileManualPath,
                    onValueChange = { value -> state.dotfileManualPath = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("手工导入路径") },
                )
                OutlinedTextField(
                    value = state.dotfileManualName,
                    onValueChange = { value -> state.dotfileManualName = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("逻辑名（可选）") },
                )
                Button(onClick = onImportManual, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("导入手工路径")
                }
            }
        }
    }
}

@Composable
private fun ComposeTab(
    state: KboxStorageToolState,
    modifier: Modifier,
    onRefresh: () -> Unit,
    onRegister: () -> Unit,
    onRemove: () -> Unit,
    onSaveCompose: () -> Unit,
    onSaveEnv: () -> Unit,
    onRunAction: (KboxComposeAction) -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DataPanel(
            title = "Compose 项目",
            modifier = Modifier.width(380.dp).fillMaxHeight(),
            actions = {
                Button(onClick = onRefresh, enabled = !state.isBusy) { Text("刷新") }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.composeProjectDirInput,
                    onValueChange = { value -> state.composeProjectDirInput = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("项目目录") },
                )
                OutlinedTextField(
                    value = state.composeProjectNameInput,
                    onValueChange = { value -> state.composeProjectNameInput = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("显示名称（可选）") },
                )
                Button(onClick = onRegister, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("注册项目")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.composeProjects, key = { project -> project.config.projectId }) { project ->
                        SelectableCardRow(
                            title = project.config.name,
                            subtitle = "${project.config.directory}\n${project.availability.name} · 服务 ${project.services.size} 个",
                            selected = state.selectedComposeProjectId == project.config.projectId,
                            onClick = { state.selectComposeProject(project.config.projectId) },
                        )
                    }
                }
            }
        }
        DataPanel(
            title = "项目编辑器",
            modifier = Modifier.weight(1f).fillMaxHeight(),
            actions = {
                Button(
                    onClick = onRemove,
                    enabled = !state.isBusy && state.selectedComposeProject != null,
                ) {
                    Text("移除项目")
                }
            },
        ) {
            val project = state.selectedComposeProject
            if (project == null) {
                EmptyState("请选择一个 Compose 项目")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MonoLine("项目目录", project.config.directory)
                    MonoLine("Compose 可用性", "${project.availability.name} · ${project.availabilityMessage}")
                    if (project.services.isNotEmpty()) {
                        MonoLine("服务", project.services.joinToString())
                    }
                    Button(onClick = { onOpenFolder(project.config.directory) }, enabled = !state.isBusy) {
                        Text("打开项目目录")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        project.config.composeFiles.forEach { fileName ->
                            FilterChip(
                                selected = state.selectedComposeFile == fileName,
                                onClick = { state.selectComposeFile(fileName) },
                                label = { Text(fileName) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.composeYamlText,
                        onValueChange = { value -> state.updateComposeYamlText(value) },
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        label = { Text("Compose YAML") },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onSaveCompose, enabled = !state.isBusy) { Text("保存 YAML") }
                        KboxComposeAction.entries.forEach { action ->
                            Button(
                                onClick = { onRunAction(action) },
                                enabled = !state.isBusy,
                            ) {
                                Text(action.displayName)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.composeEnvText,
                        onValueChange = { value -> state.updateComposeEnvText(value) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        label = { Text(".env") },
                    )
                    Button(onClick = onSaveEnv, enabled = !state.isBusy) {
                        Text("保存 .env")
                    }
                    OutlinedTextField(
                        value = state.composeCommandOutput,
                        onValueChange = {},
                        modifier = Modifier.fillMaxSize(),
                        label = { Text("命令输出") },
                        readOnly = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: KboxStorageToolState,
    modifier: Modifier,
    onSave: () -> Unit,
    onTestSsh: () -> Unit,
) {
    val draft = state.draft
    DataPanel(
        title = "全局设置",
        modifier = modifier,
        actions = {
            Button(onClick = onSave, enabled = !state.isBusy) { Text("保存设置") }
            Button(onClick = onTestSsh, enabled = !state.isBusy) { Text("测试 SSH") }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = draft.localAppDataOverride,
                onValueChange = { value -> state.updateDraft { it.copy(localAppDataOverride = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("本地数据目录（留空走系统默认）") },
            )
            MonoLine("迁移预检查", state.migrationPreviewText)
            OutlinedTextField(
                value = draft.installerScanRootsText,
                onValueChange = { value -> state.updateDraft { it.copy(installerScanRootsText = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("安装包扫描目录") },
                minLines = 4,
            )
            OutlinedTextField(
                value = draft.largeFileScanRootsText,
                onValueChange = { value -> state.updateDraft { it.copy(largeFileScanRootsText = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("大文件扫描目录") },
                minLines = 4,
            )
            OutlinedTextField(
                value = draft.largeFileThresholdGbText,
                onValueChange = { value -> state.updateDraft { it.copy(largeFileThresholdGbText = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("大文件阈值（GB）") },
            )
            SettingsToggleRow(
                label = "启用 SSH 远端迁移",
                checked = draft.sshEnabled,
                onCheckedChange = { checked -> state.updateDraft { it.copy(sshEnabled = checked) } },
            )
            OutlinedTextField(
                value = draft.sshHost,
                onValueChange = { value -> state.updateDraft { it.copy(sshHost = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("SSH 主机") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.sshPortText,
                    onValueChange = { value -> state.updateDraft { it.copy(sshPortText = value) } },
                    modifier = Modifier.weight(0.35f),
                    label = { Text("端口") },
                )
                OutlinedTextField(
                    value = draft.sshUsername,
                    onValueChange = { value -> state.updateDraft { it.copy(sshUsername = value) } },
                    modifier = Modifier.weight(0.65f),
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
                    onValueChange = { value -> state.updateDraft { it.copy(sshPassword = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("密码") },
                )
            } else {
                OutlinedTextField(
                    value = draft.sshPrivateKeyPath,
                    onValueChange = { value -> state.updateDraft { it.copy(sshPrivateKeyPath = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("私钥路径") },
                )
                OutlinedTextField(
                    value = draft.sshPrivateKeyPassphrase,
                    onValueChange = { value -> state.updateDraft { it.copy(sshPrivateKeyPassphrase = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("私钥口令") },
                )
            }
            SettingsToggleRow(
                label = "严格主机校验",
                checked = draft.sshStrictHostKeyChecking,
                onCheckedChange = { checked -> state.updateDraft { it.copy(sshStrictHostKeyChecking = checked) } },
            )
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
                onValueChange = { value -> state.updateDraft { it.copy(remoteUserHome = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端用户目录（可选）") },
            )
            OutlinedTextField(
                value = draft.remoteLocalAppData,
                onValueChange = { value -> state.updateDraft { it.copy(remoteLocalAppData = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 LOCALAPPDATA（Windows 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteAppData,
                onValueChange = { value -> state.updateDraft { it.copy(remoteAppData = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 APPDATA（Windows 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteXdgDataHome,
                onValueChange = { value -> state.updateDraft { it.copy(remoteXdgDataHome = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 XDG_DATA_HOME（Linux 可选）") },
            )
            OutlinedTextField(
                value = draft.remoteAppName,
                onValueChange = { value -> state.updateDraft { it.copy(remoteAppName = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("远端 appData 名称") },
            )
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
private fun FileSelectionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primary: String,
    secondary: String,
    onOpenFolder: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                primary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                fontFamily = FontFamily.Monospace,
            )
            Text(
                secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace,
            )
        }
        Button(onClick = onOpenFolder) {
            Text("打开目录")
        }
    }
}

@Composable
private fun PackageDiffRow(
    diff: KboxPackageDiff,
) {
    SimpleCardRow(
        title = diff.displayName,
        subtitle = buildString {
            append("${diff.managerId} · 可用=${diff.available}")
            appendLine()
            append("请求 ${diff.requestedPackages} 个，已存在 ${diff.installedPackages} 个，缺失 ${diff.missingPackages.size} 个")
            if (diff.missingPackages.isNotEmpty()) {
                appendLine()
                append(diff.missingPackages.take(12).joinToString())
                if (diff.missingPackages.size > 12) {
                    append(" ...")
                }
            }
        },
        highlighted = diff.available && diff.missingPackages.isEmpty(),
    )
}

@Composable
private fun SelectableCardRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                },
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun SimpleCardRow(
    title: String,
    subtitle: String,
    highlighted: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                if (highlighted) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                },
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
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
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
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

@Composable
private fun MonoLine(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun EmptyState(
    message: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
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
