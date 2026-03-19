package site.addzero.vibepocket

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.addzero.media.playlist.player.ProvidePlaylistPlayerHost
import site.addzero.vibepocket.music.hasCompletedVibePocketSetup
import site.addzero.vibepocket.music.loadSunoRuntimeConfig
import site.addzero.vibepocket.feature.VibePocketFeatureRegistry
import site.addzero.vibepocket.feature.VibePocketFeatureSidebar
import site.addzero.vibepocket.screens.PlaceholderScreen
import site.addzero.vibepocket.screens.WelcomeScreenWrapper
import site.addzero.vibepocket.ui.VibeGlassAppTheme

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
    var expandedMenuIds by remember { mutableStateOf(featureRegistry.defaultExpandedIds) }

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
        expandedMenuIds = expandedMenuIds + featureRegistry.defaultExpandedIds
        isStartupReady = true
    }

    if (!isStartupReady) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "正在读取本地配置...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }

    if (!isSetupDone) {
        WelcomeScreenWrapper(
            onSetupComplete = { _, _ ->
                isSetupDone = true
                selectedMenuId = featureRegistry.defaultLeafId
                expandedMenuIds = featureRegistry.defaultExpandedIds
            },
        )
    } else {
        MainScreen(
            featureRegistry = featureRegistry,
            selectedMenuId = selectedMenuId,
            expandedMenuIds = expandedMenuIds,
            onLeafClick = { menuId ->
                val normalized = featureRegistry.normalizeMenuId(menuId)
                selectedMenuId = normalized
                expandedMenuIds = expandedMenuIds + featureRegistry.ancestorIdsFor(normalized)
            },
            onGroupToggle = { menuId ->
                val normalized = featureRegistry.normalizeMenuId(menuId)
                expandedMenuIds = expandedMenuIds.toMutableSet().apply {
                    if (!add(normalized)) {
                        remove(normalized)
                    }
                }
            },
        )
    }
}

@Composable
private fun MainScreen(
    featureRegistry: VibePocketFeatureRegistry,
    selectedMenuId: String,
    expandedMenuIds: Set<String>,
    onLeafClick: (String) -> Unit,
    onGroupToggle: (String) -> Unit,
) {
    val selectedNode = featureRegistry.findLeaf(selectedMenuId)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedCard(
                modifier = Modifier
                    .width(232.dp)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    VibePocketFeatureSidebar(
                        nodes = featureRegistry.menuTree,
                        selectedId = selectedNode?.id.orEmpty(),
                        expandedIds = expandedMenuIds,
                        onLeafClick = onLeafClick,
                        onGroupToggle = onGroupToggle,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    ) {
                        val content = selectedNode?.entry?.content
                        if (content == null) {
                            PlaceholderScreen("🧩", "这个功能页面暂时还没有挂载内容。")
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 2.dp),
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }
}
