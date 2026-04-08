package site.addzero.kcloud.plugins.codegencontext.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class CodegenConsumerTarget {
    MCU_CONSOLE,
}

@Serializable
enum class CodegenSchemaDirection {
    READ,
    WRITE,
}

@Serializable
enum class CodegenFunctionCode {
    READ_COILS,
    READ_DISCRETE_INPUTS,
    READ_INPUT_REGISTERS,
    READ_HOLDING_REGISTERS,
    WRITE_SINGLE_COIL,
    WRITE_MULTIPLE_COILS,
    WRITE_SINGLE_REGISTER,
    WRITE_MULTIPLE_REGISTERS,
}

@Serializable
enum class CodegenTransportType {
    BOOL_COIL,
    U16,
    U32_BE,
    STRING_ASCII,
    STRING_UTF8,
}
