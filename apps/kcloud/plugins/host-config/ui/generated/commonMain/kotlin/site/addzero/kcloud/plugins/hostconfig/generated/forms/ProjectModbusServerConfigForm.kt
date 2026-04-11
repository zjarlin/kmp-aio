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

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "transportType", "enabled", "tcpPort", "portName", "baudRate", "dataBits", "stopBits", "parity", "stationNo", "project")
    }
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
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        ProjectModbusServerConfigFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        ProjectModbusServerConfigFormProps.transportType to {
            AddTextField(
                value = state.value.transportType?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(transportType = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "transportType",
                isRequired = true
            )
        },
        ProjectModbusServerConfigFormProps.enabled to {
            AddTextField(
                value = state.value.enabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(enabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "enabled",
                isRequired = true
            )
        },
        ProjectModbusServerConfigFormProps.tcpPort to {
            AddTextField(
                value = state.value.tcpPort?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(tcpPort = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "tcpPort",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.portName to {
            AddTextField(
                value = state.value.portName?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(portName = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "portName",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.baudRate to {
            AddTextField(
                value = state.value.baudRate?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(baudRate = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "baudRate",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.dataBits to {
            AddTextField(
                value = state.value.dataBits?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(dataBits = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "dataBits",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.stopBits to {
            AddTextField(
                value = state.value.stopBits?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(stopBits = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "stopBits",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.parity to {
            AddTextField(
                value = state.value.parity?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(parity = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "parity",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.stationNo to {
            AddTextField(
                value = state.value.stationNo?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(stationNo = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "stationNo",
                isRequired = false
            )
        },
        ProjectModbusServerConfigFormProps.project to {
            AddTextField(
                value = state.value.project?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(project = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "project",
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

class ProjectModbusServerConfigFormDsl(
    val state: MutableState<ProjectModbusServerConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
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

        order?.let { orderValue ->
            updateFieldOrder("createdAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("updatedAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("transportType", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("enabled", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("tcpPort", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("portName", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("baudRate", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("dataBits", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("stopBits", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("parity", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("stationNo", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("project", orderValue)
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
            fieldOrder.addAll(ProjectModbusServerConfigFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(ProjectModbusServerConfigFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
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