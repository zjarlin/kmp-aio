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
 * ProjectGatewayPinConfig 表单属性常量
 */
object ProjectGatewayPinConfigFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val faultIndicatorPin = "faultIndicatorPin"
    const val runningIndicatorPin = "runningIndicatorPin"
    const val project = "project"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "faultIndicatorPin", "runningIndicatorPin", "project")
}

@Composable
fun ProjectGatewayPinConfigForm(
    state: MutableState<ProjectGatewayPinConfigIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProjectGatewayPinConfigFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProjectGatewayPinConfigFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProjectGatewayPinConfigFormOriginal(
    state: MutableState<ProjectGatewayPinConfigIso>,
    dslConfig: ProjectGatewayPinConfigFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProjectGatewayPinConfigFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProjectGatewayPinConfigFormProps.createdAt to {
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
        ProjectGatewayPinConfigFormProps.updatedAt to {
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
        ProjectGatewayPinConfigFormProps.faultIndicatorPin to {
            AddTextField(
                value = state.value.faultIndicatorPin?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(faultIndicatorPin = value)
                },
                label = "faultIndicatorPin",
                isRequired = true
            )
        },
        ProjectGatewayPinConfigFormProps.runningIndicatorPin to {
            AddTextField(
                value = state.value.runningIndicatorPin?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(runningIndicatorPin = value)
                },
                label = "runningIndicatorPin",
                isRequired = true
            )
        },
        ProjectGatewayPinConfigFormProps.project to {
            AddTextField(
                value = state.value.project?.toString() ?: "",
                onValueChange = {},
                label = "project",
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

class ProjectGatewayPinConfigFormDsl(
    val state: MutableState<ProjectGatewayPinConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectGatewayPinConfigIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectGatewayPinConfigIso>) -> Unit)? = null
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

    fun faultIndicatorPin(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectGatewayPinConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("faultIndicatorPin")
                renderMap.remove("faultIndicatorPin")
            }
            render != null -> {
                hiddenFields.remove("faultIndicatorPin")
                renderMap["faultIndicatorPin"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("faultIndicatorPin")
                renderMap.remove("faultIndicatorPin")
            }
        }
        order?.let { updateFieldOrder("faultIndicatorPin", it) }
    }

    fun runningIndicatorPin(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectGatewayPinConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("runningIndicatorPin")
                renderMap.remove("runningIndicatorPin")
            }
            render != null -> {
                hiddenFields.remove("runningIndicatorPin")
                renderMap["runningIndicatorPin"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("runningIndicatorPin")
                renderMap.remove("runningIndicatorPin")
            }
        }
        order?.let { updateFieldOrder("runningIndicatorPin", it) }
    }

    fun project(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectGatewayPinConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("project")
                renderMap.remove("project")
            }
            render != null -> {
                hiddenFields.remove("project")
                renderMap["project"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("project")
                renderMap.remove("project")
            }
        }
        order?.let { updateFieldOrder("project", it) }
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
        val allFields = ProjectGatewayPinConfigFormProps.getAllFields()
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
fun rememberProjectGatewayPinConfigFormState(current: ProjectGatewayPinConfigIso? = null): MutableState<ProjectGatewayPinConfigIso> {
    return remember(current) { mutableStateOf(current ?: ProjectGatewayPinConfigIso()) }
}