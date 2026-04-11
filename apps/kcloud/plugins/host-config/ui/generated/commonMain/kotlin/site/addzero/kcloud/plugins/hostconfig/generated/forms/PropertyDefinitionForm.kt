package site.addzero.kcloud.plugins.hostconfig.generated.forms

import androidx.compose.runtime.*
import site.addzero.component.high_level.AddMultiColumnContainer
import site.addzero.component.drawer.AddDrawer
import site.addzero.component.form.date.AddDateField
import site.addzero.component.form.switch.AddSwitchField
import site.addzero.component.form.text.AddTextField
import site.addzero.kcloud.plugins.hostconfig.generated.isomorphic.*
import site.addzero.kcloud.plugins.hostconfig.model.enums.*

/**
 * PropertyDefinition 表单属性常量
 */
object PropertyDefinitionFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val identifier = "identifier"
    const val name = "name"
    const val description = "description"
    const val unit = "unit"
    const val required = "required"
    const val writable = "writable"
    const val telemetry = "telemetry"
    const val nullable = "nullable"
    const val length = "length"
    const val attributesJson = "attributesJson"
    const val sortIndex = "sortIndex"
    const val deviceDefinition = "deviceDefinition"
    const val node = "node"
    const val dataType = "dataType"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "identifier", "name", "description", "unit", "required", "writable", "telemetry", "nullable", "length", "attributesJson", "sortIndex", "deviceDefinition", "node", "dataType")
}

@Composable
fun PropertyDefinitionForm(
    state: MutableState<PropertyDefinitionIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: PropertyDefinitionFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        PropertyDefinitionFormOriginal(state, dslConfig)
    }
}

@Composable
fun PropertyDefinitionFormOriginal(
    state: MutableState<PropertyDefinitionIso>,
    dslConfig: PropertyDefinitionFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = PropertyDefinitionFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        PropertyDefinitionFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toLongOrNull()
                    if (parsed != null) {
                        state.value = state.value.copy(createdAt = parsed)
                    }
                },
                label = "createdAt",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toLongOrNull()
                    if (parsed != null) {
                        state.value = state.value.copy(updatedAt = parsed)
                    }
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.identifier to {
            AddTextField(
                value = state.value.identifier?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(identifier = value)
                },
                label = "identifier",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.unit to {
            AddTextField(
                value = state.value.unit?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(unit = value.ifEmpty { null })
                },
                label = "unit",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.required to {
            AddSwitchField(
                value = state.value.required ?: false,
                onValueChange = { state.value = state.value.copy(required = it) },
                label = "required"
            )
        },
        PropertyDefinitionFormProps.writable to {
            AddSwitchField(
                value = state.value.writable ?: false,
                onValueChange = { state.value = state.value.copy(writable = it) },
                label = "writable"
            )
        },
        PropertyDefinitionFormProps.telemetry to {
            AddSwitchField(
                value = state.value.telemetry ?: false,
                onValueChange = { state.value = state.value.copy(telemetry = it) },
                label = "telemetry"
            )
        },
        PropertyDefinitionFormProps.nullable to {
            AddSwitchField(
                value = state.value.nullable ?: false,
                onValueChange = { state.value = state.value.copy(nullable = it) },
                label = "nullable"
            )
        },
        PropertyDefinitionFormProps.length to {
            AddTextField(
                value = state.value.length?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(length = null)
                        parsed != null -> state.value = state.value.copy(length = parsed)
                    }
                },
                label = "length",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.attributesJson to {
            AddTextField(
                value = state.value.attributesJson?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(attributesJson = value.ifEmpty { null })
                },
                label = "attributesJson",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    if (parsed != null) {
                        state.value = state.value.copy(sortIndex = parsed)
                    }
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.deviceDefinition to {
            AddTextField(
                value = state.value.deviceDefinition?.toString() ?: "",
                onValueChange = {},
                label = "deviceDefinition",
                isRequired = false,
                disable = true
            )
        },
        PropertyDefinitionFormProps.node to {
            AddTextField(
                value = state.value.node?.toString() ?: "",
                onValueChange = {},
                label = "node",
                isRequired = true,
                disable = true
            )
        },
        PropertyDefinitionFormProps.dataType to {
            AddTextField(
                value = state.value.dataType?.toString() ?: "",
                onValueChange = {},
                label = "dataType",
                isRequired = true,
                disable = true
            )
        }
    )

    val finalItems = remember(renderMap, dsl.hiddenFields, dsl.fieldOrder) {
        val orderedFieldNames = if (dsl.fieldOrder.isNotEmpty()) dsl.fieldOrder else defaultRenderMap.keys.toList()
        orderedFieldNames
            .filterNot { it in dsl.hiddenFields }
            .mapNotNull { fieldName -> renderMap[fieldName] ?: defaultRenderMap[fieldName] }
    }

    AddMultiColumnContainer(
        howMuchColumn = 2,
        items = finalItems,
    )
}

class PropertyDefinitionFormDsl(
    val state: MutableState<PropertyDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("createdAt")
                renderMap.remove("createdAt")
            }
            render != null -> {
                hiddenFields.remove("createdAt")
                renderMap["createdAt"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("createdAt")
                renderMap.remove("createdAt")
            }
        }
        order?.let { updateFieldOrder("createdAt", it) }
    }

    fun updatedAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("updatedAt")
                renderMap.remove("updatedAt")
            }
            render != null -> {
                hiddenFields.remove("updatedAt")
                renderMap["updatedAt"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("updatedAt")
                renderMap.remove("updatedAt")
            }
        }
        order?.let { updateFieldOrder("updatedAt", it) }
    }

    fun identifier(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("identifier")
                renderMap.remove("identifier")
            }
            render != null -> {
                hiddenFields.remove("identifier")
                renderMap["identifier"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("identifier")
                renderMap.remove("identifier")
            }
        }
        order?.let { updateFieldOrder("identifier", it) }
    }

    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("name")
                renderMap.remove("name")
            }
            render != null -> {
                hiddenFields.remove("name")
                renderMap["name"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("name")
                renderMap.remove("name")
            }
        }
        order?.let { updateFieldOrder("name", it) }
    }

    fun description(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("description")
                renderMap.remove("description")
            }
            render != null -> {
                hiddenFields.remove("description")
                renderMap["description"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("description")
                renderMap.remove("description")
            }
        }
        order?.let { updateFieldOrder("description", it) }
    }

    fun unit(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("unit")
                renderMap.remove("unit")
            }
            render != null -> {
                hiddenFields.remove("unit")
                renderMap["unit"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("unit")
                renderMap.remove("unit")
            }
        }
        order?.let { updateFieldOrder("unit", it) }
    }

    fun required(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("required")
                renderMap.remove("required")
            }
            render != null -> {
                hiddenFields.remove("required")
                renderMap["required"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("required")
                renderMap.remove("required")
            }
        }
        order?.let { updateFieldOrder("required", it) }
    }

    fun writable(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("writable")
                renderMap.remove("writable")
            }
            render != null -> {
                hiddenFields.remove("writable")
                renderMap["writable"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("writable")
                renderMap.remove("writable")
            }
        }
        order?.let { updateFieldOrder("writable", it) }
    }

    fun telemetry(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("telemetry")
                renderMap.remove("telemetry")
            }
            render != null -> {
                hiddenFields.remove("telemetry")
                renderMap["telemetry"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("telemetry")
                renderMap.remove("telemetry")
            }
        }
        order?.let { updateFieldOrder("telemetry", it) }
    }

    fun nullable(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("nullable")
                renderMap.remove("nullable")
            }
            render != null -> {
                hiddenFields.remove("nullable")
                renderMap["nullable"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("nullable")
                renderMap.remove("nullable")
            }
        }
        order?.let { updateFieldOrder("nullable", it) }
    }

    fun length(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("length")
                renderMap.remove("length")
            }
            render != null -> {
                hiddenFields.remove("length")
                renderMap["length"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("length")
                renderMap.remove("length")
            }
        }
        order?.let { updateFieldOrder("length", it) }
    }

    fun attributesJson(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("attributesJson")
                renderMap.remove("attributesJson")
            }
            render != null -> {
                hiddenFields.remove("attributesJson")
                renderMap["attributesJson"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("attributesJson")
                renderMap.remove("attributesJson")
            }
        }
        order?.let { updateFieldOrder("attributesJson", it) }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("sortIndex")
                renderMap.remove("sortIndex")
            }
            render != null -> {
                hiddenFields.remove("sortIndex")
                renderMap["sortIndex"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("sortIndex")
                renderMap.remove("sortIndex")
            }
        }
        order?.let { updateFieldOrder("sortIndex", it) }
    }

    fun deviceDefinition(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("deviceDefinition")
                renderMap.remove("deviceDefinition")
            }
            render != null -> {
                hiddenFields.remove("deviceDefinition")
                renderMap["deviceDefinition"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("deviceDefinition")
                renderMap.remove("deviceDefinition")
            }
        }
        order?.let { updateFieldOrder("deviceDefinition", it) }
    }

    fun node(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("node")
                renderMap.remove("node")
            }
            render != null -> {
                hiddenFields.remove("node")
                renderMap["node"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("node")
                renderMap.remove("node")
            }
        }
        order?.let { updateFieldOrder("node", it) }
    }

    fun dataType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<PropertyDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("dataType")
                renderMap.remove("dataType")
            }
            render != null -> {
                hiddenFields.remove("dataType")
                renderMap["dataType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("dataType")
                renderMap.remove("dataType")
            }
        }
        order?.let { updateFieldOrder("dataType", it) }
    }

    fun hide(vararg fields: String) {
        hiddenFields.addAll(fields)
    }

    fun order(vararg fields: String) {
        fieldOrder.clear()
        fieldOrder.addAll(fields)
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = PropertyDefinitionFormProps.getAllFields()
        val sortedFields = allFields.sortedWith { field1, field2 ->
            val order1 = fieldOrderMap[field1] ?: Int.MAX_VALUE
            val order2 = fieldOrderMap[field2] ?: Int.MAX_VALUE
            when {
                order1 != Int.MAX_VALUE && order2 != Int.MAX_VALUE -> order1.compareTo(order2)
                order1 != Int.MAX_VALUE -> -1
                order2 != Int.MAX_VALUE -> 1
                else -> allFields.indexOf(field1).compareTo(allFields.indexOf(field2))
            }
        }
        fieldOrder.clear()
        fieldOrder.addAll(sortedFields)
    }
}

@Composable
fun rememberPropertyDefinitionFormState(current: PropertyDefinitionIso? = null): MutableState<PropertyDefinitionIso> {
    return remember(current) { mutableStateOf(current ?: PropertyDefinitionIso()) }
}