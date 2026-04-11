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
 * ProjectModbusServerConfig 表单属性常量
 */
object ProjectModbusServerConfigFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val transportType = "transportType"
    const val enabled = "enabled"
    const val tcpPort = "tcpPort"
    const val portName = "portName"
    const val baudRate = "baudRate"
    const val dataBits = "dataBits"
    const val stopBits = "stopBits"
    const val parity = "parity"
    const val stationNo = "stationNo"
    const val project = "project"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "transportType", "enabled", "tcpPort", "portName", "baudRate", "dataBits", "stopBits", "parity", "stationNo", "project")
}

@Composable
fun ProjectModbusServerConfigForm(
    state: MutableState<ProjectModbusServerConfigIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProjectModbusServerConfigFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProjectModbusServerConfigFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProjectModbusServerConfigFormOriginal(
    state: MutableState<ProjectModbusServerConfigIso>,
    dslConfig: ProjectModbusServerConfigFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProjectModbusServerConfigFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProjectModbusServerConfigFormProps.createdAt to {
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
        ProjectModbusServerConfigFormProps.updatedAt to {
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
        ProjectModbusServerConfigFormProps.transportType to {
            AddTextField(
                value = state.value.transportType?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = TransportType.entries.firstOrNull { entry -> entry.name == value }
                    if (parsed != null) {
                        state.value = state.value.copy(transportType = parsed)
                    }
                },
                label = "transportType",
                isRequired = true
            )
        },
        ProjectModbusServerConfigFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "enabled"
            )
        },
        ProjectModbusServerConfigFormProps.tcpPort to {
            AddTextField(
                value = state.value.tcpPort?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(tcpPort = null)
                        parsed != null -> state.value = state.value.copy(tcpPort = parsed)
                    }
                },
                label = "tcpPort",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.portName to {
            AddTextField(
                value = state.value.portName?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(portName = value.ifEmpty { null })
                },
                label = "portName",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.baudRate to {
            AddTextField(
                value = state.value.baudRate?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(baudRate = null)
                        parsed != null -> state.value = state.value.copy(baudRate = parsed)
                    }
                },
                label = "baudRate",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.dataBits to {
            AddTextField(
                value = state.value.dataBits?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(dataBits = null)
                        parsed != null -> state.value = state.value.copy(dataBits = parsed)
                    }
                },
                label = "dataBits",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.stopBits to {
            AddTextField(
                value = state.value.stopBits?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(stopBits = null)
                        parsed != null -> state.value = state.value.copy(stopBits = parsed)
                    }
                },
                label = "stopBits",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.parity to {
            AddTextField(
                value = state.value.parity?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = Parity.entries.firstOrNull { entry -> entry.name == value }
                    when {
                        value.isEmpty() -> state.value = state.value.copy(parity = null)
                        parsed != null -> state.value = state.value.copy(parity = parsed)
                    }
                },
                label = "parity",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.stationNo to {
            AddTextField(
                value = state.value.stationNo?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(stationNo = null)
                        parsed != null -> state.value = state.value.copy(stationNo = parsed)
                    }
                },
                label = "stationNo",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.project to {
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

class ProjectModbusServerConfigFormDsl(
    val state: MutableState<ProjectModbusServerConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
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

    fun transportType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("transportType")
                renderMap.remove("transportType")
            }
            render != null -> {
                hiddenFields.remove("transportType")
                renderMap["transportType"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("transportType")
                renderMap.remove("transportType")
            }
        }
        order?.let { updateFieldOrder("transportType", it) }
    }

    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
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

    fun tcpPort(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("tcpPort")
                renderMap.remove("tcpPort")
            }
            render != null -> {
                hiddenFields.remove("tcpPort")
                renderMap["tcpPort"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("tcpPort")
                renderMap.remove("tcpPort")
            }
        }
        order?.let { updateFieldOrder("tcpPort", it) }
    }

    fun portName(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("portName")
                renderMap.remove("portName")
            }
            render != null -> {
                hiddenFields.remove("portName")
                renderMap["portName"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("portName")
                renderMap.remove("portName")
            }
        }
        order?.let { updateFieldOrder("portName", it) }
    }

    fun baudRate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("baudRate")
                renderMap.remove("baudRate")
            }
            render != null -> {
                hiddenFields.remove("baudRate")
                renderMap["baudRate"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("baudRate")
                renderMap.remove("baudRate")
            }
        }
        order?.let { updateFieldOrder("baudRate", it) }
    }

    fun dataBits(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("dataBits")
                renderMap.remove("dataBits")
            }
            render != null -> {
                hiddenFields.remove("dataBits")
                renderMap["dataBits"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("dataBits")
                renderMap.remove("dataBits")
            }
        }
        order?.let { updateFieldOrder("dataBits", it) }
    }

    fun stopBits(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("stopBits")
                renderMap.remove("stopBits")
            }
            render != null -> {
                hiddenFields.remove("stopBits")
                renderMap["stopBits"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("stopBits")
                renderMap.remove("stopBits")
            }
        }
        order?.let { updateFieldOrder("stopBits", it) }
    }

    fun parity(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("parity")
                renderMap.remove("parity")
            }
            render != null -> {
                hiddenFields.remove("parity")
                renderMap["parity"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("parity")
                renderMap.remove("parity")
            }
        }
        order?.let { updateFieldOrder("parity", it) }
    }

    fun stationNo(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
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
        order?.let { updateFieldOrder("stationNo", it) }
    }

    fun project(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectModbusServerConfigIso>) -> Unit)? = null
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
        val allFields = ProjectModbusServerConfigFormProps.getAllFields()
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
fun rememberProjectModbusServerConfigFormState(current: ProjectModbusServerConfigIso? = null): MutableState<ProjectModbusServerConfigIso> {
    return remember(current) { mutableStateOf(current ?: ProjectModbusServerConfigIso()) }
}