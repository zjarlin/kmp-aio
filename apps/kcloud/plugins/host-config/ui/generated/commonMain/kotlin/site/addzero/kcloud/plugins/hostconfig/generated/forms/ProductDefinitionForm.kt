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
 * ProductDefinition 表单属性常量
 */
object ProductDefinitionFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val code = "code"
    const val name = "name"
    const val description = "description"
    const val vendor = "vendor"
    const val category = "category"
    const val enabled = "enabled"
    const val sortIndex = "sortIndex"
    const val devices = "devices"
    const val labelLinks = "labelLinks"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "code", "name", "description", "vendor", "category", "enabled", "sortIndex", "devices", "labelLinks")
}

@Composable
fun ProductDefinitionForm(
    state: MutableState<ProductDefinitionIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProductDefinitionFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProductDefinitionFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProductDefinitionFormOriginal(
    state: MutableState<ProductDefinitionIso>,
    dslConfig: ProductDefinitionFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProductDefinitionFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProductDefinitionFormProps.createdAt to {
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
        ProductDefinitionFormProps.updatedAt to {
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
        ProductDefinitionFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(code = value)
                },
                label = "code",
                isRequired = true
            )
        },
        ProductDefinitionFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        ProductDefinitionFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        ProductDefinitionFormProps.vendor to {
            AddTextField(
                value = state.value.vendor?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(vendor = value.ifEmpty { null })
                },
                label = "vendor",
                isRequired = false
            )
        },
        ProductDefinitionFormProps.category to {
            AddTextField(
                value = state.value.category?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(category = value.ifEmpty { null })
                },
                label = "category",
                isRequired = false
            )
        },
        ProductDefinitionFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "enabled"
            )
        },
        ProductDefinitionFormProps.sortIndex to {
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
        ProductDefinitionFormProps.devices to {
            AddTextField(
                value = state.value.devices?.toString() ?: "",
                onValueChange = {},
                label = "devices",
                isRequired = true,
                disable = true
            )
        },
        ProductDefinitionFormProps.labelLinks to {
            AddTextField(
                value = state.value.labelLinks?.toString() ?: "",
                onValueChange = {},
                label = "labelLinks",
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

class ProductDefinitionFormDsl(
    val state: MutableState<ProductDefinitionIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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

    fun code(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("code", it) }
    }

    fun name(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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

    fun vendor(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("vendor")
                renderMap.remove("vendor")
            }
            render != null -> {
                hiddenFields.remove("vendor")
                renderMap["vendor"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("vendor")
                renderMap.remove("vendor")
            }
        }
        order?.let { updateFieldOrder("vendor", it) }
    }

    fun category(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("category")
                renderMap.remove("category")
            }
            render != null -> {
                hiddenFields.remove("category")
                renderMap["category"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("category")
                renderMap.remove("category")
            }
        }
        order?.let { updateFieldOrder("category", it) }
    }

    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
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

    fun devices(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("devices")
                renderMap.remove("devices")
            }
            render != null -> {
                hiddenFields.remove("devices")
                renderMap["devices"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("devices")
                renderMap.remove("devices")
            }
        }
        order?.let { updateFieldOrder("devices", it) }
    }

    fun labelLinks(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("labelLinks")
                renderMap.remove("labelLinks")
            }
            render != null -> {
                hiddenFields.remove("labelLinks")
                renderMap["labelLinks"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("labelLinks")
                renderMap.remove("labelLinks")
            }
        }
        order?.let { updateFieldOrder("labelLinks", it) }
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
        val allFields = ProductDefinitionFormProps.getAllFields()
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
fun rememberProductDefinitionFormState(current: ProductDefinitionIso? = null): MutableState<ProductDefinitionIso> {
    return remember(current) { mutableStateOf(current ?: ProductDefinitionIso()) }
}