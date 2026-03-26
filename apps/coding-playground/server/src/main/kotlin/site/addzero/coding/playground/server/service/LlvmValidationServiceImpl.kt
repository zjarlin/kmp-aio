package site.addzero.coding.playground.server.service

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.decodeStringList
import site.addzero.coding.playground.shared.dto.LlvmValidationIssueDto
import site.addzero.coding.playground.shared.dto.LlvmValidationSeverity
import site.addzero.coding.playground.shared.service.LlvmValidationService

@Single
class LlvmValidationServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmValidationService {
    override suspend fun validateModule(moduleId: String): List<LlvmValidationIssueDto> {
        val issues = mutableListOf<LlvmValidationIssueDto>()
        val module = support.moduleOrThrow(moduleId)
        if (module.targetTriple.isBlank()) {
            issues += issue("module:${module.id}", "target triple is required")
        }
        if (module.dataLayout.isBlank()) {
            issues += issue("module:${module.id}", "data layout is required")
        }

        val symbolBuckets = buildList {
            support.listTypes(moduleId).forEach { add(it.symbol to "type") }
            support.listGlobals(moduleId).forEach { add(it.symbol to "global") }
            support.listFunctions(moduleId).forEach { add(it.symbol to "function") }
            support.listAliases(moduleId).forEach { add(it.symbol to "alias") }
            support.listIfuncs(moduleId).forEach { add(it.symbol to "ifunc") }
        }.groupBy({ it.first }, { it.second })

        symbolBuckets.filterValues { it.size > 1 }.forEach { (symbol, kinds) ->
            issues += issue("module:${module.id}", "duplicate symbol '$symbol' across ${kinds.joinToString()}")
        }

        val blocksById = support.listBlocks().associateBy { it.id }
        val functions = support.listFunctions(moduleId)
        functions.forEach { function ->
            val blocks = support.listBlocks(function.id)
            if (!function.declarationOnly && blocks.isEmpty()) {
                issues += issue("function:${function.id}", "function '${function.symbol}' must contain at least one basic block")
            }
            decodeStringList(function.attributeGroupIdsJson).forEach { groupId ->
                runCatching { support.attributeGroupOrThrow(groupId) }.onFailure {
                    issues += issue("function:${function.id}", "function '${function.symbol}' references missing attribute group '$groupId'")
                }
            }
            blocks.forEach { block ->
                val instructions = support.listInstructions(block.id)
                if (instructions.isEmpty()) {
                    issues += warning("block:${block.id}", "basic block '${block.label}' does not contain any instructions")
                } else {
                    if (!instructions.last().terminator) {
                        issues += issue("block:${block.id}", "basic block '${block.label}' must end with a terminator instruction")
                    }
                    if (instructions.dropLast(1).any { it.terminator }) {
                        issues += issue("block:${block.id}", "terminator instructions must appear only at the end of a basic block")
                    }
                    var seenNonPhi = false
                    instructions.forEach { instruction ->
                        if (instruction.opcode == "PHI") {
                            if (seenNonPhi) {
                                issues += issue("instruction:${instruction.id}", "phi instructions must appear before non-phi instructions")
                            }
                        } else {
                            seenNonPhi = true
                        }
                    }
                }
            }
        }

        support.listGlobals(moduleId).forEach { global ->
            decodeStringList(global.attributeGroupIdsJson).forEach { groupId ->
                runCatching { support.attributeGroupOrThrow(groupId) }.onFailure {
                    issues += issue("global:${global.id}", "global '${global.symbol}' references missing attribute group '$groupId'")
                }
            }
        }

        support.listOperands().filter { operand ->
            val blockId = support.instructionOrThrow(operand.instructionId).blockId
            val functionId = support.blockOrThrow(blockId).functionId
            support.functionOrThrow(functionId).moduleId == moduleId
        }.forEach { operand ->
            if (operand.kind == "BLOCK" && operand.referencedBlockId == null) {
                issues += issue("operand:${operand.id}", "block operand '${operand.text}' must reference a basic block")
            }
            operand.referencedBlockId?.let { blockId ->
                val targetFunctionId = blocksById[blockId]?.functionId
                val currentFunctionId = support.blockOrThrow(support.instructionOrThrow(operand.instructionId).blockId).functionId
                if (targetFunctionId != null && targetFunctionId != currentFunctionId) {
                    issues += issue("operand:${operand.id}", "branch target block '$blockId' is outside the current function")
                }
            }
        }

        support.listMetadataAttachments(moduleId).forEach { attachment ->
            val targetCount = listOf(attachment.functionId, attachment.globalVariableId, attachment.instructionId).count { it != null }
            if (targetCount != 1) {
                issues += issue("metadata-attachment:${attachment.id}", "metadata attachment must target exactly one LLVM object")
            }
        }

        support.listCompileProfiles(moduleId).forEach { profile ->
            if (profile.outputDirectory.isBlank()) {
                issues += issue("compile-profile:${profile.id}", "compile profile '${profile.name}' requires an output directory")
            }
            if (profile.optPath.isNullOrBlank() && profile.llcPath.isNullOrBlank() && profile.clangPath.isNullOrBlank()) {
                issues += warning("compile-profile:${profile.id}", "compile profile '${profile.name}' exports .ll only because no toolchain executable is configured")
            }
        }

        return issues
    }

    private fun issue(location: String, message: String): LlvmValidationIssueDto {
        return LlvmValidationIssueDto(
            severity = LlvmValidationSeverity.ERROR,
            location = location,
            message = message,
        )
    }

    private fun warning(location: String, message: String): LlvmValidationIssueDto {
        return LlvmValidationIssueDto(
            severity = LlvmValidationSeverity.WARNING,
            location = location,
            message = message,
        )
    }
}
