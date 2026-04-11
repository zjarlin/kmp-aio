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
 * AssetNodeLabelLink 表单属性常量
 */
object AssetNodeLabelLinkFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val sortIndex = "sortIndex"
    const val asset = "asset"
    const val label = "label"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "sortIndex", "asset", "label")
}

@Composable
fun AssetNodeLabelLinkForm(
    state: MutableState<AssetNodeLabelLinkIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: AssetNodeLabelLinkFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        AssetNodeLabelLinkFormOriginal(state, dslConfig)
    }
}

@Composable
fun AssetNodeLabelLinkFormOriginal(
    state: MutableState<AssetNodeLabelLinkIso>,
    dslConfig: AssetNodeLabelLinkFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = AssetNodeLabelLinkFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        AssetNodeLabelLinkFormProps.createdAt to {
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
        AssetNodeLabelLinkFormProps.updatedAt to {
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
        AssetNodeLabelLinkFormProps.sortIndex to {
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
        AssetNodeLabelLinkFormProps.asset to {
            AddTextField(
                value = state.value.asset?.toString() ?: "",
                onValueChange = {},
                label = "asset",
                isRequired = true,
                disable = true
            )
        },
        AssetNodeLabelLinkFormProps.label to {
            AddTextField(
                value = state.value.label?.toString() ?: "",
                onValueChange = {},
                label = "label",
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

class AssetNodeLabelLinkFormDsl(
    val state: MutableState<AssetNodeLabelLinkIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeLabelLinkIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<AssetNodeLabelLinkIso>) -> Unit)? = null
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

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeLabelLinkIso>) -> Unit)? = null
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

    fun asset(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeLabelLinkIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("asset")
                renderMap.remove("asset")
            }
            render != null -> {
                hiddenFields.remove("asset")
                renderMap["asset"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("asset")
                renderMap.remove("asset")
            }
        }
        order?.let { updateFieldOrder("asset", it) }
    }

    fun label(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<AssetNodeLabelLinkIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("label")
                renderMap.remove("label")
            }
            render != null -> {
                hiddenFields.remove("label")
                renderMap["label"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("label")
                renderMap.remove("label")
            }
        }
        order?.let { updateFieldOrder("label", it) }
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
        val allFields = AssetNodeLabelLinkFormProps.getAllFields()
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
fun rememberAssetNodeLabelLinkFormState(current: AssetNodeLabelLinkIso? = null): MutableState<AssetNodeLabelLinkIso> {
    return remember(current) { mutableStateOf(current ?: AssetNodeLabelLinkIso()) }
}