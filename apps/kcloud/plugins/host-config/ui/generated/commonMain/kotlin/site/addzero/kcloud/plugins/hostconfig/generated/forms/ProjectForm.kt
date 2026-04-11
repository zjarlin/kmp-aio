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
 * Project 表单属性常量
 */
object ProjectFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val name = "name"
    const val description = "description"
    const val remark = "remark"
    const val sortIndex = "sortIndex"
    const val protocolLinks = "protocolLinks"
    const val protocols = "protocols"
    const val mqttConfig = "mqttConfig"
    const val modbusServerConfigs = "modbusServerConfigs"

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "name", "description", "remark", "sortIndex", "protocolLinks", "protocols", "mqttConfig", "modbusServerConfigs")
    }
}

@Composable
fun ProjectForm(
    state: MutableState<ProjectIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProjectFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProjectFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProjectFormOriginal(
    state: MutableState<ProjectIso>,
    dslConfig: ProjectFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProjectFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProjectFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        ProjectFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        ProjectFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        ProjectFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "description",
                isRequired = false
            )
        },
        ProjectFormProps.remark to {
            AddTextField(
                value = state.value.remark?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(remark = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "remark",
                isRequired = false
            )
        },
        ProjectFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        ProjectFormProps.protocolLinks to {
            AddTextField(
                value = state.value.protocolLinks?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(protocolLinks = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "protocolLinks",
                isRequired = true
            )
        },
        ProjectFormProps.protocols to {
            AddTextField(
                value = state.value.protocols?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(protocols = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "protocols",
                isRequired = true
            )
        },
        ProjectFormProps.mqttConfig to {
            AddTextField(
                value = state.value.mqttConfig?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(mqttConfig = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "mqttConfig",
                isRequired = false
            )
        },
        ProjectFormProps.modbusServerConfigs to {
            AddTextField(
                value = state.value.modbusServerConfigs?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(modbusServerConfigs = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "modbusServerConfigs",
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

class ProjectFormDsl(
    val state: MutableState<ProjectIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
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

    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
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

    fun remark(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("remark")
                renderMap.remove("remark")
            }
            render != null -> {
                hiddenFields.remove("remark")
                renderMap["remark"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("remark")
                renderMap.remove("remark")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("remark", orderValue)
        }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
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

    fun protocolLinks(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("protocolLinks")
                renderMap.remove("protocolLinks")
            }
            render != null -> {
                hiddenFields.remove("protocolLinks")
                renderMap["protocolLinks"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("protocolLinks")
                renderMap.remove("protocolLinks")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("protocolLinks", orderValue)
        }
    }

    fun protocols(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("protocols")
                renderMap.remove("protocols")
            }
            render != null -> {
                hiddenFields.remove("protocols")
                renderMap["protocols"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("protocols")
                renderMap.remove("protocols")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("protocols", orderValue)
        }
    }

    fun mqttConfig(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("mqttConfig")
                renderMap.remove("mqttConfig")
            }
            render != null -> {
                hiddenFields.remove("mqttConfig")
                renderMap["mqttConfig"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("mqttConfig")
                renderMap.remove("mqttConfig")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("mqttConfig", orderValue)
        }
    }

    fun modbusServerConfigs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("modbusServerConfigs")
                renderMap.remove("modbusServerConfigs")
            }
            render != null -> {
                hiddenFields.remove("modbusServerConfigs")
                renderMap["modbusServerConfigs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("modbusServerConfigs")
                renderMap.remove("modbusServerConfigs")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("modbusServerConfigs", orderValue)
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
            fieldOrder.addAll(ProjectFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(ProjectFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = ProjectFormProps.getAllFields()
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
fun rememberProjectFormState(current: ProjectIso? = null): MutableState<ProjectIso> {
    return remember(current) { mutableStateOf(current ?: ProjectIso()) }
}