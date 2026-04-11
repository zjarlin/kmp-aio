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
 * ProjectMqttConfig 表单属性常量
 */
object ProjectMqttConfigFormProps {
    const val createdAt = "createdAt"
    const val updatedAt = "updatedAt"
    const val enabled = "enabled"
    const val breakpointResume = "breakpointResume"
    const val gatewayName = "gatewayName"
    const val vendor = "vendor"
    const val host = "host"
    const val port = "port"
    const val topic = "topic"
    const val gatewayId = "gatewayId"
    const val authEnabled = "authEnabled"
    const val username = "username"
    const val passwordEncrypted = "passwordEncrypted"
    const val tlsEnabled = "tlsEnabled"
    const val certFileRef = "certFileRef"
    const val clientId = "clientId"
    const val keepAliveSec = "keepAliveSec"
    const val qos = "qos"
    const val reportPeriodSec = "reportPeriodSec"
    const val precision = "precision"
    const val valueChangeRatioEnabled = "valueChangeRatioEnabled"
    const val cloudControlDisabled = "cloudControlDisabled"
    const val project = "project"

    fun getAllFields(): List<String> = listOf("createdAt", "updatedAt", "enabled", "breakpointResume", "gatewayName", "vendor", "host", "port", "topic", "gatewayId", "authEnabled", "username", "passwordEncrypted", "tlsEnabled", "certFileRef", "clientId", "keepAliveSec", "qos", "reportPeriodSec", "precision", "valueChangeRatioEnabled", "cloudControlDisabled", "project")
}

@Composable
fun ProjectMqttConfigForm(
    state: MutableState<ProjectMqttConfigIso>,
    visible: Boolean,
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    confirmEnabled: Boolean = true,
    dslConfig: ProjectMqttConfigFormDsl.() -> Unit = {}
) {
    AddDrawer(
        visible = visible,
        title = title,
        onClose = onClose,
        onSubmit = onSubmit,
        confirmEnabled = confirmEnabled,
    ) {
        ProjectMqttConfigFormOriginal(state, dslConfig)
    }
}

@Composable
fun ProjectMqttConfigFormOriginal(
    state: MutableState<ProjectMqttConfigIso>,
    dslConfig: ProjectMqttConfigFormDsl.() -> Unit = {}
) {
    val renderMap = remember { mutableMapOf<String, @Composable () -> Unit>() }
    val dsl = ProjectMqttConfigFormDsl(state, renderMap).apply(dslConfig)
    val defaultRenderMap = linkedMapOf<String, @Composable () -> Unit>(
        ProjectMqttConfigFormProps.createdAt to {
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
        ProjectMqttConfigFormProps.updatedAt to {
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
        ProjectMqttConfigFormProps.enabled to {
            AddSwitchField(
                value = state.value.enabled ?: false,
                onValueChange = { state.value = state.value.copy(enabled = it) },
                label = "enabled"
            )
        },
        ProjectMqttConfigFormProps.breakpointResume to {
            AddSwitchField(
                value = state.value.breakpointResume ?: false,
                onValueChange = { state.value = state.value.copy(breakpointResume = it) },
                label = "breakpointResume"
            )
        },
        ProjectMqttConfigFormProps.gatewayName to {
            AddTextField(
                value = state.value.gatewayName?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(gatewayName = value.ifEmpty { null })
                },
                label = "gatewayName",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.vendor to {
            AddTextField(
                value = state.value.vendor?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(vendor = value.ifEmpty { null })
                },
                label = "vendor",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.host to {
            AddTextField(
                value = state.value.host?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(host = value.ifEmpty { null })
                },
                label = "host",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.port to {
            AddTextField(
                value = state.value.port?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(port = null)
                        parsed != null -> state.value = state.value.copy(port = parsed)
                    }
                },
                label = "port",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.topic to {
            AddTextField(
                value = state.value.topic?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(topic = value.ifEmpty { null })
                },
                label = "topic",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.gatewayId to {
            AddTextField(
                value = state.value.gatewayId?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(gatewayId = value.ifEmpty { null })
                },
                label = "gatewayId",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.authEnabled to {
            AddSwitchField(
                value = state.value.authEnabled ?: false,
                onValueChange = { state.value = state.value.copy(authEnabled = it) },
                label = "authEnabled"
            )
        },
        ProjectMqttConfigFormProps.username to {
            AddTextField(
                value = state.value.username?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(username = value.ifEmpty { null })
                },
                label = "username",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.passwordEncrypted to {
            AddTextField(
                value = state.value.passwordEncrypted?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(passwordEncrypted = value.ifEmpty { null })
                },
                label = "passwordEncrypted",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.tlsEnabled to {
            AddSwitchField(
                value = state.value.tlsEnabled ?: false,
                onValueChange = { state.value = state.value.copy(tlsEnabled = it) },
                label = "tlsEnabled"
            )
        },
        ProjectMqttConfigFormProps.certFileRef to {
            AddTextField(
                value = state.value.certFileRef?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(certFileRef = value.ifEmpty { null })
                },
                label = "certFileRef",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.clientId to {
            AddTextField(
                value = state.value.clientId?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(clientId = value.ifEmpty { null })
                },
                label = "clientId",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.keepAliveSec to {
            AddTextField(
                value = state.value.keepAliveSec?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(keepAliveSec = null)
                        parsed != null -> state.value = state.value.copy(keepAliveSec = parsed)
                    }
                },
                label = "keepAliveSec",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.qos to {
            AddTextField(
                value = state.value.qos?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(qos = null)
                        parsed != null -> state.value = state.value.copy(qos = parsed)
                    }
                },
                label = "qos",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.reportPeriodSec to {
            AddTextField(
                value = state.value.reportPeriodSec?.toString() ?: "",
                onValueChange = { value ->
                    val parsed = value.toIntOrNull()
                    when {
                        value.isEmpty() -> state.value = state.value.copy(reportPeriodSec = null)
                        parsed != null -> state.value = state.value.copy(reportPeriodSec = parsed)
                    }
                },
                label = "reportPeriodSec",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.precision to {
            AddTextField(
                value = state.value.precision?.toString() ?: "",
                onValueChange = { value ->
                    state.value = state.value.copy(precision = value.ifEmpty { null })
                },
                label = "precision",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.valueChangeRatioEnabled to {
            AddSwitchField(
                value = state.value.valueChangeRatioEnabled ?: false,
                onValueChange = { state.value = state.value.copy(valueChangeRatioEnabled = it) },
                label = "valueChangeRatioEnabled"
            )
        },
        ProjectMqttConfigFormProps.cloudControlDisabled to {
            AddSwitchField(
                value = state.value.cloudControlDisabled ?: false,
                onValueChange = { state.value = state.value.copy(cloudControlDisabled = it) },
                label = "cloudControlDisabled"
            )
        },
        ProjectMqttConfigFormProps.project to {
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

class ProjectMqttConfigFormDsl(
    val state: MutableState<ProjectMqttConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>,
) {
    val hiddenFields = mutableSetOf<String>()
    val fieldOrder = mutableListOf<String>()
    private val fieldOrderMap = mutableMapOf<String, Int>()

    fun createdAt(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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

    fun enabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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

    fun breakpointResume(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("breakpointResume")
                renderMap.remove("breakpointResume")
            }
            render != null -> {
                hiddenFields.remove("breakpointResume")
                renderMap["breakpointResume"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("breakpointResume")
                renderMap.remove("breakpointResume")
            }
        }
        order?.let { updateFieldOrder("breakpointResume", it) }
    }

    fun gatewayName(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("gatewayName")
                renderMap.remove("gatewayName")
            }
            render != null -> {
                hiddenFields.remove("gatewayName")
                renderMap["gatewayName"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("gatewayName")
                renderMap.remove("gatewayName")
            }
        }
        order?.let { updateFieldOrder("gatewayName", it) }
    }

    fun vendor(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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

    fun host(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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

    fun port(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("port")
                renderMap.remove("port")
            }
            render != null -> {
                hiddenFields.remove("port")
                renderMap["port"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("port")
                renderMap.remove("port")
            }
        }
        order?.let { updateFieldOrder("port", it) }
    }

    fun topic(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("topic")
                renderMap.remove("topic")
            }
            render != null -> {
                hiddenFields.remove("topic")
                renderMap["topic"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("topic")
                renderMap.remove("topic")
            }
        }
        order?.let { updateFieldOrder("topic", it) }
    }

    fun gatewayId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("gatewayId")
                renderMap.remove("gatewayId")
            }
            render != null -> {
                hiddenFields.remove("gatewayId")
                renderMap["gatewayId"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("gatewayId")
                renderMap.remove("gatewayId")
            }
        }
        order?.let { updateFieldOrder("gatewayId", it) }
    }

    fun authEnabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("authEnabled")
                renderMap.remove("authEnabled")
            }
            render != null -> {
                hiddenFields.remove("authEnabled")
                renderMap["authEnabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("authEnabled")
                renderMap.remove("authEnabled")
            }
        }
        order?.let { updateFieldOrder("authEnabled", it) }
    }

    fun username(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("username")
                renderMap.remove("username")
            }
            render != null -> {
                hiddenFields.remove("username")
                renderMap["username"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("username")
                renderMap.remove("username")
            }
        }
        order?.let { updateFieldOrder("username", it) }
    }

    fun passwordEncrypted(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("passwordEncrypted")
                renderMap.remove("passwordEncrypted")
            }
            render != null -> {
                hiddenFields.remove("passwordEncrypted")
                renderMap["passwordEncrypted"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("passwordEncrypted")
                renderMap.remove("passwordEncrypted")
            }
        }
        order?.let { updateFieldOrder("passwordEncrypted", it) }
    }

    fun tlsEnabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("tlsEnabled")
                renderMap.remove("tlsEnabled")
            }
            render != null -> {
                hiddenFields.remove("tlsEnabled")
                renderMap["tlsEnabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("tlsEnabled")
                renderMap.remove("tlsEnabled")
            }
        }
        order?.let { updateFieldOrder("tlsEnabled", it) }
    }

    fun certFileRef(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("certFileRef")
                renderMap.remove("certFileRef")
            }
            render != null -> {
                hiddenFields.remove("certFileRef")
                renderMap["certFileRef"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("certFileRef")
                renderMap.remove("certFileRef")
            }
        }
        order?.let { updateFieldOrder("certFileRef", it) }
    }

    fun clientId(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("clientId")
                renderMap.remove("clientId")
            }
            render != null -> {
                hiddenFields.remove("clientId")
                renderMap["clientId"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("clientId")
                renderMap.remove("clientId")
            }
        }
        order?.let { updateFieldOrder("clientId", it) }
    }

    fun keepAliveSec(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("keepAliveSec")
                renderMap.remove("keepAliveSec")
            }
            render != null -> {
                hiddenFields.remove("keepAliveSec")
                renderMap["keepAliveSec"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("keepAliveSec")
                renderMap.remove("keepAliveSec")
            }
        }
        order?.let { updateFieldOrder("keepAliveSec", it) }
    }

    fun qos(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("qos")
                renderMap.remove("qos")
            }
            render != null -> {
                hiddenFields.remove("qos")
                renderMap["qos"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("qos")
                renderMap.remove("qos")
            }
        }
        order?.let { updateFieldOrder("qos", it) }
    }

    fun reportPeriodSec(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("reportPeriodSec")
                renderMap.remove("reportPeriodSec")
            }
            render != null -> {
                hiddenFields.remove("reportPeriodSec")
                renderMap["reportPeriodSec"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("reportPeriodSec")
                renderMap.remove("reportPeriodSec")
            }
        }
        order?.let { updateFieldOrder("reportPeriodSec", it) }
    }

    fun precision(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("precision")
                renderMap.remove("precision")
            }
            render != null -> {
                hiddenFields.remove("precision")
                renderMap["precision"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("precision")
                renderMap.remove("precision")
            }
        }
        order?.let { updateFieldOrder("precision", it) }
    }

    fun valueChangeRatioEnabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("valueChangeRatioEnabled")
                renderMap.remove("valueChangeRatioEnabled")
            }
            render != null -> {
                hiddenFields.remove("valueChangeRatioEnabled")
                renderMap["valueChangeRatioEnabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("valueChangeRatioEnabled")
                renderMap.remove("valueChangeRatioEnabled")
            }
        }
        order?.let { updateFieldOrder("valueChangeRatioEnabled", it) }
    }

    fun cloudControlDisabled(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
    ) {
        when {
            hidden -> {
                hiddenFields.add("cloudControlDisabled")
                renderMap.remove("cloudControlDisabled")
            }
            render != null -> {
                hiddenFields.remove("cloudControlDisabled")
                renderMap["cloudControlDisabled"] = { render(state) }
            }
            else -> {
                hiddenFields.remove("cloudControlDisabled")
                renderMap.remove("cloudControlDisabled")
            }
        }
        order?.let { updateFieldOrder("cloudControlDisabled", it) }
    }

    fun project(
        hidden: Boolean = false,
        order: Int? = null,
        render: (@Composable (MutableState<ProjectMqttConfigIso>) -> Unit)? = null
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
        val allFields = ProjectMqttConfigFormProps.getAllFields()
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
fun rememberProjectMqttConfigFormState(current: ProjectMqttConfigIso? = null): MutableState<ProjectMqttConfigIso> {
    return remember(current) { mutableStateOf(current ?: ProjectMqttConfigIso()) }
}