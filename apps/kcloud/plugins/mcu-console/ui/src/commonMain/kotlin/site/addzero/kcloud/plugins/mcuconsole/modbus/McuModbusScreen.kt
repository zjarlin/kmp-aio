//package site.addzero.kcloud.plugins.mcuconsole.modbus
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ColumnScope
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//import org.koin.compose.koinInject
//import site.addzero.annotation.Route
//import site.addzero.annotation.RoutePlacement
//import site.addzero.annotation.RouteScene
//import site.addzero.cupertino.workbench.material3.MaterialTheme
//import site.addzero.cupertino.workbench.material3.Surface
//import site.addzero.cupertino.workbench.material3.Text
//import site.addzero.kcloud.plugins.mcuconsole.modbus.device.McuModbusDeviceInfoResponse
//
//@Route(
//    value = "开发工具",
//    title = "Modbus",
//    routePath = "mcu/modbus",
//    icon = "SettingsInputComponent",
//    order = 15.0,
//    placement = RoutePlacement(
//        scene = RouteScene(
//            name = "物联网上位机",
//            icon = "Build",
//            order = 0,
//        ),
//    ),
//)
//@Composable
//fun McuModbusScreen() {
//    val remoteService: McuModbusRemoteService = koinInject()
//    val scope = rememberCoroutineScope()
//    var screenState by remember { mutableStateOf(McuModbusScreenState()) }
//
//    fun refresh() {
//        scope.launch {
//            screenState = screenState.copy(
//                isLoading = true,
//                statusMessage = "正在刷新 Modbus 状态...",
//            )
//            runCatching {
//                val deviceInfo = remoteService.getDeviceInfo()
//                val powerLights = remoteService.getPowerLights()
//                screenState.copy(
//                    isLoading = false,
//                    deviceInfo = deviceInfo,
//                    powerLights = powerLights,
//                    statusMessage = "已刷新",
//                    errorMessage = "",
//                )
//            }.onSuccess { next ->
//                screenState = next
//            }.onFailure { throwable ->
//                screenState = screenState.copy(
//                    isLoading = false,
//                    errorMessage = throwable.message ?: "读取失败",
//                    statusMessage = "刷新失败",
//                )
//            }
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        refresh()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//    ) {
//        McuModbusHeader(
//            isLoading = screenState.isLoading,
//            statusMessage = screenState.statusMessage,
//            onRefresh = ::refresh,
//            onApplyLights = {
//                scope.launch {
//                    screenState = screenState.copy(
//                        isSubmitting = true,
//                        statusMessage = "正在写入指示灯...",
//                        errorMessage = "",
//                    )
//                    runCatching {
//                        remoteService.writeIndicatorLights(
//                            faultLightOn = screenState.faultLightOn,
//                            runLightOn = screenState.runLightOn,
//                        )
//                        val powerLights = remoteService.getPowerLights()
//                        screenState.copy(
//                            isSubmitting = false,
//                            powerLights = powerLights,
//                            statusMessage = "已写入指示灯",
//                            errorMessage = "",
//                        )
//                    }.onSuccess { next ->
//                        screenState = next
//                    }.onFailure { throwable ->
//                        screenState = screenState.copy(
//                            isSubmitting = false,
//                            errorMessage = throwable.message ?: "写入失败",
//                            statusMessage = "写入失败",
//                        )
//                    }
//                }
//            },
//            isSubmitting = screenState.isSubmitting,
//        )
//        screenState.errorMessage.takeIf { it.isNotBlank() }?.let { error ->
//            McuNoticeCard(
//                title = "请求失败",
//                body = error,
//                accent = MaterialTheme.colorScheme.errorContainer,
//            )
//        }
//        McuNoticeCard(
//            title = "页面说明",
//            body = "当前 MCU 只保留后端已存在接口支撑的 Modbus 页面。其余页面统一改为暂未开放。",
//        )
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//            McuSectionCard(
//                title = "设备信息",
//                modifier = Modifier.weight(1f),
//            ) {
//                McuInfoRows(screenState.deviceInfo)
//            }
//            McuSectionCard(
//                title = "指示灯写入",
//                modifier = Modifier.width(280.dp),
//            ) {
//                McuToggleRow(
//                    label = "故障灯",
//                    checked = screenState.faultLightOn,
//                    onToggle = {
//                        screenState = screenState.copy(faultLightOn = !screenState.faultLightOn)
//                    },
//                )
//                McuToggleRow(
//                    label = "运行灯",
//                    checked = screenState.runLightOn,
//                    onToggle = {
//                        screenState = screenState.copy(runLightOn = !screenState.runLightOn)
//                    },
//                )
//            }
//        }
//        McuSectionCard(
//            title = "24 路电源灯",
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                screenState.powerLights.chunked(6).forEachIndexed { rowIndex, row ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    ) {
//                        row.forEachIndexed { columnIndex, enabled ->
//                            val index = rowIndex * 6 + columnIndex + 1
//                            McuLightCell(
//                                index = index,
//                                enabled = enabled,
//                                modifier = Modifier.weight(1f),
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//private data class McuModbusScreenState(
//    val deviceInfo: McuModbusDeviceInfoResponse? = null,
//    val powerLights: List<Boolean> = List(24) { false },
//    val faultLightOn: Boolean = false,
//    val runLightOn: Boolean = false,
//    val isLoading: Boolean = false,
//    val isSubmitting: Boolean = false,
//    val statusMessage: String = "等待刷新",
//    val errorMessage: String = "",
//)
//
//@Composable
//private fun McuModbusHeader(
//    isLoading: Boolean,
//    isSubmitting: Boolean,
//    statusMessage: String,
//    onRefresh: () -> Unit,
//    onApplyLights: () -> Unit,
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//            Text(
//                text = "MCU Modbus",
//                style = MaterialTheme.typography.headlineSmall,
//            )
//            Text(
//                text = statusMessage,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//            )
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            McuActionChip(
//                label = if (isLoading) "刷新中..." else "刷新状态",
//                enabled = !isLoading && !isSubmitting,
//                onClick = onRefresh,
//            )
//            McuActionChip(
//                label = if (isSubmitting) "写入中..." else "写入指示灯",
//                enabled = !isLoading && !isSubmitting,
//                onClick = onApplyLights,
//            )
//        }
//    }
//}
//
//@Composable
//private fun McuSectionCard(
//    title: String,
//    modifier: Modifier = Modifier,
//    content: @Composable ColumnScope.() -> Unit,
//) {
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(18.dp),
//        tonalElevation = 2.dp,
//        color = MaterialTheme.colorScheme.surface,
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//            content = {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.titleMedium,
//                )
//                content()
//            },
//        )
//    }
//}
//
//@Composable
//private fun McuNoticeCard(
//    title: String,
//    body: String,
//    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        color = accent,
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(6.dp),
//        ) {
//            Text(text = title, style = MaterialTheme.typography.titleSmall)
//            Text(text = body, style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}
//
//@Composable
//private fun McuInfoRows(
//    deviceInfo: McuModbusDeviceInfoResponse?,
//) {
//    val rows = listOf(
//        "结果" to if (deviceInfo?.success == true) "成功" else "未获取",
//        "串口" to deviceInfo?.portPath.orDash(),
//        "固件" to deviceInfo?.firmwareVersion.orDash(),
//        "CPU" to deviceInfo?.cpuModel.orDash(),
//        "晶振" to deviceInfo?.xtalFrequencyHz?.let { "$it Hz" }.orDash(),
//        "Flash" to deviceInfo?.flashSizeBytes?.let { "$it Bytes" }.orDash(),
//        "MAC" to deviceInfo?.macAddress.orDash(),
//        "更新时间" to deviceInfo?.updatedAt.orDash(),
//        "消息" to deviceInfo?.lastMessage.orDash(),
//    )
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        rows.forEach { (label, value) ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//            ) {
//                Text(
//                    text = label,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                )
//                Text(
//                    text = value,
//                    style = MaterialTheme.typography.bodyMedium,
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun McuToggleRow(
//    label: String,
//    checked: Boolean,
//    onToggle: () -> Unit,
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onToggle)
//            .padding(vertical = 8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Text(text = label, style = MaterialTheme.typography.bodyLarge)
//        Surface(
//            shape = RoundedCornerShape(999.dp),
//            color = if (checked) {
//                MaterialTheme.colorScheme.primary
//            } else {
//                MaterialTheme.colorScheme.surfaceVariant
//            },
//        ) {
//            Text(
//                text = if (checked) "ON" else "OFF",
//                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
//                color = if (checked) {
//                    MaterialTheme.colorScheme.onPrimary
//                } else {
//                    MaterialTheme.colorScheme.onSurfaceVariant
//                },
//            )
//        }
//    }
//}
//
//@Composable
//private fun McuLightCell(
//    index: Int,
//    enabled: Boolean,
//    modifier: Modifier = Modifier,
//) {
//    Surface(
//        modifier = modifier.height(68.dp),
//        shape = RoundedCornerShape(14.dp),
//        color = if (enabled) {
//            MaterialTheme.colorScheme.primaryContainer
//        } else {
//            MaterialTheme.colorScheme.surfaceVariant
//        },
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(androidx.compose.ui.graphics.Color.Transparent),
//            contentAlignment = Alignment.Center,
//        ) {
//            Text(
//                text = "$index\n${if (enabled) "ON" else "OFF"}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = if (enabled) {
//                    MaterialTheme.colorScheme.onPrimaryContainer
//                } else {
//                    MaterialTheme.colorScheme.onSurfaceVariant
//                },
//            )
//        }
//    }
//}
//
//@Composable
//private fun McuActionChip(
//    label: String,
//    enabled: Boolean,
//    onClick: () -> Unit,
//) {
//    Surface(
//        shape = RoundedCornerShape(999.dp),
//        color = if (enabled) {
//            MaterialTheme.colorScheme.primaryContainer
//        } else {
//            MaterialTheme.colorScheme.surfaceVariant
//        },
//        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
//    ) {
//        Text(
//            text = label,
//            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
//            style = MaterialTheme.typography.bodyMedium,
//            color = if (enabled) {
//                MaterialTheme.colorScheme.onPrimaryContainer
//            } else {
//                MaterialTheme.colorScheme.onSurfaceVariant
//            },
//        )
//    }
//}
//
//private fun String?.orDash(): String {
//    return this?.takeIf { it.isNotBlank() } ?: "-"
//}
