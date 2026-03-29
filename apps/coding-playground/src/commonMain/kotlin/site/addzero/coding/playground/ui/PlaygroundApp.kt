package site.addzero.coding.playground.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import site.addzero.coding.playground.PlaygroundWorkbenchState
import site.addzero.coding.playground.shared.dto.CodeVisibility
import site.addzero.coding.playground.shared.dto.CodegenProjectDto
import site.addzero.coding.playground.shared.dto.ConflictReason
import site.addzero.coding.playground.shared.dto.CreateCodegenProjectRequest
import site.addzero.coding.playground.shared.dto.CreateDeclarationRequest
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetRequest
import site.addzero.coding.playground.shared.dto.CreateSourceFileRequest
import site.addzero.coding.playground.shared.dto.DeclarationKind
import site.addzero.coding.playground.shared.dto.DeclarationMetaDto
import site.addzero.coding.playground.shared.dto.FunctionBodyMode
import site.addzero.coding.playground.shared.dto.GenerationTargetDto
import site.addzero.coding.playground.shared.dto.ManagedArtifactMetaDto
import site.addzero.coding.playground.shared.dto.PropertyMetaDto
import site.addzero.coding.playground.shared.dto.ScenePresetKind
import site.addzero.coding.playground.shared.dto.SourceFileMetaDto
import site.addzero.coding.playground.shared.dto.SyncConflictMetaDto
import site.addzero.coding.playground.shared.dto.SyncConflictResolution
import site.addzero.coding.playground.shared.dto.ValidationIssueDto
import site.addzero.coding.playground.shared.dto.ValidationSeverity
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel

private enum class InspectorTab(val title: String) {
    SOURCE_PREVIEW("源码预览"),
    SYNC_STATUS("同步状态"),
    CONFLICTS("冲突列表"),
    KSP_INDEX("KSP 索引"),
    VALIDATION("校验结果"),
}

private enum class PresetCreateMode(val title: String) {
    DECLARATION("单声明"),
    SCENE("场景预设"),
}

private data class PendingDelete(
    val title: String,
    val message: String,
    val onConfirm: suspend () -> Unit,
)

private enum class ExplorerNodeType {
    PROJECT,
    TARGET,
    FILE,
    DECLARATION,
}

private data class ExplorerNode(
    val treeId: String,
    val entityId: String,
    val type: ExplorerNodeType,
    val label: String,
    val children: List<ExplorerNode> = emptyList(),
)

@Composable
fun PlaygroundApp(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    var inspectorTab by remember { mutableStateOf(InspectorTab.SOURCE_PREVIEW) }
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }

    val selectedProject = state.projects.firstOrNull { it.id == state.selectedProjectId }
    val selectedTarget = state.targets.firstOrNull { it.id == state.selectedTargetId }
    val selectedFile = state.files.firstOrNull { it.id == state.selectedFileId }
    val selectedDeclaration = state.declarations.firstOrNull { it.id == state.selectedDeclarationId }
    val fileAggregate = state.fileAggregate

    val selectedConstructorParams = fileAggregate?.constructorParams.orEmpty()
        .filter { it.declarationId == selectedDeclaration?.id }
    val selectedProperties = fileAggregate?.properties.orEmpty()
        .filter { it.declarationId == selectedDeclaration?.id }
    val selectedEnumEntries = fileAggregate?.enumEntries.orEmpty()
        .filter { it.declarationId == selectedDeclaration?.id }
    val selectedFunctionStubs = fileAggregate?.functionStubs.orEmpty()
        .filter { it.declarationId == selectedDeclaration?.id }
    val selectedAnnotations = fileAggregate?.annotations.orEmpty()
        .filter { it.ownerId == selectedDeclaration?.id }
    val annotationArguments = fileAggregate?.annotationArguments.orEmpty()
        .groupBy { it.annotationUsageId }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E6BAA),
            secondary = Color(0xFF5E748B),
            surface = Color(0xFFF7F9FC),
            background = Color(0xFFEFF3F8),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ExplorerPane(
                    state = state,
                    selectedProject = selectedProject,
                    selectedTarget = selectedTarget,
                    selectedFile = selectedFile,
                    selectedDeclaration = selectedDeclaration,
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                )

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkbenchHeader(
                        selectedProject = selectedProject,
                        selectedTarget = selectedTarget,
                        selectedFile = selectedFile,
                        selectedDeclaration = selectedDeclaration,
                        statusMessage = state.statusMessage,
                        statusIsError = state.statusIsError,
                        autoPreviewEnabled = state.autoPreviewEnabled,
                        autoWriteEnabled = state.autoWriteEnabled,
                        autoImportExternalEnabled = state.autoImportExternalEnabled,
                        autoSyncInProgress = state.autoSyncInProgress,
                        onRefreshPreview = { scope.launchWorkbenchAction(state) { state.refreshPreview() } },
                        onExport = { scope.launchWorkbenchAction(state) { state.exportSelectedFile() } },
                        onImport = { scope.launchWorkbenchAction(state) { state.importSelectedFile() } },
                        onToggleAutoPreview = { state.updateAutoSyncSettings(autoPreviewEnabled = it) },
                        onToggleAutoWrite = { state.updateAutoSyncSettings(autoWriteEnabled = it) },
                        onToggleAutoImport = { state.updateAutoSyncSettings(autoImportExternalEnabled = it) },
                    )

                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        EditorPane(
                            state = state,
                            selectedProject = selectedProject,
                            selectedTarget = selectedTarget,
                            selectedFile = selectedFile,
                            selectedDeclaration = selectedDeclaration,
                            selectedProperties = selectedProperties,
                            selectedConstructorParams = selectedConstructorParams,
                            selectedEnumEntries = selectedEnumEntries,
                            selectedFunctionStubs = selectedFunctionStubs,
                            selectedAnnotations = selectedAnnotations,
                            annotationArguments = annotationArguments,
                            onDeleteProject = {
                                pendingDelete = PendingDelete(
                                    title = "删除项目",
                                    message = "将删除项目“${selectedProject?.name ?: ""}”以及其下的目标、文件和声明，是否继续？",
                                    onConfirm = { state.deleteSelectedProject() },
                                )
                            },
                            onDeleteTarget = {
                                pendingDelete = PendingDelete(
                                    title = "删除生成目标",
                                    message = "将删除目标“${selectedTarget?.name ?: ""}”以及其下的文件和声明，是否继续？",
                                    onConfirm = { state.deleteSelectedTarget() },
                                )
                            },
                            onDeleteFile = {
                                pendingDelete = PendingDelete(
                                    title = "删除 Kotlin 文件",
                                    message = "将删除文件“${selectedFile?.fileName ?: ""}”及其声明，是否继续？",
                                    onConfirm = { state.deleteSelectedFile() },
                                )
                            },
                            onDeleteDeclaration = {
                                pendingDelete = PendingDelete(
                                    title = "删除声明",
                                    message = "将删除声明“${selectedDeclaration?.name ?: ""}”及其子节点，是否继续？",
                                    onConfirm = { state.deleteSelectedDeclaration() },
                                )
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )

                        InspectorPane(
                            selectedTab = inspectorTab,
                            onSelectTab = { inspectorTab = it },
                            sourcePreview = state.sourcePreview?.content,
                            kspPreview = state.kspPreview?.content,
                            outputPath = state.sourcePreview?.outputPath,
                            metadataHash = state.sourcePreview?.metadataHash,
                            contentHash = state.sourcePreview?.contentHash,
                            targetSourceRoot = state.targetPathPreview?.sourceRoot,
                            artifacts = state.artifacts,
                            conflicts = state.conflicts,
                            validationIssues = state.validationIssues,
                            onResolveConflict = { conflictId, resolution ->
                                scope.launchWorkbenchAction(state) {
                                    state.resolveConflict(conflictId, resolution)
                                }
                            },
                            modifier = Modifier.width(420.dp).fillMaxHeight(),
                        )
                    }
                }
            }
        }

        pendingDelete?.let { delete ->
            ConfirmDeleteDialog(
                title = delete.title,
                message = delete.message,
                onDismiss = { pendingDelete = null },
                onConfirm = {
                    pendingDelete = null
                    scope.launchWorkbenchAction(state) {
                        delete.onConfirm()
                    }
                },
            )
        }
    }
}

@Composable
private fun ExplorerPane(
    state: PlaygroundWorkbenchState,
    selectedProject: CodegenProjectDto?,
    selectedTarget: GenerationTargetDto?,
    selectedFile: SourceFileMetaDto?,
    selectedDeclaration: DeclarationMetaDto?,
    treeViewModel: TreeViewModel<ExplorerNode> = rememberTreeViewModel(),
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val nodes = remember(
        state.projects,
        state.projectAggregate,
        selectedProject?.id,
    ) {
        buildExplorerNodes(
            projects = state.projects,
            selectedProjectId = selectedProject?.id,
            aggregate = state.projectAggregate,
        )
    }
    val selectedTreeId = remember(
        selectedProject?.id,
        selectedTarget?.id,
        selectedFile?.id,
        selectedDeclaration?.id,
    ) {
        selectedExplorerNodeId(
            projectId = selectedProject?.id,
            targetId = selectedTarget?.id,
            fileId = selectedFile?.id,
            declarationId = selectedDeclaration?.id,
        )
    }
    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = ExplorerNode::treeId,
            getLabel = ExplorerNode::label,
            getChildren = ExplorerNode::children,
        )
    }

    SideEffect {
        treeViewModel.onNodeClick = { node ->
            scope.launchWorkbenchAction(state) {
                handleExplorerNodeClick(state, node)
            }
        }
    }

    LaunchedEffect(treeViewModel, nodes) {
        treeViewModel.setItems(
            newItems = nodes,
            initiallyExpandedIds = nodes.expandedBranchIdsForSelection(selectedTreeId),
        )
    }

    LaunchedEffect(treeViewModel, selectedTreeId) {
        treeViewModel.selectNode(selectedTreeId)
    }

    LaunchedEffect(treeViewModel, state.searchQuery) {
        if (treeViewModel.searchQuery != state.searchQuery) {
            treeViewModel.updateSearchQuery(state.searchQuery)
            if (state.searchQuery.isNotBlank()) {
                treeViewModel.performSearch()
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("项目资源树", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AddSearchBar(
                keyword = state.searchQuery,
                onKeyWordChanged = { query ->
                    state.searchQuery = query
                    treeViewModel.updateSearchQuery(query)
                    if (query.isNotBlank()) {
                        treeViewModel.performSearch()
                    }
                },
                onSearch = {
                    scope.launchWorkbenchAction(state) {
                        state.refreshAll()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "搜索项目、目标、文件、声明",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.refreshAll()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("刷新")
                }
                OutlinedButton(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.saveProject(
                                selectedId = null,
                                request = CreateCodegenProjectRequest(
                                    name = state.createDefaultProjectName(),
                                    description = "新的 Kotlin 声明式代码生成项目",
                                ),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("新建项目")
                }
            }
            HorizontalDivider()
            if (state.projects.isEmpty()) {
                EmptyHint("还没有项目，先点“新建项目”开始。")
            } else {
                AddTree(
                    viewModel = treeViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
            HorizontalDivider()
            Text(
                text = state.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun WorkbenchHeader(
    selectedProject: CodegenProjectDto?,
    selectedTarget: GenerationTargetDto?,
    selectedFile: SourceFileMetaDto?,
    selectedDeclaration: DeclarationMetaDto?,
    statusMessage: String,
    statusIsError: Boolean,
    autoPreviewEnabled: Boolean,
    autoWriteEnabled: Boolean,
    autoImportExternalEnabled: Boolean,
    autoSyncInProgress: Boolean,
    onRefreshPreview: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onToggleAutoPreview: (Boolean) -> Unit,
    onToggleAutoWrite: (Boolean) -> Unit,
    onToggleAutoImport: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kotlin 声明式代码生成台", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    listOfNotNull(
                        selectedProject?.name,
                        selectedTarget?.name,
                        selectedFile?.fileName,
                        selectedDeclaration?.name,
                    ).joinToString(" / ").ifBlank { "请选择左侧节点开始编辑" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (statusIsError) Color(0xFFB91C1C) else MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onRefreshPreview) {
                        Text("刷新预览")
                    }
                    Button(onClick = onExport) {
                        Text("写盘")
                    }
                    OutlinedButton(onClick = onImport) {
                        Text("导回")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    HeaderSwitch("自动预览", autoPreviewEnabled, onToggleAutoPreview)
                    HeaderSwitch("自动写盘", autoWriteEnabled, onToggleAutoWrite)
                    HeaderSwitch("自动导回", autoImportExternalEnabled, onToggleAutoImport)
                }
                Text(
                    text = if (autoSyncInProgress) "自动同步处理中" else "自动导回按 2 秒轮询托管文件外部改动",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun HeaderSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EditorPane(
    state: PlaygroundWorkbenchState,
    selectedProject: CodegenProjectDto?,
    selectedTarget: GenerationTargetDto?,
    selectedFile: SourceFileMetaDto?,
    selectedDeclaration: DeclarationMetaDto?,
    selectedProperties: List<PropertyMetaDto>,
    selectedConstructorParams: List<site.addzero.coding.playground.shared.dto.ConstructorParamMetaDto>,
    selectedEnumEntries: List<site.addzero.coding.playground.shared.dto.EnumEntryMetaDto>,
    selectedFunctionStubs: List<site.addzero.coding.playground.shared.dto.FunctionStubMetaDto>,
    selectedAnnotations: List<site.addzero.coding.playground.shared.dto.AnnotationUsageMetaDto>,
    annotationArguments: Map<String, List<site.addzero.coding.playground.shared.dto.AnnotationArgumentMetaDto>>,
    onDeleteProject: () -> Unit,
    onDeleteTarget: () -> Unit,
    onDeleteFile: () -> Unit,
    onDeleteDeclaration: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PresetCreateCard(state, selectedTarget)
            ProjectEditorCard(
                selectedProject = selectedProject,
                onSave = { request ->
                    scope.launchWorkbenchAction(state) {
                        val selectedId = selectedProject?.id
                        if (selectedId == null) {
                            state.saveProject(null, CreateCodegenProjectRequest(request.name, request.description))
                        } else {
                            state.saveProject(
                                selectedId,
                                CreateCodegenProjectRequest(request.name, request.description),
                            )
                        }
                    }
                },
                onDelete = onDeleteProject,
            )
            TargetEditorCard(
                state = state,
                selectedProject = selectedProject,
                selectedTarget = selectedTarget,
                onSave = { request ->
                    scope.launchWorkbenchAction(state) {
                        val selectedId = selectedTarget?.id
                        val projectId = selectedProject?.id ?: return@launchWorkbenchAction
                        if (selectedId == null) {
                            state.saveTarget(
                                null,
                                CreateGenerationTargetRequest(
                                    projectId = projectId,
                                    name = request.name,
                                    rootDir = request.rootDir,
                                    sourceSet = request.sourceSet,
                                    basePackage = request.basePackage,
                                    indexPackage = request.indexPackage,
                                    kspEnabled = request.kspEnabled,
                                    variables = request.variables,
                                ),
                            )
                        } else {
                            state.saveTarget(
                                selectedId,
                                CreateGenerationTargetRequest(
                                    projectId = projectId,
                                    name = request.name,
                                    rootDir = request.rootDir,
                                    sourceSet = request.sourceSet,
                                    basePackage = request.basePackage,
                                    indexPackage = request.indexPackage,
                                    kspEnabled = request.kspEnabled,
                                    variables = request.variables,
                                ),
                            )
                        }
                    }
                },
                onDelete = onDeleteTarget,
            )
            FileEditorCard(
                state = state,
                selectedTarget = selectedTarget,
                selectedFile = selectedFile,
                onDelete = onDeleteFile,
            )
            DeclarationEditorCard(
                state = state,
                selectedFile = selectedFile,
                selectedDeclaration = selectedDeclaration,
                onDelete = onDeleteDeclaration,
            )

            if (selectedDeclaration != null) {
                DeclarationContentCard(
                    state = state,
                    selectedDeclaration = selectedDeclaration,
                    selectedProperties = selectedProperties,
                    selectedConstructorParams = selectedConstructorParams,
                    selectedEnumEntries = selectedEnumEntries,
                    selectedFunctionStubs = selectedFunctionStubs,
                    selectedAnnotations = selectedAnnotations,
                    annotationArguments = annotationArguments,
                )
            } else {
                EmptyHint("选中一个声明后，就能继续编辑构造参数、属性、枚举项、函数桩和注解。")
            }
        }
    }
}

@Composable
private fun PresetCreateCard(
    state: PlaygroundWorkbenchState,
    selectedTarget: GenerationTargetDto?,
) {
    val scope = rememberCoroutineScope()
    var createMode by remember { mutableStateOf(PresetCreateMode.SCENE) }
    var declarationName by remember(selectedTarget?.id) { mutableStateOf("SampleModel") }
    var featureName by remember(selectedTarget?.id) { mutableStateOf("SampleFeature") }
    var packageName by remember(selectedTarget?.id) { mutableStateOf(selectedTarget?.basePackage ?: "") }
    var kind by remember(selectedTarget?.id) { mutableStateOf(DeclarationKind.DATA_CLASS) }
    var scenePreset by remember(selectedTarget?.id) { mutableStateOf(ScenePresetKind.BUSINESS_CRUD) }
    var routeSegment by remember(selectedTarget?.id) { mutableStateOf("") }
    var sceneTitle by remember(selectedTarget?.id) { mutableStateOf("") }

    SectionCard(title = "快速预设") {
        Text(
            "先选中一个生成目标。业务 CRUD、skills / dotfiles 和 KCloud 页面壳优先用场景预设；只建单个 Kotlin 声明时再切回单声明模式。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        PrimaryTabRow(selectedTabIndex = createMode.ordinal) {
            PresetCreateMode.entries.forEach { mode ->
                Tab(
                    selected = createMode == mode,
                    onClick = { createMode = mode },
                    text = { Text(mode.title) },
                )
            }
        }
        if (createMode == PresetCreateMode.DECLARATION) {
            FormGroupTitle("必填项")
            CompactFieldRow(
                left = {
                    FieldLabel("预设类型", required = true)
                    EnumDropdownField(
                        value = kind,
                        values = DeclarationKind.entries,
                        labelOf = { it.label() },
                        onSelected = { kind = it },
                    )
                },
                right = {
                    FieldLabel("声明名", required = true)
                    OutlinedTextField(
                        value = declarationName,
                        onValueChange = { declarationName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = selectedTarget != null,
                    )
                },
            )
            FieldLabel("包名", required = true)
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = selectedTarget != null,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.createPreset(kind, declarationName.trim(), packageName.trim())
                        }
                    },
                    enabled = selectedTarget != null && declarationName.isNotBlank() && packageName.isNotBlank(),
                ) {
                    Text("创建声明预设")
                }
                selectedTarget?.let {
                    Text(
                        text = "目标包前缀：${it.basePackage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
        } else {
            FormGroupTitle("必填项")
            CompactFieldRow(
                left = {
                    FieldLabel("场景类型", required = true)
                    EnumDropdownField(
                        value = scenePreset,
                        values = ScenePresetKind.entries,
                        labelOf = { it.label() },
                        onSelected = { scenePreset = it },
                    )
                },
                right = {
                    FieldLabel("特征名", required = true)
                    OutlinedTextField(
                        value = featureName,
                        onValueChange = { featureName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = selectedTarget != null,
                    )
                },
            )
            FieldLabel("包名", required = true)
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = selectedTarget != null,
            )
            OptionalFieldsSection(
                rememberKey = "scene-preset-${selectedTarget?.id}",
                defaultExpanded = routeSegment.isNotBlank() || sceneTitle.isNotBlank(),
                collapsedHint = "路由段和场景标题默认自动推导，需要覆盖时再展开。",
            ) {
                CompactFieldRow(
                    left = {
                        FieldLabel("路由段")
                        OutlinedTextField(
                            value = routeSegment,
                            onValueChange = { routeSegment = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = selectedTarget != null,
                        )
                    },
                    right = {
                        FieldLabel("场景标题")
                        OutlinedTextField(
                            value = sceneTitle,
                            onValueChange = { sceneTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = selectedTarget != null,
                        )
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.createScenePreset(
                                preset = scenePreset,
                                featureName = featureName.trim(),
                                packageName = packageName.trim(),
                                routeSegment = routeSegment.trim().ifBlank { null },
                                sceneTitle = sceneTitle.trim().ifBlank { null },
                            )
                        }
                    },
                    enabled = selectedTarget != null && featureName.isNotBlank() && packageName.isNotBlank(),
                ) {
                    Text("创建场景预设")
                }
                selectedTarget?.let {
                    Text(
                        text = "当前 sourceSet：${it.sourceSet}，若同根目录存在 commonMain / jvmMain 兄弟目标，会自动联动生成。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}

private data class ProjectFormState(
    val name: String,
    val description: String?,
)

@Composable
private fun ProjectEditorCard(
    selectedProject: CodegenProjectDto?,
    onSave: (ProjectFormState) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(selectedProject?.id) { mutableStateOf(selectedProject?.name ?: "") }
    var description by remember(selectedProject?.id) { mutableStateOf(selectedProject?.description.orEmpty()) }

    SectionCard(title = "项目") {
        FormGroupTitle("必填项")
        FieldLabel("项目名称", required = true)
        OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OptionalFieldsSection(
            rememberKey = selectedProject?.id,
            defaultExpanded = !selectedProject?.description.isNullOrBlank(),
            collapsedHint = "说明默认留空，需要时再展开。",
        ) {
            FieldLabel("说明")
            OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onSave(ProjectFormState(name = name.trim(), description = description.ifBlank { null })) },
                enabled = name.isNotBlank(),
            ) {
                Text(if (selectedProject == null) "保存新项目" else "更新项目")
            }
            if (selectedProject != null) {
                OutlinedButton(onClick = onDelete) {
                    Text("删除项目")
                }
            }
        }
    }
}

private data class TargetFormState(
    val name: String,
    val rootDir: String,
    val sourceSet: String,
    val basePackage: String,
    val indexPackage: String,
    val kspEnabled: Boolean,
    val variables: Map<String, String>,
)

@Composable
private fun TargetEditorCard(
    state: PlaygroundWorkbenchState,
    selectedProject: CodegenProjectDto?,
    selectedTarget: GenerationTargetDto?,
    onSave: (TargetFormState) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(selectedTarget?.id, selectedProject?.id) { mutableStateOf(selectedTarget?.name ?: "main-target") }
    var rootDir by remember(selectedTarget?.id, selectedProject?.id) { mutableStateOf(selectedTarget?.rootDir ?: System.getProperty("user.dir")) }
    var sourceSet by remember(selectedTarget?.id) { mutableStateOf(selectedTarget?.sourceSet ?: "main") }
    var basePackage by remember(selectedTarget?.id) { mutableStateOf(selectedTarget?.basePackage ?: "site.addzero.generated") }
    var indexPackage by remember(selectedTarget?.id) { mutableStateOf(selectedTarget?.indexPackage ?: "site.addzero.generated.index") }
    var kspEnabled by remember(selectedTarget?.id) { mutableStateOf(selectedTarget?.kspEnabled ?: true) }
    var variablesText by remember(selectedTarget?.id) { mutableStateOf(formatMapLines(selectedTarget?.variables.orEmpty())) }

    SectionCard(title = "生成目标") {
        if (selectedProject == null) {
            EmptyHint("先创建并选中项目，才能配置生成目标。")
        } else {
            FormGroupTitle("必填项")
            CompactFieldRow(
                left = {
                    FieldLabel("目标名称", required = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
                right = {
                    FieldLabel("源码集", required = true)
                    OutlinedTextField(value = sourceSet, onValueChange = { sourceSet = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
            )
            FieldLabel("根目录", required = true)
            OutlinedTextField(value = rootDir, onValueChange = { rootDir = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            CompactFieldRow(
                left = {
                    FieldLabel("基础包名", required = true)
                    OutlinedTextField(value = basePackage, onValueChange = { basePackage = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
                right = {
                    FieldLabel("索引包名", required = true)
                    OutlinedTextField(value = indexPackage, onValueChange = { indexPackage = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
            )
            OptionalFieldsSection(
                rememberKey = selectedTarget?.id,
                defaultExpanded = selectedTarget?.variables?.isNotEmpty() == true || selectedTarget?.kspEnabled == false,
                collapsedHint = "高级配置默认收起，避免干扰核心定义。",
            ) {
                CompactSwitchField(
                    label = "启用 KSP 索引伴生",
                    description = "开启后，目标工程可通过注解处理器生成静态索引。",
                    checked = kspEnabled,
                    onCheckedChange = { kspEnabled = it },
                )
                FieldLabel("自定义变量")
                OutlinedTextField(
                    value = variablesText,
                    onValueChange = { variablesText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    supportingText = { Text("每行一个键值对，格式为 key=value") },
                )
            }
            state.targetPathPreview?.let {
                InfoBlock(
                    title = "当前路径预览",
                    lines = listOf("解析根目录：${it.resolvedRootDir}", "源码输出根：${it.sourceRoot}"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSave(
                            TargetFormState(
                                name = name.trim(),
                                rootDir = rootDir.trim(),
                                sourceSet = sourceSet.trim(),
                                basePackage = basePackage.trim(),
                                indexPackage = indexPackage.trim(),
                                kspEnabled = kspEnabled,
                                variables = parseMapLines(variablesText),
                            ),
                        )
                    },
                    enabled = name.isNotBlank() && rootDir.isNotBlank() && sourceSet.isNotBlank() && basePackage.isNotBlank() && indexPackage.isNotBlank(),
                ) {
                    Text(if (selectedTarget == null) "保存新目标" else "更新目标")
                }
                if (selectedTarget != null) {
                    OutlinedButton(onClick = onDelete) {
                        Text("删除目标")
                    }
                }
            }
        }
    }
}

@Composable
private fun FileEditorCard(
    state: PlaygroundWorkbenchState,
    selectedTarget: GenerationTargetDto?,
    selectedFile: SourceFileMetaDto?,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val imports = state.fileAggregate?.imports.orEmpty()
    var packageName by remember(selectedFile?.id, selectedTarget?.id) { mutableStateOf(selectedFile?.packageName ?: selectedTarget?.basePackage.orEmpty()) }
    var fileName by remember(selectedFile?.id) { mutableStateOf(selectedFile?.fileName ?: "Sample.kt") }
    var docComment by remember(selectedFile?.id) { mutableStateOf(selectedFile?.docComment.orEmpty()) }
    var importPath by remember(selectedFile?.id) { mutableStateOf("") }
    var importAlias by remember(selectedFile?.id) { mutableStateOf("") }

    SectionCard(title = "Kotlin 文件") {
        if (selectedTarget == null) {
            EmptyHint("先选中生成目标，才能创建 Kotlin 文件。")
        } else {
            FormGroupTitle("必填项")
            CompactFieldRow(
                left = {
                    FieldLabel("包名", required = true)
                    OutlinedTextField(value = packageName, onValueChange = { packageName = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
                right = {
                    FieldLabel("文件名", required = true)
                    OutlinedTextField(value = fileName, onValueChange = { fileName = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.saveFile(
                                selectedFile?.id,
                                CreateSourceFileRequest(
                                    targetId = selectedTarget.id,
                                    packageName = packageName.trim(),
                                    fileName = fileName.trim(),
                                    docComment = docComment.ifBlank { null },
                                ),
                            )
                        }
                    },
                    enabled = packageName.isNotBlank() && fileName.isNotBlank(),
                ) {
                    Text(if (selectedFile == null) "保存新文件" else "更新文件")
                }
                if (selectedFile != null) {
                    OutlinedButton(onClick = onDelete) {
                        Text("删除文件")
                    }
                }
            }
            OptionalFieldsSection(
                rememberKey = selectedFile?.id,
                defaultExpanded = docComment.isNotBlank() || imports.isNotEmpty(),
                collapsedHint = "文档说明和导包默认隐藏，需要时再展开。",
            ) {
                FieldLabel("文档说明")
                OutlinedTextField(value = docComment, onValueChange = { docComment = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Text("导包", fontWeight = FontWeight.SemiBold)
                imports.forEach { importItem ->
                    TokenRow(
                        primary = importItem.importPath,
                        secondary = importItem.alias?.let { "别名：$it" } ?: "普通导包",
                        onDelete = {
                            scope.launchWorkbenchAction(state) {
                                state.deleteImport(importItem.id)
                            }
                        },
                    )
                }
                CompactFieldRow(
                    left = {
                        FieldLabel("新增导包")
                        OutlinedTextField(value = importPath, onValueChange = { importPath = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    },
                    right = {
                        FieldLabel("导包别名")
                        OutlinedTextField(value = importAlias, onValueChange = { importAlias = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    },
                )
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.addImport(importPath.trim(), importAlias.ifBlank { null })
                            importPath = ""
                            importAlias = ""
                        }
                    },
                    enabled = selectedFile != null && importPath.isNotBlank(),
                ) {
                    Text("添加导包")
                }
            }
        }
    }
}

@Composable
private fun DeclarationEditorCard(
    state: PlaygroundWorkbenchState,
    selectedFile: SourceFileMetaDto?,
    selectedDeclaration: DeclarationMetaDto?,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember(selectedDeclaration?.id, selectedFile?.id) { mutableStateOf(selectedDeclaration?.name ?: "SampleDeclaration") }
    var kind by remember(selectedDeclaration?.id, selectedFile?.id) { mutableStateOf(selectedDeclaration?.kind ?: DeclarationKind.DATA_CLASS) }
    var visibility by remember(selectedDeclaration?.id) { mutableStateOf(selectedDeclaration?.visibility ?: CodeVisibility.PUBLIC) }
    var modifiersText by remember(selectedDeclaration?.id) { mutableStateOf(selectedDeclaration?.modifiers?.joinToString(", ").orEmpty()) }
    var superTypesText by remember(selectedDeclaration?.id) { mutableStateOf(selectedDeclaration?.superTypes?.joinToString(", ").orEmpty()) }
    var docComment by remember(selectedDeclaration?.id) { mutableStateOf(selectedDeclaration?.docComment.orEmpty()) }

    LaunchedEffect(selectedDeclaration?.id) {
        selectedDeclaration?.kind?.let { kind = it }
    }

    SectionCard(title = "声明") {
        if (selectedFile == null) {
            EmptyHint("先选中或创建一个 Kotlin 文件，才能继续建模声明。")
        } else {
            FormGroupTitle("必填项")
            CompactFieldRow(
                left = {
                    FieldLabel("声明类型", required = true)
                    EnumDropdownField(
                        value = kind,
                        values = DeclarationKind.entries,
                        labelOf = { it.label() },
                        onSelected = { if (selectedDeclaration == null) kind = it },
                        enabled = selectedDeclaration == null,
                    )
                },
                right = {
                    FieldLabel("声明名称", required = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
            )
            FieldLabel("可见性", required = true)
            EnumDropdownField(
                value = visibility,
                values = CodeVisibility.entries,
                labelOf = { it.label() },
                onSelected = { visibility = it },
            )
            OptionalFieldsSection(
                rememberKey = selectedDeclaration?.id,
                defaultExpanded = docComment.isNotBlank() || modifiersText.isNotBlank() || superTypesText.isNotBlank(),
                collapsedHint = "文档、修饰符和继承列表默认收起。",
            ) {
                FieldLabel("文档说明")
                OutlinedTextField(value = docComment, onValueChange = { docComment = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                FieldLabel("修饰符")
                OutlinedTextField(
                    value = modifiersText,
                    onValueChange = { modifiersText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("使用逗号分隔，例如 sealed, expect") },
                )
                FieldLabel("继承或实现")
                OutlinedTextField(
                    value = superTypesText,
                    onValueChange = { superTypesText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("使用逗号分隔，例如 Serializable, BaseModel") },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launchWorkbenchAction(state) {
                            state.saveDeclaration(
                                selectedDeclaration?.id,
                                CreateDeclarationRequest(
                                    fileId = selectedFile.id,
                                    name = name.trim(),
                                    kind = selectedDeclaration?.kind ?: kind,
                                    visibility = visibility,
                                    modifiers = parseCsvList(modifiersText),
                                    superTypes = parseCsvList(superTypesText),
                                    docComment = docComment.ifBlank { null },
                                ),
                            )
                        }
                    },
                    enabled = name.isNotBlank(),
                ) {
                    Text(if (selectedDeclaration == null) "保存新声明" else "更新声明")
                }
                if (selectedDeclaration != null) {
                    OutlinedButton(onClick = onDelete) {
                        Text("删除声明")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeclarationContentCard(
    state: PlaygroundWorkbenchState,
    selectedDeclaration: DeclarationMetaDto,
    selectedProperties: List<PropertyMetaDto>,
    selectedConstructorParams: List<site.addzero.coding.playground.shared.dto.ConstructorParamMetaDto>,
    selectedEnumEntries: List<site.addzero.coding.playground.shared.dto.EnumEntryMetaDto>,
    selectedFunctionStubs: List<site.addzero.coding.playground.shared.dto.FunctionStubMetaDto>,
    selectedAnnotations: List<site.addzero.coding.playground.shared.dto.AnnotationUsageMetaDto>,
    annotationArguments: Map<String, List<site.addzero.coding.playground.shared.dto.AnnotationArgumentMetaDto>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnnotationEditorCard(
            state = state,
            selectedAnnotations = selectedAnnotations,
            annotationArguments = annotationArguments,
        )
        if (selectedDeclaration.kind == DeclarationKind.DATA_CLASS ||
            selectedDeclaration.kind == DeclarationKind.CLASS ||
            selectedDeclaration.kind == DeclarationKind.ANNOTATION_CLASS
        ) {
            ConstructorParamCard(state, selectedDeclaration, selectedConstructorParams)
        }
        if (selectedDeclaration.kind == DeclarationKind.DATA_CLASS ||
            selectedDeclaration.kind == DeclarationKind.CLASS ||
            selectedDeclaration.kind == DeclarationKind.INTERFACE ||
            selectedDeclaration.kind == DeclarationKind.OBJECT
        ) {
            PropertyEditorCard(state, selectedDeclaration, selectedProperties)
            FunctionEditorCard(state, selectedDeclaration, selectedFunctionStubs)
        }
        if (selectedDeclaration.kind == DeclarationKind.ENUM_CLASS) {
            EnumEntryEditorCard(state, selectedDeclaration, selectedEnumEntries)
        }
    }
}

@Composable
private fun AnnotationEditorCard(
    state: PlaygroundWorkbenchState,
    selectedAnnotations: List<site.addzero.coding.playground.shared.dto.AnnotationUsageMetaDto>,
    annotationArguments: Map<String, List<site.addzero.coding.playground.shared.dto.AnnotationArgumentMetaDto>>,
) {
    val scope = rememberCoroutineScope()
    var annotationClassName by remember { mutableStateOf("") }
    var argsText by remember { mutableStateOf("") }

    SectionCard(title = "声明注解") {
        selectedAnnotations.forEach { usage ->
            val arguments = annotationArguments[usage.id].orEmpty()
                .joinToString(", ") { argument ->
                    argument.name?.let { "$it = ${argument.value}" } ?: argument.value
                }
            TokenRow(
                primary = "@${usage.annotationClassName}",
                secondary = arguments.ifBlank { "无参数" },
                onDelete = {
                    scope.launchWorkbenchAction(state) {
                        state.deleteAnnotationUsage(usage.id)
                    }
                },
            )
        }
        FormGroupTitle("必填项")
        FieldLabel("注解类名", required = true)
        OutlinedTextField(
            value = annotationClassName,
            onValueChange = { annotationClassName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OptionalFieldsSection(
            rememberKey = "annotation-${selectedAnnotations.size}",
            defaultExpanded = false,
            collapsedHint = "无参数注解可以直接保存。",
        ) {
            FieldLabel("注解参数")
            OutlinedTextField(
                value = argsText,
                onValueChange = { argsText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("示例：name = \"demo\", enabled = true") },
            )
        }
        Button(
            onClick = {
                scope.launchWorkbenchAction(state) {
                    state.addDeclarationAnnotation(annotationClassName.trim(), argsText.trim())
                    annotationClassName = ""
                    argsText = ""
                }
            },
            enabled = annotationClassName.isNotBlank(),
        ) {
            Text("添加注解")
        }
    }
}

@Composable
private fun ConstructorParamCard(
    state: PlaygroundWorkbenchState,
    selectedDeclaration: DeclarationMetaDto,
    params: List<site.addzero.coding.playground.shared.dto.ConstructorParamMetaDto>,
) {
    val scope = rememberCoroutineScope()
    var name by remember(selectedDeclaration.id) { mutableStateOf("") }
    var type by remember(selectedDeclaration.id) { mutableStateOf("String") }
    var mutable by remember(selectedDeclaration.id) { mutableStateOf(false) }
    var defaultValue by remember(selectedDeclaration.id) { mutableStateOf("") }

    SectionCard(title = "构造参数") {
        params.forEach { param ->
            TokenRow(
                primary = "${if (param.mutable) "var" else "val"} ${param.name}: ${param.type}",
                secondary = param.defaultValue?.let { "默认值：$it" } ?: "无默认值",
                onDelete = {
                    scope.launchWorkbenchAction(state) { state.deleteConstructorParam(param.id) }
                },
            )
        }
        FormGroupTitle("必填项")
        CompactFieldRow(
            left = {
                FieldLabel("参数名", required = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            },
            right = {
                FieldLabel("类型", required = true)
                    OutlinedTextField(value = type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
        )
        OptionalFieldsSection(
            rememberKey = "ctor-${selectedDeclaration.id}",
            defaultExpanded = false,
            collapsedHint = "默认生成 val 参数；只有需要 var 或默认值时再展开。",
        ) {
            CompactSwitchField(
                label = "使用 var",
                checked = mutable,
                onCheckedChange = { mutable = it },
            )
            FieldLabel("默认值")
            OutlinedTextField(value = defaultValue, onValueChange = { defaultValue = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        Button(
            onClick = {
                scope.launchWorkbenchAction(state) {
                    state.addConstructorParam(name.trim(), type.trim(), mutable, defaultValue.ifBlank { null })
                    name = ""
                    type = "String"
                    mutable = false
                    defaultValue = ""
                }
            },
            enabled = name.isNotBlank() && type.isNotBlank(),
        ) {
            Text("添加构造参数")
        }
    }
}

@Composable
private fun PropertyEditorCard(
    state: PlaygroundWorkbenchState,
    selectedDeclaration: DeclarationMetaDto,
    properties: List<PropertyMetaDto>,
) {
    val scope = rememberCoroutineScope()
    var name by remember(selectedDeclaration.id) { mutableStateOf("") }
    var type by remember(selectedDeclaration.id) { mutableStateOf("String") }
    var mutable by remember(selectedDeclaration.id) { mutableStateOf(false) }
    var initializer by remember(selectedDeclaration.id) { mutableStateOf("") }

    SectionCard(title = "属性") {
        properties.forEach { property ->
            TokenRow(
                primary = "${property.visibility.label()} ${if (property.mutable) "var" else "val"} ${property.name}: ${property.type}",
                secondary = property.initializer?.let { "初始值：$it" } ?: "无初始值",
                onDelete = {
                    scope.launchWorkbenchAction(state) { state.deleteProperty(property.id) }
                },
            )
        }
        FormGroupTitle("必填项")
        CompactFieldRow(
            left = {
                FieldLabel("属性名", required = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            },
            right = {
                FieldLabel("类型", required = true)
                    OutlinedTextField(value = type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
        )
        OptionalFieldsSection(
            rememberKey = "property-${selectedDeclaration.id}",
            defaultExpanded = false,
            collapsedHint = "默认生成只读属性；只有需要 var 或初始值时再展开。",
        ) {
            CompactSwitchField(
                label = "使用 var",
                checked = mutable,
                onCheckedChange = { mutable = it },
            )
            FieldLabel("初始值")
            OutlinedTextField(value = initializer, onValueChange = { initializer = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        Button(
            onClick = {
                scope.launchWorkbenchAction(state) {
                    state.addProperty(name.trim(), type.trim(), mutable, initializer.ifBlank { null })
                    name = ""
                    type = "String"
                    mutable = false
                    initializer = ""
                }
            },
            enabled = name.isNotBlank() && type.isNotBlank(),
        ) {
            Text("添加属性")
        }
    }
}

@Composable
private fun EnumEntryEditorCard(
    state: PlaygroundWorkbenchState,
    selectedDeclaration: DeclarationMetaDto,
    entries: List<site.addzero.coding.playground.shared.dto.EnumEntryMetaDto>,
) {
    val scope = rememberCoroutineScope()
    var name by remember(selectedDeclaration.id) { mutableStateOf("") }

    SectionCard(title = "枚举项") {
        entries.forEach { entry ->
            TokenRow(
                primary = entry.name,
                secondary = entry.arguments.joinToString(", ").ifBlank { "无参数" },
                onDelete = {
                    scope.launchWorkbenchAction(state) { state.deleteEnumEntry(entry.id) }
                },
            )
        }
        FormGroupTitle("必填项")
        FieldLabel("枚举项名", required = true)
        OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(
            onClick = {
                scope.launchWorkbenchAction(state) {
                    state.addEnumEntry(name.trim())
                    name = ""
                }
            },
            enabled = name.isNotBlank(),
        ) {
            Text("添加枚举项")
        }
    }
}

@Composable
private fun FunctionEditorCard(
    state: PlaygroundWorkbenchState,
    selectedDeclaration: DeclarationMetaDto,
    functions: List<site.addzero.coding.playground.shared.dto.FunctionStubMetaDto>,
) {
    val scope = rememberCoroutineScope()
    var name by remember(selectedDeclaration.id) { mutableStateOf("") }
    var returnType by remember(selectedDeclaration.id) { mutableStateOf("Unit") }
    var parametersText by remember(selectedDeclaration.id) { mutableStateOf("") }

    SectionCard(title = "函数桩") {
        functions.forEach { function ->
            TokenRow(
                primary = "${function.name}(${formatFunctionParameters(function.parameters)})",
                secondary = "${function.visibility.label()} · ${function.returnType} · ${function.bodyMode.label()}",
                onDelete = {
                    scope.launchWorkbenchAction(state) { state.deleteFunction(function.id) }
                },
            )
        }
        FormGroupTitle("必填项")
        CompactFieldRow(
            left = {
                FieldLabel("函数名", required = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            },
            right = {
                FieldLabel("返回类型", required = true)
                    OutlinedTextField(value = returnType, onValueChange = { returnType = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                },
        )
        OptionalFieldsSection(
            rememberKey = "function-${selectedDeclaration.id}",
            defaultExpanded = false,
            collapsedHint = "无参函数可直接保存；需要参数时再展开。",
        ) {
            FieldLabel("参数列表")
            OutlinedTextField(
                value = parametersText,
                onValueChange = { parametersText = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = { Text("每行一个参数，格式：name: Type = default") },
            )
        }
        Button(
            onClick = {
                scope.launchWorkbenchAction(state) {
                    state.addFunction(name.trim(), returnType.trim(), parametersText)
                    name = ""
                    returnType = "Unit"
                    parametersText = ""
                }
            },
            enabled = name.isNotBlank() && returnType.isNotBlank(),
        ) {
            Text("添加函数桩")
        }
    }
}

@Composable
private fun InspectorPane(
    selectedTab: InspectorTab,
    onSelectTab: (InspectorTab) -> Unit,
    sourcePreview: String?,
    kspPreview: String?,
    outputPath: String?,
    metadataHash: String?,
    contentHash: String?,
    targetSourceRoot: String?,
    artifacts: List<ManagedArtifactMetaDto>,
    conflicts: List<SyncConflictMetaDto>,
    validationIssues: List<ValidationIssueDto>,
    onResolveConflict: (String, SyncConflictResolution) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = InspectorTab.entries.indexOf(selectedTab)) {
                InspectorTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = InspectorTab.entries[index] == selectedTab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.title) },
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                when (selectedTab) {
                    InspectorTab.SOURCE_PREVIEW -> CodePreviewPanel(
                        title = "托管源码",
                        content = sourcePreview ?: "当前没有可预览的源码。",
                    )

                    InspectorTab.SYNC_STATUS -> SyncStatusPanel(
                        outputPath = outputPath,
                        metadataHash = metadataHash,
                        contentHash = contentHash,
                        targetSourceRoot = targetSourceRoot,
                        artifacts = artifacts,
                    )

                    InspectorTab.CONFLICTS -> ConflictPanel(
                        conflicts = conflicts,
                        onResolveConflict = onResolveConflict,
                    )

                    InspectorTab.KSP_INDEX -> CodePreviewPanel(
                        title = "KSP 索引预览",
                        content = kspPreview ?: "当前目标还没有索引预览。",
                    )

                    InspectorTab.VALIDATION -> ValidationPanel(validationIssues)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusPanel(
    outputPath: String?,
    metadataHash: String?,
    contentHash: String?,
    targetSourceRoot: String?,
    artifacts: List<ManagedArtifactMetaDto>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InfoBlock(
            title = "当前写盘信息",
            lines = listOfNotNull(
                outputPath?.let { "输出文件：$it" },
                targetSourceRoot?.let { "源码根目录：$it" },
                metadataHash?.let { "元数据哈希：$it" },
                contentHash?.let { "内容哈希：$it" },
            ).ifEmpty { listOf("当前还没有可用的写盘信息。") },
        )
        Text("托管产物", fontWeight = FontWeight.SemiBold)
        if (artifacts.isEmpty()) {
            EmptyHint("还没有写盘记录。")
        } else {
            artifacts.forEach { artifact ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(artifact.absolutePath, fontWeight = FontWeight.SemiBold)
                        Text("状态：${artifact.syncStatus.label()}", color = syncStatusColor(artifact.syncStatus))
                        Text("文件 ID：${artifact.fileId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictPanel(
    conflicts: List<SyncConflictMetaDto>,
    onResolveConflict: (String, SyncConflictResolution) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (conflicts.isEmpty()) {
            EmptyHint("当前没有同步冲突。")
        } else {
            conflicts.forEach { conflict ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(conflict.reason.label(), fontWeight = FontWeight.SemiBold, color = conflictReasonColor(conflict.reason))
                        Text(conflict.message)
                        conflict.sourcePath?.let {
                            Text("来源文件：$it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        if (!conflict.resolved) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onResolveConflict(conflict.id, SyncConflictResolution.METADATA_WINS) }) {
                                    Text(SyncConflictResolution.METADATA_WINS.label())
                                }
                                OutlinedButton(onClick = { onResolveConflict(conflict.id, SyncConflictResolution.SOURCE_WINS) }) {
                                    Text(SyncConflictResolution.SOURCE_WINS.label())
                                }
                            }
                        } else {
                            Text(
                                "已处理：${conflict.resolution?.label().orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationPanel(validationIssues: List<ValidationIssueDto>) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (validationIssues.isEmpty()) {
            EmptyHint("当前没有校验问题。")
        } else {
            validationIssues.forEach { issue ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(issue.severity.label(), fontWeight = FontWeight.Bold, color = severityColor(issue.severity))
                        Text(issue.message)
                        Text(
                            listOfNotNull(issue.scopeType.takeIf { it.isNotBlank() }, issue.scopeId).joinToString(" / "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodePreviewPanel(
    title: String,
    content: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF111827),
        ) {
            SelectionContainer {
                Text(
                    text = content,
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE5EEF9),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                content()
            },
        )
    }
}

@Composable
private fun FormGroupTitle(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
    }
}

@Composable
private fun CompactFieldRow(
    left: @Composable ColumnScope.() -> Unit,
    right: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = left,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = right,
        )
    }
}

@Composable
private fun OptionalFieldsSection(
    rememberKey: Any?,
    defaultExpanded: Boolean = false,
    title: String = "可选项",
    collapsedHint: String = "默认收起，需要时再展开。",
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember(rememberKey) { mutableStateOf(defaultExpanded) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
            )
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起" else "展开")
            }
        }
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        } else {
            Text(
                text = collapsedHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun CompactSwitchField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label)
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (required) {
            Text(
                text = "*",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB91C1C),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun InfoBlock(
    title: String,
    lines: List<String>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun TokenRow(
    primary: String,
    secondary: String,
    onDelete: () -> Unit,
    deleteEnabled: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(primary, fontWeight = FontWeight.Medium)
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            TextButton(onClick = onDelete, enabled = deleteEnabled) {
                Text("删除")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdownField(
    value: T,
    values: List<T>,
    labelOf: (T) -> String,
    onSelected: (T) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember(value, enabled) { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = labelOf(value),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = true },
            enabled = enabled,
            readOnly = true,
            singleLine = true,
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().widthIn(min = 220.dp),
        ) {
            values.forEach { item ->
                DropdownMenuItem(
                    text = { Text(labelOf(item)) },
                    onClick = {
                        expanded = false
                        onSelected(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("确认删除")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun buildExplorerNodes(
    projects: List<CodegenProjectDto>,
    selectedProjectId: String?,
    aggregate: site.addzero.coding.playground.shared.dto.CodegenProjectAggregateDto?,
): List<ExplorerNode> {
    return projects.map { project ->
        val targetNodes = if (project.id == selectedProjectId && aggregate != null) {
            aggregate.targets.map { target ->
                val fileNodes = aggregate.files
                    .filter { it.targetId == target.id }
                    .map { file ->
                        ExplorerNode(
                            treeId = explorerTreeId(ExplorerNodeType.FILE, file.id),
                            entityId = file.id,
                            type = ExplorerNodeType.FILE,
                            label = "${file.fileName} · ${file.packageName}",
                            children = aggregate.declarations
                                .filter { it.fileId == file.id }
                                .map { declaration ->
                                    ExplorerNode(
                                        treeId = explorerTreeId(ExplorerNodeType.DECLARATION, declaration.id),
                                        entityId = declaration.id,
                                        type = ExplorerNodeType.DECLARATION,
                                        label = "${declaration.name} · ${declaration.kind.label()}",
                                    )
                                },
                        )
                    }
                ExplorerNode(
                    treeId = explorerTreeId(ExplorerNodeType.TARGET, target.id),
                    entityId = target.id,
                    type = ExplorerNodeType.TARGET,
                    label = "${target.name} · ${target.sourceSet}",
                    children = fileNodes,
                )
            }
        } else {
            emptyList()
        }
        ExplorerNode(
            treeId = explorerTreeId(ExplorerNodeType.PROJECT, project.id),
            entityId = project.id,
            type = ExplorerNodeType.PROJECT,
            label = project.description?.takeIf { it.isNotBlank() }?.let { "${project.name} · $it" } ?: project.name,
            children = targetNodes,
        )
    }
}

private fun selectedExplorerNodeId(
    projectId: String?,
    targetId: String?,
    fileId: String?,
    declarationId: String?,
): String? {
    return when {
        declarationId != null -> explorerTreeId(ExplorerNodeType.DECLARATION, declarationId)
        fileId != null -> explorerTreeId(ExplorerNodeType.FILE, fileId)
        targetId != null -> explorerTreeId(ExplorerNodeType.TARGET, targetId)
        projectId != null -> explorerTreeId(ExplorerNodeType.PROJECT, projectId)
        else -> null
    }
}

private fun explorerTreeId(
    type: ExplorerNodeType,
    entityId: String,
): String {
    return "${type.name.lowercase()}:$entityId"
}

private suspend fun handleExplorerNodeClick(
    state: PlaygroundWorkbenchState,
    node: ExplorerNode,
) {
    when (node.type) {
        ExplorerNodeType.PROJECT -> {
            state.selectProject(node.entityId)
            state.refreshProjectScope()
        }
        ExplorerNodeType.TARGET -> {
            state.selectTarget(node.entityId)
            state.refreshFileScope()
        }
        ExplorerNodeType.FILE -> {
            state.selectFile(node.entityId)
            state.refreshFileScope()
        }
        ExplorerNodeType.DECLARATION -> {
            state.selectDeclaration(node.entityId)
            state.refreshFileScope()
        }
    }
}

private fun List<ExplorerNode>.expandedBranchIdsForSelection(
    selectedTreeId: String?,
): Set<Any> {
    if (selectedTreeId == null) {
        return emptySet()
    }
    return buildSet {
        fun collect(nodes: List<ExplorerNode>): Boolean {
            nodes.forEach { node ->
                val containsSelection = node.treeId == selectedTreeId || collect(node.children)
                if (containsSelection && node.children.isNotEmpty()) {
                    add(node.treeId)
                }
                if (containsSelection) {
                    return true
                }
            }
            return false
        }
        collect(this@expandedBranchIdsForSelection)
    }
}

private fun CoroutineScope.launchWorkbenchAction(
    state: PlaygroundWorkbenchState,
    action: suspend () -> Unit,
) {
    launch {
        runCatching { action() }
            .onFailure(state::reportError)
    }
}

private fun parseMapLines(text: String): Map<String, String> {
    return text.lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .mapNotNull { line ->
            if (!line.contains("=")) {
                return@mapNotNull null
            }
            line.substringBefore("=").trim() to line.substringAfter("=").trim()
        }
        .toMap()
}

private fun formatMapLines(values: Map<String, String>): String {
    return values.entries.joinToString("\n") { "${it.key}=${it.value}" }
}

private fun parseCsvList(text: String): List<String> {
    return text.split(',')
        .map(String::trim)
        .filter(String::isNotBlank)
}

private fun formatFunctionParameters(
    parameters: List<site.addzero.coding.playground.shared.dto.FunctionParameterDto>,
): String {
    return parameters.joinToString(", ") { parameter ->
        buildString {
            append("${parameter.name}: ${parameter.type}")
            parameter.defaultValue?.let {
                append(" = $it")
            }
        }
    }
}

private fun FunctionBodyMode.label(): String {
    return when (this) {
        FunctionBodyMode.TEMPLATE -> "模板占位体"
        FunctionBodyMode.RAW_TEXT -> "原样文本体"
    }
}

@Composable
private fun severityColor(severity: ValidationSeverity): Color {
    return when (severity) {
        ValidationSeverity.INFO -> Color(0xFF1D4ED8)
        ValidationSeverity.WARNING -> Color(0xFFD97706)
        ValidationSeverity.ERROR -> Color(0xFFB91C1C)
    }
}

@Composable
private fun syncStatusColor(status: site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus): Color {
    return when (status) {
        site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus.CLEAN -> Color(0xFF15803D)
        site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus.METADATA_DIRTY -> Color(0xFF1D4ED8)
        site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus.SOURCE_DIRTY -> Color(0xFFD97706)
        site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus.CONFLICT -> Color(0xFFB91C1C)
        site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus.MISSING -> Color(0xFF6B7280)
    }
}

@Composable
private fun conflictReasonColor(reason: ConflictReason): Color {
    return when (reason) {
        ConflictReason.BOTH_CHANGED -> Color(0xFFB91C1C)
        ConflictReason.PARSE_FAILED -> Color(0xFFB45309)
        ConflictReason.UNSUPPORTED_SOURCE -> Color(0xFF7C3AED)
        ConflictReason.FILE_NOT_MANAGED -> Color(0xFF1D4ED8)
        ConflictReason.MARKER_MISMATCH -> Color(0xFF475569)
    }
}
