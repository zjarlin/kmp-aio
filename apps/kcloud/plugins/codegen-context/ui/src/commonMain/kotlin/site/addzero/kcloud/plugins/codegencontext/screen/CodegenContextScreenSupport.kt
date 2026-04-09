package site.addzero.kcloud.plugins.codegencontext.screen

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextParamDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenMethodEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenPropertyEditorState

internal const val MODBUS_OPERATION_DEFINITION_CODE = "MODBUS_OPERATION"
internal const val MODBUS_FIELD_DEFINITION_CODE = "MODBUS_FIELD"
internal const val METHOD_DIRECTION_PARAM = "direction"
internal const val METHOD_FUNCTION_CODE_PARAM = "functionCode"
internal const val METHOD_BASE_ADDRESS_PARAM = "baseAddress"
internal const val FIELD_TRANSPORT_TYPE_PARAM = "transportType"
internal const val FIELD_REGISTER_OFFSET_PARAM = "registerOffset"
internal const val FIELD_BIT_OFFSET_PARAM = "bitOffset"
internal const val FIELD_LENGTH_PARAM = "length"

/**
 * 处理代码生成方法editor状态。
 */
internal fun CodegenMethodEditorState.effectiveMethodName(): String =
    methodName.ifBlank { "保存后自动生成" }

/**
 * 处理代码生成方法editor状态。
 */
internal fun CodegenMethodEditorState.signaturePreview(): String =
    if (methodName.isBlank()) {
        "方法名留空时由服务端自动生成，请求/响应实体名也在保存后统一派生。"
    } else {
        "当前方法名：$methodName，请求/响应实体名在保存后由服务端统一派生。"
    }

/**
 * 处理代码生成属性editor状态。
 */
internal fun CodegenPropertyEditorState.signaturePreview(): String =
    if (propertyName.isBlank()) {
        "属性名留空时由服务端自动生成，类型留空时按 transportType 由服务端推断。"
    } else {
        "$propertyName: ${typeName.ifBlank { "保存后自动推断" }}${if (nullable) "?" else ""}"
    }

/**
 * 处理代码生成属性editor状态。
 */
internal fun CodegenPropertyEditorState.effectivePropertyName(): String =
    propertyName.ifBlank { "保存后自动生成" }

/**
 * 处理代码生成方法editor状态。
 */
internal fun CodegenMethodEditorState.modbusSummary(): String {
    val direction = bindingSummaryValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_DIRECTION_PARAM).orEmpty()
    val functionCode = bindingSummaryValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_FUNCTION_CODE_PARAM).orEmpty()
    val baseAddress = bindingSummaryValue(MODBUS_OPERATION_DEFINITION_CODE, METHOD_BASE_ADDRESS_PARAM).orEmpty()
    return if (direction.isBlank() && functionCode.isBlank() && baseAddress.isBlank()) {
        "当前方法还没有绑定任何 Modbus 操作上下文。"
    } else {
        "协议上下文：direction=${direction.ifBlank { "-" }} · functionCode=${functionCode.ifBlank { "-" }} · baseAddress=${baseAddress.ifBlank { "-" }}"
    }
}

/**
 * 处理代码生成属性editor状态。
 */
internal fun CodegenPropertyEditorState.modbusSummary(): String {
    val transportType = bindingSummaryValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_TRANSPORT_TYPE_PARAM).orEmpty()
    val registerOffset = bindingSummaryValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_REGISTER_OFFSET_PARAM).orEmpty()
    val bitOffset = bindingSummaryValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_BIT_OFFSET_PARAM).orEmpty()
    val length = bindingSummaryValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_LENGTH_PARAM).orEmpty()
    return if (transportType.isBlank() && registerOffset.isBlank() && bitOffset.isBlank()) {
        "当前字段还没有绑定任何 Modbus 传输上下文。"
    } else {
        "协议上下文：transportType=${transportType.ifBlank { "-" }} · registerOffset=${registerOffset.ifBlank { "-" }} · bitOffset=${bitOffset.ifBlank { "-" }} · length=${length.ifBlank { "-" }}"
    }
}

/**
 * 处理代码生成方法editor状态。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 */
internal fun CodegenMethodEditorState.bindingSummaryValue(
    definitionCode: String,
    paramCode: String,
): String? =
    bindings.firstOrNull { it.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { it.paramCode == paramCode }
        ?.value
        ?.takeIf(String::isNotBlank)

/**
 * 处理代码生成属性editor状态。
 *
 * @param definitionCode 定义编码。
 * @param paramCode 参数编码。
 */
internal fun CodegenPropertyEditorState.bindingSummaryValue(
    definitionCode: String,
    paramCode: String,
): String? =
    bindings.firstOrNull { it.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { it.paramCode == paramCode }
        ?.value
        ?.takeIf(String::isNotBlank)

/**
 * 处理协议模板选项数据传输对象。
 */
internal fun ProtocolTemplateOptionDto.protocolContextHint(): String? {
    return when {
        code.contains("rtu", ignoreCase = true) ->
            "当前模板是 Modbus RTU。方法和字段的 context 会重点落到 RTU 语义，RTU 默认参数则来自下方生成设置。"

        code.contains("tcp", ignoreCase = true) ->
            "当前模板是 Modbus TCP。业务方法仍然按统一模型维护，TCP 网络参数由生成设置补齐。"

        code.contains("mqtt", ignoreCase = true) ->
            "当前模板是 MQTT。业务方法和字段先按统一模型定义，MQTT 通道参数由生成设置补齐。"

        else -> null
    }
}

/**
 * 处理string。
 */
internal fun String.externalOutputHint(): String =
    "外部产物会以这个目录为根继续派生，避免把生成文件写进手工维护的源码目录。"

/**
 * 处理绑定字段描述。
 *
 * @param param 参数。
 */
internal fun bindingFieldDescription(
    param: CodegenContextParamDefinitionDto,
): String? {
    return buildString {
        param.description?.takeIf { it.isNotBlank() }?.let(::append)
        if (param.required) {
            if (isNotBlank()) {
                append(" ")
            }
            append("必填。")
        }
    }.ifBlank { null }
}
