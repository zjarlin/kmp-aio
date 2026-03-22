package site.addzero.coding.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*

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
    private val support: MetadataPersistenceSupport,
) {
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

    var statusMessage by mutableStateOf("准备就绪")
        private set
    var lastGeneration by mutableStateOf<GenerationResultDto?>(null)
        private set
    var lastSnapshotJson by mutableStateOf("")
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
        etlWrappers = if (projectId == null) emptyList() else etlWrapperMetaService.list(MetadataSearchRequest(projectId = projectId))
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
    }

    fun selectContext(id: String?) {
        selectedContextId = id
    }

    fun selectEntity(id: String?) {
        selectedEntityId = id
    }

    fun selectDto(id: String?) {
        selectedDtoId = id
    }

    fun selectTemplate(id: String?) {
        selectedTemplateId = id
    }

    fun selectTarget(id: String?) {
        selectedTargetId = id
    }

    fun selectEtlWrapper(id: String?) {
        selectedEtlWrapperId = id
    }

    suspend fun saveProject(
        selectedId: String?,
        name: String,
        slug: String,
        description: String,
    ) {
        if (selectedId == null) {
            projectService.create(CreateProjectMetaRequest(name = name, slug = slug, description = description.ifBlank { null }))
            statusMessage = "项目已创建"
        } else {
            projectService.update(selectedId, UpdateProjectMetaRequest(name = name, slug = slug, description = description.ifBlank { null }))
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
            contextService.create(CreateBoundedContextMetaRequest(projectId, name, code, description.ifBlank { null }))
            statusMessage = "上下文已创建"
        } else {
            contextService.update(selectedId, UpdateBoundedContextMetaRequest(name, code, description.ifBlank { null }))
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
            entityService.create(CreateEntityMetaRequest(contextId, name, code, tableName, description.ifBlank { null }))
            statusMessage = "实体已创建"
        } else {
            entityService.update(selectedId, UpdateEntityMetaRequest(name, code, tableName, description.ifBlank { null }))
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
    ) {
        if (selectedId == null) {
            entityService.createField(CreateFieldMetaRequest(entityId, name, code, type, nullable = nullable, idField = idField))
            statusMessage = "字段已创建"
        } else {
            entityService.updateField(selectedId, UpdateFieldMetaRequest(name, code, type, nullable = nullable, idField = idField))
            statusMessage = "字段已更新"
        }
        refreshContextScope()
    }

    suspend fun removeField(id: String) {
        entityService.deleteField(id)
        statusMessage = "字段已删除"
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
        name: String,
        code: String,
        kind: DtoKind,
        description: String,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            dtoService.create(CreateDtoMetaRequest(contextId, selectedEntityId, name, code, kind, description.ifBlank { null }))
            statusMessage = "DTO 已创建"
        } else {
            dtoService.update(selectedId, UpdateDtoMetaRequest(selectedEntityId, name, code, kind, description.ifBlank { null }))
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
        name: String,
        code: String,
        type: FieldType,
        nullable: Boolean,
    ) {
        if (selectedId == null) {
            dtoService.createField(CreateDtoFieldMetaRequest(dtoId, null, name, code, type, nullable = nullable))
            statusMessage = "DTO 字段已创建"
        } else {
            dtoService.updateField(selectedId, UpdateDtoFieldMetaRequest(null, name, code, type, nullable = nullable))
            statusMessage = "DTO 字段已更新"
        }
        refreshContextScope()
    }

    suspend fun removeDtoField(id: String) {
        dtoService.deleteField(id)
        statusMessage = "DTO 字段已删除"
        refreshContextScope()
    }

    suspend fun saveTemplate(
        selectedId: String?,
        name: String,
        key: String,
        relativeOutputPath: String,
        fileNameTemplate: String,
        body: String,
    ) {
        val contextId = selectedContextId ?: return
        if (selectedId == null) {
            templateService.create(
                CreateTemplateMetaRequest(
                    contextId = contextId,
                    name = name,
                    key = key,
                    outputKind = TemplateOutputKind.KOTLIN_SOURCE,
                    body = body,
                    relativeOutputPath = relativeOutputPath,
                    fileNameTemplate = fileNameTemplate,
                ),
            )
            statusMessage = "模板已创建"
        } else {
            val existing = templates.first { it.id == selectedId }
            templateService.update(
                selectedId,
                UpdateTemplateMetaRequest(
                    etlWrapperId = existing.etlWrapperId,
                    name = name,
                    key = key,
                    outputKind = existing.outputKind,
                    body = body,
                    relativeOutputPath = relativeOutputPath,
                    fileNameTemplate = fileNameTemplate,
                    enabled = existing.enabled,
                    managedByGenerator = existing.managedByGenerator,
                ),
            )
            statusMessage = "模板已更新"
        }
        refreshContextScope()
    }

    suspend fun removeTemplate(id: String) {
        templateService.delete(id)
        statusMessage = "模板已删除"
        refreshContextScope()
    }

    suspend fun saveTarget(
        selectedId: String?,
        name: String,
        key: String,
        outputRoot: String,
        packageName: String,
        templateIds: List<String>,
    ) {
        val contextId = selectedContextId ?: return
        val projectId = selectedProjectId ?: return
        if (selectedId == null) {
            targetService.create(
                CreateGenerationTargetMetaRequest(
                    projectId = projectId,
                    contextId = contextId,
                    name = name,
                    key = key,
                    outputRoot = outputRoot,
                    packageName = packageName,
                    templateIds = templateIds,
                ),
            )
            statusMessage = "生成目标已创建"
        } else {
            targetService.update(
                selectedId,
                UpdateGenerationTargetMetaRequest(
                    name = name,
                    key = key,
                    outputRoot = outputRoot,
                    packageName = packageName,
                    templateIds = templateIds,
                ),
            )
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
    ) {
        val projectId = selectedProjectId ?: return
        if (selectedId == null) {
            etlWrapperMetaService.create(CreateEtlWrapperMetaRequest(projectId, name, key, scriptBody = scriptBody))
            statusMessage = "ETL 包裹器已创建"
        } else {
            etlWrapperMetaService.update(selectedId, UpdateEtlWrapperMetaRequest(name, key, scriptBody = scriptBody))
            statusMessage = "ETL 包裹器已更新"
        }
        refreshProjectScope()
    }

    suspend fun removeEtlWrapper(id: String) {
        etlWrapperMetaService.delete(id)
        statusMessage = "ETL 包裹器已删除"
        refreshProjectScope()
    }

    suspend fun generateSelectedTarget() {
        val targetId = selectedTargetId ?: return
        val contextId = selectedContextId ?: return
        lastGeneration = generationPlanner.generate(GenerationRequestDto(targetId = targetId, contextId = contextId))
        statusMessage = "代码已生成，共 ${lastGeneration?.files?.size ?: 0} 个文件"
    }

    suspend fun exportSelectedProjectSnapshot() {
        val projectId = selectedProjectId ?: return
        val snapshot = metadataSnapshotService.exportProject(projectId)
        lastSnapshotJson = Json { prettyPrint = true }.encodeToString(snapshot)
        statusMessage = "快照已导出"
    }

    fun fieldsForSelectedEntity(): List<FieldMetaDto> = fields.filter { it.entityId == selectedEntityId }
    fun relationsForSelectedEntity(): List<RelationMetaDto> = relations.filter { it.sourceEntityId == selectedEntityId }
    fun dtoFieldsForSelectedDto(): List<DtoFieldMetaDto> = dtoFields.filter { it.dtoId == selectedDtoId }
}
