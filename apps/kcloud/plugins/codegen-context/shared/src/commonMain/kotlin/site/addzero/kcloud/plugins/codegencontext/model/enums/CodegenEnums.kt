package site.addzero.kcloud.plugins.codegencontext.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义代码生成消费目标枚举。
 */
enum class CodegenConsumerTarget {
    MCU_CONSOLE,
}

@Serializable
/**
 * 定义代码生成node类型枚举。
 */
enum class CodegenNodeKind {
    CLASS,
    METHOD,
    FIELD,
}

@Serializable
/**
 * 定义代码生成类型枚举。
 */
enum class CodegenClassKind {
    SERVICE,
    MODEL,
    ENUM,
}

@Serializable
/**
 * 定义代码生成上下文值类型枚举。
 */
enum class CodegenContextValueType {
    STRING,
    TEXT,
    INT,
    LONG,
    DECIMAL,
    BOOLEAN,
    ENUM,
    PATH,
}

@Serializable
/**
 * 定义代码生成绑定目标模式枚举。
 */
enum class CodegenBindingTargetMode {
    SINGLE,
    MULTIPLE,
}

@Serializable
/**
 * 定义代码生成定义来源类型枚举。
 */
enum class CodegenDefinitionSourceKind {
    BUILTIN,
    CUSTOM,
}
