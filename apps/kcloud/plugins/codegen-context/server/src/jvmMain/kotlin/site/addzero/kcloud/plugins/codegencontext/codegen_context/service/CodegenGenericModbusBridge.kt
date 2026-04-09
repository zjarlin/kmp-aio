package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextParamDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType

internal const val MODBUS_OPERATION_DEFINITION_CODE = "MODBUS_OPERATION"
internal const val MODBUS_FIELD_DEFINITION_CODE = "MODBUS_FIELD"

/**
 * 表示modbus结构direction。
 */
internal enum class ModbusSchemaDirection {
    READ,
    WRITE,
}

/**
 * 表示modbusfunction编码。
 */
internal enum class ModbusFunctionCode {
    READ_COILS,
    READ_DISCRETE_INPUTS,
    READ_INPUT_REGISTERS,
    READ_HOLDING_REGISTERS,
    WRITE_SINGLE_COIL,
    WRITE_MULTIPLE_COILS,
    WRITE_SINGLE_REGISTER,
    WRITE_MULTIPLE_REGISTERS,
}

/**
 * 表示modbus传输类型。
 */
internal enum class ModbusTransportType {
    BOOL_COIL,
    U16,
    U32_BE,
    STRING_ASCII,
    STRING_UTF8,
}

/**
 * 表示modbus字段spec。
 *
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property propertyName 属性名。
 * @property transportType 传输类型。
 * @property registerOffset 寄存器offset。
 * @property bitOffset 位offset。
 * @property length length。
 * @property translationHint translationhint。
 * @property defaultLiteral 默认字面量。
 */
internal data class ModbusFieldSpec(
    val name: String,
    val description: String?,
    val sortIndex: Int,
    val propertyName: String,
    val transportType: ModbusTransportType,
    val registerOffset: Int,
    val bitOffset: Int,
    val length: Int,
    val translationHint: String?,
    val defaultLiteral: String?,
)

/**
 * 表示modbus结构spec。
 *
 * @property name 名称。
 * @property description 描述。
 * @property sortIndex 排序序号。
 * @property direction direction。
 * @property functionCode function编码。
 * @property baseAddress base地址。
 * @property methodName 方法名。
 * @property modelName 模型名称。
 * @property fields fields。
 */
internal data class ModbusSchemaSpec(
    val name: String,
    val description: String?,
    val sortIndex: Int,
    val direction: ModbusSchemaDirection,
    val functionCode: ModbusFunctionCode,
    val baseAddress: Int,
    val methodName: String,
    val modelName: String?,
    val fields: List<ModbusFieldSpec>,
)

/**
 * 处理代码生成上下文详情数据传输对象。
 */
internal fun CodegenContextDetailDto.toModbusSpecs(): List<ModbusSchemaSpec> {
    if (classes.isEmpty()) {
        return emptyList()
    }
    return classes.toModbusSpecs(protocolTemplateId, availableContextDefinitions)
}

/**
 * 处理列表。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 */
internal fun List<CodegenClassDto>.toModbusSpecs(
    protocolTemplateId: Long,
    availableDefinitions: List<CodegenContextDefinitionDto>,
): List<ModbusSchemaSpec> {
    if (availableDefinitions.isEmpty()) {
        throw BusinessValidationException(
            "Protocol template $protocolTemplateId has no context definitions, so the generic codegen model cannot be bridged to Modbus.",
        )
    }
    val modelClasses =
        filter { codegenClass -> codegenClass.classKind == CodegenClassKind.MODEL }
            .associateBy(CodegenClassDto::className)
    return filter { codegenClass -> codegenClass.classKind == CodegenClassKind.SERVICE }
        .sortedBy(CodegenClassDto::sortIndex)
        .flatMap { serviceClass ->
            serviceClass.methods
                .sortedBy(CodegenMethodDto::sortIndex)
                .mapNotNull { method ->
                    method.toModbusSchemaOrNull(
                        protocolTemplateId = protocolTemplateId,
                        availableDefinitions = availableDefinitions,
                        modelClasses = modelClasses,
                    )
                }
        }
}

/**
 * 校验modbus结构。
 *
 * @param schema 结构。
 * @param identifierPattern identifierpattern。
 */
internal fun validateModbusSchema(
    schema: ModbusSchemaSpec,
    identifierPattern: Regex,
) {
    if (schema.name.isBlank()) {
        throw BusinessValidationException("Schema name cannot be blank.")
    }
    if (!identifierPattern.matches(schema.methodName)) {
        throw BusinessValidationException("Schema methodName '${schema.methodName}' is not a valid Kotlin identifier.")
    }
    if (schema.fields.isEmpty()) {
        throw BusinessValidationException("Schema ${schema.methodName} must define at least one field.")
    }
    if (schema.direction == ModbusSchemaDirection.READ && schema.modelName.isNullOrBlank()) {
        throw BusinessValidationException("READ schema ${schema.methodName} must define modelName.")
    }
    val modelName = schema.modelName
    if (!modelName.isNullOrBlank() && !identifierPattern.matches(modelName)) {
        throw BusinessValidationException("Schema modelName '$modelName' is not a valid Kotlin identifier.")
    }
    ensureDirectionMatchesFunctionCode(schema)
    val duplicateProperties =
        schema.fields.groupBy(ModbusFieldSpec::propertyName).filterValues { fields -> fields.size > 1 }.keys
    if (duplicateProperties.isNotEmpty()) {
        throw BusinessValidationException(
            "Schema ${schema.methodName} has duplicate propertyName values: ${duplicateProperties.joinToString()}.",
        )
    }
    schema.fields.forEach { field ->
        validateModbusField(schema, field, identifierPattern)
    }
    validateModbusFieldOverlaps(schema)
    when (schema.functionCode) {
        ModbusFunctionCode.WRITE_SINGLE_COIL -> {
            if (schema.fields.size != 1 || schema.fields.single().transportType != ModbusTransportType.BOOL_COIL) {
                throw BusinessValidationException("WRITE_SINGLE_COIL requires exactly one BOOL_COIL field.")
            }
        }

        ModbusFunctionCode.WRITE_SINGLE_REGISTER -> {
            if (schema.fields.size != 1 || schema.fields.single().transportType != ModbusTransportType.U16) {
                throw BusinessValidationException("WRITE_SINGLE_REGISTER requires exactly one U16 field.")
            }
        }

        else -> Unit
    }
}

/**
 * 解析定义for绑定。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 * @param binding 绑定。
 */
internal fun resolveDefinitionForBinding(
    protocolTemplateId: Long,
    availableDefinitions: List<CodegenContextDefinitionDto>,
    binding: CodegenContextBindingDto,
): CodegenContextDefinitionDto {
    val byId =
        binding.definitionId?.let { definitionId ->
            availableDefinitions.firstOrNull { definition -> definition.id == definitionId }
        }
    val resolved =
        byId ?: availableDefinitions.firstOrNull { definition -> definition.code == binding.definitionCode }
        ?: throw BusinessValidationException(
            "Binding ${binding.definitionCode} is not registered for protocol template $protocolTemplateId.",
        )
    if (binding.definitionId != null && binding.definitionCode != resolved.code) {
        throw BusinessValidationException(
            "Binding definitionId ${binding.definitionId} does not match definitionCode ${binding.definitionCode}.",
        )
    }
    return resolved
}

/**
 * 解析参数定义for值。
 *
 * @param definition 定义。
 * @param value 待解析的值。
 */
internal fun resolveParamDefinitionForValue(
    definition: CodegenContextDefinitionDto,
    value: CodegenContextBindingValueDto,
): CodegenContextParamDefinitionDto {
    val byId =
        value.paramDefinitionId?.let { definitionId ->
            definition.params.firstOrNull { param -> param.id == definitionId }
        }
    val resolved =
        byId ?: definition.params.firstOrNull { param -> param.code == value.paramCode }
        ?: throw BusinessValidationException(
            "Binding value ${value.paramCode} is not declared by context definition ${definition.code}.",
        )
    if (value.paramDefinitionId != null && value.paramCode != resolved.code) {
        throw BusinessValidationException(
            "Binding value paramDefinitionId ${value.paramDefinitionId} does not match paramCode ${value.paramCode}.",
        )
    }
    return resolved
}

/**
 * 校验绑定值类型。
 *
 * @param paramDefinition 参数定义。
 * @param rawValue 原始值。
 */
internal fun validateBindingValueType(
    paramDefinition: CodegenContextParamDefinitionDto,
    rawValue: String?,
) {
    val value = rawValue?.trim()
    if (paramDefinition.required && value.isNullOrEmpty()) {
        throw BusinessValidationException("Context parameter ${paramDefinition.code} is required.")
    }
    if (value.isNullOrEmpty()) {
        return
    }
    when (paramDefinition.valueType) {
        CodegenContextValueType.STRING,
        CodegenContextValueType.TEXT,
        CodegenContextValueType.PATH -> Unit

        CodegenContextValueType.INT ->
            value.toIntOrNull()
                ?: throw BusinessValidationException("Context parameter ${paramDefinition.code} must be an Int.")

        CodegenContextValueType.LONG ->
            value.toLongOrNull()
                ?: throw BusinessValidationException("Context parameter ${paramDefinition.code} must be a Long.")

        CodegenContextValueType.DECIMAL ->
            value.toBigDecimalOrNull()
                ?: throw BusinessValidationException("Context parameter ${paramDefinition.code} must be a Decimal.")

        CodegenContextValueType.BOOLEAN ->
            value.toBooleanStrictOrNull()
                ?: throw BusinessValidationException("Context parameter ${paramDefinition.code} must be true or false.")

        CodegenContextValueType.ENUM -> {
            if (value !in paramDefinition.enumOptions) {
                throw BusinessValidationException(
                    "Context parameter ${paramDefinition.code} must be one of ${paramDefinition.enumOptions.joinToString()}.",
                )
            }
        }
    }
}

/**
 * 处理代码生成方法数据传输对象。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 * @param modelClasses 模型类。
 */
private fun CodegenMethodDto.toModbusSchemaOrNull(
    protocolTemplateId: Long,
    availableDefinitions: List<CodegenContextDefinitionDto>,
    modelClasses: Map<String, CodegenClassDto>,
): ModbusSchemaSpec? {
    val operationBinding =
        bindings.firstOrNull { binding ->
            resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, binding).code == MODBUS_OPERATION_DEFINITION_CODE
        } ?: return null
    val operationDefinition = resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, operationBinding)
    val direction =
        operationBinding.requiredValue(operationDefinition, "direction").enumValueOrThrow<ModbusSchemaDirection>(
            "Unsupported Modbus direction for method $methodName.",
        )
    val functionCode =
        operationBinding.requiredValue(operationDefinition, "functionCode").enumValueOrThrow<ModbusFunctionCode>(
            "Unsupported Modbus functionCode for method $methodName.",
        )
    val baseAddress = operationBinding.requiredValue(operationDefinition, "baseAddress").toInt()
    val modelClass =
        when (direction) {
            ModbusSchemaDirection.READ -> {
                val responseClass =
                    responseClassName ?: throw BusinessValidationException(
                        "Method $methodName must set responseClassName for READ Modbus generation.",
                    )
                modelClasses[responseClass]
                    ?: throw BusinessValidationException("READ method $methodName references missing model class $responseClass.")
            }

            ModbusSchemaDirection.WRITE -> {
                val requestClass =
                    requestClassName ?: throw BusinessValidationException(
                        "Method $methodName must set requestClassName for WRITE Modbus generation.",
                    )
                modelClasses[requestClass]
                    ?: throw BusinessValidationException("WRITE method $methodName references missing model class $requestClass.")
            }
        }
    return ModbusSchemaSpec(
        name = name,
        description = description,
        sortIndex = sortIndex,
        direction = direction,
        functionCode = functionCode,
        baseAddress = baseAddress,
        methodName = methodName,
        modelName = if (direction == ModbusSchemaDirection.READ) modelClass.className else requestClassName,
        fields =
            modelClass.properties
                .sortedBy(CodegenPropertyDto::sortIndex)
                .map { property ->
                    property.toModbusFieldSpec(protocolTemplateId, availableDefinitions)
                },
    )
}

/**
 * 处理代码生成属性数据传输对象。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 */
private fun CodegenPropertyDto.toModbusFieldSpec(
    protocolTemplateId: Long,
    availableDefinitions: List<CodegenContextDefinitionDto>,
): ModbusFieldSpec {
    val fieldBinding =
        bindings.firstOrNull { binding ->
            resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, binding).code == MODBUS_FIELD_DEFINITION_CODE
        } ?: throw BusinessValidationException(
            "Property $propertyName is used by Modbus generation but has no $MODBUS_FIELD_DEFINITION_CODE binding.",
        )
    val fieldDefinition = resolveDefinitionForBinding(protocolTemplateId, availableDefinitions, fieldBinding)
    val transportType =
        fieldBinding.requiredValue(fieldDefinition, "transportType").enumValueOrThrow<ModbusTransportType>(
            "Unsupported Modbus transportType for property $propertyName.",
        )
    return ModbusFieldSpec(
        name = name,
        description = description,
        sortIndex = sortIndex,
        propertyName = propertyName,
        transportType = transportType,
        registerOffset = fieldBinding.requiredValue(fieldDefinition, "registerOffset").toInt(),
        bitOffset = fieldBinding.optionalValue(fieldDefinition, "bitOffset")?.toInt() ?: 0,
        length = fieldBinding.optionalValue(fieldDefinition, "length")?.toInt() ?: 1,
        translationHint = fieldBinding.optionalValue(fieldDefinition, "translationHint"),
        defaultLiteral = fieldBinding.optionalValue(fieldDefinition, "defaultLiteral") ?: defaultLiteral,
    )
}

/**
 * 校验modbus字段。
 *
 * @param schema 结构。
 * @param field field。
 * @param identifierPattern identifierpattern。
 */
private fun validateModbusField(
    schema: ModbusSchemaSpec,
    field: ModbusFieldSpec,
    identifierPattern: Regex,
) {
    if (field.name.isBlank()) {
        throw BusinessValidationException("Field name cannot be blank in schema ${schema.methodName}.")
    }
    if (!identifierPattern.matches(field.propertyName)) {
        throw BusinessValidationException("Field propertyName '${field.propertyName}' is not a valid Kotlin identifier.")
    }
    if (field.registerOffset < 0) {
        throw BusinessValidationException("Field ${field.propertyName} must use registerOffset >= 0.")
    }
    if (field.bitOffset != 0) {
        throw BusinessValidationException("Field ${field.propertyName} currently requires bitOffset = 0 in V1.")
    }
    if (field.length < 1) {
        throw BusinessValidationException("Field ${field.propertyName} must use length >= 1.")
    }
    when (field.transportType) {
        ModbusTransportType.BOOL_COIL,
        ModbusTransportType.U16,
        ModbusTransportType.U32_BE -> {
            if (field.length != 1) {
                throw BusinessValidationException("Field ${field.propertyName} only supports length = 1 in V1.")
            }
        }

        ModbusTransportType.STRING_ASCII,
        ModbusTransportType.STRING_UTF8 -> Unit
    }
    val expectsCoil = schema.functionCode.expectsCoilSpace()
    if (expectsCoil && field.transportType != ModbusTransportType.BOOL_COIL) {
        throw BusinessValidationException(
            "Schema ${schema.methodName} uses ${schema.functionCode.name}, so field ${field.propertyName} must be BOOL_COIL.",
        )
    }
    if (!expectsCoil && field.transportType == ModbusTransportType.BOOL_COIL) {
        throw BusinessValidationException(
            "Schema ${schema.methodName} uses ${schema.functionCode.name}, so field ${field.propertyName} cannot be BOOL_COIL.",
        )
    }
}

/**
 * 校验modbus字段overlaps。
 *
 * @param schema 结构。
 */
private fun validateModbusFieldOverlaps(
    schema: ModbusSchemaSpec,
) {
    val occupied = linkedSetOf<String>()
    schema.fields.sortedBy(ModbusFieldSpec::sortIndex).forEach { field ->
        val keys =
            if (schema.functionCode.expectsCoilSpace()) {
                listOf("c${field.registerOffset}")
            } else {
                val width = field.transportType.registerWidth(field.length)
                (field.registerOffset until field.registerOffset + width).map { register ->
                    "r$register"
                }
            }
        keys.forEach { key ->
            if (!occupied.add(key)) {
                throw BusinessValidationException(
                    "Schema ${schema.methodName} has overlapping field layout around ${field.propertyName}.",
                )
            }
        }
    }
}

/**
 * 确保direction匹配function编码。
 *
 * @param schema 结构。
 */
private fun ensureDirectionMatchesFunctionCode(
    schema: ModbusSchemaSpec,
) {
    val allowedFunctionCodes =
        when (schema.direction) {
            ModbusSchemaDirection.READ ->
                setOf(
                    ModbusFunctionCode.READ_COILS,
                    ModbusFunctionCode.READ_DISCRETE_INPUTS,
                    ModbusFunctionCode.READ_INPUT_REGISTERS,
                    ModbusFunctionCode.READ_HOLDING_REGISTERS,
                )

            ModbusSchemaDirection.WRITE ->
                setOf(
                    ModbusFunctionCode.WRITE_SINGLE_COIL,
                    ModbusFunctionCode.WRITE_MULTIPLE_COILS,
                    ModbusFunctionCode.WRITE_SINGLE_REGISTER,
                    ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS,
                )
        }
    if (schema.functionCode !in allowedFunctionCodes) {
        throw BusinessValidationException(
            "Schema ${schema.methodName} uses ${schema.functionCode.name}, which does not match ${schema.direction.name}.",
        )
    }
}

/**
 * 处理modbusfunction编码。
 */
private fun ModbusFunctionCode.expectsCoilSpace(): Boolean =
    when (this) {
        ModbusFunctionCode.READ_COILS,
        ModbusFunctionCode.READ_DISCRETE_INPUTS,
        ModbusFunctionCode.WRITE_SINGLE_COIL,
        ModbusFunctionCode.WRITE_MULTIPLE_COILS -> true
        else -> false
    }

/**
 * 处理modbus传输类型。
 *
 * @param length length。
 */
internal fun ModbusTransportType.registerWidth(
    length: Int,
): Int =
    when (this) {
        ModbusTransportType.BOOL_COIL,
        ModbusTransportType.U16 -> 1
        ModbusTransportType.U32_BE -> 2
        ModbusTransportType.STRING_ASCII,
        ModbusTransportType.STRING_UTF8 -> length
    }

private inline fun <reified T : Enum<T>> String.enumValueOrThrow(
    message: String,
): T {
    return enumValues<T>().firstOrNull { value -> value.name == this }
        ?: throw BusinessValidationException("$message Value: $this")
}

/**
 * 处理代码生成上下文绑定数据传输对象。
 *
 * @param definition 定义。
 * @param paramCode 参数编码。
 */
private fun CodegenContextBindingDto.requiredValue(
    definition: CodegenContextDefinitionDto,
    paramCode: String,
): String {
    return optionalValue(definition, paramCode)
        ?: throw BusinessValidationException("Context definition ${definition.code} requires parameter $paramCode.")
}

/**
 * 处理代码生成上下文绑定数据传输对象。
 *
 * @param definition 定义。
 * @param paramCode 参数编码。
 */
private fun CodegenContextBindingDto.optionalValue(
    definition: CodegenContextDefinitionDto,
    paramCode: String,
): String? {
    val paramDefinition =
        definition.params.firstOrNull { param -> param.code == paramCode }
            ?: throw BusinessValidationException("Context definition ${definition.code} does not declare parameter $paramCode.")
    val resolved =
        values.firstOrNull { value -> value.paramCode == paramCode || value.paramDefinitionId == paramDefinition.id }
            ?.value
            ?.trim()
            ?.takeIf(String::isNotBlank)
    val effective = resolved ?: paramDefinition.defaultValue?.trim()?.takeIf(String::isNotBlank)
    validateBindingValueType(paramDefinition, effective)
    return effective
}
