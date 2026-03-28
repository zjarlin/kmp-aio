package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.ImportMeta
import site.addzero.coding.playground.server.entity.SourceFileMeta
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.server.util.ensureKtFileName
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.DeclarationService
import site.addzero.coding.playground.shared.service.SourceFileService

@Single
class SourceFileServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val serviceSupport: CodegenServiceSupport,
    private val declarationService: DeclarationService,
) : SourceFileService {
    override suspend fun create(request: CreateSourceFileRequest): SourceFileMetaDto {
        val target = support.targetOrThrow(request.targetId)
        val normalizedFileName = serviceSupport.requireFileName(request.fileName)
        serviceSupport.requirePackageName(request.packageName, "文件包名")
        if (support.listFiles(target.id).any { it.packageName == request.packageName && it.fileName == normalizedFileName }) {
            throw PlaygroundValidationException("同一目标下包名和文件名组合不能重复")
        }
        val now = support.now()
        val entity = new(SourceFileMeta::class).by {
            id = support.newId()
            project = support.projectRef(target.projectId)
            this.target = support.targetRef(target.id)
            packageName = request.packageName.trim()
            fileName = normalizedFileName
            docComment = request.docComment?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(support.listFiles(target.id).map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun createPreset(request: CreateDeclarationPresetRequest): SourceFileAggregateDto {
        val target = support.targetOrThrow(request.targetId)
        val declarationName = request.declarationName.trim()
        serviceSupport.requireIdentifier(declarationName, "声明名称")
        val file = create(
            CreateSourceFileRequest(
                targetId = target.id,
                packageName = request.packageName.ifBlank { target.basePackage },
                fileName = declarationName.ensureKtFileName(),
            ),
        )
        val declaration = declarationService.create(
            CreateDeclarationRequest(
                fileId = file.id,
                name = declarationName,
                kind = request.kind,
            ),
        )
        when (request.kind) {
            DeclarationKind.DATA_CLASS -> {
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "id", "String"))
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "name", "String", defaultValue = "\"\""))
            }

            DeclarationKind.ENUM_CLASS -> {
                declarationService.createEnumEntry(CreateEnumEntryRequest(declaration.id, "DEFAULT"))
                declarationService.createEnumEntry(CreateEnumEntryRequest(declaration.id, "DISABLED"))
            }

            DeclarationKind.INTERFACE -> {
                declarationService.createFunctionStub(
                    CreateFunctionStubRequest(
                        declarationId = declaration.id,
                        name = "load",
                        returnType = declaration.name,
                        parameters = listOf(FunctionParameterDto("id", "String")),
                        bodyMode = FunctionBodyMode.TEMPLATE,
                    ),
                )
            }

            DeclarationKind.OBJECT -> {
                declarationService.createProperty(
                    CreatePropertyRequest(
                        declarationId = declaration.id,
                        name = "version",
                        type = "String",
                        initializer = "\"1.0.0\"",
                    ),
                )
            }

            DeclarationKind.ANNOTATION_CLASS -> {
                declarationService.createConstructorParam(CreateConstructorParamRequest(declaration.id, "value", "String", defaultValue = "\"\""))
            }
        }
        return aggregate(file.id)
    }

    override suspend fun list(search: CodegenSearchRequest): List<SourceFileMetaDto> {
        return support.listFiles(search.targetId)
            .filter {
                search.matches(
                    projectId = it.projectId,
                    targetId = it.targetId,
                    fileId = it.id,
                    values = listOf(it.packageName, it.fileName, it.docComment),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): SourceFileMetaDto = support.fileOrThrow(id).toDto()

    override suspend fun aggregate(id: String): SourceFileAggregateDto = support.buildFileAggregate(id)

    override suspend fun update(id: String, request: UpdateSourceFileRequest): SourceFileMetaDto {
        val existing = support.fileOrThrow(id)
        val normalizedFileName = serviceSupport.requireFileName(request.fileName)
        serviceSupport.requirePackageName(request.packageName, "文件包名")
        if (support.listFiles(existing.targetId).any { it.id != id && it.packageName == request.packageName && it.fileName == normalizedFileName }) {
            throw PlaygroundValidationException("同一目标下包名和文件名组合不能重复")
        }
        val updated = support.inTransaction {
            val entity = new(SourceFileMeta::class).by {
                this.id = id
                project = support.projectRef(existing.projectId)
                target = support.targetRef(existing.targetId)
                packageName = request.packageName.trim()
                fileName = normalizedFileName
                docComment = request.docComment?.trim()?.ifBlank { null }
                orderIndex = existing.orderIndex
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity
        }
        support.listDeclarations(id).forEach { declaration ->
            val entity = new(site.addzero.coding.playground.server.entity.DeclarationMeta::class).by {
                this.id = declaration.id
                this.file = support.fileRef(id)
                targetId = existing.targetId
                packageName = request.packageName.trim()
                fqName = serviceSupport.buildFqName(request.packageName.trim(), declaration.name)
                name = declaration.name
                kind = declaration.kind
                visibility = declaration.visibility
                modifiersJson = declaration.modifiersJson
                superTypesJson = declaration.superTypesJson
                docComment = declaration.docComment
                orderIndex = declaration.orderIndex
                createdAt = declaration.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        return updated.toDto()
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        val aggregate = support.buildFileAggregate(id)
        return DeleteCheckResultDto(
            id = id,
            kind = "file",
            canDelete = true,
            warnings = listOf("删除文件 ${aggregate.file.fileName} 将清理 ${aggregate.declarations.size} 个声明和相关同步记录"),
        )
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val aggregate = support.buildFileAggregate(id)
        val issues = mutableListOf<ValidationIssueDto>()
        if (aggregate.declarations.isEmpty()) {
            issues += serviceSupport.buildValidationIssue("file", id, ValidationSeverity.WARNING, "文件里还没有声明")
        }
        if (aggregate.imports.map { it.importPath }.distinct().size != aggregate.imports.size) {
            issues += serviceSupport.buildValidationIssue("file", id, ValidationSeverity.ERROR, "文件存在重复导包")
        }
        return issues
    }

    override suspend fun delete(id: String) {
        support.fileOrThrow(id)
        support.inTransaction {
            support.deleteFileCascade(id)
        }
    }

    override suspend fun createImport(request: CreateImportRequest): ImportMetaDto {
        support.fileOrThrow(request.fileId)
        serviceSupport.requireText(request.importPath, "导包路径")
        val existing = support.listImports(request.fileId)
        val now = support.now()
        val entity = new(ImportMeta::class).by {
            id = support.newId()
            file = support.fileRef(request.fileId)
            importPath = request.importPath.trim()
            alias = request.alias?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateImport(id: String, request: UpdateImportRequest): ImportMetaDto {
        val existing = support.importOrThrow(id)
        serviceSupport.requireText(request.importPath, "导包路径")
        val entity = new(ImportMeta::class).by {
            this.id = id
            file = support.fileRef(existing.fileId)
            importPath = request.importPath.trim()
            alias = request.alias?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteImport(id: String) {
        support.importOrThrow(id)
        support.sqlClient.deleteById(ImportMeta::class, id)
    }

    override suspend fun reorderImports(fileId: String, request: ReorderRequestDto): List<ImportMetaDto> {
        val items = support.listImports(fileId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("导包排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(ImportMeta::class).by {
                this.id = id
                file = support.fileRef(fileId)
                importPath = existing.importPath
                alias = existing.alias
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }
}
