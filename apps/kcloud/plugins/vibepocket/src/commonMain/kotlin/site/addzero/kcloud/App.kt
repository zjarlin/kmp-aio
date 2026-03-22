//package site.addzero.vibepocket
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import org.koin.compose.koinInject
//import site.addzero.liquidglass.LiquidGlassAppTheme
//import site.addzero.liquidglass.LiquidGlassWorkbenchRoot
//import site.addzero.media.playlist.player.ProvidePlaylistPlayerHost
//import site.addzero.vibepocket.music.hasCompletedVibePocketSetup
//import site.addzero.vibepocket.music.loadSunoRuntimeConfig
//import site.addzero.vibepocket.render.VibePocketShellState
//import site.addzero.vibepocket.screens.WelcomeScreenWrapper
//import site.addzero.vibepocket.ui.StudioSectionCard
//import site.addzero.workbenchshell.RenderWorkbenchScaffold
//
//@Composable
//fun App() {
//    LiquidGlassAppTheme {
//        ProvidePlaylistPlayerHost {
//            VibePocketAppRoot()
//        }
//    }
//}
//
//@Composable
//private fun VibePocketAppRoot() {
//    val shellState: VibePocketShellState = koinInject()
//    var isStartupReady by remember { mutableStateOf(false) }
//    var isSetupDone by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        val runtimeConfig = try {
//            loadSunoRuntimeConfig()
//        } catch (_: Exception) {
//            null
//        }
//        val setupCompleted = try {
//            hasCompletedVibePocketSetup()
//        } catch (_: Exception) {
//            false
//        }
//        val hasPersistedConfig = runtimeConfig?.apiToken?.isNotBlank() == true ||
//            runtimeConfig?.baseUrl?.takeIf { it != site.addzero.vibepocket.api.suno.SunoApiClient.DEFAULT_BASE_URL } != null
//
//        isSetupDone = setupCompleted || hasPersistedConfig
//        isStartupReady = true
//    }
//
//    if (!isStartupReady) {
//        VibePocketWorkbenchRoot {
//            StartupLoadingScreen()
//        }
//        return
//    }
//
//    if (!isSetupDone) {
//        VibePocketWorkbenchRoot {
//            WelcomeScreenWrapper(
//                onSetupComplete = { _, _ ->
//                    isSetupDone = true
//                    shellState.selectDefaultScreen()
//                },
//            )
//        }
//    } else {
//        VibePocketWorkbenchRoot {
//            MainScreen()
//        }
//    }
//}
//
//@Composable
//private fun VibePocketWorkbenchRoot(
//    content: @Composable BoxScope.() -> Unit,
//) {
//    LiquidGlassWorkbenchRoot(
//        modifier = Modifier.fillMaxSize(),
//        content = content,
//    )
//}
//
//@Composable
//private fun StartupLoadingScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center,
//    ) {
//        StudioSectionCard(
//            modifier = Modifier.width(320.dp),
//            title = "正在启动 Vibepocket",
//            subtitle = "先把本地配置和工作台状态恢复好。",
//        ) {
//            androidx.compose.foundation.layout.Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                CircularProgressIndicator(
//                    modifier = Modifier.width(20.dp),
//                    strokeWidth = 2.4.dp,
//                )
//                Text(
//                    text = "正在读取本地配置...",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MainScreen() {
//    RenderWorkbenchScaffold(
//        modifier = Modifier.fillMaxSize(),
//        defaultSidebarRatio = 0.18f,
//        contentHeaderScrollable = false,
//        minSidebarWidth = 252.dp,
//        maxSidebarWidth = 340.dp,
//    )
//}
