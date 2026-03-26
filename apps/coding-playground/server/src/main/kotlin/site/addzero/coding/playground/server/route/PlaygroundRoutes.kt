package site.addzero.coding.playground.server.route

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.*
import site.addzero.coding.playground.shared.dto.*
import site.addzero.coding.playground.shared.service.*

private fun moduleService(): LlvmModuleService = KoinPlatform.getKoin().get()
private fun typeService(): LlvmTypeService = KoinPlatform.getKoin().get()
private fun globalValueService(): LlvmGlobalValueService = KoinPlatform.getKoin().get()
private fun functionService(): LlvmFunctionService = KoinPlatform.getKoin().get()
private fun metadataService(): LlvmMetadataService = KoinPlatform.getKoin().get()
private fun attributeService(): LlvmAttributeService = KoinPlatform.getKoin().get()
private fun validationService(): LlvmValidationService = KoinPlatform.getKoin().get()
private fun snapshotService(): LlvmSnapshotService = KoinPlatform.getKoin().get()
private fun exportService(): LlvmLlExportService = KoinPlatform.getKoin().get()
private fun compileProfileService(): LlvmCompileProfileService = KoinPlatform.getKoin().get()
private fun compileJobService(): LlvmCompileJobService = KoinPlatform.getKoin().get()

private fun buildSearch(
    query: String?,
    moduleId: String?,
    symbol: String?,
    opcode: LlvmInstructionOpcode?,
    typeKind: LlvmTypeKind?,
    linkage: LlvmLinkage?,
    metadataKind: LlvmMetadataKind?,
): LlvmSearchRequest {
    return LlvmSearchRequest(
        query = query,
        moduleId = moduleId,
        symbol = symbol,
        opcode = opcode,
        typeKind = typeKind,
        linkage = linkage,
        metadataKind = metadataKind,
    )
}

@GetMapping("/api/llvm-ir/modules")
suspend fun listModules(
    @RequestParam("query") query: String?,
): List<LlvmModuleDto> = moduleService().list(buildSearch(query, null, null, null, null, null, null))

@PostMapping("/api/llvm-ir/modules")
suspend fun createModule(@RequestBody request: CreateLlvmModuleRequest): LlvmModuleDto = moduleService().create(request)

@GetMapping("/api/llvm-ir/modules/{id}")
suspend fun getModule(@PathVariable id: String): LlvmModuleDto = moduleService().get(id)

@GetMapping("/api/llvm-ir/modules/{id}/aggregate")
suspend fun getModuleAggregate(@PathVariable id: String): LlvmModuleAggregateDto = moduleService().aggregate(id)

@PutMapping("/api/llvm-ir/modules/{id}")
suspend fun updateModule(@PathVariable id: String, @RequestBody request: UpdateLlvmModuleRequest): LlvmModuleDto =
    moduleService().update(id, request)

@GetMapping("/api/llvm-ir/modules/{id}/delete-check")
suspend fun deleteModuleCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = moduleService().deleteCheck(id)

@GetMapping("/api/llvm-ir/modules/{id}/validate")
suspend fun validateModule(@PathVariable id: String): List<LlvmValidationIssueDto> = validationService().validateModule(id)

@GetMapping("/api/llvm-ir/modules/{id}/export-ll")
suspend fun exportLl(@PathVariable id: String, @RequestParam("outputPath") outputPath: String?): LlvmLlExportDto =
    exportService().exportModule(id, outputPath)

@DeleteMapping("/api/llvm-ir/modules/{id}")
suspend fun deleteModule(@PathVariable id: String) = moduleService().delete(id)

@GetMapping("/api/llvm-ir/types")
suspend fun listTypes(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("symbol") symbol: String?,
    @RequestParam("typeKind") typeKind: LlvmTypeKind?,
): List<LlvmTypeDto> = typeService().list(buildSearch(query, moduleId, symbol, null, typeKind, null, null))

@PostMapping("/api/llvm-ir/types")
suspend fun createType(@RequestBody request: CreateLlvmTypeRequest): LlvmTypeDto = typeService().create(request)

@GetMapping("/api/llvm-ir/types/{id}")
suspend fun getType(@PathVariable id: String): LlvmTypeDto = typeService().get(id)

@PutMapping("/api/llvm-ir/types/{id}")
suspend fun updateType(@PathVariable id: String, @RequestBody request: UpdateLlvmTypeRequest): LlvmTypeDto =
    typeService().update(id, request)

@GetMapping("/api/llvm-ir/types/{id}/delete-check")
suspend fun deleteTypeCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = typeService().deleteCheck(id)

@GetMapping("/api/llvm-ir/types/{id}/validate")
suspend fun validateType(@PathVariable id: String): List<LlvmValidationIssueDto> = typeService().validate(id)

@DeleteMapping("/api/llvm-ir/types/{id}")
suspend fun deleteType(@PathVariable id: String) = typeService().delete(id)

@PostMapping("/api/llvm-ir/types/{typeId}/type-members")
suspend fun createTypeMember(@PathVariable typeId: String, @RequestBody request: CreateLlvmTypeMemberRequest): LlvmTypeMemberDto =
    typeService().createMember(request.copy(typeId = typeId))

@PutMapping("/api/llvm-ir/type-members/{id}")
suspend fun updateTypeMember(@PathVariable id: String, @RequestBody request: UpdateLlvmTypeMemberRequest): LlvmTypeMemberDto =
    typeService().updateMember(id, request)

@DeleteMapping("/api/llvm-ir/type-members/{id}")
suspend fun deleteTypeMember(@PathVariable id: String) = typeService().deleteMember(id)

@PostMapping("/api/llvm-ir/types/{typeId}/type-members/reorder")
suspend fun reorderTypeMembers(@PathVariable typeId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmTypeMemberDto> =
    typeService().reorderMembers(typeId, request)

@GetMapping("/api/llvm-ir/globals")
suspend fun listGlobals(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("symbol") symbol: String?,
    @RequestParam("linkage") linkage: LlvmLinkage?,
): List<LlvmGlobalVariableDto> = globalValueService().listGlobals(buildSearch(query, moduleId, symbol, null, null, linkage, null))

@PostMapping("/api/llvm-ir/globals")
suspend fun createGlobal(@RequestBody request: CreateLlvmGlobalVariableRequest): LlvmGlobalVariableDto =
    globalValueService().createGlobal(request)

@GetMapping("/api/llvm-ir/globals/{id}")
suspend fun getGlobal(@PathVariable id: String): LlvmGlobalVariableDto = globalValueService().getGlobal(id)

@PutMapping("/api/llvm-ir/globals/{id}")
suspend fun updateGlobal(@PathVariable id: String, @RequestBody request: UpdateLlvmGlobalVariableRequest): LlvmGlobalVariableDto =
    globalValueService().updateGlobal(id, request)

@GetMapping("/api/llvm-ir/globals/{id}/delete-check")
suspend fun deleteGlobalCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteGlobalCheck(id)

@DeleteMapping("/api/llvm-ir/globals/{id}")
suspend fun deleteGlobal(@PathVariable id: String) = globalValueService().deleteGlobal(id)

@GetMapping("/api/llvm-ir/constants")
suspend fun listConstants(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
): List<LlvmConstantDto> = globalValueService().listConstants(buildSearch(query, moduleId, null, null, null, null, null))

@PostMapping("/api/llvm-ir/constants")
suspend fun createConstant(@RequestBody request: CreateLlvmConstantRequest): LlvmConstantDto = globalValueService().createConstant(request)

@GetMapping("/api/llvm-ir/constants/{id}")
suspend fun getConstant(@PathVariable id: String): LlvmConstantDto = globalValueService().getConstant(id)

@PutMapping("/api/llvm-ir/constants/{id}")
suspend fun updateConstant(@PathVariable id: String, @RequestBody request: UpdateLlvmConstantRequest): LlvmConstantDto =
    globalValueService().updateConstant(id, request)

@GetMapping("/api/llvm-ir/constants/{id}/delete-check")
suspend fun deleteConstantCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteConstantCheck(id)

@DeleteMapping("/api/llvm-ir/constants/{id}")
suspend fun deleteConstant(@PathVariable id: String) = globalValueService().deleteConstant(id)

@PostMapping("/api/llvm-ir/constants/{constantId}/items")
suspend fun createConstantItem(@PathVariable constantId: String, @RequestBody request: CreateLlvmConstantItemRequest): LlvmConstantItemDto =
    globalValueService().createConstantItem(request.copy(constantId = constantId))

@PutMapping("/api/llvm-ir/constant-items/{id}")
suspend fun updateConstantItem(@PathVariable id: String, @RequestBody request: UpdateLlvmConstantItemRequest): LlvmConstantItemDto =
    globalValueService().updateConstantItem(id, request)

@DeleteMapping("/api/llvm-ir/constant-items/{id}")
suspend fun deleteConstantItem(@PathVariable id: String) = globalValueService().deleteConstantItem(id)

@PostMapping("/api/llvm-ir/constants/{constantId}/items/reorder")
suspend fun reorderConstantItems(@PathVariable constantId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmConstantItemDto> =
    globalValueService().reorderConstantItems(constantId, request)

@PostMapping("/api/llvm-ir/aliases")
suspend fun createAlias(@RequestBody request: CreateLlvmAliasRequest): LlvmAliasDto = globalValueService().createAlias(request)

@PutMapping("/api/llvm-ir/aliases/{id}")
suspend fun updateAlias(@PathVariable id: String, @RequestBody request: UpdateLlvmAliasRequest): LlvmAliasDto =
    globalValueService().updateAlias(id, request)

@GetMapping("/api/llvm-ir/aliases/{id}/delete-check")
suspend fun deleteAliasCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteAliasCheck(id)

@DeleteMapping("/api/llvm-ir/aliases/{id}")
suspend fun deleteAlias(@PathVariable id: String) = globalValueService().deleteAlias(id)

@PostMapping("/api/llvm-ir/ifuncs")
suspend fun createIfunc(@RequestBody request: CreateLlvmIfuncRequest): LlvmIfuncDto = globalValueService().createIfunc(request)

@PutMapping("/api/llvm-ir/ifuncs/{id}")
suspend fun updateIfunc(@PathVariable id: String, @RequestBody request: UpdateLlvmIfuncRequest): LlvmIfuncDto =
    globalValueService().updateIfunc(id, request)

@GetMapping("/api/llvm-ir/ifuncs/{id}/delete-check")
suspend fun deleteIfuncCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteIfuncCheck(id)

@DeleteMapping("/api/llvm-ir/ifuncs/{id}")
suspend fun deleteIfunc(@PathVariable id: String) = globalValueService().deleteIfunc(id)

@PostMapping("/api/llvm-ir/comdats")
suspend fun createComdat(@RequestBody request: CreateLlvmComdatRequest): LlvmComdatDto = globalValueService().createComdat(request)

@PutMapping("/api/llvm-ir/comdats/{id}")
suspend fun updateComdat(@PathVariable id: String, @RequestBody request: UpdateLlvmComdatRequest): LlvmComdatDto =
    globalValueService().updateComdat(id, request)

@GetMapping("/api/llvm-ir/comdats/{id}/delete-check")
suspend fun deleteComdatCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteComdatCheck(id)

@DeleteMapping("/api/llvm-ir/comdats/{id}")
suspend fun deleteComdat(@PathVariable id: String) = globalValueService().deleteComdat(id)

@PostMapping("/api/llvm-ir/inline-asms")
suspend fun createInlineAsm(@RequestBody request: CreateLlvmInlineAsmRequest): LlvmInlineAsmDto = globalValueService().createInlineAsm(request)

@PutMapping("/api/llvm-ir/inline-asms/{id}")
suspend fun updateInlineAsm(@PathVariable id: String, @RequestBody request: UpdateLlvmInlineAsmRequest): LlvmInlineAsmDto =
    globalValueService().updateInlineAsm(id, request)

@GetMapping("/api/llvm-ir/inline-asms/{id}/delete-check")
suspend fun deleteInlineAsmCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = globalValueService().deleteInlineAsmCheck(id)

@DeleteMapping("/api/llvm-ir/inline-asms/{id}")
suspend fun deleteInlineAsm(@PathVariable id: String) = globalValueService().deleteInlineAsm(id)

@GetMapping("/api/llvm-ir/functions")
suspend fun listFunctions(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("symbol") symbol: String?,
    @RequestParam("linkage") linkage: LlvmLinkage?,
): List<LlvmFunctionDto> = functionService().list(buildSearch(query, moduleId, symbol, null, null, linkage, null))

@PostMapping("/api/llvm-ir/functions")
suspend fun createFunction(@RequestBody request: CreateLlvmFunctionRequest): LlvmFunctionDto = functionService().create(request)

@GetMapping("/api/llvm-ir/functions/{id}")
suspend fun getFunction(@PathVariable id: String): LlvmFunctionDto = functionService().get(id)

@PutMapping("/api/llvm-ir/functions/{id}")
suspend fun updateFunction(@PathVariable id: String, @RequestBody request: UpdateLlvmFunctionRequest): LlvmFunctionDto =
    functionService().update(id, request)

@GetMapping("/api/llvm-ir/functions/{id}/delete-check")
suspend fun deleteFunctionCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = functionService().deleteCheck(id)

@GetMapping("/api/llvm-ir/functions/{id}/validate")
suspend fun validateFunction(@PathVariable id: String): List<LlvmValidationIssueDto> = functionService().validate(id)

@DeleteMapping("/api/llvm-ir/functions/{id}")
suspend fun deleteFunction(@PathVariable id: String) = functionService().delete(id)

@PostMapping("/api/llvm-ir/functions/{functionId}/params")
suspend fun createParam(@PathVariable functionId: String, @RequestBody request: CreateLlvmFunctionParamRequest): LlvmFunctionParamDto =
    functionService().createParam(request.copy(functionId = functionId))

@PutMapping("/api/llvm-ir/params/{id}")
suspend fun updateParam(@PathVariable id: String, @RequestBody request: UpdateLlvmFunctionParamRequest): LlvmFunctionParamDto =
    functionService().updateParam(id, request)

@DeleteMapping("/api/llvm-ir/params/{id}")
suspend fun deleteParam(@PathVariable id: String) = functionService().deleteParam(id)

@PostMapping("/api/llvm-ir/functions/{functionId}/params/reorder")
suspend fun reorderParams(@PathVariable functionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmFunctionParamDto> =
    functionService().reorderParams(functionId, request)

@PostMapping("/api/llvm-ir/functions/{functionId}/blocks")
suspend fun createBlock(@PathVariable functionId: String, @RequestBody request: CreateLlvmBasicBlockRequest): LlvmBasicBlockDto =
    functionService().createBlock(request.copy(functionId = functionId))

@PutMapping("/api/llvm-ir/blocks/{id}")
suspend fun updateBlock(@PathVariable id: String, @RequestBody request: UpdateLlvmBasicBlockRequest): LlvmBasicBlockDto =
    functionService().updateBlock(id, request)

@GetMapping("/api/llvm-ir/blocks/{id}/delete-check")
suspend fun deleteBlockCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = functionService().deleteBlockCheck(id)

@DeleteMapping("/api/llvm-ir/blocks/{id}")
suspend fun deleteBlock(@PathVariable id: String) = functionService().deleteBlock(id)

@PostMapping("/api/llvm-ir/functions/{functionId}/blocks/reorder")
suspend fun reorderBlocks(@PathVariable functionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmBasicBlockDto> =
    functionService().reorderBlocks(functionId, request)

@PostMapping("/api/llvm-ir/blocks/{blockId}/instructions")
suspend fun createInstruction(@PathVariable blockId: String, @RequestBody request: CreateLlvmInstructionRequest): LlvmInstructionDto =
    functionService().createInstruction(request.copy(blockId = blockId))

@PutMapping("/api/llvm-ir/instructions/{id}")
suspend fun updateInstruction(@PathVariable id: String, @RequestBody request: UpdateLlvmInstructionRequest): LlvmInstructionDto =
    functionService().updateInstruction(id, request)

@GetMapping("/api/llvm-ir/instructions/{id}/delete-check")
suspend fun deleteInstructionCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = functionService().deleteInstructionCheck(id)

@DeleteMapping("/api/llvm-ir/instructions/{id}")
suspend fun deleteInstruction(@PathVariable id: String) = functionService().deleteInstruction(id)

@PostMapping("/api/llvm-ir/blocks/{blockId}/instructions/reorder")
suspend fun reorderInstructions(@PathVariable blockId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmInstructionDto> =
    functionService().reorderInstructions(blockId, request)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/operands")
suspend fun createOperand(@PathVariable instructionId: String, @RequestBody request: CreateLlvmOperandRequest): LlvmOperandDto =
    functionService().createOperand(request.copy(instructionId = instructionId))

@PutMapping("/api/llvm-ir/operands/{id}")
suspend fun updateOperand(@PathVariable id: String, @RequestBody request: UpdateLlvmOperandRequest): LlvmOperandDto =
    functionService().updateOperand(id, request)

@DeleteMapping("/api/llvm-ir/operands/{id}")
suspend fun deleteOperand(@PathVariable id: String) = functionService().deleteOperand(id)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/operands/reorder")
suspend fun reorderOperands(@PathVariable instructionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmOperandDto> =
    functionService().reorderOperands(instructionId, request)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/phi-incomings")
suspend fun createPhiIncoming(@PathVariable instructionId: String, @RequestBody request: CreateLlvmPhiIncomingRequest): LlvmPhiIncomingDto =
    functionService().createPhiIncoming(request.copy(instructionId = instructionId))

@PutMapping("/api/llvm-ir/phi-incomings/{id}")
suspend fun updatePhiIncoming(@PathVariable id: String, @RequestBody request: UpdateLlvmPhiIncomingRequest): LlvmPhiIncomingDto =
    functionService().updatePhiIncoming(id, request)

@DeleteMapping("/api/llvm-ir/phi-incomings/{id}")
suspend fun deletePhiIncoming(@PathVariable id: String) = functionService().deletePhiIncoming(id)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/phi-incomings/reorder")
suspend fun reorderPhiIncoming(@PathVariable instructionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmPhiIncomingDto> =
    functionService().reorderPhiIncoming(instructionId, request)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/clauses")
suspend fun createClause(@PathVariable instructionId: String, @RequestBody request: CreateLlvmInstructionClauseRequest): LlvmInstructionClauseDto =
    functionService().createClause(request.copy(instructionId = instructionId))

@PutMapping("/api/llvm-ir/instruction-clauses/{id}")
suspend fun updateClause(@PathVariable id: String, @RequestBody request: UpdateLlvmInstructionClauseRequest): LlvmInstructionClauseDto =
    functionService().updateClause(id, request)

@DeleteMapping("/api/llvm-ir/instruction-clauses/{id}")
suspend fun deleteClause(@PathVariable id: String) = functionService().deleteClause(id)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/clauses/reorder")
suspend fun reorderClauses(@PathVariable instructionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmInstructionClauseDto> =
    functionService().reorderClauses(instructionId, request)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/bundles")
suspend fun createBundle(@PathVariable instructionId: String, @RequestBody request: CreateLlvmOperandBundleRequest): LlvmOperandBundleDto =
    functionService().createBundle(request.copy(instructionId = instructionId))

@PutMapping("/api/llvm-ir/operand-bundles/{id}")
suspend fun updateBundle(@PathVariable id: String, @RequestBody request: UpdateLlvmOperandBundleRequest): LlvmOperandBundleDto =
    functionService().updateBundle(id, request)

@DeleteMapping("/api/llvm-ir/operand-bundles/{id}")
suspend fun deleteBundle(@PathVariable id: String) = functionService().deleteBundle(id)

@PostMapping("/api/llvm-ir/instructions/{instructionId}/bundles/reorder")
suspend fun reorderBundles(@PathVariable instructionId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmOperandBundleDto> =
    functionService().reorderBundles(instructionId, request)

@GetMapping("/api/llvm-ir/metadata/named")
suspend fun listNamedMetadata(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
): List<LlvmNamedMetadataDto> = metadataService().listNamed(buildSearch(query, moduleId, null, null, null, null, null))

@PostMapping("/api/llvm-ir/metadata/named")
suspend fun createNamedMetadata(@RequestBody request: CreateLlvmNamedMetadataRequest): LlvmNamedMetadataDto =
    metadataService().createNamed(request)

@GetMapping("/api/llvm-ir/metadata/named/{id}")
suspend fun getNamedMetadata(@PathVariable id: String): LlvmNamedMetadataDto = metadataService().getNamed(id)

@PutMapping("/api/llvm-ir/metadata/named/{id}")
suspend fun updateNamedMetadata(@PathVariable id: String, @RequestBody request: UpdateLlvmNamedMetadataRequest): LlvmNamedMetadataDto =
    metadataService().updateNamed(id, request)

@GetMapping("/api/llvm-ir/metadata/named/{id}/delete-check")
suspend fun deleteNamedMetadataCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = metadataService().deleteNamedCheck(id)

@DeleteMapping("/api/llvm-ir/metadata/named/{id}")
suspend fun deleteNamedMetadata(@PathVariable id: String) = metadataService().deleteNamed(id)

@PostMapping("/api/llvm-ir/metadata/named/{id}/reorder")
suspend fun reorderNamedMetadataFields(@PathVariable id: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto> =
    metadataService().reorderNamedFields(id, request)

@GetMapping("/api/llvm-ir/metadata/nodes")
suspend fun listMetadataNodes(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
    @RequestParam("metadataKind") metadataKind: LlvmMetadataKind?,
): List<LlvmMetadataNodeDto> = metadataService().listNodes(buildSearch(query, moduleId, null, null, null, null, metadataKind))

@PostMapping("/api/llvm-ir/metadata/nodes")
suspend fun createMetadataNode(@RequestBody request: CreateLlvmMetadataNodeRequest): LlvmMetadataNodeDto = metadataService().createNode(request)

@GetMapping("/api/llvm-ir/metadata/nodes/{id}")
suspend fun getMetadataNode(@PathVariable id: String): LlvmMetadataNodeDto = metadataService().getNode(id)

@PutMapping("/api/llvm-ir/metadata/nodes/{id}")
suspend fun updateMetadataNode(@PathVariable id: String, @RequestBody request: UpdateLlvmMetadataNodeRequest): LlvmMetadataNodeDto =
    metadataService().updateNode(id, request)

@GetMapping("/api/llvm-ir/metadata/nodes/{id}/delete-check")
suspend fun deleteMetadataNodeCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = metadataService().deleteNodeCheck(id)

@DeleteMapping("/api/llvm-ir/metadata/nodes/{id}")
suspend fun deleteMetadataNode(@PathVariable id: String) = metadataService().deleteNode(id)

@PostMapping("/api/llvm-ir/metadata/nodes/{id}/reorder")
suspend fun reorderMetadataNodeFields(@PathVariable id: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto> =
    metadataService().reorderNodeFields(id, request)

@PostMapping("/api/llvm-ir/metadata/fields")
suspend fun createMetadataField(@RequestBody request: CreateLlvmMetadataFieldRequest): LlvmMetadataFieldDto = metadataService().createField(request)

@PutMapping("/api/llvm-ir/metadata/fields/{id}")
suspend fun updateMetadataField(@PathVariable id: String, @RequestBody request: UpdateLlvmMetadataFieldRequest): LlvmMetadataFieldDto =
    metadataService().updateField(id, request)

@DeleteMapping("/api/llvm-ir/metadata/fields/{id}")
suspend fun deleteMetadataField(@PathVariable id: String) = metadataService().deleteField(id)

@PostMapping("/api/llvm-ir/metadata/attachments")
suspend fun createMetadataAttachment(@RequestBody request: CreateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto =
    metadataService().createAttachment(request)

@PutMapping("/api/llvm-ir/metadata/attachments/{id}")
suspend fun updateMetadataAttachment(@PathVariable id: String, @RequestBody request: UpdateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto =
    metadataService().updateAttachment(id, request)

@DeleteMapping("/api/llvm-ir/metadata/attachments/{id}")
suspend fun deleteMetadataAttachment(@PathVariable id: String) = metadataService().deleteAttachment(id)

@GetMapping("/api/llvm-ir/attribute-groups")
suspend fun listAttributeGroups(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
): List<LlvmAttributeGroupDto> = attributeService().listGroups(buildSearch(query, moduleId, null, null, null, null, null))

@PostMapping("/api/llvm-ir/attribute-groups")
suspend fun createAttributeGroup(@RequestBody request: CreateLlvmAttributeGroupRequest): LlvmAttributeGroupDto = attributeService().createGroup(request)

@GetMapping("/api/llvm-ir/attribute-groups/{id}")
suspend fun getAttributeGroup(@PathVariable id: String): LlvmAttributeGroupDto = attributeService().getGroup(id)

@PutMapping("/api/llvm-ir/attribute-groups/{id}")
suspend fun updateAttributeGroup(@PathVariable id: String, @RequestBody request: UpdateLlvmAttributeGroupRequest): LlvmAttributeGroupDto =
    attributeService().updateGroup(id, request)

@GetMapping("/api/llvm-ir/attribute-groups/{id}/delete-check")
suspend fun deleteAttributeGroupCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = attributeService().deleteGroupCheck(id)

@DeleteMapping("/api/llvm-ir/attribute-groups/{id}")
suspend fun deleteAttributeGroup(@PathVariable id: String) = attributeService().deleteGroup(id)

@PostMapping("/api/llvm-ir/attribute-groups/{groupId}/attribute-entries")
suspend fun createAttributeEntry(@PathVariable groupId: String, @RequestBody request: CreateLlvmAttributeEntryRequest): LlvmAttributeEntryDto =
    attributeService().createEntry(request.copy(attributeGroupId = groupId))

@PutMapping("/api/llvm-ir/attribute-entries/{id}")
suspend fun updateAttributeEntry(@PathVariable id: String, @RequestBody request: UpdateLlvmAttributeEntryRequest): LlvmAttributeEntryDto =
    attributeService().updateEntry(id, request)

@DeleteMapping("/api/llvm-ir/attribute-entries/{id}")
suspend fun deleteAttributeEntry(@PathVariable id: String) = attributeService().deleteEntry(id)

@PostMapping("/api/llvm-ir/attribute-groups/{groupId}/attribute-entries/reorder")
suspend fun reorderAttributeEntries(@PathVariable groupId: String, @RequestBody request: LlvmReorderRequestDto): List<LlvmAttributeEntryDto> =
    attributeService().reorderEntries(groupId, request)

@GetMapping("/api/llvm-ir/compile/profiles")
suspend fun listCompileProfiles(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
): List<LlvmCompileProfileDto> = compileProfileService().list(buildSearch(query, moduleId, null, null, null, null, null))

@PostMapping("/api/llvm-ir/compile/profiles")
suspend fun createCompileProfile(@RequestBody request: CreateLlvmCompileProfileRequest): LlvmCompileProfileDto =
    compileProfileService().create(request)

@GetMapping("/api/llvm-ir/compile/profiles/{id}")
suspend fun getCompileProfile(@PathVariable id: String): LlvmCompileProfileDto = compileProfileService().get(id)

@PutMapping("/api/llvm-ir/compile/profiles/{id}")
suspend fun updateCompileProfile(@PathVariable id: String, @RequestBody request: UpdateLlvmCompileProfileRequest): LlvmCompileProfileDto =
    compileProfileService().update(id, request)

@GetMapping("/api/llvm-ir/compile/profiles/{id}/delete-check")
suspend fun deleteCompileProfileCheck(@PathVariable id: String): LlvmDeleteCheckResultDto = compileProfileService().deleteCheck(id)

@GetMapping("/api/llvm-ir/compile/profiles/{id}/validate")
suspend fun validateCompileProfile(@PathVariable id: String): List<LlvmValidationIssueDto> = compileProfileService().validate(id)

@DeleteMapping("/api/llvm-ir/compile/profiles/{id}")
suspend fun deleteCompileProfile(@PathVariable id: String) = compileProfileService().delete(id)

@GetMapping("/api/llvm-ir/compile/jobs")
suspend fun listCompileJobs(
    @RequestParam("moduleId") moduleId: String?,
    @RequestParam("query") query: String?,
): List<LlvmCompileJobDto> = compileJobService().list(buildSearch(query, moduleId, null, null, null, null, null))

@PostMapping("/api/llvm-ir/compile/jobs")
suspend fun createCompileJob(@RequestBody request: CreateLlvmCompileJobRequest): LlvmCompileJobDto = compileJobService().create(request)

@GetMapping("/api/llvm-ir/compile/jobs/{id}")
suspend fun getCompileJob(@PathVariable id: String): LlvmCompileJobDto = compileJobService().get(id)

@PostMapping("/api/llvm-ir/compile/jobs/{id}/execute")
suspend fun executeCompileJob(@PathVariable id: String): LlvmCompileExecutionResultDto = compileJobService().execute(id)

@GetMapping("/api/llvm-ir/compile/jobs/{id}/artifacts")
suspend fun listCompileArtifacts(@PathVariable id: String): List<LlvmCompileArtifactDto> = compileJobService().listArtifacts(id)

@DeleteMapping("/api/llvm-ir/compile/jobs/{id}")
suspend fun deleteCompileJob(@PathVariable id: String) = compileJobService().delete(id)

@GetMapping("/api/llvm-ir/snapshots/modules/{moduleId}")
suspend fun exportModuleSnapshot(@PathVariable moduleId: String): LlvmSnapshotDto = snapshotService().exportModule(moduleId)

@PostMapping("/api/llvm-ir/snapshots/import")
suspend fun importModuleSnapshot(@RequestBody snapshot: LlvmSnapshotDto): LlvmSnapshotImportResultDto = snapshotService().importSnapshot(snapshot)
