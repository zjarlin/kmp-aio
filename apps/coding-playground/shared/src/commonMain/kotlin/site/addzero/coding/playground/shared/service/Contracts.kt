package site.addzero.coding.playground.shared.service

import site.addzero.coding.playground.shared.dto.*

interface LlvmModuleService {
    suspend fun create(request: CreateLlvmModuleRequest): LlvmModuleDto
    suspend fun list(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmModuleDto>
    suspend fun get(id: String): LlvmModuleDto
    suspend fun aggregate(id: String): LlvmModuleAggregateDto
    suspend fun update(id: String, request: UpdateLlvmModuleRequest): LlvmModuleDto
    suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun validate(id: String): List<LlvmValidationIssueDto>
    suspend fun delete(id: String)
}

interface LlvmTypeService {
    suspend fun create(request: CreateLlvmTypeRequest): LlvmTypeDto
    suspend fun list(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmTypeDto>
    suspend fun get(id: String): LlvmTypeDto
    suspend fun update(id: String, request: UpdateLlvmTypeRequest): LlvmTypeDto
    suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun validate(id: String): List<LlvmValidationIssueDto>
    suspend fun delete(id: String)
    suspend fun createMember(request: CreateLlvmTypeMemberRequest): LlvmTypeMemberDto
    suspend fun updateMember(id: String, request: UpdateLlvmTypeMemberRequest): LlvmTypeMemberDto
    suspend fun deleteMember(id: String)
    suspend fun reorderMembers(typeId: String, request: LlvmReorderRequestDto): List<LlvmTypeMemberDto>
}

interface LlvmGlobalValueService {
    suspend fun createGlobal(request: CreateLlvmGlobalVariableRequest): LlvmGlobalVariableDto
    suspend fun listGlobals(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmGlobalVariableDto>
    suspend fun getGlobal(id: String): LlvmGlobalVariableDto
    suspend fun updateGlobal(id: String, request: UpdateLlvmGlobalVariableRequest): LlvmGlobalVariableDto
    suspend fun deleteGlobalCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteGlobal(id: String)

    suspend fun createAlias(request: CreateLlvmAliasRequest): LlvmAliasDto
    suspend fun updateAlias(id: String, request: UpdateLlvmAliasRequest): LlvmAliasDto
    suspend fun deleteAliasCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteAlias(id: String)

    suspend fun createIfunc(request: CreateLlvmIfuncRequest): LlvmIfuncDto
    suspend fun updateIfunc(id: String, request: UpdateLlvmIfuncRequest): LlvmIfuncDto
    suspend fun deleteIfuncCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteIfunc(id: String)

    suspend fun createComdat(request: CreateLlvmComdatRequest): LlvmComdatDto
    suspend fun updateComdat(id: String, request: UpdateLlvmComdatRequest): LlvmComdatDto
    suspend fun deleteComdatCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteComdat(id: String)

    suspend fun createConstant(request: CreateLlvmConstantRequest): LlvmConstantDto
    suspend fun listConstants(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmConstantDto>
    suspend fun getConstant(id: String): LlvmConstantDto
    suspend fun updateConstant(id: String, request: UpdateLlvmConstantRequest): LlvmConstantDto
    suspend fun deleteConstantCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteConstant(id: String)
    suspend fun createConstantItem(request: CreateLlvmConstantItemRequest): LlvmConstantItemDto
    suspend fun updateConstantItem(id: String, request: UpdateLlvmConstantItemRequest): LlvmConstantItemDto
    suspend fun deleteConstantItem(id: String)
    suspend fun reorderConstantItems(constantId: String, request: LlvmReorderRequestDto): List<LlvmConstantItemDto>

    suspend fun createInlineAsm(request: CreateLlvmInlineAsmRequest): LlvmInlineAsmDto
    suspend fun updateInlineAsm(id: String, request: UpdateLlvmInlineAsmRequest): LlvmInlineAsmDto
    suspend fun deleteInlineAsmCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteInlineAsm(id: String)
}

interface LlvmFunctionService {
    suspend fun create(request: CreateLlvmFunctionRequest): LlvmFunctionDto
    suspend fun list(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmFunctionDto>
    suspend fun get(id: String): LlvmFunctionDto
    suspend fun update(id: String, request: UpdateLlvmFunctionRequest): LlvmFunctionDto
    suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun validate(id: String): List<LlvmValidationIssueDto>
    suspend fun delete(id: String)

    suspend fun createParam(request: CreateLlvmFunctionParamRequest): LlvmFunctionParamDto
    suspend fun updateParam(id: String, request: UpdateLlvmFunctionParamRequest): LlvmFunctionParamDto
    suspend fun deleteParam(id: String)
    suspend fun reorderParams(functionId: String, request: LlvmReorderRequestDto): List<LlvmFunctionParamDto>

    suspend fun createBlock(request: CreateLlvmBasicBlockRequest): LlvmBasicBlockDto
    suspend fun updateBlock(id: String, request: UpdateLlvmBasicBlockRequest): LlvmBasicBlockDto
    suspend fun deleteBlockCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteBlock(id: String)
    suspend fun reorderBlocks(functionId: String, request: LlvmReorderRequestDto): List<LlvmBasicBlockDto>

    suspend fun createInstruction(request: CreateLlvmInstructionRequest): LlvmInstructionDto
    suspend fun updateInstruction(id: String, request: UpdateLlvmInstructionRequest): LlvmInstructionDto
    suspend fun deleteInstructionCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteInstruction(id: String)
    suspend fun reorderInstructions(blockId: String, request: LlvmReorderRequestDto): List<LlvmInstructionDto>

    suspend fun createOperand(request: CreateLlvmOperandRequest): LlvmOperandDto
    suspend fun updateOperand(id: String, request: UpdateLlvmOperandRequest): LlvmOperandDto
    suspend fun deleteOperand(id: String)
    suspend fun reorderOperands(instructionId: String, request: LlvmReorderRequestDto): List<LlvmOperandDto>

    suspend fun createPhiIncoming(request: CreateLlvmPhiIncomingRequest): LlvmPhiIncomingDto
    suspend fun updatePhiIncoming(id: String, request: UpdateLlvmPhiIncomingRequest): LlvmPhiIncomingDto
    suspend fun deletePhiIncoming(id: String)
    suspend fun reorderPhiIncoming(instructionId: String, request: LlvmReorderRequestDto): List<LlvmPhiIncomingDto>

    suspend fun createClause(request: CreateLlvmInstructionClauseRequest): LlvmInstructionClauseDto
    suspend fun updateClause(id: String, request: UpdateLlvmInstructionClauseRequest): LlvmInstructionClauseDto
    suspend fun deleteClause(id: String)
    suspend fun reorderClauses(instructionId: String, request: LlvmReorderRequestDto): List<LlvmInstructionClauseDto>

    suspend fun createBundle(request: CreateLlvmOperandBundleRequest): LlvmOperandBundleDto
    suspend fun updateBundle(id: String, request: UpdateLlvmOperandBundleRequest): LlvmOperandBundleDto
    suspend fun deleteBundle(id: String)
    suspend fun reorderBundles(instructionId: String, request: LlvmReorderRequestDto): List<LlvmOperandBundleDto>
}

interface LlvmMetadataService {
    suspend fun createNamed(request: CreateLlvmNamedMetadataRequest): LlvmNamedMetadataDto
    suspend fun listNamed(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmNamedMetadataDto>
    suspend fun getNamed(id: String): LlvmNamedMetadataDto
    suspend fun updateNamed(id: String, request: UpdateLlvmNamedMetadataRequest): LlvmNamedMetadataDto
    suspend fun deleteNamedCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteNamed(id: String)
    suspend fun reorderNamed(moduleId: String, request: LlvmReorderRequestDto): List<LlvmNamedMetadataDto>

    suspend fun createNode(request: CreateLlvmMetadataNodeRequest): LlvmMetadataNodeDto
    suspend fun listNodes(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmMetadataNodeDto>
    suspend fun getNode(id: String): LlvmMetadataNodeDto
    suspend fun updateNode(id: String, request: UpdateLlvmMetadataNodeRequest): LlvmMetadataNodeDto
    suspend fun deleteNodeCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteNode(id: String)

    suspend fun createField(request: CreateLlvmMetadataFieldRequest): LlvmMetadataFieldDto
    suspend fun updateField(id: String, request: UpdateLlvmMetadataFieldRequest): LlvmMetadataFieldDto
    suspend fun deleteField(id: String)
    suspend fun reorderNodeFields(metadataNodeId: String, request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto>
    suspend fun reorderNamedFields(namedMetadataId: String, request: LlvmReorderRequestDto): List<LlvmMetadataFieldDto>

    suspend fun createAttachment(request: CreateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto
    suspend fun updateAttachment(id: String, request: UpdateLlvmMetadataAttachmentRequest): LlvmMetadataAttachmentDto
    suspend fun deleteAttachment(id: String)
}

interface LlvmAttributeService {
    suspend fun createGroup(request: CreateLlvmAttributeGroupRequest): LlvmAttributeGroupDto
    suspend fun listGroups(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmAttributeGroupDto>
    suspend fun getGroup(id: String): LlvmAttributeGroupDto
    suspend fun updateGroup(id: String, request: UpdateLlvmAttributeGroupRequest): LlvmAttributeGroupDto
    suspend fun deleteGroupCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun deleteGroup(id: String)

    suspend fun createEntry(request: CreateLlvmAttributeEntryRequest): LlvmAttributeEntryDto
    suspend fun updateEntry(id: String, request: UpdateLlvmAttributeEntryRequest): LlvmAttributeEntryDto
    suspend fun deleteEntry(id: String)
    suspend fun reorderEntries(groupId: String, request: LlvmReorderRequestDto): List<LlvmAttributeEntryDto>
}

interface LlvmValidationService {
    suspend fun validateModule(moduleId: String): List<LlvmValidationIssueDto>
}

interface LlvmSnapshotService {
    suspend fun exportModule(moduleId: String): LlvmSnapshotDto
    suspend fun importSnapshot(snapshot: LlvmSnapshotDto): LlvmSnapshotImportResultDto
}

interface LlvmLlExportService {
    suspend fun exportModule(moduleId: String, outputPath: String? = null): LlvmLlExportDto
}

interface LlvmCompileProfileService {
    suspend fun create(request: CreateLlvmCompileProfileRequest): LlvmCompileProfileDto
    suspend fun list(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmCompileProfileDto>
    suspend fun get(id: String): LlvmCompileProfileDto
    suspend fun update(id: String, request: UpdateLlvmCompileProfileRequest): LlvmCompileProfileDto
    suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto
    suspend fun validate(id: String): List<LlvmValidationIssueDto>
    suspend fun delete(id: String)
}

interface LlvmCompileJobService {
    suspend fun create(request: CreateLlvmCompileJobRequest): LlvmCompileJobDto
    suspend fun list(search: LlvmSearchRequest = LlvmSearchRequest()): List<LlvmCompileJobDto>
    suspend fun get(id: String): LlvmCompileJobDto
    suspend fun execute(id: String): LlvmCompileExecutionResultDto
    suspend fun delete(id: String)
    suspend fun listArtifacts(jobId: String): List<LlvmCompileArtifactDto>
}
