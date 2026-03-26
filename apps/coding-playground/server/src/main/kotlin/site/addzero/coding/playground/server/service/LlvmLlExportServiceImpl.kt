package site.addzero.coding.playground.server.service

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.LlvmAttachmentTargetKind
import site.addzero.coding.playground.shared.dto.LlvmInstructionOpcode
import site.addzero.coding.playground.shared.dto.LlvmLlExportDto
import site.addzero.coding.playground.shared.dto.LlvmTypeDto
import site.addzero.coding.playground.shared.dto.LlvmTypeKind
import site.addzero.coding.playground.shared.service.LlvmLlExportService
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Single
class LlvmLlExportServiceImpl(
    private val support: MetadataPersistenceSupport,
) : LlvmLlExportService {
    override suspend fun exportModule(moduleId: String, outputPath: String?): LlvmLlExportDto {
        val aggregate = support.buildModuleAggregate(moduleId)
        val metadataIndex = aggregate.metadataNodes.sortedBy { it.orderIndex }.mapIndexed { index, node -> node.id to index }.toMap()
        val attributeIndex = aggregate.attributeGroups.sortedBy { it.orderIndex }.mapIndexed { index, group -> group.id to index }.toMap()
        val functionIndex = aggregate.functions.associateBy { it.id }
        val globalIndex = aggregate.globals.associateBy { it.id }
        val paramIndex = aggregate.params.associateBy { it.id }
        val blockIndex = aggregate.blocks.associateBy { it.id }
        val instructionIndex = aggregate.instructions.associateBy { it.id }
        val constantIndex = aggregate.constants.associateBy { it.id }
        val typeIndex = aggregate.types.associateBy { it.id }
        val inlineAsmIndex = aggregate.inlineAsms.associateBy { it.id }
        val attachmentByInstruction = aggregate.metadataAttachments.filter { it.targetKind == LlvmAttachmentTargetKind.INSTRUCTION }.groupBy { it.instructionId }
        val attachmentByFunction = aggregate.metadataAttachments.filter { it.targetKind == LlvmAttachmentTargetKind.FUNCTION }.groupBy { it.functionId }
        val attachmentByGlobal = aggregate.metadataAttachments.filter { it.targetKind == LlvmAttachmentTargetKind.GLOBAL }.groupBy { it.globalVariableId }
        val fieldsByType = aggregate.typeMembers.groupBy { it.typeId }
        val entriesByAttribute = aggregate.attributeEntries.groupBy { it.attributeGroupId }
        val itemsByConstant = aggregate.constantItems.groupBy { it.constantId }
        val paramsByFunction = aggregate.params.groupBy { it.functionId }
        val blocksByFunction = aggregate.blocks.groupBy { it.functionId }
        val instructionsByBlock = aggregate.instructions.groupBy { it.blockId }
        val operandsByInstruction = aggregate.operands.groupBy { it.instructionId }
        val phiByInstruction = aggregate.phiIncomings.groupBy { it.instructionId }
        val clausesByInstruction = aggregate.instructionClauses.groupBy { it.instructionId }
        val bundlesByInstruction = aggregate.operandBundles.groupBy { it.instructionId }
        val metadataFieldsByNode = aggregate.metadataFields.filter { it.metadataNodeId != null }.groupBy { it.metadataNodeId }
        val metadataFieldsByNamed = aggregate.metadataFields.filter { it.namedMetadataId != null }.groupBy { it.namedMetadataId }

        lateinit var renderTypeRef: (String?, String) -> String
        val renderTypeBody: (LlvmTypeDto) -> String = { type ->
            when (type.kind) {
                LlvmTypeKind.VOID -> "void"
                LlvmTypeKind.INTEGER -> "i${type.primitiveWidth ?: 32}"
                LlvmTypeKind.FLOAT -> "float"
                LlvmTypeKind.DOUBLE -> "double"
                LlvmTypeKind.FP128 -> "fp128"
                LlvmTypeKind.LABEL -> "label"
                LlvmTypeKind.TOKEN -> "token"
                LlvmTypeKind.METADATA -> "metadata"
                LlvmTypeKind.POINTER -> "${renderTypeRef(type.elementTypeRefId, type.definitionText ?: "ptr")}*"
                LlvmTypeKind.ARRAY -> "[${type.arrayLength ?: 0} x ${renderTypeRef(type.elementTypeRefId, type.definitionText ?: "i8")}]"
                LlvmTypeKind.VECTOR -> "<${type.arrayLength ?: 0} x ${renderTypeRef(type.elementTypeRefId, type.definitionText ?: "i32")}>"
                LlvmTypeKind.SCALABLE_VECTOR -> "<vscale x ${type.arrayLength ?: 1} x ${renderTypeRef(type.elementTypeRefId, type.definitionText ?: "i32")}>"
                LlvmTypeKind.STRUCT -> if (type.opaque) {
                    "opaque"
                } else {
                    val body = fieldsByType[type.id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") {
                        renderTypeRef(it.memberTypeRefId, it.memberTypeText)
                    }
                    if (type.packed) "<{ $body }>" else "{ $body }"
                }
                LlvmTypeKind.FUNCTION -> {
                    val args = fieldsByType[type.id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") {
                        renderTypeRef(it.memberTypeRefId, it.memberTypeText)
                    }
                    "${renderTypeRef(type.returnTypeRefId, type.definitionText ?: "void")} ($args)"
                }
                LlvmTypeKind.OPAQUE -> "opaque"
            }
        }

        renderTypeRef = { typeRefId, fallback ->
            typeRefId?.let { refId ->
                typeIndex[refId]?.let { type ->
                    when (type.kind) {
                        LlvmTypeKind.STRUCT,
                        LlvmTypeKind.FUNCTION,
                        LlvmTypeKind.OPAQUE,
                        -> "%${type.symbol}"
                        else -> renderTypeBody(type)
                    }
                } ?: fallback
            } ?: fallback
        }

        fun renderMetadataRef(nodeId: String?): String = nodeId?.let { id -> "!${metadataIndex[id] ?: 0}" } ?: "!0"

        fun renderOperand(id: String): String {
            val operand = aggregate.operands.first { it.id == id }
            return when {
                operand.referencedInstructionId != null -> {
                    val referencedInstructionId = operand.referencedInstructionId!!
                    val symbol = instructionIndex[referencedInstructionId]?.resultSymbol ?: referencedInstructionId
                    "%$symbol"
                }
                operand.referencedFunctionId != null -> "@${functionIndex.getValue(operand.referencedFunctionId!!).symbol}"
                operand.referencedParamId != null -> "%${paramIndex.getValue(operand.referencedParamId!!).name}"
                operand.referencedGlobalId != null -> "@${globalIndex.getValue(operand.referencedGlobalId!!).symbol}"
                operand.referencedConstantId != null -> constantIndex[operand.referencedConstantId!!]?.literalText ?: constantIndex[operand.referencedConstantId!!]?.name ?: operand.text
                operand.referencedBlockId != null -> "%${blockIndex.getValue(operand.referencedBlockId!!).label}"
                operand.referencedMetadataNodeId != null -> renderMetadataRef(operand.referencedMetadataNodeId)
                operand.referencedTypeId != null -> renderTypeRef(operand.referencedTypeId, operand.text)
                operand.referencedInlineAsmId != null -> {
                    val asm = inlineAsmIndex.getValue(operand.referencedInlineAsmId!!)
                    "asm ${'"'}${asm.asmText}${'"'}"
                }
                else -> operand.text
            }
        }

        fun renderInstruction(id: String): String {
            val instruction = instructionIndex.getValue(id)
            val prefix = instruction.resultSymbol?.takeIf { it.isNotBlank() }?.let { "%$it = " }.orEmpty()
            val operandText = operandsByInstruction[id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") { renderOperand(it.id) }
            val phiText = phiByInstruction[id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") {
                "[ ${it.valueOperandId?.let(::renderOperand) ?: it.valueText}, %${blockIndex.getValue(it.incomingBlockId).label} ]"
            }
            val clauseText = clausesByInstruction[id].orEmpty().sortedBy { it.orderIndex }.joinToString(" ") { "${it.clauseKind} ${it.clauseText}" }
            val bundleText = bundlesByInstruction[id].orEmpty().sortedBy { it.orderIndex }.joinToString(" ") {
                "[${it.tag}(${it.values.joinToString(", ")})]"
            }
            val opcodeText = when (instruction.opcode) {
                LlvmInstructionOpcode.PHI -> "${instruction.opcode.name.lowercase()} ${instruction.typeText ?: "i32"} $phiText"
                LlvmInstructionOpcode.RET -> "${instruction.opcode.name.lowercase()} ${instruction.typeText ?: "void"} $operandText".trim()
                LlvmInstructionOpcode.BR -> "${instruction.opcode.name.lowercase()} $operandText".trim()
                LlvmInstructionOpcode.SWITCH -> "${instruction.opcode.name.lowercase()} ${instruction.typeText ?: "i32"} $operandText"
                else -> buildString {
                    append(instruction.opcode.name.lowercase())
                    if (!instruction.typeText.isNullOrBlank()) {
                        append(' ')
                        append(instruction.typeText)
                    }
                    if (operandText.isNotBlank()) {
                        append(' ')
                        append(operandText)
                    }
                }.trim()
            }
            val suffix = listOfNotNull(instruction.textSuffix, clauseText.takeIf { it.isNotBlank() }, bundleText.takeIf { it.isNotBlank() }).joinToString(" ")
            val attachments = attachmentByInstruction[id].orEmpty().sortedBy { it.orderIndex }.joinToString("") { ", !${it.key} ${renderMetadataRef(it.metadataNodeId)}" }
            return buildString {
                append(prefix)
                append(opcodeText)
                if (suffix.isNotBlank()) {
                    append(' ')
                    append(suffix)
                }
                append(attachments)
            }
        }

        val content = buildString {
            appendLine("; Generated by LLVM IR coding playground")
            appendLine("source_filename = ${'"'}${aggregate.module.sourceFilename}${'"'}")
            appendLine("target datalayout = ${'"'}${aggregate.module.dataLayout}${'"'}")
            appendLine("target triple = ${'"'}${aggregate.module.targetTriple}${'"'}")
            if (!aggregate.module.moduleAsm.isNullOrBlank()) {
                appendLine("module asm ${'"'}${aggregate.module.moduleAsm}${'"'}")
            }
            if (aggregate.module.description != null) {
                appendLine("; ${aggregate.module.description}")
            }
            appendLine()

            aggregate.comdats.sortedBy { it.orderIndex }.forEach {
                appendLine("\$${it.name} = comdat ${it.selectionKind}")
            }
            if (aggregate.comdats.isNotEmpty()) appendLine()

            aggregate.types.sortedBy { it.orderIndex }.forEach { type ->
                appendLine("%${type.symbol} = type ${renderTypeBody(type)}")
            }
            if (aggregate.types.isNotEmpty()) appendLine()

            aggregate.globals.sortedBy { it.orderIndex }.forEach { global ->
                val attrs = global.attributeGroupIds.mapNotNull(attributeIndex::get).sorted().joinToString(" ") { "#$it" }
                val attachments = attachmentByGlobal[global.id].orEmpty().sortedBy { it.orderIndex }.joinToString("") { ", !${it.key} ${renderMetadataRef(it.metadataNodeId)}" }
                appendLine(
                    buildString {
                        append("@${global.symbol} = ${global.linkage.name.lowercase()} ${global.visibility.name.lowercase()} ")
                        append(if (global.constant) "constant " else "global ")
                        append(renderTypeRef(global.typeRefId, global.typeText))
                        if (!global.initializerText.isNullOrBlank()) {
                            append(" ${global.initializerText}")
                        } else if (global.initializerConstantId != null) {
                            append(" ${constantIndex[global.initializerConstantId]?.literalText ?: constantIndex[global.initializerConstantId]?.name ?: "undef"}")
                        }
                        global.sectionName?.let { append(", section ${'"'}$it${'"'}") }
                        global.comdatId?.let { append(", comdat(\$${aggregate.comdats.first { comdat -> comdat.id == it }.name})") }
                        global.alignment?.let { append(", align $it") }
                        if (attrs.isNotBlank()) append(" $attrs")
                        append(attachments)
                    },
                )
            }
            if (aggregate.globals.isNotEmpty()) appendLine()

            aggregate.aliases.sortedBy { it.orderIndex }.forEach {
                appendLine("@${it.symbol} = alias ${it.linkage.name.lowercase()} ${it.visibility.name.lowercase()} ${it.aliaseeText}")
            }
            aggregate.ifuncs.sortedBy { it.orderIndex }.forEach {
                appendLine("@${it.symbol} = ifunc ${it.linkage.name.lowercase()} ${it.visibility.name.lowercase()} ${it.resolverText}")
            }
            if (aggregate.aliases.isNotEmpty() || aggregate.ifuncs.isNotEmpty()) appendLine()

            aggregate.functions.sortedBy { it.orderIndex }.forEach { function ->
                val params = paramsByFunction[function.id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") {
                    "${renderTypeRef(it.typeRefId, it.typeText)} %${it.name}"
                }
                val attrs = function.attributeGroupIds.mapNotNull(attributeIndex::get).sorted().joinToString(" ") { "#$it" }
                val attachments = attachmentByFunction[function.id].orEmpty().sortedBy { it.orderIndex }.joinToString("") { " !${it.key} ${renderMetadataRef(it.metadataNodeId)}" }
                val header = buildString {
                    append(if (function.declarationOnly) "declare " else "define ")
                    append("${function.linkage.name.lowercase()} ${function.visibility.name.lowercase()} ")
                    append(renderTypeRef(function.returnTypeRefId, function.returnTypeText))
                    append(" @${function.symbol}(")
                    append(params)
                    if (function.variadic) {
                        if (params.isNotBlank()) append(", ")
                        append("...")
                    }
                    append(")")
                    if (attrs.isNotBlank()) append(" $attrs")
                    if (attachments.isNotBlank()) append(attachments)
                }
                if (function.declarationOnly) {
                    appendLine(header)
                } else {
                    appendLine("$header {")
                    blocksByFunction[function.id].orEmpty().sortedBy { it.orderIndex }.forEach { block ->
                        appendLine("${block.label}:")
                        instructionsByBlock[block.id].orEmpty().sortedBy { it.orderIndex }.forEach { instruction ->
                            append("  ")
                            appendLine(renderInstruction(instruction.id))
                        }
                    }
                    appendLine("}")
                }
                appendLine()
            }

            aggregate.attributeGroups.sortedBy { it.orderIndex }.forEach { group ->
                val index = attributeIndex.getValue(group.id)
                val entries = entriesByAttribute[group.id].orEmpty().sortedBy { it.orderIndex }.joinToString(" ") {
                    if (it.value.isNullOrBlank()) it.key else "${it.key}=${it.value}"
                }
                appendLine("attributes #$index = { $entries }")
            }
            if (aggregate.attributeGroups.isNotEmpty()) appendLine()

            aggregate.metadataNodes.sortedBy { it.orderIndex }.forEach { node ->
                val index = metadataIndex.getValue(node.id)
                val body = metadataFieldsByNode[node.id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") { field ->
                    when {
                        field.referencedNodeId != null -> renderMetadataRef(field.referencedNodeId)
                        field.referencedConstantId != null -> constantIndex[field.referencedConstantId]?.literalText ?: "0"
                        field.referencedTypeId != null -> "%${typeIndex.getValue(field.referencedTypeId!!).symbol}"
                        field.valueKind.name == "STRING" -> "${'"'}${field.valueText}${'"'}"
                        else -> field.valueText
                    }
                }
                appendLine("!$index = ${if (node.distinct) "distinct " else ""}!{ $body }")
            }
            aggregate.namedMetadata.sortedBy { it.orderIndex }.forEach { named ->
                val body = metadataFieldsByNamed[named.id].orEmpty().sortedBy { it.orderIndex }.joinToString(", ") { renderMetadataRef(it.referencedNodeId ?: it.metadataNodeId) }
                appendLine("!${named.name} = !{ $body }")
            }
        }

        outputPath?.let { raw ->
            val path = Paths.get(raw)
            path.parent?.createDirectories()
            path.writeText(content)
        }

        return LlvmLlExportDto(
            moduleId = aggregate.module.id,
            moduleName = aggregate.module.name,
            content = content,
            outputPath = outputPath,
        )
    }
}
