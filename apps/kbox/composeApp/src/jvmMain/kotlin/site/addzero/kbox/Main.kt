package site.addzero.kbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.KoinApplication
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.koinConfiguration
import site.addzero.kbox.core.service.KboxSettingsRepository
import site.addzero.kbox.core.service.KboxSyncCoordinator
import site.addzero.kbox.plugins.tools.storagetool.KboxSyncToolState
import site.addzero.kbox.ui.KboxSystemTrayHost
import site.addzero.kbox.ui.MainWindow
import java.awt.SystemTray

fun main() = application {
    var windowVisible by remember { mutableStateOf(true) }
    val traySupported = remember { SystemTray.isSupported() }

    KoinApplication(
        configuration = koinConfiguration<KboxComposeKoinApplication>(),
    ) {
        val koin = remember { KoinPlatform.getKoin() }
        val syncState = remember { koin.get<KboxSyncToolState>() }
        val syncCoordinator = remember { koin.get<KboxSyncCoordinator>() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            syncState.load()
            val settings = withContext(Dispatchers.IO) {
                koin.get<KboxSettingsRepository>().load()
            }
            if (
                settings.syncEnabled &&
                settings.syncStartOnLaunch &&
                settings.ssh.enabled &&
                settings.syncMappings.any { mapping -> mapping.enabled }
            ) {
                runCatching {
                    syncCoordinator.start(settings)
                }.onFailure { error ->
                    System.err.println("KBox sync failed to start on launch: ${error.message}")
                }
            }
            syncState.reloadSettings()
        }

        KboxSystemTrayHost(
            kboxSystemTrayHostProps = KboxSystemTrayHostProps(
                enabled = traySupported,
                windowVisible = windowVisible,
                syncState = syncState
            ),
            kboxSystemTrayHostEvents = KboxSystemTrayHostEvents(
                onToggleWindow = { windowVisible = !windowVisible },
                onStartSync = {
                    scope.launch {
                        syncState.start()
                    }
                },
                onPauseSync = {
                    scope.launch {
                        syncState.pause()
                    }
                },
                onRefreshSync = {
                    scope.launch {
                        syncState.refresh()
                    }
                },
                onReleaseLocalSpace = {
                    scope.launch {
                        syncState.releaseReclaimableLocalCopies()
                    }
                },
                onExit = {
                    scope.launch {
                        runCatching { syncCoordinator.stop() }
                    }
                    exitApplication()
                })
        )

        if (windowVisible) {
            Window(
                onCloseRequest = {
                    if (traySupported) {
                        windowVisible = false
                    } else {
                        exitApplication()
                    }
                },
                title = "KBox",
            ) {
                window.minimumSize = java.awt.Dimension(1440, 920)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                ) {
                    MainWindow()
                }
            }
        }
    }
}
