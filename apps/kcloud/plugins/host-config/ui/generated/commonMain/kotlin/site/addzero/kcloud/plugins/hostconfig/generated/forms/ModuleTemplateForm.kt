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
 * ModuleTemplate 表单属性常量
 */
object ModuleTemplateFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val code = "code"
    const val name = "name"
    const val description = "description"
    const val sortIndex = "sortIndex"
    const val channelCount = "channelCount"
    const val protocolTemplate = "protocolTemplate"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "code", "name", "description", "sortIndex", "channelCount", "protocolTemplate")
}

@Composable
fun ModuleTemplateForm(
    state: MutableState<ModuleTemplateIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ModuleTemplateFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ModuleTemplateFormOriginal(state, dslConfig)
    }
}

@Composable
fun ModuleTemplateFormOriginal(
    state: MutableState<ModuleTemplateIso>,
    dslConfig: ModuleTemplateFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ModuleTemplateFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ModuleTemplateFormProps.createdAt to {
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
        ModuleTemplateFormProps.updatedAt to {
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
        ModuleTemplateFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(code = value)
                },
                label = "code",
                isRequired = true
            )
        },
        ModuleTemplateFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        ModuleTemplateFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        ModuleTemplateFormProps.sortIndex to {
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
        ModuleTemplateFormProps.channelCount to {
            AddTextField(
                value = state.value.channelCount?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(channelCount = null)
                        parsed != null -> state.value = state.value.copy(channelCount = parsed)
                    }
                },
                label = "channelCount",
                isRequired = false
            )
        },
        ModuleTemplateFormProps.protocolTemplate to {
            AddTextField(
                value = state.value.protocolTemplate?.toString() ?: "",
                onValueChange = {},
                label = "protocolTemplate",
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

class ModuleTemplateFormDsl(
    val state: MutableState<ModuleTemplateIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
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

    fun code(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("code")
                renderMap.remove("code")
            }
            render != null -> {
                hiddenFields.remove("code")
                renderMap["code"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("code")
                renderMap.remove("code")
            }
        }
        order?.let { updateFieldOrder("code", it) }
    }

    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
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

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
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

    fun channelCount(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("channelCount")
                renderMap.remove("channelCount")
            }
            render != null -> {
                hiddenFields.remove("channelCount")
                renderMap["channelCount"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("channelCount")
                renderMap.remove("channelCount")
            }
        }
        order?.let { updateFieldOrder("channelCount", it) }
    }

    fun protocolTemplate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleTemplateIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("protocolTemplate")
                renderMap.remove("protocolTemplate")
            }
            render != null -> {
                hiddenFields.remove("protocolTemplate")
                renderMap["protocolTemplate"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("protocolTemplate")
                renderMap.remove("protocolTemplate")
            }
        }
        order?.let { updateFieldOrder("protocolTemplate", it) }
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
        val allFields = ModuleTemplateFormProps.getAllFields()
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
fun rememberModuleTemplateFormState(current: ModuleTemplateIso? = null): MutableState<ModuleTemplateIso> {
    return remember(current) { mutableStateOf(current ?: ModuleTemplateIso()) }
}