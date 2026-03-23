package site.addzero.coding.playground.server.service

import kotlinx.serialization.json.Json
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.domain.PlaygroundNotFoundException
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.BoundedContextMeta
import site.addzero.coding.playground.server.entity.DtoFieldMeta
import site.addzero.coding.playground.server.entity.DtoMeta
import site.addzero.coding.playground.server.entity.EntityMeta
import site.addzero.coding.playground.server.entity.EtlWrapperMeta
import site.addzero.coding.playground.server.entity.FieldMeta
import site.addzero.coding.playground.server.entity.GenerationTargetMeta
import site.addzero.coding.playground.server.entity.ProjectMeta
import site.addzero.coding.playground.server.entity.RelationMeta
import site.addzero.coding.playground.server.entity.TemplateMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.decodeStringList
import site.addzero.coding.playground.server.entity.encodeStringList
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.server.generation.BuiltinTemplateCatalog
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.CreateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.ProjectAggregateDto
import java.sql.Connection
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

@Single
class MetadataPersistenceSupport(
    val sqlClient: KSqlClient,
    private val dataSource: DataSource,
    val json: Json,
    private val builtinTemplateCatalog: BuiltinTemplateCatalog,
) {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun projectRef(id: String): ProjectMeta = new(ProjectMeta::class).by { this.id = id }
    fun contextRef(id: String): BoundedContextMeta = new(BoundedContextMeta::class).by { this.id = id }
    fun entityRef(id: String): EntityMeta = new(EntityMeta::class).by { this.id = id }
    fun fieldRef(id: String): FieldMeta = new(FieldMeta::class).by { this.id = id }
    fun dtoRef(id: String): DtoMeta = new(DtoMeta::class).by { this.id = id }
    fun etlWrapperRef(id: String): EtlWrapperMeta = new(EtlWrapperMeta::class).by { this.id = id }

    fun <T> inTransaction(block: () -> T): T {
        return PlaygroundJdbcTransactionContext.withTransaction(dataSource, block)
    }

    fun <T> withJdbcConnection(block: (Connection) -> T): T {
        val current = PlaygroundJdbcTransactionContext.connectionOrNull()
        if (current != null) {
            return block(current)
        }
        return dataSource.connection.use(block)
    }

    fun projectOrThrow(id: String): ProjectMeta {
        return sqlClient.findById(ProjectMeta::class, id)
            ?: throw PlaygroundNotFoundException("Project '$id' not found")
    }

    fun contextOrThrow(id: String): BoundedContextMeta {
        return sqlClient.findById(BoundedContextMeta::class, id)
            ?: throw PlaygroundNotFoundException("Context '$id' not found")
    }

    fun entityOrThrow(id: String): EntityMeta {
        return sqlClient.findById(EntityMeta::class, id)
            ?: throw PlaygroundNotFoundException("Entity '$id' not found")
    }

    fun fieldOrThrow(id: String): FieldMeta {
        return sqlClient.findById(FieldMeta::class, id)
            ?: throw PlaygroundNotFoundException("Field '$id' not found")
    }

    fun relationOrThrow(id: String): RelationMeta {
        return sqlClient.findById(RelationMeta::class, id)
            ?: throw PlaygroundNotFoundException("Relation '$id' not found")
    }

    fun dtoOrThrow(id: String): DtoMeta {
        return sqlClient.findById(DtoMeta::class, id)
            ?: throw PlaygroundNotFoundException("DTO '$id' not found")
    }

    fun dtoFieldOrThrow(id: String): DtoFieldMeta {
        return sqlClient.findById(DtoFieldMeta::class, id)
            ?: throw PlaygroundNotFoundException("DTO field '$id' not found")
    }

    fun templateOrThrow(id: String): TemplateMeta {
        return sqlClient.findById(TemplateMeta::class, id)
            ?: throw PlaygroundNotFoundException("Template '$id' not found")
    }

    fun targetOrThrow(id: String): GenerationTargetMeta {
        return sqlClient.findById(GenerationTargetMeta::class, id)
            ?: throw PlaygroundNotFoundException("Generation target '$id' not found")
    }

    fun etlWrapperOrThrow(id: String): EtlWrapperMeta {
        return sqlClient.findById(EtlWrapperMeta::class, id)
            ?: throw PlaygroundNotFoundException("ETL wrapper '$id' not found")
    }

    fun listProjects(): List<ProjectMeta> {
        return sqlClient.createQuery(ProjectMeta::class) {
            select(table)
        }.execute().sortedBy { it.orderIndex }
    }

    fun listContexts(projectId: String? = null): List<BoundedContextMeta> {
        val items = sqlClient.createQuery(BoundedContextMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { projectId == null || it.projectId == projectId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listEntities(contextId: String? = null): List<EntityMeta> {
        val items = sqlClient.createQuery(EntityMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { contextId == null || it.contextId == contextId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listFields(entityId: String? = null): List<FieldMeta> {
        val items = sqlClient.createQuery(FieldMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { entityId == null || it.entityId == entityId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listRelations(contextId: String? = null): List<RelationMeta> {
        val items = sqlClient.createQuery(RelationMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { contextId == null || it.contextId == contextId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listDtos(contextId: String? = null): List<DtoMeta> {
        val items = sqlClient.createQuery(DtoMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { contextId == null || it.contextId == contextId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listDtoFields(dtoId: String? = null): List<DtoFieldMeta> {
        val items = sqlClient.createQuery(DtoFieldMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { dtoId == null || it.dtoId == dtoId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listTemplates(contextId: String? = null): List<TemplateMeta> {
        val items = sqlClient.createQuery(TemplateMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { contextId == null || it.contextId == contextId }
            .sortedBy { it.orderIndex }
            .toList()
    }

    fun listTargets(projectId: String? = null, contextId: String? = null): List<GenerationTargetMeta> {
        val items = sqlClient.createQuery(GenerationTargetMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { projectId == null || it.projectId == projectId }
            .filter { contextId == null || it.contextId == contextId }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    fun listEtlWrappers(projectId: String? = null): List<EtlWrapperMeta> {
        val items = sqlClient.createQuery(EtlWrapperMeta::class) {
            select(table)
        }.execute()
        return items
            .asSequence()
            .filter { projectId == null || it.projectId == projectId }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    fun nextContextOrder(projectId: String): Int = listContexts(projectId).size
    fun nextEntityOrder(contextId: String): Int = listEntities(contextId).size
    fun nextFieldOrder(entityId: String): Int = listFields(entityId).size
    fun nextRelationOrder(contextId: String): Int = listRelations(contextId).size
    fun nextDtoOrder(contextId: String): Int = listDtos(contextId).size
    fun nextDtoFieldOrder(dtoId: String): Int = listDtoFields(dtoId).size
    fun nextTemplateOrder(contextId: String): Int = listTemplates(contextId).size

    fun templateIdsForTarget(targetId: String): List<String> {
        return withJdbcConnection { connection ->
            connection.prepareStatement(
                """
                SELECT gtt.template_id
                FROM generation_target_template gtt
                JOIN template_meta tm ON tm.id = gtt.template_id
                WHERE gtt.generation_target_id = ?
                ORDER BY tm.order_index ASC, tm.name ASC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, targetId)
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.getString(1))
                        }
                    }
                }
            }
        }
    }

    fun syncTargetTemplates(targetId: String, templateIds: List<String>) {
        inTransaction {
            withJdbcConnection { connection ->
                connection.prepareStatement(
                    "DELETE FROM generation_target_template WHERE generation_target_id = ?",
                ).use { statement ->
                    statement.setString(1, targetId)
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    "INSERT INTO generation_target_template(generation_target_id, template_id) VALUES(?, ?)",
                ).use { statement ->
                    templateIds.distinct().forEach { templateId ->
                        statement.setString(1, targetId)
                        statement.setString(2, templateId)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
            }
        }
    }

    fun deleteTargetTemplateLinksByTarget(targetId: String) {
        executeUpdate("DELETE FROM generation_target_template WHERE generation_target_id = ?", targetId)
    }

    fun deleteTargetTemplateLinksByTemplate(templateId: String) {
        executeUpdate("DELETE FROM generation_target_template WHERE template_id = ?", templateId)
    }

    fun countTargetTemplateReferences(templateId: String): Int {
        return withJdbcConnection { connection ->
            connection.prepareStatement(
                "SELECT COUNT(*) FROM generation_target_template WHERE template_id = ?",
            ).use { statement ->
                statement.setString(1, templateId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            }
        }
    }

    fun assertProjectSlugUnique(slug: String, currentId: String? = null) {
        val conflicts = listProjects().filter { it.slug == slug && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Project slug '$slug' already exists")
        }
    }

    fun assertContextCodeUnique(projectId: String, code: String, currentId: String? = null) {
        val conflicts = listContexts(projectId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Context code '$code' already exists in project")
        }
    }

    fun assertEntityCodeUnique(contextId: String, code: String, currentId: String? = null) {
        val conflicts = listEntities(contextId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Entity code '$code' already exists in context")
        }
    }

    fun assertFieldCodeUnique(entityId: String, code: String, currentId: String? = null) {
        val conflicts = listFields(entityId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Field code '$code' already exists in entity")
        }
    }

    fun assertRelationCodeUnique(contextId: String, code: String, currentId: String? = null) {
        val conflicts = listRelations(contextId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Relation code '$code' already exists in context")
        }
    }

    fun assertDtoCodeUnique(contextId: String, code: String, currentId: String? = null) {
        val conflicts = listDtos(contextId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("DTO code '$code' already exists in context")
        }
    }

    fun assertDtoFieldCodeUnique(dtoId: String, code: String, currentId: String? = null) {
        val conflicts = listDtoFields(dtoId).filter { it.code == code && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("DTO field code '$code' already exists in DTO")
        }
    }

    fun assertTemplateKeyUnique(contextId: String, key: String, currentId: String? = null) {
        val conflicts = listTemplates(contextId).filter { it.key == key && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Template key '$key' already exists in context")
        }
    }

    fun assertTargetKeyUnique(projectId: String, key: String, currentId: String? = null) {
        val conflicts = listTargets(projectId = projectId).filter { it.key == key && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("Generation target key '$key' already exists in project")
        }
    }

    fun assertEtlKeyUnique(projectId: String, key: String, currentId: String? = null) {
        val conflicts = listEtlWrappers(projectId).filter { it.key == key && it.id != currentId }
        if (conflicts.isNotEmpty()) {
            throw PlaygroundValidationException("ETL wrapper key '$key' already exists in project")
        }
    }

    fun validateName(value: String, label: String) {
        if (value.isBlank()) {
            throw PlaygroundValidationException("$label cannot be blank")
        }
    }

    fun validateFieldType(value: String) {
        val supported = site.addzero.coding.playground.shared.dto.FieldType.entries.map { it.name }.toSet()
        if (value !in supported) {
            throw PlaygroundValidationException("Unsupported field type '$value'")
        }
    }

    fun validateDtoKind(value: String) {
        val supported = site.addzero.coding.playground.shared.dto.DtoKind.entries.map { it.name }.toSet()
        if (value !in supported) {
            throw PlaygroundValidationException("Unsupported DTO kind '$value'")
        }
    }

    fun validateRelationKind(value: String) {
        val supported = site.addzero.coding.playground.shared.dto.RelationKind.entries.map { it.name }.toSet()
        if (value !in supported) {
            throw PlaygroundValidationException("Unsupported relation kind '$value'")
        }
    }

    fun validateTemplateOutputKind(value: String) {
        val supported = site.addzero.coding.playground.shared.dto.TemplateOutputKind.entries.map { it.name }.toSet()
        if (value !in supported) {
            throw PlaygroundValidationException("Unsupported template output kind '$value'")
        }
    }

    fun validateScaffoldPreset(value: String) {
        val supported = site.addzero.coding.playground.shared.dto.ScaffoldPreset.entries.map { it.name }.toSet()
        if (value !in supported) {
            throw PlaygroundValidationException("Unsupported scaffold preset '$value'")
        }
    }

    fun ensureEntityDeleteAllowed(entityId: String): DeleteCheckResultDto {
        val relationCount = listRelations().count { it.sourceEntityId == entityId || it.targetEntityId == entityId }
        val dtoFieldCount = listDtoFields().count { it.entityFieldId != null && listFields(entityId).any { field -> field.id == it.entityFieldId } }
        val reasons = buildList {
            if (relationCount > 0) {
                add("Entity is referenced by $relationCount relation(s)")
            }
            if (dtoFieldCount > 0) {
                add("Entity fields are referenced by $dtoFieldCount DTO field(s)")
            }
        }
        return DeleteCheckResultDto(
            allowed = reasons.isEmpty(),
            reasons = reasons,
        )
    }

    fun ensureTemplateDeleteAllowed(templateId: String): DeleteCheckResultDto {
        val count = countTargetTemplateReferences(templateId)
        return if (count == 0) {
            DeleteCheckResultDto(allowed = true)
        } else {
            DeleteCheckResultDto(
                allowed = false,
                reasons = listOf("Template is still referenced by $count generation target(s)"),
            )
        }
    }

    fun ensureProjectContextAlignment(projectId: String, contextId: String) {
        val context = contextOrThrow(contextId)
        if (context.projectId != projectId) {
            throw PlaygroundValidationException("Context '$contextId' does not belong to project '$projectId'")
        }
    }

    fun seedBuiltinTemplates(contextId: String) {
        if (listTemplates(contextId).isNotEmpty()) {
            return
        }
        inTransaction {
            builtinTemplateCatalog.createRequests(contextId).forEachIndexed { index, request ->
                val now = now()
                val entity = new(TemplateMeta::class).by {
                    id = newId()
                    context = contextRef(request.contextId)
                    etlWrapper = request.etlWrapperId?.let(::etlWrapperRef)
                    name = request.name
                    key = request.key
                    description = request.description
                    outputKind = request.outputKind.name
                    body = request.body
                    relativeOutputPath = request.relativeOutputPath
                    fileNameTemplate = request.fileNameTemplate
                    tagsJson = json.encodeStringList(request.tags)
                    orderIndex = index
                    enabled = request.enabled
                    managedByGenerator = request.managedByGenerator
                    createdAt = now
                    updatedAt = now
                }
                sqlClient.save(entity)
            }
        }
    }

    fun buildContextAggregate(contextId: String): ContextAggregateDto {
        val context = contextOrThrow(contextId)
        val entities = listEntities(contextId).map { it.toDto(json) }
        val fields = entities.flatMap { entity -> listFields(entity.id).map { it.toDto() } }
        val relations = listRelations(contextId).map { it.toDto() }
        val dtos = listDtos(contextId).map { it.toDto(json) }
        val dtoFields = dtos.flatMap { dto -> listDtoFields(dto.id).map { it.toDto() } }
        val templates = listTemplates(contextId).map { it.toDto(json) }
        val targets = listTargets(contextId = contextId).map { it.toDto(json, templateIdsForTarget(it.id)) }
        return ContextAggregateDto(
            context = context.toDto(json),
            entities = entities,
            fields = fields,
            relations = relations,
            dtos = dtos,
            dtoFields = dtoFields,
            templates = templates,
            generationTargets = targets,
        )
    }

    fun buildProjectAggregate(projectId: String): ProjectAggregateDto {
        val project = projectOrThrow(projectId)
        val contexts = listContexts(projectId).map { buildContextAggregate(it.id) }
        val etlWrappers = listEtlWrappers(projectId).map { it.toDto() }
        return ProjectAggregateDto(
            project = project.toDto(json),
            contexts = contexts,
            etlWrappers = etlWrappers,
        )
    }

    fun matchesSearch(search: MetadataSearchRequest, title: String, tags: List<String>, projectId: String? = null, contextId: String? = null): Boolean {
        val queryMatches = search.query.isNullOrBlank() || title.contains(search.query!!, ignoreCase = true)
        val tagMatches = search.tag.isNullOrBlank() || tags.any { it.equals(search.tag, ignoreCase = true) }
        val projectMatches = search.projectId == null || projectId == search.projectId
        val contextMatches = search.contextId == null || contextId == search.contextId
        return queryMatches && tagMatches && projectMatches && contextMatches
    }

    fun deleteField(id: String) {
        val references = listDtoFields().count { it.entityFieldId == id }
        if (references > 0) {
            throw PlaygroundValidationException("Field '$id' is still referenced by $references DTO field(s)")
        }
        sqlClient.deleteById(FieldMeta::class, id)
    }

    fun deleteDtoField(id: String) {
        sqlClient.deleteById(DtoFieldMeta::class, id)
    }

    fun deleteDto(id: String) {
        listDtoFields(id).forEach { sqlClient.deleteById(DtoFieldMeta::class, it.id) }
        sqlClient.deleteById(DtoMeta::class, id)
    }

    fun deleteEntity(id: String) {
        listFields(id).forEach { deleteField(it.id) }
        sqlClient.deleteById(EntityMeta::class, id)
    }

    fun deleteContextCascade(id: String) {
        listTargets(contextId = id).forEach {
            deleteTargetTemplateLinksByTarget(it.id)
            sqlClient.deleteById(GenerationTargetMeta::class, it.id)
        }
        listTemplates(id).forEach {
            deleteTargetTemplateLinksByTemplate(it.id)
            sqlClient.deleteById(TemplateMeta::class, it.id)
        }
        listDtos(id).forEach { deleteDto(it.id) }
        listRelations(id).forEach { sqlClient.deleteById(RelationMeta::class, it.id) }
        listEntities(id).forEach { deleteEntity(it.id) }
        sqlClient.deleteById(BoundedContextMeta::class, id)
    }

    fun deleteProjectCascade(id: String) {
        listContexts(id).forEach { deleteContextCascade(it.id) }
        listEtlWrappers(id).forEach { sqlClient.deleteById(EtlWrapperMeta::class, it.id) }
        sqlClient.deleteById(ProjectMeta::class, id)
    }

    fun countCascadeSummaryForProject(id: String): List<String> {
        val contextCount = listContexts(id).size
        val entityCount = listContexts(id).sumOf { listEntities(it.id).size }
        val dtoCount = listContexts(id).sumOf { listDtos(it.id).size }
        val templateCount = listContexts(id).sumOf { listTemplates(it.id).size }
        return listOf(
            "Deleting this project will cascade to $contextCount context(s)",
            "Deleting this project will cascade to $entityCount entity metadata record(s)",
            "Deleting this project will cascade to $dtoCount DTO metadata record(s)",
            "Deleting this project will cascade to $templateCount template record(s)",
        )
    }

    fun countCascadeSummaryForContext(id: String): List<String> {
        return listOf(
            "Deleting this context will cascade to ${listEntities(id).size} entity metadata record(s)",
            "Deleting this context will cascade to ${listDtos(id).size} DTO metadata record(s)",
            "Deleting this context will cascade to ${listTemplates(id).size} template record(s)",
            "Deleting this context will cascade to ${listTargets(contextId = id).size} generation target(s)",
        )
    }

    private fun executeUpdate(sql: String, value: String) {
        withJdbcConnection { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, value)
                statement.executeUpdate()
            }
        }
    }
}
