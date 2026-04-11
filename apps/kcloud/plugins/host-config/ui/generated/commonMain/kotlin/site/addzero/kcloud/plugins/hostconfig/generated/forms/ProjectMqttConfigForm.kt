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

    fun getAllFields(): List<String> {
        return listOf("createdAt", "updatedAt", "enabled", "breakpointResume", "gatewayName", "vendor", "host", "port", "topic", "gatewayId", "authEnabled", "username", "passwordEncrypted", "tlsEnabled", "certFileRef", "clientId", "keepAliveSec", "qos", "reportPeriodSec", "precision", "valueChangeRatioEnabled", "cloudControlDisabled", "project")
    }
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
                onValueChange = {
                    state.value = state.value.copy(createdAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "createdAt",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.updatedAt to {
            AddTextField(
                value = state.value.updatedAt?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(updatedAt = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "updatedAt",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.enabled to {
            AddTextField(
                value = state.value.enabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(enabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "enabled",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.breakpointResume to {
            AddTextField(
                value = state.value.breakpointResume?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(breakpointResume = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "breakpointResume",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.gatewayName to {
            AddTextField(
                value = state.value.gatewayName?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(gatewayName = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "gatewayName",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.vendor to {
            AddTextField(
                value = state.value.vendor?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(vendor = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "vendor",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.host to {
            AddTextField(
                value = state.value.host?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(host = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "host",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.port to {
            AddTextField(
                value = state.value.port?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(port = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "port",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.topic to {
            AddTextField(
                value = state.value.topic?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(topic = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "topic",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.gatewayId to {
            AddTextField(
                value = state.value.gatewayId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(gatewayId = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "gatewayId",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.authEnabled to {
            AddTextField(
                value = state.value.authEnabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(authEnabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "authEnabled",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.username to {
            AddTextField(
                value = state.value.username?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(username = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "username",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.passwordEncrypted to {
            AddTextField(
                value = state.value.passwordEncrypted?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(passwordEncrypted = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "passwordEncrypted",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.tlsEnabled to {
            AddTextField(
                value = state.value.tlsEnabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(tlsEnabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "tlsEnabled",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.certFileRef to {
            AddTextField(
                value = state.value.certFileRef?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(certFileRef = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "certFileRef",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.clientId to {
            AddTextField(
                value = state.value.clientId?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(clientId = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "clientId",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.keepAliveSec to {
            AddTextField(
                value = state.value.keepAliveSec?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(keepAliveSec = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "keepAliveSec",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.qos to {
            AddTextField(
                value = state.value.qos?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(qos = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "qos",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.reportPeriodSec to {
            AddTextField(
                value = state.value.reportPeriodSec?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(reportPeriodSec = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "reportPeriodSec",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.precision to {
            AddTextField(
                value = state.value.precision?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(precision = if (it.isNullOrEmpty()) null else it.parseObjectByKtx())
                },
                label = "precision",
                isRequired = false
            )
        },
        ProjectMqttConfigFormProps.valueChangeRatioEnabled to {
            AddTextField(
                value = state.value.valueChangeRatioEnabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(valueChangeRatioEnabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "valueChangeRatioEnabled",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.cloudControlDisabled to {
            AddTextField(
                value = state.value.cloudControlDisabled?.toString() ?: "",
                onValueChange = {
                    state.value = state.value.copy(cloudControlDisabled = if (it.isNullOrEmpty()) "" else it.parseObjectByKtx())
                },
                label = "cloudControlDisabled",
                isRequired = true
            )
        },
        ProjectMqttConfigFormProps.project to {
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

class ProjectMqttConfigFormDsl(
    val state: MutableState<ProjectMqttConfigIso>,
    private val renderMap: MutableMap<String, @Composable () -> Unit>
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

        order?.let { orderValue ->
            updateFieldOrder("createdAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("updatedAt", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("enabled", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("breakpointResume", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("gatewayName", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("vendor", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("host", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("port", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("topic", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("gatewayId", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("authEnabled", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("username", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("passwordEncrypted", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("tlsEnabled", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("certFileRef", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("clientId", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("keepAliveSec", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("qos", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("reportPeriodSec", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("precision", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("valueChangeRatioEnabled", orderValue)
        }
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

        order?.let { orderValue ->
            updateFieldOrder("cloudControlDisabled", orderValue)
        }
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
            fieldOrder.addAll(ProjectMqttConfigFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index, newFields.toList())
        }
    }

    fun insertAfter(targetField: String, vararg newFields: String) {
        if (fieldOrder.isEmpty()) {
            fieldOrder.addAll(ProjectMqttConfigFormProps.getAllFields())
        }
        val index = fieldOrder.indexOf(targetField)
        if (index >= 0) {
            fieldOrder.addAll(index + 1, newFields.toList())
        }
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