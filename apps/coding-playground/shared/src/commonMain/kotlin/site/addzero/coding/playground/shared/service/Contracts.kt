package site.addzero.coding.playground.shared.service

import site.addzero.coding.playground.shared.dto.BoundedContextMetaDto
import site.addzero.coding.playground.shared.dto.CompositeIntegrationResultDto
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.CreateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.dto.CreateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateDtoMetaRequest
import site.addzero.coding.playground.shared.dto.CreateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.CreateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.dto.CreateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.dto.CreateProjectMetaRequest
import site.addzero.coding.playground.shared.dto.CreateRelationMetaRequest
import site.addzero.coding.playground.shared.dto.CreateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.DeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.DtoFieldMetaDto
import site.addzero.coding.playground.shared.dto.DtoMetaDto
import site.addzero.coding.playground.shared.dto.EtlWrapperMetaDto
import site.addzero.coding.playground.shared.dto.FieldMetaDto
import site.addzero.coding.playground.shared.dto.GenerationPlanDto
import site.addzero.coding.playground.shared.dto.GenerationRequestDto
import site.addzero.coding.playground.shared.dto.GenerationResultDto
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.MetadataImportResultDto
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.MetadataSnapshotDto
import site.addzero.coding.playground.shared.dto.ProjectAggregateDto
import site.addzero.coding.playground.shared.dto.ProjectMetaDto
import site.addzero.coding.playground.shared.dto.RelationMetaDto
import site.addzero.coding.playground.shared.dto.RenderedTemplateDto
import site.addzero.coding.playground.shared.dto.ReorderRequestDto
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.dto.UpdateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateDtoMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateProjectMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateRelationMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.ValidationIssueDto

interface PathVariableResolver {
    fun resolve(rawPath: String, variables: Map<String, String> = emptyMap()): String
}

interface MetadataSnapshotService {
    suspend fun exportProject(projectId: String): MetadataSnapshotDto
    suspend fun importSnapshot(snapshot: MetadataSnapshotDto): MetadataImportResultDto
}

interface GenerationPlanner {
    suspend fun plan(request: GenerationRequestDto): GenerationPlanDto
    suspend fun generate(request: GenerationRequestDto): GenerationResultDto
}

interface TemplateRenderer {
    suspend fun render(
        template: TemplateMetaDto,
        context: ContextAggregateDto,
        target: GenerationTargetMetaDto,
        variables: Map<String, String>,
    ): RenderedTemplateDto
}

interface EtlWrapperExecutor {
    suspend fun apply(
        wrapper: EtlWrapperMetaDto?,
        rendered: RenderedTemplateDto,
        variables: Map<String, String>,
    ): RenderedTemplateDto
}

interface CompositeBuildIntegrator {
    suspend fun integrate(
        targetRoot: String,
        includeBuildPath: String,
        marker: String = "CODING_PLAYGROUND",
    ): CompositeIntegrationResultDto
}

interface ProjectMetaService {
    suspend fun create(request: CreateProjectMetaRequest): ProjectMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<ProjectMetaDto>
    suspend fun get(id: String): ProjectMetaDto
    suspend fun update(id: String, request: UpdateProjectMetaRequest): ProjectMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun delete(id: String)
    suspend fun tree(id: String): ProjectAggregateDto
}

interface ContextMetaService {
    suspend fun create(request: CreateBoundedContextMetaRequest): BoundedContextMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<BoundedContextMetaDto>
    suspend fun get(id: String): BoundedContextMetaDto
    suspend fun aggregate(id: String): ContextAggregateDto
    suspend fun update(id: String, request: UpdateBoundedContextMetaRequest): BoundedContextMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun delete(id: String)
}

interface EntityMetaService {
    suspend fun create(request: CreateEntityMetaRequest): site.addzero.coding.playground.shared.dto.EntityMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<site.addzero.coding.playground.shared.dto.EntityMetaDto>
    suspend fun get(id: String): site.addzero.coding.playground.shared.dto.EntityMetaDto
    suspend fun update(id: String, request: UpdateEntityMetaRequest): site.addzero.coding.playground.shared.dto.EntityMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun delete(id: String)
    suspend fun createField(request: CreateFieldMetaRequest): FieldMetaDto
    suspend fun updateField(id: String, request: UpdateFieldMetaRequest): FieldMetaDto
    suspend fun deleteField(id: String)
    suspend fun reorderFields(entityId: String, request: ReorderRequestDto): List<FieldMetaDto>
    suspend fun createRelation(request: CreateRelationMetaRequest): RelationMetaDto
    suspend fun updateRelation(id: String, request: UpdateRelationMetaRequest): RelationMetaDto
    suspend fun deleteRelation(id: String)
}

interface DtoMetaService {
    suspend fun create(request: CreateDtoMetaRequest): DtoMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<DtoMetaDto>
    suspend fun get(id: String): DtoMetaDto
    suspend fun update(id: String, request: UpdateDtoMetaRequest): DtoMetaDto
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun delete(id: String)
    suspend fun createField(request: CreateDtoFieldMetaRequest): DtoFieldMetaDto
    suspend fun updateField(id: String, request: UpdateDtoFieldMetaRequest): DtoFieldMetaDto
    suspend fun deleteField(id: String)
    suspend fun reorderFields(dtoId: String, request: ReorderRequestDto): List<DtoFieldMetaDto>
}

interface TemplateMetaService {
    suspend fun create(request: CreateTemplateMetaRequest): TemplateMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<TemplateMetaDto>
    suspend fun get(id: String): TemplateMetaDto
    suspend fun update(id: String, request: UpdateTemplateMetaRequest): TemplateMetaDto
    suspend fun reorder(contextId: String, request: ReorderRequestDto): List<TemplateMetaDto>
    suspend fun deleteCheck(id: String): DeleteCheckResultDto
    suspend fun delete(id: String)
    suspend fun validate(id: String): List<ValidationIssueDto>
}

interface GenerationTargetMetaService {
    suspend fun create(request: CreateGenerationTargetMetaRequest): GenerationTargetMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<GenerationTargetMetaDto>
    suspend fun get(id: String): GenerationTargetMetaDto
    suspend fun update(id: String, request: UpdateGenerationTargetMetaRequest): GenerationTargetMetaDto
    suspend fun delete(id: String)
}

interface EtlWrapperMetaService {
    suspend fun create(request: CreateEtlWrapperMetaRequest): EtlWrapperMetaDto
    suspend fun list(search: MetadataSearchRequest = MetadataSearchRequest(nodeTypes = emptySet())): List<EtlWrapperMetaDto>
    suspend fun get(id: String): EtlWrapperMetaDto
    suspend fun update(id: String, request: UpdateEtlWrapperMetaRequest): EtlWrapperMetaDto
    suspend fun delete(id: String)
}
