package site.addzero.remotecompose.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import site.addzero.remotecompose.client.RemoteComposeDemoState
import site.addzero.remotecompose.shared.RemoteComposeLocale
import site.addzero.remotecompose.shared.RemoteComposeTone

@Composable
fun RemoteComposeDemoApp(
    state: RemoteComposeDemoState = koinInject(),
) {
    MaterialTheme(
        colorScheme = remoteComposeColorScheme(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(remoteComposeBackdrop()),
                    .padding(18.dp),
            ) {
                RemoteComposeDemoShell(state = state)
            }
        }
    }
}

@Composable
private fun RemoteComposeDemoShell(
    state: RemoteComposeDemoState,
) {
    val scope = rememberCoroutineScope()
    val strings = rememberShellStrings(state.locale)

    LaunchedEffect(Unit) {
        state.ensureLoaded()
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.width(280.dp).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = strings.sidebarTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = strings.sidebarSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { scope.launch { state.refresh() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.reload)
                    }
                    OutlinedButton(
                        onClick = { scope.launch { state.toggleLocale() } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.localeButton)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = toneColor(state.statusTone).copy(alpha = 0.18f),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = strings.runtimeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = state.baseUrl,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = strings.screenCatalog,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.screens.forEach { screen ->
                        val selected = screen.id == state.selectedScreenId
                        Surface(
                            onClick = {
                                scope.launch {
                                    state.selectScreen(screen.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            tonalElevation = if (selected) 4.dp else 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                )
                                Text(
                                    text = screen.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (screen.badge.isNotBlank()) {
                                    Text(
                                        text = screen.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = toneColor(screen.tone),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DemoHeader(
                    state = state,
                    strings = strings,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f))
                        .padding(20.dp),
                ) {
                    when {
                        state.isLoading && state.currentScreen == null -> LoadingPlaceholder(text = strings.loading)
                        state.currentScreen != null -> RemoteComposeRenderer(
                            screen = state.currentScreen,
                            onAction = { action ->
                                scope.launch {
                                    state.handleAction(action)
                                }
                            },
                        )
                        else -> EmptyPlaceholder(text = strings.emptyState)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.widthIn(min = 320.dp, max = 360.dp).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ),
            shape = RoundedCornerShape(26.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = strings.inspectorTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = strings.inspectorSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0A1220))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        Text(
                            text = if (state.inspectorJson.isBlank()) strings.noPayload else state.inspectorJson,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFDDE7FF),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoHeader(
    state: RemoteComposeDemoState,
    strings: ShellStrings,
) {
    val payload = state.currentScreen
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = payload?.title ?: strings.headerFallbackTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = payload?.subtitle ?: strings.headerFallbackSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(18.dp),
                    strokeWidth = 2.4.dp,
                )
                Text(
                    text = strings.loading,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class ShellStrings(
    val sidebarTitle: String,
    val sidebarSubtitle: String,
    val reload: String,
    val localeButton: String,
    val runtimeLabel: String,
    val screenCatalog: String,
    val inspectorTitle: String,
    val inspectorSubtitle: String,
    val noPayload: String,
    val loading: String,
    val emptyState: String,
    val headerFallbackTitle: String,
    val headerFallbackSubtitle: String,
)

@Composable
private fun rememberShellStrings(locale: RemoteComposeLocale): ShellStrings {
    return if (locale == RemoteComposeLocale.ZH_CN) {
        ShellStrings(
            sidebarTitle = "Remote Compose 控制台",
            sidebarSubtitle = "左侧是服务端返回的 screen 列表，中间是客户端解释执行后的 Compose 结果。",
            reload = "重新拉取 Schema",
            localeButton = "切换到 English",
            runtimeLabel = "当前服务端地址",
            screenCatalog = "服务端 screen 目录",
            inspectorTitle = "Schema Inspector",
            inspectorSubtitle = "右侧看到的是当前 payload 的完整 JSON，方便确认服务端到底下发了什么。",
            noPayload = "// 还没有收到 payload",
            loading = "正在同步",
            emptyState = "服务端还没有返回可展示的 screen。",
            headerFallbackTitle = "等待服务端返回 screen",
            headerFallbackSubtitle = "切换左侧目录或重试拉取。",
        )
    } else {
        ShellStrings(
            sidebarTitle = "Remote Compose Console",
            sidebarSubtitle = "The sidebar is server data, the middle area is client-side Compose interpretation.",
            reload = "Reload Schema",
            localeButton = "Switch to 中文",
            runtimeLabel = "Current server endpoint",
            screenCatalog = "Server screen catalog",
            inspectorTitle = "Schema Inspector",
            inspectorSubtitle = "The right panel shows the full payload JSON so we can verify what the server really sent.",
            noPayload = "// no payload yet",
            loading = "Syncing",
            emptyState = "The server has not returned a renderable screen yet.",
            headerFallbackTitle = "Waiting for a screen payload",
            headerFallbackSubtitle = "Pick a screen from the sidebar or reload the schema.",
        )
    }
}

private fun remoteComposeColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFF8DC6FF),
        onPrimary = Color(0xFF03213D),
        secondary = Color(0xFF88D5D1),
        onSecondary = Color(0xFF072B2A),
        tertiary = Color(0xFFBBD1FF),
        background = Color(0xFF07111D),
        surface = Color(0xFF0C1727),
        surfaceVariant = Color(0xFF132236),
        onBackground = Color(0xFFE8F0FF),
        onSurface = Color(0xFFE8F0FF),
        onSurfaceVariant = Color(0xFF98A9C4),
        error = Color(0xFFFF8C88),
    )
}

private fun remoteComposeBackdrop(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF08101C),
            Color(0xFF0A1730),
            Color(0xFF102943),
        ),
    )
}

@Composable
private fun LoadingPlaceholder(
    text: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyPlaceholder(
    text: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun toneColor(tone: RemoteComposeTone): Color {
    return when (tone) {
        RemoteComposeTone.NEUTRAL -> MaterialTheme.colorScheme.secondary
        RemoteComposeTone.ACCENT -> MaterialTheme.colorScheme.primary
        RemoteComposeTone.SUCCESS -> Color(0xFF72D9A1)
        RemoteComposeTone.WARNING -> Color(0xFFFFCC7A)
        RemoteComposeTone.DANGER -> MaterialTheme.colorScheme.error
        RemoteComposeTone.INFO -> Color(0xFF8DB8FF)
    }
}
