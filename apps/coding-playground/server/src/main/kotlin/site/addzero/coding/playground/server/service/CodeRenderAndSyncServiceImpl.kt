package site.addzero.coding.playground.server.service

import kotlinx.serialization.json.Json
import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundConflictException
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.*
import site.addzero.coding.playground.server.util.sha256
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.CodeRenderService
import site.addzero.coding.playground.shared.service.KspIndexService
import site.addzero.coding.playground.shared.service.ManagedArtifactService
import site.addzero.coding.playground.shared.service.SyncService
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private const val managedAnnotationImport = "site.addzero.coding.playground.annotations.GeneratedManagedDeclaration"
private const val generatedIndexPackage = "site.addzero.coding.playground.annotations"

@Single
class CodeRenderAndSyncServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val pathResolver: CodegenPathResolver,
    private val json: Json,
) : CodeRenderService, ManagedArtifactService, SyncService, KspIndexService {
    override suspend fun previewFile(fileId: String): CodeRenderPreviewDto {
        val aggregate = support.buildFileAggregate(fileId)
        val target = support.targetOrThrow(aggregate.file.targetId).toDto()
        val metadataHash = json.encodeToString(aggregate.toManagedMetadataSnapshot()).sha256()
        val body = renderFile(aggregate)
        val contentHash = body.sha256()
        val marker = buildMarker(aggregate.file.projectId, target.id, fileId, aggregate.declarations.map { it.id }, metadataHash, contentHash)
        val outputPath = pathResolver.resolveFilePath(target, aggregate.file).toString()
        return CodeRenderPreviewDto(
            file = aggregate.file,
            outputPath = outputPath,
            declarationIds = aggregate.declarations.map { it.id },
            metadataHash = metadataHash,
            contentHash = contentHash,
            content = marker + "\n\n" + body,
        )
    }

    override suspend fun list(search: CodegenSearchRequest): List<ManagedArtifactMetaDto> {
        return support.listArtifacts(search.targetId, search.fileId)
            .filter {
                search.matches(
                    projectId = it.projectId,
                    targetId = it.targetId,
                    fileId = it.fileId,
                    values = listOf(it.absolutePath, it.syncStatus),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun get(id: String): ManagedArtifactMetaDto = support.artifactOrThrow(id).toDto()

    override suspend fun export(request: SyncExportRequest): SyncExportResultDto {
        val fileIds: List<String> = when {
            request.fileId != null -> listOf(requireNotNull(request.fileId))
            request.targetId != null -> support.listFiles(request.targetId).map { it.id }
            else -> support.listFiles().map { it.id }
        }
        val previews = mutableListOf<CodeRenderPreviewDto>()
        val artifacts = mutableListOf<ManagedArtifactMetaDto>()
        val conflicts = mutableListOf<SyncConflictMetaDto>()
        val messages = mutableListOf<String>()
        fileIds.forEach { fileId ->
            val preview = previewFile(fileId)
            previews += preview
            when (val result = exportOne(preview, request.force)) {
                is ExportWriteResult.Artifact -> {
                    artifacts += result.artifact.toDto()
                    messages += "已写入 ${preview.file.fileName}"
                }

                is ExportWriteResult.Conflict -> {
                    conflicts += result.conflict.toDto()
                    messages += result.conflict.message
                }
            }
        }
        return SyncExportResultDto(
            previews = previews,
            artifacts = artifacts,
            conflicts = conflicts,
            messages = messages,
        )
    }

    override suspend fun importSource(request: SyncImportRequest): SyncImportResultDto {
        val artifacts = resolveArtifactsForImport(request)
        val updatedFiles = mutableListOf<SourceFileAggregateDto>()
        val conflicts = mutableListOf<SyncConflictMetaDto>()
        val messages = mutableListOf<String>()
        artifacts.forEach { artifact ->
            when (val result = importOne(artifact, force = false)) {
                is ImportWriteResult.Success -> {
                    updatedFiles += result.file
                    messages += "已导回 ${result.file.file.fileName}"
                }

                is ImportWriteResult.Conflict -> {
                    conflicts += result.conflict.toDto()
                    messages += result.conflict.message
                }
            }
        }
        return SyncImportResultDto(
            files = updatedFiles,
            conflicts = conflicts,
            messages = messages,
        )
    }

    override suspend fun listConflicts(search: CodegenSearchRequest): List<SyncConflictMetaDto> {
        return support.listConflicts(search.targetId, search.fileId)
            .filter {
                search.matches(
                    projectId = it.projectId,
                    targetId = it.targetId,
                    fileId = it.fileId,
                    values = listOf(it.message, it.reason),
                )
            }
            .map { it.toDto() }
    }

    override suspend fun resolveConflict(id: String, request: ResolveSyncConflictRequest): SyncConflictMetaDto {
        val conflict = support.conflictOrThrow(id)
        when (request.resolution) {
            SyncConflictResolution.METADATA_WINS -> {
                export(SyncExportRequest(fileId = conflict.fileId, force = true))
            }

            SyncConflictResolution.SOURCE_WINS -> {
                val artifactId = conflict.artifactId
                val artifact = artifactId?.let { support.artifactOrThrow(it) }
                    ?: throw PlaygroundConflictException("当前冲突缺少托管产物记录，无法按源码为准")
                when (val result = importOne(artifact, force = true)) {
                    is ImportWriteResult.Success -> Unit
                    is ImportWriteResult.Conflict -> throw PlaygroundConflictException(result.conflict.message)
                }
            }
        }
        val entity = new(SyncConflictMeta::class).by {
            this.id = conflict.id
            project = support.projectRef(conflict.projectId)
            target = support.targetRef(conflict.targetId)
            file = support.fileRef(conflict.fileId)
            artifact = conflict.artifactId?.let { support.artifactRef(it) }
            reason = conflict.reason
            message = conflict.message
            metadataHash = conflict.metadataHash
            sourceHash = conflict.sourceHash
            sourcePath = conflict.sourcePath
            resolved = true
            resolution = request.resolution.name
            createdAt = conflict.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun previewIndex(targetId: String): KspIndexPreviewDto {
        val target = support.targetOrThrow(targetId).toDto()
        val files = support.listFiles(targetId).map { it.toDto() }
        val declarations = support.listDeclarations().filter { it.targetId == targetId }.map { it.toDto() }
        val content = buildGeneratedCodeIndex(target, files, declarations)
        return KspIndexPreviewDto(
            targetId = targetId,
            packageName = target.indexPackage,
            fileCount = files.size,
            declarationCount = declarations.size,
            content = content,
        )
    }

    private fun resolveArtifactsForImport(request: SyncImportRequest): List<ManagedArtifactMeta> {
        return when {
            request.artifactId != null -> listOf(support.artifactOrThrow(requireNotNull(request.artifactId)))
            request.fileId != null -> support.listArtifacts(fileId = request.fileId).takeIf { it.isNotEmpty() }
                ?: throw PlaygroundValidationException("文件还没有托管产物记录")
            request.absolutePath != null -> {
                val path = Paths.get(request.absolutePath).toAbsolutePath().normalize().toString()
                support.listArtifacts().filter { it.absolutePath == path }.takeIf { it.isNotEmpty() }
                    ?: throw PlaygroundValidationException("找不到对应的托管产物: $path")
            }

            else -> throw PlaygroundValidationException("导回时必须提供 fileId、artifactId 或 absolutePath")
        }
    }

    private suspend fun exportOne(preview: CodeRenderPreviewDto, force: Boolean): ExportWriteResult {
        val path = Paths.get(preview.outputPath)
        val existingText = path.takeIf { it.exists() }?.readText()
        val existingHeader = existingText?.let(::parseMarker)
        if (existingText != null && existingHeader == null) {
            return ExportWriteResult.Conflict(
                createConflict(
                    file = preview.file,
                    artifact = support.listArtifacts(fileId = preview.file.id).firstOrNull(),
                    reason = ConflictReason.FILE_NOT_MANAGED,
                    message = "目标文件不是工作台托管文件，已阻止覆盖: ${preview.outputPath}",
                    metadataHash = preview.metadataHash,
                    sourceHash = existingText.sha256(),
                    sourcePath = preview.outputPath,
                ),
            )
        }
        val artifact = support.listArtifacts(fileId = preview.file.id).firstOrNull()
        val currentDiskHash = existingText?.sha256()
        val metadataChanged = artifact?.metadataHash != null && artifact.metadataHash != preview.metadataHash
        val sourceChanged = artifact?.sourceHash != null && currentDiskHash != null && artifact.sourceHash != currentDiskHash
        if (!force && artifact != null && metadataChanged && sourceChanged) {
            return ExportWriteResult.Conflict(
                createConflict(
                    file = preview.file,
                    artifact = artifact,
                    reason = ConflictReason.BOTH_CHANGED,
                    message = "元数据和源码都已变化，已阻止直接覆盖: ${preview.file.fileName}",
                    metadataHash = preview.metadataHash,
                    sourceHash = currentDiskHash,
                    sourcePath = preview.outputPath,
                ),
            )
        }
        path.parent?.createDirectories()
        path.writeText(preview.content)
        val saved = upsertArtifact(preview, ManagedArtifactSyncStatus.CLEAN, preview.content.sha256(), artifact)
        return ExportWriteResult.Artifact(saved)
    }

    private suspend fun importOne(artifact: ManagedArtifactMeta, force: Boolean): ImportWriteResult {
        val path = Paths.get(artifact.absolutePath)
        if (!path.exists()) {
            return ImportWriteResult.Conflict(
                createConflict(
                    file = support.fileOrThrow(artifact.fileId).toDto(),
                    artifact = artifact,
                    reason = ConflictReason.FILE_NOT_MANAGED,
                    message = "托管文件不存在: ${artifact.absolutePath}",
                    metadataHash = artifact.metadataHash,
                    sourceHash = null,
                    sourcePath = artifact.absolutePath,
                ),
            )
        }
        val text = path.readText()
        val marker = parseMarker(text) ?: return ImportWriteResult.Conflict(
            createConflict(
                file = support.fileOrThrow(artifact.fileId).toDto(),
                artifact = artifact,
                reason = ConflictReason.FILE_NOT_MANAGED,
                message = "文件缺少托管 marker，无法导回: ${artifact.absolutePath}",
                metadataHash = artifact.metadataHash,
                sourceHash = text.sha256(),
                sourcePath = artifact.absolutePath,
            ),
        )
        if (marker.fileId != artifact.fileId) {
            return ImportWriteResult.Conflict(
                createConflict(
                    file = support.fileOrThrow(artifact.fileId).toDto(),
                    artifact = artifact,
                    reason = ConflictReason.MARKER_MISMATCH,
                    message = "文件 marker 与数据库记录不一致，无法导回",
                    metadataHash = artifact.metadataHash,
                    sourceHash = text.sha256(),
                    sourcePath = artifact.absolutePath,
                ),
            )
        }
        val latestPreview = previewFile(artifact.fileId)
        val metadataChanged = artifact.metadataHash != latestPreview.metadataHash
        val sourceChanged = artifact.sourceHash != text.sha256()
        if (!force && metadataChanged && sourceChanged) {
            return ImportWriteResult.Conflict(
                createConflict(
                    file = latestPreview.file,
                    artifact = artifact,
                    reason = ConflictReason.BOTH_CHANGED,
                    message = "元数据和源码都已变化，必须先选择冲突解决策略",
                    metadataHash = latestPreview.metadataHash,
                    sourceHash = text.sha256(),
                    sourcePath = artifact.absolutePath,
                ),
            )
        }
        val parsed = runCatching { parseManagedSource(text) }.getOrElse { error ->
            return ImportWriteResult.Conflict(
                createConflict(
                    file = latestPreview.file,
                    artifact = artifact,
                    reason = ConflictReason.PARSE_FAILED,
                    message = "源码导回失败: ${error.message}",
                    metadataHash = latestPreview.metadataHash,
                    sourceHash = text.sha256(),
                    sourcePath = artifact.absolutePath,
                ),
            )
        }
        val updated = applyParsedSource(artifact.fileId, parsed)
        val refreshedPreview = previewFile(updated.file.id)
        upsertArtifact(refreshedPreview, ManagedArtifactSyncStatus.CLEAN, text.sha256(), artifact)
        return ImportWriteResult.Success(updated)
    }

    private fun applyParsedSource(fileId: String, parsed: ParsedManagedSource): SourceFileAggregateDto {
        support.inTransaction {
            val file = support.fileOrThrow(fileId)
            val existingDeclarations = support.listDeclarations(fileId).associateBy { it.id }
            val fileEntity = new(SourceFileMeta::class).by {
                this.id = file.id
                project = support.projectRef(file.projectId)
                target = support.targetRef(file.targetId)
                packageName = parsed.packageName
                fileName = file.fileName
                docComment = file.docComment
                orderIndex = file.orderIndex
                createdAt = file.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(fileEntity)
            support.listImports(fileId).forEach { support.sqlClient.deleteById(ImportMeta::class, it.id) }
            support.listAnnotations(fileId).forEach { usage ->
                support.listAnnotationArguments(usage.id).forEach { support.sqlClient.deleteById(AnnotationArgumentMeta::class, it.id) }
                support.sqlClient.deleteById(AnnotationUsageMeta::class, usage.id)
            }
            support.listDeclarations(fileId).forEach { support.deleteDeclarationCascade(it.id) }

            parsed.imports.forEachIndexed { index, item ->
                val importEntity = new(ImportMeta::class).by {
                    id = support.newId()
                    this.file = support.fileRef(fileId)
                    importPath = item.importPath
                    alias = item.alias
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                support.sqlClient.save(importEntity)
            }

            parsed.fileAnnotations.forEachIndexed { index, annotation ->
                val usage = new(AnnotationUsageMeta::class).by {
                    id = support.newId()
                    ownerType = AnnotationOwnerType.FILE.name
                    ownerId = fileId
                    annotationClassName = annotation.name
                    useSiteTarget = "file"
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                val saved = support.sqlClient.save(usage).modifiedEntity
                annotation.arguments.forEachIndexed { argIndex, argument ->
                    val argEntity = new(AnnotationArgumentMeta::class).by {
                        id = support.newId()
                        annotationUsage = support.annotationUsageRef(saved.id)
                        name = argument.name
                        value = argument.value
                        orderIndex = argIndex
                        createdAt = support.now()
                        updatedAt = support.now()
                    }
                    support.sqlClient.save(argEntity)
                }
            }

            val declarationId = parsed.declaration.declarationId ?: support.newId()
            val existingDeclaration = existingDeclarations[declarationId]
            val declarationEntity = new(DeclarationMeta::class).by {
                id = declarationId
                this.file = support.fileRef(fileId)
                targetId = file.targetId
                packageName = parsed.packageName
                fqName = serviceFqName(parsed.packageName, parsed.declaration.name)
                name = parsed.declaration.name
                kind = parsed.declaration.kind.name
                visibility = parsed.declaration.visibility.name
                modifiersJson = encodeStringList(parsed.declaration.modifiers)
                superTypesJson = encodeStringList(parsed.declaration.superTypes)
                docComment = existingDeclaration?.docComment
                orderIndex = existingDeclaration?.orderIndex ?: 0
                createdAt = existingDeclaration?.createdAt ?: support.now()
                updatedAt = support.now()
            }
            support.sqlClient.save(declarationEntity)

            parsed.declaration.annotations.forEachIndexed { index, annotation ->
                val usage = new(AnnotationUsageMeta::class).by {
                    id = support.newId()
                    ownerType = AnnotationOwnerType.DECLARATION.name
                    ownerId = declarationId
                    annotationClassName = annotation.name
                    useSiteTarget = annotation.useSiteTarget
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                val saved = support.sqlClient.save(usage).modifiedEntity
                annotation.arguments.forEachIndexed { argIndex, argument ->
                    val argEntity = new(AnnotationArgumentMeta::class).by {
                        id = support.newId()
                        annotationUsage = support.annotationUsageRef(saved.id)
                        name = argument.name
                        value = argument.value
                        orderIndex = argIndex
                        createdAt = support.now()
                        updatedAt = support.now()
                    }
                    support.sqlClient.save(argEntity)
                }
            }

            parsed.declaration.constructorParams.forEachIndexed { index, param ->
                val paramEntity = new(ConstructorParamMeta::class).by {
                    id = support.newId()
                    declaration = support.declarationRef(declarationId)
                    name = param.name
                    type = param.type
                    mutable = param.mutable
                    nullable = param.nullable
                    defaultValue = param.defaultValue
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                support.sqlClient.save(paramEntity)
            }

            parsed.declaration.properties.forEachIndexed { index, property ->
                val propertyEntity = new(PropertyMeta::class).by {
                    id = support.newId()
                    declaration = support.declarationRef(declarationId)
                    name = property.name
                    type = property.type
                    mutable = property.mutable
                    nullable = property.nullable
                    initializer = property.initializer
                    visibility = property.visibility.name
                    isOverride = property.isOverride
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                val saved = support.sqlClient.save(propertyEntity).modifiedEntity
                property.annotations.forEachIndexed { annIndex, annotation ->
                    saveOwnerAnnotation(saved.id, AnnotationOwnerType.PROPERTY, annIndex, annotation)
                }
            }

            parsed.declaration.enumEntries.forEachIndexed { index, entry ->
                val entryEntity = new(EnumEntryMeta::class).by {
                    id = support.newId()
                    declaration = support.declarationRef(declarationId)
                    name = entry.name
                    argumentsJson = encodeStringList(entry.arguments)
                    bodyText = entry.bodyText
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                support.sqlClient.save(entryEntity)
            }

            parsed.declaration.functions.forEachIndexed { index, function ->
                val functionEntity = new(FunctionStubMeta::class).by {
                    id = support.newId()
                    declaration = support.declarationRef(declarationId)
                    name = function.name
                    returnType = function.returnType
                    visibility = function.visibility.name
                    modifiersJson = encodeStringList(function.modifiers)
                    parametersJson = encodeFunctionParameters(function.parameters)
                    bodyMode = function.bodyMode.name
                    bodyText = function.bodyText
                    orderIndex = index
                    createdAt = support.now()
                    updatedAt = support.now()
                }
                val saved = support.sqlClient.save(functionEntity).modifiedEntity
                function.annotations.forEachIndexed { annIndex, annotation ->
                    saveOwnerAnnotation(saved.id, AnnotationOwnerType.FUNCTION, annIndex, annotation)
                }
            }
        }
        return support.buildFileAggregate(fileId)
    }

    private fun saveOwnerAnnotation(ownerId: String, ownerType: AnnotationOwnerType, index: Int, annotation: ParsedAnnotation) {
        val usage = new(AnnotationUsageMeta::class).by {
            id = support.newId()
            this.ownerType = ownerType.name
            this.ownerId = ownerId
            annotationClassName = annotation.name
            useSiteTarget = annotation.useSiteTarget
            orderIndex = index
            createdAt = support.now()
            updatedAt = support.now()
        }
        val saved = support.sqlClient.save(usage).modifiedEntity
        annotation.arguments.forEachIndexed { argIndex, argument ->
            val argEntity = new(AnnotationArgumentMeta::class).by {
                id = support.newId()
                annotationUsage = support.annotationUsageRef(saved.id)
                name = argument.name
                value = argument.value
                orderIndex = argIndex
                createdAt = support.now()
                updatedAt = support.now()
            }
            support.sqlClient.save(argEntity)
        }
    }

    private fun upsertArtifact(
        preview: CodeRenderPreviewDto,
        syncStatus: ManagedArtifactSyncStatus,
        sourceHash: String?,
        existing: ManagedArtifactMeta?,
    ): ManagedArtifactMeta {
        val createdAt = existing?.createdAt ?: support.now()
        val entity = new(ManagedArtifactMeta::class).by {
            id = existing?.id ?: support.newId()
            project = support.projectRef(preview.file.projectId)
            target = support.targetRef(preview.file.targetId)
            file = support.fileRef(preview.file.id)
            declarationIdsJson = encodeStringList(preview.declarationIds)
            absolutePath = preview.outputPath
            markerText = preview.content.lineSequence().takeWhile { it.isNotBlank() }.joinToString("\n")
            metadataHash = preview.metadataHash
            this.sourceHash = sourceHash
            contentHash = preview.contentHash
            this.syncStatus = syncStatus.name
            lastExportedAt = if (syncStatus == ManagedArtifactSyncStatus.CLEAN) support.now() else existing?.lastExportedAt
            lastImportedAt = existing?.lastImportedAt
            this.createdAt = createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity
    }

    private fun createConflict(
        file: SourceFileMetaDto,
        artifact: ManagedArtifactMeta?,
        reason: ConflictReason,
        message: String,
        metadataHash: String,
        sourceHash: String?,
        sourcePath: String?,
    ): SyncConflictMeta {
        support.listConflicts(fileId = file.id).filter { !it.resolved }.forEach {
            support.sqlClient.deleteById(SyncConflictMeta::class, it.id)
        }
        artifact?.let {
            val entity = new(ManagedArtifactMeta::class).by {
                id = it.id
                project = support.projectRef(it.projectId)
                target = support.targetRef(it.targetId)
                this.file = support.fileRef(it.fileId)
                declarationIdsJson = it.declarationIdsJson
                absolutePath = it.absolutePath
                markerText = it.markerText
                this.metadataHash = metadataHash
                this.sourceHash = sourceHash
                contentHash = it.contentHash
                syncStatus = ManagedArtifactSyncStatus.CONFLICT.name
                lastExportedAt = it.lastExportedAt
                lastImportedAt = it.lastImportedAt
                createdAt = it.createdAt
                updatedAt = support.now()
            }
            support.sqlClient.save(entity)
        }
        val entity = new(SyncConflictMeta::class).by {
            id = support.newId()
            project = support.projectRef(file.projectId)
            target = support.targetRef(file.targetId)
            this.file = support.fileRef(file.id)
            this.artifact = artifact?.let { support.artifactRef(it.id) }
            this.reason = reason.name
            this.message = message
            this.metadataHash = metadataHash
            this.sourceHash = sourceHash
            this.sourcePath = sourcePath
            resolved = false
            resolution = null
            createdAt = support.now()
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity
    }

    private fun buildGeneratedCodeIndex(
        target: GenerationTargetDto,
        files: List<SourceFileMetaDto>,
        declarations: List<DeclarationMetaDto>,
    ): String {
        val fileEntries = files.joinToString(",\n        ") { file ->
            "GeneratedCodeFileDescriptor(fileId = \"${file.id}\", targetId = \"${file.targetId}\", packageName = \"${file.packageName}\", fileName = \"${file.fileName}\", declarationIds = listOf(${declarations.filter { it.fileId == file.id }.joinToString(", ") { "\"${it.id}\"" }}))"
        }
        val declarationEntries = declarations.joinToString(",\n        ") { declaration ->
            "GeneratedCodeDeclarationDescriptor(declarationId = \"${declaration.id}\", fileId = \"${declaration.fileId}\", fqName = \"${declaration.fqName}\", kind = \"${declaration.kind.name}\", presetType = \"${declaration.kind.name}\")"
        }
        return buildString {
            appendLine("package ${target.indexPackage}")
            appendLine()
            appendLine("import $generatedIndexPackage.GeneratedCodeDeclarationDescriptor")
            appendLine("import $generatedIndexPackage.GeneratedCodeFileDescriptor")
            appendLine("import $generatedIndexPackage.GeneratedCodeIndexContract")
            appendLine()
            appendLine("object GeneratedCodeIndex : GeneratedCodeIndexContract {")
            appendLine("    private val files = listOf(")
            appendLine("        $fileEntries")
            appendLine("    )")
            appendLine()
            appendLine("    private val declarations = listOf(")
            appendLine("        $declarationEntries")
            appendLine("    )")
            appendLine()
            appendLine("    override fun files(): List<GeneratedCodeFileDescriptor> = files")
            appendLine()
            appendLine("    override fun declarations(): List<GeneratedCodeDeclarationDescriptor> = declarations")
            appendLine()
            appendLine("    override fun findByFqName(fqName: String): GeneratedCodeDeclarationDescriptor? = declarations.firstOrNull { it.fqName == fqName }")
            appendLine()
            appendLine("    override fun findByDeclarationId(declarationId: String): GeneratedCodeDeclarationDescriptor? = declarations.firstOrNull { it.declarationId == declarationId }")
            appendLine("}")
        }
    }

    private fun renderFile(aggregate: SourceFileAggregateDto): String {
        val annotationArgs = aggregate.annotationArguments.groupBy { it.annotationUsageId }
        val annotationsByOwner = aggregate.annotations.groupBy { it.ownerId }
        val constructorParamsByDeclaration = aggregate.constructorParams.groupBy { it.declarationId }
        val propertiesByDeclaration = aggregate.properties.groupBy { it.declarationId }
        val enumEntriesByDeclaration = aggregate.enumEntries.groupBy { it.declarationId }
        val functionsByDeclaration = aggregate.functionStubs.groupBy { it.declarationId }
        val declarationText = aggregate.declarations.joinToString("\n\n") { declaration ->
            renderDeclaration(
                declaration = declaration,
                constructorParams = constructorParamsByDeclaration[declaration.id].orEmpty(),
                properties = propertiesByDeclaration[declaration.id].orEmpty(),
                enumEntries = enumEntriesByDeclaration[declaration.id].orEmpty(),
                functions = functionsByDeclaration[declaration.id].orEmpty(),
                ownerAnnotations = annotationsByOwner,
                annotationArgs = annotationArgs,
            )
        }
        val userImports = aggregate.imports.map {
            if (it.alias.isNullOrBlank()) {
                "import ${it.importPath}"
            } else {
                "import ${it.importPath} as ${it.alias}"
            }
        }
        val imports = (userImports + "import $managedAnnotationImport").distinct().sorted()
        return buildString {
            renderFileAnnotations(aggregate.file.id, annotationsByOwner, annotationArgs)?.let {
                appendLine(it)
            }
            appendLine("package ${aggregate.file.packageName}")
            appendLine()
            imports.forEach { appendLine(it) }
            if (imports.isNotEmpty()) {
                appendLine()
            }
            appendLine(declarationText)
        }.trimEnd()
    }

    private fun renderFileAnnotations(
        fileId: String,
        annotationsByOwner: Map<String, List<AnnotationUsageMetaDto>>,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
    ): String? {
        val annotations = annotationsByOwner[fileId].orEmpty().filter { it.useSiteTarget == "file" }
        if (annotations.isEmpty()) {
            return null
        }
        return annotations.joinToString("\n") { annotation ->
            "@file:${annotation.annotationClassName}${renderAnnotationArgs(annotation.id, annotationArgs)}"
        }
    }

    private fun renderDeclaration(
        declaration: DeclarationMetaDto,
        constructorParams: List<ConstructorParamMetaDto>,
        properties: List<PropertyMetaDto>,
        enumEntries: List<EnumEntryMetaDto>,
        functions: List<FunctionStubMetaDto>,
        ownerAnnotations: Map<String, List<AnnotationUsageMetaDto>>,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
    ): String {
        val lines = mutableListOf<String>()
        renderDeclarationKDoc(declaration, constructorParams)?.let(lines::add)
        ownerAnnotations[declaration.id].orEmpty()
            .filterNot { it.annotationClassName == "GeneratedManagedDeclaration" }
            .forEach { annotation ->
                lines += "@${annotation.annotationClassName}${renderAnnotationArgs(annotation.id, annotationArgs)}"
            }
        lines += "@GeneratedManagedDeclaration(declarationId = \"${declaration.id}\", fileId = \"${declaration.fileId}\", presetType = \"${declaration.kind.name}\")"
        val prefix = buildPrefix(declaration.visibility, declaration.modifiers)
        val superTypeSuffix = declaration.superTypes.takeIf { it.isNotEmpty() }?.joinToString(", ", prefix = " : ") ?: ""
        when (declaration.kind) {
            DeclarationKind.DATA_CLASS -> {
                val constructorText = constructorParams.joinToString(",\n", prefix = "(\n", postfix = "\n)") {
                    "    ${renderConstructorParam(it, ownerAnnotations, annotationArgs)}"
                }
                val members = renderMembers(properties, functions, ownerAnnotations, annotationArgs)
                lines += "${prefix}data class ${declaration.name}$constructorText$superTypeSuffix${renderOptionalBody(members)}"
            }

            DeclarationKind.CLASS -> {
                val constructorText = constructorParams.takeIf { it.isNotEmpty() }?.joinToString(",\n", prefix = "(\n", postfix = "\n)") {
                    "    ${renderConstructorParam(it, ownerAnnotations, annotationArgs)}"
                }.orEmpty()
                val body = renderMembers(properties, functions, ownerAnnotations, annotationArgs)
                lines += "${prefix}class ${declaration.name}$constructorText$superTypeSuffix ${body.ifBlank { "{\n}" }}"
            }

            DeclarationKind.ENUM_CLASS -> {
                val header = "${prefix}enum class ${declaration.name}$superTypeSuffix"
                val body = renderEnumBody(enumEntries, properties, functions, ownerAnnotations, annotationArgs)
                lines += "$header $body"
            }

            DeclarationKind.INTERFACE -> {
                val body = renderMembers(properties, functions, ownerAnnotations, annotationArgs, interfaceMode = true)
                lines += "${prefix}interface ${declaration.name}$superTypeSuffix ${body.ifBlank { "{\n}" }}"
            }

            DeclarationKind.OBJECT -> {
                val body = renderMembers(properties, functions, ownerAnnotations, annotationArgs)
                lines += "${prefix}object ${declaration.name}$superTypeSuffix ${body.ifBlank { "{\n}" }}"
            }

            DeclarationKind.ANNOTATION_CLASS -> {
                val constructorText = constructorParams.joinToString(",\n", prefix = "(\n", postfix = "\n)") {
                    "    ${renderAnnotationConstructorParam(it)}"
                }
                lines += "${prefix}annotation class ${declaration.name}$constructorText"
            }
        }
        return lines.joinToString("\n")
    }

    private fun renderDeclarationKDoc(
        declaration: DeclarationMetaDto,
        constructorParams: List<ConstructorParamMetaDto>,
    ): String? {
        val descriptionLines = declaration.docComment
            ?.lineSequence()
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            ?.toList()
            .orEmpty()
        val tagLines = buildList {
            add("@author ${resolveDocAuthor()}")
            add("@date ${formatDocDate(declaration.createdAt)}")
            if (declaration.kind == DeclarationKind.DATA_CLASS ||
                declaration.kind == DeclarationKind.ANNOTATION_CLASS ||
                (declaration.kind == DeclarationKind.CLASS && constructorParams.isNotEmpty())
            ) {
                add("@constructor 创建[${declaration.name}]")
                constructorParams.forEach { param ->
                    add("@param [${param.name}]")
                }
            }
        }
        if (descriptionLines.isEmpty() && tagLines.isEmpty()) {
            return null
        }
        return buildString {
            appendLine("/**")
            descriptionLines.forEach { line ->
                appendLine(" * $line")
            }
            if (descriptionLines.isNotEmpty() && tagLines.isNotEmpty()) {
                appendLine(" *")
            }
            tagLines.forEach { line ->
                appendLine(" * $line")
            }
            append(" */")
        }
    }

    private fun buildPrefix(visibility: CodeVisibility, modifiers: List<String>): String {
        val parts = buildList {
            if (visibility != CodeVisibility.PUBLIC) {
                add(visibility.name.lowercase())
            }
            addAll(modifiers.map { it.lowercase() })
        }.filter { it.isNotBlank() }
        return if (parts.isEmpty()) "" else parts.joinToString(" ", postfix = " ")
    }

    private fun renderConstructorParam(
        param: ConstructorParamMetaDto,
        ownerAnnotations: Map<String, List<AnnotationUsageMetaDto>>,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
    ): String {
        val annotations = ownerAnnotations[param.id].orEmpty().joinToString(" ") {
            "@${it.annotationClassName}${renderAnnotationArgs(it.id, annotationArgs)}"
        }
        val valOrVar = if (param.mutable) "var" else "val"
        val defaultSuffix = param.defaultValue?.let { " = $it" }.orEmpty()
        val annotationPrefix = annotations.takeIf { it.isNotBlank() }?.plus(" ").orEmpty()
        return "$annotationPrefix$valOrVar ${param.name}: ${param.type}$defaultSuffix"
    }

    private fun renderAnnotationConstructorParam(param: ConstructorParamMetaDto): String {
        val defaultSuffix = param.defaultValue?.let { " = $it" }.orEmpty()
        return "val ${param.name}: ${param.type}$defaultSuffix"
    }

    private fun renderMembers(
        properties: List<PropertyMetaDto>,
        functions: List<FunctionStubMetaDto>,
        ownerAnnotations: Map<String, List<AnnotationUsageMetaDto>>,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
        interfaceMode: Boolean = false,
    ): String {
        val memberLines = mutableListOf<String>()
        properties.forEach { property ->
            ownerAnnotations[property.id].orEmpty().forEach { annotation ->
                memberLines += "    @${annotation.annotationClassName}${renderAnnotationArgs(annotation.id, annotationArgs)}"
            }
            val overridePrefix = if (property.isOverride) "override " else ""
            val visibilityPrefix = if (property.visibility == CodeVisibility.PUBLIC) "" else "${property.visibility.name.lowercase()} "
            val valOrVar = if (property.mutable) "var" else "val"
            val initializer = property.initializer?.let { " = $it" }.orEmpty()
            val effectiveInitializer = if (interfaceMode) "" else initializer
            memberLines += "    $visibilityPrefix${overridePrefix}$valOrVar ${property.name}: ${property.type}$effectiveInitializer"
        }
        functions.forEach { function ->
            ownerAnnotations[function.id].orEmpty().forEach { annotation ->
                memberLines += "    @${annotation.annotationClassName}${renderAnnotationArgs(annotation.id, annotationArgs)}"
            }
            memberLines += renderFunction(function, interfaceMode).prependIndent("    ")
        }
        if (memberLines.isEmpty()) {
            return ""
        }
        return "{\n${memberLines.joinToString("\n")}\n}"
    }

    private fun renderFunction(function: FunctionStubMetaDto, interfaceMode: Boolean): String {
        val prefix = buildPrefix(function.visibility, function.modifiers)
        val parameters = function.parameters.joinToString(", ") { param ->
            val defaultSuffix = param.defaultValue?.let { " = $it" }.orEmpty()
            "${param.name}: ${param.type}$defaultSuffix"
        }
        val signature = "${prefix}fun ${function.name}($parameters): ${function.returnType}"
        if (interfaceMode && function.bodyText.isNullOrBlank()) {
            return signature
        }
        val body = when (function.bodyMode) {
            FunctionBodyMode.RAW_TEXT -> function.bodyText?.trim().orEmpty()
            FunctionBodyMode.TEMPLATE -> function.bodyText?.trim().takeUnless { it.isNullOrBlank() } ?: "TODO(\"由 coding-playground 生成\")"
        }
        return "$signature {\n        $body\n    }"
    }

    private fun renderEnumBody(
        entries: List<EnumEntryMetaDto>,
        properties: List<PropertyMetaDto>,
        functions: List<FunctionStubMetaDto>,
        ownerAnnotations: Map<String, List<AnnotationUsageMetaDto>>,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
    ): String {
        val entryLines = entries.map { entry ->
            val args = if (entry.arguments.isEmpty()) "" else entry.arguments.joinToString(", ", prefix = "(", postfix = ")")
            entry.name + args
        }
        val members = renderMembers(properties, functions, ownerAnnotations, annotationArgs)
        return if (members.isBlank()) {
            "{\n    ${entryLines.joinToString(",\n    ")}\n}"
        } else {
            "{\n    ${entryLines.joinToString(",\n    ")},\n    ;\n${members.removePrefix("{\n").removeSuffix("\n}")}\n}"
        }
    }

    private fun renderOptionalBody(members: String): String = if (members.isBlank()) "" else " $members"

    private fun renderAnnotationArgs(
        usageId: String,
        annotationArgs: Map<String, List<AnnotationArgumentMetaDto>>,
    ): String {
        val args = annotationArgs[usageId].orEmpty()
        if (args.isEmpty()) {
            return ""
        }
        return args.joinToString(", ", prefix = "(", postfix = ")") { arg ->
            if (arg.name.isNullOrBlank()) {
                arg.value
            } else {
                "${arg.name} = ${arg.value}"
            }
        }
    }
}

private fun SourceFileAggregateDto.toManagedMetadataSnapshot(): SourceFileAggregateDto {
    return copy(
        artifacts = emptyList(),
        conflicts = emptyList(),
    )
}

private sealed interface ExportWriteResult {
    data class Artifact(val artifact: ManagedArtifactMeta) : ExportWriteResult
    data class Conflict(val conflict: SyncConflictMeta) : ExportWriteResult
}

private sealed interface ImportWriteResult {
    data class Success(val file: SourceFileAggregateDto) : ImportWriteResult
    data class Conflict(val conflict: SyncConflictMeta) : ImportWriteResult
}

private data class MarkerInfo(
    val projectId: String,
    val targetId: String,
    val fileId: String,
    val declarationIds: List<String>,
    val metadataHash: String,
    val contentHash: String,
)

private data class ParsedManagedSource(
    val packageName: String,
    val imports: List<ParsedImport>,
    val fileAnnotations: List<ParsedAnnotation>,
    val declaration: ParsedDeclaration,
)

private data class ParsedImport(
    val importPath: String,
    val alias: String?,
)

private data class ParsedAnnotation(
    val name: String,
    val useSiteTarget: String? = null,
    val arguments: List<ParsedAnnotationArgument> = emptyList(),
)

private data class ParsedAnnotationArgument(
    val name: String?,
    val value: String,
)

private data class ParsedConstructorParam(
    val name: String,
    val type: String,
    val mutable: Boolean,
    val nullable: Boolean,
    val defaultValue: String?,
)

private data class ParsedProperty(
    val name: String,
    val type: String,
    val mutable: Boolean,
    val nullable: Boolean,
    val initializer: String?,
    val visibility: CodeVisibility,
    val isOverride: Boolean,
    val annotations: List<ParsedAnnotation>,
)

private data class ParsedEnumEntry(
    val name: String,
    val arguments: List<String> = emptyList(),
    val bodyText: String? = null,
)

private data class ParsedFunction(
    val name: String,
    val returnType: String,
    val visibility: CodeVisibility,
    val modifiers: List<String>,
    val parameters: List<FunctionParameterDto>,
    val bodyMode: FunctionBodyMode,
    val bodyText: String?,
    val annotations: List<ParsedAnnotation>,
)

private data class ParsedDeclaration(
    val declarationId: String?,
    val name: String,
    val kind: DeclarationKind,
    val visibility: CodeVisibility,
    val modifiers: List<String>,
    val superTypes: List<String>,
    val annotations: List<ParsedAnnotation>,
    val constructorParams: List<ParsedConstructorParam> = emptyList(),
    val properties: List<ParsedProperty> = emptyList(),
    val enumEntries: List<ParsedEnumEntry> = emptyList(),
    val functions: List<ParsedFunction> = emptyList(),
)

private fun buildMarker(
    projectId: String,
    targetId: String,
    fileId: String,
    declarationIds: List<String>,
    metadataHash: String,
    contentHash: String,
): String {
    return buildString {
        appendLine("/* coding-playground-managed")
        appendLine("projectId=$projectId")
        appendLine("targetId=$targetId")
        appendLine("fileId=$fileId")
        appendLine("declarationIds=${declarationIds.joinToString(",")}")
        appendLine("metadataHash=$metadataHash")
        appendLine("contentHash=$contentHash")
        append("*/")
    }
}

private fun parseMarker(text: String): MarkerInfo? {
    val regex = Regex("""/\* coding-playground-managed\s+projectId=(.+)\s+targetId=(.+)\s+fileId=(.+)\s+declarationIds=(.*)\s+metadataHash=(.+)\s+contentHash=(.+)\s+\*/""")
    val match = regex.find(text) ?: return null
    return MarkerInfo(
        projectId = match.groupValues[1].trim(),
        targetId = match.groupValues[2].trim(),
        fileId = match.groupValues[3].trim(),
        declarationIds = match.groupValues[4].split(',').filter { it.isNotBlank() },
        metadataHash = match.groupValues[5].trim(),
        contentHash = match.groupValues[6].trim(),
    )
}

private fun parseManagedSource(text: String): ParsedManagedSource {
    val body = text.replaceFirst(Regex("""/\* coding-playground-managed[\s\S]*?\*/\s*"""), "")
    val sanitizedBody = stripKDocBlocks(body)
    val lines = sanitizedBody.lines()
    val fileAnnotations = lines.takeWhile { it.startsWith("@file:") }.map { parseAnnotationLine(it.removePrefix("@file:").trim(), useSiteTarget = "file") }
    val packageLine = lines.firstOrNull { it.startsWith("package ") } ?: throw PlaygroundValidationException("源码缺少 package 声明")
    val packageName = packageLine.removePrefix("package ").trim()
    val importLines = lines.filter { it.startsWith("import ") }
    val imports = importLines.map { line ->
        val content = line.removePrefix("import ").trim()
        val parts = content.split(" as ")
        ParsedImport(parts[0].trim(), parts.getOrNull(1)?.trim())
    }
    val declarationStart = lines.indexOfFirst {
        val trimmed = it.trim()
        trimmed.startsWith("@") || trimmed.contains("class ") || trimmed.startsWith("interface ") || trimmed.startsWith("object ")
    }
    if (declarationStart < 0) {
        throw PlaygroundValidationException("源码里没有可导回的声明")
    }
    val declarationText = lines.drop(declarationStart).joinToString("\n").trim()
    val declaration = parseDeclaration(declarationText)
    return ParsedManagedSource(
        packageName = packageName,
        imports = imports,
        fileAnnotations = fileAnnotations,
        declaration = declaration,
    )
}

private fun parseDeclaration(text: String): ParsedDeclaration {
    val lines = text.lines().toMutableList()
    val annotations = mutableListOf<ParsedAnnotation>()
    while (lines.firstOrNull()?.trim()?.startsWith("@") == true) {
        val line = lines.removeAt(0).trim()
        if (!line.startsWith("@GeneratedManagedDeclaration")) {
            annotations += parseAnnotationLine(line.removePrefix("@").trim())
        }
    }
    val headerAndBody = lines.joinToString("\n").trim()
    val declarationId = Regex("""@GeneratedManagedDeclaration\(declarationId = "([^"]+)"""").find(text)?.groupValues?.get(1)
    val headerRegex = Regex("""^(?:(public|internal|private)\s+)?([A-Za-z\s]*?)?(annotation class|data class|enum class|class|interface|object)\s+([A-Za-z_][A-Za-z0-9_]*)([\s\S]*)$""")
    val match = headerRegex.find(headerAndBody) ?: throw PlaygroundValidationException("暂不支持当前声明头部格式")
    val visibility = match.groupValues[1].takeIf { it.isNotBlank() }?.let { enumValueOf<CodeVisibility>(it.uppercase()) }
        ?: CodeVisibility.PUBLIC
    val modifierText = match.groupValues[2].trim()
    val kindToken = match.groupValues[3].trim()
    val name = match.groupValues[4].trim()
    val remainder = match.groupValues[5]
    val kind = when (kindToken) {
        "data class" -> DeclarationKind.DATA_CLASS
        "class" -> DeclarationKind.CLASS
        "enum class" -> DeclarationKind.ENUM_CLASS
        "interface" -> DeclarationKind.INTERFACE
        "object" -> DeclarationKind.OBJECT
        "annotation class" -> DeclarationKind.ANNOTATION_CLASS
        else -> throw PlaygroundValidationException("未知声明类型: $kindToken")
    }
    val signature = remainder.substringBefore("{").trim()
    val body = if (remainder.contains("{")) remainder.substringAfter("{").substringBeforeLast("}").trim() else ""
    val constructorParams = if (kind in setOf(DeclarationKind.DATA_CLASS, DeclarationKind.CLASS, DeclarationKind.ANNOTATION_CLASS)) {
        parseConstructorParams(signature)
    } else {
        emptyList()
    }
    val superTypes = parseSuperTypes(signature)
    return ParsedDeclaration(
        declarationId = declarationId,
        name = name,
        kind = kind,
        visibility = visibility,
        modifiers = modifierText.split(' ').filter { it.isNotBlank() },
        superTypes = superTypes,
        annotations = annotations,
        constructorParams = constructorParams,
        properties = parseProperties(body),
        enumEntries = if (kind == DeclarationKind.ENUM_CLASS) parseEnumEntries(body) else emptyList(),
        functions = parseFunctions(body, kind == DeclarationKind.INTERFACE),
    )
}

private fun parseConstructorParams(signature: String): List<ParsedConstructorParam> {
    val block = signature.substringAfter("(", "").substringBeforeLast(")", "")
    if (block.isBlank()) {
        return emptyList()
    }
    return block.lines()
        .map { it.trim().trimEnd(',') }
        .filter { it.isNotBlank() }
        .map { line ->
            if (line.contains("@")) {
                throw PlaygroundValidationException("一期导回暂不支持构造参数注解，请先在工作台内编辑")
            }
            val regex = Regex("""(val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*:\s*([^=]+?)(?:\s*=\s*(.+))?$""")
            val match = regex.find(line) ?: throw PlaygroundValidationException("无法解析构造参数: $line")
            ParsedConstructorParam(
                name = match.groupValues[2].trim(),
                type = match.groupValues[3].trim(),
                mutable = match.groupValues[1] == "var",
                nullable = match.groupValues[3].trim().endsWith("?"),
                defaultValue = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.trim(),
            )
        }
}

private fun parseSuperTypes(signature: String): List<String> {
    val candidate = signature.substringAfter(")", signature).substringAfter(":", "")
    if (candidate.isBlank()) {
        return emptyList()
    }
    return candidate.split(',').map { it.trim() }.filter { it.isNotBlank() }
}

private fun parseProperties(body: String): List<ParsedProperty> {
    val lines = body.lines()
    val results = mutableListOf<ParsedProperty>()
    val pendingAnnotations = mutableListOf<ParsedAnnotation>()
    lines.forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isBlank() || line.endsWith(",") || line == ";") {
            return@forEach
        }
        if (line.startsWith("@")) {
            pendingAnnotations += parseAnnotationLine(line.removePrefix("@").trim())
            return@forEach
        }
        val regex = Regex("""(?:(public|internal|private)\s+)?(override\s+)?(val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*:\s*([^=]+?)(?:\s*=\s*(.+))?$""")
        val match = regex.find(line) ?: return@forEach
        results += ParsedProperty(
            name = match.groupValues[4].trim(),
            type = match.groupValues[5].trim(),
            mutable = match.groupValues[3] == "var",
            nullable = match.groupValues[5].trim().endsWith("?"),
            initializer = match.groupValues.getOrNull(6)?.takeIf { it.isNotBlank() }?.trim(),
            visibility = match.groupValues[1].takeIf { it.isNotBlank() }?.let { enumValueOf<CodeVisibility>(it.uppercase()) }
                ?: CodeVisibility.PUBLIC,
            isOverride = match.groupValues[2].isNotBlank(),
            annotations = pendingAnnotations.toList(),
        )
        pendingAnnotations.clear()
    }
    return results
}

private fun parseEnumEntries(body: String): List<ParsedEnumEntry> {
    val entrySection = body.substringBefore(";").trim()
    if (entrySection.isBlank()) {
        return emptyList()
    }
    return entrySection.lines()
        .map { it.trim().trimEnd(',') }
        .filter { it.isNotBlank() }
        .map { line ->
            val name = line.substringBefore("(").trim()
            val args = line.substringAfter("(", "").substringBeforeLast(")", "").takeIf { it.isNotBlank() }?.split(',')?.map { it.trim() } ?: emptyList()
            ParsedEnumEntry(name = name, arguments = args)
        }
}

private fun parseFunctions(body: String, interfaceMode: Boolean): List<ParsedFunction> {
    val lines = body.lines()
    val results = mutableListOf<ParsedFunction>()
    val pendingAnnotations = mutableListOf<ParsedAnnotation>()
    var index = 0
    while (index < lines.size) {
        val line = lines[index].trim()
        if (line.isBlank() || line.endsWith(",") || line == ";") {
            index++
            continue
        }
        if (line.startsWith("@")) {
            pendingAnnotations += parseAnnotationLine(line.removePrefix("@").trim())
            index++
            continue
        }
        if (!line.contains("fun ")) {
            index++
            continue
        }
        val collected = mutableListOf(lines[index])
        var braceDelta = countBraces(lines[index])
        while (braceDelta > 0 && index + 1 < lines.size) {
            index++
            collected += lines[index]
            braceDelta += countBraces(lines[index])
        }
        val functionText = collected.joinToString("\n").trim()
        results += parseFunctionBlock(functionText, pendingAnnotations.toList(), interfaceMode)
        pendingAnnotations.clear()
        index++
    }
    return results
}

private fun parseFunctionBlock(text: String, annotations: List<ParsedAnnotation>, interfaceMode: Boolean): ParsedFunction {
    val header = text.substringBefore("{").trim()
    val body = text.substringAfter("{", "").substringBeforeLast("}", "").trim().ifBlank { null }
    val regex = Regex("""(?:(public|internal|private)\s+)?([A-Za-z\s]*)fun\s+([A-Za-z_][A-Za-z0-9_]*)\((.*)\)\s*:\s*(.+)$""")
    val match = regex.find(header) ?: throw PlaygroundValidationException("无法解析函数声明: $text")
    val parameters = match.groupValues[4].split(',').mapNotNull { item ->
        val raw = item.trim()
        if (raw.isBlank()) {
            null
        } else {
            val paramMatch = Regex("""([A-Za-z_][A-Za-z0-9_]*)\s*:\s*([^=]+?)(?:\s*=\s*(.+))?$""").find(raw)
                ?: throw PlaygroundValidationException("无法解析函数参数: $raw")
            FunctionParameterDto(
                name = paramMatch.groupValues[1].trim(),
                type = paramMatch.groupValues[2].trim(),
                nullable = paramMatch.groupValues[2].trim().endsWith("?"),
                defaultValue = paramMatch.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.trim(),
            )
        }
    }
    return ParsedFunction(
        name = match.groupValues[3].trim(),
        returnType = match.groupValues[5].trim(),
        visibility = match.groupValues[1].takeIf { it.isNotBlank() }?.let { enumValueOf<CodeVisibility>(it.uppercase()) }
            ?: CodeVisibility.PUBLIC,
        modifiers = match.groupValues[2].split(' ').filter { it.isNotBlank() },
        parameters = parameters,
        bodyMode = if (body.isNullOrBlank() && interfaceMode) FunctionBodyMode.TEMPLATE else FunctionBodyMode.RAW_TEXT,
        bodyText = body,
        annotations = annotations,
    )
}

private fun parseAnnotationLine(text: String, useSiteTarget: String? = null): ParsedAnnotation {
    val name = text.substringBefore("(").substringBefore(":").trim()
    val argsBlock = text.substringAfter("(", "").substringBeforeLast(")", "")
    val args = if (argsBlock.isBlank()) {
        emptyList()
    } else {
        argsBlock.split(',').map { item ->
            val raw = item.trim()
            if (raw.contains("=")) {
                ParsedAnnotationArgument(
                    name = raw.substringBefore("=").trim(),
                    value = raw.substringAfter("=").trim(),
                )
            } else {
                ParsedAnnotationArgument(name = null, value = raw)
            }
        }
    }
    return ParsedAnnotation(
        name = name,
        useSiteTarget = useSiteTarget,
        arguments = args,
    )
}

private fun resolveDocAuthor(): String {
    return System.getProperty("user.name")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "unknown"
}

private fun formatDocDate(createdAt: String): String {
    val candidate = createdAt.substringBefore('T').substringBefore(' ')
    return runCatching {
        LocalDate.parse(candidate).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }.getOrElse {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }
}

private fun stripKDocBlocks(text: String): String {
    return text.replace(Regex("""/\*\*[\s\S]*?\*/\s*"""), "")
}

private fun countBraces(text: String): Int = text.count { it == '{' } - text.count { it == '}' }

private fun serviceFqName(packageName: String, name: String): String {
    return if (packageName.isBlank()) {
        name
    } else {
        "$packageName.$name"
    }
}
