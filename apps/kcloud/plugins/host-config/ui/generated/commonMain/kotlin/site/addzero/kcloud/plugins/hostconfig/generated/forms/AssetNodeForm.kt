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
 * AssetNode 表单属性常量
 */
object AssetNodeFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val nodeType = "nodeType"
    const val code = "code"
    const val name = "name"
    const val description = "description"
    const val enabled = "enabled"
    const val sortIndex = "sortIndex"
    const val vendor = "vendor"
    const val category = "category"
    const val supportsTelemetry = "supportsTelemetry"
    const val supportsControl = "supportsControl"
    const val parent = "parent"
    const val protocolTemplate = "protocolTemplate"
    const val deviceType = "deviceType"
    const val moduleTemplate = "moduleTemplate"
    const val children = "children"
    const val labelLinks = "labelLinks"
    const val properties = "properties"
    const val features = "features"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "nodeType", "code", "name", "description", "enabled", "sortIndex", "vendor", "category", "supportsTelemetry", "supportsControl", "parent", "protocolTemplate", "deviceType", "moduleTemplate", "children", "labelLinks", "properties", "features")
}

@Composable
fun AssetNodeForm(
    state: MutableState<AssetNodeIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: AssetNodeFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        AssetNodeFormOriginal(state, dslConfig)
    }
}

@Composable
fun AssetNodeFormOriginal(
    state: MutableState<AssetNodeIso>,
    dslConfig: AssetNodeFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = AssetNodeFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        AssetNodeFormProps.createdAt to {
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
        AssetNodeFormProps.updatedAt to {
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
        AssetNodeFormProps.nodeType to {
            AddTextField(
                value = state.value.nodeType?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = AssetNodeType.entries.firstOrNull { entry -> entry.name == value }
                    if (parsed != null) {
                        state.value = state.value.copy(nodeType = parsed)
                    }
                },
                label = "nodeType",
                isRequired = true
            )
        },
        AssetNodeFormProps.code to {
            AddTextField(
                value = state.value.code?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(code = value)
                },
                label = "code",
                isRequired = true
            )
        },
        AssetNodeFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        AssetNodeFormProps.description to {
            AddTextField(
                value = state.value.description?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(description = value.ifEmpty { null })
                },
                label = "description",
                isRequired = false
            )
        },
        AssetNodeFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "enabled"
            )
        },
        AssetNodeFormProps.sortIndex to {
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
        AssetNodeFormProps.vendor to {
            AddTextField(
                value = state.value.vendor?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(vendor = value.ifEmpty { null })
                },
                label = "vendor",
                isRequired = false
            )
        },
        AssetNodeFormProps.category to {
            AddTextField(
                value = state.value.category?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(category = value.ifEmpty { null })
                },
                label = "category",
                isRequired = false
            )
        },
        AssetNodeFormProps.supportsTelemetry to {
            AddSwitchField(
                value = state.value.supportsTelemetry ?: false,
                onValueChange = { state.value = state.value.copy(supportsTelemetry = it) },
                label = "supportsTelemetry"
            )
        },
        AssetNodeFormProps.supportsControl to {
            AddSwitchField(
                value = state.value.supportsControl ?: false,
                onValueChange = { state.value = state.value.copy(supportsControl = it) },
                label = "supportsControl"
            )
        },
        AssetNodeFormProps.parent to {
            AddTextField(
                value = state.value.parent?.toString() ?: "",
                onValueChange = {},
                label = "parent",
                isRequired = false,
                disable = true
            )
        },
        AssetNodeFormProps.protocolTemplate to {
            AddTextField(
                value = state.value.protocolTemplate?.toString() ?: "",
                onValueChange = {},
                label = "protocolTemplate",
                isRequired = false,
                disable = true
            )
        },
        AssetNodeFormProps.deviceType to {
            AddTextField(
                value = state.value.deviceType?.toString() ?: "",
                onValueChange = {},
                label = "deviceType",
                isRequired = false,
                disable = true
            )
        },
        AssetNodeFormProps.moduleTemplate to {
            AddTextField(
                value = state.value.moduleTemplate?.toString() ?: "",
                onValueChange = {},
                label = "moduleTemplate",
                isRequired = false,
                disable = true
            )
        },
        AssetNodeFormProps.children to {
            AddTextField(
                value = state.value.children?.toString() ?: "",
                onValueChange = {},
                label = "children",
                isRequired = true,
                disable = true
            )
        },
        AssetNodeFormProps.labelLinks to {
            AddTextField(
                value = state.value.labelLinks?.toString() ?: "",
                onValueChange = {},
                label = "labelLinks",
                isRequired = true,
                disable = true
            )
        },
        AssetNodeFormProps.properties to {
            AddTextField(
                value = state.value.properties?.toString() ?: "",
                onValueChange = {},
                label = "properties",
                isRequired = true,
                disable = true
            )
        },
        AssetNodeFormProps.features to {
            AddTextField(
                value = state.value.features?.toString() ?: "",
                onValueChange = {},
                label = "features",
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

class AssetNodeFormDsl(
    val state: MutableState<AssetNodeIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun nodeType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("nodeType")
                renderMap.remove("nodeType")
            }
            render != null -> {
                hiddenFields.remove("nodeType")
                renderMap["nodeType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("nodeType")
                renderMap.remove("nodeType")
            }
        }
        order?.let { updateFieldOrder("nodeType", it) }
    }

    fun code(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun vendor(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun supportsTelemetry(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("supportsTelemetry", it) }
    }

    fun supportsControl(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("supportsControl", it) }
    }

    fun parent(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("parent")
                renderMap.remove("parent")
            }
            render != null -> {
                hiddenFields.remove("parent")
                renderMap["parent"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("parent")
                renderMap.remove("parent")
            }
        }
        order?.let { updateFieldOrder("parent", it) }
    }

    fun protocolTemplate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("protocolTemplate")
                renderMap.remove("protocolTemplate")
            }
            render != null -> {
                hiddenFields.remove("protocolTemplate")
                renderMap["protocolTemplate"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("protocolTemplate")
                renderMap.remove("protocolTemplate")
            }
        }
        order?.let { updateFieldOrder("protocolTemplate", it) }
    }

    fun deviceType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("deviceType", it) }
    }

    fun moduleTemplate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun children(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("children")
                renderMap.remove("children")
            }
            render != null -> {
                hiddenFields.remove("children")
                renderMap["children"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("children")
                renderMap.remove("children")
            }
        }
        order?.let { updateFieldOrder("children", it) }
    }

    fun labelLinks(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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

    fun properties(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("properties", it) }
    }

    fun features(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("features", it) }
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
        val allFields = AssetNodeFormProps.getAllFields()
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
fun rememberAssetNodeFormState(current: AssetNodeIso? = null): MutableState<AssetNodeIso> {
    return remember(current) { mutableStateOf(current ?: AssetNodeIso()) }
}