package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind

@Route(
    value = "开发工具",
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
)
@Composable
fun McuFlashScreen() {
    val state = rememberMcuWorkbenchState()
    val scope = rememberCoroutineScope()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("刷新串口", Icons.Default.Search) {
                scope.launch {
                    state.refreshPorts()
                    state.refreshFlashProfiles()
                }
            },
            McuToolbarAction(
                label = "开始烧录",
                icon = Icons.Default.Upload,
                enabled = state.canStartFlash,
            ) {
                scope.launch {
                    state.startFlash()
                }
            },
            McuToolbarAction("刷新状态", Icons.Default.Refresh) {
                scope.launch {
                    state.refreshFlashStatus()
                    state.loadRecentEvents()
                }
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            McuPanel(
                title = "烧录能力包",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            ) {
                McuFlashProfileBrowser(
                    profiles = state.flashProfiles,
                    selectedProfileId = state.selectedFlashProfileId,
                    onSelect = { profileId -> state.selectFlashProfile(profileId) },
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                )
                McuCompactInput(
                    value = state.firmwarePathText,
                    onValueChange = { state.firmwarePathText = it },
                    label = state.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                    supportingText = state.selectedFlashProfile?.artifactHint,
                )
                McuCompactInput(
                    value = state.baudRateText,
                    onValueChange = { state.baudRateText = it },
                    label = "baudRate",
                )
                if (state.selectedFlashProfile?.supportsCommandOverride == true) {
                    McuCompactInput(
                        value = state.flashCommandTemplateText,
                        onValueChange = { state.flashCommandTemplateText = it },
                        label = "commandTemplate",
                        supportingText = "{firmwarePath} {portPath} {baudRate} {firmwareName} {firmwareDir} {profileId} {runtimeKind} {mcuFamily}",
                        singleLine = false,
                    )
                }
                McuSummaryTable(
                    rows = listOf(
                        "能力包" to (state.selectedFlashProfile?.title.orEmpty()),
                        "运行时" to (state.selectedFlashProfile?.runtimeKind?.name.orEmpty()),
                        "策略" to (state.selectedFlashProfile?.strategyKind?.name.orEmpty()),
                        "串口要求" to if (state.selectedFlashProfile?.requiresPort == true) "需要" else "可选",
                        "目标串口" to (state.selectedPortPath ?: state.session.portPath.orEmpty()),
                        "烧录状态" to state.flashStatus.state.name,
                        "进度" to "${state.flashStatus.bytesSent} / ${state.flashStatus.totalBytes}",
                        "固件" to state.flashStatus.firmwarePath.orEmpty(),
                        "命令" to state.flashStatus.commandPreview.orEmpty(),
                        "消息" to state.flashStatus.lastMessage.orEmpty(),
                    ),
                )
            }

            McuPanel(
                title = "烧录事件",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                McuEventFeed(
                    events = state.events.filter { event ->
                        event.kind == McuEventKind.FLASH || event.kind == McuEventKind.ERROR
                    }.takeLast(120),
                )
            }
        }
    }
}
