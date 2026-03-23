package site.addzero.coding.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*
import java.util.prefs.Preferences

enum class PlaygroundUiLanguage {
    ZH_CN,
    EN_US,
}

@Single
class PlaygroundWorkbenchState(
    private val projectService: ProjectMetaService,
    private val contextService: ContextMetaService,
    private val entityService: EntityMetaService,
    private val dtoService: DtoMetaService,
    private val templateService: TemplateMetaService,
    private val targetService: GenerationTargetMetaService,
    private val etlWrapperMetaService: EtlWrapperMetaService,
    private val metadataSnapshotService: MetadataSnapshotService,
    private val generationPlanner: GenerationPlanner,
    private val pathVariableResolver: PathVariableResolver,
) {
    private val prefs = Preferences.userNodeForPackage(PlaygroundWorkbenchState::class.java)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    var projects by mutableStateOf<List<ProjectMetaDto>>(emptyList())
        private set
    var contexts by mutableStateOf<List<BoundedContextMetaDto>>(emptyList())
        private set
    var entities by mutableStateOf<List<EntityMetaDto>>(emptyList())
        private set
    var fields by mutableStateOf<List<FieldMetaDto>>(emptyList())
        private set
    var relations by mutableStateOf<List<RelationMetaDto>>(emptyList())
        private set
    var dtos by mutableStateOf<List<DtoMetaDto>>(emptyList())
        private set
    var dtoFields by mutableStateOf<List<DtoFieldMetaDto>>(emptyList())
        private set
    var templates by mutableStateOf<List<TemplateMetaDto>>(emptyList())
        private set
    var targets by mutableStateOf<List<GenerationTargetMetaDto>>(emptyList())
        private set
    var etlWrappers by mutableStateOf<List<EtlWrapperMetaDto>>(emptyList())
        private set

    var selectedProjectId by mutableStateOf<String?>(null)
        private set
    var selectedContextId by mutableStateOf<String?>(null)
        private set
    var selectedEntityId by mutableStateOf<String?>(null)
        private set
    var selectedDtoId by mutableStateOf<String?>(null)
        private set
    var selectedTemplateId by mutableStateOf<String?>(null)
        private set
    var selectedTargetId by mutableStateOf<String?>(null)
        private set
    var selectedEtlWrapperId by mutableStateOf<String?>(null)
        private set

    var uiLanguage by mutableStateOf(loadLanguage())
        private set
    var explorerQuery by mutableStateOf("")
    var statusMessage by mutableStateOf("准备就绪")
        private set
    var lastDeleteCheck by mutableStateOf<DeleteCheckResultDto?>(null)
        private set
    var lastValidationIssues by mutableStateOf<List<ValidationIssueDto>>(emptyList())
        private set
    var lastGenerationPlan by mutableStateOf<GenerationPlanDto?>(null)
        private set
    var lastGeneration by mutableStateOf<GenerationResultDto?>(null)
        private set
    var lastSnapshotJson by mutableStateOf("")
        private set
    var snapshotEditorText by mutableStateOf("")
    var outputRootPreview by mutableStateOf("")
        private set

    suspend fun refreshAll() {
        projects = projectService.list()
        if (selectedProjectId !in projects.map { it.id }) {
            selectedProjectId = projects.firstOrNull()?.id
        }
        refreshProjectScope()
    }

    suspend fun refreshProjectScope() {
        val projectId = selectedProjectId
        contexts = if (projectId == null) emptyList() else contextService.list(MetadataSearchRequest(projectId = projectId))
        if (selectedContextId !in contexts.map { it.id }) {
            selectedContextId = contexts.firstOrNull()?.id
        }
        etlWrappers = if (projectId == null) {
            emptyList()
        } else {
            etlWrapperMetaService.list(MetadataSearchRequest(projectId = projectId))
        }
        if (selectedEtlWrapperId !in etlWrappers.map { it.id }) {
            selectedEtlWrapperId = etlWrappers.firstOrNull()?.id
        }
        refreshContextScope()
    }

    suspend fun refreshContextScope() {
        val contextId = selectedContextId
        if (contextId == null) {
            entities = emptyList()
            fields = emptyList()
            relations = emptyList()
            dtos = emptyList()
            dtoFields = emptyList()
            templates = emptyList()
            targets = emptyList()
            return
        }
        val aggregate = contextService.aggregate(contextId)
        entities = aggregate.entities
        fields = aggregate.fields
        relations = aggregate.relations
        dtos = aggregate.dtos
        dtoFields = aggregate.dtoFields
        templates = aggregate.templates
        targets = aggregate.generationTargets
        if (selectedEntityId !in entities.map { it.id }) {
            selectedEntityId = entities.firstOrNull()?.id
        }
        if (selectedDtoId !in dtos.map { it.id }) {
            selectedDtoId = dtos.firstOrNull()?.id
        }
        if (selectedTemplateId !in templates.map { it.id }) {
            selectedTemplateId = templates.firstOrNull()?.id
        }
        if (selectedTargetId !in targets.map { it.id }) {
            selectedTargetId = targets.firstOrNull()?.id
        }
    }

    fun selectProject(id: String?) {
        selectedProjectId = id
        clearDiagnostics()
    }

    fun selectContext(id: String?) {
        selectedContextId = id
        clearDiagnostics()
    }

    fun selectEntity(id: String?) {
        selectedEntityId = id
        clearDiagnostics()
    }

    fun selectDto(id: String?) {
        selectedDtoId = id
        clearDiagnostics()
    }

    fun selectTemplate(id: String?) {
        selectedTemplateId = id
        clearDiagnostics()
    }

    fun selectTarget(id: String?) {
        selectedTargetId = id
        clearDiagnostics()
    }

    fun selectEtlWrapper(id: String?) {
        selectedEtlWrapperId = id
        clearDiagnostics()
    }

    fun toggleLanguage() {
        uiLanguage = when (uiLanguage) {
            PlaygroundUiLanguage.ZH_CN -> PlaygroundUiLanguage.EN_US
            PlaygroundUiLanguage.EN_US -> PlaygroundUiLanguage.ZH_CN
        }
        prefs.put("language", uiLanguage.name)
    }

    suspend fun saveProject(
        selectedId: String?,
        name: String,
        slug: String,
        description: String,
    ) {
        if (selectedId == null) {
            val created = projectService.create(CreateProjectMetaRequest(name = name, slug = slug, description = description.ifBlank { null }))
            selectedProjectId = created.id
            statusMessage = "项目已创建"
        } else {
            val updated = projectService.update(selectedId, UpdateProjectMetaRequest(name = name, slug = slug, description = description.ifBlank { null }))
            selectedProjectId = updated.id
            statusMessage = "项目已更新"
        }
        refreshAll()
    }

    suspend fun removeProject(id: String) {
        projectService.delete(id)
        statusMessage = "项目已删除"
        refreshAll()
    }

    suspend fun saveContext(
        selectedId: String?,
        name: String,
        code: String,
        description: String,
    ) {
        val projectId = selectedProjectId ?: return
        if (selectedId == null) {
            val created = contextService.create(CreateBoundedContextMetaRequest(projectId, name, code, description.ifBlank { null }))
            selectedContextId = created.id
            statusMessage = "上下文已创建"
        } else {
            val updated = contextService.update(selectedId, UpdateBoundedContextMetaRequest(name, code, description.ifBlank { null }))
            selectedContextId = updated.id
            statusMessage = "上下文已更新"
        }
        refreshProjectScope()
    }

    suspend fun removeContext(id: String) {
        contextService.delete(id)
        statusMessage = "上下文已删除"
        refreshProjectScope()
    }

    suspend fun saveEntity(
        selectedId: String?,
        name: String,
        code: String,
        tableName: String,
        description: String,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            val created = entityService.create(CreateEntityMetaRequest(contextId, name, code, tableName, description.ifBlank { null }))
            selectedEntityId = created.id
            statusMessage = "实体已创建"
        } else {
            val updated = entityService.update(selectedId, UpdateEntityMetaRequest(name, code, tableName, description.ifBlank { null }))
            selectedEntityId = updated.id
            statusMessage = "实体已更新"
        }
        refreshContextScope()
    }

    suspend fun removeEntity(id: String) {
        entityService.delete(id)
        statusMessage = "实体已删除"
        refreshContextScope()
    }

    suspend fun saveField(
        selectedId: String?,
        entityId: String,
        name: String,
        code: String,
        type: FieldType,
        nullable: Boolean,
        idField: Boolean,
        keyField: Boolean,
        searchable: Boolean,
    ) {
        if (selectedId == null) {
            entityService.createField(
                CreateFieldMetaRequest(
                    entityId = entityId,
                    name = name,
                    code = code,
                    type = type,
                    nullable = nullable,
                    idField = idField,
                    keyField = keyField,
                    searchable = searchable,
                ),
            )
            statusMessage = "字段已创建"
        } else {
            entityService.updateField(
                selectedId,
                UpdateFieldMetaRequest(
                    name = name,
                    code = code,
                    type = type,
                    nullable = nullable,
                    idField = idField,
                    keyField = keyField,
                    searchable = searchable,
                ),
            )
            statusMessage = "字段已更新"
        }
        refreshContextScope()
    }

    suspend fun removeField(id: String) {
        entityService.deleteField(id)
        statusMessage = "字段已删除"
        refreshContextScope()
    }

    suspend fun moveField(id: String, delta: Int) {
        val entityId = fields.firstOrNull { it.id == id }?.entityId ?: return
        val orderedIds = fields
            .filter { it.entityId == entityId }
            .map { it.id }
            .toMutableList()
        moveId(orderedIds, id, delta)
        entityService.reorderFields(entityId, ReorderRequestDto(orderedIds))
        refreshContextScope()
    }

    suspend fun saveRelation(
        selectedId: String?,
        sourceEntityId: String,
        targetEntityId: String,
        name: String,
        code: String,
        kind: RelationKind,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            entityService.createRelation(CreateRelationMetaRequest(contextId, sourceEntityId, targetEntityId, name, code, kind))
            statusMessage = "关系已创建"
        } else {
            entityService.updateRelation(selectedId, UpdateRelationMetaRequest(name, code, kind))
            statusMessage = "关系已更新"
        }
        refreshContextScope()
    }

    suspend fun removeRelation(id: String) {
        entityService.deleteRelation(id)
        statusMessage = "关系已删除"
        refreshContextScope()
    }

    suspend fun saveDto(
        selectedId: String?,
        entityId: String?,
        name: String,
        code: String,
        kind: DtoKind,
        description: String,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            val created = dtoService.create(CreateDtoMetaRequest(contextId, entityId, name, code, kind, description.ifBlank { null }))
            selectedDtoId = created.id
            statusMessage = "DTO 已创建"
        } else {
            val updated = dtoService.update(selectedId, UpdateDtoMetaRequest(entityId, name, code, kind, description.ifBlank { null }))
            selectedDtoId = updated.id
            statusMessage = "DTO 已更新"
        }
        refreshContextScope()
    }

    suspend fun removeDto(id: String) {
        dtoService.delete(id)
        statusMessage = "DTO 已删除"
        refreshContextScope()
    }

    suspend fun saveDtoField(
        selectedId: String?,
        dtoId: String,
        entityFieldId: String?,
        name: String,
        code: String,
        type: FieldType,
        nullable: Boolean,
    ) {
        if (selectedId == null) {
            dtoService.createField(CreateDtoFieldMetaRequest(dtoId, entityFieldId, name, code, type, nullable = nullable))
            statusMessage = "DTO 字段已创建"
        } else {
            dtoService.updateField(selectedId, UpdateDtoFieldMetaRequest(entityFieldId, name, code, type, nullable = nullable))
            statusMessage = "DTO 字段已更新"
        }
        refreshContextScope()
    }

    suspend fun removeDtoField(id: String) {
        dtoService.deleteField(id)
        statusMessage = "DTO 字段已删除"
        refreshContextScope()
    }

    suspend fun moveDtoField(id: String, delta: Int) {
        val dtoId = dtoFields.firstOrNull { it.id == id }?.dtoId ?: return
        val orderedIds = dtoFields
            .filter { it.dtoId == dtoId }
            .map { it.id }
            .toMutableList()
        moveId(orderedIds, id, delta)
        dtoService.reorderFields(dtoId, ReorderRequestDto(orderedIds))
        refreshContextScope()
    }

    suspend fun saveTemplate(
        selectedId: String?,
        name: String,
        key: String,
        outputKind: TemplateOutputKind,
        relativeOutputPath: String,
        fileNameTemplate: String,
        body: String,
        etlWrapperId: String?,
        enabled: Boolean,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            val created = templateService.create(
                CreateTemplateMetaRequest(
                    contextId = contextId,
                    etlWrapperId = etlWrapperId,
                    name = name,
                    key = key,
                    outputKind = outputKind,
                    body = body,
                    relativeOutputPath = relativeOutputPath,
                    fileNameTemplate = fileNameTemplate,
                    enabled = enabled,
                ),
            )
            selectedTemplateId = created.id
            statusMessage = "模板已创建"
        } else {
            val existing = templates.first { it.id == selectedId }
            val updated = templateService.update(
                selectedId,
                UpdateTemplateMetaRequest(
                    etlWrapperId = etlWrapperId,
                    name = name,
                    key = key,
                    outputKind = outputKind,
                    body = body,
                    relativeOutputPath = relativeOutputPath,
                    fileNameTemplate = fileNameTemplate,
                    tags = existing.tags,
                    enabled = enabled,
                    managedByGenerator = existing.managedByGenerator,
                ),
            )
            selectedTemplateId = updated.id
            statusMessage = "模板已更新"
        }
        refreshContextScope()
    }

    suspend fun removeTemplate(id: String) {
        templateService.delete(id)
        statusMessage = "模板已删除"
        refreshContextScope()
    }

    suspend fun moveTemplate(id: String, delta: Int) {
        val orderedIds = templates.map { it.id }.toMutableList()
        moveId(orderedIds, id, delta)
        val contextId = selectedContextId ?: return
        templateService.reorder(contextId, ReorderRequestDto(orderedIds))
        refreshContextScope()
    }

    suspend fun saveTarget(
        selectedId: String?,
        name: String,
        key: String,
        outputRoot: String,
        packageName: String,
        scaffoldPreset: ScaffoldPreset,
        templateIds: List<String>,
        variablesText: String,
        enableEtl: Boolean,
        autoIntegrateCompositeBuild: Boolean,
        managedMarker: String,
    ) {
        val contextId = selectedContextId ?: return
        val projectId = selectedProjectId ?: return
        val variables = parseVariablesText(variablesText)
        if (selectedId == null) {
            val created = targetService.create(
                CreateGenerationTargetMetaRequest(
                    projectId = projectId,
                    contextId = contextId,
                    name = name,
                    key = key,
                    outputRoot = outputRoot,
                    packageName = packageName,
                    scaffoldPreset = scaffoldPreset,
                    templateIds = templateIds,
                    variables = variables,
                    enableEtl = enableEtl,
                    autoIntegrateCompositeBuild = autoIntegrateCompositeBuild,
                    managedMarker = managedMarker,
                ),
            )
            selectedTargetId = created.id
            statusMessage = "生成目标已创建"
        } else {
            val updated = targetService.update(
                selectedId,
                UpdateGenerationTargetMetaRequest(
                    name = name,
                    key = key,
                    outputRoot = outputRoot,
                    packageName = packageName,
                    scaffoldPreset = scaffoldPreset,
                    templateIds = templateIds,
                    variables = variables,
                    enableEtl = enableEtl,
                    autoIntegrateCompositeBuild = autoIntegrateCompositeBuild,
                    managedMarker = managedMarker,
                ),
            )
            selectedTargetId = updated.id
            statusMessage = "生成目标已更新"
        }
        refreshContextScope()
    }

    suspend fun removeTarget(id: String) {
        targetService.delete(id)
        statusMessage = "生成目标已删除"
        refreshContextScope()
    }

    suspend fun saveEtlWrapper(
        selectedId: String?,
        name: String,
        key: String,
        scriptBody: String,
        enabled: Boolean,
    ) {
        val projectId = selectedProjectId ?: return
        if (selectedId == null) {
            val created = etlWrapperMetaService.create(
                CreateEtlWrapperMetaRequest(
                    projectId = projectId,
                    name = name,
                    key = key,
                    scriptBody = scriptBody,
                    enabled = enabled,
                ),
            )
            selectedEtlWrapperId = created.id
            statusMessage = "ETL 包裹器已创建"
        } else {
            val updated = etlWrapperMetaService.update(
                selectedId,
                UpdateEtlWrapperMetaRequest(
                    name = name,
                    key = key,
                    scriptBody = scriptBody,
                    enabled = enabled,
                ),
            )
            selectedEtlWrapperId = updated.id
            statusMessage = "ETL 包裹器已更新"
        }
        refreshProjectScope()
    }

    suspend fun removeEtlWrapper(id: String) {
        etlWrapperMetaService.delete(id)
        statusMessage = "ETL 包裹器已删除"
        refreshProjectScope()
    }

    suspend fun planSelectedTarget() {
        val targetId = selectedTargetId ?: return
        val contextId = selectedContextId ?: return
        lastGenerationPlan = generationPlanner.plan(GenerationRequestDto(targetId = targetId, contextId = contextId, previewOnly = true))
        statusMessage = "生成计划已刷新"
    }

    suspend fun generateSelectedTarget() {
        val targetId = selectedTargetId ?: return
        val contextId = selectedContextId ?: return
        lastGeneration = generationPlanner.generate(GenerationRequestDto(targetId = targetId, contextId = contextId))
        lastGenerationPlan = lastGeneration?.plan
        statusMessage = "代码已生成，共 ${lastGeneration?.files?.size ?: 0} 个文件"
    }

    suspend fun exportSelectedProjectSnapshot() {
        val projectId = selectedProjectId ?: return
        val snapshot = metadataSnapshotService.exportProject(projectId)
        lastSnapshotJson = json.encodeToString(snapshot)
        snapshotEditorText = lastSnapshotJson
        statusMessage = "快照已导出"
    }

    suspend fun importSnapshotFromEditor() {
        val snapshot = json.decodeFromString<MetadataSnapshotDto>(snapshotEditorText)
        metadataSnapshotService.importSnapshot(snapshot)
        statusMessage = "快照已导入"
        refreshAll()
    }

    suspend fun previewProjectDelete(id: String? = selectedProjectId) {
        val resolvedId = id ?: return
        lastDeleteCheck = projectService.deleteCheck(resolvedId)
    }

    suspend fun previewContextDelete(id: String? = selectedContextId) {
        val resolvedId = id ?: return
        lastDeleteCheck = contextService.deleteCheck(resolvedId)
    }

    suspend fun previewEntityDelete(id: String? = selectedEntityId) {
        val resolvedId = id ?: return
        lastDeleteCheck = entityService.deleteCheck(resolvedId)
    }

    suspend fun previewDtoDelete(id: String? = selectedDtoId) {
        val resolvedId = id ?: return
        lastDeleteCheck = dtoService.deleteCheck(resolvedId)
    }

    suspend fun previewTemplateDelete(id: String? = selectedTemplateId) {
        val resolvedId = id ?: return
        lastDeleteCheck = templateService.deleteCheck(resolvedId)
    }

    suspend fun previewTargetDelete(id: String? = selectedTargetId) {
        val resolvedId = id ?: return
        lastDeleteCheck = targetService.deleteCheck(resolvedId)
    }

    suspend fun previewEtlDelete(id: String? = selectedEtlWrapperId) {
        val resolvedId = id ?: return
        lastDeleteCheck = etlWrapperMetaService.deleteCheck(resolvedId)
    }

    suspend fun validateSelectedTemplate(id: String? = selectedTemplateId) {
        val resolvedId = id ?: return
        lastValidationIssues = templateService.validate(resolvedId)
    }

    suspend fun validateSelectedTarget(id: String? = selectedTargetId) {
        val resolvedId = id ?: return
        lastValidationIssues = targetService.validate(resolvedId)
    }

    suspend fun validateSelectedEtl(id: String? = selectedEtlWrapperId) {
        val resolvedId = id ?: return
        lastValidationIssues = etlWrapperMetaService.validate(resolvedId)
    }

    fun previewOutputRoot(rawPath: String, variablesText: String): String {
        outputRootPreview = runCatching {
            pathVariableResolver.resolve(rawPath, parseVariablesText(variablesText))
        }.getOrElse { throwable ->
            throwable.message ?: "路径解析失败"
        }
        return outputRootPreview
    }

    fun clearDiagnostics() {
        lastDeleteCheck = null
        lastValidationIssues = emptyList()
    }

    fun parseVariablesText(raw: String): Map<String, String> {
        return raw.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .associate { line ->
                val splitIndex = line.indexOf('=')
                if (splitIndex <= 0) {
                    line to ""
                } else {
                    line.substring(0, splitIndex).trim() to line.substring(splitIndex + 1).trim()
                }
            }
    }

    fun formatVariablesText(variables: Map<String, String>): String {
        return variables.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    fun fieldsForSelectedEntity(): List<FieldMetaDto> = fields.filter { it.entityId == selectedEntityId }

    fun relationsForSelectedEntity(): List<RelationMetaDto> = relations.filter { it.sourceEntityId == selectedEntityId }

    fun dtoFieldsForSelectedDto(): List<DtoFieldMetaDto> = dtoFields.filter { it.dtoId == selectedDtoId }

    fun filteredProjects(): List<ProjectMetaDto> = projects.filter { matchesExplorer(it.name) }

    fun filteredContexts(): List<BoundedContextMetaDto> = contexts.filter { matchesExplorer(it.name) }

    fun filteredEntities(): List<EntityMetaDto> = entities.filter { matchesExplorer(it.name) }

    fun filteredDtos(): List<DtoMetaDto> = dtos.filter { matchesExplorer(it.name) }

    fun filteredTemplates(): List<TemplateMetaDto> = templates.filter { matchesExplorer(it.name) }

    fun filteredTargets(): List<GenerationTargetMetaDto> = targets.filter { matchesExplorer(it.name) }

    fun filteredEtlWrappers(): List<EtlWrapperMetaDto> = etlWrappers.filter { matchesExplorer(it.name) }

    private fun matchesExplorer(name: String): Boolean {
        return explorerQuery.isBlank() || name.contains(explorerQuery, ignoreCase = true)
    }

    private fun moveId(ids: MutableList<String>, id: String, delta: Int) {
        val currentIndex = ids.indexOf(id)
        if (currentIndex < 0) {
            return
        }
        val targetIndex = (currentIndex + delta).coerceIn(0, ids.lastIndex)
        if (currentIndex == targetIndex) {
            return
        }
        ids.removeAt(currentIndex)
        ids.add(targetIndex, id)
    }

    private fun loadLanguage(): PlaygroundUiLanguage {
        return runCatching {
            PlaygroundUiLanguage.valueOf(prefs.get("language", PlaygroundUiLanguage.ZH_CN.name))
        }.getOrDefault(PlaygroundUiLanguage.ZH_CN)
    }
}
