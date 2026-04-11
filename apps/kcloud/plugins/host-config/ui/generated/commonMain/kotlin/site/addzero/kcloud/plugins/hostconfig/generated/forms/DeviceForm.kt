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
 * Device 表单属性常量
 */
object DeviceFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val name = "name"
    const val stationNo = "stationNo"
    const val requestIntervalMs = "requestIntervalMs"
    const val writeIntervalMs = "writeIntervalMs"
    const val byteOrder2 = "byteOrder2"
    const val byteOrder4 = "byteOrder4"
    const val floatOrder = "floatOrder"
    const val batchAnalogStart = "batchAnalogStart"
    const val batchAnalogLength = "batchAnalogLength"
    const val batchDigitalStart = "batchDigitalStart"
    const val batchDigitalLength = "batchDigitalLength"
    const val disabled = "disabled"
    const val sortIndex = "sortIndex"
    const val deviceType = "deviceType"
    const val protocol = "protocol"
    const val modules = "modules"
    const val tags = "tags"

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "name", "stationNo", "requestIntervalMs", "writeIntervalMs", "byteOrder2", "byteOrder4", "floatOrder", "batchAnalogStart", "batchAnalogLength", "batchDigitalStart", "batchDigitalLength", "disabled", "sortIndex", "deviceType", "protocol", "modules", "tags")
    }
}

@Composable
fun DeviceForm(
    state: MutableState<DeviceIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: DeviceFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        DeviceFormOriginal(state, dslConfig)
    }
}

@Composable
fun DeviceFormOriginal(
    state: MutableState<DeviceIso>,
    dslConfig: DeviceFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = DeviceFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        DeviceFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        DeviceFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        DeviceFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        DeviceFormProps.stationNo to {
            AddTextField(
                value = state.value.stationNo?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(stationNo = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "stationNo",
                isRequired = true
            )
        },
        DeviceFormProps.requestIntervalMs to {
            AddTextField(
                value = state.value.requestIntervalMs?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(requestIntervalMs = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "requestIntervalMs",
                isRequired = false
            )
        },
        DeviceFormProps.writeIntervalMs to {
            AddTextField(
                value = state.value.writeIntervalMs?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(writeIntervalMs = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "writeIntervalMs",
                isRequired = false
            )
        },
        DeviceFormProps.byteOrder2 to {
            AddTextField(
                value = state.value.byteOrder2?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(byteOrder2 = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "byteOrder2",
                isRequired = false
            )
        },
        DeviceFormProps.byteOrder4 to {
            AddTextField(
                value = state.value.byteOrder4?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(byteOrder4 = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "byteOrder4",
                isRequired = false
            )
        },
        DeviceFormProps.floatOrder to {
            AddTextField(
                value = state.value.floatOrder?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(floatOrder = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "floatOrder",
                isRequired = false
            )
        },
        DeviceFormProps.batchAnalogStart to {
            AddTextField(
                value = state.value.batchAnalogStart?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(batchAnalogStart = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "batchAnalogStart",
                isRequired = false
            )
        },
        DeviceFormProps.batchAnalogLength to {
            AddTextField(
                value = state.value.batchAnalogLength?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(batchAnalogLength = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "batchAnalogLength",
                isRequired = false
            )
        },
        DeviceFormProps.batchDigitalStart to {
            AddTextField(
                value = state.value.batchDigitalStart?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(batchDigitalStart = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "batchDigitalStart",
                isRequired = false
            )
        },
        DeviceFormProps.batchDigitalLength to {
            AddTextField(
                value = state.value.batchDigitalLength?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(batchDigitalLength = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "batchDigitalLength",
                isRequired = false
            )
        },
        DeviceFormProps.disabled to {
            AddTextField(
                value = state.value.disabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(disabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "disabled",
                isRequired = true
            )
        },
        DeviceFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        DeviceFormProps.deviceType to {
            AddTextField(
                value = state.value.deviceType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(deviceType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "deviceType",
                isRequired = true
            )
        },
        DeviceFormProps.protocol to {
            AddTextField(
                value = state.value.protocol?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(protocol = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "protocol",
                isRequired = true
            )
        },
        DeviceFormProps.modules to {
            AddTextField(
                value = state.value.modules?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(modules = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "modules",
                isRequired = true
            )
        },
        DeviceFormProps.tags to {
            AddTextField(
                value = state.value.tags?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(tags = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "tags",
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

class DeviceFormDsl(
    val state: MutableState<DeviceIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
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

    fun stationNo(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("stationNo")
                renderMap.remove("stationNo")
            }
            render != null -> {
                hiddenFields.remove("stationNo")
                renderMap["stationNo"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("stationNo")
                renderMap.remove("stationNo")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("stationNo", orderValue)
        }
    }

    fun requestIntervalMs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("requestIntervalMs")
                renderMap.remove("requestIntervalMs")
            }
            render != null -> {
                hiddenFields.remove("requestIntervalMs")
                renderMap["requestIntervalMs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("requestIntervalMs")
                renderMap.remove("requestIntervalMs")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("requestIntervalMs", orderValue)
        }
    }

    fun writeIntervalMs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("writeIntervalMs")
                renderMap.remove("writeIntervalMs")
            }
            render != null -> {
                hiddenFields.remove("writeIntervalMs")
                renderMap["writeIntervalMs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("writeIntervalMs")
                renderMap.remove("writeIntervalMs")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("writeIntervalMs", orderValue)
        }
    }

    fun byteOrder2(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("byteOrder2")
                renderMap.remove("byteOrder2")
            }
            render != null -> {
                hiddenFields.remove("byteOrder2")
                renderMap["byteOrder2"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("byteOrder2")
                renderMap.remove("byteOrder2")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("byteOrder2", orderValue)
        }
    }

    fun byteOrder4(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("byteOrder4")
                renderMap.remove("byteOrder4")
            }
            render != null -> {
                hiddenFields.remove("byteOrder4")
                renderMap["byteOrder4"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("byteOrder4")
                renderMap.remove("byteOrder4")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("byteOrder4", orderValue)
        }
    }

    fun floatOrder(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("floatOrder")
                renderMap.remove("floatOrder")
            }
            render != null -> {
                hiddenFields.remove("floatOrder")
                renderMap["floatOrder"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("floatOrder")
                renderMap.remove("floatOrder")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("floatOrder", orderValue)
        }
    }

    fun batchAnalogStart(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("batchAnalogStart")
                renderMap.remove("batchAnalogStart")
            }
            render != null -> {
                hiddenFields.remove("batchAnalogStart")
                renderMap["batchAnalogStart"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("batchAnalogStart")
                renderMap.remove("batchAnalogStart")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("batchAnalogStart", orderValue)
        }
    }

    fun batchAnalogLength(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("batchAnalogLength")
                renderMap.remove("batchAnalogLength")
            }
            render != null -> {
                hiddenFields.remove("batchAnalogLength")
                renderMap["batchAnalogLength"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("batchAnalogLength")
                renderMap.remove("batchAnalogLength")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("batchAnalogLength", orderValue)
        }
    }

    fun batchDigitalStart(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("batchDigitalStart")
                renderMap.remove("batchDigitalStart")
            }
            render != null -> {
                hiddenFields.remove("batchDigitalStart")
                renderMap["batchDigitalStart"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("batchDigitalStart")
                renderMap.remove("batchDigitalStart")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("batchDigitalStart", orderValue)
        }
    }

    fun batchDigitalLength(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("batchDigitalLength")
                renderMap.remove("batchDigitalLength")
            }
            render != null -> {
                hiddenFields.remove("batchDigitalLength")
                renderMap["batchDigitalLength"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("batchDigitalLength")
                renderMap.remove("batchDigitalLength")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("batchDigitalLength", orderValue)
        }
    }

    fun disabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("disabled")
                renderMap.remove("disabled")
            }
            render != null -> {
                hiddenFields.remove("disabled")
                renderMap["disabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("disabled")
                renderMap.remove("disabled")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("disabled", orderValue)
        }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
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

    fun deviceType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
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

    fun protocol(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("protocol")
                renderMap.remove("protocol")
            }
            render != null -> {
                hiddenFields.remove("protocol")
                renderMap["protocol"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("protocol")
                renderMap.remove("protocol")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("protocol", orderValue)
        }
    }

    fun modules(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("modules")
                renderMap.remove("modules")
            }
            render != null -> {
                hiddenFields.remove("modules")
                renderMap["modules"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("modules")
                renderMap.remove("modules")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("modules", orderValue)
        }
    }

    fun tags(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<DeviceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("tags")
                renderMap.remove("tags")
            }
            render != null -> {
                hiddenFields.remove("tags")
                renderMap["tags"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("tags")
                renderMap.remove("tags")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("tags", orderValue)
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
            fieldOrder.addAll(DeviceFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(DeviceFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = DeviceFormProps.getAllFields()
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
fun rememberDeviceFormState(current: DeviceIso? = null): MutableState<DeviceIso> {
    return remember(current) { mutableStateOf(current ?: DeviceIso()) }
}