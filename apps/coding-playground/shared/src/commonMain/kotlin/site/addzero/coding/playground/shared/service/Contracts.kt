package site.addzero.coding.playground.shared.service

import site.addzero.coding.playground.shared.dto.*

interface CodegenProjectService {
    suspend fun create(request: CreateCodegenProjectRequest): CodegenProjectDto
    suspend fun list(search: CodegenSearchRequest = CodegenSearchRequest()): List<CodegenProjectDto>
    suspend fun get(id: String): CodegenProjectDto
    suspend fun aggregate(id: String): CodegenProjectAggregateDto
    suspend fun update(id: String, request: UpdateCodegenProjectRequest): CodegenProjectDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun validate(id: String): List<ValidationIssueDto>
    suspend fun delete(id: String)
}

interface GenerationTargetService {
    suspend fun create(request: CreateGenerationTargetRequest): GenerationTargetDto
    suspend fun list(search: CodegenSearchRequest = CodegenSearchRequest()): List<GenerationTargetDto>
    suspend fun get(id: String): GenerationTargetDto
    suspend fun update(id: String, request: UpdateGenerationTargetRequest): GenerationTargetDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun validate(id: String): List<ValidationIssueDto>
    suspend fun previewPath(id: String): PathPreviewDto
    suspend fun delete(id: String)
}

interface SourceFileService {
    suspend fun create(request: CreateSourceFileRequest): SourceFileMetaDto
    suspend fun createPreset(request: CreateDeclarationPresetRequest): SourceFileAggregateDto
    suspend fun list(search: CodegenSearchRequest = CodegenSearchRequest()): List<SourceFileMetaDto>
    suspend fun get(id: String): SourceFileMetaDto
    suspend fun aggregate(id: String): SourceFileAggregateDto
    suspend fun update(id: String, request: UpdateSourceFileRequest): SourceFileMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun validate(id: String): List<ValidationIssueDto>
    suspend fun delete(id: String)

    suspend fun createImport(request: CreateImportRequest): ImportMetaDto
    suspend fun updateImport(id: String, request: UpdateImportRequest): ImportMetaDto
    suspend fun deleteImport(id: String)
    suspend fun reorderImports(fileId: String, request: ReorderRequestDto): List<ImportMetaDto>
}

interface DeclarationService {
    suspend fun create(request: CreateDeclarationRequest): DeclarationMetaDto
    suspend fun list(search: CodegenSearchRequest = CodegenSearchRequest()): List<DeclarationMetaDto>
    suspend fun get(id: String): DeclarationMetaDto
    suspend fun update(id: String, request: UpdateDeclarationRequest): DeclarationMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun validate(id: String): List<ValidationIssueDto>
    suspend fun delete(id: String)
    suspend fun reorderDeclarations(fileId: String, request: ReorderRequestDto): List<DeclarationMetaDto>

    suspend fun createConstructorParam(request: CreateConstructorParamRequest): ConstructorParamMetaDto
    suspend fun updateConstructorParam(id: String, request: UpdateConstructorParamRequest): ConstructorParamMetaDto
    suspend fun deleteConstructorParam(id: String)
    suspend fun reorderConstructorParams(declarationId: String, request: ReorderRequestDto): List<ConstructorParamMetaDto>

    suspend fun createProperty(request: CreatePropertyRequest): PropertyMetaDto
    suspend fun updateProperty(id: String, request: UpdatePropertyRequest): PropertyMetaDto
    suspend fun deleteProperty(id: String)
    suspend fun reorderProperties(declarationId: String, request: ReorderRequestDto): List<PropertyMetaDto>

    suspend fun createEnumEntry(request: CreateEnumEntryRequest): EnumEntryMetaDto
    suspend fun updateEnumEntry(id: String, request: UpdateEnumEntryRequest): EnumEntryMetaDto
    suspend fun deleteEnumEntry(id: String)
    suspend fun reorderEnumEntries(declarationId: String, request: ReorderRequestDto): List<EnumEntryMetaDto>

    suspend fun createAnnotationUsage(request: CreateAnnotationUsageRequest): AnnotationUsageMetaDto
    suspend fun updateAnnotationUsage(id: String, request: UpdateAnnotationUsageRequest): AnnotationUsageMetaDto
    suspend fun deleteAnnotationUsage(id: String)

    suspend fun createAnnotationArgument(request: CreateAnnotationArgumentRequest): AnnotationArgumentMetaDto
    suspend fun updateAnnotationArgument(id: String, request: UpdateAnnotationArgumentRequest): AnnotationArgumentMetaDto
    suspend fun deleteAnnotationArgument(id: String)
    suspend fun reorderAnnotationArguments(annotationUsageId: String, request: ReorderRequestDto): List<AnnotationArgumentMetaDto>

    suspend fun createFunctionStub(request: CreateFunctionStubRequest): FunctionStubMetaDto
    suspend fun updateFunctionStub(id: String, request: UpdateFunctionStubRequest): FunctionStubMetaDto
    suspend fun deleteFunctionStub(id: String)
    suspend fun reorderFunctionStubs(declarationId: String, request: ReorderRequestDto): List<FunctionStubMetaDto>
}

interface CodeRenderService {
    suspend fun previewFile(fileId: String): CodeRenderPreviewDto
}

interface ManagedArtifactService {
    suspend fun list(search: CodegenSearchRequest = CodegenSearchRequest()): List<ManagedArtifactMetaDto>
    suspend fun get(id: String): ManagedArtifactMetaDto
}

interface SyncService {
    suspend fun export(request: SyncExportRequest): SyncExportResultDto
    suspend fun importSource(request: SyncImportRequest): SyncImportResultDto
    suspend fun listConflicts(search: CodegenSearchRequest = CodegenSearchRequest()): List<SyncConflictMetaDto>
    suspend fun resolveConflict(id: String, request: ResolveSyncConflictRequest): SyncConflictMetaDto
}

interface KspIndexService {
    suspend fun previewIndex(targetId: String): KspIndexPreviewDto
}
