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

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "identifier", "name", "description", "inputSchema", "outputSchema", "asynchronous", "sortIndex", "deviceDefinition", "node")
    }
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
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.identifier to {
            AddTextField(
                value = state.value.identifier?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(identifier = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "identifier",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "description",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.inputSchema to {
            AddTextField(
                value = state.value.inputSchema?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(inputSchema = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "inputSchema",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.outputSchema to {
            AddTextField(
                value = state.value.outputSchema?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(outputSchema = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "outputSchema",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.asynchronous to {
            AddTextField(
                value = state.value.asynchronous?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(asynchronous = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "asynchronous",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        FeatureDefinitionFormProps.deviceDefinition to {
            AddTextField(
                value = state.value.deviceDefinition?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(deviceDefinition = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "deviceDefinition",
                isRequired = false
            )
        },
        FeatureDefinitionFormProps.node to {
            AddTextField(
                value = state.value.node?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(node = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "node",
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

class FeatureDefinitionFormDsl(
    val state: MutableState<FeatureDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
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

        order?.let { orderValue ->
            updateFieldOrder("createdAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("updatedAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("identifier", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("name", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("description", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("inputSchema", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("outputSchema", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("asynchronous", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("sortIndex", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("deviceDefinition", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("node", orderValue)
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
            fieldOrder.addAll(FeatureDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(FeatureDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
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