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
 * TagValueText 表单属性常量
 */
object TagValueTextFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val rawValue = "rawValue"
    const val displayText = "displayText"
    const val sortIndex = "sortIndex"
    const val tag = "tag"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "rawValue", "displayText", "sortIndex", "tag")
}

@Composable
fun TagValueTextForm(
    state: MutableState<TagValueTextIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: TagValueTextFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        TagValueTextFormOriginal(state, dslConfig)
    }
}

@Composable
fun TagValueTextFormOriginal(
    state: MutableState<TagValueTextIso>,
    dslConfig: TagValueTextFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = TagValueTextFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        TagValueTextFormProps.createdAt to {
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
        TagValueTextFormProps.updatedAt to {
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
        TagValueTextFormProps.rawValue to {
            AddTextField(
                value = state.value.rawValue?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(rawValue = value)
                },
                label = "rawValue",
                isRequired = true
            )
        },
        TagValueTextFormProps.displayText to {
            AddTextField(
                value = state.value.displayText?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(displayText = value)
                },
                label = "displayText",
                isRequired = true
            )
        },
        TagValueTextFormProps.sortIndex to {
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
        TagValueTextFormProps.tag to {
            AddTextField(
                value = state.value.tag?.toString() ?: "",
                onValueChange = {},
                label = "tag",
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

class TagValueTextFormDsl(
    val state: MutableState<TagValueTextIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
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

    fun rawValue(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("rawValue")
                renderMap.remove("rawValue")
            }
            render != null -> {
                hiddenFields.remove("rawValue")
                renderMap["rawValue"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("rawValue")
                renderMap.remove("rawValue")
            }
        }
        order?.let { updateFieldOrder("rawValue", it) }
    }

    fun displayText(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("displayText")
                renderMap.remove("displayText")
            }
            render != null -> {
                hiddenFields.remove("displayText")
                renderMap["displayText"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("displayText")
                renderMap.remove("displayText")
            }
        }
        order?.let { updateFieldOrder("displayText", it) }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
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

    fun tag(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagValueTextIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("tag")
                renderMap.remove("tag")
            }
            render != null -> {
                hiddenFields.remove("tag")
                renderMap["tag"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("tag")
                renderMap.remove("tag")
            }
        }
        order?.let { updateFieldOrder("tag", it) }
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
        val allFields = TagValueTextFormProps.getAllFields()
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
fun rememberTagValueTextFormState(current: TagValueTextIso? = null): MutableState<TagValueTextIso> {
    return remember(current) { mutableStateOf(current ?: TagValueTextIso()) }
}