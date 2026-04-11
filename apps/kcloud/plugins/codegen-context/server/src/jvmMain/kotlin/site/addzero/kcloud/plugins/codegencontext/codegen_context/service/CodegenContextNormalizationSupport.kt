package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenGenerationSettingsDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.util.str.toGeneratedMethodName
import site.addzero.util.str.toGeneratedPropertyName
import site.addzero.util.str.toGeneratedTypeName

private const val FIELD_TRANSPORT_TYPE_PARAM = "transportType"

internal fun CodegenContextDetailDto.normalizeGenericDetail(): CodegenContextDetailDto {
    val trimmedClasses =
        classes.map { codegenClass ->
            codegenClass.copy(
                name = codegenClass.name.trim(),
                description = codegenClass.description.cleanNullable(),
                className = codegenClass.className.trim(),
                packageName = codegenClass.packageName.cleanNullable(),
                bindings =
                    codegenClass.bindings.map { binding ->
                        binding.normalizedBinding()
                    },
                methods =
                    codegenClass.methods.map { method ->
                        method.copy(
                            name = method.name.trim(),
                            description = method.description.cleanNullable(),
                            methodName = method.methodName.trim(),
                            requestClassName = method.requestClassName.cleanNullable(),
                            responseClassName = method.responseClassName.cleanNullable(),
                            bindings =
                                method.bindings.map { binding ->
                                    binding.normalizedBinding()
                                },
                        )
                    },
                properties =
                    codegenClass.properties.map { property ->
                        property.copy(
                            name = property.name.trim(),
                            description = property.description.cleanNullable(),
                            propertyName = property.propertyName.trim(),
                            typeName = property.typeName.trim(),
                            defaultLiteral = property.defaultLiteral.cleanNullable(),
                            bindings =
                                property.bindings.map { binding ->
                                    binding.normalizedBinding()
                                },
                        )
                    },
            )
        }.normalizeGeneratedIdentifiers()
    return copy(
        code = code.trim(),
        name = name.trim(),
        description = description.cleanNullable(),
        enabled = enabled,
        nodeId = nodeId.cleanNullable(),
        consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
        externalCOutputRoot = externalCOutputRoot.cleanNullable(),
        generationSettings = generationSettings.normalizedGenerationSettings(),
        availableContextDefinitions = availableContextDefinitions,
        classes = trimmedClasses,
    )
}

private fun CodegenContextBindingDto.normalizedBinding(): CodegenContextBindingDto {
    return copy(
        definitionCode = definitionCode.trim(),
        values =
            values.map { value ->
                value.copy(
                    paramCode = value.paramCode.trim(),
                    value = value.value.cleanNullable(),
                )
            },
    )
}

private fun List<CodegenClassDto>.normalizeGeneratedIdentifiers(): List<CodegenClassDto> {
    val classesWithResolvedMembers =
        map { codegenClass ->
            codegenClass.copy(
                methods = codegenClass.methods.normalizeGeneratedMethodIdentifiers(),
                properties = codegenClass.properties.normalizeGeneratedPropertyIdentifiers(),
            )
        }
    val requestNameQueues = mutableMapOf<String, ArrayDeque<String>>()
    val responseNameQueues = mutableMapOf<String, ArrayDeque<String>>()
    classesWithResolvedMembers.flatMap(CodegenClassDto::methods).forEach { method ->
        requestNameQueues.getOrPut(method.name) { ArrayDeque() }.addLast(requireNotNull(method.requestClassName))
        responseNameQueues.getOrPut(method.name) { ArrayDeque() }.addLast(requireNotNull(method.responseClassName))
    }
    val usedClassNames = mutableSetOf<String>()
    return classesWithResolvedMembers.map { codegenClass ->
        val explicitClassName = codegenClass.className.cleanNullable()
        val resolvedClassName =
            if (explicitClassName != null) {
                explicitClassName
            } else {
                val matchedMethodClassName =
                    requestNameQueues.pollGeneratedClassName(
                        methodDisplayName = codegenClass.name.removeSuffix("请求实体"),
                        enabled = codegenClass.name.endsWith("请求实体"),
                    ) ?: responseNameQueues.pollGeneratedClassName(
                        methodDisplayName = codegenClass.name.removeSuffix("响应实体"),
                        enabled = codegenClass.name.endsWith("响应实体"),
                    )
                ensureUniqueGeneratedIdentifier(
                    base = matchedMethodClassName ?: codegenClass.name.toGeneratedTypeName(),
                    existing = usedClassNames,
                )
            }
        usedClassNames += resolvedClassName
        codegenClass.copy(className = resolvedClassName)
    }
}

private fun List<CodegenMethodDto>.normalizeGeneratedMethodIdentifiers(): List<CodegenMethodDto> {
    val usedMethodNames = mutableSetOf<String>()
    return map { method ->
        val explicitMethodName = method.methodName.cleanNullable()
        val resolvedMethodName =
            explicitMethodName
                ?: ensureUniqueGeneratedIdentifier(
                    base = method.name.toGeneratedMethodName(),
                    existing = usedMethodNames,
                )
        usedMethodNames += resolvedMethodName
        method.copy(
            methodName = resolvedMethodName,
            requestClassName = method.requestClassName.cleanNullable() ?: resolvedMethodName.toGeneratedTypeName("GeneratedMethod") + "Request",
            responseClassName = method.responseClassName.cleanNullable() ?: resolvedMethodName.toGeneratedTypeName("GeneratedMethod") + "Response",
        )
    }
}

private fun List<CodegenPropertyDto>.normalizeGeneratedPropertyIdentifiers(): List<CodegenPropertyDto> {
    val usedPropertyNames = mutableSetOf<String>()
    return map { property ->
        val explicitPropertyName = property.propertyName.cleanNullable()
        val resolvedPropertyName =
            explicitPropertyName
                ?: ensureUniqueGeneratedIdentifier(
                    base = property.name.toGeneratedPropertyName(),
                    existing = usedPropertyNames,
                )
        usedPropertyNames += resolvedPropertyName
        property.copy(
            propertyName = resolvedPropertyName,
            typeName = property.typeName.cleanNullable() ?: property.bindings.bindingValue(MODBUS_FIELD_DEFINITION_CODE, FIELD_TRANSPORT_TYPE_PARAM).toPropertyTypeName(),
        )
    }
}

private fun MutableMap<String, ArrayDeque<String>>.pollGeneratedClassName(
    methodDisplayName: String,
    enabled: Boolean,
): String? {
    if (!enabled) {
        return null
    }
    val queue = get(methodDisplayName) ?: return null
    if (queue.isEmpty()) {
        remove(methodDisplayName)
        return null
    }
    val value = queue.removeFirst()
    if (queue.isEmpty()) {
        remove(methodDisplayName)
    }
    return value
}

private fun List<CodegenContextBindingDto>.bindingValue(
    definitionCode: String,
    paramCode: String,
): String? =
    firstOrNull { binding -> binding.definitionCode == definitionCode }
        ?.values
        ?.firstOrNull { value -> value.paramCode == paramCode }
        ?.value
        ?.cleanNullable()

private fun String?.toPropertyTypeName(): String =
    when (this) {
        "BOOL_COIL" -> "Boolean"
        "STRING_ASCII",
        "STRING_UTF8" -> "String"
        else -> "Int"
    }

private fun ensureUniqueGeneratedIdentifier(
    base: String,
    existing: Collection<String>,
): String {
    val candidateBase = base.ifBlank { "generatedName" }
    if (candidateBase !in existing) {
        return candidateBase
    }
    var index = 2
    while (true) {
        val candidate = "$candidateBase$index"
        if (candidate !in existing) {
            return candidate
        }
        index += 1
    }
}

internal fun CodegenGenerationSettingsDto.normalizedGenerationSettings(): CodegenGenerationSettingsDto {
    return copy(
        serverOutputRoot = serverOutputRoot.cleanNullable(),
        sharedOutputRoot = sharedOutputRoot.cleanNullable(),
        gatewayOutputRoot = gatewayOutputRoot.cleanNullable(),
        apiClientOutputRoot = apiClientOutputRoot.cleanNullable(),
        apiClientPackageName = apiClientPackageName.cleanNullable(),
        springRouteOutputRoot = springRouteOutputRoot.cleanNullable(),
        cOutputRoot = cOutputRoot.cleanNullable(),
        markdownOutputRoot = markdownOutputRoot.cleanNullable(),
        rtuDefaults =
            rtuDefaults.copy(
                portPath = rtuDefaults.portPath.trim(),
                parity = rtuDefaults.parity.trim(),
            ),
        tcpDefaults =
            tcpDefaults.copy(
                host = tcpDefaults.host.trim(),
            ),
        mqttDefaults =
            mqttDefaults.copy(
                brokerUrl = mqttDefaults.brokerUrl.trim(),
                clientId = mqttDefaults.clientId.trim(),
                requestTopic = mqttDefaults.requestTopic.trim(),
                responseTopic = mqttDefaults.responseTopic.trim(),
            ),
    )
}

internal fun String?.cleanNullable(): String? {
    return this?.trim()?.takeIf(String::isNotBlank)
}
