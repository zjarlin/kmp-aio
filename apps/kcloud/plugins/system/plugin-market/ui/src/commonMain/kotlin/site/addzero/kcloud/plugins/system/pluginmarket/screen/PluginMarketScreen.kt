package site.addzero.kcloud.plugins.system.pluginmarket.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.button.AddButton
import site.addzero.component.button.AddIconButton
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketWorkbenchState
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginActivationState
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentStatus
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind

@Route(
    value = "插件市场",
    title = "插件源码市场",
    routePath = "system/plugin-market/packages",
    icon = "Apps",
    order = 15.0,
    enabled = false,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统",
            icon = "AdminPanelSettings",
            order = 100,
        ),
    ),
)
@Composable
fun PluginMarketPackagesScreen() {
    val viewModel: PluginMarketPackagesViewModel = koinViewModel()
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state.refreshAll()
        state.selectedPackageId?.let { state.selectPackage(it) }
    }

    DeletePackageConfirmDialog(
        state = state,
        onDismiss = { state.cancelDeletePackage() },
        onConfirm = { scope.launch { state.confirmDeleteSelectedPackage() } },
    )

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PluginMarketTreePanel(
            state = state,
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            onRefresh = { scope.launch { state.refreshAll() } },
            onCreate = { state.beginCreatePackage() },
            onSelectPackage = { id -> scope.launch { state.selectPackage(id) } },
            onSelectDiscovery = { id -> state.selectDiscovery(id) },
            onSelectFile = { id -> state.selectFile(id) },
            onSelectJob = { id -> state.selectJob(id) },
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PluginPackageEditorPanel(
                    state = state,
                    modifier = Modifier.weight(0.9f).fillMaxHeight(),
                    onSave = { scope.launch { state.savePackage() } },
                    onImport = { scope.launch { state.importSelectedDiscovery() } },
                    onEnable = { scope.launch { state.enableSelectedPackage() } },
                    onDisable = { scope.launch { state.disableSelectedPackage() } },
                    onDeploy = { scope.launch { state.deploySelectedPackage() } },
                    onBuild = { scope.launch { state.runBuildForSelectedPackage() } },
                    onDelete = { scope.launch { state.prepareDeleteSelectedPackage() } },
                )
                PluginSourceEditorPanel(
                    state = state,
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    onSave = { scope.launch { state.saveFile() } },
                    onDelete = { scope.launch { state.deleteSelectedFile() } },
                )
            }
            PluginBottomPanel(
                state = state,
                selectedTab = viewModel.selectedBottomTab,
                onTabChange = { viewModel.selectedBottomTab = it },
                modifier = Modifier.fillMaxWidth().height(260.dp),
            )
        }
    }
}

private data class PluginTreeNode(
    val id: String,
    val label: String,
    val kind: String,
    val packageId: String? = null,
    val discoveryId: String? = null,
    val fileId: String? = null,
    val jobId: String? = null,
    val children: List<PluginTreeNode> = emptyList(),
)

@Composable
private fun PluginMarketTreePanel(
    state: PluginMarketWorkbenchState,
    modifier: Modifier,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onSelectPackage: (String?) -> Unit,
    onSelectDiscovery: (String?) -> Unit,
    onSelectFile: (String?) -> Unit,
    onSelectJob: (String?) -> Unit,
    treeViewModel: TreeViewModel<PluginTreeNode> = rememberTreeViewModel(),
) {
    val items = remember(state.packages.toList(), state.discoveries.toList(), state.currentFiles.toList(), state.jobs.toList()) {
        buildPluginTree(state)
    }
    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = PluginTreeNode::id,
            getLabel = PluginTreeNode::label,
            getChildren = PluginTreeNode::children,
        )
    }
    LaunchedEffect(treeViewModel, items) {
        treeViewModel.setItems(items)
    }
    LaunchedEffect(treeViewModel) {
        treeViewModel.onNodeClick = { node ->
            when (node.kind) {
                "package" -> onSelectPackage(node.packageId)
                "discovery" -> onSelectDiscovery(node.discoveryId)
                "file" -> onSelectFile(node.fileId)
                "job" -> onSelectJob(node.jobId)
            }
        }
    }

    PluginMarketPanel(
        title = "插件树",
        modifier = modifier,
    ) {
        AddSearchBar(
            keyword = state.searchKeyword,
            onKeyWordChanged = { state.searchKeyword = it },
            onSearch = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "搜索插件、文件、构建任务",
            leftSloat = {
                AddButton(
                    displayName = "新建",
                    onClick = onCreate,
                )
            },
        )
        AddTree(
            viewModel = treeViewModel,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun PluginPackageEditorPanel(
    state: PluginMarketWorkbenchState,
    modifier: Modifier,
    onSave: () -> Unit,
    onImport: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDeploy: () -> Unit,
    onBuild: () -> Unit,
    onDelete: () -> Unit,
) {
    val selectedPackage = state.selectedPackage
    val selectedDiscovery = state.selectedDiscovery
    PluginMarketPanel(
        title = "插件包",
        modifier = modifier,
        actions = {
            if (selectedPackage != null) {
                AddIconButton(text = "保存元数据", imageVector = Icons.Default.Save, onClick = onSave)
                AddIconButton(text = "导出安装", imageVector = Icons.Default.Upload, onClick = onDeploy)
                if (selectedPackage.activationState == PluginActivationState.DISABLED || !selectedPackage.enabled) {
                    AddIconButton(text = "启用", imageVector = Icons.Default.PlayCircle, onClick = onEnable)
                } else if (selectedPackage.activationState == PluginActivationState.ENABLED) {
                    AddIconButton(text = "停用", imageVector = Icons.Default.PauseCircle, onClick = onDisable)
                } else {
                    AddIconButton(text = "启用", imageVector = Icons.Default.PlayCircle, onClick = onEnable)
                }
                AddIconButton(text = "验证构建", imageVector = Icons.Default.Build, onClick = onBuild)
                AddIconButton(text = "卸载", imageVector = Icons.Default.Delete, onClick = onDelete)
            } else {
                AddIconButton(text = "新建托管插件", imageVector = Icons.Default.Save, onClick = onSave)
                if (selectedDiscovery != null) {
                    AddIconButton(text = "导入发现项", imageVector = Icons.Default.CloudDownload, onClick = onImport)
                }
            }
        },
    ) {
        PackageStateSummary(state = state)
        CompactTextField(
            label = "插件名称",
            value = state.packageName,
            onValueChange = { state.packageName = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                label = "插件 ID",
                value = state.packagePluginId,
                onValueChange = { state.packagePluginId = it },
                modifier = Modifier.weight(1f),
            )
            CompactTextField(
                label = "分组",
                value = state.packageGroup,
                onValueChange = { state.packageGroup = it },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                label = "版本",
                value = state.packageVersion,
                onValueChange = { state.packageVersion = it },
                modifier = Modifier.weight(1f),
            )
            PresetSelector(
                current = state.packagePresetKind,
                onSelect = { state.packagePresetKind = it },
                modifier = Modifier.weight(1f),
            )
        }
        CompactTextField(
            label = "基础包名",
            value = state.packageBasePackage,
            onValueChange = { state.packageBasePackage = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "模块目录",
            value = state.packageModuleDir,
            onValueChange = { state.packageModuleDir = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "Compose Koin 模块类",
            value = state.packageComposeKoinModuleClass,
            onValueChange = { state.packageComposeKoinModuleClass = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "Server Koin 模块类",
            value = state.packageServerKoinModuleClass,
            onValueChange = { state.packageServerKoinModuleClass = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "路由导入",
            value = state.packageRouteRegistrarImport,
            onValueChange = { state.packageRouteRegistrarImport = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "路由调用",
            value = state.packageRouteRegistrarCall,
            onValueChange = { state.packageRouteRegistrarCall = it },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "说明",
            value = state.packageDescription,
            onValueChange = { state.packageDescription = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            minLines = 4,
        )
        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodySmall,
            color = if (state.statusIsError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun PackageStateSummary(
    state: PluginMarketWorkbenchState,
) {
    val selectedPackage = state.selectedPackage
    val selectedDiscovery = state.selectedDiscovery
    val latestJob = selectedPackage?.let { plugin ->
        state.jobs
            .filter { it.packageId == plugin.id }
            .maxByOrNull { it.updatedAt }
    }
    val entries = when {
        selectedPackage != null -> listOf(
            "当前对象" to "${selectedPackage.name} (${selectedPackage.pluginId})",
            "安装状态" to if (selectedPackage.moduleInstalled) "模块目录已存在" else "模块目录未落盘",
            "接线状态" to selectedPackage.activationState.toChineseLabel(),
            "部署开关" to if (selectedPackage.enabled) "允许参与主应用自动聚合" else "已从主应用自动聚合中停用",
            "托管方式" to if (selectedPackage.managedByDb) "数据库托管" else "只读发现",
            "模块目录" to selectedPackage.moduleDir,
            "最近任务" to latestJob?.status?.toChineseLabel().orEmpty().ifBlank { "还没有执行导出或构建" },
        )
        selectedDiscovery != null -> listOf(
            "当前对象" to "磁盘发现项",
            "插件 ID" to selectedDiscovery.pluginId,
            "Gradle 路径" to selectedDiscovery.gradlePath,
            "模块目录" to selectedDiscovery.moduleDir,
        )
        else -> listOf(
            "当前对象" to "新建插件包",
            "说明" to "先填写元数据，再执行“导出安装”生成模块目录并接入主应用",
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            entries.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "$label：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (selectedPackage != null) {
                Text(
                    text = selectedPackage.runtimeHint(latestJob),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PluginSourceEditorPanel(
    state: PluginMarketWorkbenchState,
    modifier: Modifier,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    PluginMarketPanel(
        title = "源码文件",
        modifier = modifier,
        actions = {
            AddIconButton(text = "刷新", imageVector = Icons.Default.Refresh, onClick = {
                state.selectedPackageId?.let {
                    state.selectedFileId?.let(state::selectFile)
                }
            })
            AddIconButton(text = "保存文件", imageVector = Icons.Default.Save, onClick = onSave)
            AddIconButton(text = "删除文件", imageVector = Icons.Default.Delete, onClick = onDelete)
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactTextField(
                label = "相对路径",
                value = state.fileRelativePath,
                onValueChange = { state.fileRelativePath = it },
                modifier = Modifier.weight(1f),
            )
            CompactTextField(
                label = "分组",
                value = state.fileGroup,
                onValueChange = { state.fileGroup = it },
                modifier = Modifier.width(140.dp),
            )
        }
        OutlinedTextField(
            value = state.fileContent,
            onValueChange = { state.fileContent = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            label = { Text("源码内容") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(state.currentFiles, key = { it.id }) { file ->
                    Text(
                        text = file.relativePath,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { state.selectFile(file.id) }
                            .background(
                                if (state.selectedFileId == file.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                                },
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginBottomPanel(
    state: PluginMarketWorkbenchState,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier,
) {
    val tabs = listOf("导出预览", "受管差异", "构建日志", "插件配置")
    PluginMarketPanel(
        title = "运行面板",
        modifier = modifier,
    ) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabChange(index) },
                    text = { Text(title) },
                )
            }
        }
        when (selectedTab) {
            0 -> PreviewBox(state.previewText)
            1 -> PreviewBox(state.diffText.ifBlank { "受管接线差异将在导出时显示" })
            2 -> PreviewBox(state.logsText.ifBlank { "还没有构建日志" })
            else -> PluginConfigEditor(state)
        }
    }
}

@Composable
private fun PluginConfigEditor(
    state: PluginMarketWorkbenchState,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactTextField(
            label = "插件导出根目录",
            value = state.config.exportRootDir,
            onValueChange = { state.updateConfig { config -> config.copy(exportRootDir = it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "Gradle 命令",
            value = state.config.gradleCommand,
            onValueChange = { state.updateConfig { config -> config.copy(gradleCommand = it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "Gradle 任务",
            value = state.config.gradleTasks.joinToString(" "),
            onValueChange = {
                state.updateConfig { config ->
                    config.copy(
                        gradleTasks = it.split(" ").map(String::trim).filter(String::isNotBlank),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "JAVA_HOME",
            value = state.config.javaHome.orEmpty(),
            onValueChange = {
                state.updateConfig { config ->
                    config.copy(javaHome = it.ifBlank { null })
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        CompactTextField(
            label = "环境变量",
            value = state.config.environmentLines.joinToString("\n"),
            onValueChange = {
                state.updateConfig { config ->
                    config.copy(
                        environmentLines = it.lineSequence().map(String::trim).filter(String::isNotBlank).toList(),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("导出后自动构建")
                Switch(
                    checked = state.config.autoBuildEnabled,
                    onCheckedChange = { enabled ->
                        state.updateConfig { config -> config.copy(autoBuildEnabled = enabled) }
                    },
                )
            }
            AddButton(
                displayName = "保存配置",
                onClick = { scope.launch { state.saveConfig() } },
            )
        }
    }
}

@Composable
private fun PreviewBox(
    text: String,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PluginMarketPanel(
    title: String,
    modifier: Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), content = actions)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
private fun CompactTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else Int.MAX_VALUE,
        label = { Text(label) },
        textStyle = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun PresetSelector(
    current: PluginPresetKind,
    onSelect: (PluginPresetKind) -> Unit,
    modifier: Modifier,
) {
    CompactTextField(
        label = "脚手架预设",
        value = current.toChineseLabel(),
        onValueChange = { changed ->
            PluginPresetKind.entries.firstOrNull { it.toChineseLabel() == changed }?.let(onSelect)
        },
        modifier = modifier,
    )
}

private fun buildPluginTree(state: PluginMarketWorkbenchState): List<PluginTreeNode> {
    val keyword = state.searchKeyword.trim()
    fun matchesKeyword(vararg values: String?): Boolean {
        if (keyword.isBlank()) {
            return true
        }
        return values.any { value -> value?.contains(keyword, ignoreCase = true) == true }
    }

    val packageNodes = state.packages
        .filter { plugin ->
            matchesKeyword(
                plugin.name,
                plugin.pluginId,
                plugin.pluginGroup,
                plugin.moduleDir,
                plugin.activationState.toChineseLabel(),
            )
        }
        .map { plugin ->
        PluginTreeNode(
            id = "package/${plugin.id}",
            label = "${plugin.name} (${plugin.pluginId}) · ${plugin.treeStatusLabel()}",
            kind = "package",
            packageId = plugin.id,
            children = state.currentFiles
                .filter { state.selectedPackageId == plugin.id }
                .map { file ->
                    PluginTreeNode(
                        id = "file/${file.id}",
                        label = file.relativePath,
                        kind = "file",
                        fileId = file.id,
                    )
                },
        )
    }
    val discoveryNodes = state.discoveries
        .filter { discovery ->
            matchesKeyword(
                discovery.pluginId,
                discovery.pluginGroup,
                discovery.moduleDir,
                discovery.gradlePath,
            )
        }
        .map { discovery ->
        PluginTreeNode(
            id = "discovery/${discovery.discoveryId}",
            label = discoveryLabel(discovery),
            kind = "discovery",
            discoveryId = discovery.discoveryId,
        )
    }
    val jobNodes = state.jobs
        .filter { job ->
            matchesKeyword(
                job.exportedModuleDir,
                job.status.toChineseLabel(),
                job.summaryText,
            )
        }
        .map { job ->
        PluginTreeNode(
            id = "job/${job.id}",
            label = "${job.exportedModuleDir.substringAfterLast('/')} · ${job.status.toChineseLabel()}",
            kind = "job",
            jobId = job.id,
        )
    }
    return listOf(
        PluginTreeNode(
            id = "root/packages",
            label = "数据库托管",
            kind = "group",
            children = packageNodes,
        ),
        PluginTreeNode(
            id = "root/discovery",
            label = "磁盘发现",
            kind = "group",
            children = discoveryNodes,
        ),
        PluginTreeNode(
            id = "root/jobs",
            label = "构建任务",
            kind = "group",
            children = jobNodes,
        ),
    )
}

@Composable
private fun DeletePackageConfirmDialog(
    state: PluginMarketWorkbenchState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val deleteCheck = state.deleteCheckResult ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认卸载插件包") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("将执行数据库托管插件卸载。")
                deleteCheck.warnings.forEach { warning ->
                    Text("提示：$warning", style = MaterialTheme.typography.bodySmall)
                }
                deleteCheck.blockers.forEach { blocker ->
                    Text(
                        text = "阻断：$blocker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (!deleteCheck.canDelete) {
                    Text(
                        text = "当前存在阻断项，不能继续卸载。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = deleteCheck.canDelete,
            ) {
                Text("确认卸载")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun discoveryLabel(discovery: PluginDiscoveryItemDto): String {
    val issueMark = if (discovery.issues.isEmpty()) "" else " · ${discovery.issues.size} 个问题"
    return "${discovery.pluginId}$issueMark"
}

private fun PluginPresetKind.toChineseLabel(): String {
    return when (this) {
        PluginPresetKind.BLANK -> "空白插件"
        PluginPresetKind.TOOL -> "工具插件"
        PluginPresetKind.ADMIN -> "管理后台插件"
    }
}

private fun PluginDeploymentStatus.toChineseLabel(): String {
    return when (this) {
        PluginDeploymentStatus.PENDING -> "待处理"
        PluginDeploymentStatus.EXPORTED -> "已导出"
        PluginDeploymentStatus.BUILDING -> "构建中"
        PluginDeploymentStatus.SUCCESS -> "成功"
        PluginDeploymentStatus.FAILED -> "失败"
        PluginDeploymentStatus.RESTART_REQUIRED -> "待重启生效"
    }
}

private fun PluginActivationState.toChineseLabel(): String {
    return when (this) {
        PluginActivationState.NOT_INSTALLED -> "未安装"
        PluginActivationState.ENABLED -> "已安装且启用"
        PluginActivationState.DISABLED -> "已安装但停用"
    }
}

private fun PluginPackageDto.treeStatusLabel(): String {
    return when {
        !moduleInstalled -> "未安装"
        disabledByMarker -> "已停用"
        else -> "已启用"
    }
}

private fun PluginPackageDto.runtimeHint(
    latestJob: PluginDeploymentJobDto?,
): String {
    return when {
        !moduleInstalled && enabled -> "当前模块目录还不存在。先执行“导出安装”，主应用下次构建时才会发现这个插件。"
        !moduleInstalled && !enabled -> "当前模块目录还不存在，并且部署开关处于停用状态。若要接入主应用，先启用再执行“导出安装”。"
        disabledByMarker -> "当前插件目录已存在，但受管禁用标记也已存在。下次构建并重启后，主应用不会聚合它。"
        latestJob?.status == PluginDeploymentStatus.RESTART_REQUIRED -> "最近一次验证构建已经通过。重启 KCloud 后，菜单和服务端路由才会按当前源码状态生效。"
        latestJob?.status == PluginDeploymentStatus.FAILED -> "最近一次验证构建失败。先看底部构建日志，再修正源码或接线问题。"
        else -> "当前插件已处于自动聚合路径中。重新构建并重启后，主应用会按当前源码装配菜单与服务端路由。"
    }
}
