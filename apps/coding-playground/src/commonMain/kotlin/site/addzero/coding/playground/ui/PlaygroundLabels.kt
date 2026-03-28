package site.addzero.coding.playground.ui

import site.addzero.coding.playground.shared.dto.CodeVisibility
import site.addzero.coding.playground.shared.dto.ConflictReason
import site.addzero.coding.playground.shared.dto.DeclarationKind
import site.addzero.coding.playground.shared.dto.ManagedArtifactSyncStatus
import site.addzero.coding.playground.shared.dto.SyncConflictResolution
import site.addzero.coding.playground.shared.dto.ValidationSeverity

fun DeclarationKind.label(): String {
    return when (this) {
        DeclarationKind.DATA_CLASS -> "数据类"
        DeclarationKind.ENUM_CLASS -> "枚举类"
        DeclarationKind.INTERFACE -> "接口"
        DeclarationKind.OBJECT -> "对象"
        DeclarationKind.ANNOTATION_CLASS -> "注解类"
    }
}

fun CodeVisibility.label(): String {
    return when (this) {
        CodeVisibility.PUBLIC -> "公开"
        CodeVisibility.INTERNAL -> "模块内"
        CodeVisibility.PRIVATE -> "私有"
    }
}

fun ManagedArtifactSyncStatus.label(): String {
    return when (this) {
        ManagedArtifactSyncStatus.CLEAN -> "已同步"
        ManagedArtifactSyncStatus.METADATA_DIRTY -> "元数据已变更"
        ManagedArtifactSyncStatus.SOURCE_DIRTY -> "源码已变更"
        ManagedArtifactSyncStatus.CONFLICT -> "存在冲突"
        ManagedArtifactSyncStatus.MISSING -> "文件缺失"
    }
}

fun ConflictReason.label(): String {
    return when (this) {
        ConflictReason.BOTH_CHANGED -> "元数据与源码同时变更"
        ConflictReason.PARSE_FAILED -> "源码解析失败"
        ConflictReason.UNSUPPORTED_SOURCE -> "源码结构暂不支持"
        ConflictReason.FILE_NOT_MANAGED -> "文件不是托管文件"
        ConflictReason.MARKER_MISMATCH -> "托管标记不匹配"
    }
}

fun SyncConflictResolution.label(): String {
    return when (this) {
        SyncConflictResolution.METADATA_WINS -> "以元数据为准"
        SyncConflictResolution.SOURCE_WINS -> "以源码为准"
    }
}

fun ValidationSeverity.label(): String {
    return when (this) {
        ValidationSeverity.INFO -> "提示"
        ValidationSeverity.WARNING -> "警告"
        ValidationSeverity.ERROR -> "错误"
    }
}
