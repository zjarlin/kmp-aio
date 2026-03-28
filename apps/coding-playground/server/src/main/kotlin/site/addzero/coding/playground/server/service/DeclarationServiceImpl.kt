package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundNotFoundException
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.DeclarationService

@Single
class DeclarationServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val serviceSupport: CodegenServiceSupport,
) : DeclarationService {
    override suspend fun create(request: CreateDeclarationRequest): DeclarationMetaDto {
        val file = support.fileOrThrow(request.fileId)
        serviceSupport.requireIdentifier(request.name, "声明名称")
        if (support.listDeclarations(request.fileId).any { it.name == request.name }) {
            throw PlaygroundValidationException("同一文件中声明名称不能重复: ${request.name}")
        }
        val fqName = serviceSupport.buildFqName(file.packageName, request.name)
        if (support.listDeclarations().any { it.targetId == file.targetId && it.fqName == fqName }) {
            throw PlaygroundValidationException("同一目标中全限定名不能重复: $fqName")
        }
        val now = support.now()
        val entity = new(DeclarationMeta::class).by {
            id = support.newId()
            this.file = support.fileRef(file.id)
            targetId = file.targetId
            packageName = file.packageName
            this.fqName = fqName
            name = request.name.trim()
            kind = request.kind.name
            visibility = request.visibility.name
            modifiersJson = encodeStringList(request.modifiers)
            superTypesJson = encodeStringList(request.superTypes)
            docComment = request.docComment?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(support.listDeclarations(file.id).map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: CodegenSearchRequest): List<DeclarationMetaDto> {
        return support.listDeclarations(search.fileId)
            .filter {
                search.matches(
                    targetId = it.targetId,
                    fileId = it.fileId,
                    declarationKind = enumValueOf<DeclarationKind>(it.kind),
                    values = listOf(it.name, it.fqName, it.docComment),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): DeclarationMetaDto = support.declarationOrThrow(id).toDto()

    override suspend fun update(id: String, request: UpdateDeclarationRequest): DeclarationMetaDto {
        val existing = support.declarationOrThrow(id)
        serviceSupport.requireIdentifier(request.name, "声明名称")
        val fqName = serviceSupport.buildFqName(existing.packageName, request.name)
        if (support.listDeclarations(existing.fileId).any { it.id != id && it.name == request.name }) {
            throw PlaygroundValidationException("同一文件中声明名称不能重复: ${request.name}")
        }
        if (support.listDeclarations().any { it.id != id && it.targetId == existing.targetId && it.fqName == fqName }) {
            throw PlaygroundValidationException("同一目标中全限定名不能重复: $fqName")
        }
            val entity = new(DeclarationMeta::class).by {
                this.id = id
                this.file = support.fileRef(existing.fileId)
                targetId = existing.targetId
                packageName = existing.packageName
                this.fqName = fqName
            name = request.name.trim()
            kind = existing.kind
            visibility = request.visibility.name
            modifiersJson = encodeStringList(request.modifiers)
            superTypesJson = encodeStringList(request.superTypes)
            docComment = request.docComment?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): DeleteCheckResultDto {
        val declaration = support.declarationOrThrow(id)
        return DeleteCheckResultDto(
            id = id,
            kind = "declaration",
            canDelete = true,
            warnings = listOf("删除声明 ${declaration.name} 会一并删除其构造参数、属性、函数和注解"),
        )
    }

    override suspend fun validate(id: String): List<ValidationIssueDto> {
        val declaration = support.declarationOrThrow(id).toDto()
        val issues = mutableListOf<ValidationIssueDto>()
        if (declaration.kind == DeclarationKind.DATA_CLASS && support.listConstructorParams(id).isEmpty()) {
            issues += serviceSupport.buildValidationIssue("declaration", id, ValidationSeverity.WARNING, "data class 没有主构造参数")
        }
        if (declaration.kind == DeclarationKind.CLASS && support.listFunctionStubs(id).isEmpty() && support.listProperties(id).isEmpty()) {
            issues += serviceSupport.buildValidationIssue("declaration", id, ValidationSeverity.INFO, "普通类还没有属性或函数")
        }
        if (declaration.kind == DeclarationKind.ENUM_CLASS && support.listEnumEntries(id).isEmpty()) {
            issues += serviceSupport.buildValidationIssue("declaration", id, ValidationSeverity.WARNING, "枚举类还没有枚举项")
        }
        if (declaration.kind == DeclarationKind.ANNOTATION_CLASS && support.listFunctionStubs(id).isNotEmpty()) {
            issues += serviceSupport.buildValidationIssue("declaration", id, ValidationSeverity.ERROR, "注解类不支持函数桩")
        }
        return issues
    }

    override suspend fun delete(id: String) {
        support.declarationOrThrow(id)
        support.inTransaction {
            support.deleteDeclarationCascade(id)
        }
    }

    override suspend fun reorderDeclarations(fileId: String, request: ReorderRequestDto): List<DeclarationMetaDto> {
        val items = support.listDeclarations(fileId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("声明排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(DeclarationMeta::class).by {
                this.id = id
                this.file = support.fileRef(fileId)
                targetId = existing.targetId
                packageName = existing.packageName
                fqName = existing.fqName
                name = existing.name
                kind = existing.kind
                visibility = existing.visibility
                modifiersJson = existing.modifiersJson
                superTypesJson = existing.superTypesJson
                docComment = existing.docComment
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    override suspend fun createConstructorParam(request: CreateConstructorParamRequest): ConstructorParamMetaDto {
        val declaration = support.declarationOrThrow(request.declarationId).toDto()
        if (declaration.kind !in setOf(DeclarationKind.DATA_CLASS, DeclarationKind.CLASS, DeclarationKind.ANNOTATION_CLASS)) {
            throw PlaygroundValidationException("只有 data class、class 和 annotation class 支持主构造参数")
        }
        serviceSupport.requireIdentifier(request.name, "参数名称")
        serviceSupport.requireText(request.type, "参数类型")
        val existing = support.listConstructorParams(request.declarationId)
        val now = support.now()
        val entity = new(ConstructorParamMeta::class).by {
            id = support.newId()
            this.declaration = support.declarationRef(request.declarationId)
            name = request.name.trim()
            type = request.type.trim()
            mutable = request.mutable
            nullable = request.nullable
            defaultValue = request.defaultValue?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateConstructorParam(id: String, request: UpdateConstructorParamRequest): ConstructorParamMetaDto {
        val existing = support.constructorParamOrThrow(id)
        serviceSupport.requireIdentifier(request.name, "参数名称")
        serviceSupport.requireText(request.type, "参数类型")
        val entity = new(ConstructorParamMeta::class).by {
            this.id = id
            declaration = support.declarationRef(existing.declarationId)
            name = request.name.trim()
            type = request.type.trim()
            mutable = request.mutable
            nullable = request.nullable
            defaultValue = request.defaultValue?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteConstructorParam(id: String) {
        support.constructorParamOrThrow(id)
        support.sqlClient.deleteById(ConstructorParamMeta::class, id)
    }

    override suspend fun reorderConstructorParams(declarationId: String, request: ReorderRequestDto): List<ConstructorParamMetaDto> {
        val items = support.listConstructorParams(declarationId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("构造参数排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(ConstructorParamMeta::class).by {
                this.id = id
                declaration = support.declarationRef(declarationId)
                name = existing.name
                type = existing.type
                mutable = existing.mutable
                nullable = existing.nullable
                defaultValue = existing.defaultValue
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    override suspend fun createProperty(request: CreatePropertyRequest): PropertyMetaDto {
        val declaration = support.declarationOrThrow(request.declarationId).toDto()
        serviceSupport.ensureDeclarationChildrenAllowed(declaration.kind, "property")
        serviceSupport.requireIdentifier(request.name, "属性名称")
        serviceSupport.requireText(request.type, "属性类型")
        val existing = support.listProperties(request.declarationId)
        val now = support.now()
            val entity = new(PropertyMeta::class).by {
                id = support.newId()
                this.declaration = support.declarationRef(request.declarationId)
                name = request.name.trim()
                type = request.type.trim()
                mutable = request.mutable
            nullable = request.nullable
            initializer = request.initializer?.trim()?.ifBlank { null }
            visibility = request.visibility.name
            isOverride = request.isOverride
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateProperty(id: String, request: UpdatePropertyRequest): PropertyMetaDto {
        val existing = support.propertyOrThrow(id)
        serviceSupport.requireIdentifier(request.name, "属性名称")
        serviceSupport.requireText(request.type, "属性类型")
            val entity = new(PropertyMeta::class).by {
                this.id = id
                this.declaration = support.declarationRef(existing.declarationId)
                name = request.name.trim()
                type = request.type.trim()
                mutable = request.mutable
            nullable = request.nullable
            initializer = request.initializer?.trim()?.ifBlank { null }
            visibility = request.visibility.name
            isOverride = request.isOverride
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteProperty(id: String) {
        support.propertyOrThrow(id)
        support.sqlClient.deleteById(PropertyMeta::class, id)
    }

    override suspend fun reorderProperties(declarationId: String, request: ReorderRequestDto): List<PropertyMetaDto> {
        val items = support.listProperties(declarationId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("属性排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(PropertyMeta::class).by {
                this.id = id
                this.declaration = support.declarationRef(declarationId)
                name = existing.name
                type = existing.type
                mutable = existing.mutable
                nullable = existing.nullable
                initializer = existing.initializer
                visibility = existing.visibility
                isOverride = existing.isOverride
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    override suspend fun createEnumEntry(request: CreateEnumEntryRequest): EnumEntryMetaDto {
        val declaration = support.declarationOrThrow(request.declarationId).toDto()
        serviceSupport.ensureDeclarationChildrenAllowed(declaration.kind, "enum-entry")
        serviceSupport.requireIdentifier(request.name, "枚举项名称")
        val existing = support.listEnumEntries(request.declarationId)
        val now = support.now()
        val entity = new(EnumEntryMeta::class).by {
            id = support.newId()
            this.declaration = support.declarationRef(request.declarationId)
            name = request.name.trim()
            argumentsJson = encodeStringList(request.arguments)
            bodyText = request.bodyText?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateEnumEntry(id: String, request: UpdateEnumEntryRequest): EnumEntryMetaDto {
        val existing = support.enumEntryOrThrow(id)
        serviceSupport.requireIdentifier(request.name, "枚举项名称")
        val entity = new(EnumEntryMeta::class).by {
            this.id = id
            declaration = support.declarationRef(existing.declarationId)
            name = request.name.trim()
            argumentsJson = encodeStringList(request.arguments)
            bodyText = request.bodyText?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteEnumEntry(id: String) {
        support.enumEntryOrThrow(id)
        support.sqlClient.deleteById(EnumEntryMeta::class, id)
    }

    override suspend fun reorderEnumEntries(declarationId: String, request: ReorderRequestDto): List<EnumEntryMetaDto> {
        val items = support.listEnumEntries(declarationId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("枚举项排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(EnumEntryMeta::class).by {
                this.id = id
                declaration = support.declarationRef(declarationId)
                name = existing.name
                argumentsJson = existing.argumentsJson
                bodyText = existing.bodyText
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    override suspend fun createAnnotationUsage(request: CreateAnnotationUsageRequest): AnnotationUsageMetaDto {
        val ownerExists = when (request.ownerType) {
            AnnotationOwnerType.FILE -> runCatching { support.fileOrThrow(request.ownerId) }.isSuccess
            AnnotationOwnerType.DECLARATION -> runCatching { support.declarationOrThrow(request.ownerId) }.isSuccess
            AnnotationOwnerType.CONSTRUCTOR_PARAM -> runCatching { support.constructorParamOrThrow(request.ownerId) }.isSuccess
            AnnotationOwnerType.PROPERTY -> runCatching { support.propertyOrThrow(request.ownerId) }.isSuccess
            AnnotationOwnerType.FUNCTION -> runCatching { support.functionStubOrThrow(request.ownerId) }.isSuccess
        }
        if (!ownerExists) {
            throw PlaygroundNotFoundException("注解挂载目标不存在: ${request.ownerType} / ${request.ownerId}")
        }
        serviceSupport.requireText(request.annotationClassName, "注解类名")
        val existing = support.listAnnotations(request.ownerId)
        val now = support.now()
        val entity = new(AnnotationUsageMeta::class).by {
            id = support.newId()
            ownerType = request.ownerType.name
            ownerId = request.ownerId
            annotationClassName = request.annotationClassName.trim()
            useSiteTarget = request.useSiteTarget?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateAnnotationUsage(id: String, request: UpdateAnnotationUsageRequest): AnnotationUsageMetaDto {
        val existing = support.annotationUsageOrThrow(id)
        serviceSupport.requireText(request.annotationClassName, "注解类名")
        val entity = new(AnnotationUsageMeta::class).by {
            this.id = id
            ownerType = existing.ownerType
            ownerId = existing.ownerId
            annotationClassName = request.annotationClassName.trim()
            useSiteTarget = request.useSiteTarget?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteAnnotationUsage(id: String) {
        support.annotationUsageOrThrow(id)
        support.listAnnotationArguments(id).forEach { support.sqlClient.deleteById(AnnotationArgumentMeta::class, it.id) }
        support.sqlClient.deleteById(AnnotationUsageMeta::class, id)
    }

    override suspend fun createAnnotationArgument(request: CreateAnnotationArgumentRequest): AnnotationArgumentMetaDto {
        support.annotationUsageOrThrow(request.annotationUsageId)
        serviceSupport.requireText(request.value, "注解参数值")
        val existing = support.listAnnotationArguments(request.annotationUsageId)
        val now = support.now()
        val entity = new(AnnotationArgumentMeta::class).by {
            id = support.newId()
            annotationUsage = support.annotationUsageRef(request.annotationUsageId)
            name = request.name?.trim()?.ifBlank { null }
            value = request.value.trim()
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateAnnotationArgument(id: String, request: UpdateAnnotationArgumentRequest): AnnotationArgumentMetaDto {
        val existing = support.annotationArgumentOrThrow(id)
        serviceSupport.requireText(request.value, "注解参数值")
        val entity = new(AnnotationArgumentMeta::class).by {
            this.id = id
            annotationUsage = support.annotationUsageRef(existing.annotationUsageId)
            name = request.name?.trim()?.ifBlank { null }
            value = request.value.trim()
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteAnnotationArgument(id: String) {
        support.annotationArgumentOrThrow(id)
        support.sqlClient.deleteById(AnnotationArgumentMeta::class, id)
    }

    override suspend fun reorderAnnotationArguments(annotationUsageId: String, request: ReorderRequestDto): List<AnnotationArgumentMetaDto> {
        val items = support.listAnnotationArguments(annotationUsageId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("注解参数排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(AnnotationArgumentMeta::class).by {
                this.id = id
                annotationUsage = support.annotationUsageRef(annotationUsageId)
                name = existing.name
                value = existing.value
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }

    override suspend fun createFunctionStub(request: CreateFunctionStubRequest): FunctionStubMetaDto {
        val declaration = support.declarationOrThrow(request.declarationId).toDto()
        serviceSupport.ensureDeclarationChildrenAllowed(declaration.kind, "function")
        serviceSupport.requireIdentifier(request.name, "函数名称")
        serviceSupport.requireText(request.returnType, "返回类型")
        val existing = support.listFunctionStubs(request.declarationId)
        val now = support.now()
            val entity = new(FunctionStubMeta::class).by {
                id = support.newId()
                this.declaration = support.declarationRef(request.declarationId)
                name = request.name.trim()
                returnType = request.returnType.trim()
                visibility = request.visibility.name
            modifiersJson = encodeStringList(request.modifiers)
            parametersJson = encodeFunctionParameters(request.parameters)
            bodyMode = request.bodyMode.name
            bodyText = request.bodyText?.trim()?.ifBlank { null }
            orderIndex = serviceSupport.nextOrder(existing.map { it.orderIndex })
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun updateFunctionStub(id: String, request: UpdateFunctionStubRequest): FunctionStubMetaDto {
        val existing = support.functionStubOrThrow(id)
        serviceSupport.requireIdentifier(request.name, "函数名称")
        serviceSupport.requireText(request.returnType, "返回类型")
            val entity = new(FunctionStubMeta::class).by {
                this.id = id
                this.declaration = support.declarationRef(existing.declarationId)
                name = request.name.trim()
                returnType = request.returnType.trim()
                visibility = request.visibility.name
            modifiersJson = encodeStringList(request.modifiers)
            parametersJson = encodeFunctionParameters(request.parameters)
            bodyMode = request.bodyMode.name
            bodyText = request.bodyText?.trim()?.ifBlank { null }
            orderIndex = existing.orderIndex
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteFunctionStub(id: String) {
        support.functionStubOrThrow(id)
        support.sqlClient.deleteById(FunctionStubMeta::class, id)
    }

    override suspend fun reorderFunctionStubs(declarationId: String, request: ReorderRequestDto): List<FunctionStubMetaDto> {
        val items = support.listFunctionStubs(declarationId)
        if (items.map { it.id }.toSet() != request.orderedIds.toSet()) {
            throw PlaygroundValidationException("函数排序列表不完整")
        }
        return request.orderedIds.mapIndexed { index, id ->
            val existing = items.first { it.id == id }
            val entity = new(FunctionStubMeta::class).by {
                this.id = id
                this.declaration = support.declarationRef(declarationId)
                name = existing.name
                returnType = existing.returnType
                visibility = existing.visibility
                modifiersJson = existing.modifiersJson
                parametersJson = existing.parametersJson
                bodyMode = existing.bodyMode
                bodyText = existing.bodyText
                orderIndex = index
                createdAt = existing.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity).modifiedEntity.toDto()
        }
    }
}
