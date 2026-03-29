package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind

@Route(
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            id = "device",
            name = "设备",
            icon = "Build",
            order = 0,
        ),
        menuPath = ["开发工具"],
        defaultInScene = true,
    ),
)
@Composable
fun McuFlashScreen() {
    val state = rememberMcuWorkbenchState()
    val scope = rememberCoroutineScope()

    McuWorkbenchFrame(
        state = state,
        actions = listOf(
            McuToolbarAction("刷新资源", Icons.Default.Search) {
                scope.launch {
                    state.refreshPorts()
                    state.refreshFlashProfiles()
                    state.refreshRuntimeBundles()
                    state.refreshRuntimeStatus()
                }
            },
            McuToolbarAction(
                label = "刷内置运行时",
                icon = Icons.Default.Build,
                enabled = state.session.isOpen && state.selectedRuntimeBundle != null,
            ) {
                scope.launch {
                    state.ensureRuntime(forceReflash = true)
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
                    state.refreshRuntimeStatus()
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
                title = "运行时与能力包",
                modifier = Modifier.width(380.dp).fillMaxHeight(),
            ) {
                McuRuntimeBundleBrowser(
                    bundles = state.runtimeBundles,
                    selectedBundleId = state.selectedRuntimeBundleId,
                    onSelect = { bundleId -> state.selectRuntimeBundle(bundleId) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
                McuFlashProfileBrowser(
                    profiles = state.flashProfiles,
                    selectedProfileId = state.selectedFlashProfileId,
                    onSelect = { profileId -> state.selectFlashProfile(profileId) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
                McuCompactInput(
                    value = state.firmwarePathText,
                    onValueChange = { state.firmwarePathText = it },
                    label = state.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                    supportingText = state.runtimeStatus.artifactPath
                        ?: state.selectedFlashProfile?.artifactHint,
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
            }

            McuPanel(
                title = "任务状态",
                modifier = Modifier.width(360.dp).fillMaxHeight(),
            ) {
                McuSummaryTable(
                    rows = listOf(
                        "Bundle" to (state.runtimeStatus.bundleTitle ?: state.selectedRuntimeBundle?.title.orEmpty()),
                        "运行时" to state.runtimeStatus.state.name,
                        "FlashProfile" to (state.runtimeStatus.defaultFlashProfileId ?: state.selectedFlashProfile?.id.orEmpty()),
                        "目标串口" to (state.selectedPortPath ?: state.session.portPath.orEmpty()),
                        "烧录状态" to state.flashStatus.state.name,
                        "进度" to "${state.flashStatus.bytesSent} / ${state.flashStatus.totalBytes}",
                        "固件" to (state.flashStatus.firmwarePath ?: state.runtimeStatus.artifactPath.orEmpty()),
                        "命令" to state.flashStatus.commandPreview.orEmpty(),
                        "消息" to (state.flashStatus.lastMessage ?: state.runtimeStatus.lastMessage.orEmpty()),
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
