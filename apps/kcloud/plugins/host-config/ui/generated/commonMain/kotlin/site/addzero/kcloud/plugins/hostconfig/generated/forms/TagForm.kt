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

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "name", "description", "registerAddress", "enabled", "defaultValue", "exceptionValue", "pointType", "debounceMs", "sortIndex", "scalingEnabled", "scalingOffset", "rawMin", "rawMax", "engMin", "engMax", "forwardEnabled", "forwardRegisterAddress", "device", "dataType", "registerType", "forwardRegisterType", "valueTexts")
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
        TagFormProps.updatedAt to {
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
        TagFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        TagFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        TagFormProps.registerAddress to {
            AddTextField(
                value = state.value.registerAddress?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    if (parsed != null) {
                        state.value = state.value.copy(registerAddress = parsed)
                    }
                },
                label = "registerAddress",
                isRequired = true
            )
        },
        TagFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "enabled"
            )
        },
        TagFormProps.defaultValue to {
            AddTextField(
                value = state.value.defaultValue?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(defaultValue = value.ifEmpty { null })
                },
                label = "defaultValue",
                isRequired = false
            )
        },
        TagFormProps.exceptionValue to {
            AddTextField(
                value = state.value.exceptionValue?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(exceptionValue = value.ifEmpty { null })
                },
                label = "exceptionValue",
                isRequired = false
            )
        },
        TagFormProps.pointType to {
            AddTextField(
                value = state.value.pointType?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = PointType.entries.firstOrNull { entry -> entry.name == value }
                    when {
                        value.isEmpty() -> state.value = state.value.copy(pointType = null)
                        parsed != null -> state.value = state.value.copy(pointType = parsed)
                    }
                },
                label = "pointType",
                isRequired = false
            )
        },
        TagFormProps.debounceMs to {
            AddTextField(
                value = state.value.debounceMs?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(debounceMs = null)
                        parsed != null -> state.value = state.value.copy(debounceMs = parsed)
                    }
                },
                label = "debounceMs",
                isRequired = false
            )
        },
        TagFormProps.sortIndex to {
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
        TagFormProps.scalingEnabled to {
            AddSwitchField(
                value = state.value.scalingEnabled ?: false,
                onValueChange = { state.value = state.value.copy(scalingEnabled = it) },
                label = "scalingEnabled"
            )
        },
        TagFormProps.scalingOffset to {
            AddTextField(
                value = state.value.scalingOffset?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(scalingOffset = value.ifEmpty { null })
                },
                label = "scalingOffset",
                isRequired = false
            )
        },
        TagFormProps.rawMin to {
            AddTextField(
                value = state.value.rawMin?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(rawMin = value.ifEmpty { null })
                },
                label = "rawMin",
                isRequired = false
            )
        },
        TagFormProps.rawMax to {
            AddTextField(
                value = state.value.rawMax?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(rawMax = value.ifEmpty { null })
                },
                label = "rawMax",
                isRequired = false
            )
        },
        TagFormProps.engMin to {
            AddTextField(
                value = state.value.engMin?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(engMin = value.ifEmpty { null })
                },
                label = "engMin",
                isRequired = false
            )
        },
        TagFormProps.engMax to {
            AddTextField(
                value = state.value.engMax?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(engMax = value.ifEmpty { null })
                },
                label = "engMax",
                isRequired = false
            )
        },
        TagFormProps.forwardEnabled to {
            AddSwitchField(
                value = state.value.forwardEnabled ?: false,
                onValueChange = { state.value = state.value.copy(forwardEnabled = it) },
                label = "forwardEnabled"
            )
        },
        TagFormProps.forwardRegisterAddress to {
            AddTextField(
                value = state.value.forwardRegisterAddress?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(forwardRegisterAddress = null)
                        parsed != null -> state.value = state.value.copy(forwardRegisterAddress = parsed)
                    }
                },
                label = "forwardRegisterAddress",
                isRequired = false
            )
        },
        TagFormProps.device to {
            AddTextField(
                value = state.value.device?.toString() ?: "",
                onValueChange = {},
                label = "device",
                isRequired = true,
                disable = true
            )
        },
        TagFormProps.dataType to {
            AddTextField(
                value = state.value.dataType?.toString() ?: "",
                onValueChange = {},
                label = "dataType",
                isRequired = true,
                disable = true
            )
        },
        TagFormProps.registerType to {
            AddTextField(
                value = state.value.registerType?.toString() ?: "",
                onValueChange = {},
                label = "registerType",
                isRequired = true,
                disable = true
            )
        },
        TagFormProps.forwardRegisterType to {
            AddTextField(
                value = state.value.forwardRegisterType?.toString() ?: "",
                onValueChange = {},
                label = "forwardRegisterType",
                isRequired = false,
                disable = true
            )
        },
        TagFormProps.valueTexts to {
            AddTextField(
                value = state.value.valueTexts?.toString() ?: "",
                onValueChange = {},
                label = "valueTexts",
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

class TagFormDsl(
    val state: MutableState<TagIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
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
        order?.let { updateFieldOrder("createdAt", it) }
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
        order?.let { updateFieldOrder("updatedAt", it) }
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
        order?.let { updateFieldOrder("name", it) }
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
        order?.let { updateFieldOrder("description", it) }
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
        order?.let { updateFieldOrder("registerAddress", it) }
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
        order?.let { updateFieldOrder("enabled", it) }
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
        order?.let { updateFieldOrder("defaultValue", it) }
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
        order?.let { updateFieldOrder("exceptionValue", it) }
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
        order?.let { updateFieldOrder("pointType", it) }
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
        order?.let { updateFieldOrder("debounceMs", it) }
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
        order?.let { updateFieldOrder("sortIndex", it) }
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
        order?.let { updateFieldOrder("scalingEnabled", it) }
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
        order?.let { updateFieldOrder("scalingOffset", it) }
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
        order?.let { updateFieldOrder("rawMin", it) }
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
        order?.let { updateFieldOrder("rawMax", it) }
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
        order?.let { updateFieldOrder("engMin", it) }
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
        order?.let { updateFieldOrder("engMax", it) }
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
        order?.let { updateFieldOrder("forwardEnabled", it) }
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
        order?.let { updateFieldOrder("forwardRegisterAddress", it) }
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
        order?.let { updateFieldOrder("device", it) }
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
        order?.let { updateFieldOrder("dataType", it) }
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
        order?.let { updateFieldOrder("registerType", it) }
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
        order?.let { updateFieldOrder("forwardRegisterType", it) }
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
        order?.let { updateFieldOrder("valueTexts", it) }
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