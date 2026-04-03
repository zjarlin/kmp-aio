package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.McuEventKind
import site.addzero.kcloud.plugins.mcuconsole.workbench.*
import site.addzero.kcloud.plugins.mcuconsole.workbench.cupertino.*

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
    val state: McuConsoleWorkbenchState = koinInject()
    val workbenchState = rememberMcuWorkbenchState(state)
    val runAction = rememberMcuActionRunner()

    McuCupertinoScene {
        McuWorkbenchFrame(
            state = workbenchState,
            actions = {
                McuCupertinoSecondaryButton(
                    text = "刷新资源",
                    onClick = {
                        runAction {
                            workbenchState.refreshPorts()
                            workbenchState.refreshFlashProfiles()
                            workbenchState.refreshRuntimeBundles()
                            workbenchState.refreshRuntimeStatus()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "刷内置运行时",
                    enabled = workbenchState.session.isOpen && workbenchState.selectedRuntimeBundle != null,
                    onClick = {
                        runAction {
                            workbenchState.ensureRuntime(forceReflash = true)
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "在线下载",
                    enabled = workbenchState.canDownloadFirmwareOnline,
                    onClick = {
                        runAction {
                            workbenchState.downloadFirmwareOnline(flashAfterDownload = false)
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "下载并烧录",
                    enabled = workbenchState.canDownloadFirmwareOnline,
                    onClick = {
                        runAction {
                            workbenchState.downloadFirmwareOnline(flashAfterDownload = true)
                        }
                    },
                )
                McuCupertinoPrimaryButton(
                    text = "开始烧录",
                    enabled = workbenchState.canStartFlash,
                    onClick = {
                        runAction {
                            workbenchState.startFlash()
                        }
                    },
                )
                McuCupertinoSecondaryButton(
                    text = "刷新状态",
                    onClick = {
                        runAction {
                            workbenchState.refreshFlashStatus()
                            workbenchState.refreshRuntimeStatus()
                            workbenchState.loadRecentEvents()
                        }
                    },
                )
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
                        bundles = workbenchState.runtimeBundles,
                        selectedBundleId = workbenchState.selectedRuntimeBundleId,
                        onSelect = { bundleId -> workbenchState.selectRuntimeBundle(bundleId) },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                    )
                    McuFlashProfileBrowser(
                        profiles = workbenchState.flashProfiles,
                        selectedProfileId = workbenchState.selectedFlashProfileId,
                        onSelect = { profileId -> workbenchState.selectFlashProfile(profileId) },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                    )
                    McuCupertinoField(
                        value = workbenchState.firmwarePathText,
                        onValueChange = { workbenchState.firmwarePathText = it },
                        label = workbenchState.selectedFlashProfile?.artifactLabel ?: "firmware.bin",
                        supportingText = workbenchState.runtimeStatus.artifactPath
                            ?: workbenchState.selectedFlashProfile?.artifactHint,
                    )
                    if (workbenchState.selectedFlashProfile?.supportsOnlineDownload == true || workbenchState.firmwareDownloadUrlText.isNotBlank()) {
                        McuCupertinoField(
                            value = workbenchState.firmwareDownloadUrlText,
                            onValueChange = { workbenchState.firmwareDownloadUrlText = it },
                            label = "downloadUrl",
                            supportingText = workbenchState.selectedFlashProfile?.downloadUrlHint
                                ?: workbenchState.selectedFlashProfile?.defaultDownloadUrl,
                            singleLine = false,
                            maxLines = 4,
                        )
                    }
                    McuCupertinoField(
                        value = workbenchState.baudRateText,
                        onValueChange = { workbenchState.baudRateText = it },
                        label = "baudRate",
                    )
                    if (workbenchState.selectedFlashProfile?.supportsCommandOverride == true) {
                        McuCupertinoField(
                            value = workbenchState.flashCommandTemplateText,
                            onValueChange = { workbenchState.flashCommandTemplateText = it },
                            label = "commandTemplate",
                            supportingText = "{firmwarePath} {portPath} {baudRate} {firmwareName} {firmwareDir} {profileId} {runtimeKind} {mcuFamily}",
                            singleLine = false,
                            maxLines = 4,
                        )
                    }
                }

                McuPanel(
                    title = "任务状态",
                    modifier = Modifier.width(360.dp).fillMaxHeight(),
                ) {
                    McuCupertinoSummarySection(
                        rows = listOf(
                            "Bundle" to (workbenchState.runtimeStatus.bundleTitle ?: workbenchState.selectedRuntimeBundle?.title.orEmpty()),
                            "运行时" to workbenchState.runtimeStatus.state.name,
                            "FlashProfile" to (workbenchState.runtimeStatus.defaultFlashProfileId ?: workbenchState.selectedFlashProfile?.id.orEmpty()),
                            "目标串口" to (workbenchState.selectedPortPath ?: workbenchState.session.portPath.orEmpty()),
                            "烧录状态" to workbenchState.flashStatus.state.name,
                            "进度" to "${workbenchState.flashStatus.bytesSent} / ${workbenchState.flashStatus.totalBytes}",
                            "固件" to (workbenchState.flashStatus.firmwarePath ?: workbenchState.runtimeStatus.artifactPath.orEmpty()),
                            "下载源" to workbenchState.firmwareDownloadUrlText,
                            "命令" to workbenchState.flashStatus.commandPreview.orEmpty(),
                            "消息" to (workbenchState.flashStatus.lastMessage ?: workbenchState.runtimeStatus.lastMessage.orEmpty()),
                        ),
                    )
                }

                McuPanel(
                    title = "烧录事件",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    McuEventFeed(
                        events = workbenchState.events.filter { event ->
                            event.kind == McuEventKind.FLASH || event.kind == McuEventKind.ERROR
                        }.takeLast(120),
                    )
                }
            }
        }
    }
}
