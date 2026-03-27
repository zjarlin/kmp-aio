package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
    val isAdminShell = state.currentShell == site.addzero.appsidebar.AppSidebarScaffoldShell.AdminWorkbench
    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (isAdminShell) {
            SidebarShowcaseTokens.adminBackground
        } else {
            SidebarShowcaseTokens.background
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (!isAdminShell) {
                SidebarShowcaseBackdrop()
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
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
    val adminBackground = Color(0xFFF2F5F9)
}
