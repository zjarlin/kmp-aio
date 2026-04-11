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
 * ProjectProtocol 表单属性常量
 */
object ProjectProtocolFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val sortIndex = "sortIndex"
    const val project = "project"
    const val protocol = "protocol"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "sortIndex", "project", "protocol")
}

@Composable
fun ProjectProtocolForm(
    state: MutableState<ProjectProtocolIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProjectProtocolFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProjectProtocolFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProjectProtocolFormOriginal(
    state: MutableState<ProjectProtocolIso>,
    dslConfig: ProjectProtocolFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProjectProtocolFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProjectProtocolFormProps.createdAt to {
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
        ProjectProtocolFormProps.updatedAt to {
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
        ProjectProtocolFormProps.sortIndex to {
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
        ProjectProtocolFormProps.project to {
            AddTextField(
                value = state.value.project?.toString() ?: "",
                onValueChange = {},
                label = "project",
                isRequired = true,
                disable = true
            )
        },
        ProjectProtocolFormProps.protocol to {
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

class ProjectProtocolFormDsl(
    val state: MutableState<ProjectProtocolIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectProtocolIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectProtocolIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectProtocolIso>) -> Unit)? = null
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

    fun project(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectProtocolIso>) -> Unit)? = null
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

    fun protocol(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectProtocolIso>) -> Unit)? = null
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
        val allFields = ProjectProtocolFormProps.getAllFields()
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
fun rememberProjectProtocolFormState(current: ProjectProtocolIso? = null): MutableState<ProjectProtocolIso> {
    return remember(current) { mutableStateOf(current ?: ProjectProtocolIso()) }
}