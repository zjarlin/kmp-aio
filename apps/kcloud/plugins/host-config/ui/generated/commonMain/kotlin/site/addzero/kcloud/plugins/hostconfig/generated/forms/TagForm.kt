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
 * Tag 表单属性常量
 */
object TagFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val name = "name"
    const val description = "description"
    const val registerAddress = "registerAddress"
    const val enabled = "enabled"
    const val defaultValue = "defaultValue"
    const val exceptionValue = "exceptionValue"
    const val pointType = "pointType"
    const val debounceMs = "debounceMs"
    const val sortIndex = "sortIndex"
    const val scalingEnabled = "scalingEnabled"
    const val scalingOffset = "scalingOffset"
    const val rawMin = "rawMin"
    const val rawMax = "rawMax"
    const val engMin = "engMin"
    const val engMax = "engMax"
    const val forwardEnabled = "forwardEnabled"
    const val forwardRegisterAddress = "forwardRegisterAddress"
    const val device = "device"
    const val dataType = "dataType"
    const val registerType = "registerType"
    const val forwardRegisterType = "forwardRegisterType"
    const val valueTexts = "valueTexts"

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "name", "description", "registerAddress", "enabled", "defaultValue", "exceptionValue", "pointType", "debounceMs", "sortIndex", "scalingEnabled", "scalingOffset", "rawMin", "rawMax", "engMin", "engMax", "forwardEnabled", "forwardRegisterAddress", "device", "dataType", "registerType", "forwardRegisterType", "valueTexts")
    }
}

@Composable
fun TagForm(
    state: MutableState<TagIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: TagFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        TagFormOriginal(state, dslConfig)
    }
}

@Composable
fun TagFormOriginal(
    state: MutableState<TagIso>,
    dslConfig: TagFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = TagFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        TagFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        TagFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        TagFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(name = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "name",
                isRequired = true
            )
        },
        TagFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(description = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "description",
                isRequired = false
            )
        },
        TagFormProps.registerAddress to {
            AddTextField(
                value = state.value.registerAddress?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(registerAddress = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "registerAddress",
                isRequired = true
            )
        },
        TagFormProps.enabled to {
            AddTextField(
                value = state.value.enabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(enabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "enabled",
                isRequired = true
            )
        },
        TagFormProps.defaultValue to {
            AddTextField(
                value = state.value.defaultValue?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(defaultValue = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "defaultValue",
                isRequired = false
            )
        },
        TagFormProps.exceptionValue to {
            AddTextField(
                value = state.value.exceptionValue?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(exceptionValue = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "exceptionValue",
                isRequired = false
            )
        },
        TagFormProps.pointType to {
            AddTextField(
                value = state.value.pointType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(pointType = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "pointType",
                isRequired = false
            )
        },
        TagFormProps.debounceMs to {
            AddTextField(
                value = state.value.debounceMs?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(debounceMs = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "debounceMs",
                isRequired = false
            )
        },
        TagFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        TagFormProps.scalingEnabled to {
            AddTextField(
                value = state.value.scalingEnabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(scalingEnabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "scalingEnabled",
                isRequired = true
            )
        },
        TagFormProps.scalingOffset to {
            AddTextField(
                value = state.value.scalingOffset?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(scalingOffset = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "scalingOffset",
                isRequired = false
            )
        },
        TagFormProps.rawMin to {
            AddTextField(
                value = state.value.rawMin?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(rawMin = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "rawMin",
                isRequired = false
            )
        },
        TagFormProps.rawMax to {
            AddTextField(
                value = state.value.rawMax?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(rawMax = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "rawMax",
                isRequired = false
            )
        },
        TagFormProps.engMin to {
            AddTextField(
                value = state.value.engMin?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(engMin = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "engMin",
                isRequired = false
            )
        },
        TagFormProps.engMax to {
            AddTextField(
                value = state.value.engMax?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(engMax = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "engMax",
                isRequired = false
            )
        },
        TagFormProps.forwardEnabled to {
            AddTextField(
                value = state.value.forwardEnabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(forwardEnabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "forwardEnabled",
                isRequired = true
            )
        },
        TagFormProps.forwardRegisterAddress to {
            AddTextField(
                value = state.value.forwardRegisterAddress?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(forwardRegisterAddress = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "forwardRegisterAddress",
                isRequired = false
            )
        },
        TagFormProps.device to {
            AddTextField(
                value = state.value.device?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(device = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "device",
                isRequired = true
            )
        },
        TagFormProps.dataType to {
            AddTextField(
                value = state.value.dataType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(dataType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "dataType",
                isRequired = true
            )
        },
        TagFormProps.registerType to {
            AddTextField(
                value = state.value.registerType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(registerType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "registerType",
                isRequired = true
            )
        },
        TagFormProps.forwardRegisterType to {
            AddTextField(
                value = state.value.forwardRegisterType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(forwardRegisterType = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "forwardRegisterType",
                isRequired = false
            )
        },
        TagFormProps.valueTexts to {
            AddTextField(
                value = state.value.valueTexts?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(valueTexts = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "valueTexts",
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

class TagFormDsl(
    val state: MutableState<TagIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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

    fun registerAddress(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("registerAddress")
                renderMap.remove("registerAddress")
            }
            render != null -> {
                hiddenFields.remove("registerAddress")
                renderMap["registerAddress"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("registerAddress")
                renderMap.remove("registerAddress")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("registerAddress", orderValue)
        }
    }

    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("enabled")
                renderMap.remove("enabled")
            }
            render != null -> {
                hiddenFields.remove("enabled")
                renderMap["enabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("enabled")
                renderMap.remove("enabled")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("enabled", orderValue)
        }
    }

    fun defaultValue(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("defaultValue")
                renderMap.remove("defaultValue")
            }
            render != null -> {
                hiddenFields.remove("defaultValue")
                renderMap["defaultValue"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("defaultValue")
                renderMap.remove("defaultValue")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("defaultValue", orderValue)
        }
    }

    fun exceptionValue(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("exceptionValue")
                renderMap.remove("exceptionValue")
            }
            render != null -> {
                hiddenFields.remove("exceptionValue")
                renderMap["exceptionValue"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("exceptionValue")
                renderMap.remove("exceptionValue")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("exceptionValue", orderValue)
        }
    }

    fun pointType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("pointType")
                renderMap.remove("pointType")
            }
            render != null -> {
                hiddenFields.remove("pointType")
                renderMap["pointType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("pointType")
                renderMap.remove("pointType")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("pointType", orderValue)
        }
    }

    fun debounceMs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("debounceMs")
                renderMap.remove("debounceMs")
            }
            render != null -> {
                hiddenFields.remove("debounceMs")
                renderMap["debounceMs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("debounceMs")
                renderMap.remove("debounceMs")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("debounceMs", orderValue)
        }
    }

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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

    fun scalingEnabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("scalingEnabled")
                renderMap.remove("scalingEnabled")
            }
            render != null -> {
                hiddenFields.remove("scalingEnabled")
                renderMap["scalingEnabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("scalingEnabled")
                renderMap.remove("scalingEnabled")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("scalingEnabled", orderValue)
        }
    }

    fun scalingOffset(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("scalingOffset")
                renderMap.remove("scalingOffset")
            }
            render != null -> {
                hiddenFields.remove("scalingOffset")
                renderMap["scalingOffset"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("scalingOffset")
                renderMap.remove("scalingOffset")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("scalingOffset", orderValue)
        }
    }

    fun rawMin(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("rawMin")
                renderMap.remove("rawMin")
            }
            render != null -> {
                hiddenFields.remove("rawMin")
                renderMap["rawMin"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("rawMin")
                renderMap.remove("rawMin")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("rawMin", orderValue)
        }
    }

    fun rawMax(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("rawMax")
                renderMap.remove("rawMax")
            }
            render != null -> {
                hiddenFields.remove("rawMax")
                renderMap["rawMax"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("rawMax")
                renderMap.remove("rawMax")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("rawMax", orderValue)
        }
    }

    fun engMin(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("engMin")
                renderMap.remove("engMin")
            }
            render != null -> {
                hiddenFields.remove("engMin")
                renderMap["engMin"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("engMin")
                renderMap.remove("engMin")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("engMin", orderValue)
        }
    }

    fun engMax(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("engMax")
                renderMap.remove("engMax")
            }
            render != null -> {
                hiddenFields.remove("engMax")
                renderMap["engMax"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("engMax")
                renderMap.remove("engMax")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("engMax", orderValue)
        }
    }

    fun forwardEnabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("forwardEnabled")
                renderMap.remove("forwardEnabled")
            }
            render != null -> {
                hiddenFields.remove("forwardEnabled")
                renderMap["forwardEnabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("forwardEnabled")
                renderMap.remove("forwardEnabled")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("forwardEnabled", orderValue)
        }
    }

    fun forwardRegisterAddress(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("forwardRegisterAddress")
                renderMap.remove("forwardRegisterAddress")
            }
            render != null -> {
                hiddenFields.remove("forwardRegisterAddress")
                renderMap["forwardRegisterAddress"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("forwardRegisterAddress")
                renderMap.remove("forwardRegisterAddress")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("forwardRegisterAddress", orderValue)
        }
    }

    fun device(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("device")
                renderMap.remove("device")
            }
            render != null -> {
                hiddenFields.remove("device")
                renderMap["device"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("device")
                renderMap.remove("device")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("device", orderValue)
        }
    }

    fun dataType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
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

    fun registerType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("registerType")
                renderMap.remove("registerType")
            }
            render != null -> {
                hiddenFields.remove("registerType")
                renderMap["registerType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("registerType")
                renderMap.remove("registerType")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("registerType", orderValue)
        }
    }

    fun forwardRegisterType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("forwardRegisterType")
                renderMap.remove("forwardRegisterType")
            }
            render != null -> {
                hiddenFields.remove("forwardRegisterType")
                renderMap["forwardRegisterType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("forwardRegisterType")
                renderMap.remove("forwardRegisterType")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("forwardRegisterType", orderValue)
        }
    }

    fun valueTexts(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<TagIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("valueTexts")
                renderMap.remove("valueTexts")
            }
            render != null -> {
                hiddenFields.remove("valueTexts")
                renderMap["valueTexts"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("valueTexts")
                renderMap.remove("valueTexts")
            }
        }

        order?.let { orderValue ->
            updateFieldOrder("valueTexts", orderValue)
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
            fieldOrder.addAll(TagFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(TagFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = TagFormProps.getAllFields()
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
fun rememberTagFormState(current: TagIso? = null): MutableState<TagIso> {
    return remember(current) { mutableStateOf(current ?: TagIso()) }
}