package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.appsidebar.RenderAppSidebarScaffold

@Composable
fun RenderSidebarShowcaseShell(
    modifier: Modifier = Modifier,
    state: SidebarShowcaseState = koinInject(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = SidebarShowcaseTokens.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            SidebarShowcaseBackdrop()

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SidebarShowcaseSceneSwitcher(
                    state = state,
                )

                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    RenderAppSidebarScaffold(
                        shell = state.currentShell,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarShowcaseSceneSwitcher(
    state: SidebarShowcaseState,
) {
    Surface(
        color = SidebarShowcaseTokens.panelBackground,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Sidebar / Workbench Showcase",
                    style = MaterialTheme.typography.titleLarge,
                    color = SidebarShowcaseTokens.textPrimary,
                )
                Text(
                    text = "场景树来自 Koin 注入的 Screen 集合；App 层只负责启动 Koin 和挂一个宿主。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SidebarShowcaseTokens.textMuted,
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.scenes.forEach { scene ->
                    val selected = scene.id == state.selectedSceneId
                    if (selected) {
                        Button(
                            onClick = { state.selectScene(scene.id) },
                            shape = CircleShape,
                        ) {
                            Text(scene.name)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { state.selectScene(scene.id) },
                            shape = CircleShape,
                        ) {
                            Text(scene.name)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.activeSlot.config.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SidebarShowcaseTokens.textFaint,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SidebarShowcaseBackdrop() {
    Box(
        modifier = Modifier.matchParentSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF07111F),
                    Color(0xFF060B14),
                    Color(0xFF050912),
                ),
            ),
        ),
    )
    Box(
        modifier = Modifier.align(Alignment.TopStart)
            .padding(start = 72.dp, top = 18.dp)
            .width(340.dp)
            .height(340.dp)
            .blur(48.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x33289BFF),
                        Color.Transparent,
                    ),
                ),
                shape = CircleShape,
            ),
    )
    Box(
        modifier = Modifier.align(Alignment.BottomEnd)
            .padding(end = 52.dp, bottom = 24.dp)
            .width(380.dp)
            .height(280.dp)
            .blur(42.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2238DDB8),
                        Color.Transparent,
                    ),
                ),
                shape = RoundedCornerShape(48.dp),
            ),
    )
}

private object SidebarShowcaseTokens {
    val background = Color(0xFF060B14)
    val panelBackground = Color(0xA0141D2D)
    val textPrimary = Color(0xFFE8EEF8)
    val textMuted = Color(0xFF9EB0C7)
    val textFaint = Color(0xFF7C8DA4)
}
