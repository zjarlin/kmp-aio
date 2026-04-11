package site.addzero.kcloud.plugins.hostconfig.generated.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.high_level.AddMultiColumnContainer
import site.addzero.component.drawer.AddDrawer
import site.addzero.component.form.*
import site.addzero.component.form.number.AddMoneyField
import site.addzero.component.form.number.AddNumberField
import site.addzero.component.form.number.AddIntegerField
import site.addzero.component.form.number.AddDecimalField
import site.addzero.component.form.number.AddPercentageField
import site.addzero.component.form.text.AddTextField
import site.addzero.component.form.text.AddPasswordField
import site.addzero.component.form.text.AddEmailField
import site.addzero.component.form.text.AddPhoneField
import site.addzero.component.form.text.AddUrlField
import site.addzero.component.form.text.AddUsernameField
import site.addzero.component.form.text.AddIdCardField
import site.addzero.component.form.text.AddBankCardField
import site.addzero.component.form.date.AddDateField
import site.addzero.component.form.date.DateType
import site.addzero.component.form.switch.AddSwitchField
import site.addzero.component.form.selector.AddGenericSingleSelector
import site.addzero.component.form.selector.AddGenericMultiSelector
import site.addzero.core.ext.parseObjectByKtx
import site.addzero.core.validation.RegexEnum
import site.addzero.kcloud.plugins.hostconfig.generated.isomorphic.*
import site.addzero.kcloud.plugins.hostconfig.generated.forms.dataprovider.Iso2DataProvider
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

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "rawValue", "displayText", "sortIndex", "tag")
    }
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
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        TagValueTextFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        TagValueTextFormProps.rawValue to {
            AddTextField(
                value = state.value.rawValue?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(rawValue = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "rawValue",
                isRequired = true
            )
        },
        TagValueTextFormProps.displayText to {
            AddTextField(
                value = state.value.displayText?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(displayText = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "displayText",
                isRequired = true
            )
        },
        TagValueTextFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        TagValueTextFormProps.tag to {
            AddTextField(
                value = state.value.tag?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(tag = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "tag",
                isRequired = true
            )
        }
    )

    val finalItems = remember(renderMap, dsl.hiddenFields, dsl.fieldOrder) {
        val orderedFieldNames = if (dsl.fieldOrder.isNotEmpty()) {
            dsl.fieldOrder
        } else {
            defaultRenderMap.keys.toList()
        }

        orderedFieldNames
            .filter { fieldName -> fieldName !in dsl.hiddenFields }
            .mapNotNull { fieldName ->
                when {
                    renderMap.containsKey(fieldName) -> renderMap[fieldName]
                    defaultRenderMap.containsKey(fieldName) -> defaultRenderMap[fieldName]
                    else -> null
                }
            }
    }

    AddMultiColumnContainer(
        howMuchColumn = 2,
        items = finalItems
    )
}

class TagValueTextFormDsl(
    val state: MutableState<TagValueTextIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
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

        order?.let { orderValue ->
            updateFieldOrder("createdAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("updatedAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("rawValue", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("displayText", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("sortIndex", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("tag", orderValue)
        }
    }

    fun hide(vararg fields: String) {
        hiddenFields.addAll(fields)
    }

    fun order(vararg fields: String) {
        fieldOrder.clear()
        fieldOrder.addAll(fields)
    }

    fun insertBefore(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(TagValueTextFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(TagValueTextFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
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