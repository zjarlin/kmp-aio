package site.addzero.kcloud.plugins.hostconfig.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogDetailFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldWidgetType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogValueRenderType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigBooleanField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigKeyValueRow
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigOption
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigSelectionField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTextField
import site.addzero.kcloud.plugins.hostconfig.common.orDash

private val CatalogJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

data class CatalogFormDraft(
    val stringValues: Map<String, String> = emptyMap(),
    val booleanValues: Map<String, Boolean> = emptyMap(),
    val multiValues: Map<String, Set<String>> = emptyMap(),
) {
    fun stringValue(key: String): String = stringValues[key].orEmpty()

    fun booleanValue(key: String): Boolean = booleanValues[key] ?: false

    fun multiValue(key: String): Set<String> = multiValues[key].orEmpty()

    fun updateString(key: String, value: String): CatalogFormDraft {
        return copy(stringValues = stringValues + (key to value))
    }

    fun updateBoolean(key: String, value: Boolean): CatalogFormDraft {
        return copy(booleanValues = booleanValues + (key to value))
    }

    fun updateMulti(key: String, value: Set<String>): CatalogFormDraft {
        return copy(multiValues = multiValues + (key to value))
    }

    fun requiredFieldMessage(
        metadata: CatalogEntityMetadataResponse,
    ): String? {
        return metadata.formFields.firstNotNullOfOrNull { field ->
            when {
                !field.required -> null
                field.widget == CatalogFieldWidgetType.BOOLEAN -> null
                field.widget == CatalogFieldWidgetType.MULTI_SELECT && multiValue(field.key).isEmpty() ->
                    "${field.label}不能为空"

                stringValue(field.key).isBlank() -> "${field.label}不能为空"
                else -> null
            }
        }
    }

    fun toProductCreateRequest(): ProductDefinitionCreateRequest {
        return ProductDefinitionCreateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            vendor = optionalText("vendor"),
            category = optionalText("category"),
            enabled = booleanValue("enabled"),
            sortIndex = intValue("sortIndex"),
            labelIds = multiValue("labelIds").mapNotNull(String::toLongOrNull),
        )
    }

    fun toProductUpdateRequest(): ProductDefinitionUpdateRequest {
        return ProductDefinitionUpdateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            vendor = optionalText("vendor"),
            category = optionalText("category"),
            enabled = booleanValue("enabled"),
            sortIndex = intValue("sortIndex"),
            labelIds = multiValue("labelIds").mapNotNull(String::toLongOrNull),
        )
    }

    fun toDeviceDefinitionCreateRequest(): DeviceDefinitionCreateRequest {
        return DeviceDefinitionCreateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            deviceTypeId = optionalLong("deviceTypeId"),
            supportsTelemetry = booleanValue("supportsTelemetry"),
            supportsControl = booleanValue("supportsControl"),
            sortIndex = intValue("sortIndex"),
        )
    }

    fun toDeviceDefinitionUpdateRequest(): DeviceDefinitionUpdateRequest {
        return DeviceDefinitionUpdateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            deviceTypeId = optionalLong("deviceTypeId"),
            supportsTelemetry = booleanValue("supportsTelemetry"),
            supportsControl = booleanValue("supportsControl"),
            sortIndex = intValue("sortIndex"),
        )
    }

    fun toPropertyDefinitionCreateRequest(): Result<PropertyDefinitionCreateRequest> {
        return decodeAttributes().map { attributes ->
            PropertyDefinitionCreateRequest(
                identifier = requiredText("identifier"),
                name = requiredText("name"),
                description = optionalText("description"),
                dataTypeId = requiredLong("dataTypeId"),
                unit = optionalText("unit"),
                required = booleanValue("required"),
                writable = booleanValue("writable"),
                telemetry = booleanValue("telemetry"),
                nullable = booleanValue("nullable"),
                length = optionalInt("length"),
                attributes = attributes,
                sortIndex = intValue("sortIndex"),
            )
        }
    }

    fun toPropertyDefinitionUpdateRequest(): Result<PropertyDefinitionUpdateRequest> {
        return decodeAttributes().map { attributes ->
            PropertyDefinitionUpdateRequest(
                identifier = requiredText("identifier"),
                name = requiredText("name"),
                description = optionalText("description"),
                dataTypeId = requiredLong("dataTypeId"),
                unit = optionalText("unit"),
                required = booleanValue("required"),
                writable = booleanValue("writable"),
                telemetry = booleanValue("telemetry"),
                nullable = booleanValue("nullable"),
                length = optionalInt("length"),
                attributes = attributes,
                sortIndex = intValue("sortIndex"),
            )
        }
    }

    fun toFeatureDefinitionCreateRequest(): FeatureDefinitionCreateRequest {
        return FeatureDefinitionCreateRequest(
            identifier = requiredText("identifier"),
            name = requiredText("name"),
            description = optionalText("description"),
            inputSchema = optionalText("inputSchema"),
            outputSchema = optionalText("outputSchema"),
            asynchronous = booleanValue("asynchronous"),
            sortIndex = intValue("sortIndex"),
        )
    }

    fun toFeatureDefinitionUpdateRequest(): FeatureDefinitionUpdateRequest {
        return FeatureDefinitionUpdateRequest(
            identifier = requiredText("identifier"),
            name = requiredText("name"),
            description = optionalText("description"),
            inputSchema = optionalText("inputSchema"),
            outputSchema = optionalText("outputSchema"),
            asynchronous = booleanValue("asynchronous"),
            sortIndex = intValue("sortIndex"),
        )
    }

    fun toLabelDefinitionCreateRequest(): LabelDefinitionCreateRequest {
        return LabelDefinitionCreateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            colorHex = optionalText("colorHex"),
            sortIndex = intValue("sortIndex"),
        )
    }

    fun toLabelDefinitionUpdateRequest(): LabelDefinitionUpdateRequest {
        return LabelDefinitionUpdateRequest(
            code = requiredText("code"),
            name = requiredText("name"),
            description = optionalText("description"),
            colorHex = optionalText("colorHex"),
            sortIndex = intValue("sortIndex"),
        )
    }

    private fun requiredText(key: String): String = stringValue(key).trim()

    private fun optionalText(key: String): String? = stringValue(key).trim().ifBlank { null }

    private fun intValue(key: String): Int = optionalInt(key) ?: 0

    private fun optionalInt(key: String): Int? = stringValue(key).trim().ifBlank { null }?.toIntOrNull()

    private fun requiredLong(key: String): Long = optionalLong(key) ?: 0L

    private fun optionalLong(key: String): Long? = stringValue(key).trim().ifBlank { null }?.toLongOrNull()

    private fun decodeAttributes(): Result<Map<String, String>> {
        val text = stringValue("attributes").trim()
        if (text.isBlank()) {
            return Result.success(emptyMap())
        }
        return runCatching {
            CatalogJson.decodeFromString<Map<String, String>>(text)
        }.recoverCatching {
            throw IllegalArgumentException("扩展属性必须是 JSON 对象，且 value 需为字符串")
        }
    }

    companion object {
        fun from(
            metadata: CatalogEntityMetadataResponse,
            initialValues: Map<String, Any?>,
        ): CatalogFormDraft {
            val stringValues = metadata.formFields
                .filter { field ->
                    field.widget != CatalogFieldWidgetType.BOOLEAN &&
                        field.widget != CatalogFieldWidgetType.MULTI_SELECT
                }.associate { field ->
                    field.key to initialValues.toStringValue(field)
                }
            val booleanValues = metadata.formFields
                .filter { field -> field.widget == CatalogFieldWidgetType.BOOLEAN }
                .associate { field ->
                    field.key to (initialValues[field.key] as? Boolean ?: false)
                }
            val multiValues = metadata.formFields
                .filter { field -> field.widget == CatalogFieldWidgetType.MULTI_SELECT }
                .associate { field ->
                    field.key to initialValues.toMultiValue(field)
                }
            return CatalogFormDraft(
                stringValues = stringValues,
                booleanValues = booleanValues,
                multiValues = multiValues,
            )
        }
    }
}

fun defaultCatalogValues(
    entityType: CatalogEntityType,
): Map<String, Any?> {
    return when (entityType) {
        CatalogEntityType.PRODUCT -> mapOf(
            "enabled" to true,
            "sortIndex" to 0,
            "labelIds" to emptyList<String>(),
        )

        CatalogEntityType.DEVICE -> mapOf(
            "supportsTelemetry" to true,
            "supportsControl" to false,
            "sortIndex" to 0,
        )

        CatalogEntityType.PROPERTY -> mapOf(
            "required" to false,
            "writable" to false,
            "telemetry" to true,
            "nullable" to true,
            "sortIndex" to 0,
            "attributes" to emptyMap<String, String>(),
        )

        CatalogEntityType.FEATURE -> mapOf(
            "asynchronous" to false,
            "sortIndex" to 0,
        )

        CatalogEntityType.LABEL -> mapOf(
            "sortIndex" to 0,
        )
    }
}

fun ProductDefinitionTreeResponse.toCatalogValues(): Map<String, Any?> {
    return mapOf(
        "code" to code,
        "name" to name,
        "description" to description,
        "vendor" to vendor,
        "category" to category,
        "enabled" to enabled,
        "sortIndex" to sortIndex,
        "labelIds" to labels.map { label -> label.id.toString() },
        "labels" to labels.map { label -> label.name },
        "updatedAt" to updatedAt,
    )
}

fun DeviceDefinitionTreeResponse.toCatalogValues(): Map<String, Any?> {
    return mapOf(
        "code" to code,
        "name" to name,
        "description" to description,
        "deviceTypeId" to deviceTypeId,
        "deviceTypeName" to deviceTypeName,
        "supportsTelemetry" to supportsTelemetry,
        "supportsControl" to supportsControl,
        "sortIndex" to sortIndex,
        "updatedAt" to updatedAt,
    )
}

fun PropertyDefinitionResponse.toCatalogValues(): Map<String, Any?> {
    return mapOf(
        "identifier" to identifier,
        "name" to name,
        "description" to description,
        "dataTypeId" to dataTypeId,
        "dataTypeName" to dataTypeName,
        "unit" to unit,
        "required" to required,
        "writable" to writable,
        "telemetry" to telemetry,
        "nullable" to nullable,
        "length" to length,
        "attributes" to attributes,
        "sortIndex" to sortIndex,
        "updatedAt" to updatedAt,
    )
}

fun FeatureDefinitionResponse.toCatalogValues(): Map<String, Any?> {
    return mapOf(
        "identifier" to identifier,
        "name" to name,
        "description" to description,
        "inputSchema" to inputSchema,
        "outputSchema" to outputSchema,
        "asynchronous" to asynchronous,
        "sortIndex" to sortIndex,
        "updatedAt" to updatedAt,
    )
}

fun LabelDefinitionResponse.toCatalogValues(): Map<String, Any?> {
    return mapOf(
        "code" to code,
        "name" to name,
        "description" to description,
        "colorHex" to colorHex,
        "sortIndex" to sortIndex,
        "updatedAt" to updatedAt,
    )
}

@Composable
fun CatalogMetadataDetailContent(
    metadata: CatalogEntityMetadataResponse,
    values: Map<String, Any?>,
    modifier: Modifier = Modifier,
) {
    val rows = metadata.detailFields.map { field ->
        field.label to renderDetailValue(field, values[field.key])
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { (label, value) ->
            HostConfigKeyValueRow(label, value)
        }
    }
}

@Composable
fun CatalogMetadataFormContent(
    metadata: CatalogEntityMetadataResponse,
    optionSets: Map<String, List<CatalogFieldOptionResponse>>,
    draft: CatalogFormDraft,
    onDraftChange: (CatalogFormDraft) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        metadata.formFields.forEach { field ->
            val displayLabel = if (field.required) {
                "${field.label} *"
            } else {
                field.label
            }
            when (field.widget) {
                CatalogFieldWidgetType.TEXT,
                CatalogFieldWidgetType.NUMBER,
                -> HostConfigTextField(
                    label = displayLabel,
                    value = draft.stringValue(field.key),
                    onValueChange = { value ->
                        onDraftChange(draft.updateString(field.key, value))
                    },
                    placeholder = field.placeholder.orEmpty(),
                )

                CatalogFieldWidgetType.TEXTAREA,
                CatalogFieldWidgetType.JSON,
                -> HostConfigTextField(
                    label = displayLabel,
                    value = draft.stringValue(field.key),
                    onValueChange = { value ->
                        onDraftChange(draft.updateString(field.key, value))
                    },
                    placeholder = field.placeholder.orEmpty(),
                    singleLine = false,
                )

                CatalogFieldWidgetType.BOOLEAN -> HostConfigBooleanField(
                    label = displayLabel,
                    checked = draft.booleanValue(field.key),
                    onCheckedChange = { checked ->
                        onDraftChange(draft.updateBoolean(field.key, checked))
                    },
                    description = field.helperText,
                )

                CatalogFieldWidgetType.SELECT -> HostConfigSelectionField(
                    label = displayLabel,
                    options = resolveOptions(field, optionSets).map { option ->
                        HostConfigOption(
                            value = option.value,
                            label = option.label,
                            caption = option.description,
                        )
                    },
                    selectedValue = draft.stringValue(field.key).ifBlank { null },
                    onSelected = { selected ->
                        onDraftChange(draft.updateString(field.key, selected.orEmpty()))
                    },
                    placeholder = field.placeholder ?: "请选择${field.label}",
                    allowClear = !field.required,
                )

                CatalogFieldWidgetType.MULTI_SELECT -> CatalogMultiSelectionField(
                    label = displayLabel,
                    options = resolveOptions(field, optionSets),
                    selectedValues = draft.multiValue(field.key),
                    helperText = field.helperText,
                    onSelectionChange = { selected ->
                        onDraftChange(draft.updateMulti(field.key, selected))
                    },
                )
            }
            field.helperText
                ?.takeIf { text -> text.isNotBlank() && field.widget != CatalogFieldWidgetType.BOOLEAN }
                ?.let { text ->
                    CupertinoText(
                        text = text,
                        style = CupertinoTheme.typography.footnote,
                        color = CupertinoTheme.colorScheme.secondaryLabel,
                    )
                }
        }
    }
}

@Composable
private fun CatalogMultiSelectionField(
    label: String,
    options: List<CatalogFieldOptionResponse>,
    selectedValues: Set<String>,
    helperText: String?,
    onSelectionChange: (Set<String>) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CupertinoText(
            text = label,
            style = CupertinoTheme.typography.subhead,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        if (options.isEmpty()) {
            HostConfigStatusStrip("当前没有可选项")
        } else {
            options.forEach { option ->
                HostConfigBooleanField(
                    label = option.label,
                    checked = selectedValues.contains(option.value),
                    onCheckedChange = { checked ->
                        val nextValues = selectedValues.toMutableSet().apply {
                            if (checked) {
                                add(option.value)
                            } else {
                                remove(option.value)
                            }
                        }
                        onSelectionChange(nextValues)
                    },
                    description = option.description,
                )
            }
        }
        helperText?.takeIf(String::isNotBlank)?.let { text ->
            CupertinoText(
                text = text,
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }
    }
}

private fun Map<String, Any?>.toStringValue(
    field: CatalogFieldMetadataResponse,
): String {
    val value = this[field.key]
    if (value == null) {
        return ""
    }
    return when {
        field.widget == CatalogFieldWidgetType.JSON && value is Map<*, *> ->
            CatalogJson.encodeToString(
                value.entries.associate { entry ->
                    entry.key.toString() to entry.value.toString()
                },
            )

        value is Iterable<*> -> value.joinToString(",") { item -> item.toString() }
        else -> value.toString()
    }
}

private fun Map<String, Any?>.toMultiValue(
    field: CatalogFieldMetadataResponse,
): Set<String> {
    val value = this[field.key] ?: return emptySet()
    return when (value) {
        is Set<*> -> value.map(Any?::toString).toSet()
        is Iterable<*> -> value.map(Any?::toString).toSet()
        else -> setOf(value.toString())
    }
}

private fun resolveOptions(
    field: CatalogFieldMetadataResponse,
    optionSets: Map<String, List<CatalogFieldOptionResponse>>,
): List<CatalogFieldOptionResponse> {
    return field.optionSource?.let { key -> optionSets[key] }.orEmpty().ifEmpty { field.options }
}

private fun renderDetailValue(
    field: CatalogDetailFieldMetadataResponse,
    value: Any?,
): String {
    return when (field.renderType) {
        CatalogValueRenderType.BOOLEAN -> if (value as? Boolean == true) "是" else "否"
        CatalogValueRenderType.TAGS -> (value as? Iterable<*>)?.joinToString("、") { item ->
            item.toString()
        }.orDash()

        CatalogValueRenderType.DATETIME -> formatEpochMillis(value as? Number)
        CatalogValueRenderType.CODE,
        CatalogValueRenderType.TEXT,
        CatalogValueRenderType.BADGE,
        -> value?.toString().orDash()
    }
}

private fun formatEpochMillis(
    value: Number?,
): String {
    val epochMillis = value?.toLong() ?: return "未设置"
    return runCatching {
        val localDateTime = Instant
            .fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        buildString {
            append(localDateTime.date)
            append(' ')
            append(localDateTime.hour.pad2())
            append(':')
            append(localDateTime.minute.pad2())
            append(':')
            append(localDateTime.second.pad2())
        }
    }.getOrDefault(epochMillis.toString())
}

private fun Int.pad2(): String = toString().padStart(2, '0')
