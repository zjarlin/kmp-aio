package site.addzero.coding.playground.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.coding.playground.PlaygroundWorkbenchState
import site.addzero.coding.playground.shared.dto.*

private enum class PlaygroundTab(val label: String) {
    PROJECTS("项目"),
    CONTEXTS("上下文"),
    ENTITIES("实体"),
    DTOS("DTO"),
    TEMPLATES("模板"),
    TARGETS("目标"),
    ETL("ETL"),
}

@Composable
fun PlaygroundApp(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(PlaygroundTab.PROJECTS) }
    MaterialTheme(
        colorScheme = lightColorScheme(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { scope.launch { state.refreshAll() } }) {
                    Text("刷新")
                }
                Button(onClick = { scope.launch { state.generateSelectedTarget() } }) {
                    Text("生成当前目标")
                }
                Button(onClick = { scope.launch { state.exportSelectedProjectSnapshot() } }) {
                    Text("导出快照")
                }
                Text(
                    text = state.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            ContextHeader(state)

            TabRow(selectedTabIndex = selectedTab.ordinal) {
                PlaygroundTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) },
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    PlaygroundTab.PROJECTS -> ProjectTab(state)
                    PlaygroundTab.CONTEXTS -> ContextTab(state)
                    PlaygroundTab.ENTITIES -> EntityTab(state)
                    PlaygroundTab.DTOS -> DtoTab(state)
                    PlaygroundTab.TEMPLATES -> TemplateTab(state)
                    PlaygroundTab.TARGETS -> TargetTab(state)
                    PlaygroundTab.ETL -> EtlTab(state)
                }
            }
        }
    }
}

@Composable
private fun ContextHeader(state: PlaygroundWorkbenchState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AssistChip(
            onClick = {},
            label = { Text("项目: ${state.projects.firstOrNull { it.id == state.selectedProjectId }?.name ?: "未选"}") },
        )
        AssistChip(
            onClick = {},
            label = { Text("上下文: ${state.contexts.firstOrNull { it.id == state.selectedContextId }?.name ?: "未选"}") },
        )
        AssistChip(
            onClick = {},
            label = { Text("实体: ${state.entities.firstOrNull { it.id == state.selectedEntityId }?.name ?: "未选"}") },
        )
    }
}

@Composable
private fun ProjectTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.projects.firstOrNull { it.id == state.selectedProjectId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var slug by remember(selected?.id) { mutableStateOf(selected?.slug.orEmpty()) }
    var description by remember(selected?.id) { mutableStateOf(selected?.description.orEmpty()) }
    TwoColumnEditor(
        left = {
            SimpleList(
                title = "项目列表",
                items = state.projects,
                label = { it.name },
                selectedId = state.selectedProjectId,
                onSelect = {
                    state.selectProject(it)
                    scope.launch { state.refreshProjectScope() }
                },
            )
        },
        right = {
            FormTitle("项目编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Slug", slug) { slug = it }
            LabeledTextField("说明", description) { description = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch { state.saveProject(selected?.id, name, slug, description) }
                }) { Text(if (selected == null) "创建" else "更新") }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeProject(selected.id) } }) {
                        Text("删除")
                    }
                }
            }
            SnapshotPreview(state)
        },
    )
}

@Composable
private fun ContextTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.contexts.firstOrNull { it.id == state.selectedContextId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var code by remember(selected?.id) { mutableStateOf(selected?.code.orEmpty()) }
    var description by remember(selected?.id) { mutableStateOf(selected?.description.orEmpty()) }
    TwoColumnEditor(
        left = {
            SimpleList(
                title = "上下文列表",
                items = state.contexts,
                label = { it.name },
                selectedId = state.selectedContextId,
                onSelect = {
                    state.selectContext(it)
                    scope.launch { state.refreshContextScope() }
                },
            )
        },
        right = {
            FormTitle("上下文编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Code", code) { code = it }
            LabeledTextField("说明", description) { description = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.saveContext(selected?.id, name, code, description) } }) {
                    Text(if (selected == null) "创建" else "更新")
                }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeContext(selected.id) } }) {
                        Text("删除")
                    }
                }
            }
        },
    )
}

@Composable
private fun EntityTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.entities.firstOrNull { it.id == state.selectedEntityId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var code by remember(selected?.id) { mutableStateOf(selected?.code.orEmpty()) }
    var tableName by remember(selected?.id) { mutableStateOf(selected?.tableName.orEmpty()) }
    var description by remember(selected?.id) { mutableStateOf(selected?.description.orEmpty()) }
    var fieldName by remember { mutableStateOf("") }
    var fieldCode by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf(FieldType.STRING.name) }
    var relationName by remember { mutableStateOf("") }
    var relationCode by remember { mutableStateOf("") }
    var relationTarget by remember { mutableStateOf("") }
    var relationKind by remember { mutableStateOf(RelationKind.MANY_TO_ONE.name) }
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SimpleList(
            modifier = Modifier.weight(0.9f),
            title = "实体列表",
            items = state.entities,
            label = { it.name },
            selectedId = state.selectedEntityId,
            onSelect = { state.selectEntity(it) },
        )
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormTitle("实体编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Code", code) { code = it }
            LabeledTextField("表名", tableName) { tableName = it }
            LabeledTextField("说明", description) { description = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.saveEntity(selected?.id, name, code, tableName, description) } }) {
                    Text(if (selected == null) "创建" else "更新")
                }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeEntity(selected.id) } }) { Text("删除") }
                }
            }
            Divider()
            FormTitle("字段")
            LabeledTextField("字段名", fieldName) { fieldName = it }
            LabeledTextField("字段 Code", fieldCode) { fieldCode = it }
            LabeledTextField("类型", fieldType) { fieldType = it }
            Button(
                onClick = {
                    val entityId = state.selectedEntityId ?: return@Button
                    scope.launch {
                        state.saveField(null, entityId, fieldName, fieldCode, FieldType.valueOf(fieldType), nullable = false, idField = false)
                        fieldName = ""
                        fieldCode = ""
                    }
                },
            ) { Text("新增字段") }
            SimpleInlineList(state.fieldsForSelectedEntity(), { "${it.name}:${it.type}" }) { item ->
                scope.launch { state.removeField(item.id) }
            }
            Divider()
            FormTitle("关系")
            LabeledTextField("关系名", relationName) { relationName = it }
            LabeledTextField("关系 Code", relationCode) { relationCode = it }
            LabeledTextField("目标实体 Code", relationTarget) { relationTarget = it }
            LabeledTextField("关系类型", relationKind) { relationKind = it }
            Button(
                onClick = {
                    val source = state.selectedEntityId ?: return@Button
                    val target = state.entities.firstOrNull { it.code == relationTarget }?.id ?: return@Button
                    scope.launch {
                        state.saveRelation(null, source, target, relationName, relationCode, RelationKind.valueOf(relationKind))
                        relationName = ""
                        relationCode = ""
                    }
                },
            ) { Text("新增关系") }
            SimpleInlineList(state.relationsForSelectedEntity(), { "${it.name}:${it.kind}" }) { item ->
                scope.launch { state.removeRelation(item.id) }
            }
        }
    }
}

@Composable
private fun DtoTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.dtos.firstOrNull { it.id == state.selectedDtoId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var code by remember(selected?.id) { mutableStateOf(selected?.code.orEmpty()) }
    var kind by remember(selected?.id) { mutableStateOf(selected?.kind?.name ?: DtoKind.REQUEST.name) }
    var description by remember(selected?.id) { mutableStateOf(selected?.description.orEmpty()) }
    var fieldName by remember { mutableStateOf("") }
    var fieldCode by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf(FieldType.STRING.name) }
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SimpleList(
            modifier = Modifier.weight(0.9f),
            title = "DTO 列表",
            items = state.dtos,
            label = { it.name },
            selectedId = state.selectedDtoId,
            onSelect = { state.selectDto(it) },
        )
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormTitle("DTO 编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Code", code) { code = it }
            LabeledTextField("类型", kind) { kind = it }
            LabeledTextField("说明", description) { description = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.saveDto(selected?.id, name, code, DtoKind.valueOf(kind), description) } }) {
                    Text(if (selected == null) "创建" else "更新")
                }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeDto(selected.id) } }) { Text("删除") }
                }
            }
            Divider()
            FormTitle("DTO 字段")
            LabeledTextField("字段名", fieldName) { fieldName = it }
            LabeledTextField("字段 Code", fieldCode) { fieldCode = it }
            LabeledTextField("类型", fieldType) { fieldType = it }
            Button(
                onClick = {
                    val dtoId = state.selectedDtoId ?: return@Button
                    scope.launch {
                        state.saveDtoField(null, dtoId, fieldName, fieldCode, FieldType.valueOf(fieldType), nullable = false)
                        fieldName = ""
                        fieldCode = ""
                    }
                },
            ) { Text("新增 DTO 字段") }
            SimpleInlineList(state.dtoFieldsForSelectedDto(), { "${it.name}:${it.type}" }) { item ->
                scope.launch { state.removeDtoField(item.id) }
            }
        }
    }
}

@Composable
private fun TemplateTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.templates.firstOrNull { it.id == state.selectedTemplateId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var key by remember(selected?.id) { mutableStateOf(selected?.key.orEmpty()) }
    var outputPath by remember(selected?.id) { mutableStateOf(selected?.relativeOutputPath.orEmpty()) }
    var fileName by remember(selected?.id) { mutableStateOf(selected?.fileNameTemplate.orEmpty()) }
    var body by remember(selected?.id) { mutableStateOf(selected?.body.orEmpty()) }
    TwoColumnEditor(
        left = {
            SimpleList(
                title = "模板列表",
                items = state.templates,
                label = { it.name },
                selectedId = state.selectedTemplateId,
                onSelect = { state.selectTemplate(it) },
            )
        },
        right = {
            FormTitle("模板编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Key", key) { key = it }
            LabeledTextField("输出路径", outputPath) { outputPath = it }
            LabeledTextField("文件名模板", fileName) { fileName = it }
            LabeledTextField(
                label = "模板体",
                value = body,
                onValueChange = { body = it },
                minLines = 8,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.saveTemplate(selected?.id, name, key, outputPath, fileName, body) } }) {
                    Text(if (selected == null) "创建" else "更新")
                }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeTemplate(selected.id) } }) { Text("删除") }
                }
            }
        },
    )
}

@Composable
private fun TargetTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.targets.firstOrNull { it.id == state.selectedTargetId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var key by remember(selected?.id) { mutableStateOf(selected?.key.orEmpty()) }
    var outputRoot by remember(selected?.id) { mutableStateOf(selected?.outputRoot.orEmpty()) }
    var packageName by remember(selected?.id) { mutableStateOf(selected?.packageName.orEmpty()) }
    var templateIds by remember(selected?.id) { mutableStateOf(selected?.templateIds?.joinToString(",").orEmpty()) }
    TwoColumnEditor(
        left = {
            SimpleList(
                title = "生成目标",
                items = state.targets,
                label = { it.name },
                selectedId = state.selectedTargetId,
                onSelect = { state.selectTarget(it) },
            )
        },
        right = {
            FormTitle("目标编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Key", key) { key = it }
            LabeledTextField("输出根目录", outputRoot) { outputRoot = it }
            LabeledTextField("Package", packageName) { packageName = it }
            LabeledTextField("模板 ID 列表", templateIds) { templateIds = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            state.saveTarget(
                                selected?.id,
                                name,
                                key,
                                outputRoot,
                                packageName,
                                templateIds.split(",").mapNotNull { it.trim().takeIf(String::isNotBlank) },
                            )
                        }
                    },
                ) { Text(if (selected == null) "创建" else "更新") }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeTarget(selected.id) } }) { Text("删除") }
                }
            }
            Divider()
            FormTitle("生成结果")
            Text(state.lastGeneration?.files?.joinToString("\n") { it.relativePath } ?: "暂无", fontFamily = FontFamily.Monospace)
        },
    )
}

@Composable
private fun EtlTab(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val selected = state.etlWrappers.firstOrNull { it.id == state.selectedEtlWrapperId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var key by remember(selected?.id) { mutableStateOf(selected?.key.orEmpty()) }
    var scriptBody by remember(selected?.id) { mutableStateOf(selected?.scriptBody.orEmpty()) }
    TwoColumnEditor(
        left = {
            SimpleList(
                title = "ETL 包裹器",
                items = state.etlWrappers,
                label = { it.name },
                selectedId = state.selectedEtlWrapperId,
                onSelect = { state.selectEtlWrapper(it) },
            )
        },
        right = {
            FormTitle("ETL 编辑")
            LabeledTextField("名称", name) { name = it }
            LabeledTextField("Key", key) { key = it }
            LabeledTextField(
                label = "脚本体",
                value = scriptBody,
                onValueChange = { scriptBody = it },
                minLines = 10,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.saveEtlWrapper(selected?.id, name, key, scriptBody) } }) {
                    Text(if (selected == null) "创建" else "更新")
                }
                if (selected != null) {
                    OutlinedButton(onClick = { scope.launch { state.removeEtlWrapper(selected.id) } }) { Text("删除") }
                }
            }
        },
    )
}

@Composable
private fun SnapshotPreview(state: PlaygroundWorkbenchState) {
    if (state.lastSnapshotJson.isBlank()) {
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormTitle("快照预览")
        Text(
            text = state.lastSnapshotJson,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun FormTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
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
    )
}

@Composable
private fun <T> SimpleList(
    modifier: Modifier = Modifier,
    title: String,
    items: List<T>,
    label: (T) -> String,
    selectedId: String?,
    onSelect: (String) -> Unit,
) where T : Any {
    Card(modifier = modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormTitle(title)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items) { item ->
                    val id = itemId(item)
                    FilterChip(
                        selected = id == selectedId,
                        onClick = { onSelect(id) },
                        label = { Text(label(item)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SimpleInlineList(
    items: List<T>,
    label: (T) -> String,
    onDelete: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label(item))
                TextButton(onClick = { onDelete(item) }) { Text("删除") }
            }
        }
    }
}

@Composable
private fun TwoColumnEditor(
    left: @Composable ColumnScope.() -> Unit,
    right: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.weight(0.9f).fillMaxHeight()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = left)
        }
        Card(modifier = Modifier.weight(1.1f).fillMaxHeight()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = right)
        }
    }
}

private fun itemId(item: Any): String = when (item) {
    is ProjectMetaDto -> item.id
    is BoundedContextMetaDto -> item.id
    is EntityMetaDto -> item.id
    is DtoMetaDto -> item.id
    is TemplateMetaDto -> item.id
    is GenerationTargetMetaDto -> item.id
    is EtlWrapperMetaDto -> item.id
    else -> error("Unsupported item type: ${item::class.simpleName}")
}
