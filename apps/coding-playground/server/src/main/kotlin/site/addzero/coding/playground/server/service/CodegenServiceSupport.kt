package site.addzero.coding.playground.server.service

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.util.ensureKtFileName
import site.addzero.coding.playground.server.util.isValidKotlinIdentifier
import site.addzero.coding.playground.server.util.isValidKotlinPackage
import site.addzero.coding.playground.shared.dto.CodegenSearchRequest
import site.addzero.coding.playground.shared.dto.DeclarationKind
import site.addzero.coding.playground.shared.dto.ValidationIssueDto
import site.addzero.coding.playground.shared.dto.ValidationSeverity

@Single
class CodegenServiceSupport {
    fun requireText(value: String, label: String) {
        if (value.isBlank()) {
            throw PlaygroundValidationException("$label 不能为空")
        }
    }

    fun requireIdentifier(value: String, label: String) {
        requireText(value, label)
        if (!value.isValidKotlinIdentifier()) {
            throw PlaygroundValidationException("$label 必须是合法的 Kotlin 标识符")
        }
    }

    fun requirePackageName(value: String, label: String) {
        requireText(value, label)
        if (!value.isValidKotlinPackage()) {
            throw PlaygroundValidationException("$label 必须是合法的包名")
        }
    }

    fun requireFileName(value: String): String {
        val normalized = value.ensureKtFileName()
        if (!normalized.removeSuffix(".kt").isValidKotlinIdentifier()) {
            throw PlaygroundValidationException("文件名必须是合法的 Kotlin 类型名并以 .kt 结尾")
        }
        return normalized
    }

    fun nextOrder(indexes: List<Int>): Int = (indexes.maxOrNull() ?: -1) + 1

    fun buildFqName(packageName: String, name: String): String {
        return if (packageName.isBlank()) {
            name
        } else {
            "$packageName.$name"
        }
    }

    fun ensureDeclarationChildrenAllowed(kind: DeclarationKind, action: String) {
        if (kind == DeclarationKind.ANNOTATION_CLASS && action in setOf("property", "function")) {
            throw PlaygroundValidationException("注解类暂不支持独立 $action，参数请使用主构造参数")
        }
        if (kind != DeclarationKind.ENUM_CLASS && action == "enum-entry") {
            throw PlaygroundValidationException("只有枚举类可以维护枚举项")
        }
    }

    fun buildValidationIssue(
        scopeType: String,
        scopeId: String?,
        severity: ValidationSeverity,
        message: String,
    ): ValidationIssueDto {
        return ValidationIssueDto(
            scopeType = scopeType,
            scopeId = scopeId,
            severity = severity,
            message = message,
        )
    }
}

internal fun CodegenSearchRequest.matches(
    projectId: String? = null,
    targetId: String? = null,
    fileId: String? = null,
    declarationKind: DeclarationKind? = null,
    values: List<String?> = emptyList(),
): Boolean {
    if (this.projectId != null && this.projectId != projectId) {
        return false
    }
    if (this.targetId != null && this.targetId != targetId) {
        return false
    }
    if (this.fileId != null && this.fileId != fileId) {
        return false
    }
    if (this.kind != null && this.kind != declarationKind) {
        return false
    }
    val candidates = values.filterNotNull()
    val queryValue = query
    if (!queryValue.isNullOrBlank() && candidates.none { it.contains(queryValue, ignoreCase = true) }) {
        return false
    }
    return true
}
