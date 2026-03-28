package site.addzero.coding.playground.server.route

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*

private fun projectService(): CodegenProjectService = KoinPlatform.getKoin().get()
private fun targetService(): GenerationTargetService = KoinPlatform.getKoin().get()
private fun fileService(): SourceFileService = KoinPlatform.getKoin().get()
private fun declarationService(): DeclarationService = KoinPlatform.getKoin().get()
private fun renderService(): CodeRenderService = KoinPlatform.getKoin().get()
private fun artifactService(): ManagedArtifactService = KoinPlatform.getKoin().get()
private fun syncService(): SyncService = KoinPlatform.getKoin().get()
private fun kspIndexService(): KspIndexService = KoinPlatform.getKoin().get()

@GetMapping("/api/codegen/projects")
suspend fun listProjects(@RequestParam("query") query: String?): List<CodegenProjectDto> =
    projectService().list(CodegenSearchRequest(query = query))

@PostMapping("/api/codegen/projects")
suspend fun createProject(@RequestBody request: CreateCodegenProjectRequest): CodegenProjectDto = projectService().create(request)

@GetMapping("/api/codegen/projects/{id}")
suspend fun getProject(@PathVariable id: String): CodegenProjectDto = projectService().get(id)

@GetMapping("/api/codegen/projects/{id}/aggregate")
suspend fun getProjectAggregate(@PathVariable id: String): CodegenProjectAggregateDto = projectService().aggregate(id)

@PutMapping("/api/codegen/projects/{id}")
suspend fun updateProject(@PathVariable id: String, @RequestBody request: UpdateCodegenProjectRequest): CodegenProjectDto =
    projectService().update(id, request)

@GetMapping("/api/codegen/projects/{id}/delete-check")
suspend fun deleteProjectCheck(@PathVariable id: String): DeleteCheckResultDto = projectService().deleteCheck(id)

@GetMapping("/api/codegen/projects/{id}/validate")
suspend fun validateProject(@PathVariable id: String): List<ValidationIssueDto> = projectService().validate(id)

@DeleteMapping("/api/codegen/projects/{id}")
suspend fun deleteProject(@PathVariable id: String) = projectService().delete(id)

@GetMapping("/api/codegen/targets")
suspend fun listTargets(
    @RequestParam("projectId") projectId: String?,
    @RequestParam("query") query: String?,
): List<GenerationTargetDto> = targetService().list(CodegenSearchRequest(query = query, projectId = projectId))

@PostMapping("/api/codegen/targets")
suspend fun createTarget(@RequestBody request: CreateGenerationTargetRequest): GenerationTargetDto = targetService().create(request)

@GetMapping("/api/codegen/targets/{id}")
suspend fun getTarget(@PathVariable id: String): GenerationTargetDto = targetService().get(id)

@PutMapping("/api/codegen/targets/{id}")
suspend fun updateTarget(@PathVariable id: String, @RequestBody request: UpdateGenerationTargetRequest): GenerationTargetDto =
    targetService().update(id, request)

@GetMapping("/api/codegen/targets/{id}/preview-path")
suspend fun previewTargetPath(@PathVariable id: String): PathPreviewDto = targetService().previewPath(id)

@GetMapping("/api/codegen/targets/{id}/delete-check")
suspend fun deleteTargetCheck(@PathVariable id: String): DeleteCheckResultDto = targetService().deleteCheck(id)

@GetMapping("/api/codegen/targets/{id}/validate")
suspend fun validateTarget(@PathVariable id: String): List<ValidationIssueDto> = targetService().validate(id)

@DeleteMapping("/api/codegen/targets/{id}")
suspend fun deleteTarget(@PathVariable id: String) = targetService().delete(id)

@GetMapping("/api/codegen/files")
suspend fun listFiles(
    @RequestParam("targetId") targetId: String?,
    @RequestParam("query") query: String?,
): List<SourceFileMetaDto> = fileService().list(CodegenSearchRequest(query = query, targetId = targetId))

@PostMapping("/api/codegen/files")
suspend fun createFile(@RequestBody request: CreateSourceFileRequest): SourceFileMetaDto = fileService().create(request)

@PostMapping("/api/codegen/files/presets")
suspend fun createFilePreset(@RequestBody request: CreateDeclarationPresetRequest): SourceFileAggregateDto = fileService().createPreset(request)

@GetMapping("/api/codegen/files/{id}")
suspend fun getFile(@PathVariable id: String): SourceFileMetaDto = fileService().get(id)

@GetMapping("/api/codegen/files/{id}/aggregate")
suspend fun getFileAggregate(@PathVariable id: String): SourceFileAggregateDto = fileService().aggregate(id)

@PutMapping("/api/codegen/files/{id}")
suspend fun updateFile(@PathVariable id: String, @RequestBody request: UpdateSourceFileRequest): SourceFileMetaDto =
    fileService().update(id, request)

@GetMapping("/api/codegen/files/{id}/delete-check")
suspend fun deleteFileCheck(@PathVariable id: String): DeleteCheckResultDto = fileService().deleteCheck(id)

@GetMapping("/api/codegen/files/{id}/validate")
suspend fun validateFile(@PathVariable id: String): List<ValidationIssueDto> = fileService().validate(id)

@DeleteMapping("/api/codegen/files/{id}")
suspend fun deleteFile(@PathVariable id: String) = fileService().delete(id)

@PostMapping("/api/codegen/files/{fileId}/imports")
suspend fun createImport(@PathVariable fileId: String, @RequestBody request: CreateImportRequest): ImportMetaDto =
    fileService().createImport(request.copy(fileId = fileId))

@PutMapping("/api/codegen/imports/{id}")
suspend fun updateImport(@PathVariable id: String, @RequestBody request: UpdateImportRequest): ImportMetaDto =
    fileService().updateImport(id, request)

@DeleteMapping("/api/codegen/imports/{id}")
suspend fun deleteImport(@PathVariable id: String) = fileService().deleteImport(id)

@PostMapping("/api/codegen/files/{fileId}/imports/reorder")
suspend fun reorderImports(@PathVariable fileId: String, @RequestBody request: ReorderRequestDto): List<ImportMetaDto> =
    fileService().reorderImports(fileId, request)

@GetMapping("/api/codegen/declarations")
suspend fun listDeclarations(
    @RequestParam("fileId") fileId: String?,
    @RequestParam("targetId") targetId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("kind") kind: DeclarationKind?,
): List<DeclarationMetaDto> = declarationService().list(
    CodegenSearchRequest(query = query, fileId = fileId, targetId = targetId, kind = kind),
)

@PostMapping("/api/codegen/declarations")
suspend fun createDeclaration(@RequestBody request: CreateDeclarationRequest): DeclarationMetaDto = declarationService().create(request)

@GetMapping("/api/codegen/declarations/{id}")
suspend fun getDeclaration(@PathVariable id: String): DeclarationMetaDto = declarationService().get(id)

@PutMapping("/api/codegen/declarations/{id}")
suspend fun updateDeclaration(@PathVariable id: String, @RequestBody request: UpdateDeclarationRequest): DeclarationMetaDto =
    declarationService().update(id, request)

@GetMapping("/api/codegen/declarations/{id}/delete-check")
suspend fun deleteDeclarationCheck(@PathVariable id: String): DeleteCheckResultDto = declarationService().deleteCheck(id)

@GetMapping("/api/codegen/declarations/{id}/validate")
suspend fun validateDeclaration(@PathVariable id: String): List<ValidationIssueDto> = declarationService().validate(id)

@DeleteMapping("/api/codegen/declarations/{id}")
suspend fun deleteDeclaration(@PathVariable id: String) = declarationService().delete(id)

@PostMapping("/api/codegen/files/{fileId}/declarations/reorder")
suspend fun reorderDeclarations(@PathVariable fileId: String, @RequestBody request: ReorderRequestDto): List<DeclarationMetaDto> =
    declarationService().reorderDeclarations(fileId, request)

@PostMapping("/api/codegen/declarations/{declarationId}/constructor-params")
suspend fun createConstructorParam(
    @PathVariable declarationId: String,
    @RequestBody request: CreateConstructorParamRequest,
): ConstructorParamMetaDto = declarationService().createConstructorParam(request.copy(declarationId = declarationId))

@PutMapping("/api/codegen/constructor-params/{id}")
suspend fun updateConstructorParam(@PathVariable id: String, @RequestBody request: UpdateConstructorParamRequest): ConstructorParamMetaDto =
    declarationService().updateConstructorParam(id, request)

@DeleteMapping("/api/codegen/constructor-params/{id}")
suspend fun deleteConstructorParam(@PathVariable id: String) = declarationService().deleteConstructorParam(id)

@PostMapping("/api/codegen/declarations/{declarationId}/constructor-params/reorder")
suspend fun reorderConstructorParams(
    @PathVariable declarationId: String,
    @RequestBody request: ReorderRequestDto,
): List<ConstructorParamMetaDto> = declarationService().reorderConstructorParams(declarationId, request)

@PostMapping("/api/codegen/declarations/{declarationId}/properties")
suspend fun createProperty(@PathVariable declarationId: String, @RequestBody request: CreatePropertyRequest): PropertyMetaDto =
    declarationService().createProperty(request.copy(declarationId = declarationId))

@PutMapping("/api/codegen/properties/{id}")
suspend fun updateProperty(@PathVariable id: String, @RequestBody request: UpdatePropertyRequest): PropertyMetaDto =
    declarationService().updateProperty(id, request)

@DeleteMapping("/api/codegen/properties/{id}")
suspend fun deleteProperty(@PathVariable id: String) = declarationService().deleteProperty(id)

@PostMapping("/api/codegen/declarations/{declarationId}/properties/reorder")
suspend fun reorderProperties(@PathVariable declarationId: String, @RequestBody request: ReorderRequestDto): List<PropertyMetaDto> =
    declarationService().reorderProperties(declarationId, request)

@PostMapping("/api/codegen/declarations/{declarationId}/enum-entries")
suspend fun createEnumEntry(@PathVariable declarationId: String, @RequestBody request: CreateEnumEntryRequest): EnumEntryMetaDto =
    declarationService().createEnumEntry(request.copy(declarationId = declarationId))

@PutMapping("/api/codegen/enum-entries/{id}")
suspend fun updateEnumEntry(@PathVariable id: String, @RequestBody request: UpdateEnumEntryRequest): EnumEntryMetaDto =
    declarationService().updateEnumEntry(id, request)

@DeleteMapping("/api/codegen/enum-entries/{id}")
suspend fun deleteEnumEntry(@PathVariable id: String) = declarationService().deleteEnumEntry(id)

@PostMapping("/api/codegen/declarations/{declarationId}/enum-entries/reorder")
suspend fun reorderEnumEntries(@PathVariable declarationId: String, @RequestBody request: ReorderRequestDto): List<EnumEntryMetaDto> =
    declarationService().reorderEnumEntries(declarationId, request)

@PostMapping("/api/codegen/annotations")
suspend fun createAnnotationUsage(@RequestBody request: CreateAnnotationUsageRequest): AnnotationUsageMetaDto =
    declarationService().createAnnotationUsage(request)

@PutMapping("/api/codegen/annotations/{id}")
suspend fun updateAnnotationUsage(@PathVariable id: String, @RequestBody request: UpdateAnnotationUsageRequest): AnnotationUsageMetaDto =
    declarationService().updateAnnotationUsage(id, request)

@DeleteMapping("/api/codegen/annotations/{id}")
suspend fun deleteAnnotationUsage(@PathVariable id: String) = declarationService().deleteAnnotationUsage(id)

@PostMapping("/api/codegen/annotation-arguments")
suspend fun createAnnotationArgument(@RequestBody request: CreateAnnotationArgumentRequest): AnnotationArgumentMetaDto =
    declarationService().createAnnotationArgument(request)

@PutMapping("/api/codegen/annotation-arguments/{id}")
suspend fun updateAnnotationArgument(
    @PathVariable id: String,
    @RequestBody request: UpdateAnnotationArgumentRequest,
): AnnotationArgumentMetaDto = declarationService().updateAnnotationArgument(id, request)

@DeleteMapping("/api/codegen/annotation-arguments/{id}")
suspend fun deleteAnnotationArgument(@PathVariable id: String) = declarationService().deleteAnnotationArgument(id)

@PostMapping("/api/codegen/annotations/{annotationUsageId}/arguments/reorder")
suspend fun reorderAnnotationArguments(
    @PathVariable annotationUsageId: String,
    @RequestBody request: ReorderRequestDto,
): List<AnnotationArgumentMetaDto> = declarationService().reorderAnnotationArguments(annotationUsageId, request)

@PostMapping("/api/codegen/declarations/{declarationId}/functions")
suspend fun createFunctionStub(@PathVariable declarationId: String, @RequestBody request: CreateFunctionStubRequest): FunctionStubMetaDto =
    declarationService().createFunctionStub(request.copy(declarationId = declarationId))

@PutMapping("/api/codegen/functions/{id}")
suspend fun updateFunctionStub(@PathVariable id: String, @RequestBody request: UpdateFunctionStubRequest): FunctionStubMetaDto =
    declarationService().updateFunctionStub(id, request)

@DeleteMapping("/api/codegen/functions/{id}")
suspend fun deleteFunctionStub(@PathVariable id: String) = declarationService().deleteFunctionStub(id)

@PostMapping("/api/codegen/declarations/{declarationId}/functions/reorder")
suspend fun reorderFunctionStubs(
    @PathVariable declarationId: String,
    @RequestBody request: ReorderRequestDto,
): List<FunctionStubMetaDto> = declarationService().reorderFunctionStubs(declarationId, request)

@GetMapping("/api/codegen/render/preview/{fileId}")
suspend fun previewCode(@PathVariable fileId: String): CodeRenderPreviewDto = renderService().previewFile(fileId)

@GetMapping("/api/codegen/artifacts")
suspend fun listArtifacts(
    @RequestParam("targetId") targetId: String?,
    @RequestParam("fileId") fileId: String?,
    @RequestParam("query") query: String?,
): List<ManagedArtifactMetaDto> = artifactService().list(CodegenSearchRequest(query = query, targetId = targetId, fileId = fileId))

@PostMapping("/api/codegen/sync/export")
suspend fun exportCode(@RequestBody request: SyncExportRequest): SyncExportResultDto = syncService().export(request)

@PostMapping("/api/codegen/sync/import")
suspend fun importCode(@RequestBody request: SyncImportRequest): SyncImportResultDto = syncService().importSource(request)

@GetMapping("/api/codegen/sync/conflicts")
suspend fun listConflicts(
    @RequestParam("targetId") targetId: String?,
    @RequestParam("fileId") fileId: String?,
    @RequestParam("query") query: String?,
): List<SyncConflictMetaDto> = syncService().listConflicts(CodegenSearchRequest(query = query, targetId = targetId, fileId = fileId))

@PostMapping("/api/codegen/sync/conflicts/{id}/resolve")
suspend fun resolveConflict(@PathVariable id: String, @RequestBody request: ResolveSyncConflictRequest): SyncConflictMetaDto =
    syncService().resolveConflict(id, request)

@GetMapping("/api/codegen/ksp/index-preview/{targetId}")
suspend fun previewKspIndex(@PathVariable targetId: String): KspIndexPreviewDto = kspIndexService().previewIndex(targetId)
