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
 * ModuleInstance 表单属性常量
 */
object ModuleInstanceFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val name = "name"
    const val sortIndex = "sortIndex"
    const val moduleTemplate = "moduleTemplate"
    const val device = "device"
    const val protocol = "protocol"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "name", "sortIndex", "moduleTemplate", "device", "protocol")
}

@Composable
fun ModuleInstanceForm(
    state: MutableState<ModuleInstanceIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ModuleInstanceFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ModuleInstanceFormOriginal(state, dslConfig)
    }
}

@Composable
fun ModuleInstanceFormOriginal(
    state: MutableState<ModuleInstanceIso>,
    dslConfig: ModuleInstanceFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ModuleInstanceFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ModuleInstanceFormProps.createdAt to {
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
        ModuleInstanceFormProps.updatedAt to {
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
        ModuleInstanceFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        ModuleInstanceFormProps.sortIndex to {
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
        ModuleInstanceFormProps.moduleTemplate to {
            AddTextField(
                value = state.value.moduleTemplate?.toString() ?: "",
                onValueChange = {},
                label = "moduleTemplate",
                isRequired = true,
                disable = true
            )
        },
        ModuleInstanceFormProps.device to {
            AddTextField(
                value = state.value.device?.toString() ?: "",
                onValueChange = {},
                label = "device",
                isRequired = true,
                disable = true
            )
        },
        ModuleInstanceFormProps.protocol to {
            AddTextField(
                value = state.value.protocol?.toString() ?: "",
                onValueChange = {},
                label = "protocol",
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

class ModuleInstanceFormDsl(
    val state: MutableState<ModuleInstanceIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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

    fun moduleTemplate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("moduleTemplate")
                renderMap.remove("moduleTemplate")
            }
            render != null -> {
                hiddenFields.remove("moduleTemplate")
                renderMap["moduleTemplate"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("moduleTemplate")
                renderMap.remove("moduleTemplate")
            }
        }
        order?.let { updateFieldOrder("moduleTemplate", it) }
    }

    fun device(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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

    fun protocol(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ModuleInstanceIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("protocol", it) }
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
        val allFields = ModuleInstanceFormProps.getAllFields()
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
fun rememberModuleInstanceFormState(current: ModuleInstanceIso? = null): MutableState<ModuleInstanceIso> {
    return remember(current) { mutableStateOf(current ?: ModuleInstanceIso()) }
}