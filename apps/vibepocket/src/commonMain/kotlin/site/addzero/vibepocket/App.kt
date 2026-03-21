package site.addzero.vibepocket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.AppSidebarScaffold
import site.addzero.liquidglass.liquidGlassSurface
import site.addzero.media.playlist.player.ProvidePlaylistPlayerHost
import site.addzero.vibepocket.feature.VibePocketFeatureRegistry
import site.addzero.vibepocket.feature.VibePocketFeatureSidebar
import site.addzero.vibepocket.music.hasCompletedVibePocketSetup
import site.addzero.vibepocket.music.loadSunoRuntimeConfig
import site.addzero.vibepocket.screens.PlaceholderScreen
import site.addzero.vibepocket.screens.WelcomeScreenWrapper
import site.addzero.vibepocket.ui.StudioSectionCard
import site.addzero.vibepocket.ui.VibeGlassAppTheme
import site.addzero.vibepocket.ui.VibePocketLiquidGlass
import site.addzero.vibepocket.ui.VibePocketLiquidGlassRoot

@Composable
@Preview
fun App() {
    VibeGlassAppTheme {
        ProvidePlaylistPlayerHost {
            VibePocketAppRoot()
        }
    }
}

@Composable
private fun VibePocketAppRoot() {
    val featureRegistry = remember { VibePocketFeatureRegistry() }

    var isStartupReady by remember { mutableStateOf(false) }
    var isSetupDone by remember { mutableStateOf(false) }
    var selectedMenuId by remember { mutableStateOf(featureRegistry.defaultLeafId) }

    LaunchedEffect(featureRegistry.defaultLeafId) {
        val runtimeConfig = try {
            loadSunoRuntimeConfig()
        } catch (_: Exception) {
            null
        }
        val setupCompleted = try {
            hasCompletedVibePocketSetup()
        } catch (_: Exception) {
            false
        }
        val hasPersistedConfig = runtimeConfig?.apiToken?.isNotBlank() == true ||
            runtimeConfig?.baseUrl?.takeIf { it != site.addzero.vibepocket.api.suno.SunoApiClient.DEFAULT_BASE_URL } != null

        isSetupDone = setupCompleted || hasPersistedConfig
        selectedMenuId = featureRegistry.normalizeMenuId(selectedMenuId)
        isStartupReady = true
    }

    if (!isStartupReady) {
        VibePocketLiquidGlassRoot {
            StartupLoadingScreen()
        }
        return
    }

    if (!isSetupDone) {
        VibePocketLiquidGlassRoot {
            WelcomeScreenWrapper(
                onSetupComplete = { _, _ ->
                    isSetupDone = true
                    selectedMenuId = featureRegistry.defaultLeafId
                },
            )
        }
    } else {
        VibePocketLiquidGlassRoot {
            MainScreen(
                featureRegistry = featureRegistry,
                selectedMenuId = selectedMenuId,
                onLeafClick = { menuId ->
                    selectedMenuId = featureRegistry.normalizeMenuId(menuId)
                },
            )
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        StudioSectionCard(
            modifier = Modifier.width(320.dp),
            title = "正在启动 Vibepocket",
            subtitle = "先把本地配置和工作台状态恢复好。",
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.4.dp,
                )
                Text(
                    text = "正在读取本地配置...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MainScreen(
    featureRegistry: VibePocketFeatureRegistry,
    selectedMenuId: String,
    onLeafClick: (String) -> Unit,
) {
    val selectedNode = featureRegistry.findLeaf(selectedMenuId)

    AppSidebarScaffold(
        modifier = Modifier.workspaceFrame(),
        defaultSidebarRatio = 0.18f,
        minSidebarWidth = 252.dp,
        maxSidebarWidth = 340.dp,
        sidebar = {
            VibePocketFeatureSidebar(
                nodes = featureRegistry.menuTree,
                selectedId = selectedNode?.id.orEmpty(),
                onLeafClick = onLeafClick,
                modifier = Modifier.fillMaxSize(),
            )
        },
        content = {
            Box(
                modifier = Modifier.fillMaxSize().liquidGlassSurface(VibePocketLiquidGlass.workspaceSpec),
            ) {
                val content = selectedNode?.entry?.content
                if (content == null) {
                    PlaceholderScreen("🧩", "这个功能页面暂时还没有挂载内容。")
                } else {
                    Box(
                        modifier = Modifier.contentPaddingFrame(),
                    ) {
                        content()
                    }
                }
            }
        },
    )
}

/** 主工作台留白：让整个玻璃壳体和窗口边缘拉开呼吸距离。 */
private fun Modifier.workspaceFrame(): Modifier {
    return fillMaxSize().padding(16.dp)
}

/** 内容内边距：给业务页面留安全区，避免控件直接贴到玻璃边缘。 */
private fun Modifier.contentPaddingFrame(): Modifier {
    return fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)
}
