package site.addzero.coding.playground.server.route

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*

private fun projectService(): ProjectMetaService = KoinPlatform.getKoin().get()
private fun contextService(): ContextMetaService = KoinPlatform.getKoin().get()
private fun entityService(): EntityMetaService = KoinPlatform.getKoin().get()
private fun dtoService(): DtoMetaService = KoinPlatform.getKoin().get()
private fun templateService(): TemplateMetaService = KoinPlatform.getKoin().get()
private fun targetService(): GenerationTargetMetaService = KoinPlatform.getKoin().get()
private fun etlService(): EtlWrapperMetaService = KoinPlatform.getKoin().get()
private fun snapshotService(): MetadataSnapshotService = KoinPlatform.getKoin().get()
private fun generationPlanner(): GenerationPlanner = KoinPlatform.getKoin().get()
private fun support(): MetadataPersistenceSupport = KoinPlatform.getKoin().get()

private fun buildSearch(
    query: String?,
    projectId: String?,
    contextId: String?,
    tag: String?,
): MetadataSearchRequest {
    return MetadataSearchRequest(
        query = query,
        projectId = projectId,
        contextId = contextId,
        tag = tag,
    )
}

@GetMapping("/api/playground/projects")
suspend fun listProjects(
    @RequestParam("query") query: String?,
    @RequestParam("tag") tag: String?,
): List<ProjectMetaDto> = projectService().list(buildSearch(query, null, null, tag))

@PostMapping("/api/playground/projects")
suspend fun createProject(
    @RequestBody request: CreateProjectMetaRequest,
): ProjectMetaDto = projectService().create(request)

@GetMapping("/api/playground/projects/{id}")
suspend fun getProject(
    @PathVariable id: String,
): ProjectMetaDto = projectService().get(id)

@PutMapping("/api/playground/projects/{id}")
suspend fun updateProject(
    @PathVariable id: String,
    @RequestBody request: UpdateProjectMetaRequest,
): ProjectMetaDto = projectService().update(id, request)

@GetMapping("/api/playground/projects/{id}/tree")
suspend fun getProjectTree(
    @PathVariable id: String,
): ProjectAggregateDto = projectService().tree(id)

@GetMapping("/api/playground/projects/{id}/delete-check")
suspend fun checkProjectDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = projectService().deleteCheck(id)

@DeleteMapping("/api/playground/projects/{id}")
suspend fun deleteProject(
    @PathVariable id: String,
) = projectService().delete(id)

@GetMapping("/api/playground/contexts")
suspend fun listContexts(
    @RequestParam("projectId") projectId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("tag") tag: String?,
): List<BoundedContextMetaDto> = contextService().list(buildSearch(query, projectId, null, tag))

@PostMapping("/api/playground/contexts")
suspend fun createContext(
    @RequestBody request: CreateBoundedContextMetaRequest,
): BoundedContextMetaDto = contextService().create(request)

@GetMapping("/api/playground/contexts/{id}")
suspend fun getContext(
    @PathVariable id: String,
): BoundedContextMetaDto = contextService().get(id)

@PutMapping("/api/playground/contexts/{id}")
suspend fun updateContext(
    @PathVariable id: String,
    @RequestBody request: UpdateBoundedContextMetaRequest,
): BoundedContextMetaDto = contextService().update(id, request)

@GetMapping("/api/playground/contexts/{id}/aggregate")
suspend fun getContextAggregate(
    @PathVariable id: String,
): ContextAggregateDto = contextService().aggregate(id)

@GetMapping("/api/playground/contexts/{id}/delete-check")
suspend fun checkContextDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = contextService().deleteCheck(id)

@DeleteMapping("/api/playground/contexts/{id}")
suspend fun deleteContext(
    @PathVariable id: String,
) = contextService().delete(id)

@GetMapping("/api/playground/entities")
suspend fun listEntities(
    @RequestParam("contextId") contextId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("tag") tag: String?,
): List<EntityMetaDto> = entityService().list(buildSearch(query, null, contextId, tag))

@PostMapping("/api/playground/entities")
suspend fun createEntity(
    @RequestBody request: CreateEntityMetaRequest,
): EntityMetaDto = entityService().create(request)

@GetMapping("/api/playground/entities/{id}")
suspend fun getEntity(
    @PathVariable id: String,
): EntityMetaDto = entityService().get(id)

@PutMapping("/api/playground/entities/{id}")
suspend fun updateEntity(
    @PathVariable id: String,
    @RequestBody request: UpdateEntityMetaRequest,
): EntityMetaDto = entityService().update(id, request)

@GetMapping("/api/playground/entities/{id}/delete-check")
suspend fun checkEntityDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = entityService().deleteCheck(id)

@DeleteMapping("/api/playground/entities/{id}")
suspend fun deleteEntity(
    @PathVariable id: String,
) = entityService().delete(id)

@GetMapping("/api/playground/entities/{id}/fields")
suspend fun listEntityFields(
    @PathVariable id: String,
): List<FieldMetaDto> = support().listFields(id).map { it.toDto() }

@PostMapping("/api/playground/entities/{entityId}/fields")
suspend fun createEntityField(
    @PathVariable entityId: String,
    @RequestBody request: CreateFieldMetaRequest,
): FieldMetaDto = entityService().createField(request.copy(entityId = entityId))

@PutMapping("/api/playground/fields/{id}")
suspend fun updateField(
    @PathVariable id: String,
    @RequestBody request: UpdateFieldMetaRequest,
): FieldMetaDto = entityService().updateField(id, request)

@DeleteMapping("/api/playground/fields/{id}")
suspend fun deleteField(
    @PathVariable id: String,
) = entityService().deleteField(id)

@PostMapping("/api/playground/entities/{entityId}/fields/reorder")
suspend fun reorderFields(
    @PathVariable entityId: String,
    @RequestBody request: ReorderRequestDto,
): List<FieldMetaDto> = entityService().reorderFields(entityId, request)

@GetMapping("/api/playground/contexts/{contextId}/relations")
suspend fun listRelations(
    @PathVariable contextId: String,
): List<RelationMetaDto> = support().listRelations(contextId).map { it.toDto() }

@PostMapping("/api/playground/relations")
suspend fun createRelation(
    @RequestBody request: CreateRelationMetaRequest,
): RelationMetaDto = entityService().createRelation(request)

@PutMapping("/api/playground/relations/{id}")
suspend fun updateRelation(
    @PathVariable id: String,
    @RequestBody request: UpdateRelationMetaRequest,
): RelationMetaDto = entityService().updateRelation(id, request)

@DeleteMapping("/api/playground/relations/{id}")
suspend fun deleteRelation(
    @PathVariable id: String,
) = entityService().deleteRelation(id)

@GetMapping("/api/playground/dtos")
suspend fun listDtos(
    @RequestParam("contextId") contextId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("tag") tag: String?,
): List<DtoMetaDto> = dtoService().list(buildSearch(query, null, contextId, tag))

@PostMapping("/api/playground/dtos")
suspend fun createDto(
    @RequestBody request: CreateDtoMetaRequest,
): DtoMetaDto = dtoService().create(request)

@GetMapping("/api/playground/dtos/{id}")
suspend fun getDto(
    @PathVariable id: String,
): DtoMetaDto = dtoService().get(id)

@PutMapping("/api/playground/dtos/{id}")
suspend fun updateDto(
    @PathVariable id: String,
    @RequestBody request: UpdateDtoMetaRequest,
): DtoMetaDto = dtoService().update(id, request)

@GetMapping("/api/playground/dtos/{id}/delete-check")
suspend fun checkDtoDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = dtoService().deleteCheck(id)

@DeleteMapping("/api/playground/dtos/{id}")
suspend fun deleteDto(
    @PathVariable id: String,
) = dtoService().delete(id)

@GetMapping("/api/playground/dtos/{id}/fields")
suspend fun listDtoFields(
    @PathVariable id: String,
): List<DtoFieldMetaDto> = support().listDtoFields(id).map { it.toDto() }

@PostMapping("/api/playground/dtos/{dtoId}/fields")
suspend fun createDtoField(
    @PathVariable dtoId: String,
    @RequestBody request: CreateDtoFieldMetaRequest,
): DtoFieldMetaDto = dtoService().createField(request.copy(dtoId = dtoId))

@PutMapping("/api/playground/dto-fields/{id}")
suspend fun updateDtoField(
    @PathVariable id: String,
    @RequestBody request: UpdateDtoFieldMetaRequest,
): DtoFieldMetaDto = dtoService().updateField(id, request)

@DeleteMapping("/api/playground/dto-fields/{id}")
suspend fun deleteDtoField(
    @PathVariable id: String,
) = dtoService().deleteField(id)

@PostMapping("/api/playground/dtos/{dtoId}/fields/reorder")
suspend fun reorderDtoFields(
    @PathVariable dtoId: String,
    @RequestBody request: ReorderRequestDto,
): List<DtoFieldMetaDto> = dtoService().reorderFields(dtoId, request)

@GetMapping("/api/playground/templates")
suspend fun listTemplates(
    @RequestParam("contextId") contextId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("tag") tag: String?,
): List<TemplateMetaDto> = templateService().list(buildSearch(query, null, contextId, tag))

@PostMapping("/api/playground/templates")
suspend fun createTemplate(
    @RequestBody request: CreateTemplateMetaRequest,
): TemplateMetaDto = templateService().create(request)

@GetMapping("/api/playground/templates/{id}")
suspend fun getTemplate(
    @PathVariable id: String,
): TemplateMetaDto = templateService().get(id)

@PutMapping("/api/playground/templates/{id}")
suspend fun updateTemplate(
    @PathVariable id: String,
    @RequestBody request: UpdateTemplateMetaRequest,
): TemplateMetaDto = templateService().update(id, request)

@GetMapping("/api/playground/templates/{id}/delete-check")
suspend fun checkTemplateDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = templateService().deleteCheck(id)

@GetMapping("/api/playground/templates/{id}/validate")
suspend fun validateTemplate(
    @PathVariable id: String,
): List<ValidationIssueDto> = templateService().validate(id)

@DeleteMapping("/api/playground/templates/{id}")
suspend fun deleteTemplate(
    @PathVariable id: String,
) = templateService().delete(id)

@PostMapping("/api/playground/contexts/{contextId}/templates/reorder")
suspend fun reorderTemplates(
    @PathVariable contextId: String,
    @RequestBody request: ReorderRequestDto,
): List<TemplateMetaDto> = templateService().reorder(contextId, request)

@GetMapping("/api/playground/generation-targets")
suspend fun listGenerationTargets(
    @RequestParam("projectId") projectId: String?,
    @RequestParam("contextId") contextId: String?,
    @RequestParam("query") query: String?,
): List<GenerationTargetMetaDto> = targetService().list(buildSearch(query, projectId, contextId, null))

@PostMapping("/api/playground/generation-targets")
suspend fun createGenerationTarget(
    @RequestBody request: CreateGenerationTargetMetaRequest,
): GenerationTargetMetaDto = targetService().create(request)

@GetMapping("/api/playground/generation-targets/{id}")
suspend fun getGenerationTarget(
    @PathVariable id: String,
): GenerationTargetMetaDto = targetService().get(id)

@PutMapping("/api/playground/generation-targets/{id}")
suspend fun updateGenerationTarget(
    @PathVariable id: String,
    @RequestBody request: UpdateGenerationTargetMetaRequest,
): GenerationTargetMetaDto = targetService().update(id, request)

@GetMapping("/api/playground/generation-targets/{id}/delete-check")
suspend fun checkGenerationTargetDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = targetService().deleteCheck(id)

@GetMapping("/api/playground/generation-targets/{id}/validate")
suspend fun validateGenerationTarget(
    @PathVariable id: String,
): List<ValidationIssueDto> = targetService().validate(id)

@DeleteMapping("/api/playground/generation-targets/{id}")
suspend fun deleteGenerationTarget(
    @PathVariable id: String,
) = targetService().delete(id)

@GetMapping("/api/playground/etl-wrappers")
suspend fun listEtlWrappers(
    @RequestParam("projectId") projectId: String?,
    @RequestParam("query") query: String?,
): List<EtlWrapperMetaDto> = etlService().list(buildSearch(query, projectId, null, null))

@PostMapping("/api/playground/etl-wrappers")
suspend fun createEtlWrapper(
    @RequestBody request: CreateEtlWrapperMetaRequest,
): EtlWrapperMetaDto = etlService().create(request)

@GetMapping("/api/playground/etl-wrappers/{id}")
suspend fun getEtlWrapper(
    @PathVariable id: String,
): EtlWrapperMetaDto = etlService().get(id)

@PutMapping("/api/playground/etl-wrappers/{id}")
suspend fun updateEtlWrapper(
    @PathVariable id: String,
    @RequestBody request: UpdateEtlWrapperMetaRequest,
): EtlWrapperMetaDto = etlService().update(id, request)

@GetMapping("/api/playground/etl-wrappers/{id}/delete-check")
suspend fun checkEtlWrapperDelete(
    @PathVariable id: String,
): DeleteCheckResultDto = etlService().deleteCheck(id)

@GetMapping("/api/playground/etl-wrappers/{id}/validate")
suspend fun validateEtlWrapper(
    @PathVariable id: String,
): List<ValidationIssueDto> = etlService().validate(id)

@DeleteMapping("/api/playground/etl-wrappers/{id}")
suspend fun deleteEtlWrapper(
    @PathVariable id: String,
) = etlService().delete(id)

@GetMapping("/api/playground/snapshots/projects/{projectId}")
suspend fun exportProjectSnapshot(
    @PathVariable projectId: String,
): MetadataSnapshotDto = snapshotService().exportProject(projectId)

@PostMapping("/api/playground/snapshots/import")
suspend fun importProjectSnapshot(
    @RequestBody snapshot: MetadataSnapshotDto,
): MetadataImportResultDto = snapshotService().importSnapshot(snapshot)

@PostMapping("/api/playground/generation/plan")
suspend fun planGeneration(
    @RequestBody request: GenerationRequestDto,
): GenerationPlanDto = generationPlanner().plan(request)

@PostMapping("/api/playground/generation/execute")
suspend fun executeGeneration(
    @RequestBody request: GenerationRequestDto,
): GenerationResultDto = generationPlanner().generate(request)
