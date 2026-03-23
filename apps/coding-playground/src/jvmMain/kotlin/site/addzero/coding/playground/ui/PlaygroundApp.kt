package site.addzero.coding.playground.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.coding.playground.PlaygroundUiLanguage
import site.addzero.coding.playground.PlaygroundWorkbenchState
import site.addzero.coding.playground.shared.dto.BoundedContextMetaDto
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.DtoFieldMetaDto
import site.addzero.coding.playground.shared.dto.DtoKind
import site.addzero.coding.playground.shared.dto.DtoMetaDto
import site.addzero.coding.playground.shared.dto.EntityMetaDto
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.FieldMetaDto
import site.addzero.coding.playground.shared.dto.FieldType
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.ProjectMetaDto
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.RelationMetaDto
import site.addzero.coding.playground.shared.dto.ScaffoldPreset
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.dto.TemplateOutputKind

private enum class WorkbenchTab {
    PROJECTS,
    CONTEXTS,
    ENTITIES,
    DTOS,
    TEMPLATES,
    TARGETS,
    ETL,
    SNAPSHOT,
}

@Composable
fun PlaygroundApp(state: PlaygroundWorkbenchState) {
    var activeTab by remember { mutableStateOf(WorkbenchTab.PROJECTS) }
    PlaygroundTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            WorkbenchHeader(
                state = state,
                onRefresh = { scopeState ->
                    scopeState.launch { state.refreshAll() }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                ExplorerPane(
                    state = state,
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight(),
                    onSelectTab = { activeTab = it },
                )
                VerticalLine()
                EditorPane(
                    state = state,
                    activeTab = activeTab,
                    onTabChange = { activeTab = it },
                    modifier = Modifier.weight(1f),
                )
            }
            DiagnosticsPanel(state = state)
            StatusBar(state = state)
        }
    }
}

@Composable
private fun WorkbenchHeader(
    state: PlaygroundWorkbenchState,
    onRefresh: (kotlinx.coroutines.CoroutineScope) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr(state, "Coding Playground 工作台", "Coding Playground Workbench"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = tr(
                    state,
                    "元数据设计台、管理后台与 CRUD 骨架生成器",
                    "Metadata studio, admin console and CRUD skeleton generator",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = { onRefresh(scope) }) {
            Text(tr(state, "刷新", "Refresh"))
        }
        OutlinedButton(onClick = { state.toggleLanguage() }) {
            Text(if (state.uiLanguage == PlaygroundUiLanguage.ZH_CN) "EN" else "中文")
        }
    }
}

@Composable
private fun ExplorerPane(
    state: PlaygroundWorkbenchState,
    modifier: Modifier = Modifier,
    onSelectTab: (WorkbenchTab) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = state.explorerQuery,
            onValueChange = { state.explorerQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(tr(state, "搜索树节点", "Search")) },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.selectedProjectId != null,
                onClick = {
                    onSelectTab(WorkbenchTab.PROJECTS)
                    state.clearDiagnostics()
                },
                label = { Text(tr(state, "项目", "Projects")) },
            )
            FilterChip(
                selected = state.selectedContextId != null,
                onClick = {
                    onSelectTab(WorkbenchTab.CONTEXTS)
                    state.clearDiagnostics()
                },
                label = { Text(tr(state, "上下文", "Contexts")) },
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ExplorerSectionTitle(tr(state, "项目树", "Workspace Tree"))
            state.filteredProjects().forEach { project ->
                ExplorerRow(
                    title = project.name,
                    subtitle = project.slug,
                    selected = state.selectedProjectId == project.id,
                    indent = 0,
                    onClick = {
                        onSelectTab(WorkbenchTab.PROJECTS)
                        scope.launch {
                            state.selectProject(project.id)
                            state.refreshProjectScope()
                        }
                    },
                )
            }

            if (state.selectedProjectId != null) {
                Spacer(Modifier.height(6.dp))
                ExplorerSectionTitle(tr(state, "上下文", "Contexts"))
                state.filteredContexts().forEach { context ->
                    ExplorerRow(
                        title = context.name,
                        subtitle = context.code,
                        selected = state.selectedContextId == context.id,
                        indent = 1,
                        onClick = {
                            onSelectTab(WorkbenchTab.CONTEXTS)
                            scope.launch {
                                state.selectContext(context.id)
                                state.refreshContextScope()
                            }
                        },
                    )
                }

                if (state.selectedContextId != null) {
                    ExplorerSectionTitle(tr(state, "实体", "Entities"))
                    state.filteredEntities().forEach { entity ->
                        ExplorerRow(
                            title = entity.name,
                            subtitle = entity.code,
                            selected = state.selectedEntityId == entity.id,
                            indent = 2,
                            onClick = {
                                onSelectTab(WorkbenchTab.ENTITIES)
                                state.selectEntity(entity.id)
                            },
                        )
                    }
                    ExplorerSectionTitle(tr(state, "DTO", "DTOs"))
                    state.filteredDtos().forEach { dto ->
                        ExplorerRow(
                            title = dto.name,
                            subtitle = dto.code,
                            selected = state.selectedDtoId == dto.id,
                            indent = 2,
                            onClick = {
                                onSelectTab(WorkbenchTab.DTOS)
                                state.selectDto(dto.id)
                            },
                        )
                    }
                    ExplorerSectionTitle(tr(state, "模板", "Templates"))
                    state.filteredTemplates().forEach { template ->
                        ExplorerRow(
                            title = template.name,
                            subtitle = template.key,
                            selected = state.selectedTemplateId == template.id,
                            indent = 2,
                            onClick = {
                                onSelectTab(WorkbenchTab.TEMPLATES)
                                state.selectTemplate(template.id)
                            },
                        )
                    }
                    ExplorerSectionTitle(tr(state, "生成目标", "Targets"))
                    state.filteredTargets().forEach { target ->
                        ExplorerRow(
                            title = target.name,
                            subtitle = target.key,
                            selected = state.selectedTargetId == target.id,
                            indent = 2,
                            onClick = {
                                onSelectTab(WorkbenchTab.TARGETS)
                                state.selectTarget(target.id)
                            },
                        )
                    }
                }

                ExplorerSectionTitle(tr(state, "ETL 包裹器", "ETL Wrappers"))
                state.filteredEtlWrappers().forEach { wrapper ->
                    ExplorerRow(
                        title = wrapper.name,
                        subtitle = wrapper.key,
                        selected = state.selectedEtlWrapperId == wrapper.id,
                        indent = 1,
                        onClick = {
                            onSelectTab(WorkbenchTab.ETL)
                            state.selectEtlWrapper(wrapper.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplorerSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun ExplorerRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    indent: Int,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(start = (indent * 16).dp + 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small,
                ),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EditorPane(
    state: PlaygroundWorkbenchState,
    activeTab: WorkbenchTab,
    onTabChange: (WorkbenchTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScrollableTabRow(selectedTabIndex = activeTab.ordinal) {
            WorkbenchTab.entries.forEach { tab ->
                Tab(
                    selected = tab == activeTab,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            when (tab) {
                                WorkbenchTab.PROJECTS -> tr(state, "项目", "Projects")
                                WorkbenchTab.CONTEXTS -> tr(state, "上下文", "Contexts")
                                WorkbenchTab.ENTITIES -> tr(state, "实体", "Entities")
                                WorkbenchTab.DTOS -> tr(state, "DTO", "DTOs")
                                WorkbenchTab.TEMPLATES -> tr(state, "模板", "Templates")
                                WorkbenchTab.TARGETS -> tr(state, "目标", "Targets")
                                WorkbenchTab.ETL -> tr(state, "ETL", "ETL")
                                WorkbenchTab.SNAPSHOT -> tr(state, "快照", "Snapshot")
                            },
                        )
                    },
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (activeTab) {
                WorkbenchTab.PROJECTS -> ProjectEditor(state)
                WorkbenchTab.CONTEXTS -> ContextEditor(state)
                WorkbenchTab.ENTITIES -> EntityEditor(state)
                WorkbenchTab.DTOS -> DtoEditor(state)
                WorkbenchTab.TEMPLATES -> TemplateEditor(state)
                WorkbenchTab.TARGETS -> TargetEditor(state)
                WorkbenchTab.ETL -> EtlEditor(state)
                WorkbenchTab.SNAPSHOT -> SnapshotEditor(state)
            }
        }
    }
}

@Composable
private fun ProjectEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId }
    var name by remember(project?.id) { mutableStateOf(project?.name.orEmpty()) }
    var slug by remember(project?.id) { mutableStateOf(project?.slug.orEmpty()) }
    var description by remember(project?.id) { mutableStateOf(project?.description.orEmpty()) }
    var showDeleteDialog by remember(project?.id) { mutableStateOf(false) }
    EditorScrollContainer {
        EditorCard(
            title = tr(state, "项目详情", "Project Details"),
            subtitle = tr(state, "平台元数据的根聚合。", "Root aggregate for playground metadata."),
        ) {
            LabeledTextField(tr(state, "项目名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "项目标识", "Slug"), slug) { slug = it }
            LabeledTextField(
                label = tr(state, "说明", "Description"),
                value = description,
                onValueChange = { description = it },
                minLines = 3,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存项目", "Save Project"),
                onPrimary = {
                    scope.launch { state.saveProject(project?.id, name, slug, description) }
                },
                secondaryLabel = tr(state, "新建项目", "New Project"),
                onSecondary = {
                    state.selectProject(null)
                    scope.launch { state.refreshProjectScope() }
                    name = ""
                    slug = ""
                    description = ""
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (project != null) {
                    {
                        scope.launch {
                            state.previewProjectDelete(project.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
        }
        SummaryCard(
            title = tr(state, "当前项目摘要", "Current Project"),
            rows = listOf(
                tr(state, "项目", "Project") to (project?.name ?: tr(state, "未选择", "Not selected")),
                tr(state, "上下文数量", "Contexts") to state.contexts.size.toString(),
                tr(state, "ETL 数量", "ETL wrappers") to state.etlWrappers.size.toString(),
            ),
        )
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除项目", "Delete Project"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (project != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeProject(project.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun ContextEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val context = state.contexts.firstOrNull { it.id == state.selectedContextId }
    var name by remember(context?.id) { mutableStateOf(context?.name.orEmpty()) }
    var code by remember(context?.id) { mutableStateOf(context?.code.orEmpty()) }
    var description by remember(context?.id) { mutableStateOf(context?.description.orEmpty()) }
    var showDeleteDialog by remember(context?.id) { mutableStateOf(false) }
    EditorScrollContainer {
        EditorCard(
            title = tr(state, "上下文详情", "Context Details"),
            subtitle = tr(state, "一个上下文会显式级联其下实体、DTO、模板与目标。", "Deleting a context cascades its entities, DTOs, templates and targets."),
        ) {
            LabeledTextField(tr(state, "上下文名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "上下文代码", "Code"), code) { code = it }
            LabeledTextField(
                label = tr(state, "说明", "Description"),
                value = description,
                onValueChange = { description = it },
                minLines = 3,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存上下文", "Save Context"),
                onPrimary = {
                    scope.launch { state.saveContext(context?.id, name, code, description) }
                },
                secondaryLabel = tr(state, "新建上下文", "New Context"),
                onSecondary = {
                    state.selectContext(null)
                    scope.launch { state.refreshContextScope() }
                    name = ""
                    code = ""
                    description = ""
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (context != null) {
                    {
                        scope.launch {
                            state.previewContextDelete(context.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
        }
        SummaryCard(
            title = tr(state, "上下文聚合摘要", "Aggregate Summary"),
            rows = listOf(
                tr(state, "实体", "Entities") to state.entities.size.toString(),
                tr(state, "DTO", "DTOs") to state.dtos.size.toString(),
                tr(state, "模板", "Templates") to state.templates.size.toString(),
                tr(state, "目标", "Targets") to state.targets.size.toString(),
            ),
        )
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除上下文", "Delete Context"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (context != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeContext(context.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun EntityEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val entity = state.entities.firstOrNull { it.id == state.selectedEntityId }
    val entityFields = state.fieldsForSelectedEntity()
    val entityRelations = state.relationsForSelectedEntity()
    var name by remember(entity?.id) { mutableStateOf(entity?.name.orEmpty()) }
    var code by remember(entity?.id) { mutableStateOf(entity?.code.orEmpty()) }
    var tableName by remember(entity?.id) { mutableStateOf(entity?.tableName.orEmpty()) }
    var description by remember(entity?.id) { mutableStateOf(entity?.description.orEmpty()) }
    var showDeleteDialog by remember(entity?.id) { mutableStateOf(false) }

    var editingFieldId by remember(entity?.id, entityFields.map { it.id }.joinToString()) {
        mutableStateOf(entityFields.firstOrNull()?.id)
    }
    val editingField = entityFields.firstOrNull { it.id == editingFieldId }
    var fieldName by remember(editingField?.id) { mutableStateOf(editingField?.name.orEmpty()) }
    var fieldCode by remember(editingField?.id) { mutableStateOf(editingField?.code.orEmpty()) }
    var fieldType by remember(editingField?.id) { mutableStateOf(editingField?.type ?: FieldType.STRING) }
    var fieldNullable by remember(editingField?.id) { mutableStateOf(editingField?.nullable ?: false) }
    var fieldIdField by remember(editingField?.id) { mutableStateOf(editingField?.idField ?: false) }
    var fieldKeyField by remember(editingField?.id) { mutableStateOf(editingField?.keyField ?: false) }
    var fieldSearchable by remember(editingField?.id) { mutableStateOf(editingField?.searchable ?: false) }

    var editingRelationId by remember(entity?.id, entityRelations.map { it.id }.joinToString()) {
        mutableStateOf(entityRelations.firstOrNull()?.id)
    }
    val editingRelation = entityRelations.firstOrNull { it.id == editingRelationId }
    var relationName by remember(editingRelation?.id) { mutableStateOf(editingRelation?.name.orEmpty()) }
    var relationCode by remember(editingRelation?.id) { mutableStateOf(editingRelation?.code.orEmpty()) }
    var relationKind by remember(editingRelation?.id) { mutableStateOf(editingRelation?.kind ?: RelationKind.MANY_TO_ONE) }
    var relationTargetId by remember(editingRelation?.id, entity?.id) {
        mutableStateOf(editingRelation?.targetEntityId ?: state.entities.firstOrNull { it.id != entity?.id }?.id.orEmpty())
    }

    EditorScrollContainer {
        EditorCard(
            title = tr(state, "实体详情", "Entity Details"),
            subtitle = tr(state, "字段支持排序，删除前会校验 relation 与 dto-field 引用。", "Fields are reorderable and entity delete is guarded by relation and dto-field references."),
        ) {
            LabeledTextField(tr(state, "实体名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "实体代码", "Code"), code) { code = it }
            LabeledTextField(tr(state, "表名", "Table Name"), tableName) { tableName = it }
            LabeledTextField(
                label = tr(state, "说明", "Description"),
                value = description,
                onValueChange = { description = it },
                minLines = 3,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存实体", "Save Entity"),
                onPrimary = {
                    scope.launch { state.saveEntity(entity?.id, name, code, tableName, description) }
                },
                secondaryLabel = tr(state, "新建实体", "New Entity"),
                onSecondary = {
                    state.selectEntity(null)
                    name = ""
                    code = ""
                    tableName = ""
                    description = ""
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (entity != null) {
                    {
                        scope.launch {
                            state.previewEntityDelete(entity.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
        }

        EditorCard(
            title = tr(state, "字段管理", "Fields"),
            subtitle = tr(state, "支持创建、编辑、删除与排序。", "Create, edit, delete and reorder fields."),
        ) {
            ReorderList(
                items = entityFields,
                selectedId = editingFieldId,
                getTitle = { it.name },
                getSubtitle = { "${it.code} · ${it.type.name}" },
                onSelect = { editingFieldId = it.id },
                onMoveUp = { scope.launch { state.moveField(it.id, -1) } },
                onMoveDown = { scope.launch { state.moveField(it.id, 1) } },
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(tr(state, "字段名称", "Field Name"), fieldName) { fieldName = it }
            LabeledTextField(tr(state, "字段代码", "Field Code"), fieldCode) { fieldCode = it }
            EnumSelector(
                label = tr(state, "字段类型", "Field Type"),
                selected = fieldType,
                options = FieldType.entries.toList(),
                labelOf = { it.name },
                onSelect = { fieldType = it },
            )
            CheckboxLine(tr(state, "允许为空", "Nullable"), fieldNullable) { fieldNullable = it }
            CheckboxLine(tr(state, "主键字段", "ID Field"), fieldIdField) { fieldIdField = it }
            CheckboxLine(tr(state, "业务键", "Key Field"), fieldKeyField) { fieldKeyField = it }
            CheckboxLine(tr(state, "参与搜索", "Searchable"), fieldSearchable) { fieldSearchable = it }
            ButtonRow(
                primaryLabel = tr(state, "保存字段", "Save Field"),
                onPrimary = if (entity != null) {
                    {
                        scope.launch {
                            state.saveField(
                                selectedId = editingField?.id,
                                entityId = entity.id,
                                name = fieldName,
                                code = fieldCode,
                                type = fieldType,
                                nullable = fieldNullable,
                                idField = fieldIdField,
                                keyField = fieldKeyField,
                                searchable = fieldSearchable,
                            )
                        }
                    }
                } else {
                    {}
                },
                secondaryLabel = tr(state, "新建字段", "New Field"),
                onSecondary = {
                    editingFieldId = null
                    fieldName = ""
                    fieldCode = ""
                    fieldType = FieldType.STRING
                    fieldNullable = false
                    fieldIdField = false
                    fieldKeyField = false
                    fieldSearchable = false
                },
                tertiaryLabel = tr(state, "删除字段", "Delete Field"),
                onTertiary = if (editingField != null) {
                    { scope.launch { state.removeField(editingField.id) } }
                } else {
                    null
                },
            )
        }

        EditorCard(
            title = tr(state, "关系管理", "Relations"),
            subtitle = tr(state, "用于 ManyToOne / OneToMany 等建模。", "Use for ManyToOne / OneToMany relation modeling."),
        ) {
            SimpleSelectionList(
                items = entityRelations,
                selectedId = editingRelationId,
                getTitle = { it.name },
                getSubtitle = { "${it.code} · ${it.kind.name}" },
                onSelect = { editingRelationId = it.id },
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(tr(state, "关系名称", "Relation Name"), relationName) { relationName = it }
            LabeledTextField(tr(state, "关系代码", "Relation Code"), relationCode) { relationCode = it }
            EnumSelector(
                label = tr(state, "关系类型", "Relation Kind"),
                selected = relationKind,
                options = RelationKind.entries.toList(),
                labelOf = { it.name },
                onSelect = { relationKind = it },
            )
            StringSelector(
                label = tr(state, "目标实体", "Target Entity"),
                selectedId = relationTargetId,
                options = state.entities.filter { it.id != entity?.id }.map { it.id to "${it.name} (${it.code})" },
                onSelect = { relationTargetId = it.orEmpty() },
            )
            ButtonRow(
                primaryLabel = tr(state, "保存关系", "Save Relation"),
                onPrimary = if (entity != null && relationTargetId.isNotBlank()) {
                    {
                        scope.launch {
                            state.saveRelation(
                                selectedId = editingRelation?.id,
                                sourceEntityId = entity.id,
                                targetEntityId = relationTargetId,
                                name = relationName,
                                code = relationCode,
                                kind = relationKind,
                            )
                        }
                    }
                } else {
                    {}
                },
                secondaryLabel = tr(state, "新建关系", "New Relation"),
                onSecondary = {
                    editingRelationId = null
                    relationName = ""
                    relationCode = ""
                    relationKind = RelationKind.MANY_TO_ONE
                    relationTargetId = state.entities.firstOrNull { it.id != entity?.id }?.id.orEmpty()
                },
                tertiaryLabel = tr(state, "删除关系", "Delete Relation"),
                onTertiary = if (editingRelation != null) {
                    { scope.launch { state.removeRelation(editingRelation.id) } }
                } else {
                    null
                },
            )
        }
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除实体", "Delete Entity"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (entity != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeEntity(entity.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun DtoEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val dto = state.dtos.firstOrNull { it.id == state.selectedDtoId }
    val dtoFields = state.dtoFieldsForSelectedDto()
    var name by remember(dto?.id) { mutableStateOf(dto?.name.orEmpty()) }
    var code by remember(dto?.id) { mutableStateOf(dto?.code.orEmpty()) }
    var description by remember(dto?.id) { mutableStateOf(dto?.description.orEmpty()) }
    var kind by remember(dto?.id) { mutableStateOf(dto?.kind ?: DtoKind.REQUEST) }
    var sourceEntityId by remember(dto?.id) { mutableStateOf(dto?.entityId ?: state.selectedEntityId) }
    var showDeleteDialog by remember(dto?.id) { mutableStateOf(false) }

    var editingFieldId by remember(dto?.id, dtoFields.map { it.id }.joinToString()) {
        mutableStateOf(dtoFields.firstOrNull()?.id)
    }
    val editingField = dtoFields.firstOrNull { it.id == editingFieldId }
    var dtoFieldName by remember(editingField?.id) { mutableStateOf(editingField?.name.orEmpty()) }
    var dtoFieldCode by remember(editingField?.id) { mutableStateOf(editingField?.code.orEmpty()) }
    var dtoFieldType by remember(editingField?.id) { mutableStateOf(editingField?.type ?: FieldType.STRING) }
    var dtoFieldNullable by remember(editingField?.id) { mutableStateOf(editingField?.nullable ?: false) }
    var dtoFieldEntityFieldId by remember(editingField?.id, sourceEntityId) {
        mutableStateOf(editingField?.entityFieldId ?: state.fields.firstOrNull { it.entityId == sourceEntityId }?.id)
    }

    EditorScrollContainer {
        EditorCard(
            title = tr(state, "DTO 详情", "DTO Details"),
            subtitle = tr(state, "DTO 支持绑定业务实体，并维护独立字段列表。", "DTOs can bind to entities and maintain an independent field list."),
        ) {
            LabeledTextField(tr(state, "DTO 名称", "DTO Name"), name) { name = it }
            LabeledTextField(tr(state, "DTO 代码", "DTO Code"), code) { code = it }
            EnumSelector(
                label = tr(state, "DTO 类型", "DTO Kind"),
                selected = kind,
                options = DtoKind.entries.toList(),
                labelOf = { it.name },
                onSelect = { kind = it },
            )
            StringSelector(
                label = tr(state, "绑定实体", "Entity"),
                selectedId = sourceEntityId,
                options = state.entities.map { it.id to "${it.name} (${it.code})" },
                onSelect = { sourceEntityId = it },
            )
            LabeledTextField(
                label = tr(state, "说明", "Description"),
                value = description,
                onValueChange = { description = it },
                minLines = 3,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存 DTO", "Save DTO"),
                onPrimary = {
                    scope.launch { state.saveDto(dto?.id, sourceEntityId, name, code, kind, description) }
                },
                secondaryLabel = tr(state, "新建 DTO", "New DTO"),
                onSecondary = {
                    state.selectDto(null)
                    name = ""
                    code = ""
                    description = ""
                    kind = DtoKind.REQUEST
                    sourceEntityId = state.selectedEntityId
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (dto != null) {
                    {
                        scope.launch {
                            state.previewDtoDelete(dto.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
        }

        EditorCard(
            title = tr(state, "DTO 字段", "DTO Fields"),
            subtitle = tr(state, "支持排序以及绑定实体字段。", "Reorderable and optionally linked to entity fields."),
        ) {
            ReorderList(
                items = dtoFields,
                selectedId = editingFieldId,
                getTitle = { it.name },
                getSubtitle = { "${it.code} · ${it.type.name}" },
                onSelect = { editingFieldId = it.id },
                onMoveUp = { scope.launch { state.moveDtoField(it.id, -1) } },
                onMoveDown = { scope.launch { state.moveDtoField(it.id, 1) } },
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(tr(state, "字段名称", "Name"), dtoFieldName) { dtoFieldName = it }
            LabeledTextField(tr(state, "字段代码", "Code"), dtoFieldCode) { dtoFieldCode = it }
            EnumSelector(
                label = tr(state, "字段类型", "Type"),
                selected = dtoFieldType,
                options = FieldType.entries.toList(),
                labelOf = { it.name },
                onSelect = { dtoFieldType = it },
            )
            StringSelector(
                label = tr(state, "来源实体字段", "Entity Field"),
                selectedId = dtoFieldEntityFieldId,
                options = state.fields.filter { it.entityId == sourceEntityId }.map { it.id to "${it.name} (${it.code})" },
                onSelect = { dtoFieldEntityFieldId = it },
                allowEmpty = true,
            )
            CheckboxLine(tr(state, "允许为空", "Nullable"), dtoFieldNullable) { dtoFieldNullable = it }
            ButtonRow(
                primaryLabel = tr(state, "保存 DTO 字段", "Save DTO Field"),
                onPrimary = if (dto != null) {
                    {
                        scope.launch {
                            state.saveDtoField(
                                selectedId = editingField?.id,
                                dtoId = dto.id,
                                entityFieldId = dtoFieldEntityFieldId,
                                name = dtoFieldName,
                                code = dtoFieldCode,
                                type = dtoFieldType,
                                nullable = dtoFieldNullable,
                            )
                        }
                    }
                } else {
                    {}
                },
                secondaryLabel = tr(state, "新建 DTO 字段", "New DTO Field"),
                onSecondary = {
                    editingFieldId = null
                    dtoFieldName = ""
                    dtoFieldCode = ""
                    dtoFieldType = FieldType.STRING
                    dtoFieldNullable = false
                    dtoFieldEntityFieldId = state.fields.firstOrNull { it.entityId == sourceEntityId }?.id
                },
                tertiaryLabel = tr(state, "删除 DTO 字段", "Delete DTO Field"),
                onTertiary = if (editingField != null) {
                    { scope.launch { state.removeDtoField(editingField.id) } }
                } else {
                    null
                },
            )
        }
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除 DTO", "Delete DTO"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (dto != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeDto(dto.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun TemplateEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val template = state.templates.firstOrNull { it.id == state.selectedTemplateId }
    var name by remember(template?.id) { mutableStateOf(template?.name.orEmpty()) }
    var key by remember(template?.id) { mutableStateOf(template?.key.orEmpty()) }
    var relativeOutputPath by remember(template?.id) { mutableStateOf(template?.relativeOutputPath.orEmpty()) }
    var fileNameTemplate by remember(template?.id) { mutableStateOf(template?.fileNameTemplate.orEmpty()) }
    var body by remember(template?.id) { mutableStateOf(template?.body.orEmpty()) }
    var outputKind by remember(template?.id) { mutableStateOf(template?.outputKind ?: TemplateOutputKind.KOTLIN_SOURCE) }
    var enabled by remember(template?.id) { mutableStateOf(template?.enabled ?: true) }
    var etlWrapperId by remember(template?.id) { mutableStateOf(template?.etlWrapperId) }
    var showDeleteDialog by remember(template?.id) { mutableStateOf(false) }
    EditorScrollContainer {
        EditorCard(
            title = tr(state, "模板详情", "Template Details"),
            subtitle = tr(state, "支持 ETL 绑定、校验与顺序调整。", "Supports ETL linkage, validation and ordering."),
        ) {
            ReorderList(
                items = state.templates,
                selectedId = template?.id,
                getTitle = { it.name },
                getSubtitle = { it.key },
                onSelect = {
                    state.selectTemplate(it.id)
                },
                onMoveUp = { scope.launch { state.moveTemplate(it.id, -1) } },
                onMoveDown = { scope.launch { state.moveTemplate(it.id, 1) } },
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(tr(state, "模板名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "模板键", "Key"), key) { key = it }
            EnumSelector(
                label = tr(state, "输出类型", "Output Kind"),
                selected = outputKind,
                options = TemplateOutputKind.entries.toList(),
                labelOf = { it.name },
                onSelect = { outputKind = it },
            )
            StringSelector(
                label = tr(state, "ETL 包裹器", "ETL Wrapper"),
                selectedId = etlWrapperId,
                options = state.etlWrappers.map { it.id to "${it.name} (${it.key})" },
                allowEmpty = true,
                onSelect = { etlWrapperId = it },
            )
            CheckboxLine(tr(state, "启用模板", "Enabled"), enabled) { enabled = it }
            LabeledTextField(tr(state, "相对输出路径", "Relative Output"), relativeOutputPath) { relativeOutputPath = it }
            LabeledTextField(tr(state, "文件名模板", "File Name Template"), fileNameTemplate) { fileNameTemplate = it }
            LabeledTextField(
                label = tr(state, "模板正文", "Template Body"),
                value = body,
                onValueChange = { body = it },
                minLines = 8,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存模板", "Save Template"),
                onPrimary = {
                    scope.launch {
                        state.saveTemplate(
                            selectedId = template?.id,
                            name = name,
                            key = key,
                            outputKind = outputKind,
                            relativeOutputPath = relativeOutputPath,
                            fileNameTemplate = fileNameTemplate,
                            body = body,
                            etlWrapperId = etlWrapperId,
                            enabled = enabled,
                        )
                    }
                },
                secondaryLabel = tr(state, "校验模板", "Validate"),
                onSecondary = {
                    if (template != null) {
                        scope.launch { state.validateSelectedTemplate(template.id) }
                    }
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (template != null) {
                    {
                        scope.launch {
                            state.previewTemplateDelete(template.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
            OutlinedButton(
                onClick = {
                    state.selectTemplate(null)
                    name = ""
                    key = ""
                    relativeOutputPath = ""
                    fileNameTemplate = ""
                    body = ""
                    outputKind = TemplateOutputKind.KOTLIN_SOURCE
                    enabled = true
                    etlWrapperId = null
                },
            ) {
                Text(tr(state, "新建模板", "New Template"))
            }
        }
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除模板", "Delete Template"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (template != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeTemplate(template.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun TargetEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val target = state.targets.firstOrNull { it.id == state.selectedTargetId }
    var name by remember(target?.id) { mutableStateOf(target?.name.orEmpty()) }
    var key by remember(target?.id) { mutableStateOf(target?.key.orEmpty()) }
    var outputRoot by remember(target?.id) { mutableStateOf(target?.outputRoot.orEmpty()) }
    var packageName by remember(target?.id) { mutableStateOf(target?.packageName.orEmpty()) }
    var scaffoldPreset by remember(target?.id) { mutableStateOf(target?.scaffoldPreset ?: ScaffoldPreset.KCLOUD_STYLE) }
    var variablesText by remember(target?.id) { mutableStateOf(state.formatVariablesText(target?.variables ?: emptyMap())) }
    var enableEtl by remember(target?.id) { mutableStateOf(target?.enableEtl ?: false) }
    var autoIntegrate by remember(target?.id) { mutableStateOf(target?.autoIntegrateCompositeBuild ?: true) }
    var managedMarker by remember(target?.id) { mutableStateOf(target?.managedMarker ?: "CODING_PLAYGROUND") }
    var selectedTemplateIds by remember(target?.id, state.templates.map { it.id }.joinToString()) {
        mutableStateOf(target?.templateIds?.toSet() ?: state.templates.filter { it.enabled }.map { it.id }.toSet())
    }
    var showDeleteDialog by remember(target?.id) { mutableStateOf(false) }

    EditorScrollContainer {
        EditorCard(
            title = tr(state, "生成目标", "Generation Target"),
            subtitle = tr(state, "支持路径变量、模板选择、计划预览、执行生成与 composite build 自动接入。", "Supports path variables, template selection, planning, generation and composite build integration."),
        ) {
            LabeledTextField(tr(state, "目标名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "目标键", "Key"), key) { key = it }
            LabeledTextField(tr(state, "输出根目录", "Output Root"), outputRoot) { outputRoot = it }
            LabeledTextField(tr(state, "包名", "Package Name"), packageName) { packageName = it }
            EnumSelector(
                label = tr(state, "脚手架预设", "Scaffold Preset"),
                selected = scaffoldPreset,
                options = ScaffoldPreset.entries.toList(),
                labelOf = { it.name },
                onSelect = { scaffoldPreset = it },
            )
            CheckboxLine(tr(state, "启用 ETL", "Enable ETL"), enableEtl) { enableEtl = it }
            CheckboxLine(tr(state, "自动接入 composite build", "Auto integrate composite build"), autoIntegrate) { autoIntegrate = it }
            LabeledTextField(tr(state, "受管 marker", "Managed Marker"), managedMarker) { managedMarker = it }
            LabeledTextField(
                label = tr(state, "路径变量", "Variables"),
                value = variablesText,
                onValueChange = { variablesText = it },
                minLines = 5,
            )
            SummaryCard(
                title = tr(state, "输出路径预览", "Resolved Output Root"),
                rows = listOf(
                    tr(state, "解析结果", "Resolved") to (
                        state.outputRootPreview.ifBlank { tr(state, "点击下方按钮执行预览", "Click preview below") }
                    ),
                ),
            )
            OutlinedButton(onClick = { state.previewOutputRoot(outputRoot, variablesText) }) {
                Text(tr(state, "预览输出路径", "Preview Output"))
            }
            Text(
                text = tr(state, "启用模板", "Enabled Templates"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            state.templates.forEach { template ->
                CheckboxLine(
                    title = "${template.name} (${template.key})",
                    checked = template.id in selectedTemplateIds,
                    onCheckedChange = { checked ->
                        selectedTemplateIds = if (checked) {
                            selectedTemplateIds + template.id
                        } else {
                            selectedTemplateIds - template.id
                        }
                    },
                )
            }
            ButtonRow(
                primaryLabel = tr(state, "保存目标", "Save Target"),
                onPrimary = {
                    scope.launch {
                        state.saveTarget(
                            selectedId = target?.id,
                            name = name,
                            key = key,
                            outputRoot = outputRoot,
                            packageName = packageName,
                            scaffoldPreset = scaffoldPreset,
                            templateIds = selectedTemplateIds.toList(),
                            variablesText = variablesText,
                            enableEtl = enableEtl,
                            autoIntegrateCompositeBuild = autoIntegrate,
                            managedMarker = managedMarker,
                        )
                    }
                },
                secondaryLabel = tr(state, "校验目标", "Validate"),
                onSecondary = {
                    if (target != null) {
                        scope.launch { state.validateSelectedTarget(target.id) }
                    }
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (target != null) {
                    {
                        scope.launch {
                            state.previewTargetDelete(target.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        state.selectTarget(null)
                        name = ""
                        key = ""
                        outputRoot = ""
                        packageName = ""
                        scaffoldPreset = ScaffoldPreset.KCLOUD_STYLE
                        variablesText = ""
                        enableEtl = false
                        autoIntegrate = true
                        managedMarker = "CODING_PLAYGROUND"
                        selectedTemplateIds = state.templates.filter { it.enabled }.map { it.id }.toSet()
                    },
                ) {
                    Text(tr(state, "新建目标", "New Target"))
                }
                Button(onClick = { scope.launch { state.planSelectedTarget() } }) {
                    Text(tr(state, "生成计划预览", "Plan"))
                }
                Button(onClick = { scope.launch { state.generateSelectedTarget() } }) {
                    Text(tr(state, "执行生成", "Generate"))
                }
            }
        }
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除目标", "Delete Target"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (target != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeTarget(target.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun EtlEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val wrapper = state.etlWrappers.firstOrNull { it.id == state.selectedEtlWrapperId }
    var name by remember(wrapper?.id) { mutableStateOf(wrapper?.name.orEmpty()) }
    var key by remember(wrapper?.id) { mutableStateOf(wrapper?.key.orEmpty()) }
    var scriptBody by remember(wrapper?.id) { mutableStateOf(wrapper?.scriptBody ?: "return content") }
    var enabled by remember(wrapper?.id) { mutableStateOf(wrapper?.enabled ?: true) }
    var showDeleteDialog by remember(wrapper?.id) { mutableStateOf(false) }
    EditorScrollContainer {
        EditorCard(
            title = tr(state, "ETL 包裹器", "ETL Wrapper"),
            subtitle = tr(state, "脚本绑定变量固定为 content / template / target / variables，返回值必须是 String。", "Script bindings are content / template / target / variables and must return String."),
        ) {
            LabeledTextField(tr(state, "名称", "Name"), name) { name = it }
            LabeledTextField(tr(state, "键", "Key"), key) { key = it }
            CheckboxLine(tr(state, "启用", "Enabled"), enabled) { enabled = it }
            LabeledTextField(
                label = tr(state, "脚本正文", "Script Body"),
                value = scriptBody,
                onValueChange = { scriptBody = it },
                minLines = 10,
            )
            ButtonRow(
                primaryLabel = tr(state, "保存 ETL", "Save ETL"),
                onPrimary = {
                    scope.launch { state.saveEtlWrapper(wrapper?.id, name, key, scriptBody, enabled) }
                },
                secondaryLabel = tr(state, "校验 ETL", "Validate"),
                onSecondary = {
                    if (wrapper != null) {
                        scope.launch { state.validateSelectedEtl(wrapper.id) }
                    }
                },
                tertiaryLabel = tr(state, "删除预检", "Delete Check"),
                onTertiary = if (wrapper != null) {
                    {
                        scope.launch {
                            state.previewEtlDelete(wrapper.id)
                            showDeleteDialog = true
                        }
                    }
                } else {
                    null
                },
            )
            OutlinedButton(
                onClick = {
                    state.selectEtlWrapper(null)
                    name = ""
                    key = ""
                    scriptBody = "return content"
                    enabled = true
                },
            ) {
                Text(tr(state, "新建 ETL", "New ETL"))
            }
        }
    }
    DeleteCheckDialog(
        visible = showDeleteDialog,
        title = tr(state, "删除 ETL", "Delete ETL"),
        check = state.lastDeleteCheck,
        onDismiss = { showDeleteDialog = false },
        onConfirm = if (wrapper != null && state.lastDeleteCheck?.allowed == true) {
            {
                showDeleteDialog = false
                scope.launch { state.removeEtlWrapper(wrapper.id) }
            }
        } else {
            null
        },
    )
}

@Composable
private fun SnapshotEditor(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    EditorScrollContainer {
        EditorCard(
            title = tr(state, "快照导入 / 导出", "Snapshot Import / Export"),
            subtitle = tr(state, "versioned JSON DTO 继续作为快照格式。", "Versioned JSON DTO remains the snapshot format."),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.exportSelectedProjectSnapshot() } }) {
                    Text(tr(state, "导出当前项目", "Export"))
                }
                Button(onClick = { scope.launch { state.importSnapshotFromEditor() } }) {
                    Text(tr(state, "从编辑器导入", "Import"))
                }
            }
            LabeledTextField(
                label = tr(state, "快照 JSON", "Snapshot JSON"),
                value = state.snapshotEditorText,
                onValueChange = { state.snapshotEditorText = it },
                minLines = 18,
            )
        }
    }
}

@Composable
private fun DiagnosticsPanel(state: PlaygroundWorkbenchState) {
    val scrollState = rememberScrollState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = tr(state, "诊断与生成输出", "Diagnostics & Generation"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = state.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.outputRootPreview.isNotBlank()) {
                Text(
                    text = "${tr(state, "路径预览", "Output")}: ${state.outputRootPreview}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.lastDeleteCheck?.let { check ->
                DiagnosticSection(
                    title = tr(state, "删除预检", "Delete Check"),
                    lines = listOf(
                        "${tr(state, "允许删除", "Allowed")}: ${if (check.allowed) tr(state, "是", "Yes") else tr(state, "否", "No")}",
                    ) + check.reasons,
                )
            }
            if (state.lastValidationIssues.isNotEmpty()) {
                DiagnosticSection(
                    title = tr(state, "校验结果", "Validation"),
                    lines = state.lastValidationIssues.map { "${it.field}: ${it.message}" },
                )
            }
            state.lastGenerationPlan?.let { plan ->
                DiagnosticSection(
                    title = tr(state, "生成计划", "Generation Plan"),
                    lines = listOf(
                        "${tr(state, "脚手架模式", "Scaffold Mode")}: ${plan.scaffoldMode.name}",
                        "${tr(state, "计划文件数", "Files")}: ${plan.files.size}",
                    ) + plan.files.take(24).map { "${it.templateKey} -> ${it.relativePath}/${it.fileName}" },
                )
            }
            state.lastGeneration?.let { result ->
                DiagnosticSection(
                    title = tr(state, "生成结果", "Generation Result"),
                    lines = listOf(
                        "${tr(state, "写盘文件数", "Generated Files")}: ${result.files.size}",
                        "${tr(state, "接入动作", "Integrations")}: ${result.integrations.size}",
                    ) + result.files.take(24).map { "${it.templateKey} -> ${it.relativePath}" },
                )
            }
        }
    }
}

@Composable
private fun StatusBar(state: PlaygroundWorkbenchState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (state.uiLanguage == PlaygroundUiLanguage.ZH_CN) "ZH-CN" else "EN-US",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EditorScrollContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun EditorCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            },
        )
    }
}

@Composable
private fun SummaryCard(title: String, rows: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            rows.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = minLines,
        singleLine = minLines == 1,
    )
}

@Composable
private fun ButtonRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    tertiaryLabel: String? = null,
    onTertiary: (() -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onPrimary) {
            Text(primaryLabel)
        }
        OutlinedButton(onClick = onSecondary) {
            Text(secondaryLabel)
        }
        if (tertiaryLabel != null && onTertiary != null) {
            OutlinedButton(onClick = onTertiary) {
                Text(tertiaryLabel)
            }
        }
    }
}

@Composable
private fun CheckboxLine(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(title, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun <T> ReorderList(
    items: List<T>,
    selectedId: String?,
    getTitle: (T) -> String,
    getSubtitle: (T) -> String,
    onSelect: (T) -> Unit,
    onMoveUp: (T) -> Unit,
    onMoveDown: (T) -> Unit,
) where T : Any {
    if (items.isEmpty()) {
        Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, item ->
            val selected = selectedId == itemId(item)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                    )
                    .clickable { onSelect(item) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(getTitle(item), style = MaterialTheme.typography.bodyMedium)
                    Text(getSubtitle(item), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { onMoveUp(item) }, enabled = index > 0) {
                        Text("↑")
                    }
                    OutlinedButton(onClick = { onMoveDown(item) }, enabled = index < items.lastIndex) {
                        Text("↓")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SimpleSelectionList(
    items: List<T>,
    selectedId: String?,
    getTitle: (T) -> String,
    getSubtitle: (T) -> String,
    onSelect: (T) -> Unit,
) where T : Any {
    if (items.isEmpty()) {
        Text("暂无数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            val selected = selectedId == itemId(item)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                    )
                    .clickable { onSelect(item) }
                    .padding(8.dp),
            ) {
                Column {
                    Text(getTitle(item), style = MaterialTheme.typography.bodyMedium)
                    Text(getSubtitle(item), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun <T> EnumSelector(
    label: String,
    selected: T,
    options: List<T>,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember(label, selected) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(labelOf(selected))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(labelOf(option)) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StringSelector(
    label: String,
    selectedId: String?,
    options: List<Pair<String, String>>,
    onSelect: (String?) -> Unit,
    allowEmpty: Boolean = false,
) {
    var expanded by remember(label, selectedId, options) { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: if (allowEmpty) "未绑定" else "请选择"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedLabel)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (allowEmpty) {
                    DropdownMenuItem(
                        text = { Text("未绑定") },
                        onClick = {
                            expanded = false
                            onSelect(null)
                        },
                    )
                }
                options.forEach { (id, value) ->
                    DropdownMenuItem(
                        text = { Text(value) },
                        onClick = {
                            expanded = false
                            onSelect(id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteCheckDialog(
    visible: Boolean,
    title: String,
    check: DeleteCheckResultDto?,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)?,
) {
    if (!visible) {
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (check?.allowed == true) "删除许可已通过，可以继续。" else "删除前置校验未通过或尚未完成。")
                check?.reasons?.forEach { reason ->
                    Text("• $reason", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm ?: onDismiss, enabled = onConfirm != null) {
                Text(if (onConfirm != null) "确认删除" else "关闭")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun DiagnosticSection(title: String, lines: List<String>) {
    if (lines.isEmpty()) {
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        lines.forEach { line ->
            Text(line, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun VerticalLine() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    )
}

@Composable
private fun PlaygroundTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF355E88),
            onPrimary = Color.White,
            secondary = Color(0xFF4F6E86),
            background = Color(0xFFF5F7FA),
            surface = Color(0xFFFDFEFF),
            surfaceVariant = Color(0xFFE7EDF3),
            outline = Color(0xFF9AA8B7),
        ),
        content = content,
    )
}

private fun tr(state: PlaygroundWorkbenchState, zh: String, en: String): String {
    return if (state.uiLanguage == PlaygroundUiLanguage.ZH_CN) zh else en
}

private fun itemId(item: Any): String {
    return when (item) {
        is ProjectMetaDto -> item.id
        is BoundedContextMetaDto -> item.id
        is EntityMetaDto -> item.id
        is FieldMetaDto -> item.id
        is RelationMetaDto -> item.id
        is DtoMetaDto -> item.id
        is DtoFieldMetaDto -> item.id
        is TemplateMetaDto -> item.id
        is GenerationTargetMetaDto -> item.id
        is EtlWrapperMetaDto -> item.id
        else -> error("Unsupported item type: ${item::class}")
    }
}
