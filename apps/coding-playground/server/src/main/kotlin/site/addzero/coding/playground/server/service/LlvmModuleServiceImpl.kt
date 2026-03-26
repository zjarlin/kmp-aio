package site.addzero.coding.playground.server.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.entity.LlvmModule
import site.addzero.coding.playground.server.entity.by
import site.addzero.coding.playground.server.entity.encodeStringMap
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.shared.dto.CreateLlvmModuleRequest
import site.addzero.coding.playground.shared.dto.LlvmDeleteCheckResultDto
import site.addzero.coding.playground.shared.dto.LlvmModuleAggregateDto
import site.addzero.coding.playground.shared.dto.LlvmModuleDto
import site.addzero.coding.playground.shared.dto.LlvmSearchRequest
import site.addzero.coding.playground.shared.dto.LlvmValidationIssueDto
import site.addzero.coding.playground.shared.dto.UpdateLlvmModuleRequest
import site.addzero.coding.playground.shared.service.LlvmModuleService
import site.addzero.coding.playground.shared.service.LlvmValidationService

@Single
class LlvmModuleServiceImpl(
    private val support: MetadataPersistenceSupport,
    private val validationService: LlvmValidationService,
) : LlvmModuleService {
    override suspend fun create(request: CreateLlvmModuleRequest): LlvmModuleDto {
        requireText(request.name, "module name")
        requireText(request.sourceFilename, "source filename")
        requireText(request.targetTriple, "target triple")
        requireText(request.dataLayout, "data layout")
        if (support.listModules().any { it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("LLVM module name '${request.name}' already exists")
        }
        val now = support.now()
        val entity = new(LlvmModule::class).by {
            id = support.newId()
            name = request.name
            sourceFilename = request.sourceFilename
            targetTriple = request.targetTriple
            dataLayout = request.dataLayout
            moduleAsm = request.moduleAsm
            moduleFlagsJson = encodeStringMap(request.moduleFlags)
            description = request.description
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun list(search: LlvmSearchRequest): List<LlvmModuleDto> {
        return support.listModules()
            .map { it.toDto() }
            .filter { search.matches(symbol = it.name, extras = listOf(it.sourceFilename, it.targetTriple, it.description)) }
    }

    override suspend fun get(id: String): LlvmModuleDto = support.moduleOrThrow(id).toDto()

    override suspend fun aggregate(id: String): LlvmModuleAggregateDto = support.buildModuleAggregate(id)

    override suspend fun update(id: String, request: UpdateLlvmModuleRequest): LlvmModuleDto {
        val existing = support.moduleOrThrow(id)
        requireText(request.name, "module name")
        requireText(request.sourceFilename, "source filename")
        requireText(request.targetTriple, "target triple")
        requireText(request.dataLayout, "data layout")
        if (support.listModules().any { it.id != id && it.name.equals(request.name, ignoreCase = true) }) {
            throw PlaygroundValidationException("LLVM module name '${request.name}' already exists")
        }
        val entity = new(LlvmModule::class).by {
            this.id = id
            name = request.name
            sourceFilename = request.sourceFilename
            targetTriple = request.targetTriple
            dataLayout = request.dataLayout
            moduleAsm = request.moduleAsm
            moduleFlagsJson = encodeStringMap(request.moduleFlags)
            description = request.description
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun deleteCheck(id: String): LlvmDeleteCheckResultDto {
        val aggregate = support.buildModuleAggregate(id)
        val counts = listOf(
            "types=${aggregate.types.size}",
            "globals=${aggregate.globals.size}",
            "functions=${aggregate.functions.size}",
            "metadataNodes=${aggregate.metadataNodes.size}",
            "compileProfiles=${aggregate.compileProfiles.size}",
        )
        return LlvmDeleteCheckResultDto(
            id = id,
            kind = "module",
            deletable = true,
            message = "Deleting module '${aggregate.module.name}' will cascade into ${counts.joinToString(", ")}",
        )
    }

    override suspend fun validate(id: String): List<LlvmValidationIssueDto> = validationService.validateModule(id)

    override suspend fun delete(id: String) {
        support.moduleOrThrow(id)
        support.inTransaction {
            support.deleteModuleCascade(id)
        }
    }
}
