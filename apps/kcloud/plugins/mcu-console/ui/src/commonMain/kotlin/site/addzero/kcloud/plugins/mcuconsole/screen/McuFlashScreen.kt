package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.button.AddIconButton
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind

@Route(
    value = "开发工具",
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "设备",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuFlashScreen() {
    val viewModel: McuFlashViewModel = koinViewModel()
    val state = rememberMcuWorkbenchState(viewModel.state)
    val runAction = rememberMcuActionRunner()

    McuWorkbenchFrame(
        state = state,
        actions = {
            AddIconButton(
                text = "刷新资源",
                imageVector = Icons.Default.Search,
            ) {
                runAction {
                    state.refreshPorts()
                    state.refreshFlashProfiles()
                    state.refreshRuntimeBundles()
                    state.refreshRuntimeStatus()
                }
            }
            AddIconButton(
                text = "刷内置运行时",
                imageVector = Icons.Default.Build,
                enabled = state.session.isOpen && state.selectedRuntimeBundle != null,
            ) {
                runAction {
                    state.ensureRuntime(forceReflash = true)
                }
            }
            AddIconButton(
                text = "在线下载",
                imageVector = Icons.Default.Search,
                enabled = state.canDownloadFirmwareOnline,
            ) {
                runAction {
                    state.downloadFirmwareOnline(flashAfterDownload = false)
                }
            }
            AddIconButton(
                text = "在线下载并烧录",
                imageVector = Icons.Default.Upload,
                enabled = state.canDownloadFirmwareOnline,
            ) {
                runAction {
                    state.downloadFirmwareOnline(flashAfterDownload = true)
                }
            }
            AddIconButton(
                text = "开始烧录",
                imageVector = Icons.Default.Upload,
                enabled = state.canStartFlash,
            ) {
                runAction {
                    state.startFlash()
                }
            }
            AddIconButton(
                text = "刷新状态",
                imageVector = Icons.Default.Refresh,
            ) {
                runAction {
                    state.refreshFlashStatus()
                    state.refreshRuntimeStatus()
                    state.loadRecentEvents()
                }
            }
        },
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
                if (state.selectedFlashProfile?.supportsOnlineDownload == true || state.firmwareDownloadUrlText.isNotBlank()) {
                    McuCompactInput(
                        value = state.firmwareDownloadUrlText,
                        onValueChange = { state.firmwareDownloadUrlText = it },
                        label = "downloadUrl",
                        supportingText = state.selectedFlashProfile?.downloadUrlHint
                            ?: state.selectedFlashProfile?.defaultDownloadUrl,
                        singleLine = false,
                    )
                }
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
                        "下载源" to state.firmwareDownloadUrlText,
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
