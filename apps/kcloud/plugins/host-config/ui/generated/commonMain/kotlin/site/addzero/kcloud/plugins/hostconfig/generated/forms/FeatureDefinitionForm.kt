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
 * FeatureDefinition 表单属性常量
 */
object FeatureDefinitionFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val identifier = "identifier"
    const val name = "name"
    const val description = "description"
    const val inputSchema = "inputSchema"
    const val outputSchema = "outputSchema"
    const val asynchronous = "asynchronous"
    const val sortIndex = "sortIndex"
    const val deviceDefinition = "deviceDefinition"
    const val node = "node"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "identifier", "name", "description", "inputSchema", "outputSchema", "asynchronous", "sortIndex", "deviceDefinition", "node")
}

@Composable
fun FeatureDefinitionForm(
    state: MutableState<FeatureDefinitionIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: FeatureDefinitionFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        FeatureDefinitionFormOriginal(state, dslConfig)
    }
}

@Composable
fun FeatureDefinitionFormOriginal(
    state: MutableState<FeatureDefinitionIso>,
    dslConfig: FeatureDefinitionFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = FeatureDefinitionFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        FeatureDefinitionFormProps.createdAt to {
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
        FeatureDefinitionFormProps.updatedAt to {
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
        FeatureDefinitionFormProps.identifier to {
            AddTextField(
                value = state.value.identifier?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(identifier = value)
                },
                label = "identifier",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.inputSchema to {
            AddTextField(
                value = state.value.inputSchema?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(inputSchema = value.ifEmpty { null })
                },
                label = "inputSchema",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.outputSchema to {
            AddTextField(
                value = state.value.outputSchema?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(outputSchema = value.ifEmpty { null })
                },
                label = "outputSchema",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.asynchronous to {
            AddSwitchField(
                value = state.value.asynchronous ?: false,
                onValueChange = { state.value = state.value.copy(asynchronous = it) },
                label = "asynchronous"
            )
        },
        FeatureDefinitionFormProps.sortIndex to {
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
        FeatureDefinitionFormProps.deviceDefinition to {
            AddTextField(
                value = state.value.deviceDefinition?.toString() ?: "",
                onValueChange = {},
                label = "deviceDefinition",
                isRequired = false,
                disable = true
            )
        },
        FeatureDefinitionFormProps.node to {
            AddTextField(
                value = state.value.node?.toString() ?: "",
                onValueChange = {},
                label = "node",
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

class FeatureDefinitionFormDsl(
    val state: MutableState<FeatureDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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

    fun inputSchema(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("inputSchema")
                renderMap.remove("inputSchema")
            }
            render != null -> {
                hiddenFields.remove("inputSchema")
                renderMap["inputSchema"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("inputSchema")
                renderMap.remove("inputSchema")
            }
        }
        order?.let { updateFieldOrder("inputSchema", it) }
    }

    fun outputSchema(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("outputSchema")
                renderMap.remove("outputSchema")
            }
            render != null -> {
                hiddenFields.remove("outputSchema")
                renderMap["outputSchema"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("outputSchema")
                renderMap.remove("outputSchema")
            }
        }
        order?.let { updateFieldOrder("outputSchema", it) }
    }

    fun asynchronous(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("asynchronous")
                renderMap.remove("asynchronous")
            }
            render != null -> {
                hiddenFields.remove("asynchronous")
                renderMap["asynchronous"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("asynchronous")
                renderMap.remove("asynchronous")
            }
        }
        order?.let { updateFieldOrder("asynchronous", it) }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<FeatureDefinitionIso>) -> Unit)? = null
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

    fun hide(vararg fields: String) {
        hiddenFields.addAll(fields)
    }

    fun order(vararg fields: String) {
        fieldOrder.clear()
        fieldOrder.addAll(fields)
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = FeatureDefinitionFormProps.getAllFields()
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
fun rememberFeatureDefinitionFormState(current: FeatureDefinitionIso? = null): MutableState<FeatureDefinitionIso> {
    return remember(current) { mutableStateOf(current ?: FeatureDefinitionIso()) }
}