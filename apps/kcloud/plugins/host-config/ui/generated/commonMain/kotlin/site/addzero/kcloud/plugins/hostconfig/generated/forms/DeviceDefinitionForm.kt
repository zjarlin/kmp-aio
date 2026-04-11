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
 * DeviceDefinition 表单属性常量
 */
object DeviceDefinitionFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val code = "code"
    const val name = "name"
    const val description = "description"
    const val supportsTelemetry = "supportsTelemetry"
    const val supportsControl = "supportsControl"
    const val sortIndex = "sortIndex"
    const val product = "product"
    const val deviceType = "deviceType"
    const val properties = "properties"
    const val features = "features"

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "code", "name", "description", "supportsTelemetry", "supportsControl", "sortIndex", "product", "deviceType", "properties", "features")
    }
}

@Composable
fun DeviceDefinitionForm(
    state: MutableState<DeviceDefinitionIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: DeviceDefinitionFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        DeviceDefinitionFormOriginal(state, dslConfig)
    }
}

@Composable
fun DeviceDefinitionFormOriginal(
    state: MutableState<DeviceDefinitionIso>,
    dslConfig: DeviceDefinitionFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = DeviceDefinitionFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        DeviceDefinitionFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(code = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "code",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "description",
                isRequired = false
            )
        },
        DeviceDefinitionFormProps.supportsTelemetry to {
            AddTextField(
                value = state.value.supportsTelemetry?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(supportsTelemetry = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "supportsTelemetry",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.supportsControl to {
            AddTextField(
                value = state.value.supportsControl?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(supportsControl = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "supportsControl",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.product to {
            AddTextField(
                value = state.value.product?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(product = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "product",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.deviceType to {
            AddTextField(
                value = state.value.deviceType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(deviceType = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "deviceType",
                isRequired = false
            )
        },
        DeviceDefinitionFormProps.properties to {
            AddTextField(
                value = state.value.properties?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(properties = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "properties",
                isRequired = true
            )
        },
        DeviceDefinitionFormProps.features to {
            AddTextField(
                value = state.value.features?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(features = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "features",
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

class DeviceDefinitionFormDsl(
    val state: MutableState<DeviceDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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

    fun code(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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

        order?.let { orderValue ->
            updateFieldOrder("code", orderValue)
        }
    }

    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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

    fun supportsTelemetry(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("supportsTelemetry")
                renderMap.remove("supportsTelemetry")
            }
            render != null -> {
                hiddenFields.remove("supportsTelemetry")
                renderMap["supportsTelemetry"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("supportsTelemetry")
                renderMap.remove("supportsTelemetry")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("supportsTelemetry", orderValue)
        }
    }

    fun supportsControl(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("supportsControl")
                renderMap.remove("supportsControl")
            }
            render != null -> {
                hiddenFields.remove("supportsControl")
                renderMap["supportsControl"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("supportsControl")
                renderMap.remove("supportsControl")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("supportsControl", orderValue)
        }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
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

    fun product(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("product")
                renderMap.remove("product")
            }
            render != null -> {
                hiddenFields.remove("product")
                renderMap["product"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("product")
                renderMap.remove("product")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("product", orderValue)
        }
    }

    fun deviceType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("deviceType")
                renderMap.remove("deviceType")
            }
            render != null -> {
                hiddenFields.remove("deviceType")
                renderMap["deviceType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("deviceType")
                renderMap.remove("deviceType")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("deviceType", orderValue)
        }
    }

    fun properties(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("properties")
                renderMap.remove("properties")
            }
            render != null -> {
                hiddenFields.remove("properties")
                renderMap["properties"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("properties")
                renderMap.remove("properties")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("properties", orderValue)
        }
    }

    fun features(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("features")
                renderMap.remove("features")
            }
            render != null -> {
                hiddenFields.remove("features")
                renderMap["features"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("features")
                renderMap.remove("features")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("features", orderValue)
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
            fieldOrder.addAll(DeviceDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(DeviceDefinitionFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = DeviceDefinitionFormProps.getAllFields()
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
fun rememberDeviceDefinitionFormState(current: DeviceDefinitionIso? = null): MutableState<DeviceDefinitionIso> {
    return remember(current) { mutableStateOf(current ?: DeviceDefinitionIso()) }
}