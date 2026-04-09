@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
import site.addzero.kcloud.plugins.hostconfig.cloud.CloudViewModel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigBooleanField
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigDialog
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigFormSection
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigKeyValueRow
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigPanel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigTextField
import site.addzero.kcloud.plugins.hostconfig.common.orDash

@Route(
    title = "云接入",
    routePath = "host-config/cloud",
    icon = "Apps",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
    ),
)
@Composable
fun CloudScreen() {
    val viewModel = koinViewModel<CloudViewModel>()
    val state = viewModel.screenState
    var editorVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.projects,
            selectedId = state.selectedProjectId,
            onNodeClick = { project ->
                viewModel.selectProject(project.id)
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.30f),
            searchPlaceholder = "搜索工程",
            getId = { item -> item.id },
            getLabel = { item -> item.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.SettingsApplications },
            header = {
                state.errorMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                state.noticeMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(0.70f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HostConfigPanel(
                title = state.selectedProject?.name ?: "未选择工程",
                subtitle = "MQTT 指标与接入参数概览",
                actions = {
                    WorkbenchActionButton(
                        text = "编辑 MQTT",
                        onClick = {
                            editorVisible = true
                            viewModel.clearNotice()
                        },
                        variant = WorkbenchButtonVariant.Default,
                        enabled = state.selectedProjectId != null,
                    )
                },
            ) {
                HostConfigKeyValueRow("启用 MQTT", if (state.mqttConfig.enabled) "是" else "否")
                HostConfigKeyValueRow("网关名称", state.mqttConfig.gatewayName.orDash())
                HostConfigKeyValueRow("接入地址", state.mqttConfig.host.orDash())
                HostConfigKeyValueRow("端口", state.mqttConfig.port?.toString() ?: "-")
                HostConfigKeyValueRow("主题", state.mqttConfig.topic.orDash())
                HostConfigKeyValueRow("QoS", state.mqttConfig.qos?.toString() ?: "-")
                HostConfigKeyValueRow("Client ID", state.mqttConfig.clientId.orDash())
                HostConfigKeyValueRow("精度阈值", state.mqttConfig.precision.orDash())
            }

            HostConfigPanel(
                title = "状态指标",
                subtitle = "用于快速确认当前工程的云接入关键开关。",
            ) {
                HostConfigKeyValueRow("断点续传", if (state.mqttConfig.breakpointResume) "开启" else "关闭")
                HostConfigKeyValueRow("启用认证", if (state.mqttConfig.authEnabled) "开启" else "关闭")
                HostConfigKeyValueRow("启用 TLS", if (state.mqttConfig.tlsEnabled) "开启" else "关闭")
                HostConfigKeyValueRow("值变化上报", if (state.mqttConfig.valueChangeRatioEnabled) "开启" else "关闭")
                HostConfigKeyValueRow("禁用云端控制", if (state.mqttConfig.cloudControlDisabled) "是" else "否")
            }
        }
    }

    if (editorVisible && state.selectedProjectId != null) {
        CloudConfigDialog(
            initial = state.mqttConfig,
            saving = state.busy,
            onDismissRequest = {
                editorVisible = false
            },
            onSave = { request ->
                viewModel.saveConfig(request)
                editorVisible = false
            },
        )
    }
}

@Composable
private fun CloudConfigDialog(
    initial: site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (ProjectMqttConfigRequest) -> Unit,
) {
    var enabled by remember(initial.id, initial.enabled) { mutableStateOf(initial.enabled) }
    var breakpointResume by remember(initial.id, initial.breakpointResume) { mutableStateOf(initial.breakpointResume) }
    var gatewayName by remember(initial.id, initial.gatewayName) { mutableStateOf(initial.gatewayName.orEmpty()) }
    var vendor by remember(initial.id, initial.vendor) { mutableStateOf(initial.vendor.orEmpty()) }
    var host by remember(initial.id, initial.host) { mutableStateOf(initial.host.orEmpty()) }
    var port by remember(initial.id, initial.port) { mutableStateOf(initial.port?.toString().orEmpty()) }
    var topic by remember(initial.id, initial.topic) { mutableStateOf(initial.topic.orEmpty()) }
    var gatewayId by remember(initial.id, initial.gatewayId) { mutableStateOf(initial.gatewayId.orEmpty()) }
    var authEnabled by remember(initial.id, initial.authEnabled) { mutableStateOf(initial.authEnabled) }
    var username by remember(initial.id, initial.username) { mutableStateOf(initial.username.orEmpty()) }
    var passwordEncrypted by remember(initial.id, initial.passwordEncrypted) { mutableStateOf(initial.passwordEncrypted.orEmpty()) }
    var tlsEnabled by remember(initial.id, initial.tlsEnabled) { mutableStateOf(initial.tlsEnabled) }
    var certFileRef by remember(initial.id, initial.certFileRef) { mutableStateOf(initial.certFileRef.orEmpty()) }
    var clientId by remember(initial.id, initial.clientId) { mutableStateOf(initial.clientId.orEmpty()) }
    var keepAliveSec by remember(initial.id, initial.keepAliveSec) { mutableStateOf(initial.keepAliveSec?.toString().orEmpty()) }
    var qos by remember(initial.id, initial.qos) { mutableStateOf(initial.qos?.toString().orEmpty()) }
    var reportPeriodSec by remember(initial.id, initial.reportPeriodSec) { mutableStateOf(initial.reportPeriodSec?.toString().orEmpty()) }
    var precision by remember(initial.id, initial.precision) { mutableStateOf(initial.precision.orEmpty()) }
    var valueChangeRatioEnabled by remember(initial.id, initial.valueChangeRatioEnabled) { mutableStateOf(initial.valueChangeRatioEnabled) }
    var cloudControlDisabled by remember(initial.id, initial.cloudControlDisabled) { mutableStateOf(initial.cloudControlDisabled) }

    HostConfigDialog(
        title = "编辑 MQTT",
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton(
                text = "取消",
                onClick = onDismissRequest,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    onSave(
                        ProjectMqttConfigRequest(
                            enabled = enabled,
                            breakpointResume = breakpointResume,
                            gatewayName = gatewayName.ifBlank { null },
                            vendor = vendor.ifBlank { null },
                            host = host.ifBlank { null },
                            port = port.toIntOrNull(),
                            topic = topic.ifBlank { null },
                            gatewayId = gatewayId.ifBlank { null },
                            authEnabled = authEnabled,
                            username = username.ifBlank { null },
                            passwordEncrypted = passwordEncrypted.ifBlank { null },
                            tlsEnabled = tlsEnabled,
                            certFileRef = certFileRef.ifBlank { null },
                            clientId = clientId.ifBlank { null },
                            keepAliveSec = keepAliveSec.toIntOrNull(),
                            qos = qos.toIntOrNull(),
                            reportPeriodSec = reportPeriodSec.toIntOrNull(),
                            precision = precision.ifBlank { null },
                            valueChangeRatioEnabled = valueChangeRatioEnabled,
                            cloudControlDisabled = cloudControlDisabled,
                        ),
                    )
                },
                enabled = !saving,
            )
        },
    ) {
        HostConfigFormSection(
            title = "开关策略",
            subtitle = "高频布尔项集中放在首屏，方便快速核对。",
        ) {
            item {
                HostConfigBooleanField("启用 MQTT", enabled, { enabled = it })
            }
            item {
                HostConfigBooleanField("断点续传", breakpointResume, { breakpointResume = it })
            }
            item {
                HostConfigBooleanField("启用认证", authEnabled, { authEnabled = it })
            }
            item {
                HostConfigBooleanField("启用 TLS", tlsEnabled, { tlsEnabled = it })
            }
            item {
                HostConfigBooleanField("值变化上报", valueChangeRatioEnabled, { valueChangeRatioEnabled = it })
            }
            item {
                HostConfigBooleanField("禁用云端控制", cloudControlDisabled, { cloudControlDisabled = it })
            }
        }
        HostConfigFormSection(
            title = "连接参数",
            subtitle = "地址、主题和网关标识默认双栏并排。",
        ) {
            item {
                HostConfigTextField("网关名称", gatewayName, { gatewayName = it })
            }
            item {
                HostConfigTextField("云平台厂家", vendor, { vendor = it })
            }
            item {
                HostConfigTextField("IP 地址", host, { host = it }, placeholder = "例如 10.0.0.15")
            }
            item {
                HostConfigTextField("端口号", port, { port = it })
            }
            item {
                HostConfigTextField("主题", topic, { topic = it })
            }
            item {
                HostConfigTextField("网关 ID", gatewayId, { gatewayId = it })
            }
            item {
                HostConfigTextField("Client ID", clientId, { clientId = it })
            }
            item {
                HostConfigTextField("QoS", qos, { qos = it })
            }
            item {
                HostConfigTextField("保活时间", keepAliveSec, { keepAliveSec = it })
            }
            item {
                HostConfigTextField("上报周期", reportPeriodSec, { reportPeriodSec = it })
            }
            item {
                HostConfigTextField("变化精度", precision, { precision = it })
            }
        }
        HostConfigFormSection(
            title = "认证与证书",
            subtitle = "认证和 TLS 相关字段单独成组，避免和连接参数混在一起。",
        ) {
            item {
                HostConfigTextField("用户名", username, { username = it })
            }
            item {
                HostConfigTextField("密码", passwordEncrypted, { passwordEncrypted = it })
            }
            item {
                HostConfigTextField("证书引用", certFileRef, { certFileRef = it })
            }
        }
    }
}
