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

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "identifier", "name", "description", "unit", "required", "writable", "telemetry", "nullable", "length", "attributesJson", "sortIndex", "deviceDefinition", "node", "dataType")
    }
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
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.identifier to {
            AddTextField(
                value = state.value.identifier?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(identifier = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "identifier",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "description",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.unit to {
            AddTextField(
                value = state.value.unit?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(unit = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "unit",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.required to {
            AddTextField(
                value = state.value.required?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(required = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "required",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.writable to {
            AddTextField(
                value = state.value.writable?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(writable = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "writable",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.telemetry to {
            AddTextField(
                value = state.value.telemetry?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(telemetry = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "telemetry",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.nullable to {
            AddTextField(
                value = state.value.nullable?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(nullable = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "nullable",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.length to {
            AddTextField(
                value = state.value.length?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(length = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "length",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.attributesJson to {
            AddTextField(
                value = state.value.attributesJson?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(attributesJson = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "attributesJson",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.deviceDefinition to {
            AddTextField(
                value = state.value.deviceDefinition?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(deviceDefinition = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "deviceDefinition",
                isRequired = false
            )
        },
        PropertyDefinitionFormProps.node to {
            AddTextField(
                value = state.value.node?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(node = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "node",
                isRequired = true
            )
        },
        PropertyDefinitionFormProps.dataType to {
            AddTextField(
                value = state.value.dataType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(dataType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "dataType",
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

class PropertyDefinitionFormDsl(
    val state: MutableState<PropertyDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
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

        order?.let { orderValue ->
            updateFieldOrder("createdAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("updatedAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("identifier", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("name", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("description", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("unit", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("required", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("writable", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("telemetry", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("nullable", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("length", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("attributesJson", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("sortIndex", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("deviceDefinition", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("node", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("dataType", orderValue)
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
            fieldOrder.addAll(PropertyDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(PropertyDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
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