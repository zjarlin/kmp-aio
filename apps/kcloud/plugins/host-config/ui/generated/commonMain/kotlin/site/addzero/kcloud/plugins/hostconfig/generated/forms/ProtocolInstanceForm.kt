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
 * ProtocolInstance 表单属性常量
 */
object ProtocolInstanceFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val name = "name"
    const val pollingIntervalMs = "pollingIntervalMs"
    const val transportType = "transportType"
    const val host = "host"
    const val tcpPort = "tcpPort"
    const val portName = "portName"
    const val baudRate = "baudRate"
    const val dataBits = "dataBits"
    const val stopBits = "stopBits"
    const val parity = "parity"
    const val responseTimeoutMs = "responseTimeoutMs"
    const val protocolTemplate = "protocolTemplate"
    const val projectLinks = "projectLinks"
    const val projects = "projects"
    const val devices = "devices"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "name", "pollingIntervalMs", "transportType", "host", "tcpPort", "portName", "baudRate", "dataBits", "stopBits", "parity", "responseTimeoutMs", "protocolTemplate", "projectLinks", "projects", "devices")
}

@Composable
fun ProtocolInstanceForm(
    state: MutableState<ProtocolInstanceIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProtocolInstanceFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProtocolInstanceFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProtocolInstanceFormOriginal(
    state: MutableState<ProtocolInstanceIso>,
    dslConfig: ProtocolInstanceFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProtocolInstanceFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProtocolInstanceFormProps.createdAt to {
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
        ProtocolInstanceFormProps.updatedAt to {
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
        ProtocolInstanceFormProps.name to {
            AddTextField(
                value = state.value.name?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(name = value)
                },
                label = "name",
                isRequired = true
            )
        },
        ProtocolInstanceFormProps.pollingIntervalMs to {
            AddTextField(
                value = state.value.pollingIntervalMs?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    if (parsed != null) {
                        state.value = state.value.copy(pollingIntervalMs = parsed)
                    }
                },
                label = "pollingIntervalMs",
                isRequired = true
            )
        },
        ProtocolInstanceFormProps.transportType to {
            AddTextField(
                value = state.value.transportType?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = TransportType.entries.firstOrNull { entry -> entry.name == value }
                    when {
                        value.isEmpty() -> state.value = state.value.copy(transportType = null)
                        parsed != null -> state.value = state.value.copy(transportType = parsed)
                    }
                },
                label = "transportType",
                isRequired = false
            )
        },
        ProtocolInstanceFormProps.host to {
            AddTextField(
                value = state.value.host?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(host = value.ifEmpty { null })
                },
                label = "host",
                isRequired = false
            )
        },
        ProtocolInstanceFormProps.tcpPort to {
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
        ProtocolInstanceFormProps.portName to {
            AddTextField(
                value = state.value.portName?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(portName = value.ifEmpty { null })
                },
                label = "portName",
                isRequired = false
            )
        },
        ProtocolInstanceFormProps.baudRate to {
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
        ProtocolInstanceFormProps.dataBits to {
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
        ProtocolInstanceFormProps.stopBits to {
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
        ProtocolInstanceFormProps.parity to {
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
        ProtocolInstanceFormProps.responseTimeoutMs to {
            AddTextField(
                value = state.value.responseTimeoutMs?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(responseTimeoutMs = null)
                        parsed != null -> state.value = state.value.copy(responseTimeoutMs = parsed)
                    }
                },
                label = "responseTimeoutMs",
                isRequired = false
            )
        },
        ProtocolInstanceFormProps.protocolTemplate to {
            AddTextField(
                value = state.value.protocolTemplate?.toString() ?: "",
                onValueChange = {},
                label = "protocolTemplate",
                isRequired = true,
                disable = true
            )
        },
        ProtocolInstanceFormProps.projectLinks to {
            AddTextField(
                value = state.value.projectLinks?.toString() ?: "",
                onValueChange = {},
                label = "projectLinks",
                isRequired = true,
                disable = true
            )
        },
        ProtocolInstanceFormProps.projects to {
            AddTextField(
                value = state.value.projects?.toString() ?: "",
                onValueChange = {},
                label = "projects",
                isRequired = true,
                disable = true
            )
        },
        ProtocolInstanceFormProps.devices to {
            AddTextField(
                value = state.value.devices?.toString() ?: "",
                onValueChange = {},
                label = "devices",
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

class ProtocolInstanceFormDsl(
    val state: MutableState<ProtocolInstanceIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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

    fun pollingIntervalMs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("pollingIntervalMs")
                renderMap.remove("pollingIntervalMs")
            }
            render != null -> {
                hiddenFields.remove("pollingIntervalMs")
                renderMap["pollingIntervalMs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("pollingIntervalMs")
                renderMap.remove("pollingIntervalMs")
            }
        }
        order?.let { updateFieldOrder("pollingIntervalMs", it) }
    }

    fun transportType(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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

    fun host(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("host")
                renderMap.remove("host")
            }
            render != null -> {
                hiddenFields.remove("host")
                renderMap["host"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("host")
                renderMap.remove("host")
            }
        }
        order?.let { updateFieldOrder("host", it) }
    }

    fun tcpPort(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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

    fun responseTimeoutMs(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("responseTimeoutMs")
                renderMap.remove("responseTimeoutMs")
            }
            render != null -> {
                hiddenFields.remove("responseTimeoutMs")
                renderMap["responseTimeoutMs"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("responseTimeoutMs")
                renderMap.remove("responseTimeoutMs")
            }
        }
        order?.let { updateFieldOrder("responseTimeoutMs", it) }
    }

    fun protocolTemplate(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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

    fun projectLinks(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("projectLinks")
                renderMap.remove("projectLinks")
            }
            render != null -> {
                hiddenFields.remove("projectLinks")
                renderMap["projectLinks"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("projectLinks")
                renderMap.remove("projectLinks")
            }
        }
        order?.let { updateFieldOrder("projectLinks", it) }
    }

    fun projects(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("projects")
                renderMap.remove("projects")
            }
            render != null -> {
                hiddenFields.remove("projects")
                renderMap["projects"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("projects")
                renderMap.remove("projects")
            }
        }
        order?.let { updateFieldOrder("projects", it) }
    }

    fun devices(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProtocolInstanceIso>) -> Unit)? = null
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

    fun hide(vararg fields: String) {
        hiddenFields.addAll(fields)
    }

    fun order(vararg fields: String) {
        fieldOrder.clear()
        fieldOrder.addAll(fields)
    }

    private fun updateFieldOrder(fieldName: String, orderValue: Int) {
        fieldOrderMap[fieldName] = orderValue
        val allFields = ProtocolInstanceFormProps.getAllFields()
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
fun rememberProtocolInstanceFormState(current: ProtocolInstanceIso? = null): MutableState<ProtocolInstanceIso> {
    return remember(current) { mutableStateOf(current ?: ProtocolInstanceIso()) }
}