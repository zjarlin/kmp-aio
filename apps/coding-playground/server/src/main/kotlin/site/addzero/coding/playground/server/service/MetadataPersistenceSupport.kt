package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.domain.PlaygroundNotFoundException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.CodegenProjectAggregateDto
import site.addzero.coding.playground.shared.dto.SourceFileAggregateDto
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

@Single
class MetadataPersistenceSupport(
    val sqlClient: KSqlClient,
    private val dataSource: DataSource,
) {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun projectRef(id: String): CodegenProject = new(CodegenProject::class).by { this.id = id }
    fun targetRef(id: String): GenerationTarget = new(GenerationTarget::class).by { this.id = id }
    fun fileRef(id: String): SourceFileMeta = new(SourceFileMeta::class).by { this.id = id }
    fun declarationRef(id: String): DeclarationMeta = new(DeclarationMeta::class).by { this.id = id }
    fun annotationUsageRef(id: String): AnnotationUsageMeta = new(AnnotationUsageMeta::class).by { this.id = id }
    fun artifactRef(id: String): ManagedArtifactMeta = new(ManagedArtifactMeta::class).by { this.id = id }

    fun <T> inTransaction(block: () -> T): T = PlaygroundJdbcTransactionContext.withTransaction(dataSource, block)

    fun projectOrThrow(id: String): CodegenProject =
        sqlClient.findById(CodegenProject::class, id) ?: throw PlaygroundNotFoundException("项目不存在: $id")

    fun targetOrThrow(id: String): GenerationTarget =
        sqlClient.findById(GenerationTarget::class, id) ?: throw PlaygroundNotFoundException("生成目标不存在: $id")

    fun fileOrThrow(id: String): SourceFileMeta =
        sqlClient.findById(SourceFileMeta::class, id) ?: throw PlaygroundNotFoundException("Kotlin 文件不存在: $id")

    fun declarationOrThrow(id: String): DeclarationMeta =
        sqlClient.findById(DeclarationMeta::class, id) ?: throw PlaygroundNotFoundException("声明不存在: $id")

    fun constructorParamOrThrow(id: String): ConstructorParamMeta =
        sqlClient.findById(ConstructorParamMeta::class, id) ?: throw PlaygroundNotFoundException("构造参数不存在: $id")

    fun propertyOrThrow(id: String): PropertyMeta =
        sqlClient.findById(PropertyMeta::class, id) ?: throw PlaygroundNotFoundException("属性不存在: $id")

    fun enumEntryOrThrow(id: String): EnumEntryMeta =
        sqlClient.findById(EnumEntryMeta::class, id) ?: throw PlaygroundNotFoundException("枚举项不存在: $id")

    fun annotationUsageOrThrow(id: String): AnnotationUsageMeta =
        sqlClient.findById(AnnotationUsageMeta::class, id) ?: throw PlaygroundNotFoundException("注解使用不存在: $id")

    fun annotationArgumentOrThrow(id: String): AnnotationArgumentMeta =
        sqlClient.findById(AnnotationArgumentMeta::class, id) ?: throw PlaygroundNotFoundException("注解参数不存在: $id")

    fun importOrThrow(id: String): ImportMeta =
        sqlClient.findById(ImportMeta::class, id) ?: throw PlaygroundNotFoundException("导包不存在: $id")

    fun functionStubOrThrow(id: String): FunctionStubMeta =
        sqlClient.findById(FunctionStubMeta::class, id) ?: throw PlaygroundNotFoundException("函数桩不存在: $id")

    fun artifactOrThrow(id: String): ManagedArtifactMeta =
        sqlClient.findById(ManagedArtifactMeta::class, id) ?: throw PlaygroundNotFoundException("托管产物不存在: $id")

    fun conflictOrThrow(id: String): SyncConflictMeta =
        sqlClient.findById(SyncConflictMeta::class, id) ?: throw PlaygroundNotFoundException("同步冲突不存在: $id")

    fun listProjects(): List<CodegenProject> = sqlClient.createQuery(CodegenProject::class) { select(table) }
        .execute()
        .sortedBy { it.name.lowercase() }

    fun listTargets(projectId: String? = null): List<GenerationTarget> = sqlClient.createQuery(GenerationTarget::class) { select(table) }
        .execute()
        .filter { projectId == null || it.projectId == projectId }
        .sortedBy { it.name.lowercase() }

    fun listFiles(targetId: String? = null): List<SourceFileMeta> = sqlClient.createQuery(SourceFileMeta::class) { select(table) }
        .execute()
        .filter { targetId == null || it.targetId == targetId }
        .sortedBy { it.orderIndex }

    fun listDeclarations(fileId: String? = null): List<DeclarationMeta> = sqlClient.createQuery(DeclarationMeta::class) { select(table) }
        .execute()
        .filter { fileId == null || it.fileId == fileId }
        .sortedBy { it.orderIndex }

    fun listConstructorParams(declarationId: String? = null): List<ConstructorParamMeta> =
        sqlClient.createQuery(ConstructorParamMeta::class) { select(table) }
            .execute()
            .filter { declarationId == null || it.declarationId == declarationId }
            .sortedBy { it.orderIndex }

    fun listProperties(declarationId: String? = null): List<PropertyMeta> =
        sqlClient.createQuery(PropertyMeta::class) { select(table) }
            .execute()
            .filter { declarationId == null || it.declarationId == declarationId }
            .sortedBy { it.orderIndex }

    fun listEnumEntries(declarationId: String? = null): List<EnumEntryMeta> =
        sqlClient.createQuery(EnumEntryMeta::class) { select(table) }
            .execute()
            .filter { declarationId == null || it.declarationId == declarationId }
            .sortedBy { it.orderIndex }

    fun listAnnotations(ownerId: String? = null): List<AnnotationUsageMeta> =
        sqlClient.createQuery(AnnotationUsageMeta::class) { select(table) }
            .execute()
            .filter { ownerId == null || it.ownerId == ownerId }
            .sortedBy { it.orderIndex }

    fun listAnnotationArguments(annotationUsageId: String? = null): List<AnnotationArgumentMeta> =
        sqlClient.createQuery(AnnotationArgumentMeta::class) { select(table) }
            .execute()
            .filter { annotationUsageId == null || it.annotationUsageId == annotationUsageId }
            .sortedBy { it.orderIndex }

    fun listImports(fileId: String? = null): List<ImportMeta> = sqlClient.createQuery(ImportMeta::class) { select(table) }
        .execute()
        .filter { fileId == null || it.fileId == fileId }
        .sortedBy { it.orderIndex }

    fun listFunctionStubs(declarationId: String? = null): List<FunctionStubMeta> =
        sqlClient.createQuery(FunctionStubMeta::class) { select(table) }
            .execute()
            .filter { declarationId == null || it.declarationId == declarationId }
            .sortedBy { it.orderIndex }

    fun listArtifacts(targetId: String? = null, fileId: String? = null): List<ManagedArtifactMeta> =
        sqlClient.createQuery(ManagedArtifactMeta::class) { select(table) }
            .execute()
            .filter { (targetId == null || it.targetId == targetId) && (fileId == null || it.fileId == fileId) }
            .sortedByDescending { it.updatedAt }

    fun listConflicts(targetId: String? = null, fileId: String? = null): List<SyncConflictMeta> =
        sqlClient.createQuery(SyncConflictMeta::class) { select(table) }
            .execute()
            .filter { (targetId == null || it.targetId == targetId) && (fileId == null || it.fileId == fileId) }
            .sortedByDescending { it.updatedAt }

    fun buildProjectAggregate(projectId: String): CodegenProjectAggregateDto {
        val project = projectOrThrow(projectId).toDto()
        val targets = listTargets(projectId)
        val targetIds = targets.map { it.id }.toSet()
        val files = listFiles().filter { it.targetId in targetIds }
        val fileIds = files.map { it.id }.toSet()
        val declarations = listDeclarations().filter { it.fileId in fileIds }
        val declarationIds = declarations.map { it.id }.toSet()
        val annotationUsages = sqlClient.createQuery(AnnotationUsageMeta::class) { select(table) }
            .execute()
            .filter { usage ->
                usage.ownerId in fileIds || usage.ownerId in declarationIds || usage.ownerId in listConstructorParams().map { it.id } ||
                    usage.ownerId in listProperties().map { it.id } || usage.ownerId in listFunctionStubs().map { it.id }
            }
            .sortedBy { it.orderIndex }
        val annotationIds = annotationUsages.map { it.id }.toSet()
        return CodegenProjectAggregateDto(
            project = project,
            targets = targets.map { it.toDto() },
            files = files.map { it.toDto() },
            declarations = declarations.map { it.toDto() },
            constructorParams = listConstructorParams().filter { it.declarationId in declarationIds }.map { it.toDto() },
            properties = listProperties().filter { it.declarationId in declarationIds }.map { it.toDto() },
            enumEntries = listEnumEntries().filter { it.declarationId in declarationIds }.map { it.toDto() },
            annotations = annotationUsages.map { it.toDto() },
            annotationArguments = listAnnotationArguments().filter { it.annotationUsageId in annotationIds }.map { it.toDto() },
            imports = listImports().filter { it.fileId in fileIds }.map { it.toDto() },
            functionStubs = listFunctionStubs().filter { it.declarationId in declarationIds }.map { it.toDto() },
            artifacts = listArtifacts().filter { it.fileId in fileIds }.map { it.toDto() },
            conflicts = listConflicts().filter { it.fileId in fileIds }.map { it.toDto() },
        )
    }

    fun buildFileAggregate(fileId: String): SourceFileAggregateDto {
        val file = fileOrThrow(fileId)
        val declarations = listDeclarations(fileId)
        val declarationIds = declarations.map { it.id }.toSet()
        val paramIds = listConstructorParams().filter { it.declarationId in declarationIds }.map { it.id }.toSet()
        val propertyIds = listProperties().filter { it.declarationId in declarationIds }.map { it.id }.toSet()
        val functionIds = listFunctionStubs().filter { it.declarationId in declarationIds }.map { it.id }.toSet()
        val annotations = sqlClient.createQuery(AnnotationUsageMeta::class) { select(table) }
            .execute()
            .filter { it.ownerId == fileId || it.ownerId in declarationIds || it.ownerId in paramIds || it.ownerId in propertyIds || it.ownerId in functionIds }
            .sortedBy { it.orderIndex }
        val annotationIds = annotations.map { it.id }.toSet()
        return SourceFileAggregateDto(
            file = file.toDto(),
            imports = listImports(fileId).map { it.toDto() },
            declarations = declarations.map { it.toDto() },
            constructorParams = listConstructorParams().filter { it.declarationId in declarationIds }.map { it.toDto() },
            properties = listProperties().filter { it.declarationId in declarationIds }.map { it.toDto() },
            enumEntries = listEnumEntries().filter { it.declarationId in declarationIds }.map { it.toDto() },
            annotations = annotations.map { it.toDto() },
            annotationArguments = listAnnotationArguments().filter { it.annotationUsageId in annotationIds }.map { it.toDto() },
            functionStubs = listFunctionStubs().filter { it.declarationId in declarationIds }.map { it.toDto() },
            artifacts = listArtifacts(fileId = fileId).map { it.toDto() },
            conflicts = listConflicts(fileId = fileId).map { it.toDto() },
        )
    }

    fun deleteProjectCascade(projectId: String) {
        listTargets(projectId).forEach { deleteTargetCascade(it.id) }
        sqlClient.deleteById(CodegenProject::class, projectId)
    }

    fun deleteTargetCascade(targetId: String) {
        listFiles(targetId).forEach { deleteFileCascade(it.id) }
        listConflicts(targetId = targetId).forEach { deleteEntity<SyncConflictMeta>(it.id) }
        listArtifacts(targetId = targetId).forEach { deleteEntity<ManagedArtifactMeta>(it.id) }
        sqlClient.deleteById(GenerationTarget::class, targetId)
    }

    fun deleteFileCascade(fileId: String) {
        listDeclarations(fileId).forEach { deleteDeclarationCascade(it.id) }
        listImports(fileId).forEach { deleteEntity<ImportMeta>(it.id) }
        listConflicts(fileId = fileId).forEach { deleteEntity<SyncConflictMeta>(it.id) }
        listArtifacts(fileId = fileId).forEach { deleteEntity<ManagedArtifactMeta>(it.id) }
        val annotationIds = listAnnotations(fileId).map { it.id }
        deleteAnnotationUsageCascade(annotationIds)
        sqlClient.deleteById(SourceFileMeta::class, fileId)
    }

    fun deleteDeclarationCascade(declarationId: String) {
        val paramIds = listConstructorParams(declarationId).map { it.id }
        val propertyIds = listProperties(declarationId).map { it.id }
        val functionIds = listFunctionStubs(declarationId).map { it.id }
        val annotationIds = sqlClient.createQuery(AnnotationUsageMeta::class) { select(table) }
            .execute()
            .filter { it.ownerId == declarationId || it.ownerId in paramIds || it.ownerId in propertyIds || it.ownerId in functionIds }
            .map { it.id }
        deleteAnnotationUsageCascade(annotationIds)
        listFunctionStubs(declarationId).forEach { deleteEntity<FunctionStubMeta>(it.id) }
        listEnumEntries(declarationId).forEach { deleteEntity<EnumEntryMeta>(it.id) }
        listProperties(declarationId).forEach { deleteEntity<PropertyMeta>(it.id) }
        listConstructorParams(declarationId).forEach { deleteEntity<ConstructorParamMeta>(it.id) }
        sqlClient.deleteById(DeclarationMeta::class, declarationId)
    }

    private fun deleteAnnotationUsageCascade(annotationIds: List<String>) {
        if (annotationIds.isEmpty()) {
            return
        }
        listAnnotationArguments().filter { it.annotationUsageId in annotationIds }.forEach { deleteEntity<AnnotationArgumentMeta>(it.id) }
        annotationIds.forEach { deleteEntity<AnnotationUsageMeta>(it) }
    }

    private inline fun <reified E : Any> deleteEntity(id: String) {
        sqlClient.deleteById(E::class, id)
    }
}
