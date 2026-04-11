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
 * ProtocolTemplate 表单属性常量
 */
object ProtocolTemplateFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val code = "code"
    const val name = "name"
    const val description = "description"
    const val metadataJson = "metadataJson"
    const val sortIndex = "sortIndex"
    const val moduleTemplates = "moduleTemplates"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "code", "name", "description", "metadataJson", "sortIndex", "moduleTemplates")
}

@Composable
fun ProtocolTemplateForm(
    state: MutableState<ProtocolTemplateIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProtocolTemplateFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProtocolTemplateFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProtocolTemplateFormOriginal(
    state: MutableState<ProtocolTemplateIso>,
    dslConfig: ProtocolTemplateFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProtocolTemplateFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProtocolTemplateFormProps.createdAt to {
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
        ProtocolTemplateFormProps.updatedAt to {
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
        ProtocolTemplateFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(code = value)
                },
                label = "code",
                isRequired = true
            )
        },
        ProtocolTemplateFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        ProtocolTemplateFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        ProtocolTemplateFormProps.metadataJson to {
            AddTextField(
                value = state.value.metadataJson?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(metadataJson = value.ifEmpty { null })
                },
                label = "metadataJson",
                isRequired = false
            )
        },
        ProtocolTemplateFormProps.sortIndex to {
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
        ProtocolTemplateFormProps.moduleTemplates to {
            AddTextField(
                value = state.value.moduleTemplates?.toString() ?: "",
                onValueChange = {},
                label = "moduleTemplates",
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

class ProtocolTemplateFormDsl(
    val state: MutableState<ProtocolTemplateIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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

    fun metadataJson(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("metadataJson")
                renderMap.remove("metadataJson")
            }
            render != null -> {
                hiddenFields.remove("metadataJson")
                renderMap["metadataJson"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("metadataJson")
                renderMap.remove("metadataJson")
            }
        }
        order?.let { updateFieldOrder("metadataJson", it) }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
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

    fun moduleTemplates(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolTemplateIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("moduleTemplates")
                renderMap.remove("moduleTemplates")
            }
            render != null -> {
                hiddenFields.remove("moduleTemplates")
                renderMap["moduleTemplates"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("moduleTemplates")
                renderMap.remove("moduleTemplates")
            }
        }
        order?.let { updateFieldOrder("moduleTemplates", it) }
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
        val allFields = ProtocolTemplateFormProps.getAllFields()
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
fun rememberProtocolTemplateFormState(current: ProtocolTemplateIso? = null): MutableState<ProtocolTemplateIso> {
    return remember(current) { mutableStateOf(current ?: ProtocolTemplateIso()) }
}