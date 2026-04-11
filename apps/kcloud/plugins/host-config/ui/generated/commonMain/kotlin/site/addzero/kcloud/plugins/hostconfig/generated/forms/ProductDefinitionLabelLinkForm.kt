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
 * ProductDefinitionLabelLink 表单属性常量
 */
object ProductDefinitionLabelLinkFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val sortIndex = "sortIndex"
    const val product = "product"
    const val label = "label"

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "sortIndex", "product", "label")
    }
}

@Composable
fun ProductDefinitionLabelLinkForm(
    state: MutableState<ProductDefinitionLabelLinkIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProductDefinitionLabelLinkFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProductDefinitionLabelLinkFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProductDefinitionLabelLinkFormOriginal(
    state: MutableState<ProductDefinitionLabelLinkIso>,
    dslConfig: ProductDefinitionLabelLinkFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProductDefinitionLabelLinkFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProductDefinitionLabelLinkFormProps.createdAt to {
            AddTextField(
                value = state.value.createdAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        ProductDefinitionLabelLinkFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        ProductDefinitionLabelLinkFormProps.sortIndex to {
            AddTextField(
                value = state.value.sortIndex?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(sortIndex = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "sortIndex",
                isRequired = true
            )
        },
        ProductDefinitionLabelLinkFormProps.product to {
            AddTextField(
                value = state.value.product?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(product = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "product",
                isRequired = true
            )
        },
        ProductDefinitionLabelLinkFormProps.label to {
            AddTextField(
                value = state.value.label?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(label = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "label",
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

class ProductDefinitionLabelLinkFormDsl(
    val state: MutableState<ProductDefinitionLabelLinkIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionLabelLinkIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProductDefinitionLabelLinkIso>) -> Unit)? = null
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

    fun sortIndex(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionLabelLinkIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProductDefinitionLabelLinkIso>) -> Unit)? = null
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

    fun label(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProductDefinitionLabelLinkIso>) -> Unit)? = null
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

        order?.let { orderValue ->
            updateFieldOrder("label", orderValue)
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
            fieldOrder.addAll(ProductDefinitionLabelLinkFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(ProductDefinitionLabelLinkFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = ProductDefinitionLabelLinkFormProps.getAllFields()
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
fun rememberProductDefinitionLabelLinkFormState(current: ProductDefinitionLabelLinkIso? = null): MutableState<ProductDefinitionLabelLinkIso> {
    return remember(current) { mutableStateOf(current ?: ProductDefinitionLabelLinkIso()) }
}